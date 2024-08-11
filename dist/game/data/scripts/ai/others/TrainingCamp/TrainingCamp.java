package ai.others.TrainingCamp;

import java.util.concurrent.TimeUnit;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogout;
import org.l2jmobius.gameserver.model.holders.TrainingHolder;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.training.ExTrainingZone_Admission;
import org.l2jmobius.gameserver.network.serverpackets.training.ExTrainingZone_Leaving;

import ai.AbstractNpcAI;

/**
 * TrainingCamp AI.
 * @author 보리넷 가츠
 */
public class TrainingCamp extends AbstractNpcAI
{
	// NPC
	private static final int RECRUITER = 4378;
	// Misc
	private static final Location TRAINING_LOCATION = new Location(-56516, 135938, -2672);
	
	private TrainingCamp()
	{
		addStartNpc(RECRUITER);
		addFirstTalkId(RECRUITER);
		addTalkId(RECRUITER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "4378.htm":
			{
				htmltext = event;
				break;
			}
			case "info":
			{
				if (player.hasPremiumStatus() || !Config.TRAINING_CAMP_PREMIUM_ONLY)
				{
					int time = Config.TRAINING_CAMP_MAX_DURATION / 3600;
					htmltext = getHtm(player, "4378-02.htm");// = "4378-02.htm";
					htmltext = htmltext.replace("%charName%", player.getName());
					htmltext = htmltext.replace("%charLevel%", String.valueOf(player.getLevel()));
					htmltext = htmltext.replace("%times%", String.valueOf(time));
				}
				else
				{
					htmltext = "4378-07.htm";
				}
				break;
			}
			case "enter":
			{
				TrainingHolder holder = player.getTraingCampInfo();
				final long trainingCampDuration = player.getTraingCampDuration();
				if (!Config.TRAINING_CAMP_ENABLE || !checkConditions(player))
				{
					return htmltext;
				}
				
				if ((holder != null) && (holder.getObjectId() != player.getObjectId()))
				{
					player.sendPacket(SystemMessageId.ONLY_ONE_CHARACTER_PER_ACCOUNT_MAY_ENTER_AT_ANY_TIME);
					String html = getHtm(player, "4378-08.htm");
					html = html.replace("%charName%", player.getName());
					htmltext = html;
				}
				
				if (trainingCampDuration >= Config.TRAINING_CAMP_MAX_DURATION)
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_COMPLETED_THE_DAY_S_TRAINING);
				}
				
				if ((holder != null) && (holder.getTrainingTime(TimeUnit.MINUTES) < 1))
				{
					holder = null;
				}
				
				if (holder == null)
				{
					if (player.hasPet())
					{
						player.setPetItems();
						player.sendMessage("펫을 소환해제 합니다.");
					}
					
					player.disableAutoShotsAll();
					player.setLastLocation();
					player.disableAllSkills();
					player.setInvul(true);
					player.setInvisible(true);
					player.teleToLocation(TRAINING_LOCATION);
					player.sendPacket(SystemMessageId.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
					player.setImmobilized(true);
					player.setTraingCampInfo(new TrainingHolder(player.getObjectId(), player.getClassIndex(), player.getLevel(), System.currentTimeMillis(), -1));
					final long timeRemaining = Config.TRAINING_CAMP_MAX_DURATION - trainingCampDuration;
					player.sendPacket(new ExTrainingZone_Admission(player.getLevel(), 0, timeRemaining));
					startQuestTimer("finish", TimeUnit.SECONDS.toMillis(timeRemaining), npc, player);
				}
				else
				{
					final long trainingTime = Math.max(0, holder.getTrainingTime(TimeUnit.MINUTES));
					final long expGained = (long) ((Config.TRAINING_CAMP_EXP_MULTIPLIER * ((trainingTime * (ExperienceData.getInstance().getExpForLevel(holder.getLevel()) * ExperienceData.getInstance().getTrainingRate(holder.getLevel()))) / TrainingHolder.getTrainingDivider())) / 60);
					final long spGained = (long) (Config.TRAINING_CAMP_SP_MULTIPLIER * (expGained / 250L));
					String html = getHtm(player, "4378-06.htm");
					html = html.replace("%charName%", player.getName());
					html = html.replace("%training_level%", String.valueOf(holder.getLevel()));
					html = html.replace("%training_time%", String.valueOf(trainingTime));
					html = html.replace("%training_exp%", String.valueOf(expGained));
					html = html.replace("%training_sp%", String.valueOf(spGained));
					htmltext = html;
				}
				break;
			}
			case "removeEnter":
			{
				final long trainingCampDuration = player.getTraingCampDuration();
				
				player.disableAutoShotsAll();
				player.setLastLocation();
				player.disableAllSkills();
				player.setInvul(true);
				player.setInvisible(true);
				player.teleToLocation(TRAINING_LOCATION);
				player.sendPacket(SystemMessageId.YOU_CANNOT_TAKE_OTHER_ACTION_WHILE_ENTERING_THE_TRAINING_CAMP);
				player.setImmobilized(true);
				// @Sdw: Here we are supposed to send ExUserInfoEquipSlot with a fake equip of a SLS, feels ugly to me, not doing it.
				player.setTraingCampInfo(new TrainingHolder(player.getObjectId(), player.getClassIndex(), player.getLevel(), System.currentTimeMillis(), -1));
				final long timeRemaining = Config.TRAINING_CAMP_MAX_DURATION - trainingCampDuration;
				player.sendPacket(new ExTrainingZone_Admission(player.getLevel(), 0, timeRemaining));
				startQuestTimer("finish", TimeUnit.SECONDS.toMillis(timeRemaining), npc, player);
				break;
			}
			case "4378-04.htm":
			{
				final TrainingHolder holder = player.getTraingCampInfo();
				if ((holder != null) && (holder.getObjectId() == player.getObjectId()))
				{
					if (holder.getClassIndex() == player.getClassIndex())
					{
						final long trainingTime = Math.max(0, holder.getTrainingTime(TimeUnit.MINUTES));
						if (trainingTime > 0)
						{
							final long expGained = (long) ((Config.TRAINING_CAMP_EXP_MULTIPLIER * ((trainingTime * (ExperienceData.getInstance().getExpForLevel(holder.getLevel()) * ExperienceData.getInstance().getTrainingRate(holder.getLevel()))) / TrainingHolder.getTrainingDivider())) / 60);
							final long spGained = (long) (Config.TRAINING_CAMP_SP_MULTIPLIER * (expGained / 250L));
							String html = getHtm(player, "4378-04.htm");
							html = html.replace("%training_level%", String.valueOf(holder.getLevel()));
							html = html.replace("%training_time%", String.valueOf(trainingTime));
							html = html.replace("%training_exp%", String.valueOf(expGained));
							html = html.replace("%training_sp%", String.valueOf(spGained));
							htmltext = html;
						}
						else
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_REWARDS_FOR_TRAINING_IF_YOU_HAVE_TRAINED_FOR_LESS_THAN_1_MINUTE);
						}
					}
					else
					{
						player.sendPacket(SystemMessageId.YOU_CAN_ONLY_BE_REWARDED_AS_THE_CLASS_IN_WHICH_YOU_ENTERED_THE_TRAINING_CAMP);
					}
				}
				else
				{
					htmltext = "4378-05.htm";
				}
				break;
			}
			case "calculate":
			{
				final TrainingHolder holder = player.getTraingCampInfo();
				if ((holder != null) && (holder.getObjectId() == player.getObjectId()))
				{
					if (holder.getClassIndex() == player.getClassIndex())
					{
						final long trainingTime = holder.getTrainingTime(TimeUnit.MINUTES);
						if (trainingTime > 0)
						{
							player.sendPacket(SystemMessageId.CALCULATING_XP_AND_SP_OBTAINED_FROM_TRAINING);
							
							final long expGained = (long) ((Config.TRAINING_CAMP_EXP_MULTIPLIER * ((trainingTime * (ExperienceData.getInstance().getExpForLevel(holder.getLevel()) * ExperienceData.getInstance().getTrainingRate(holder.getLevel()))) / TrainingHolder.getTrainingDivider())) / 60);
							final long spGained = (long) (Config.TRAINING_CAMP_SP_MULTIPLIER * (expGained / 250L));
							player.addExpAndSp(expGained, spGained);
							
							final SystemMessage sysMsg = new SystemMessage(SystemMessageId.YOU_HAVE_COMPLETED_TRAINING_IN_THE_ROYAL_TRAINING_CAMP_AND_OBTAINED_S1_XP_AND_S2_SP);
							sysMsg.addLong(expGained);
							sysMsg.addLong(spGained);
							player.sendPacket(sysMsg);
						}
						else
						{
							player.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_REWARDS_FOR_TRAINING_IF_YOU_HAVE_TRAINED_FOR_LESS_THAN_1_MINUTE);
						}
						player.setTraingCampDuration(player.getTraingCampDuration() + holder.getTrainingTime(TimeUnit.SECONDS));
						player.removeTraingCampInfo();
					}
					else
					{
						player.sendPacket(SystemMessageId.YOU_CAN_ONLY_BE_REWARDED_AS_THE_CLASS_IN_WHICH_YOU_ENTERED_THE_TRAINING_CAMP);
					}
				}
				else
				{
					String html = getHtm(player, "4378-08.htm");
					html = html.replace("%charName%", player.getName());
					htmltext = html;
				}
				break;
			}
			case "finish":
			{
				final TrainingHolder holder = player.getTraingCampInfo();
				if ((holder != null) && (holder.getObjectId() == player.getObjectId()))
				{
					holder.setEndTime(System.currentTimeMillis());
					player.setTraingCampInfo(holder);
					player.enableAllSkills();
					player.setInvul(false);
					player.setInvisible(false);
					player.setImmobilized(false);
					if (player.getLastLocation() != null)
					{
						player.teleToLocation(player.getLastLocation());
					}
					else
					{
						player.teleToLocation(82901, 149334, -3452);
					}
					player.unsetLastLocation();
					player.sendPacket(ExTrainingZone_Leaving.STATIC_PACKET);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "4378.htm";
	}
	
	private boolean checkConditions(Player player)
	{
		int playerLevel = player.getLevel();
		if ((playerLevel <= Config.TRAINING_CAMP_MIN_LEVEL) || (playerLevel >= Config.TRAINING_CAMP_MAX_LEVEL))
		{
			player.sendMessage(Config.TRAINING_CAMP_MIN_LEVEL + " ~ " + (Config.TRAINING_CAMP_MAX_LEVEL - 1) + " 레벨의 캐릭터만 이용 가능합니다.");
			return false;
		}
		else if (player.isFlyingMounted() || player.isTransformed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_THE_TRAINING_CAMP_WITH_A_MOUNT_OR_IN_A_TRANSFORMED_STATE);
			return false;
		}
		else if (player.isInParty())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_THE_TRAINING_CAMP_WHILE_IN_A_PARTY_OR_USING_THE_AUTOMATIC_REPLACEMENT_SYSTEM);
			return false;
		}
		else if (player.isCursedWeaponEquipped() || (player.getReputation() < 0) || player.isInDuel() || player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player) || player.isOnEvent() || (player.getBlockCheckerArena() > -1) || player.isInInstance() || player.isInSiege() || player.isInsideZone(ZoneId.SIEGE) || player.isFishing() || player.hasServitors())
		{
			return false;
		}
		return true;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		final TrainingHolder holder = player.getTraingCampInfo();
		if (holder == null)
		{
			return;
		}
		
		if (holder.isValid(player) && holder.isTraining())
		{
			final long elapsedTime = holder.getElapsedTime();
			final long remainingPlayerTime = Config.TRAINING_CAMP_MAX_DURATION - player.getTraingCampDuration();
			if (elapsedTime < remainingPlayerTime)
			{
				player.setLastLocation();
				player.disableAllSkills();
				player.setInvul(true);
				player.setInvisible(true);
				player.teleToLocation(TRAINING_LOCATION);
				player.setImmobilized(true);
				final long remainingDuration = remainingPlayerTime - elapsedTime;
				player.sendPacket(new ExTrainingZone_Admission(holder.getLevel(), TimeUnit.SECONDS.toMinutes(elapsedTime), remainingDuration));
				startQuestTimer("finish", TimeUnit.SECONDS.toMillis(remainingDuration), null, player);
			}
			else
			{
				holder.setEndTime(holder.getStartTime() + (remainingPlayerTime * 1000));
				player.setTraingCampInfo(holder);
				player.sendPacket(SystemMessageId.YOU_HAVE_COMPLETED_THE_DAY_S_TRAINING);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLogout(OnPlayerLogout event)
	{
		final Player player = event.getPlayer();
		final TrainingHolder holder = player.getTraingCampInfo();
		if (holder == null)
		{
			return;
		}
		
		if (holder.isValid(player) && holder.isTraining())
		{
			cancelQuestTimer("finish", null, player);
		}
	}
	
	public static void main(String[] args)
	{
		new TrainingCamp();
	}
}