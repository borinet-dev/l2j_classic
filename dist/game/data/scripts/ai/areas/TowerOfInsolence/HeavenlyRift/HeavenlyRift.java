package ai.areas.TowerOfInsolence.HeavenlyRift;

import java.util.Arrays;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.AttackableAI;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.HeavenlyRiftManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExChangeNpcState;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import ai.AbstractNpcAI;

public class HeavenlyRift extends AbstractNpcAI
{
	// NPCs
	private static final int BOMB = 18003;
	private static final int TOWER = 18004;
	
	public HeavenlyRift()
	{
		addKillId(BOMB, TOWER);
		addSpawnId(BOMB);
		addTalkId(TOWER);
		addFirstTalkId(TOWER);
		addKillId(HeavenlyRiftManager.DIVINE_ANGELS);
		addSpawnId(HeavenlyRiftManager.DIVINE_ANGELS);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		if (HeavenlyRiftManager.isSucsess)
		{
			htmltext = "18004-1.htm";
		}
		else
		{
			htmltext = "18004.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (HeavenlyRiftManager.isStarted)
		{
			if (npc.getId() == TOWER)
			{
				for (Creature creature : HeavenlyRiftManager.ZONE.getCharactersInside())
				{
					if (creature.isMonster() && !creature.isDead())
					{
						creature.deleteMe();
					}
				}
				HeavenlyRiftManager.endRift(false, GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0));
			}
			else if (npc.getId() == BOMB)
			{
				
				if (getRandom(100) < 75)
				{
					int count = Rnd.get(1, 3);
					for (int i = 0; i < count; ++i)
					{
						int randomIndex = Rnd.get(0, HeavenlyRiftManager.DIVINE_ANGELS.length - 1);
						int randomNpcId = HeavenlyRiftManager.DIVINE_ANGELS[randomIndex];
						HeavenlyRiftManager.spawnMonster(randomNpcId, npc.getX() + getRandom(-100, 100), npc.getY() + getRandom(-100, 100), 10984, 1200000);
					}
				}
				else
				{
					World.getInstance().forEachVisibleObjectInRange((WorldObject) npc, Playable.class, 200, creature ->
					{
						if ((creature != null) && !creature.isDead())
						{
							creature.reduceCurrentHp(getRandom(300, 400), npc, null);
						}
					});
				}
				boolean allDivineAngelsDead = true;
				for (int npcId : HeavenlyRiftManager.DIVINE_ANGELS)
				{
					if (HeavenlyRiftManager.getAliveNpcCount(npcId) > 0)
					{
						allDivineAngelsDead = false;
						break; // 하나라도 살아있으면 더 이상 검사할 필요가 없음
					}
				}
				boolean bombDead = HeavenlyRiftManager.getAliveNpcCount(BOMB) == 0;
				if (allDivineAngelsDead && bombDead)
				{
					// DIVINE_ANGELS와 BOMB 모두가 모두 죽어있을 때의 처리
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
					sm.addString("모든 폭탄과 몬스터를 처치하였습니다!");
					HeavenlyRiftManager.ZONE.broadcastPacket(sm);
					HeavenlyRiftManager.endRift(true, GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0));
				}
			}
			else
			{
				int divineAngelCount = 0;
				int bombCount = 0;
				
				for (int npcId : HeavenlyRiftManager.DIVINE_ANGELS)
				{
					divineAngelCount += HeavenlyRiftManager.getAliveNpcCount(npcId);
				}
				
				bombCount = HeavenlyRiftManager.getAliveNpcCount(BOMB);
				
				if (divineAngelCount == 0)
				{
					int riftLevel = GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
					if (riftLevel == 1)
					{
						if (bombCount == 0)
						{
							sm.addString("모든 폭탄과 정령 천사를 처치하였습니다!");
							HeavenlyRiftManager.ZONE.broadcastPacket(sm);
							HeavenlyRiftManager.endRift(true, riftLevel);
						}
					}
					else if (riftLevel == 2)
					{
						sm.addString("수호탑 디펜스에 성공하였습니다!");
						HeavenlyRiftManager.isSucsess = true;
						HeavenlyRiftManager.ZONE.broadcastPacket(sm);
						HeavenlyRiftManager.endRift(true, riftLevel);
					}
					else
					{
						sm.addString("모든 정령 천사를 처치하였습니다!");
						HeavenlyRiftManager.ZONE.broadcastPacket(sm);
						HeavenlyRiftManager.endRift(true, riftLevel);
					}
				}
				
				if (Rnd.chance(28))
				{
					npc.dropItem(killer.getActingPlayer(), 49756, 1);
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (npc.getId() == BOMB)
		{
			npc.broadcastPacket(new ExChangeNpcState(npc.getObjectId(), 1));
		}
		if (Arrays.stream(HeavenlyRiftManager.DIVINE_ANGELS).anyMatch(id -> id == npc.getId()))
		{
			if (GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0) == 2)
			{
				addAttackTowerDesire(npc, HeavenlyRiftManager._tower, 999);
				((AttackableAI) npc.getAI()).setGlobalAggro(-120000);
			}
		}
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new HeavenlyRift();
	}
}
