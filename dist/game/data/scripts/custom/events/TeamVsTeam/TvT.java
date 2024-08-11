/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.events.TeamVsTeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.enums.PartyDistributionType;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.enums.Team;
import org.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.CommandChannel;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureDeath;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogout;
import org.l2jmobius.gameserver.model.events.listeners.AbstractEventListener;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.instancezone.InstanceTemplate;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.quest.Event;
import org.l2jmobius.gameserver.model.quest.QuestTimer;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.Util;

/**
 * Team vs Team event.
 * @author Mobius, 보리넷 가츠
 */
public class TvT extends Event
{
	// private static final Logger LOG = Logger.getLogger(TvT.class.getName());
	// NPC
	private static final int MANAGER = 70010;
	boolean forfeit = false;
	
	// Skills
	private static final SkillHolder[] FIGHTER_BUFFS =
	{
		new SkillHolder(4322, 1), // Wind Walk
		new SkillHolder(4323, 1), // Shield
		new SkillHolder(5637, 1), // Magic Barrier
		new SkillHolder(4324, 1), // Bless the Body
		new SkillHolder(4325, 1), // Vampiric Rage
		new SkillHolder(4326, 1), // Regeneration
		new SkillHolder(5632, 1), // Haste
	};
	private static final SkillHolder[] MAGE_BUFFS =
	{
		new SkillHolder(4322, 1), // Wind Walk
		new SkillHolder(4323, 1), // Shield
		new SkillHolder(5637, 1), // Magic Barrier
		new SkillHolder(4324, 1), // Bless the Body
		new SkillHolder(4329, 1), // Acumen
		new SkillHolder(4330, 1), // Concentration
		new SkillHolder(4331, 1), // Empower
	};
	private static final SkillHolder GHOST_WALKING = new SkillHolder(100000, 1); // Custom Ghost Walking
	// Others
	private static final int INSTANCE_ID = 93;
	private static final int BLUE_DOOR_ID = 24190002;
	private static final int RED_DOOR_ID = 24190003;
	private static final Location MANAGER_SPAWN_LOC = new Location(83425, 148585, -3406, 32938);
	private static final Location BLUE_BUFFER_SPAWN_LOC = new Location(147450, 46913, -3400, 49000);
	private static final Location RED_BUFFER_SPAWN_LOC = new Location(151545, 46528, -3400, 16000);
	private static final Location BLUE_SPAWN_LOC = new Location(147447, 46722, -3416);
	private static final Location RED_SPAWN_LOC = new Location(151536, 46722, -3416);
	private static final ZoneType BLUE_PEACE_ZONE = ZoneManager.getInstance().getZoneByName("colosseum_peace1");
	private static final ZoneType RED_PEACE_ZONE = ZoneManager.getInstance().getZoneByName("colosseum_peace2");
	private static final Location GIRAN_LOC = new Location(83292, 148616, -3393);
	// Config Settings
	private static final int REGISTRATION_TIME = Config.TvT_REGISTRATION_TIME; // Minutes
	private static final int WAIT_TIME = Config.TvT_WAIT_TIME; // Minutes
	private static final int FIGHT_TIME = Config.TvT_FIGHT_TIME; // Minutes
	private static final int INACTIVITY_TIME = 1; // Minutes
	private static final int MINIMUM_PARTICIPANT_LEVEL_LOW = Config.TvT_MINIMUM_PARTICIPANT_LEVEL_LOW;
	private static final int MAXIMUM_PARTICIPANT_LEVEL_LOW = Config.TvT_MAXIMUM_PARTICIPANT_LEVEL_LOW;
	private static final int MINIMUM_PARTICIPANT_LEVEL_MIDDLE = Config.TvT_MINIMUM_PARTICIPANT_LEVEL_MIDDLE;
	private static final int MAXIMUM_PARTICIPANT_LEVEL_MIDDLE = Config.TvT_MAXIMUM_PARTICIPANT_LEVEL_MIDDLE;
	private static final int MINIMUM_PARTICIPANT_LEVEL_HIGH = Config.TvT_MINIMUM_PARTICIPANT_LEVEL_HIGH;
	private static final int MAXIMUM_PARTICIPANT_LEVEL_HIGH = Config.TvT_MAXIMUM_PARTICIPANT_LEVEL_HIGH;
	
	private static final int MINIMUM_PARTICIPANT_COUNT_LOW = Config.TvT_MINIMUM_PARTICIPANT_COUNT_LOW;
	private static final int MINIMUM_PARTICIPANT_COUNT_MIDDLE = Config.TvT_MINIMUM_PARTICIPANT_COUNT_MIDDLE;
	private static final int MINIMUM_PARTICIPANT_COUNT_HIGH = Config.TvT_MINIMUM_PARTICIPANT_COUNT_HIGH;
	private static final int MAXIMUM_PARTICIPANT_COUNT = 24; // Scoreboard has 25 slots
	private static final int PARTY_MEMBER_COUNT = 12;
	// Misc
	private static final Map<Player, Integer> PLAYER_SCORES = new ConcurrentHashMap<>();
	private static final Set<Player> PLAYER_LIST = ConcurrentHashMap.newKeySet();
	private static final Set<Player> BLUE_TEAM = ConcurrentHashMap.newKeySet();
	private static final Set<Player> RED_TEAM = ConcurrentHashMap.newKeySet();
	private static volatile int BLUE_SCORE;
	private static volatile int RED_SCORE;
	private static Instance PVP_WORLD = null;
	private static Npc MANAGER_NPC_INSTANCE = null;
	private static boolean EVENT_ACTIVE = false;
	private static int level = 0;
	
	private TvT()
	{
		if (BorinetTask._isActive)
		{
			return;
		}
		addTalkId(MANAGER);
		addFirstTalkId(MANAGER);
		addExitZoneId(BLUE_PEACE_ZONE.getId(), RED_PEACE_ZONE.getId());
		addEnterZoneId(BLUE_PEACE_ZONE.getId(), RED_PEACE_ZONE.getId());
		
		if (Config.TvT_EVENT_ENABLE_LOW)
		{
			ThreadPool.scheduleAtFixedRate(() -> startEvent(1), BorinetTask.TvTLowEventStart(), BorinetUtil.MILLIS_PER_DAY); // 1 day
		}
		if (Config.TvT_EVENT_ENABLE_MIDDLE)
		{
			ThreadPool.scheduleAtFixedRate(() -> startEvent(2), BorinetTask.TvTMiddleEventStart(), BorinetUtil.MILLIS_PER_DAY); // 1 day
		}
		if (Config.TvT_EVENT_ENABLE_HIGH)
		{
			ThreadPool.scheduleAtFixedRate(() -> startEvent(3), BorinetTask.TvTHighEventStart(), BorinetUtil.MILLIS_PER_DAY); // 1 day
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (!EVENT_ACTIVE)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "info":
			{
				htmltext = getHtm(player, "info.html");
				htmltext = htmltext.replace("%fight%", String.valueOf(FIGHT_TIME));
				switch (level)
				{
					case 1:
						htmltext = htmltext.replace("%minLvl%", String.valueOf(MINIMUM_PARTICIPANT_LEVEL_LOW));
						htmltext = htmltext.replace("%maxLvl%", String.valueOf(MAXIMUM_PARTICIPANT_LEVEL_LOW));
						htmltext = htmltext.replace("%size%", String.valueOf(MINIMUM_PARTICIPANT_COUNT_LOW));
						break;
					case 2:
						htmltext = htmltext.replace("%minLvl%", String.valueOf(MINIMUM_PARTICIPANT_LEVEL_MIDDLE));
						htmltext = htmltext.replace("%maxLvl%", String.valueOf(MAXIMUM_PARTICIPANT_LEVEL_MIDDLE));
						htmltext = htmltext.replace("%size%", String.valueOf(MINIMUM_PARTICIPANT_COUNT_MIDDLE));
						break;
					case 3:
						htmltext = htmltext.replace("%minLvl%", String.valueOf(MINIMUM_PARTICIPANT_LEVEL_HIGH));
						htmltext = htmltext.replace("%maxLvl%", String.valueOf(MAXIMUM_PARTICIPANT_LEVEL_HIGH));
						htmltext = htmltext.replace("%size%", String.valueOf(MINIMUM_PARTICIPANT_COUNT_HIGH));
						break;
				}
				break;
			}
			case "list":
			{
				int list = PLAYER_LIST.size();
				htmltext = getHtm(player, "manager-list.html");
				htmltext = htmltext.replace("%list%", String.valueOf(list));
				break;
			}
			case "back":
			{
				htmltext = "manager-register.html";
				break;
			}
			case "Participate":
			{
				if (canRegister(player))
				{
					if ((Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP == 0) || AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.L2EVENT_ID, player, Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP))
					{
						PLAYER_LIST.add(player);
						PLAYER_SCORES.put(player, 0);
						player.setOnEvent(true);
						addLogoutListener(player);
						htmltext = getHtm(player, "registration-success.html");
						htmltext = htmltext.replace("%list%", String.valueOf(PLAYER_LIST.size()));
					}
					else
					{
						htmltext = "registration-ip.html";
					}
				}
				break;
			}
			case "CancelParticipation":
			{
				if (player.isOnEvent())
				{
					return null;
				}
				// Remove the player from the IP count
				if (Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP > 0)
				{
					AntiFeedManager.getInstance().removePlayer(AntiFeedManager.L2EVENT_ID, player);
				}
				PLAYER_LIST.remove(player);
				PLAYER_SCORES.remove(player);
				removeListeners(player);
				player.setOnEvent(false);
				htmltext = "registration-canceled.html";
				break;
			}
			case "BuffHeal":
			{
				if (player.isOnEvent() || player.isGM())
				{
					if (player.isInCombat())
					{
						htmltext = "manager-combat.html";
					}
					else
					{
						if (player.isMageClass())
						{
							for (SkillHolder skill : MAGE_BUFFS)
							{
								SkillCaster.triggerCast(npc, player, skill.getSkill());
							}
						}
						else
						{
							for (SkillHolder skill : FIGHTER_BUFFS)
							{
								SkillCaster.triggerCast(npc, player, skill.getSkill());
							}
						}
						player.setCurrentHp(player.getMaxHp());
						player.setCurrentMp(player.getMaxMp());
						player.setCurrentCp(player.getMaxCp());
					}
				}
				break;
			}
			case "TeleportToArena":
			{
				// Remove offline players.
				for (Player participant : PLAYER_LIST)
				{
					if ((participant == null) || (participant.isOnlineInt() != 1))
					{
						PLAYER_LIST.remove(participant);
						PLAYER_SCORES.remove(participant);
					}
				}
				boolean SIZE = false;
				switch (level)
				{
					case 1:
						if (PLAYER_LIST.size() >= MINIMUM_PARTICIPANT_COUNT_LOW)
						{
							SIZE = true;
						}
						break;
					case 2:
						if (PLAYER_LIST.size() >= MINIMUM_PARTICIPANT_COUNT_MIDDLE)
						{
							SIZE = true;
						}
						break;
					case 3:
						if (PLAYER_LIST.size() >= MINIMUM_PARTICIPANT_COUNT_HIGH)
						{
							SIZE = true;
						}
						break;
				}
				if (SIZE)
				{
					Broadcast.toAllOnlinePlayers("TvT 이벤트: TvT 이벤트 경기가 진행됩니다. 무운을 빕니다!");
				}
				// Check if there are enough players to start the event.
				if (!SIZE)
				{
					Broadcast.toAllOnlinePlayers("TvT 이벤트: 참가자가 충분하지 않아 이벤트가 취소되었습니다.");
					// LOG.info("TvT 이벤트: TvT 이벤트가 취소되었습니다.");
					for (Player participant : PLAYER_LIST)
					{
						removeListeners(participant);
						participant.setTeam(Team.NONE);
						participant.setOnEvent(false);
					}
					level = 0;
					EVENT_ACTIVE = false;
					return null;
				}
				// Create the instance.
				final InstanceManager manager = InstanceManager.getInstance();
				final InstanceTemplate template = manager.getInstanceTemplate(INSTANCE_ID);
				PVP_WORLD = manager.createInstance(template, null);
				// Randomize player list and separate teams.
				final List<Player> playerList = new ArrayList<>(PLAYER_LIST.size());
				playerList.addAll(PLAYER_LIST);
				Collections.shuffle(playerList);
				PLAYER_LIST.clear();
				PLAYER_LIST.addAll(playerList);
				boolean team = getRandomBoolean(); // If teams are not even, randomize where extra player goes.
				for (Player participant : PLAYER_LIST)
				{
					if (participant.hasSummon())
					{
						final Summon pet = participant.getPet();
						if (pet != null)
						{
							pet.setRestoreSummon(true);
							pet.unSummon(participant);
						}
						participant.getServitors().values().forEach(s ->
						{
							s.setRestoreSummon(true);
							s.unSummon(participant);
						});
					}
					if (team)
					{
						BLUE_TEAM.add(participant);
						PVP_WORLD.addAllowed(participant);
						participant.leaveParty();
						participant.setTeam(Team.BLUE);
						participant.teleToLocation(BLUE_SPAWN_LOC, PVP_WORLD);
						team = false;
						participant.stopAllEffectsExceptThoseThatLastThroughDeath();
						participant.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
					}
					else
					{
						RED_TEAM.add(participant);
						PVP_WORLD.addAllowed(participant);
						participant.leaveParty();
						participant.setTeam(Team.RED);
						participant.teleToLocation(RED_SPAWN_LOC, PVP_WORLD);
						team = true;
						participant.stopAllEffectsExceptThoseThatLastThroughDeath();
						participant.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
					}
					addDeathListener(participant);
				}
				// Make Blue CC.
				if (BLUE_TEAM.size() > 1)
				{
					CommandChannel blueCC = null;
					Party lastBlueParty = null;
					int blueParticipantCounter = 0;
					for (Player participant : BLUE_TEAM)
					{
						blueParticipantCounter++;
						if (blueParticipantCounter == 1)
						{
							lastBlueParty = new Party(participant, PartyDistributionType.FINDERS_KEEPERS);
							participant.joinParty(lastBlueParty);
							if (BLUE_TEAM.size() > PARTY_MEMBER_COUNT)
							{
								if (blueCC == null)
								{
									blueCC = new CommandChannel(participant);
								}
								else
								{
									blueCC.addParty(lastBlueParty);
								}
							}
						}
						else
						{
							participant.joinParty(lastBlueParty);
						}
						if (blueParticipantCounter == PARTY_MEMBER_COUNT)
						{
							blueParticipantCounter = 0;
						}
					}
				}
				// Make Red CC.
				if (RED_TEAM.size() > 1)
				{
					CommandChannel redCC = null;
					Party lastRedParty = null;
					int redParticipantCounter = 0;
					for (Player participant : RED_TEAM)
					{
						redParticipantCounter++;
						if (redParticipantCounter == 1)
						{
							lastRedParty = new Party(participant, PartyDistributionType.FINDERS_KEEPERS);
							participant.joinParty(lastRedParty);
							if (RED_TEAM.size() > PARTY_MEMBER_COUNT)
							{
								if (redCC == null)
								{
									redCC = new CommandChannel(participant);
								}
								else
								{
									redCC.addParty(lastRedParty);
								}
							}
						}
						else
						{
							participant.joinParty(lastRedParty);
						}
						if (redParticipantCounter == PARTY_MEMBER_COUNT)
						{
							redParticipantCounter = 0;
						}
					}
				}
				// Spawn managers.
				addSpawn(MANAGER, BLUE_BUFFER_SPAWN_LOC, false, (WAIT_TIME + FIGHT_TIME) * 60000, false, PVP_WORLD.getId());
				addSpawn(MANAGER, RED_BUFFER_SPAWN_LOC, false, (WAIT_TIME + FIGHT_TIME) * 60000, false, PVP_WORLD.getId());
				// Initialize scores.
				BLUE_SCORE = 0;
				RED_SCORE = 0;
				// Initialize scoreboard.
				PVP_WORLD.broadcastPacket(new ExPVPMatchCCRecord(ExPVPMatchCCRecord.INITIALIZE, Util.sortByValue(PLAYER_SCORES, true)));
				// Schedule start.
				startQuestTimer("5", (WAIT_TIME * 60000) - 5000, null, null);
				startQuestTimer("4", (WAIT_TIME * 60000) - 4000, null, null);
				startQuestTimer("3", (WAIT_TIME * 60000) - 3000, null, null);
				startQuestTimer("2", (WAIT_TIME * 60000) - 2000, null, null);
				startQuestTimer("1", (WAIT_TIME * 60000) - 1000, null, null);
				startQuestTimer("StartFight", WAIT_TIME * 60000, null, null);
				break;
			}
			case "StartFight":
			{
				// Open doors.
				openDoor(BLUE_DOOR_ID, PVP_WORLD.getId());
				openDoor(RED_DOOR_ID, PVP_WORLD.getId());
				// Send message.
				broadcastScreenMessageWithEffect("결투가 " + FIGHT_TIME + "분간 지속됩니다!", 10);
				// Schedule finish.
				startQuestTimer("10", (FIGHT_TIME * 60000) - 10000, null, null);
				startQuestTimer("9", (FIGHT_TIME * 60000) - 9000, null, null);
				startQuestTimer("8", (FIGHT_TIME * 60000) - 8000, null, null);
				startQuestTimer("7", (FIGHT_TIME * 60000) - 7000, null, null);
				startQuestTimer("6", (FIGHT_TIME * 60000) - 6000, null, null);
				startQuestTimer("5", (FIGHT_TIME * 60000) - 5000, null, null);
				startQuestTimer("4", (FIGHT_TIME * 60000) - 4000, null, null);
				startQuestTimer("3", (FIGHT_TIME * 60000) - 3000, null, null);
				startQuestTimer("2", (FIGHT_TIME * 60000) - 2000, null, null);
				startQuestTimer("1", (FIGHT_TIME * 60000) - 1000, null, null);
				startQuestTimer("EndFight", FIGHT_TIME * 60000, null, null);
				break;
			}
			case "EndFight":
			{
				// Close doors.
				closeDoor(BLUE_DOOR_ID, PVP_WORLD.getId());
				closeDoor(RED_DOOR_ID, PVP_WORLD.getId());
				// Disable players.
				for (Player participant : PLAYER_LIST)
				{
					participant.setInvul(true);
					participant.setImmobilized(true);
					participant.disableAllSkills();
					for (Summon summon : participant.getServitors().values())
					{
						summon.setInvul(true);
						summon.setImmobilized(true);
						summon.disableAllSkills();
					}
				}
				// Make sure noone is dead.
				for (Player participant : PLAYER_LIST)
				{
					if (participant.isDead())
					{
						participant.doRevive();
					}
				}
				if (forfeit)
				{
					if (BLUE_TEAM.isEmpty() && !RED_TEAM.isEmpty())
					{
						final Skill skill = CommonSkill.FIREWORK.getSkill();
						broadcastScreenMessageWithEffect("레드팀이 이벤트에서 우승했습니다!", 7);
						for (Player participant : RED_TEAM)
						{
							if ((participant != null) && (participant.getInstanceWorld() == PVP_WORLD))
							{
								participant.broadcastPacket(new MagicSkillUse(participant, participant, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
								participant.broadcastSocialAction(3);
								switch (level)
								{
									case 1:
										rewardTvT(participant, Config.TvT_REWARD_ITEMS_WINNERS_LOW);
										break;
									case 2:
										rewardTvT(participant, Config.TvT_REWARD_ITEMS_WINNERS_MIDDLE);
										break;
									case 3:
										rewardTvT(participant, Config.TvT_REWARD_ITEMS_WINNERS_HIGH);
										break;
								}
							}
						}
					}
					else if (!BLUE_TEAM.isEmpty() && RED_TEAM.isEmpty())
					{
						final Skill skill = CommonSkill.FIREWORK.getSkill();
						broadcastScreenMessageWithEffect("블루팀이 이벤트에서 우승했습니다!", 7);
						for (Player participant : BLUE_TEAM)
						{
							if ((participant != null) && (participant.getInstanceWorld() == PVP_WORLD))
							{
								participant.broadcastPacket(new MagicSkillUse(participant, participant, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
								participant.broadcastSocialAction(3);
								switch (level)
								{
									case 1:
										rewardTvT(participant, Config.TvT_REWARD_ITEMS_WINNERS_LOW);
										break;
									case 2:
										rewardTvT(participant, Config.TvT_REWARD_ITEMS_WINNERS_MIDDLE);
										break;
									case 3:
										rewardTvT(participant, Config.TvT_REWARD_ITEMS_WINNERS_HIGH);
										break;
								}
							}
						}
					}
				}
				else
				{
					// Team Blue wins.
					if (BLUE_SCORE > RED_SCORE)
					{
						final Skill skill = CommonSkill.FIREWORK.getSkill();
						broadcastScreenMessageWithEffect("블루팀이 이벤트에서 우승했습니다!", 7);
						for (Player winners : BLUE_TEAM)
						{
							if ((winners != null) && (winners.getInstanceWorld() == PVP_WORLD))
							{
								winners.broadcastPacket(new MagicSkillUse(winners, winners, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
								winners.broadcastSocialAction(3);
								switch (level)
								{
									case 1:
										rewardTvT(winners, Config.TvT_REWARD_ITEMS_WINNERS_LOW);
										break;
									case 2:
										rewardTvT(winners, Config.TvT_REWARD_ITEMS_WINNERS_MIDDLE);
										break;
									case 3:
										rewardTvT(winners, Config.TvT_REWARD_ITEMS_WINNERS_HIGH);
										break;
								}
							}
						}
						for (Player losers : RED_TEAM)
						{
							if ((losers != null) && (losers.getInstanceWorld() == PVP_WORLD))
							{
								losers.broadcastPacket(new MagicSkillUse(losers, losers, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
								losers.broadcastSocialAction(3);
								switch (level)
								{
									case 1:
										rewardTvT(losers, Config.TvT_REWARD_ITEMS_LOSERS_LOW);
										break;
									case 2:
										rewardTvT(losers, Config.TvT_REWARD_ITEMS_LOSERS_MIDDLE);
										break;
									case 3:
										rewardTvT(losers, Config.TvT_REWARD_ITEMS_LOSERS_HIGH);
										break;
								}
							}
						}
					}
					// Team Red wins.
					else if (RED_SCORE > BLUE_SCORE)
					{
						final Skill skill = CommonSkill.FIREWORK.getSkill();
						broadcastScreenMessageWithEffect("레드팀이 이벤트에서 우승했습니다!", 7);
						for (Player winners : RED_TEAM)
						{
							if ((winners != null) && (winners.getInstanceWorld() == PVP_WORLD))
							{
								winners.broadcastPacket(new MagicSkillUse(winners, winners, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
								winners.broadcastSocialAction(3);
								switch (level)
								{
									case 1:
										rewardTvT(winners, Config.TvT_REWARD_ITEMS_WINNERS_LOW);
										break;
									case 2:
										rewardTvT(winners, Config.TvT_REWARD_ITEMS_WINNERS_MIDDLE);
										break;
									case 3:
										rewardTvT(winners, Config.TvT_REWARD_ITEMS_WINNERS_HIGH);
										break;
								}
							}
						}
						for (Player losers : BLUE_TEAM)
						{
							if ((losers != null) && (losers.getInstanceWorld() == PVP_WORLD))
							{
								losers.broadcastPacket(new MagicSkillUse(losers, losers, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
								losers.broadcastSocialAction(3);
								switch (level)
								{
									case 1:
										rewardTvT(losers, Config.TvT_REWARD_ITEMS_LOSERS_LOW);
										break;
									case 2:
										rewardTvT(losers, Config.TvT_REWARD_ITEMS_LOSERS_MIDDLE);
										break;
									case 3:
										rewardTvT(losers, Config.TvT_REWARD_ITEMS_LOSERS_HIGH);
										break;
								}
							}
						}
					}
					// Tie.
					else
					{
						broadcastScreenMessageWithEffect("이벤트는 동점으로 종료되었습니다!", 7);
						for (Player list : PLAYER_LIST)
						{
							list.broadcastSocialAction(13);
							switch (level)
							{
								case 1:
									rewardTvT(list, Config.TvT_REWARD_ITEMS_LOSERS_LOW);
									break;
								case 2:
									rewardTvT(list, Config.TvT_REWARD_ITEMS_LOSERS_MIDDLE);
									break;
								case 3:
									rewardTvT(list, Config.TvT_REWARD_ITEMS_LOSERS_HIGH);
									break;
							}
						}
					}
				}
				startQuestTimer("ScoreBoard", 3500, null, null);
				startQuestTimer("TeleportOut", 7000, null, null);
				level = 0;
				// LOG.info("TvT 이벤트: TvT 이벤트가 종료되었습니다.");
				break;
			}
			case "ScoreBoard":
			{
				PVP_WORLD.broadcastPacket(new ExPVPMatchCCRecord(ExPVPMatchCCRecord.FINISH, Util.sortByValue(PLAYER_SCORES, true)));
				break;
			}
			case "TeleportOut":
			{
				// Remove event listeners.
				for (Player participant : PLAYER_LIST)
				{
					removeListeners(participant);
					participant.setTeam(Team.NONE);
					participant.setOnEvent(false);
					participant.leaveParty();
				}
				// Destroy world.
				if (PVP_WORLD != null)
				{
					PVP_WORLD.destroy();
					PVP_WORLD = null;
				}
				// Enable players.
				for (Player participant : PLAYER_LIST)
				{
					participant.setInvul(false);
					participant.setImmobilized(false);
					participant.enableAllSkills();
					for (Summon summon : participant.getServitors().values())
					{
						summon.setInvul(true);
						summon.setImmobilized(true);
						summon.disableAllSkills();
					}
				}
				PLAYER_LIST.clear();
				EVENT_ACTIVE = false;
				break;
			}
			case "ResurrectPlayer":
			{
				if (player.isDead() && player.isOnEvent())
				{
					if (BLUE_TEAM.contains(player))
					{
						player.setIsPendingRevive(true);
						player.teleToLocation(BLUE_SPAWN_LOC, false, PVP_WORLD);
						// Make player invulnerable for 30 seconds.
						GHOST_WALKING.getSkill().applyEffects(player, player);
						// Reset existing activity timers.
						resetActivityTimers(player); // In case player died in peace zone.
					}
					else if (RED_TEAM.contains(player))
					{
						player.setIsPendingRevive(true);
						player.teleToLocation(RED_SPAWN_LOC, false, PVP_WORLD);
						// Make player invulnerable for 30 seconds.
						GHOST_WALKING.getSkill().applyEffects(player, player);
						// Reset existing activity timers.
						resetActivityTimers(player); // In case player died in peace zone.
					}
				}
				break;
			}
			case "10":
			case "9":
			case "8":
			case "7":
			case "6":
			case "5":
			case "4":
			case "3":
			case "2":
			case "1":
			{
				broadcastScreenMessage(event, 4);
				break;
			}
		}
		// Activity timer.
		if (event.startsWith("KickPlayer") && (player != null) && (player.getInstanceWorld() == PVP_WORLD))
		{
			if (event.contains("Warning"))
			{
				sendScreenMessage(player, "경고! 당신은 결투를 피하고 있습니다!", 10);
				player.sendMessage("경고! 당신은 결투를 피하고 있습니다!");
				player.sendMessage("다음 경고에는 퇴장입니다!");
			}
			else
			{
				player.setTeam(Team.NONE);
				player.setOnEvent(false);
				removeListeners(player);
				player.leaveParty();
				player.setInvul(false);
				player.setImmobilized(false);
				player.enableAllSkills();
				player.teleToLocation(GIRAN_LOC);
				for (Summon summon : player.getServitors().values())
				{
					summon.setInvul(true);
					summon.setImmobilized(true);
					summon.disableAllSkills();
				}
				player.sendMessage("당신은 결투를 하지않아서 퇴장당했습니다.");
				if (PVP_WORLD != null)
				{
					// Manage forfeit.
					if ((BLUE_TEAM.isEmpty() && !RED_TEAM.isEmpty()) || (RED_TEAM.isEmpty() && !BLUE_TEAM.isEmpty()))
					{
						manageForfeit();
					}
					else
					{
						if (player.getTeam() == Team.RED)
						{
							RED_TEAM.remove(player);
						}
						else
						{
							BLUE_TEAM.remove(player);
						}
						broadcastScreenMessageWithEffect(player.getName() + "님은 결투에 참가하지 않아서 퇴장당했습니다!", 7);
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		// Event not active.
		if (!EVENT_ACTIVE)
		{
			return null;
		}
		
		String htmltext = null;
		
		// Player has already registered.
		if (PLAYER_LIST.contains(player))
		{
			// Npc is in instance.
			if (npc.getInstanceWorld() != null)
			{
				return "manager-buffheal.html";
			}
			htmltext = getHtm(player, "manager-cancel.html");
			htmltext = htmltext.replace("%list%", String.valueOf(PLAYER_LIST.size()));
			return htmltext;
		}
		// Player is not registered.
		return "manager-register.html";
	}
	
	@Override
	public String onEnterZone(Creature creature, ZoneType zone)
	{
		if (creature.isPlayable() && creature.getActingPlayer().isOnEvent())
		{
			// Kick enemy players.
			if ((zone == BLUE_PEACE_ZONE) && (creature.getTeam() == Team.RED))
			{
				creature.teleToLocation(RED_SPAWN_LOC, PVP_WORLD);
				sendScreenMessage(creature.getActingPlayer(), "적 본부 진입 금지!", 10);
			}
			if ((zone == RED_PEACE_ZONE) && (creature.getTeam() == Team.BLUE))
			{
				creature.teleToLocation(BLUE_SPAWN_LOC, PVP_WORLD);
				sendScreenMessage(creature.getActingPlayer(), "적 본부 진입 금지!", 10);
			}
			// Start inactivity check.
			if (creature.isPlayer() && //
				(((zone == BLUE_PEACE_ZONE) && (creature.getTeam() == Team.BLUE)) || //
					((zone == RED_PEACE_ZONE) && (creature.getTeam() == Team.RED))))
			{
				resetActivityTimers(creature.getActingPlayer());
			}
		}
		return null;
	}
	
	@Override
	public String onExitZone(Creature creature, ZoneType zone)
	{
		if (creature.isPlayer() && creature.getActingPlayer().isOnEvent())
		{
			final Player player = creature.getActingPlayer();
			cancelQuestTimer("KickPlayer" + creature.getObjectId(), null, player);
			cancelQuestTimer("KickPlayerWarning" + creature.getObjectId(), null, player);
			// Removed invulnerability shield.
			if (player.isAffectedBySkill(GHOST_WALKING))
			{
				player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, GHOST_WALKING.getSkill());
			}
		}
		return super.onExitZone(creature, zone);
	}
	
	private static void openHtml(Player player, String var)
	{
		String html = HtmCache.getInstance().getHtm(player, "data/scripts/custom/events/TeamVsTeam/registration-failed.html");
		html = html.replace("<?text?>", var);
		player.sendMessage(var);
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	private boolean canRegister(Player player)
	{
		switch (level)
		{
			case 1:
				if (player.getLevel() < MINIMUM_PARTICIPANT_LEVEL_LOW)
				{
					openHtml(player, MINIMUM_PARTICIPANT_LEVEL_LOW + "레벨부터 참가가능합니다.");
					return false;
				}
				if (player.getLevel() > MAXIMUM_PARTICIPANT_LEVEL_LOW)
				{
					openHtml(player, MAXIMUM_PARTICIPANT_LEVEL_LOW + "레벨까지 참가할 수 있습니다.");
					return false;
				}
				break;
			case 2:
				if (player.getLevel() < MINIMUM_PARTICIPANT_LEVEL_MIDDLE)
				{
					openHtml(player, MINIMUM_PARTICIPANT_LEVEL_MIDDLE + "레벨부터 참가가능합니다.");
					return false;
				}
				if (player.getLevel() > MAXIMUM_PARTICIPANT_LEVEL_MIDDLE)
				{
					openHtml(player, MAXIMUM_PARTICIPANT_LEVEL_MIDDLE + "레벨까지 참가할 수 있습니다.");
					return false;
				}
				break;
			case 3:
				if (player.getLevel() < MINIMUM_PARTICIPANT_LEVEL_HIGH)
				{
					openHtml(player, MINIMUM_PARTICIPANT_LEVEL_HIGH + "레벨부터 참가가능합니다.");
					return false;
				}
				if (player.getLevel() > MAXIMUM_PARTICIPANT_LEVEL_HIGH)
				{
					openHtml(player, MAXIMUM_PARTICIPANT_LEVEL_HIGH + "레벨까지 참가할 수 있습니다.");
					return false;
				}
				break;
		}
		if (PLAYER_LIST.contains(player) || player.isOnEvent() || (player.getBlockCheckerArena() > -1))
		{
			openHtml(player, "이미 이벤트에 등록되어 있습니다.");
			return false;
		}
		if (PLAYER_LIST.size() >= MAXIMUM_PARTICIPANT_COUNT)
		{
			openHtml(player, "이벤트에 등록된 플레이어가 너무 많습니다.");
			return false;
		}
		if (player.isFlyingMounted())
		{
			openHtml(player, "비행 중에는 이벤트에 등록할 수 없습니다.");
			return false;
		}
		if (player.isTransformed())
		{
			openHtml(player, "변신 상태에서는 이벤트에 등록할 수 없습니다.");
			return false;
		}
		if (!player.isInventoryUnder80(false))
		{
			openHtml(player, "인벤토리에 항목이 너무 많습니다.");
			player.sendMessage("일부 항목을 제거해 보십시오.");
			return false;
		}
		if ((player.getWeightPenalty() != 0))
		{
			openHtml(player, "인벤토리 무게가 정상 한도를 초과했습니다.");
			player.sendMessage("일부 항목을 제거해 보십시오.");
			return false;
		}
		if (player.isCursedWeaponEquipped() || (player.getReputation() < 0))
		{
			openHtml(player, "카오틱 캐릭터는 등록할 수 없습니다.");
			return false;
		}
		if (player.isInDuel())
		{
			openHtml(player, "결투 중에는 등록할 수 없습니다.");
			return false;
		}
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			openHtml(player, "올림피아드에 등록된 상태에서는 참가할 수 없습니다.");
			return false;
		}
		if (player.isInInstance())
		{
			openHtml(player, "인스턴스에 있는 동안에는 등록할 수 없습니다.");
			return false;
		}
		if (player.isInSiege() || player.isInsideZone(ZoneId.SIEGE))
		{
			openHtml(player, "공성 중에는 등록할 수 없습니다.");
			return false;
		}
		if (player.isFishing())
		{
			openHtml(player, "낚시 중에는 등록할 수 없습니다.");
			return false;
		}
		return true;
	}
	
	private void sendScreenMessage(Player player, String message, int duration)
	{
		player.sendPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, duration * 1000, 0, true, false));
	}
	
	private void broadcastScreenMessage(String message, int duration)
	{
		PVP_WORLD.broadcastPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, duration * 1000, 0, true, false));
	}
	
	private void broadcastScreenMessageWithEffect(String message, int duration)
	{
		PVP_WORLD.broadcastPacket(new ExShowScreenMessage(message, ExShowScreenMessage.TOP_CENTER, duration * 1000, 0, true, true));
	}
	
	private void broadcastScoreMessage()
	{
		PVP_WORLD.broadcastPacket(new ExShowScreenMessage("블루: " + BLUE_SCORE + " - 레드: " + RED_SCORE, ExShowScreenMessage.BOTTOM_RIGHT, 15000, 0, true, false));
	}
	
	private void addLogoutListener(Player player)
	{
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> onPlayerLogout(event), this));
	}
	
	private void addDeathListener(Player player)
	{
		player.addListener(new ConsumerEventListener(player, EventType.ON_CREATURE_DEATH, (OnCreatureDeath event) -> onPlayerDeath(event), this));
	}
	
	private void removeListeners(Player player)
	{
		for (AbstractEventListener listener : player.getListeners(EventType.ON_PLAYER_LOGOUT))
		{
			if (listener.getOwner() == this)
			{
				listener.unregisterMe();
			}
		}
		for (AbstractEventListener listener : player.getListeners(EventType.ON_CREATURE_DEATH))
		{
			if (listener.getOwner() == this)
			{
				listener.unregisterMe();
			}
		}
	}
	
	private void resetActivityTimers(Player player)
	{
		cancelQuestTimer("KickPlayer" + player.getObjectId(), null, player);
		cancelQuestTimer("KickPlayerWarning" + player.getObjectId(), null, player);
		startQuestTimer("KickPlayer" + player.getObjectId(), PVP_WORLD.getDoor(BLUE_DOOR_ID).isOpen() ? INACTIVITY_TIME * 60000 : (INACTIVITY_TIME * 60000) + (WAIT_TIME * 60000), null, player);
		startQuestTimer("KickPlayerWarning" + player.getObjectId(), PVP_WORLD.getDoor(BLUE_DOOR_ID).isOpen() ? (INACTIVITY_TIME) * 60000 : ((INACTIVITY_TIME) * 60000) + (WAIT_TIME * 60000), null, player);
	}
	
	private void manageForfeit()
	{
		cancelQuestTimer("10", null, null);
		cancelQuestTimer("9", null, null);
		cancelQuestTimer("8", null, null);
		cancelQuestTimer("7", null, null);
		cancelQuestTimer("6", null, null);
		cancelQuestTimer("5", null, null);
		cancelQuestTimer("4", null, null);
		cancelQuestTimer("3", null, null);
		cancelQuestTimer("2", null, null);
		cancelQuestTimer("1", null, null);
		cancelQuestTimer("EndFight", null, null);
		startQuestTimer("EndFight", 10000, null, null);
		broadcastScreenMessageWithEffect("적팀이 몰수되었습니다!", 7);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	private void onPlayerLogout(OnPlayerLogout event)
	{
		final Player player = event.getPlayer();
		// Remove player from lists.
		PLAYER_LIST.remove(player);
		PLAYER_SCORES.remove(player);
		BLUE_TEAM.remove(player);
		RED_TEAM.remove(player);
		// Manage forfeit.
		if ((BLUE_TEAM.isEmpty() && !RED_TEAM.isEmpty()) || //
			(RED_TEAM.isEmpty() && !BLUE_TEAM.isEmpty()))
		{
			manageForfeit();
			forfeit = true;
		}
	}
	
	@RegisterEvent(EventType.ON_CREATURE_DEATH)
	public void onPlayerDeath(OnCreatureDeath event)
	{
		if (event.getTarget().isPlayer())
		{
			final Player killedPlayer = event.getTarget().getActingPlayer();
			final Player killer = event.getAttacker().getActingPlayer();
			// Confirm Blue team kill.
			if ((killer.getTeam() == Team.BLUE) && (killedPlayer.getTeam() == Team.RED))
			{
				PLAYER_SCORES.put(killer, PLAYER_SCORES.get(killer) + 1);
				BLUE_SCORE++;
				broadcastScoreMessage();
				PVP_WORLD.broadcastPacket(new ExPVPMatchCCRecord(ExPVPMatchCCRecord.UPDATE, Util.sortByValue(PLAYER_SCORES, true)));
			}
			// Confirm Red team kill.
			if ((killer.getTeam() == Team.RED) && (killedPlayer.getTeam() == Team.BLUE))
			{
				PLAYER_SCORES.put(killer, PLAYER_SCORES.get(killer) + 1);
				RED_SCORE++;
				broadcastScoreMessage();
				PVP_WORLD.broadcastPacket(new ExPVPMatchCCRecord(ExPVPMatchCCRecord.UPDATE, Util.sortByValue(PLAYER_SCORES, true)));
			}
			broadcastScreenMessageWithEffect(killer.getTeam().name() + "팀의 [" + killer.getName() + "]님이 " + killedPlayer.getTeam().name() + "팀의 [" + killedPlayer.getName() + "]님을 처치하였습니다!", 5);
			// Auto release after 10 seconds.
			startQuestTimer("ResurrectPlayer", 10000, null, killedPlayer);
		}
	}
	
	private void startEvent(int type)
	{
		level = type;
		eventStart(null);
	}
	
	@Override
	public boolean eventStart(Player eventMaker)
	{
		if (EVENT_ACTIVE)
		{
			return false;
		}
		EVENT_ACTIVE = true;
		
		// Cancel timers. (In case event started immediately after another event was canceled.)
		for (List<QuestTimer> timers : getQuestTimers().values())
		{
			for (QuestTimer timer : timers)
			{
				timer.cancel();
			}
		}
		// Register the event at AntiFeedManager and clean it for just in case if the event is already registered
		
		if (Config.DUALBOX_CHECK_MAX_L2EVENT_PARTICIPANTS_PER_IP > 0)
		{
			AntiFeedManager.getInstance().registerEvent(AntiFeedManager.L2EVENT_ID);
			AntiFeedManager.getInstance().clear(AntiFeedManager.L2EVENT_ID);
		}
		
		String text = null;
		switch (level)
		{
			case 1:
				text = "52레벨 ~ 60레벨";
				break;
			case 2:
				text = "61레벨 ~ 75레벨";
				break;
			case 3:
				text = "76레벨 이상";
				break;
		}
		// Clear player lists.
		PLAYER_LIST.clear();
		PLAYER_SCORES.clear();
		BLUE_TEAM.clear();
		RED_TEAM.clear();
		// Spawn event manager.
		MANAGER_NPC_INSTANCE = addSpawn(MANAGER, MANAGER_SPAWN_LOC, false, REGISTRATION_TIME * 60000);
		startQuestTimer("TeleportToArena", REGISTRATION_TIME * 60000, null, null);
		// Send message to players.
		Broadcast.toAllOnlinePlayers("TvT 이벤트: 기란성 마을광장에 감독관을 통해 TvT 참가 신청이 가능합니다.");
		Broadcast.toAllOnlinePlayers("TvT 이벤트: 지금부터 " + REGISTRATION_TIME + "분동안 " + text + "의 참가신청을 받습니다.");
		// LOG.info("TvT 이벤트: TvT 이벤트가 시작되었습니다.");
		return true;
	}
	
	@Override
	public boolean eventStop()
	{
		if (!EVENT_ACTIVE)
		{
			return false;
		}
		EVENT_ACTIVE = false;
		level = 0;
		
		// Despawn event manager.
		MANAGER_NPC_INSTANCE.deleteMe();
		// Cancel timers.
		for (List<QuestTimer> timers : getQuestTimers().values())
		{
			for (QuestTimer timer : timers)
			{
				timer.cancel();
			}
		}
		// Remove participants.
		for (Player participant : PLAYER_LIST)
		{
			removeListeners(participant);
			participant.setTeam(Team.NONE);
			participant.setOnEvent(false);
		}
		if (PVP_WORLD != null)
		{
			PVP_WORLD.destroy();
			PVP_WORLD = null;
		}
		// Send message to players.
		Broadcast.toAllOnlinePlayers("TvT 이벤트: TvT 이벤트가 취소되었습니다.");
		PLAYER_LIST.clear();
		return true;
	}
	
	@Override
	public boolean eventBypass(Player player, String bypass)
	{
		return false;
	}
	
	public static void main(String[] args)
	{
		new TvT();
	}
}
