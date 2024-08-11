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
package org.l2jmobius.gameserver.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.CaptchaSystem.CaptchaHandler;
import org.l2jmobius.gameserver.util.CaptchaSystem.CaptchaTimer;

/**
 * @author BiggBoss
 * @author 보리넷 가츠
 */
public class BotReportTable
{
	protected static final Logger LOGGER = Logger.getLogger(BotReportTable.class.getName());
	
	public static final int ATTACK_ACTION_BLOCK_ID = -1;
	public static final int TRADE_ACTION_BLOCK_ID = -2;
	public static final int PARTY_ACTION_BLOCK_ID = -3;
	public static final int ACTION_BLOCK_ID = -4;
	public static final int CHAT_BLOCK_ID = -5;
	
	private static final String SQL_INSERT_REPORTED_CHAR_DATA = "INSERT INTO bot_reported_char_data (reporterName, reporterId, botName, botId, reportTime) VALUES (?,?,?,?,?)";
	private Map<Integer, ReporterCharData> _charRegistry;
	private Map<Integer, BotCharData> _Bots;
	
	protected BotReportTable()
	{
		if (Config.ENABLE_BOTREPORT_SYSTEM)
		{
			_charRegistry = new ConcurrentHashMap<>();
			_Bots = new ConcurrentHashMap<>();
			
			scheduleResetPointTask();
			LOGGER.info("사동사냥신고 기능을 활성화하였습니다.");
		}
	}
	
	private void saveReportedCharData(String reporterName, int reporterId, String botName, int botId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_INSERT_REPORTED_CHAR_DATA))
		{
			ps.setString(1, reporterName);
			ps.setInt(2, reporterId);
			ps.setString(3, botName);
			ps.setInt(4, botId);
			ps.setString(5, BorinetUtil.dataDateFormat.format(new Date(System.currentTimeMillis())));
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "신고된 캐릭터 데이터를 데이터베이스에 업데이트할 수 없습니다.", e);
		}
	}
	
	/**
	 * Attempts to perform a bot report. R/W to ip and char id registry is synchronized. Triggers bot punish management
	 * @param reporter (Player who issued the report)
	 * @return True, if the report was registered, False otherwise
	 */
	public boolean reportBot(Player reporter)
	{
		final WorldObject target = reporter.getTarget();
		if (target == null)
		{
			return false;
		}
		
		final Creature bot = ((Creature) target);
		// final Player botChar = ((Player) target);
		if ((!bot.isPlayer() && !bot.isFakePlayer()) || (bot.isFakePlayer() && !((Npc) bot).getTemplate().isFakePlayerTalkable()) || (target.getObjectId() == reporter.getObjectId()))
		{
			return false;
		}
		
		if (bot.isInsideZone(ZoneId.PEACE) || bot.isInsideZone(ZoneId.PVP) || bot.isInsideZone(ZoneId.SIEGE))
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_A_CHARACTER_WHO_IS_IN_A_PEACE_ZONE_OR_A_BATTLEGROUND);
			return false;
		}
		
		if (!bot.getActingPlayer().isInBattle() || !bot.isInCombat() || bot.getActingPlayer().isSitting() || bot.getActingPlayer().isFishing() || bot.getActingPlayer().isInStoreMode() || bot.isDead() || bot.getActingPlayer().isInSiege())
		{
			reporter.sendMessage("사냥중이 아닌 캐릭터는 신고할 수 없습니다.");
			return false;
		}
		
		boolean isCaptchaActive = bot.getActingPlayer().getQuickVarB("IsCaptchaActive", false);
		if (isCaptchaActive)
		{
			reporter.sendMessage("대상은 현재 보안문자 입력 중 입니다.");
			return false;
		}
		
		if (bot.getActingPlayer().getVariables().getBoolean("자동따라가기", false))
		{
			reporter.sendMessage("타겟은 현재 따라가기 모드상태이므로 신고할 수 없습니다.");
			return false;
		}
		
		if (reporter.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(reporter))
		{
			reporter.sendMessage("올림피아드에 참가 중에는 신고할 수 없습니다.");
			return false;
		}
		
		if (reporter.isInInstance())
		{
			reporter.sendMessage("인스턴트 던전 내에서는 신고할 수 없습니다.");
			return false;
		}
		
		if (bot.isPlayer() && bot.getActingPlayer().isInOlympiadMode())
		{
			reporter.sendPacket(SystemMessageId.THIS_CHARACTER_CANNOT_MAKE_A_REPORT_YOU_CANNOT_MAKE_A_REPORT_WHILE_LOCATED_INSIDE_A_PEACE_ZONE_OR_A_BATTLEGROUND_WHILE_YOU_ARE_AN_OPPOSING_CLAN_MEMBER_DURING_A_CLAN_WAR_OR_WHILE_PARTICIPATING_IN_THE_OLYMPIAD);
			return false;
		}
		
		if ((bot.getClan() != null) && (reporter.getClan() != null) && bot.getClan().isAtWarWith(reporter.getClan()))
		{
			reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_WHEN_A_CLAN_WAR_HAS_BEEN_DECLARED);
			return false;
		}
		
		if ((bot.getActingPlayer().getQuickVarL("LastExp") + (Config.LAST_EXP_SECONDS * 1000)) < System.currentTimeMillis())
		{
			reporter.sendMessage("1분이상 사냥하지 않은 캐릭터는 신고할 수 없습니다.");
			return false;
		}
		if ((bot.getActingPlayer().getQuickVarL("LastCaptcha") + (Config.CAPTCHA_TIME_BETWEEN_TIME * 60000)) > System.currentTimeMillis())
		{
			reporter.sendMessage("대상은 자동사냥 테스트에 통과하여 당분간은 신고하실 수 없습니다.");
			return false;
		}
		if (!CaptchaTimer.getInstance().canReportBotAgain(reporter))
		{
			reporter.sendMessage("연속으로 신고할 수 없습니다.");
			return false;
		}
		
		BotCharData rcd = _Bots.get(bot.getObjectId());
		ReporterCharData rcdRep = _charRegistry.get(reporter.getObjectId());
		final int reporterId = reporter.getObjectId();
		
		if (!reporter.isGM())
		{
			synchronized (this)
			{
				if (rcdRep != null)
				{
					if (rcdRep.getPointsLeft() == 0)
					{
						reporter.sendPacket(SystemMessageId.YOU_HAVE_USED_ALL_AVAILABLE_POINTS_POINTS_ARE_RESET_EVERYDAY_AT_NOON);
						return false;
					}
					
					final long reuse = rcdRep.getLastReporTime();
					if (reuse > System.currentTimeMillis())
					{
						final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
						final int minutes = (int) ((remainingTime % 3600) / 60);
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CAN_MAKE_ANOTHER_REPORT_IN_S1_MINUTE_S_YOU_HAVE_S2_POINT_S_REMAINING_ON_THIS_ACCOUNT);
						sm.addInt(minutes);
						sm.addInt(rcdRep.getPointsLeft());
						reporter.sendPacket(sm);
						return false;
					}
				}
				
				if (rcd != null)
				{
					final long reuse = rcd.getLastBotTime();
					if (reuse > System.currentTimeMillis())
					{
						final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
						final int minutes = (int) ((remainingTime % 3600) / 60);
						reporter.sendMessage("타겟은 " + minutes + "분 이후에 신고가 가능합니다.");
						return false;
					}
				}
				
				final long curTimeReporter = System.currentTimeMillis() + Config.BOTREPORT_REPORT_DELAY;
				final long curTimeBot = System.currentTimeMillis() + (Config.BOTREPORT_REPORT_DELAY / 3);
				if (rcd == null)
				{
					rcd = new BotCharData();
					_Bots.put(bot.getObjectId(), rcd);
				}
				rcd.addBot(reporterId, curTimeBot);
				if (rcdRep == null)
				{
					rcdRep = new ReporterCharData();
				}
				rcdRep.registerReport(curTimeReporter);
				
				_charRegistry.put(reporterId, rcdRep);
			}
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.C1_WAS_REPORTED_AS_A_BOT);
		sm.addString(bot.getName());
		reporter.sendPacket(sm);
		saveReportedCharData(reporter.getName(), reporter.getObjectId(), bot.getName(), bot.getObjectId());
		CaptchaHandler.Captcha(reporter, bot.getActingPlayer());
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_USED_A_REPORT_POINT_ON_C1_YOU_HAVE_S2_POINTS_REMAINING_ON_THIS_ACCOUNT);
		sm.addString(bot.getName());
		sm.addInt(rcdRep.getPointsLeft());
		reporter.sendPacket(sm);
		
		return true;
	}
	
	public static boolean AutoReport(Player player)
	{
		boolean isCaptchaActive = player.getQuickVarB("IsCaptchaActive", false);
		if ((player.getLevel() < 40) || isCaptchaActive)
		{
			return false;
		}
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player) || player.isInInstance() || !player.isInBattle() || !player.isInCombat() || player.isSitting() || player.isFishing() || player.isInStoreMode() || player.isDead() || player.isInSiege())
		{
			return false;
		}
		if (player.getVariables().getBoolean("자동따라가기", false))
		{
			return false;
		}
		if (player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.SIEGE) || (player.getPvpFlag() > 0) || player.isInOlympiadMode())
		{
			return false;
		}
		if ((player.getQuickVarL("LastCaptcha") + (Config.LAST_CAPTCHA_TIME * 60000)) > System.currentTimeMillis())
		{
			return false;
		}
		return true;
	}
	
	void resetPointsAndSchedule()
	{
		synchronized (_charRegistry)
		{
			for (ReporterCharData rcd : _charRegistry.values())
			{
				rcd.setPoints(7);
			}
		}
		
		scheduleResetPointTask();
	}
	
	private void scheduleResetPointTask()
	{
		try
		{
			final String[] hour = Config.BOTREPORT_RESETPOINT_HOUR;
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
			c.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
			c.set(Calendar.SECOND, 0);
			if (System.currentTimeMillis() > c.getTimeInMillis())
			{
				c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
			}
			
			ThreadPool.schedule(new ResetPointTask(), c.getTimeInMillis() - System.currentTimeMillis());
		}
		catch (Exception e)
		{
			ThreadPool.schedule(new ResetPointTask(), 24 * 3600 * 1000);
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Could not properly schedule bot report points reset task. Scheduled in 24 hours.", e);
		}
	}
	
	public static BotReportTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * Represents the info about a reporter
	 */
	private final class ReporterCharData
	{
		private long _lastReport;
		private byte _reportPoints;
		
		ReporterCharData()
		{
			_reportPoints = 7;
			_lastReport = 0;
		}
		
		void registerReport(long time)
		{
			_reportPoints -= 1;
			_lastReport = time;
		}
		
		long getLastReporTime()
		{
			return _lastReport;
		}
		
		byte getPointsLeft()
		{
			return _reportPoints;
		}
		
		void setPoints(int points)
		{
			_reportPoints = (byte) points;
		}
	}
	
	/**
	 * Represents the info about a reported character
	 */
	private final class BotCharData
	{
		private long _lastBot;
		Map<Integer, Long> _Bots;
		
		BotCharData()
		{
			_Bots = new HashMap<>();
		}
		
		void addBot(int objectId, long reportTime)
		{
			_Bots.put(objectId, reportTime);
			_lastBot = reportTime;
		}
		
		long getLastBotTime()
		{
			return _lastBot;
		}
	}
	
	private class ResetPointTask implements Runnable
	{
		public ResetPointTask()
		{
		}
		
		@Override
		public void run()
		{
			resetPointsAndSchedule();
		}
	}
	
	private static final class SingletonHolder
	{
		static final BotReportTable INSTANCE = new BotReportTable();
	}
}
