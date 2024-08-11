package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.OfflineTraderTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;

public class OfflineModeManager
{
	private static final Logger LOGGER = Logger.getLogger(OfflineModeManager.class.getName());
	
	protected OfflineModeManager()
	{
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
		ThreadPool.scheduleAtFixedRate(this::offline, startDelay, 1800000);
	}
	
	private void offline()
	{
		int cleanCount = 0;
		try (Connection con = DatabaseFactory.getConnection();
			Statement stm = con.createStatement();
			ResultSet rs = stm.executeQuery("SELECT charId, time FROM character_offline_trade"))
		{
			while (rs.next())
			{
				if (Config.OFFLINE_MAX_DAYS > 0)
				{
					final Player seller = World.getInstance().getPlayer(rs.getInt("charId"));
					final Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(rs.getLong("time"));
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS);
					
					if (cal.getTimeInMillis() < System.currentTimeMillis())
					{
						OfflineTraderTable.getInstance().removeTrader(rs.getInt("charId"));
						Disconnection.of(seller).storeMe().deleteMe();
						Disconnection.of(seller).defaultSequence(LeaveWorld.STATIC_PACKET);
						cleanCount++;
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		if (cleanCount > 0)
		{
			LOGGER.info("오프라인 상점: " + cleanCount + "개의 오프라인 상점을 정리하였습니다.");
		}
	}
	
	public static OfflineModeManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final OfflineModeManager INSTANCE = new OfflineModeManager();
	}
}