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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.instancemanager.tasks.GrandBossManagerStoreTask;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.interfaces.IStorable;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * Grand Boss manager.
 * @author DaRkRaGe Revised by Emperorc
 */
public class GrandBossManager implements IStorable
{
	protected static long _eventStartTime;
	protected static long _eventEndTime;
	// SQL queries
	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set respawn_time = ?, status = ? where boss_id = ?";
	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";
	
	protected static final Logger LOGGER = Logger.getLogger(GrandBossManager.class.getName());
	
	protected static Map<Integer, GrandBoss> _bosses = new ConcurrentHashMap<>();
	
	protected static Map<Integer, StatSet> _storedInfo = new HashMap<>();
	
	private final Map<Integer, Integer> _bossStatus = new HashMap<>();
	
	protected GrandBossManager()
	{
		init();
	}
	
	private void init()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT * from grandboss_data ORDER BY boss_id"))
		{
			// Read all info from DB, and store it for AI to read and decide what to do faster than accessing DB in real time
			while (rs.next())
			{
				final int bossId = rs.getInt("boss_id");
				if (NpcData.getInstance().getTemplate(bossId) != null)
				{
					final StatSet info = new StatSet();
					info.set("loc_x", rs.getInt("loc_x"));
					info.set("loc_y", rs.getInt("loc_y"));
					info.set("loc_z", rs.getInt("loc_z"));
					info.set("heading", rs.getInt("heading"));
					info.set("respawn_time", rs.getLong("respawn_time"));
					info.set("currentHP", rs.getDouble("currentHP") * Config.GRANDBOSS_HP_MULTIPLIER);
					info.set("currentMP", rs.getDouble("currentMP") * Config.GRANDBOSS_MP_MULTIPLIER);
					final int status = rs.getInt("status");
					_bossStatus.put(bossId, status);
					_storedInfo.put(bossId, info);
					if (status == 0)
					{
						LOGGER.info(NpcData.getInstance().getTemplate(bossId).getName() + "(" + bossId + "): 레이드 가능");
					}
					else
					{
						String date = BorinetUtil.dataDateFormatKor.format(new Date(info.getLong("respawn_time")));
						LOGGER.info(NpcData.getInstance().getTemplate(bossId).getName() + "(" + bossId + ")" + ": 레이드 불가능(다음 스폰시간: " + date + ")");
					}
				}
				else
				{
					LOGGER.warning(getClass().getSimpleName() + ": Could not find GrandBoss NPC template for " + bossId);
				}
			}
			// LOGGER.info(getClass().getSimpleName() + ": Loaded " + _storedInfo.size() + " instances.");
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Could not load grandboss_data table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while initializing GrandBossManager: " + e.getMessage(), e);
		}
		ThreadPool.scheduleAtFixedRate(new GrandBossManagerStoreTask(), 5 * 60 * 1000, 5 * 60 * 1000);
	}
	
	public int getStatus(int bossId)
	{
		if (!_bossStatus.containsKey(bossId))
		{
			return -1;
		}
		return _bossStatus.get(bossId);
	}
	
	public void setStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		if (status == 3)
		{
			Broadcast.toAllOnlinePlayersOnScreen("스페셜 보스 [" + NpcData.getInstance().getTemplate(bossId).getName() + "] 레이드를 성공하였습니다!");
		}
		updateDb(bossId, true);
	}
	
	/**
	 * Adds a GrandBoss to the list of bosses.
	 * @param boss
	 */
	public void addBoss(GrandBoss boss)
	{
		if (boss != null)
		{
			_bosses.put(boss.getId(), boss);
		}
	}
	
	public GrandBoss getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}
	
	public StatSet getStatSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}
	
	public void setStatSet(int bossId, StatSet info)
	{
		_storedInfo.put(bossId, info);
		updateDb(bossId, false);
	}
	
	@Override
	public boolean storeMe()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (Entry<Integer, StatSet> e : _storedInfo.entrySet())
			{
				final GrandBoss boss = _bosses.get(e.getKey());
				final StatSet info = e.getValue();
				if ((boss == null) || (info == null))
				{
					try (PreparedStatement update = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
					{
						update.setInt(1, _bossStatus.get(e.getKey()));
						update.setInt(2, e.getKey());
						update.executeUpdate();
						update.clearParameters();
					}
				}
				else
				{
					try (PreparedStatement update = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
					{
						update.setLong(1, info.getLong("respawn_time"));
						update.setInt(2, _bossStatus.get(e.getKey()));
						update.setInt(3, e.getKey());
						update.executeUpdate();
						update.clearParameters();
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Couldn't store grandbosses to database: " + e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	private void updateDb(int bossId, boolean statusOnly)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final GrandBoss boss = _bosses.get(bossId);
			final StatSet info = _storedInfo.get(bossId);
			if (statusOnly || (boss == null) || (info == null))
			{
				try (PreparedStatement ps = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
				{
					ps.setInt(1, _bossStatus.get(bossId));
					ps.setInt(2, bossId);
					ps.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement ps = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
				{
					ps.setLong(1, info.getLong("respawn_time"));
					ps.setInt(2, _bossStatus.get(bossId));
					ps.setInt(3, bossId);
					ps.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Couldn't update grandbosses to database:" + e.getMessage(), e);
		}
	}
	
	/**
	 * Saves all Grand Boss info and then clears all info from memory, including all schedules.
	 */
	public void cleanUp()
	{
		storeMe();
		
		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
	}
	
	/**
	 * Gets the single instance of {@code GrandBossManager}.
	 * @return single instance of {@code GrandBossManager}
	 */
	public static GrandBossManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GrandBossManager INSTANCE = new GrandBossManager();
	}
}
