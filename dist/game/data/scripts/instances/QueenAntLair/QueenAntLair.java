package instances.QueenAntLair;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

import events.InstanceOut.InstanceOut;
import instances.AbstractInstance;

/**
 * @author Serenitty
 * @URL https://l2central.info/essence/events_and_promos/832.html
 */
public class QueenAntLair extends AbstractInstance
{
	// NPCs
	private static final int QUEEN = 18020;
	private static final int ESCORT = 18023;
	private static final int NURSE = 18022;
	private static final int 텔레포터 = InstanceOut.텔레포터; // Teleport Bonus
	// Skills
	private static final SkillHolder RAIN_OF_STONES_LV_1 = new SkillHolder(48254, 1); // When player in Radius target boss atack
	private static final SkillHolder HEAL = new SkillHolder(4020, 1);
	// Misc
	private static final int TEMPLATE_ID = 201;
	
	public QueenAntLair()
	{
		super(TEMPLATE_ID);
		addKillId(QUEEN, ESCORT, NURSE);
		addAttackId(QUEEN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "ENTER":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				final Instance world = player.getInstanceWorld();
				if (world != null)
				{
					player.sendPacket(new ExSendUIEvent(player, false, false, (int) (world.getRemainingTime() / 1000), 0, NpcStringId.REMAINING_TIME));
				}
				break;
			}
			case "SPAWN_MINION":
			{
				final Instance world = npc.getInstanceWorld();
				if (world == null)
				{
					return null;
				}
				
				if (!world.getParameters().getBoolean("spawnedMinions", false))
				{
					world.getParameters().set("spawnedMinions", true);
					
					final int stage = world.getParameters().getInt("stage", 0);
					world.getParameters().set("stage", stage + 1); // +1= -10% BOSS HP
					
					addSpawn(ESCORT, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(ESCORT, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(ESCORT, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId());
					world.setParameter("minion1", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion2", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion3", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion4", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion5", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion6", addSpawn(NURSE, world.getNpc(QUEEN).getX() + getRandom(-400, 350), world.getNpc(QUEEN).getY() + getRandom(-400, 350), world.getNpc(QUEEN).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					startQuestTimer("SUPPORT_QUEEN", 5000, npc, null);
				}
				break;
			}
			case "SUPPORT_QUEEN":
			{
				final Instance world = npc.getInstanceWorld();
				if (world == null)
				{
					return null;
				}
				
				final Npc nurse1 = world.getParameters().getObject("minion1", Npc.class);
				final Npc nurse2 = world.getParameters().getObject("minion2", Npc.class);
				final Npc nurse3 = world.getParameters().getObject("minion3", Npc.class);
				final Npc nurse4 = world.getParameters().getObject("minion4", Npc.class);
				final Npc nurse5 = world.getParameters().getObject("minion5", Npc.class);
				final Npc nurse6 = world.getParameters().getObject("minion6", Npc.class);
				if (!nurse1.isDead())
				{
					nurse1.setTarget(world.getNpc(QUEEN));
					nurse1.doCast(HEAL.getSkill());
				}
				if (!nurse2.isDead())
				{
					nurse2.setTarget(world.getNpc(QUEEN));
					nurse2.doCast(HEAL.getSkill());
				}
				if (!nurse3.isDead())
				{
					nurse3.setTarget(world.getNpc(QUEEN));
					nurse3.doCast(HEAL.getSkill());
				}
				if (!nurse4.isDead())
				{
					nurse4.setTarget(world.getNpc(QUEEN));
					nurse4.doCast(HEAL.getSkill());
				}
				if (!nurse5.isDead())
				{
					nurse5.setTarget(world.getNpc(QUEEN));
					nurse5.doCast(HEAL.getSkill());
				}
				if (!nurse6.isDead())
				{
					nurse6.setTarget(world.getNpc(QUEEN));
					nurse6.doCast(HEAL.getSkill());
				}
				
				startQuestTimer("SUPPORT_QUEEN", 5000, npc, null);
				break;
			}
			case "RAINOF_STONES":
			{
				if (SkillCaster.checkUseConditions(npc, RAIN_OF_STONES_LV_1.getSkill()))
				{
					npc.doCast(RAIN_OF_STONES_LV_1.getSkill());
				}
				break;
			}
		}
		return null;
	}
	
	// +1= -10% BOSS HP ACORD 4GAME
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		if (npc.getId() == QUEEN)
		{
			if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.90)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.81)))
			{
				startQuestTimer("RAINOF_STONES", 10000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 0)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.80)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.71)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 1)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.70)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.61)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 2)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.60)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.51)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 3)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.50)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.41)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 7000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 4)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.40)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.31)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 6000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 5)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.20)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.09)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("RAINOF_STONES", 4000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 6)
				{
					startQuestTimer("RAINOF_STONES", 2000, npc, null);
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	void broadcastPacket(Instance world, IClientOutgoingPacket packet)
	{
		for (Player player : world.getPlayers())
		{
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(packet);
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		if (npc.getId() == QUEEN)
		{
			for (Npc spawn : world.getNpcs())
			{
				if (spawn != null)
				{
					spawn.deleteMe();
				}
			}
			
			cancelQuestTimer("SPAWN_MINION", npc, player);
			cancelQuestTimer("SUPPORT_QUEEN", npc, player);
			
			addSpawn(텔레포터, -22130, 182482, -5720, 49151, false, 0, true, player.getInstanceId());
			broadcastPacket(world, new ExShowScreenMessage("여왕개미 레이드를 성공하였습니다. 루피아를 통해서 마을로 갈 수 있어요!", 2, 9000));
			
			for (Player gamer : world.getPlayers())
			{
				gamer.sendPacket(new ExSendUIEvent(gamer, false, false, 0, 0, NpcStringId.REMAINING_TIME));
			}
			world.finishInstance();
		}
		
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new QueenAntLair();
	}
}