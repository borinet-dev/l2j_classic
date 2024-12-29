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
package org.l2jmobius.gameserver.instancemanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.xml.DailyMissionData;
import org.l2jmobius.gameserver.data.xml.PrimeShopData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.events.EnterEventTimes;
import org.l2jmobius.gameserver.model.holders.AttendanceInfoHolder;
import org.l2jmobius.gameserver.model.holders.SubClassHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.primeshop.PrimeShopGroup;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExVitalityEffectInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExWorldChatCnt;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.attendance.ExConfirmVipAttendanceCheck;
import org.l2jmobius.gameserver.network.serverpackets.attendance.ExVipAttendanceItemList;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author UnAfraid
 */
public class DailyTaskManager
{
	private static final Logger LOGGER = Logger.getLogger(DailyTaskManager.class.getName());
	private static final String QUERY = "SELECT charId, createDate FROM characters WHERE createDate LIKE ?";
	private int _count = 0;
	private final Map<Integer, ClanMember> _members = new ConcurrentHashMap<>();
	
	private static final Set<Integer> RESET_SKILLS = new HashSet<>();
	static
	{
		RESET_SKILLS.add(2510); // Wondrous Cubic
		RESET_SKILLS.add(22180); // Wondrous Cubic - 1 time use
	}
	
	protected DailyTaskManager()
	{
		olymStart();
		dailyReset();
		dailyAfternoonReset();
		Save();
		customReset();
		ThreadPool.scheduleAtFixedRate(this::AutoLunaReset, 600000, 60000); // 1 min
	}
	
	private void olymStart()
	{
		final long currentTime = System.currentTimeMillis();
		// Schedule reset monday at 12:00.
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onOlyStart, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
	}
	
	private void onOlyStart()
	{
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
		{
			Olympiad.getInstance().setNewOlympiad();
		}
	}
	
	private void dailyReset()
	{
		final long currentTime = System.currentTimeMillis();
		// Schedule reset everyday at 6:30.
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onDailyReset, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
	}
	
	private void dailyAfternoonReset()
	{
		final long currentTime = System.currentTimeMillis();
		// Schedule reset everyday at 13:00.
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 13);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onRecReset, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
	}
	
	private void Save()
	{
		// Schedule reset everyhour at 30 mins.
		final Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.MINUTE) >= 30)
		{
			calendar.add(Calendar.HOUR_OF_DAY, 1);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		}
		else
		{
			calendar.set(Calendar.MINUTE, 30);
			calendar.set(Calendar.SECOND, 0);
		}
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - System.currentTimeMillis());
		ThreadPool.scheduleAtFixedRate(this::onSave, startDelay, 1800000); // 1800000 = 30 minutes
	}
	
	private void customReset()
	{
		// 밤 12시에 리셋
		final long currentTime = System.currentTimeMillis();
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 5);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onMiniGameReset, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
		ThreadPool.scheduleAtFixedRate(this::onLogReset, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
		ThreadPool.scheduleAtFixedRate(this::checkEvent, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
	}
	
	public void onDailyReset()
	{
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY)
		{
			resetClanLeaderApply();
			// resetVitalityWeekly();
		}
		else
		{
			// resetVitalityDaily();
		}
		
		resetClanBonus();
		resetDailyMissionRewards();
		resetDailySkills();
		if (Config.PRIMESHOP_ENABLED)
		{
			resetDailyPrimeShopData();
		}
		resetWorldChatPoints();
		resetAttendance();
		resetTrainingCamp();
		CleanFor30Days();
		BirthDay();
		resetSchedule();
		resetFamilyEvent();
		resetHeavenlyRift();
		resetCharVar();
		resetVarEvent();
		cleanUpExpiredData();
	}
	
	private void resetSchedule()
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO event_schedulers set id = '1', eventName = 'Morning', schedulerName = 'reset', lastRun = ?"))
		{
			statement.setTimestamp(1, new Timestamp(calendar.getTimeInMillis()));
			statement.execute();
		}
		catch (Exception e)
		{
		}
	}
	
	public void onRecReset()
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 13);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement reset = con.prepareStatement("REPLACE INTO event_schedulers set id = '2', eventName = 'Afternoon', schedulerName = 'RecReset', lastRun = ?"))
			{
				reset.setTimestamp(1, new Timestamp(calendar.getTimeInMillis()));
				reset.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, rec_have=0 WHERE rec_have <=  20"))
			{
				ps.setInt(1, 20); // Rec left = 20
				ps.executeUpdate();
			}
			
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, rec_have=GREATEST(rec_have-20,0) WHERE rec_have > 20"))
			{
				ps.setInt(1, 20); // Rec left = 20
				ps.executeUpdate();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "추천 시스템을 초기화 할 수 없습니다.", e);
		}
		
		for (Player player : World.getInstance().getPlayers())
		{
			player.setRecomLeft(20);
			player.setRecomHave(player.getRecomHave() - 20);
			player.sendPacket(new ExVoteSystemInfo(player));
			player.broadcastUserInfo();
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATION_S);
			sm.addInt(20);
			player.sendPacket(sm);
			player.updateUserInfo();
		}
		LOGGER.info("추천 시스템이 초기화 되었습니다.");
	}
	
	private void onSave()
	{
		GlobalVariablesManager.getInstance().storeMe();
		
		if (Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
		}
	}
	
	private void resetClanLeaderApply()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getNewLeaderId() != 0)
			{
				final ClanMember member = clan.getClanMember(clan.getNewLeaderId());
				if (member == null)
				{
					continue;
				}
				
				clan.setNewLeader(member);
			}
		}
		LOGGER.info("혈맹 군주가 업데이트되었습니다.");
	}
	
	@SuppressWarnings("unused")
	private void resetVitalityDaily()
	{
		if (!Config.ENABLE_VITALITY)
		{
			return;
		}
		
		for (Player player : World.getInstance().getPlayers())
		{
			int vp = player.getVitalityPoints() + PlayerStat.RESET_VITALITY_POINTS;
			if (vp >= PlayerStat.MAX_VITALITY_POINTS)
			{
				player.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS, false);
			}
			else
			{
				player.setVitalityPoints(vp, false);
			}
			for (SubClassHolder subclass : player.getSubClasses().values())
			{
				if (vp >= PlayerStat.MAX_VITALITY_POINTS)
				{
					subclass.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS);
				}
				else
				{
					subclass.setVitalityPoints(vp);
				}
			}
			player.sendPacket(new ExVitalityEffectInfo(player));
		}
		
		int charid = 0;
		int classid = 0;
		int vp = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId, vitality_points FROM characters WHERE vitality_points < " + PlayerStat.MAX_VITALITY_POINTS))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					charid = rs.getInt("charId");
					vp = rs.getInt("vitality_points");
					
					try (PreparedStatement mt = con.prepareStatement("UPDATE characters SET vitality_points = ? WHERE charId = ?"))
					{
						if ((vp + PlayerStat.RESET_VITALITY_POINTS) >= PlayerStat.MAX_VITALITY_POINTS)
						{
							mt.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
							mt.setInt(2, charid);
							mt.executeUpdate();
						}
						else
						{
							mt.setInt(1, vp + PlayerStat.RESET_VITALITY_POINTS);
							mt.setInt(2, charid);
							mt.executeUpdate();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "사이하의 은총을 업데이트하는 동안 오류가 발생했습니다.", e);
		}
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId, class_id, vitality_points FROM character_subclasses WHERE vitality_points < " + PlayerStat.MAX_VITALITY_POINTS))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					charid = rs.getInt("charId");
					classid = rs.getInt("class_id");
					vp = rs.getInt("vitality_points");
					
					try (PreparedStatement mt = con.prepareStatement("UPDATE character_subclasses SET vitality_points = ? WHERE charId = ? AND class_id = ?"))
					{
						if ((vp + PlayerStat.RESET_VITALITY_POINTS) >= PlayerStat.MAX_VITALITY_POINTS)
						{
							mt.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
							mt.setInt(2, charid);
							mt.setInt(3, classid);
							mt.executeUpdate();
						}
						else
						{
							mt.setInt(1, vp + PlayerStat.RESET_VITALITY_POINTS);
							mt.setInt(2, charid);
							mt.setInt(3, classid);
							mt.executeUpdate();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "사이하의 은총을 업데이트하는 동안 오류가 발생했습니다.", e);
		}
		LOGGER.info("사이하의 은총 25% 지급완료");
	}
	
	@SuppressWarnings("unused")
	private void resetVitalityWeekly()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			player.setVitalityItemsUsed(0);
			player.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS, false);
			player.sendPacket(new ExVitalityEffectInfo(player));
			player.sendMessage("사이하의 은총이 모두 충전되었으며 아이템 사용 또한 초기화 되었습니다!");
			
			for (SubClassHolder subclass : player.getSubClasses().values())
			{
				subclass.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS);
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET vitality_points = " + PlayerStat.MAX_VITALITY_POINTS + " WHERE vitality_points < " + PlayerStat.MAX_VITALITY_POINTS))
			{
				ps.executeUpdate();
			}
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_subclasses SET vitality_points = " + PlayerStat.MAX_VITALITY_POINTS + " WHERE vitality_points < " + PlayerStat.MAX_VITALITY_POINTS))
			{
				ps.executeUpdate();
			}
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_variables SET val = 0 WHERE var = 'VITALITY_ITEMS_USED' AND val != 0"))
			{
				ps.executeUpdate();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "사이하의 은총을 업데이트하는 동안 오류가 발생했습니다.", e);
		}
		LOGGER.info("샤이하의 은총 아이템 사용 초기화");
	}
	
	private void resetFamilyEvent()
	{
		if (BorinetTask.SpecialEvent() && (Config.CUSTOM_EVENT_NAME == 3))
		{
			for (Player player : World.getInstance().getPlayers())
			{
				player.sendMessage("가정의 달 기념 선물 상자 - 이벤트 구매가 가능합니다.");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "가정의 달 기념 선물 상자 - 이벤트 구매가 가능합니다."));
			}
		}
		for (Player player : World.getInstance().getPlayers())
		{
			player.getAccountVariables().remove("묵찌빠");
		}
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = '묵찌빠'"))
			{
				ps.executeUpdate();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "스킬 재사용을 초기화할 수 없습니다.", e);
		}
	}
	
	private void resetClanBonus()
	{
		clanReset();
		ClanTable.getInstance().getClans().forEach(Clan::resetClanBonus);
		LOGGER.info("혈맹 보너스가 초기화 되었습니다.");
	}
	
	private void clanReset()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().set("CLAIMED_CLAN_REWARDS", 0);
			player.getVariables().storeMe();
		}
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE character_variables SET val = 0 WHERE var = 'CLAIMED_CLAN_REWARDS'"))
		{
			statement.execute();
		}
		catch (Exception e)
		{
		}
	}
	
	public void broadcastToOnlineMembers(IClientOutgoingPacket packet)
	{
		for (ClanMember member : _members.values())
		{
			if ((member != null) && member.isOnline())
			{
				member.getPlayer().sendPacket(packet);
			}
		}
	}
	
	private void resetDailySkills()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove("축하메세지");
		}
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (int skillId : RESET_SKILLS)
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE skill_id=?;"))
				{
					ps.setInt(1, skillId);
					ps.execute();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "스킬 재사용을 초기화할 수 없습니다.", e);
		}
		
		// final Set<Player> updates = new HashSet<>();
		for (int skillId : RESET_SKILLS)
		{
			final Skill skill = SkillData.getInstance().getSkill(skillId, 1 /* No known need for more levels */);
			if (skill != null)
			{
				for (Player player : World.getInstance().getPlayers())
				{
					if (player.hasSkillReuse(skill.getReuseHashCode()))
					{
						player.removeTimeStamp(skill);
						// updates.add(player);
					}
				}
			}
		}
		// for (Player player : updates)
		// {
		// player.sendSkillList();
		// }
		
		LOGGER.info("스킬 재사용이 초기화 되었습니다.");
	}
	
	private void resetWorldChatPoints()
	{
		if (!Config.ENABLE_WORLD_CHAT)
		{
			return;
		}
		
		// Update data for offline players.
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE character_variables SET val = ? WHERE var = ?"))
		{
			ps.setInt(1, 0);
			ps.setString(2, PlayerVariables.WORLD_CHAT_VARIABLE_NAME);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "월드 채팅 포인트를 초기화 할 수 없습니다.", e);
		}
		
		// Update data for online players.
		for (Player player : World.getInstance().getPlayers())
		{
			player.setWorldChatUsed(0);
			player.sendPacket(new ExWorldChatCnt(player));
			player.getVariables().storeMe();
		}
		
		LOGGER.info("월드 채팅 포인트가 초기화 되었습니다.");
	}
	
	private void resetAttendance()
	{
		int date = Calendar.getInstance().get(Calendar.DATE);
		if (date == 1)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				if (Config.ATTENDANCE_REWARDS_SHARE_ACCOUNT)
				{
					player.getAccountVariables().remove("ATTENDANCE_DATE");
					player.getAccountVariables().remove("ATTENDANCE_INDEX");
				}
				else
				{
					player.getVariables().remove("ATTENDANCE_DATE");
					player.getVariables().remove("ATTENDANCE_INDEX");
				}
				player.sendPacket(new ExConfirmVipAttendanceCheck(true, 0));
			}
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = 'ATTENDANCE_INDEX'"))
				{
					ps.executeUpdate();
				}
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = 'ATTENDANCE_DATE'"))
				{
					ps.executeUpdate();
				}
			}
			catch (Exception e)
			{
			}
			LOGGER.info("출석 1일 초기화 완료");
		}
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE enter_event SET 1hour='0', 2hour='0', 3hour='0', 5hour='0', inTimes='0'"))
		{
			statement.executeUpdate();
		}
		catch (Exception e)
		{
		}
		
		for (Player player : World.getInstance().getPlayers())
		{
			EnterEventTimes.stopTask();
			EnterEventTimes.EnterEventTimeStart(player);
			player.onPlayerEnter();
			player.setUptime(System.currentTimeMillis());
			
			if (Config.ATTENDANCE_REWARD_DELAY != 0)
			{
				ThreadPool.schedule(() ->
				{
					// Check if player can receive reward today.
					final AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
					if (attendanceInfo.isRewardAvailable())
					{
						final int lastRewardIndex = attendanceInfo.getRewardIndex() + 1;
						final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_DAY_S1_ATTENDANCE_REWARD_IS_READY_CLICK_ON_THE_REWARDS_ICON_YOU_CAN_REDEEM_YOUR_REWARD_30_MINUTES_AFTER_LOGGING_IN);
						msg.addInt(lastRewardIndex);
						player.sendPacket(msg);
						Broadcast.toPlayerScreenMessage(player, lastRewardIndex + "일차 출석 보상을 받을 수 있습니다. 보상 아이콘을 클릭해 주세요.");
						if (Config.ATTENDANCE_POPUP_WINDOW)
						{
							player.sendPacket(new ExVipAttendanceItemList(player));
						}
					}
				}, Config.ATTENDANCE_REWARD_DELAY * 60 * 1000);
			}
			else if (Config.ATTENDANCE_REWARD_DELAY == 0)
			{
				// Check if player can receive reward today.
				final AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
				if (attendanceInfo.isRewardAvailable())
				{
					final int lastRewardIndex = attendanceInfo.getRewardIndex() + 1;
					final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_DAY_S1_ATTENDANCE_REWARD_IS_READY_CLICK_ON_THE_REWARDS_ICON_YOU_CAN_REDEEM_YOUR_REWARD_30_MINUTES_AFTER_LOGGING_IN);
					msg.addInt(lastRewardIndex);
					player.sendPacket(msg);
					
					Broadcast.toPlayerScreenMessage(player, lastRewardIndex + "일차 출석 보상을 받을 수 있습니다. 보상 아이콘을 클릭해 주세요.");
					if (Config.ATTENDANCE_POPUP_WINDOW)
					{
						player.sendPacket(new ExVipAttendanceItemList(player));
					}
				}
			}
		}
		Broadcast.toAllOnlinePlayers("출석 및 접속보상이 초기화 되었습니다!", true);
		Broadcast.toAllOnlinePlayersOnScreen("출석 및 접속보상이 초기화 되었습니다!");
		LOGGER.info("출석 및 접속보상이 초기화 되었습니다.");
	}
	
	private void resetTrainingCamp()
	{
		if (Config.TRAINING_CAMP_ENABLE)
		{
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ?"))
			{
				ps.setString(1, "TRAINING_CAMP_DURATION");
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "왕립 훈련소를 초기화 할 수 없습니다.", e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.resetTraingCampDuration();
				player.getAccountVariables().storeMe();
			}
			
			LOGGER.info("왕립 훈련소가 초기화 되었습니다.");
		}
	}
	
	private void resetDailyMissionRewards()
	{
		Calendar calendar = Calendar.getInstance();
		int date = Calendar.getInstance().get(Calendar.DATE);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		if ((date == 1) && (day == Calendar.MONDAY))
		{
			DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::resetMonthWeekly);
		}
		else if (day == Calendar.MONDAY)
		{
			for (Player players : World.getInstance().getPlayers())
			{
				players.getVariables().remove("강화전도사");
			}
			missionVariablesRemove();
			DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::resetWeekly);
		}
		else if (date == 1)
		{
			DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::resetMonth);
		}
		else
		{
			DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::reset);
		}
	}
	
	private void resetHeavenlyRift()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_heavenly_rift"))
		{
			ps.execute();
		}
		catch (Exception e)
		{
		}
	}
	
	private static void resetVarEvent()
	{
		List<String> variablesToRemove = List.of("CUSTOM_EVENT_BOX", "CHUSEOK_ITEM", "MAPLE_ITEM", "문자수집가의선물", "BLOCK_CHECKER");
		for (Player player : World.getInstance().getPlayers())
		{
			variablesToRemove.forEach(player.getAccountVariables()::remove);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("DELETE FROM event_hwid WHERE name = 'CUSTOM_EVENT_BOX';");
			statement.executeUpdate("DELETE FROM account_gsdata WHERE var = 'CUSTOM_EVENT_BOX';");
			statement.executeUpdate("DELETE FROM account_gsdata WHERE var = 'MAPLE_ITEM';");
			statement.executeUpdate("DELETE FROM account_gsdata WHERE var = '문자수집가의선물';");
			statement.executeUpdate("DELETE FROM account_gsdata WHERE var = 'BLOCK_CHECKER';");
		}
		catch (Exception e)
		{
			LOGGER.warning("커스텀이벤트 데이터베이스 정리 오류" + e);
		}
	}
	
	public static void cleanUpExpiredData()
	{
		long currentTimeMillis = System.currentTimeMillis();
		
		String deleteReuseDelayQuery = "DELETE FROM character_item_reuse_save WHERE reuseDelay < ?";
		String deleteVariablesQuery = "DELETE FROM character_variables WHERE var LIKE ? AND val < ?";
		String selectPlayerVariablesQuery = "SELECT charId, var FROM character_variables WHERE var LIKE ? AND val < ?";
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			// character_item_reuse_save 테이블 데이터 삭제
			try (PreparedStatement ps = con.prepareStatement(deleteReuseDelayQuery))
			{
				ps.setLong(1, currentTimeMillis);
				ps.executeUpdate();
				// int rowsDeleted = ps.executeUpdate();
				// LOGGER.info("character_item_reuse_save 테이블에서 " + rowsDeleted + "개의 데이터가 삭제되었습니다.");
			}
			
			// character_variables 테이블에서 만료된 데이터 삭제
			try (PreparedStatement ps = con.prepareStatement(deleteVariablesQuery))
			{
				ps.setString(1, "%재사용시간%");
				ps.setLong(2, currentTimeMillis);
				ps.executeUpdate();
				// int rowsDeleted = ps.executeUpdate();
				// LOGGER.info("character_variables 테이블에서 " + rowsDeleted + "개의 데이터가 삭제되었습니다.");
			}
			
			// 플레이어별 변수 삭제 처리
			try (PreparedStatement ps = con.prepareStatement(selectPlayerVariablesQuery))
			{
				ps.setString(1, "%재사용시간%");
				ps.setLong(2, currentTimeMillis);
				
				try (ResultSet rs = ps.executeQuery())
				{
					// ResultSet을 순회하며 즉시 변수 삭제 처리
					while (rs.next())
					{
						int charId = rs.getInt("charId");
						String variable = rs.getString("var");
						
						// 해당 charId를 가진 플레이어 가져오기
						Player player = World.getInstance().getPlayer(charId);
						if (player != null)
						{
							player.getVariables().remove(variable);
						}
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "cleanUpExpiredData: 데이터베이스 작업 중 오류 발생", e);
		}
	}
	
	private static void resetCharVar()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("DELETE FROM character_variables WHERE var LIKE '%Lollipop_%';");
			statement.executeUpdate("DELETE FROM account_gsdata WHERE var LIKE 'SPRING_BUFF';");
			if ((BorinetTask.Month() != Calendar.FEBRUARY))
			{
				statement.executeUpdate("DELETE FROM account_gsdata WHERE var = '발렌타인데이선물';");
				statement.executeUpdate("DELETE FROM account_gsdata WHERE var = 'CUPON_L2DRAGON';");
			}
			if ((BorinetTask.Month() != Calendar.MARCH))
			{
				statement.executeUpdate("DELETE FROM account_gsdata WHERE var = '삼일절선물';");
				statement.executeUpdate("DELETE FROM account_gsdata WHERE var = '화이트데이선물';");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("이벤트 데이터베이스 정리 오류" + e);
		}
		
		List<String> variablesToRemove = List.of("Lollipop_41258", "Lollipop_41259");
		List<String> AccountvariablesToRemove = List.of("SPRING_BUFF");
		List<String> AccountvariablesToFebruary = List.of("발렌타인데이선물", "CUPON_L2DRAGON");
		List<String> AccountvariablesToMarch = List.of("삼일절선물", "화이트데이선물");
		for (Player player : World.getInstance().getPlayers())
		{
			variablesToRemove.forEach(player.getVariables()::remove);
			AccountvariablesToRemove.forEach(player.getAccountVariables()::remove);
			if ((BorinetTask.Month() != Calendar.FEBRUARY))
			{
				AccountvariablesToFebruary.forEach(player.getAccountVariables()::remove);
			}
			if ((BorinetTask.Month() != Calendar.MARCH))
			{
				AccountvariablesToMarch.forEach(player.getAccountVariables()::remove);
			}
		}
	}
	
	private void CleanFor30Days()
	{
		int deleteDays = Config.AUTO_DELETE_CHAR_DAYS * 3600 * 24;
		if (Config.AUTO_DELETE_CHAR)
		{
			try (Connection con = DatabaseFactory.getConnection();
				Statement statement = con.createStatement())
			{
				int charCount = 0;
				int accountCount = 0;
				
				charCount += statement.executeUpdate("DELETE FROM characters WHERE online = 0 AND characters.account_name IN (select login from accounts WHERE donate < 100) AND ((UNIX_TIMESTAMP() - ( lastAccess/1000)) >= " + deleteDays + ");");
				accountCount += statement.executeUpdate("DELETE FROM accounts WHERE donate < 100 AND ((UNIX_TIMESTAMP() - (lastactive/1000)) >= " + deleteDays + ");");
				
				LOGGER.info(Config.AUTO_DELETE_CHAR_DAYS + "일간 미접속 캐릭터 " + charCount + "개와 계정 " + accountCount + "개를 데이터 베이스에서 정리하였습니다.");
			}
			catch (SQLException e)
			{
				LOGGER.severe("데이터베이스에서 개체 ID를 읽을 수 없습니다. 구성을 확인하십시오.");
			}
		}
		DatabaseClean();
		for (Player players : World.getInstance().getPlayers())
		{
			players.getVariables().set("DailyLuna", 0);
		}
	}
	
	private void BirthDay()
	{
		final Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(QUERY))
		{
			statement.setString(1, "%-%-" + getNum(day));
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final int playerId = rset.getInt("charId");
					final Calendar createDate = Calendar.getInstance();
					createDate.setTime(rset.getDate("createDate"));
					
					if (createDate.get(Calendar.MONTH) == month)
					{
						continue;
					}
					
					int msgId = IdManager.getInstance().getNextId();
					String topic = Config.ALT_BIRTHDAY_MAIL_SUBJECT;
					String body = Config.ALT_BIRTHDAY_MAIL_TEXT;
					body = body.replace("$c1", CharInfoTable.getInstance().getNameById(playerId));
					Message msg = new Message(msgId, playerId, topic, body, 7, MailType.PRIME_SHOP_GIFT, false);
					Mail attachments = msg.createAttachments();
					attachments.addItem("생일축하선물", Config.ALT_BIRTHDAY_GIFT, 1, null, null);
					MailManager.getInstance().sendMessage(msg);
					final Player birthPlayer = World.getInstance().getPlayer(playerId);
					if (birthPlayer != null)
					{
						birthPlayer.sendPacket(SystemMessageId.HAPPY_BIRTHDAY_ALEGRIA_HAS_SENT_YOU_A_BIRTHDAY_GIFT);
						birthPlayer.getVariables().set("축하메세지", 1);
					}
					
					_count++;
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.severe("생일선물 지급에 오류가 발생했습니다.");
		}
		LOGGER.info(_count + "명에게 생일 선물지급이 완료 되었습니다!");
	}
	
	private String getNum(int num)
	{
		return (num <= 9) ? "0" + num : String.valueOf(num);
	}
	
	private void DatabaseClean()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			final long cleanupStart = System.currentTimeMillis();
			int cleanCount = 0;
			
			cleanCount += statement.executeUpdate("DELETE FROM account_premium WHERE account_premium.account_name NOT IN (SELECT account_name FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM accounts WHERE accounts.login NOT IN (SELECT account_name FROM characters);");
			
			// Characters
			cleanCount += statement.executeUpdate("DELETE FROM account_gsdata WHERE account_gsdata.account_name NOT IN (SELECT account_name FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_contacts WHERE character_contacts.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_contacts WHERE character_contacts.contactId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_friends WHERE character_friends.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_friends WHERE character_friends.friendId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_quests WHERE character_quests.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_recipeshoplist WHERE character_recipeshoplist.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_skills WHERE character_skills.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_instance_time WHERE character_instance_time.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_spec WHERE character_spec.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_stats WHERE character_stats.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_daily_rewards WHERE character_daily_rewards.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_use_item;");
			cleanCount += statement.executeUpdate("DELETE FROM character_variables WHERE var = '축하메세지';");
			cleanCount += statement.executeUpdate("DELETE FROM character_hwid WHERE character_hwid.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_summon_skills_save WHERE character_summon_skills_save.ownerId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_summons WHERE character_summons.ownerId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_item_reuse_save WHERE character_item_reuse_save.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_spirits WHERE character_spirits.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM custom_mail WHERE custom_mail.receiver NOT IN (SELECT charId FROM characters);");
			
			// Items
			cleanCount += statement.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT charId FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data) AND items.owner_id NOT IN (SELECT item_obj_id FROM pets) AND items.owner_id != -1;");
			cleanCount += statement.executeUpdate("DELETE FROM items WHERE items.owner_id = -1 AND loc LIKE 'MAIL' AND loc_data NOT IN (SELECT messageId FROM messages WHERE senderId = -1);");
			cleanCount += statement.executeUpdate("DELETE FROM item_auction_bid WHERE item_auction_bid.playerObjId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM item_variations WHERE item_variations.itemId NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM item_elementals WHERE item_elementals.itemId NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM item_special_abilities WHERE item_special_abilities.objectId NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM item_variables WHERE item_variables.id NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM character_minigame_score WHERE character_minigame_score.object_id NOT IN (SELECT charId FROM characters);");
			
			// Misc
			cleanCount += statement.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM heroes WHERE heroes.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM olympiad_nobles_eom WHERE olympiad_nobles_eom.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM merchant_lease WHERE merchant_lease.player_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_reco_bonus WHERE character_reco_bonus.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_data WHERE clan_data.clan_id NOT IN (SELECT clanid FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charOneId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charTwoId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM heroes_diary WHERE heroes_diary.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_offline_trade WHERE character_offline_trade.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_offline_trade_items WHERE character_offline_trade_items.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_tpbookmark WHERE character_tpbookmark.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_variables WHERE character_variables.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM messages WHERE messages.receiverId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM enter_event WHERE enter_event.account NOT IN (SELECT account_name FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM commission_items WHERE commission_items.item_object_id NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM event_hwid WHERE event_hwid.HWID NOT IN (SELECT hwid FROM character_hwid);");
			// cleanCount += statement.executeUpdate("DELETE FROM daily_luna;");
			// cleanCount += statement.executeUpdate("DELETE FROM character_variables WHERE val = 'DailyLuna';");
			// cleanCount += statement.executeUpdate("DELETE FROM pledge_applicant WHERE pledge_applicant.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_pet_skills_save WHERE character_pet_skills_save.petObjItemId NOT IN (SELECT object_id FROM items);");
			
			// Clan
			cleanCount += statement.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_notices WHERE clan_notices.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_variables WHERE clan_variables.clanId NOT IN (SELECT clan_id FROM clan_data);");
			// cleanCount += statement.executeUpdate("DELETE FROM crests WHERE crests.crest_id NOT IN (SELECT crest_id FROM clan_data);");
			// cleanCount += statement.executeUpdate("DELETE FROM pledge_applicant WHERE pledge_applicant.clanId NOT IN (SELECT clan_id FROM clan_data);");
			// cleanCount += statement.executeUpdate("DELETE FROM pledge_waiting_list WHERE pledge_waiting_list.char_id NOT IN (SELECT charId FROM characters);");
			
			// Forums
			cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
			cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT charId FROM characters) AND forums.forum_parent=3;");
			cleanCount += statement.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
			cleanCount += statement.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");
			cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_name NOT IN (SELECT clan_name FROM clan_data) AND forums.forum_parent=2;");
			
			// Update needed items after cleaning has taken place.
			statement.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
			statement.executeUpdate("UPDATE clan_data SET new_leader_id = 0 WHERE new_leader_id <> 0 AND new_leader_id NOT IN (SELECT charId FROM characters);");
			statement.executeUpdate("UPDATE clan_subpledges SET leader_id=0 WHERE clan_subpledges.leader_id NOT IN (SELECT charId FROM characters) AND leader_id > 0;");
			statement.executeUpdate("UPDATE castle SET side='NEUTRAL' WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data);");
			statement.executeUpdate("UPDATE characters SET clanid=0, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0, clan_join_expiry_time=0, clan_create_expiry_time=0 WHERE characters.clanid > 0 AND characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
			statement.executeUpdate("UPDATE fort SET owner=0 WHERE owner NOT IN (SELECT clan_id FROM clan_data);");
			
			// 운영일수 계산
			statement.executeUpdate("UPDATE web_connect SET date=" + BorinetUtil.serviceDays(true));
			statement.executeUpdate("UPDATE web_connect SET newdate=" + BorinetUtil.serviceDays(false));
			
			if (cleanCount > 0)
			{
				LOGGER.info("데이터베이스 정리: " + cleanCount + "개의 데이터 베이스를 정리하였습니다. 소요시간: " + ((System.currentTimeMillis() - cleanupStart) / 1000) + "초");
			}
			else
			{
				LOGGER.info("데이터베이스 정리: 정리할 데이터 베이스가 없습니다!");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("데이터베이스 정리: 데이터 베이스를 정리할 수 없습니다.: " + e);
		}
		
		// Cleanup timestamps.
		try (Connection con = DatabaseFactory.getConnection())
		{
			int cleanCount = 0;
			for (String line : TIMESTAMPS_CLEAN)
			{
				try (PreparedStatement statement = con.prepareStatement(line))
				{
					statement.setLong(1, System.currentTimeMillis());
					cleanCount += statement.executeUpdate();
				}
			}
			if (cleanCount > 0)
			{
				LOGGER.info("데이터베이스 정리: " + cleanCount + "개의 만료된 타임스템프를 정리하였습니다.");
			}
			else
			{
				LOGGER.info("데이터베이스 정리: 만료된 타임스템프가 없습니다.");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("데이터베이스 정리: 데이터베이스의 타임스탬프를 정리할 수 없습니다. " + e);
		}
	}
	
	private void onMiniGameReset()
	{
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (
					PreparedStatement rs = con.prepareStatement("SELECT characters.char_name AS name, character_minigame_score.object_id AS object_id, character_minigame_score.score AS score FROM characters, character_minigame_score WHERE characters.charId=character_minigame_score.object_id ORDER BY score DESC LIMIT 1"))
				{
					ResultSet check = rs.executeQuery();
					while (check.next())
					{
						String name = check.getString("char_name");
						int charid = check.getInt("object_id");
						
						if (name != null)
						{
							PreparedStatement rsb = con.prepareStatement("INSERT INTO items_reward_mail (char_name, charId, delivered) VALUES ('" + name + "', '" + charid + "', 0)");
							rsb.execute();
							for (Player player : World.getInstance().getPlayers())
							{
								Broadcast.toPlayerScreenMessageS(player, "새로운 미니게임 경기가 시작되었으며, 지난 경기 1위에게 상품이 지급되었습니다!");
								player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "새로운 미니게임 경기가 시작되었으며, 지난 경기 1위에게 상품이 지급되었습니다!"));
							}
						}
						else
						{
							for (Player player : World.getInstance().getPlayers())
							{
								Broadcast.toPlayerScreenMessageS(player, "새로운 미니게임 경기가 시작되었으며, 지난 경기의 1위는 없습니다!");
								player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "새로운 미니게임 경기가 시작되었으며, 지난 경기의 1위는 없습니다!"));
							}
						}
					}
				}
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_minigame_score"))
				{
					ps.execute();
				}
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "미니게임 리셋중 오류가 발생했습니다.", e);
			}
			LOGGER.info("미니게임: 스코어가 리셋되었으며, 새로운 미니게임 경기가 시작되었습니다.");
		}
	}
	
	private void resetDailyPrimeShopData()
	{
		for (PrimeShopGroup holder : PrimeShopData.getInstance().getPrimeItems().values())
		{
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var=?"))
			{
				ps.setString(1, AccountVariables.PRIME_SHOP_PRODUCT_DAILY_COUNT + holder.getBrId());
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Could not reset PrimeShopData: " + e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.getVariables().remove(AccountVariables.PRIME_SHOP_PRODUCT_DAILY_COUNT + holder.getBrId());
				player.getAccountVariables().storeMe();
			}
		}
		LOGGER.info("PrimeShopData has been resetted.");
	}
	
	private void checkEvent()
	{
		int month = BorinetTask.Month();
		int day = BorinetTask.Days();
		
		String eventName = null;
		String topic = null;
		String body = null;
		String items = null;
		
		if (((month == Calendar.FEBRUARY) && (day == 14)))
		{
			eventName = "발렌타인데이선물";
			topic = "오늘은 발렌타인 데이!";
			body = "오늘도 어김없이 " + Config.SERVER_NAME_KOR + "에 오신것을 환영하며, 발렌타인 데이 기념선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
			items = "20214,5;23627,10;22235,10;37705,3;37706,3;20191,1";
		}
		else if (month == Calendar.MARCH)
		{
			if (day == 1)
			{
				eventName = "삼일절선물";
				topic = "오늘은 삼일절 입니다.";
				body = "이런날도 어김없이 " + Config.SERVER_NAME_KOR + "에 오신것을 환영하며, 삼일절을 맞이해서 기념선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
				items = "47825,1";
			}
			else if (day == 14)
			{
				eventName = "화이트데이선물";
				topic = "오늘은 화이트 데이!";
				body = "오늘도 어김없이 " + Config.SERVER_NAME_KOR + "에 오신것을 환영하며, 화이트 데이 기념선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
				items = "14766,3;37705,3;37706,3;14767,10;14768,1;9140,1";
			}
		}
		
		if (eventName != null)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				int checkGift = player.getAccountVariables().getInt(eventName, 0);
				if ((player.getLevel() >= 20) && (checkGift != 1))
				{
					BorinetUtil.getInstance().sendEventMail(player, topic, body, items, eventName, false);
				}
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, eventName + " 이벤트가 시작되었습니다!"));
			}
		}
	}
	
	private void onLogReset()
	{
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		final String today = formatter.format(new Date());
		
		File Chatfile = new File("log/chat.log");
		File newChatfile = new File("log/chat/chat_" + today + ".log");
		// File Itemfile = new File("log/item.log");
		// File newItemfile = new File("log/item/item_" + today + ".log");
		
		File chat = new File("log/chat");
		// File item = new File("log/item");
		
		if (!chat.exists())
		{
			new File("log/chat").mkdirs();
			LOGGER.info("log/chat 폴더를 생성했습니다.");
		}
		// if (!item.exists())
		// {
		// new File("log/item").mkdirs();
		// LOGGER.info("log/item 폴더를 생성했습니다.");
		// }
		
		try
		{
			Files.copy(Chatfile.toPath(), newChatfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			// Files.copy(Itemfile.toPath(), newItemfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			new FileOutputStream("log/chat.log").close();
			// new FileOutputStream("log/item.log").close();
			LOGGER.info("로그 파일을 초기화 하였습니다.");
		}
		catch (IOException e)
		{
			LOGGER.warning("로그 파일 초기화 중 오류가 발생했습니다.");
		}
	}
	
	public void cleanWebConnect()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE web_connect SET fishing=0, offline=0, used_mem=0, total_mem=0"))
		{
			ps.execute();
		}
		catch (Exception e)
		{
		}
	}
	
	private void missionVariablesRemove()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var = '강화전도사';"))
		{
			ps.execute();
		}
		catch (Exception e)
		{
		}
	}
	
	private static final String[] TIMESTAMPS_CLEAN =
	{
		"DELETE FROM character_instance_time WHERE time <= ?",
		"DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?"
	};
	
	private void AutoLunaReset()
	{
		String selectQuery = "SELECT id, send_time FROM auto_lunabuy WHERE checked = 0";
		String updateQuery = "UPDATE auto_lunabuy SET checked = 3 WHERE id = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement selectStmt = con.prepareStatement(selectQuery);
			PreparedStatement updateStmt = con.prepareStatement(updateQuery);
			ResultSet rs = selectStmt.executeQuery())
		{
			long currentTime = System.currentTimeMillis(); // 현재 시간을 밀리세컨드 단위로 가져옴
			
			while (rs.next())
			{
				int id = rs.getInt("id");
				long sendTime = rs.getLong("send_time"); // 밀리세컨드 단위의 send_time
				
				// 현재 시간보다 24시간이 지났는지 확인
				if ((currentTime - sendTime) >= 86400000) // 86400000 밀리세컨드는 24시간
				{
					updateStmt.setInt(1, id);
					updateStmt.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static DailyTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DailyTaskManager INSTANCE = new DailyTaskManager();
	}
}
