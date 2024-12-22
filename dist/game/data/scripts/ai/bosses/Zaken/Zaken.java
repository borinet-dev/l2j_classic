package ai.bosses.Zaken;

import java.util.List;
import java.util.stream.Collectors;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.events.EnterRaidCheck;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.type.NoRestartZone;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

/**
 * Lilith AI<br>
 * @author 보리넷 가츠
 */
public class Zaken extends AbstractNpcAI
{
	// NPC
	private static final int ZAKEN = 29022;
	private GrandBoss _zaken = null;
	// Location
	private static final Location ZAKEN_LOC = new Location(52207, 217230, -3341, 41740);
	// Zone
	private static final NoRestartZone zone = ZoneManager.getInstance().getZoneById(70054, NoRestartZone.class);
	// Misc
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int IN_FIGHT = 2;
	private static final int DEAD = 3;
	
	private static final int MIN_PEOPLE_ZAKEN = Config.ZAKEN_MIN_MEMBER;
	private static final Location TELEPORT_IN_LOC = new Location(52199, 218953, -3224);
	private static long _lastAttack = 0;
	private static int _lock = 0;
	private static Player attacker_1 = null;
	private static Player attacker_2 = null;
	private static Player attacker_3 = null;
	private static int attacker_1_hate = 0;
	private static int attacker_2_hate = 0;
	private static int attacker_3_hate = 0;
	
	private Zaken()
	{
		addKillId(ZAKEN);
		addAttackId(ZAKEN);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(ZAKEN);
		bossDelete();
		switch (getStatus())
		{
			case WAITING:
			{
				setStatus(ALIVE);
			}
			case ALIVE:
			{
				break;
			}
			case IN_FIGHT:
			{
				_zaken = (GrandBoss) addSpawn(ZAKEN, ZAKEN_LOC, false, 0);
				_lastAttack = System.currentTimeMillis();
				addBoss(_zaken);
				
				startQuestTimer("CHECK_ATTACK", 1000, _zaken, null);
				break;
			}
			case DEAD:
			{
				final long remain = info.getLong("respawn_time") - System.currentTimeMillis();
				if (remain > 0)
				{
					startQuestTimer("zaken_unlock", remain, null, null);
				}
				else
				{
					notifyEvent("zaken_unlock", null, null);
				}
				break;
			}
		}
		_lock = 0;
	}
	
	public void bossDelete()
	{
		for (Creature creature : zone.getCharactersInside())
		{
			if (creature != null)
			{
				if (creature.isNpc())
				{
					creature.deleteMe();
				}
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enter":
			{
				if (player.isGM())
				{
					player.teleToLocation(TELEPORT_IN_LOC);
					player.sendMessage("자켄 레이드 존으로 이동하였습니다.");
					if (getStatus() != IN_FIGHT)
					{
						if (getStatus() != WAITING)
						{
							setStatus(WAITING);
						}
					}
					startQuestTimer("MASSEGE_GM", 5000, _zaken, player);
					break;
				}
				else if (getStatus() == DEAD)
				{
					Broadcast.toPlayerScreenMessageS(player, "현재 자켄이 사망한 상태이므로 이동할 수 없습니다.");
					break;
				}
				else if (getStatus() == IN_FIGHT)
				{
					Broadcast.toPlayerScreenMessageS(player, "현재 레이드가 진행 중 입니다. 입장할 수 없습니다.");
					break;
				}
				else if (player.getParty() == null)
				{
					Broadcast.toPlayerScreenMessageS(player, "현재 파티상태가 아니므로 입장할 수 없습니다.");
					break;
				}
				else if (player.isInParty())
				{
					final Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<Player> members = isInCC ? party.getCommandChannel().getMembers() : party.getMembers();
					final boolean isPartyLeader = party.isLeader(player);
					
					if (!isInCC)
					{
						if (!isPartyLeader)
						{
							Broadcast.toPlayerScreenMessageS(player, "파티장만이 입장시도를 할 수 있습니다.");
							break;
						}
					}
					else
					{
						final boolean isCCLeader = party.getCommandChannel().isLeader(player);
						if (!isCCLeader)
						{
							Broadcast.toPlayerScreenMessageS(player, "연합 파티장만이 입장시도를 할 수 있습니다.");
							break;
						}
					}
					if (members.size() < MIN_PEOPLE_ZAKEN)
					{
						Broadcast.toPlayerScreenMessageS(player, "입장가능 최소인원은 " + MIN_PEOPLE_ZAKEN + "명 입니다.");
					}
					else
					{
						if (EnterRaidCheck.ConditionCheck(player, isInCC, members))
						{
							for (Player member : members)
							{
								member.teleToLocation(TELEPORT_IN_LOC);
								member.sendMessage("자켄 레이드 존으로 이동하였습니다.");
							}
							
							String leaderName = isInCC ? party.getCommandChannel().getLeader().getName() : party.getLeader().getName();
							String memberNames = members.stream().map(Player::getName).collect(Collectors.joining(";"));
							EnterRaidCheck.enterMessage(player, isInCC, "자켄", leaderName, memberNames, false);
						}
						if (getStatus() != IN_FIGHT)
						{
							if (getStatus() != WAITING)
							{
								setStatus(WAITING);
							}
						}
					}
				}
				startQuestTimer("MASSEGE", 10000, _zaken, player);
				break;
			}
			case "MASSEGE":
			{
				final Party party = player.getParty();
				final List<Player> members = party.getMembers();
				for (Player member : members)
				{
					if (_lock == 0)
					{
						Broadcast.toPlayerScreenMessage(member, "잠시 후 자켄이 등장합니다! 준비하세요!");
						startQuestTimer("ZAKEN_LOCK", 50000, null, null);
						_lock = 1;
					}
				}
				break;
			}
			case "MASSEGE_GM":
			{
				if (_lock == 0)
				{
					Broadcast.toPlayerScreenMessage(player, "잠시 후 자켄이 등장합니다! 준비하세요!");
					startQuestTimer("ZAKEN_LOCK", 55000, null, null);
					_lock = 1;
				}
				break;
			}
			case "ZAKEN_LOCK":
			{
				final GrandBoss _zaken = (GrandBoss) addSpawn(ZAKEN, ZAKEN_LOC, false, 0);
				GrandBossManager.getInstance().setStatus(ZAKEN, IN_FIGHT);
				GrandBossManager.getInstance().addBoss(_zaken);
				_zaken.broadcastPacket(new PlaySound(1, "BS01_A", 1, _zaken.getObjectId(), _zaken.getX(), _zaken.getY(), _zaken.getZ()));
				_lastAttack = System.currentTimeMillis();
				
				startQuestTimer("CHECK_ATTACK", 60000, _zaken, null);
				break;
			}
			case "CHECK_ATTACK":
			{
				if ((npc != null) && ((_lastAttack + 600000) < System.currentTimeMillis()))
				{
					notifyEvent("CLEAR_ZONE", null, null);
					setStatus(ALIVE);
				}
				else
				{
					startQuestTimer("CHECK_ATTACK", 60000, npc, null);
				}
				break;
			}
			case "CLEAR_ZONE":
			{
				for (Creature creature : zone.getCharactersInside())
				{
					if (creature != null)
					{
						if (creature.isNpc())
						{
							creature.deleteMe();
						}
						if (creature.isPlayer())
						{
							notifyEvent("REMOVE_PLAYERS_FROM_ZONE_ZAKEN", null, (Player) creature);
						}
					}
				}
				_lock = 0;
				break;
			}
			case "REMOVE_PLAYERS_FROM_ZONE_ZAKEN":
			{
				for (Creature charInside : zone.getCharactersInside())
				{
					if ((charInside != null) && charInside.isPlayer())
					{
						charInside.teleToLocation(81929 + getRandom(300), 149309 + getRandom(300), -3464);
					}
				}
				break;
			}
			case "zaken_unlock":
			{
				GrandBossManager.getInstance().setStatus(ZAKEN, ALIVE);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	private final void refreshAiParams(Player attacker, int damage)
	{
		if ((attacker_1 != null) && (attacker == attacker_1))
		{
			if (attacker_1_hate < (damage + 1000))
			{
				attacker_1_hate = damage + getRandom(3000);
			}
		}
		else if ((attacker_2 != null) && (attacker == attacker_2))
		{
			if (attacker_2_hate < (damage + 1000))
			{
				attacker_2_hate = damage + getRandom(3000);
			}
		}
		else if ((attacker_3 != null) && (attacker == attacker_3))
		{
			if (attacker_3_hate < (damage + 1000))
			{
				attacker_3_hate = damage + getRandom(3000);
			}
		}
		else
		{
			final int i1 = CommonUtil.min(attacker_1_hate, attacker_2_hate, attacker_3_hate);
			if (attacker_1_hate == i1)
			{
				attacker_1_hate = damage + getRandom(3000);
				attacker_1 = attacker;
			}
			else if (attacker_2_hate == i1)
			{
				attacker_2_hate = damage + getRandom(3000);
				attacker_2 = attacker;
			}
			else if (attacker_3_hate == i1)
			{
				attacker_3_hate = damage + getRandom(3000);
				attacker_3 = attacker;
			}
		}
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		_lastAttack = System.currentTimeMillis();
		if (npc.getId() == ZAKEN)
		{
			if (!zone.isCharacterInZone(attacker) || (getStatus() != IN_FIGHT))
			{
				LOGGER.warning(getClass().getSimpleName() + ": " + attacker.getName() + " 캐릭터가 잘못된 조건에서 자켄을 공격했습니다!");
				attacker.teleToLocation(80464, 152294, -3534);
			}
			
			if (skill == null)
			{
				refreshAiParams(attacker, damage * 1000);
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.25))
			{
				refreshAiParams(attacker, (damage / 3) * 100);
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.5))
			{
				refreshAiParams(attacker, damage * 20);
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.75))
			{
				refreshAiParams(attacker, damage * 10);
			}
			else
			{
				refreshAiParams(attacker, (damage / 3) * 20);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		GrandBossManager.getInstance().setStatus(ZAKEN, DEAD);
		final long respawnTime = (Config.ZAKEN_SPAWN_INTERVAL + getRandom(-Config.ZAKEN_SPAWN_RANDOM, Config.ZAKEN_SPAWN_RANDOM)) * 3600000;
		startQuestTimer("zaken_unlock", respawnTime, null, null);
		final StatSet info = GrandBossManager.getInstance().getStatSet(ZAKEN);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatSet(ZAKEN, info);
		_lock = 0;
		
		startQuestTimer("REMOVE_PLAYERS_FROM_ZONE_ZAKEN", 600000, null, killer);
		addSpawn(31088, 52207, 217230, -3341, 0, false, 600000, false, npc.getInstanceId());
		return super.onKill(npc, killer, isSummon);
	}
	
	private int getStatus()
	{
		return GrandBossManager.getInstance().getStatus(ZAKEN);
	}
	
	private void addBoss(GrandBoss grandboss)
	{
		GrandBossManager.getInstance().addBoss(grandboss);
	}
	
	private void setStatus(int status)
	{
		GrandBossManager.getInstance().setStatus(ZAKEN, status);
	}
	
	public static void main(String[] args)
	{
		new Zaken();
	}
}
