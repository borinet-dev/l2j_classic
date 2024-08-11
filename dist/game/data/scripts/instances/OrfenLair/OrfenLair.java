package instances.OrfenLair;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

import events.InstanceOut.InstanceOut;
import instances.AbstractInstance;

/**
 * @author 보리넷 가츠
 */
public class OrfenLair extends AbstractInstance
{
	// NPCs
	private static final int 오르펜 = 18029;
	private static final int 라이켈 = 18030;
	private static final int 라이켈_레오스 = 18031;
	private static final int 리바 = 18032;
	private static final int 리바_이렌 = 18033;
	private static final int 텔레포터 = InstanceOut.텔레포터; // Teleport Bonus
	// Skills
	private static final SkillHolder Slasher = new SkillHolder(32486, 1);
	private static final SkillHolder FatalSlasher = new SkillHolder(32487, 1);
	private static final SkillHolder EnergyScatter = new SkillHolder(32488, 1);
	private static final SkillHolder FuryEnergyWave = new SkillHolder(32489, 1);
	private static final SkillHolder RaiseSpore = new SkillHolder(32493, 1);
	
	private static SkillHolder HEAL = new SkillHolder(4516, 1);
	// Misc
	private static final int TEMPLATE_ID = 202;
	
	public OrfenLair()
	{
		super(TEMPLATE_ID);
		addKillId(오르펜, 라이켈, 라이켈_레오스, 리바, 리바_이렌);
		addAttackId(오르펜);
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
					
					addSpawn(라이켈, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(라이켈, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(라이켈_레오스, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(라이켈_레오스, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId());
					world.setParameter("minion1", addSpawn(리바, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion2", addSpawn(리바, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion3", addSpawn(리바_이렌, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					world.setParameter("minion4", addSpawn(리바_이렌, world.getNpc(오르펜).getX() + getRandom(-400, 350), world.getNpc(오르펜).getY() + getRandom(-400, 350), world.getNpc(오르펜).getZ(), 31011, true, 0, true, npc.getInstanceId()));
					startQuestTimer("SUPPORT_ORFEN", 5000, npc, null);
				}
				break;
			}
			case "SUPPORT_ORFEN":
			{
				final Instance world = npc.getInstanceWorld();
				if (world == null)
				{
					return null;
				}
				
				final Npc 리바1 = world.getParameters().getObject("minion1", Npc.class);
				final Npc 리바2 = world.getParameters().getObject("minion2", Npc.class);
				final Npc 리바3 = world.getParameters().getObject("minion3", Npc.class);
				final Npc 리바4 = world.getParameters().getObject("minion4", Npc.class);
				if (!리바1.isDead())
				{
					리바1.setTarget(world.getNpc(오르펜));
					리바1.doCast(HEAL.getSkill());
				}
				if (!리바2.isDead())
				{
					리바2.setTarget(world.getNpc(오르펜));
					리바2.doCast(HEAL.getSkill());
				}
				if (!리바3.isDead())
				{
					리바3.setTarget(world.getNpc(오르펜));
					리바3.doCast(HEAL.getSkill());
				}
				if (!리바4.isDead())
				{
					리바4.setTarget(world.getNpc(오르펜));
					리바4.doCast(HEAL.getSkill());
				}
				
				startQuestTimer("SUPPORT_ORFEN", 5000, npc, null);
				break;
			}
			case "SKILLS":
			{
				int skill = Rnd.get(1, 5);
				switch (skill)
				{
					case 1:
						npc.setTarget(player);
						npc.doCast(Slasher.getSkill());
						break;
					case 2:
						npc.setTarget(player);
						npc.doCast(FatalSlasher.getSkill());
						break;
					case 3:
						npc.setTarget(player);
						npc.doCast(EnergyScatter.getSkill());
						break;
					case 4:
						npc.setTarget(player);
						npc.doCast(FuryEnergyWave.getSkill());
						break;
					case 5:
						npc.setTarget(player);
						npc.doCast(RaiseSpore.getSkill());
						break;
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
		
		if (npc.getId() == 오르펜)
		{
			if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.90)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.81)))
			{
				startQuestTimer("SKILLS", 10000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 0)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.80)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.71)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("SKILLS", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 1)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.70)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.61)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("SKILLS", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 2)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.60)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.51)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("SKILLS", 8000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 3)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.50)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.41)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("SKILLS", 7000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 4)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.40)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.31)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("SKILLS", 6000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 5)
				{
					startQuestTimer("SPAWN_MINION", 1000, npc, null);
				}
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.20)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.09)))
			{
				world.getParameters().set("spawnedMinions", false);
				startQuestTimer("SKILLS", 4000, npc, null);
				
				if (world.getParameters().getInt("stage", 0) == 6)
				{
					startQuestTimer("SKILLS", 2000, npc, null);
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
		
		if (npc.getId() == 오르펜)
		{
			for (Npc spawn : world.getNpcs())
			{
				if (spawn != null)
				{
					spawn.deleteMe();
				}
			}
			
			cancelQuestTimer("SPAWN_MINION", npc, player);
			cancelQuestTimer("SUPPORT_ORFEN", npc, player);
			
			addSpawn(텔레포터, 44957, 17660, -4335, 43578, false, 0, true, player.getInstanceId());
			broadcastPacket(world, new ExShowScreenMessage("오르펜 레이드를 성공하였습니다. 루피아를 통해서 마을로 갈 수 있어요!", 2, 9000));
			
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
		new OrfenLair();
	}
}