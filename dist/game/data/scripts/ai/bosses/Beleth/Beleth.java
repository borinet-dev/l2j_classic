package ai.bosses.Beleth;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;
import smartguard.core.utils.Rnd;

/**
 * Queen Beleth AI
 * @author 보리넷 가츠
 */
public class Beleth extends AbstractNpcAI
{
	// NPC
	private static final int BELETH = 29244;
	private static final int MINIONS = 25672;
	// Skills
	private static final SkillHolder BLEED = new SkillHolder(5495, 1);
	private static final SkillHolder FIREBALL = new SkillHolder(5496, 1);
	private static final SkillHolder HORN_OF_RISING = new SkillHolder(5497, 1);
	private static final SkillHolder LIGHTENING = new SkillHolder(5499, 1);
	// variables
	private static boolean selfBuff = false;
	private Npc _beleth;
	
	private static final int[] MOBS =
	{
		BELETH,
		MINIONS
	};
	
	private static final Location BELETH_LOC = new Location(79634, -55428, -6104, 0);
	
	private static final byte ALIVE = 0;
	private static final byte DEAD = 3;
	
	private static Set<Attackable> _minions = ConcurrentHashMap.newKeySet();
	
	private Beleth()
	{
		addKillId(MOBS);
		addAttackId(MOBS);
		registerMobs(BELETH);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(BELETH);
		final int status = GrandBossManager.getInstance().getStatus(BELETH);
		if (status == DEAD)
		{
			// load the unlock date and time for beleth from DB
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			// if beleth is locked until a certain time, mark it so and start the unlock timer
			// the unlock time has not yet expired.
			if (temp > 0)
			{
				startQuestTimer("beleth_unlock", temp, null, null);
			}
			else
			{
				_beleth = addSpawn(BELETH, BELETH_LOC, false, 0);
				GrandBossManager.getInstance().setStatus(BELETH, ALIVE);
				spawnBoss(_beleth);
			}
		}
		else
		{
			_beleth = addSpawn(BELETH, BELETH_LOC, false, 0);
			spawnBoss(_beleth);
		}
	}
	
	private void spawnBoss(Npc npc)
	{
		GrandBossManager.getInstance().addBoss((GrandBoss) npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		// Spawn minions
		spawnMinion(MINIONS, npc.getX(), npc.getY(), npc.getZ());
		spawnMinion(MINIONS, npc.getX(), npc.getY(), npc.getZ());
		spawnMinion(MINIONS, npc.getX(), npc.getY(), npc.getZ());
		spawnMinion(MINIONS, npc.getX(), npc.getY(), npc.getZ());
		spawnMinion(MINIONS, npc.getX(), npc.getY(), npc.getZ());
		spawnMinion(MINIONS, npc.getX(), npc.getY(), npc.getZ());
		startQuestTimer("check_minion_loc", 10000, npc, null, true);
		
		npc.setRandomWalking(false);
		npc.setLethalable(false);
	}
	
	private void spawnMinion(int npcId, int x, int y, int z)
	{
		int ranX = Rnd.get(1, 200);
		int ranY = Rnd.get(-200, -100);
		final Attackable mob = (Attackable) addSpawn(npcId, x + ranX, y - ranY, z, 0, false, 0);
		mob.setIsRaidMinion(true);
		_minions.add(mob);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "beleth_unlock":
			{
				final GrandBoss beleth = (GrandBoss) addSpawn(BELETH, BELETH_LOC, false, 0);
				GrandBossManager.getInstance().setStatus(BELETH, ALIVE);
				spawnBoss(beleth);
				break;
			}
			case "check_minion_loc":
			{
				for (Attackable mob : _minions)
				{
					if (!npc.isInsideRadius2D(mob, 3000))
					{
						mob.teleToLocation(npc.getLocation());
						((Attackable) npc).clearAggroList();
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
					}
				}
				break;
			}
			case "despawn_minions":
			{
				for (Attackable mob : _minions)
				{
					mob.decayMe();
				}
				_minions.clear();
				break;
			}
			case "spawn_minions":
			{
				spawnMinion(MINIONS, npc.getX(), npc.getY(), npc.getZ());
				break;
			}
			case "CAST":
			{
				if (!npc.isDead() && !npc.isCastingNow())
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					npc.doCast(FIREBALL.getSkill());
				}
				break;
			}
		}
		
		return null;
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if (!npc.isDead() && !npc.isCastingNow())
		{
			if ((player != null) && !player.isDead())
			{
				final double distance2 = npc.calculateDistance2D(player);
				if ((distance2 > 890) && !npc.isMovementDisabled())
				{
					npc.setTarget(player);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
					startQuestTimer("CAST", (int) (((distance2 - 890) / (npc.isRunning() ? npc.getRunSpeed() : npc.getWalkSpeed())) * 1000), npc, null);
				}
				else if (distance2 < 890)
				{
					npc.setTarget(player);
					npc.doCast(FIREBALL.getSkill());
				}
				return null;
			}
			if ((getRandom(100) < 40) && !World.getInstance().getVisibleObjectsInRange(npc, Player.class, 200).isEmpty())
			{
				npc.doCast(LIGHTENING.getSkill());
				return null;
			}
			//@formatter:off
			final Player plr = World.getInstance().getVisibleObjectsInRange(npc, Player.class, 950)
				.stream()
				.findFirst()
				.orElse(null);
			//@formatter:on
			if (plr != null)
			{
				npc.setTarget(plr);
				npc.doCast(FIREBALL.getSkill());
				return null;
			}
			((Attackable) npc).clearAggroList();
		}
		return null;
	}
	
	@Override
	public String onSkillSee(Npc npc, Player player, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		if (!npc.isDead() && (npc.getId() == BELETH) && !npc.isCastingNow() && skill.hasEffectType(EffectType.HEAL) && (getRandom(100) < 80))
		{
			npc.setTarget(player);
			npc.doCast(HORN_OF_RISING.getSkill());
		}
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		if (!npc.isDead() && !npc.isCastingNow() && (getRandom(100) < 40) && !World.getInstance().getVisibleObjectsInRange(npc, Player.class, 200).isEmpty())
		{
			npc.setTarget(player);
			npc.doCast(FIREBALL.getSkill());
		}
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final int npcId = npc.getId();
		if ((npcId == BELETH) || (npcId == MINIONS))
		{
			if (attacker.getVariables().getInt("베레스", 0) == 0)
			{
				Broadcast.toPlayerScreenMessageS(attacker, "레이드 매니저를 통해서 레이드를 진행해 주시기 바랍니다.");
				attacker.teleToLocation(81929 + getRandom(50), 149232 + getRandom(50), -3464);
				return null;
			}
		}
		
		final double distance = npc.calculateDistance2D(attacker);
		if ((distance > 500) || (getRandom(100) < 80))
		{
			if ((npcId == BELETH) && (_beleth != null) && !_beleth.isDead() && Util.checkIfInRange(900, _beleth, attacker, false) && !_beleth.isCastingNow())
			{
				if (!selfBuff)
				{
					_beleth.doCast(BLEED.getSkill());
					selfBuff = true;
				}
				if (getRandom(100) < 40)
				{
					return null;
				}
				_beleth.setTarget(attacker);
				_beleth.doCast(FIREBALL.getSkill());
			}
		}
		else if (!npc.isDead() && !npc.isCastingNow())
		{
			if (!World.getInstance().getVisibleObjectsInRange(npc, Player.class, 200).isEmpty())
			{
				npc.doCast(LIGHTENING.getSkill());
				return null;
			}
			((Attackable) npc).clearAggroList();
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (npc.getId() == BELETH)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			GrandBossManager.getInstance().setStatus(BELETH, DEAD);
			// Calculate Min and Max respawn times randomly.
			addSpawn(40034, 79634, -55428, -6104, 0, false, 900000);
			long respawnTime = Config.ANTHARAS_SPAWN_INTERVAL + getRandom(-Config.ANTHARAS_SPAWN_RANDOM, Config.ANTHARAS_SPAWN_RANDOM);
			respawnTime *= 3600000;
			startQuestTimer("beleth_unlock", respawnTime, null, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatSet info = GrandBossManager.getInstance().getStatSet(BELETH);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(BELETH, info);
			cancelQuestTimer("check_minion_loc", npc, null);
			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minions");
			selfBuff = false;
			npc.deleteMe();
		}
		else if ((GrandBossManager.getInstance().getStatus(BELETH) == ALIVE))
		{
			startQuestTimer("spawn_minions", 360000, GrandBossManager.getInstance().getBoss(BELETH), null);
			_minions.remove(npc);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Beleth();
	}
}
