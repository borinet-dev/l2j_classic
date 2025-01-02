package ai.areas.MithrilMine;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.SpawnTable;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerMineMania;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

import ai.AbstractNpcAI;

public class MithrilMine extends AbstractNpcAI
{
	public static final ZoneType ZONE = ZoneManager.getInstance().getZoneByName("MithrilMine_No_Bookmark");
	
	private ScheduledFuture<?> _despawn = null;
	protected Set<Spawn> _boxs = ConcurrentHashMap.newKeySet();
	
	private final int despawn_delay = (Config.MITHRIL_MINE_DESPAWN_DELAY) * 60 * 1000;
	private final int boss_delay = (Config.MITHRIL_MINE_BOSS_DELAY) * 60 * 1000;
	private final int box_delay = (Config.MITHRIL_MINE_BOX_DELAY) * 60;
	
	// NPCs
	private static final int GRAVE_ROBBER_SUMMONER = 22678; // 도굴꾼 소환사
	private static final int GRAVE_ROBBER_MAGICIAN = 22679; // 도굴꾼 마법사
	private static final int[] SUMMONER_MINIONS =
	{
		22683, // 어둠의 소환수 단검 무기 내성
		22684, // 어둠의 소환수 둔기류 무기 내성
	};
	private static final int[] MAGICIAN_MINIONS =
	{
		22685, // 어둠의 소환수 격투 무기 내성
		22686, // 어둠의 소환수 창 무기 내성
	};
	
	// @formatter:off
	private static final int[][] SPAWNS =
	{
		{176143, -178647, -3720}, {176738, -184832, -3720}, {176418, -184664, -3720},
		{175969, -184712, -3720}, {175613, -185070, -3720}, {175625, -185595, -3720},
		{176090, -185846, -3720}, {176552, -185536, -3720}, {176809, -185201, -3720},
		{177121, -184978, -3720}, {176959, -184627, -3720}, {176790, -184431, -3720},
		{176462, -184279, -3720}, {176202, -184236, -3720}, {175707, -184399, -3720},
		{175360, -184772, -3720}, {175258, -185315, -3720}, {175474, -185706, -3720},
		{175654, -186081, -3720}, {176047, -186284, -3720}, {176528, -186121, -3720},
		{176858, -185863, -3720}, {177160, -185418, -3720}, {176908, -185531, -3720},
		{176440, -185887, -3720}, {175975, -186053, -3720}, {175776, -185866, -3720},
		{175398, -185200, -3720}, {175689, -184731, -3720}, {176165, -184496, -3720},
		{176614, -184559, -3720}, {176946, -184946, -3720}, {176983, -185329, -3720},
		{177062, -185638, -3720}, {176762, -186102, -3720}, {175885, -185688, -3720}
	};

    private static final int[] ITEM_DROP_1 =
    {
        1864, 1865, 1866, 1867, 1868, 1869, 1870, 1871, 1872, 1873,
        1874, 1875, 1876, 1877, 1878, 1879, 1880, 1881, 1882, 1883,
        1884, 1885, 1886, 1887, 1888, 1889, 1890, 1891, 1892, 1893,
        1894, 1895, 4039, 4040, 4041, 4042, 4043, 4044, 4045, 4046, 4047, 4048
    };
    private static final int[] ITEM_DROP_2 =
    {
        5549, 5550, 5551, 5552, 5553, 5554, 9628, 9629, 9630, 9631, 49756
    };
    private static final int[] ITEM_DROP_최상급 =
    {
        36550, 36555, 36560
    };
    private static final int[] ITEM_DROP_아포칼립스조각 =
    {
        19203,19204,19205,19206,19207,19208,19209,19211,19212,19213
    };
    // @formatter:on
	
	private static final int WEAPON_ID = 32773;
	
	// 몬스터의 ObjectId와 현재 공격 중인 플레이어의 ObjectId를 저장하는 Map
	private final Map<Integer, Integer> attackingPlayers = new ConcurrentHashMap<>();
	
	private MithrilMine()
	{
		addAttackId(31468);
		addKillId(31468, 36706);
		addSpawnId(GRAVE_ROBBER_SUMMONER, GRAVE_ROBBER_MAGICIAN);
		initialize();
	}
	
	private void initialize()
	{
		for (Creature creature : ZONE.getCharactersInside())
		{
			if (creature.isRaid() && !creature.isDead())
			{
				creature.deleteMe();
			}
			if (creature.getId() == 31468)
			{
				creature.decayMe();
			}
		}
		cancelQuestTimers("SPAWN_RAID");
		cancelQuestTimers("DESPAWN_BOX");
		
		spawnBoss();
		
		ThreadPool.scheduleAtFixedRate(this::removeExpiredEntries, 0, 60000);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (npc == null)
		{
			return null;
		}
		
		switch (event)
		{
			case "SPAWN_RAID":
			{
				deleteBox(false);
				spawnBoss();
				break;
			}
			case "DESPAWN_BOX":
			{
				deleteBox(false);
				cancelQuestTimers("SPAWN_RAID");
				startQuestTimer("SPAWN_RAID", boss_delay, null, null);
				break;
			}
			case "TALK_TEXT":
			{
				// 보스의 어그로 범위 내에 플레이어가 있는지 확인
				boolean hasPlayersInAggroRange = ZONE.getCharactersInside().stream() //
					.anyMatch(creature -> creature.isPlayer() && !creature.isInvisible() && creature.isInsideRadius3D(npc, 2000));
				
				if (hasPlayersInAggroRange)
				{
					String[] npcSayings =
					{
						"하찮은 것들이 채굴을 하러와?",
						"나를 쓰러트리기 전에는 네놈들이 원하는 채굴은 할 수 없을것이다!!",
						"누군가? 누가 방금 소리를 내었지?",
						"나는 미스릴 광산을 지키는 보스다!!",
						"도망갈 수 있을때 도망가도록!!",
						"세상을 저주하는 자들이여, 내가 왔도다!",
						"내 앞을 가로막는 자! 모두 무사하지 못할 것이다!",
						"나의 권위에 도전하는 하찮은 무리들은 앞으로 나서라!",
						"살육의 희열! 강탈의 쾌감! 얘들아, 오늘도 한바탕 해보자!",
						"겁도 없이 채굴을 하러 들어온 대가를 치르게 해주마!",
						"최근 내 구역 안에서 멋 모르고 날뛰는 놈들이 있다던데...",
						"이 근처에 요즘 설치고 다니는 놈들이 있다던데..."
					};
					
					String selectedSaying = npcSayings[Rnd.get(0, npcSayings.length - 1)];
					npc.broadcastSay(ChatType.NPC_GENERAL, selectedSaying);
				}
				int sec = Rnd.get(10, 30) * 1000;
				startQuestTimer("TALK_TEXT", sec, npc, null);
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isSummon)
	{
		int monsterObjectId = npc.getObjectId();
		Integer currentAttackerObjectId = attackingPlayers.get(monsterObjectId);
		
		if (player.isPlayer() && (player.getActiveWeaponInstance() != null) && !isSummon)
		{
			npc.setInvul(true);
			if (player.getActiveWeaponItem().getId() == WEAPON_ID)
			{
				if ((currentAttackerObjectId != null) && (player.getObjectId() != currentAttackerObjectId))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.sendMessage("다른 플레이어가 채굴중인 광석입니다.");
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.setTarget(null);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					// 현재 공격 중인 몬스터의 ObjectId 설정
					attackingPlayers.put(monsterObjectId, player.getObjectId());
					deleteBox(true);
					
					npc.setInvul(false);
					npc.getStatus().reduceHp(300, player);
					if (Rnd.chance(Config.MITHRIL_MINE_DROP_RATE))
					{
						dropItems(npc, player);
					}
				}
			}
		}
		
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	private void dropItems(Npc npc, Player player)
	{
		double randomValue = Rnd.get(100.0); // 0.0부터 100.0까지의 무작위 값
		
		if (randomValue < 33.0) // 33% 확률
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMineMania(player), player);
			player.doAutoLoot((Attackable) npc, 41253, Rnd.get(1, 1));
		}
		else if (randomValue < 78.0) // 45% 확률 (33.0 + 45)
		{
			player.doAutoLoot((Attackable) npc, ITEM_DROP_1[Rnd.get(ITEM_DROP_1.length)], Rnd.get(1, 10));
		}
		else if (randomValue < 97.0) // 19% 확률 (78.0 + 19)
		{
			player.doAutoLoot((Attackable) npc, ITEM_DROP_2[Rnd.get(ITEM_DROP_2.length)], Rnd.get(1, 5));
		}
		else if (randomValue < 99.0) // 2% 확률 (97.0 + 2)
		{
			player.doAutoLoot((Attackable) npc, ITEM_DROP_아포칼립스조각[Rnd.get(ITEM_DROP_아포칼립스조각.length)], Rnd.get(1, 1));
		}
		else // 나머지 (1%)
		{
			player.doAutoLoot((Attackable) npc, ITEM_DROP_최상급[Rnd.get(ITEM_DROP_최상급.length)], Rnd.get(1, 1));
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (npc.getId() == 36706)
		{
			String[] npcSayings =
			{
				"이 치욕은 반드시 갚아주마!",
				"다음에는 반드시 죽여주마!",
				"더렵혀진 내 명예, 반드시 되찾으리라!",
				"원망과 증오의 힘으로, 너를 저주하노라!",
				"내, 내가 패배하다니!!!",
				"누명을 벗을 때까지는 결코 잠들 수 없다!",
				"비록...이렇게 허무하게 사라지더라도...너희들은 내가 내린 저주에서 평생을 고통받으리라...",
				"모두 나와 같은 고통을 느끼도록 해주리라!",
				"나는 이렇게 사라지지만 또 다른 내가 나타나서 너희들을 응징할 것이다!"
			};
			
			String selectedSaying = npcSayings[Rnd.get(0, npcSayings.length - 1)];
			npc.broadcastSay(ChatType.NPC_GENERAL, selectedSaying);
			cancelQuestTimers("TALK_TEXT");
			
			deleteBox(false);
			spawn(31468);
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
			sm.addString("레이드보스를 처치하여 광물 상자가 생성됩니다!");
			ZONE.broadcastPacket(sm);
			
			for (Creature creature : ZONE.getCharactersInside())
			{
				if (creature.isPlayer())
				{
					if (!creature.isInsideRadius3D(killer, 1700))
					{
						if (Config.MITHRIL_MINE_TELEPORT_RAID)
						{
							sendHtmlMessage((Player) creature);
						}
						else
						{
							creature.teleToLocation(BorinetUtil.MITHRIL_RAID_ZONE_POINT[Rnd.get(BorinetUtil.MITHRIL_RAID_ZONE_POINT.length)]);
						}
					}
				}
			}
			cancelQuestTimers("DESPAWN_BOX");
			startQuestTimer("DESPAWN_BOX", despawn_delay, null, null);
		}
		if (npc.getId() == 31468)
		{
			deleteBox(true);
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	private void deleteBox(boolean delete)
	{
		if (delete)
		{
			if (_despawn != null)
			{
				_despawn.cancel(true);
				_despawn = null;
			}
			_despawn = ThreadPool.schedule(() ->
			{
				for (Spawn spawn : _boxs)
				{
					Npc lastSpawn = spawn.getLastSpawn();
					spawn.stopRespawn();
					if (lastSpawn != null)
					{
						lastSpawn.deleteMe();
					}
					SpawnTable.getInstance().deleteSpawn(spawn, false);
				}
				_boxs.clear();
			}, 595000);
		}
		else
		{
			for (Spawn spawn : _boxs)
			{
				Npc lastSpawn = spawn.getLastSpawn();
				spawn.stopRespawn();
				if (lastSpawn != null)
				{
					lastSpawn.deleteMe();
				}
				SpawnTable.getInstance().deleteSpawn(spawn, false);
			}
			_boxs.clear();
		}
	}
	
	private void sendHtmlMessage(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		final String content = getHtm(player, "MithrilMine.htm");
		html.setHtml(content);
		player.sendPacket(html);
	}
	
	private void removeExpiredEntries()
	{
		synchronized (attackingPlayers)
		{
			attackingPlayers.entrySet().removeIf(entry -> ((System.currentTimeMillis() - entry.getValue()) > 60000));
		}
	}
	
	private void spawn(int npcId)
	{
		deleteBox(true);
		for (int[] spawn : SPAWNS)
		{
			int xValue = spawn[0] + Rnd.get(-50, 50);
			int yValue = spawn[1] + Rnd.get(-50, 50);
			int zValue = spawn[2];
			
			Npc npc = addSpawn(npcId, xValue, yValue, zValue, 0, false, 0);
			final Spawn spawnObj = npc.getSpawn();
			spawnObj.setRespawnDelay(box_delay);
			spawnObj.setAmount(1);
			spawnObj.startRespawn();
			_boxs.add(spawnObj);
		}
	}
	
	private void spawnBoss()
	{
		cancelQuestTimers("TALK_TEXT");
		Npc npc = addSpawn(36706, 176525, -185195, -3720, 64554, false, 0);
		if (npc == null)
		{
			ThreadPool.schedule(this::spawnBoss, 10000); // 10초 후 재시도
			return;
		}
		if (!Config.BOSS_HAS_IMMUNITY)
		{
			int[] selectedSkills = BorinetUtil.getSkillLevel();
			String immun = selectedSkills[1] == 9 ? " 내성" : " 내성";
			npc.addSkill(SkillData.getInstance().getSkill(30264, selectedSkills[0]));
			npc.addSkill(SkillData.getInstance().getSkill(30265, selectedSkills[1]));
			npc.setTitle(npc.getTitle() + " - " + BorinetUtil.getSkillName(selectedSkills[0]) + "/" + BorinetUtil.getSkillName(selectedSkills[1]) + immun);
		}
		for (Creature creature : ZONE.getCharactersInside())
		{
			if (creature.isPlayer())
			{
				creature.decayMe();
				creature.spawnMe(creature.getX(), creature.getY(), creature.getZ());
			}
		}
		startQuestTimer("TALK_TEXT", 1000, npc, null); // TALK_TEXT 실행
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		final int[] minions = (npc.getId() == GRAVE_ROBBER_SUMMONER) ? SUMMONER_MINIONS : MAGICIAN_MINIONS;
		addMinion((Monster) npc, minions[getRandom(minions.length)]);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new MithrilMine();
	}
}
