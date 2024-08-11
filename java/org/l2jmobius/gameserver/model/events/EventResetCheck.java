package org.l2jmobius.gameserver.model.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.instancemanager.DailyTaskManager;

public class EventResetCheck
{
	private static final Logger LOGGER = Logger.getLogger(EventResetCheck.class.getName());
	
	public static void resetDailyCheck()
	{
		long lastRun = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT lastRun FROM event_schedulers WHERE schedulerName = 'reset'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				lastRun = rset.getTimestamp(1).getTime() + 86390000;
				if (lastRun < System.currentTimeMillis())
				{
					DailyTaskManager.getInstance().onDailyReset();
				}
				else
				{
					LOGGER.info("데일리 리셋을 할 필요 없습니다.");
				}
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public static void resetRecomCheck()
	{
		long lastRun = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT lastRun FROM event_schedulers WHERE schedulerName = 'RecReset'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				lastRun = rset.getTimestamp(1).getTime() + 86390000;
				if (lastRun < System.currentTimeMillis())
				{
					DailyTaskManager.getInstance().onRecReset();
				}
				else
				{
					LOGGER.info("추천권 리셋을 할 필요 없습니다.");
				}
			}
		}
		catch (Exception e)
		{
		}
	}
}