package instances.Kamaloka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerKamalokaMania;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.instancezone.InstanceTemplate;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import instances.AbstractInstance;

public class Kamaloka extends AbstractInstance
{
	private static final Map<Integer, Integer> HoleLevels = new HashMap<>();
	private static final Map<Integer, Integer> LabyrinthLevels = new HashMap<>();
	
	static
	{
		HoleLevels.put(9, 53);
		HoleLevels.put(10, 56);
		HoleLevels.put(12, 63);
		HoleLevels.put(13, 66);
		HoleLevels.put(15, 73);
		
		LabyrinthLevels.put(11, 59);
		LabyrinthLevels.put(14, 69);
		LabyrinthLevels.put(16, 78);
		LabyrinthLevels.put(17, 81);
		LabyrinthLevels.put(18, 83);
	}
	
	/*
	 * Maximum level difference between players level and kamaloka level Default: 5
	 */
	private static final int MAX_LEVEL_DIFFERENCE = 5;
	
	/*
	 * If true shaman in the first room will have same npcId as other mobs, making radar useless Default: true (but not retail like)
	 */
	private static final boolean STEALTH_SHAMAN = true;
	// Template IDs for Kamaloka
	// @formatter:off
	private static final int[] TEMPLATE_IDS =
	{
		57, 58, 73, 60, 61, 74, 63, 64, 75, 66, 67, 76, 69, 70, 77, 72, 78, 79, 134
	};
	// Level of the Kamaloka
	private static final int[] LEVEL =
	{
		23, 26, 29, 33, 36, 39, 43, 46, 49, 53, 56, 59, 63, 66, 69, 73, 78, 81, 83
	};
	// Duration of the instance, minutes
	private static final int DURATION = 30;
	// Maximum party size for the instance
	private static final int MAX_PARTY_SIZE = 9;
	
	/**
	 * List of buffs NOT removed on enter from player and pet<br>
	 * On retail only newbie guide buffs not removed<br>
	 * CAUTION: array must be sorted in ascension order!
	 */
	protected static final int[] BUFFS_WHITELIST =
	{
		4322, 4323, 4324, 4325, 4326, 4327, 4328, 4329, 4330, 4331, 5632, 5637, 5950
	};
	
	// Teleport points into instances x, y, z
	private static final Location[] TELEPORTS =
	{
		new Location(-88429, -220629, -7903),
		new Location(-82464, -219532, -7899),
		new Location(-10700, -174882, -10936), // -76280, -185540, -10936
		new Location(-89683, -213573, -8106),
		new Location(-81413, -213568, -8104),
		new Location(-10700, -174882, -10936), // -76280, -174905, -10936
		new Location(-89759, -206143, -8120),
		new Location(-81415, -206078, -8107),
		new Location(-10700, -174882, -10936),
		new Location(-56999, -219856, -8117),
		new Location(-48794, -220261, -8075),
		new Location(-10700, -174882, -10936),
		new Location(-56940, -212939, -8072),
		new Location(-55566, -206139, -8120),
		new Location(-10700, -174882, -10936),
		new Location(-49805, -206139, -8117),
		new Location(-10700, -174882, -10936),
		new Location(-10700, -174882, -10936),
		new Location(22003, -174886, -10900),
	};
	
	// Respawn delay for the mobs in the first room, seconds Default: 25
	private static final int FIRST_ROOM_RESPAWN_DELAY = 25;
	
	/**
	 * First room information, null if room not spawned.<br>
	 * Skill is casted on the boss when shaman is defeated and mobs respawn stopped<br>
	 * Default: 5699 (decrease pdef)<br>
	 * shaman npcId, minions npcId, skillId, skillLevel
	 */
	private static final int[][] FIRST_ROOM =
	{
		null, null, {22485, 22486, 5699, 1},
		null, null, {22488, 22489, 5699, 2},
		null, null, {22491, 22492, 5699, 3},
		null, null, {22494, 22495, 5699, 4},
		null, null, {22497, 22498, 5699, 5},
		null, {22500 ,22501, 5699, 6}, {22503, 22504, 5699, 7}, {25706, 25707, 5699, 7}
	};
	
	/*
	 * First room spawns, null if room not spawned x, y, z
	 */
	private static final int[][][] FIRST_ROOM_SPAWNS =
	{
		null, null, {
			{-12381, -174973, -10955},
			{-12413, -174905, -10955},
			{-12377, -174838, -10953},
			{-12316, -174903, -10953},
			{-12326, -174786, -10953},
			{-12330, -175024, -10953},
			{-12211, -174900, -10955},
			{-12238, -174849, -10953},
			{-12233, -174954, -10953}},
		null, null, {
			{-12381, -174973, -10955},
			{-12413, -174905, -10955},
			{-12377, -174838, -10953},
			{-12316, -174903, -10953},
			{-12326, -174786, -10953},
			{-12330, -175024, -10953},
			{-12211, -174900, -10955},
			{-12238, -174849, -10953},
			{-12233, -174954, -10953}},
		null, null, {
			{-12381, -174973, -10955},
			{-12413, -174905, -10955},
			{-12377, -174838, -10953},
			{-12316, -174903, -10953},
			{-12326, -174786, -10953},
			{-12330, -175024, -10953},
			{-12211, -174900, -10955},
			{-12238, -174849, -10953},
			{-12233, -174954, -10953}},
		null, null, {
			{-12381, -174973, -10955},
			{-12413, -174905, -10955},
			{-12377, -174838, -10953},
			{-12316, -174903, -10953},
			{-12326, -174786, -10953},
			{-12330, -175024, -10953},
			{-12211, -174900, -10955},
			{-12238, -174849, -10953},
			{-12233, -174954, -10953}},
		null, null, {
			{-12381, -174973, -10955},
			{-12413, -174905, -10955},
			{-12377, -174838, -10953},
			{-12316, -174903, -10953},
			{-12326, -174786, -10953},
			{-12330, -175024, -10953},
			{-12211, -174900, -10955},
			{-12238, -174849, -10953},
			{-12233, -174954, -10953}},
		null, {
			{-12381, -174973, -10955},
			{-12413, -174905, -10955},
			{-12377, -174838, -10953},
			{-12316, -174903, -10953},
			{-12326, -174786, -10953},
			{-12330, -175024, -10953},
			{-12211, -174900, -10955},
			{-12238, -174849, -10953},
			{-12233, -174954, -10953}},
			{
			{-12381, -174973, -10955},
			{-12413, -174905, -10955},
			{-12377, -174838, -10953},
			{-12316, -174903, -10953},
			{-12326, -174786, -10953},
			{-12330, -175024, -10953},
			{-12211, -174900, -10955},
			{-12238, -174849, -10953},
			{-12233, -174954, -10953}},
			{
			{20409, -174827, -10912},
			{20409, -174947, -10912},
			{20494, -174887, -10912},
			{20494, -174767, -10912},
			{20614, -174887, -10912},
			{20579, -174827, -10912},
			{20579, -174947, -10912},
			{20494, -175007, -10912},
			{20374, -174887, -10912}}
	};
	
	/*
	 * Second room information, null if room not spawned Skill is casted on the boss when all mobs are defeated Default: 5700 (decrease mdef) npcId, skillId, skillLevel
	 */
	private static final int[][] SECOND_ROOM =
	{
		null, null, {22487, 5700, 1},
		null, null, {22490, 5700, 2},
		null, null, {22493, 5700, 3},
		null, null, {22496, 5700, 4},
		null, null, {22499, 5700, 5},
		null, {22502, 5700, 6}, {22505, 5700, 7}, {25708, 5700, 7}
	};
	
	/*
	 * Spawns for second room, null if room not spawned x, y, z
	 */
	private static final int[][][] SECOND_ROOM_SPAWNS =
	{
		null, null, {
			{-14547, -174901, -10690},
			{-14543, -175030, -10690},
			{-14668, -174900, -10690},
			{-14538, -174774, -10690},
			{-14410, -174904, -10690}},
		null, null, {
			{-14547, -174901, -10690},
			{-14543, -175030, -10690},
			{-14668, -174900, -10690},
			{-14538, -174774, -10690},
			{-14410, -174904, -10690}},
		null, null, {
			{-14547, -174901, -10690},
			{-14543, -175030, -10690},
			{-14668, -174900, -10690},
			{-14538, -174774, -10690},
			{-14410, -174904, -10690}},
		null, null, {
			{-14547, -174901, -10690},
			{-14543, -175030, -10690},
			{-14668, -174900, -10690},
			{-14538, -174774, -10690},
			{-14410, -174904, -10690}},
		null, null, {
			{-14547, -174901, -10690},
			{-14543, -175030, -10690},
			{-14668, -174900, -10690},
			{-14538, -174774, -10690},
			{-14410, -174904, -10690}},
		null, {
			{-14547, -174901, -10690},
			{-14543, -175030, -10690},
			{-14668, -174900, -10690},
			{-14538, -174774, -10690},
			{-14410, -174904, -10690}},
			{
			{-14547, -174901, -10690},
			{-14543, -175030, -10690},
			{-14668, -174900, -10690},
			{-14538, -174774, -10690},
			{-14410, -174904, -10690}},
			{
			{18175, -174991, -10653},
			{18070, -174890, -10655},
			{18157, -174886, -10655},
			{18249, -174885, -10653},
			{18144, -174821, -10648}}
	};
	
	// miniboss info
	// skill is casted on the boss when miniboss is defeated
	// npcId, x, y, z, skill id, skill level
	/*
	 * Miniboss information, null if miniboss not spawned Skill is casted on the boss when miniboss is defeated Default: 5701 (decrease patk) npcId, x, y, z, skillId, skillLevel
	 */
	private static final int[][] MINIBOSS =
	{
		null, null, {25616, -16874, -174900, -10427, 5701, 1},
		null, null, {25617, -16874, -174900, -10427, 5701, 2},
		null, null, {25618, -16874, -174900, -10427, 5701, 3},
		null, null, {25619, -16874, -174900, -10427, 5701, 4},
		null, null, {25620, -16874, -174900, -10427, 5701, 5},
		null, {25621, -16874, -174900, -10427, 5701, 6}, {25622, -16874, -174900, -10427, 5701, 7}, {25709, 15828, -174885, -10384, 5701, 7}
	};
	
	/*
	 * Bosses of the kamaloka Instance ends when boss is defeated npcId, x, y, z
	 */
	private static final int[][] BOSS =
	{
		{18554, -88998, -220077, -7892},
		{18555, -81891, -220078, -7893},
		{29129, -20659, -174903, -9983},
		{18558, -89183, -213564, -8100},
		{18559, -81937, -213566, -8100},
		{29132, -20659, -174903, -9983},
		{18562, -89054, -206144, -8115},
		{18564, -81937, -206077, -8100},
		{29135, -20659, -174903, -9983},
		{18566, -56281, -219859, -8115},
		{18568, -49336, -220260, -8068},
		{29138, -20659, -174903, -9983},
		{18571, -56415, -212939, -8068},
		{18573, -56281, -206140, -8115},
		{29141, -20659, -174903, -9983},
		{18577, -49084, -206140, -8115},
		{29144, -20659, -174903, -9983},
		{29147, -20659, -174903, -9983},
		{25710, 12047, -174887, -9944}
	};
	
	/*
	 * Escape telepoters spawns, null if not spawned x, y, z
	 */
	private static final int[][] TELEPORTERS =
	{
		null, null, {-10865, -174905, -10944},
		null, null, {-10865, -174905, -10944},
		null, null, {-10865, -174905, -10944},
		null, null, {-10865, -174905, -10944},
		null, null, {-10865, -174905, -10944},
		null, {-10865, -174905, -10944}, {-10865, -174905, -10944}, {21837, -174885, -10904}
	};
	// @formatter:on
	
	/*
	 * Escape teleporter npcId
	 */
	private static final int TELEPORTER = 32496;
	
	/** Kamaloka captains (start NPC) */
	private static final int 마티아스 = 31340;
	
	public Kamaloka()
	{
		super(TEMPLATE_IDS);
		addFirstTalkId(TELEPORTER);
		addTalkId(TELEPORTER, 마티아스);
		addStartNpc(마티아스);
		addInstanceLeaveId(TEMPLATE_IDS);
		
		for (int[] mob : FIRST_ROOM)
		{
			if (mob != null)
			{
				if (STEALTH_SHAMAN)
				{
					addKillId(mob[1]);
				}
				else
				{
					addKillId(mob[0]);
				}
			}
		}
		for (int[] mob : SECOND_ROOM)
		{
			if (mob != null)
			{
				addKillId(mob[0]);
			}
		}
		for (int[] mob : MINIBOSS)
		{
			if (mob != null)
			{
				addKillId(mob[0]);
			}
		}
		for (int[] mob : BOSS)
		{
			addKillId(mob[0]);
		}
	}
	
	private static boolean checkPartyConditions(Player player, int index, int templateId)
	{
		final Party party = player.getParty();
		// player must be in party
		if (party == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return false;
		}
		// ...and be party leader
		if (party.getLeader() != player)
		{
			player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
			player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
			return false;
		}
		// party must not exceed max size for selected instance
		if (party.getMemberCount() > MAX_PARTY_SIZE)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
			return false;
		}
		
		// get level of the instance
		final int level = LEVEL[index];
		// and client name
		final String instanceName = InstanceManager.getInstance().getInstanceName(TEMPLATE_IDS[index]);
		Map<Integer, Long> instanceTimes;
		// for each party member
		for (Player partyMember : party.getMembers())
		{
			// player level must be in range
			if (Math.abs(partyMember.getLevel() - level) > MAX_LEVEL_DIFFERENCE)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return false;
			}
			// player must be near party leader
			if (!partyMember.isInsideRadius3D(player, 1000))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return false;
			}
			// get instances reenter times for player
			instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(partyMember);
			if (instanceTimes != null)
			{
				for (Entry<Integer, Long> entry : instanceTimes.entrySet())
				{
					// find instance with same name (kamaloka or labyrinth)
					if ((instanceName != null) && !instanceName.equals(InstanceManager.getInstance().getInstanceName(entry.getKey().intValue())))
					{
						continue;
					}
					// Check if any player from the group has already finished the instance
					if (InstanceManager.getInstance().getInstanceTime(partyMember, templateId) > 0)
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
						sm.addPcName(partyMember);
						player.sendPacket(sm);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Get instance world associated with {@code player}.
	 * @param player player who wants get instance world
	 * @return instance world if found, otherwise null
	 */
	@Override
	public Instance getPlayerInstance(Player player)
	{
		return InstanceManager.getInstance().getPlayerInstance(player, false);
	}
	
	/**
	 * Removing all buffs from player and pet except BUFFS_WHITELIST
	 * @param ch player
	 */
	private void removeBuffs(Creature ch)
	{
		// Stop all buffs.
		ch.getEffectList().stopEffects(info -> (info != null) && !info.getSkill().isStayAfterDeath() && (Arrays.binarySearch(BUFFS_WHITELIST, info.getSkill().getId()) < 0), true, true);
	}
	
	/**
	 * Handling enter of the players into kamaloka
	 * @param player party leader
	 * @param index (0-18) kamaloka index in arrays
	 */
	private final synchronized void enterInstance(Player player, int index)
	{
		int templateId = TEMPLATE_IDS[index];
		
		// check for existing instances for this player
		Instance world = getPlayerInstance(player);
		// player already in the instance
		if (world != null)
		{
			// but not in kamaloka
			if (world.getTemplateId() != templateId)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return;
			}
			// check for level difference again on reenter
			if (Math.abs(player.getLevel() - LEVEL[world.getParameters().getInt("index", 0)]) > MAX_LEVEL_DIFFERENCE)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY);
				sm.addPcName(player);
				player.sendPacket(sm);
				return;
			}
			// check what instance still exist
			final Instance inst = InstanceManager.getInstance().getInstance(world.getId());
			if (inst != null)
			{
				removeBuffs(player);
				player.teleToLocation(TELEPORTS[index], world);
				final Skill BlessingOfLight = SkillData.getInstance().getSkill(30267, 1);
				BlessingOfLight.applyEffects(player, player, false, 1800);
				player.sendMessage("빛의 가호의 효과가 느껴집니다.");
			}
			return;
		}
		// Creating new kamaloka instance
		if (!checkPartyConditions(player, index, templateId))
		{
			return;
		}
		
		// Creating instance
		final InstanceManager manager = InstanceManager.getInstance();
		final InstanceTemplate template = manager.getInstanceTemplate(templateId);
		world = manager.createInstance(template, player);
		// set duration and empty destroy time
		world.setDuration(DURATION * 60000);
		// set index for easy access to the arrays
		world.setParameter("index", index);
		world.setStatus(0);
		// spawn npcs
		spawnKama(world);
		
		// and finally teleport party into instance
		final Party party = player.getParty();
		for (Player partyMember : party.getMembers())
		{
			world.addAllowed(partyMember);
			removeBuffs(partyMember);
			partyMember.teleToLocation(TELEPORTS[index], world);
			partyMember.sendPacket(new ExSendUIEvent(partyMember, false, false, (DURATION * 60), 0, NpcStringId.REMAINING_TIME));
			ThreadPool.schedule(() -> partyMember.sendPacket(new ExSendUIEvent(partyMember, true, false, 0, 0, NpcStringId.REMAINING_TIME)), 11000);
			final Skill BlessingOfLight = SkillData.getInstance().getSkill(30267, 1);
			BlessingOfLight.applyEffects(player, partyMember, false, 1800);
			partyMember.sendMessage("빛의 가호의 효과가 느껴집니다.");
		}
	}
	
	/**
	 * Spawn all NPCs in kamaloka
	 * @param world instanceWorld
	 */
	private final void spawnKama(Instance world)
	{
		int[] npcs;
		int[][] spawns;
		Npc npc;
		final int index = world.getParameters().getInt("index");
		
		// first room
		npcs = FIRST_ROOM[index];
		spawns = FIRST_ROOM_SPAWNS[index];
		if (npcs != null)
		{
			final List<Spawn> firstRoom = new ArrayList<>(spawns.length - 1);
			final int shaman = getRandom(spawns.length); // random position for shaman
			for (int i = 0; i < spawns.length; i++)
			{
				if (i == shaman)
				{
					// stealth shaman use same npcId as other mobs
					npc = addSpawn(STEALTH_SHAMAN ? npcs[1] : npcs[0], spawns[i][0], spawns[i][1], spawns[i][2], 0, false, 0, false, world.getId());
					world.setParameter("shaman", npc.getObjectId());
				}
				else
				{
					npc = addSpawn(npcs[1], spawns[i][0], spawns[i][1], spawns[i][2], 0, false, 0, false, world.getId());
					final Spawn spawn = npc.getSpawn();
					spawn.setRespawnDelay(FIRST_ROOM_RESPAWN_DELAY);
					spawn.setAmount(1);
					spawn.startRespawn();
					firstRoom.add(spawn); // store mobs spawns
				}
				world.setParameter("firstRoom", firstRoom);
				npc.setRandomWalking(true);
			}
		}
		
		// second room
		npcs = SECOND_ROOM[index];
		spawns = SECOND_ROOM_SPAWNS[index];
		if (npcs != null)
		{
			final List<Integer> secondRoom = new ArrayList<>(spawns.length);
			for (int[] spawn : spawns)
			{
				npc = addSpawn(npcs[0], spawn[0], spawn[1], spawn[2], 0, false, 0, false, world.getId());
				npc.setRandomWalking(true);
				secondRoom.add(npc.getObjectId());
			}
			world.setParameter("secondRoom", secondRoom);
		}
		
		// miniboss
		if (MINIBOSS[index] != null)
		{
			npc = addSpawn(MINIBOSS[index][0], MINIBOSS[index][1], MINIBOSS[index][2], MINIBOSS[index][3], 0, false, 0, false, world.getId());
			npc.setRandomWalking(true);
			world.setParameter("miniBoss", npc.getObjectId());
		}
		
		// escape teleporter
		if (TELEPORTERS[index] != null)
		{
			Npc teleporter = addSpawn(TELEPORTER, TELEPORTERS[index][0], TELEPORTERS[index][1], TELEPORTERS[index][2], 0, false, 0, false, world.getId());
			ThreadPool.schedule(() -> teleporter.deleteMe(), 30000);
		}
		
		// boss
		npc = addSpawn(BOSS[index][0], BOSS[index][1], BOSS[index][2], BOSS[index][3], 0, false, 0, false, world.getId());
		world.setParameter("boss", npc);
	}
	
	/**
	 * Handles only player's enter, single parameter - integer kamaloka index
	 */
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (player == null)
		{
			return null;
		}
		
		int lvl = player.getLevel();
		// @formatter:off
        Map<String, int[][]> levelRanges = new HashMap<>();
        levelRanges.put("Hole", new int[][]
        {
            {68, 78, 15},
            {61, 71, 13},
            {58, 68, 12},
            {51, 61, 10},
            {48, 58, 9}
        });
        levelRanges.put("Labyrinth", new int[][]
        {
            {78, Integer.MAX_VALUE, 18},
            {76, Integer.MAX_VALUE, 17},
            {73, 83, 16},
            {64, 74, 14},
            {54, 64, 11}
        });
        // @formatter:on
		
		int Hole = getZone(lvl, levelRanges.get("Hole"));
		int Labyrinth = getZone(lvl, levelRanges.get("Labyrinth"));
		
		int HoleLvl = HoleLevels.getOrDefault(Hole, 0);
		int LabyrinthLvl = LabyrinthLevels.getOrDefault(Labyrinth, 0);
		
		switch (event)
		{
			case "Hole":
			{
				enterInstance(player, Hole);
				break;
			}
			case "Labyrinth":
			{
				enterInstance(player, Labyrinth);
				break;
			}
			case "HoleNo":
			{
				player.sendMessage("심연의 홀은 레벨이 초과되어 입장할 수 없습니다.");
				startQuestTimer("show", 0, null, player);
				break;
			}
			case "LabyrinthNo":
			{
				player.sendMessage("심연의 미궁은 레벨이 초과되어 입장할 수 없습니다.");
				startQuestTimer("show", 0, null, player);
				break;
			}
			case "show":
			{
				String result;
				String holeFinal = (HoleLvl > 0) ? "<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h Quest Kamaloka Hole\">심연의 홀에 들어간다 (대상레벨: " + HoleLvl + ")</Button>" : "<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h Quest Kamaloka HoleNo\">심연의 홀에 들어간다 (입장불가)</Button>";
				String labyFinal = (LabyrinthLvl > 0) ? "<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h Quest Kamaloka Labyrinth\">심연의 미궁에 들어간다 (대상레벨: " + LabyrinthLvl + ")</Button>" : "<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h Quest Kamaloka LabyrinthNo\">심연의 미궁에 들어간다 (입장불가)</Button>";
				Instance world = getPlayerInstance(player);
				
				if ((world != null) && isInstanceAllowed(world.getTemplateId()))
				{
					InstanceTemplate template = InstanceManager.getInstance().getInstanceTemplate(world.getTemplateId());
					result = getHtm(player, "31340-1.htm").replace("%instantName%", template.getName());
				}
				else
				{
					result = getHtm(player, "31340.htm").replace("%Hole%", holeFinal).replace("%Labyrinth%", labyFinal);
				}
				return result;
			}
			case "reenter":
			{
				Instance world = getPlayerInstance(player);
				if (world != null)
				{
					int templateIdToFind = world.getTemplateId();
					List<Integer> templateIdsList = Arrays.stream(TEMPLATE_IDS).boxed().collect(Collectors.toList());
					int index = templateIdsList.indexOf(templateIdToFind);
					
					enterInstance(player, index);
				}
				break;
			}
			case "giveup":
			{
				giveUp(player);
				break;
			}
		}
		return null;
	}
	
	/**
	 * Talk with captains and using of the escape teleporter
	 */
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final int npcId = npc.getId();
		if (npcId == TELEPORTER)
		{
			final Instance world = npc.getInstanceWorld();
			if (world != null)
			{
				final Party party = player.getParty();
				// only party leader can talk with escape teleporter
				if ((party != null) && party.isLeader(player))
				{
					// party members must be in the instance
					if (world.isAllowed(player))
					{
						// teleports entire party away
						for (Player partyMember : party.getMembers())
						{
							if ((partyMember != null) && (partyMember.getInstanceId() == world.getId()))
							{
								partyMember.getInstanceWorld().ejectPlayer(partyMember);
							}
						}
						world.finishInstance(0);
					}
				}
			}
		}
		else
		{
			return npcId + ".htm";
		}
		return null;
	}
	
	/**
	 * Only escape teleporters first talk handled
	 */
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getId() == TELEPORTER)
		{
			if (player.isInParty() && player.getParty().isLeader(player))
			{
				return "32496.htm";
			}
			return "32496-no.htm";
		}
		return null;
	}
	
	private void giveUp(Player player)
	{
		Instance world = getPlayerInstance(player);
		final long times = System.currentTimeMillis() + (1 * 3600000) + (30 * 60000);
		
		// player already in the instance
		if (world != null)
		{
			final Party party = player.getParty();
			// only party leader can talk with escape teleporter
			if (party != null)
			{
				for (Player partyMember : party.getMembers())
				{
					if (((world.getTemplateId() >= 57) && (world.getTemplateId() <= 79)) || (world.getTemplateId() == 134))
					{
						final InstanceTemplate template = InstanceManager.getInstance().getInstanceTemplate(world.getTemplateId());
						partyMember.sendMessage(template.getName() + "을 포기하였습니다.");
						InstanceManager.getInstance().setReenterPenalty(partyMember.getObjectId(), world.getTemplateId(), times);
						world.finishInstance(0);
					}
				}
			}
			else
			{
				for (Player recentPartyMember : world.getPlayers())
				{
					final InstanceTemplate template = InstanceManager.getInstance().getInstanceTemplate(world.getTemplateId());
					recentPartyMember.sendMessage(template.getName() + "을 포기하였습니다.");
					InstanceManager.getInstance().setReenterPenalty(recentPartyMember.getObjectId(), world.getTemplateId(), times);
					world.finishInstance(0);
				}
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			final int objectId = npc.getObjectId();
			
			// first room was spawned ?
			if (world.getParameters().getList("firstRoom", Spawn.class) != null)
			{
				// is shaman killed ?
				if ((world.getParameters().getInt("shaman", 0) != 0) && (world.getParameters().getInt("shaman", 0) == objectId))
				{
					world.setParameter("shaman", 0);
					// stop respawn of the minions
					for (Spawn spawn : world.getParameters().getList("firstRoom", Spawn.class))
					{
						if (spawn != null)
						{
							spawn.stopRespawn();
						}
					}
					world.getParameters().remove("firstRoom");
					
					if (world.getParameters().getObject("boss", Npc.class) != null)
					{
						final int skillId = FIRST_ROOM[world.getParameters().getInt("index")][2];
						final int skillLevel = FIRST_ROOM[world.getParameters().getInt("index")][3];
						if ((skillId != 0) && (skillLevel != 0))
						{
							final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
							if (skill != null)
							{
								skill.applyEffects(world.getParameters().getObject("boss", Npc.class), world.getParameters().getObject("boss", Npc.class));
							}
						}
					}
					
					return super.onKill(npc, player, isSummon);
				}
			}
			
			// second room was spawned ?
			if (world.getParameters().getList("secondRoom", Integer.class) != null)
			{
				boolean all = true;
				// check for all mobs in the second room
				for (int i = 0; i < world.getParameters().getList("secondRoom", Integer.class).size(); i++)
				{
					// found killed now mob
					if (world.getParameters().getList("secondRoom", Integer.class).get(i) == objectId)
					{
						world.getParameters().getList("secondRoom", Integer.class).set(i, 0);
					}
					else if (world.getParameters().getList("secondRoom", Integer.class).get(i) != 0)
					{
						all = false;
					}
				}
				// all mobs killed ?
				if (all)
				{
					world.getParameters().remove("secondRoom");
					
					if (world.getParameters().getObject("boss", Npc.class) != null)
					{
						final int skillId = SECOND_ROOM[world.getParameters().getInt("index")][1];
						final int skillLevel = SECOND_ROOM[world.getParameters().getInt("index")][2];
						if ((skillId != 0) && (skillLevel != 0))
						{
							final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
							if (skill != null)
							{
								skill.applyEffects(world.getParameters().getObject("boss", Npc.class), world.getParameters().getObject("boss", Npc.class));
							}
						}
					}
					
					return super.onKill(npc, player, isSummon);
				}
			}
			
			// miniboss spawned ?
			if ((world.getParameters().getInt("miniBoss", 0) != 0) && (world.getParameters().getInt("miniBoss", 0) == objectId))
			{
				world.setParameter("miniBoss", 0);
				if (world.getParameters().getObject("boss", Npc.class) != null)
				{
					final int skillId = MINIBOSS[world.getParameters().getInt("index")][4];
					final int skillLevel = MINIBOSS[world.getParameters().getInt("index")][5];
					if ((skillId != 0) && (skillLevel != 0))
					{
						final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
						if (skill != null)
						{
							skill.applyEffects(world.getParameters().getObject("boss", Npc.class), world.getParameters().getObject("boss", Npc.class));
						}
					}
				}
				
				return super.onKill(npc, player, isSummon);
			}
			
			// boss was killed, finish instance
			if ((world.getParameters().getObject("boss", Npc.class) != null) && (world.getParameters().getObject("boss", Npc.class) == npc))
			{
				world.getParameters().remove("boss");
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.INSTANCE_ZONE_S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_POSSIBLE_ENTRY_TIME_BY_USING_THE_COMMAND_INSTANCEZONE);
				sm.addInstanceName(world.getTemplateId());
				
				final long times = System.currentTimeMillis() + (1 * 3600000) + (30 * 60000);
				
				// set instance reenter time for all allowed players
				for (Player plr : world.getAllowed())
				{
					if (plr != null)
					{
						InstanceManager.getInstance().setReenterPenalty(plr.getObjectId(), world.getTemplateId(), times);
						if (plr.isOnline())
						{
							plr.sendPacket(sm);
							plr.addItem("카마로카 보상 상자", 23956, Rnd.get(1, 3), null, true);
							EventDispatcher.getInstance().notifyEventAsync(new OnPlayerKamalokaMania(plr), plr);
						}
					}
				}
				world.finishInstance(1);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private static int getZone(int lvl, int[][] levelRanges)
	{
		for (int[] range : levelRanges)
		{
			if ((lvl >= range[0]) && (lvl <= range[1]))
			{
				return range[2];
			}
		}
		return 0;
	}
	
	private boolean isInstanceAllowed(int templateId)
	{
		return ((templateId >= 57) && (templateId <= 79)) || (templateId == 134);
	}
	
	@Override
	public void onInstanceLeave(Player player, Instance instance)
	{
		player.stopSkillEffects(SkillFinishType.REMOVED, 30267);
		player.sendPacket(new ExSendUIEvent(player, false, false, 0, 0, NpcStringId.REMAINING_TIME));
	}
	
	public static void main(String[] args)
	{
		new Kamaloka();
	}
}
