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
package org.l2jmobius.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.enums.SpecialItemType;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.ListenersContainer;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerMissionEvent;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerMissionMania;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author Sdw
 */
public abstract class AbstractDailyMissionHandler extends ListenersContainer
{
	protected Logger LOGGER = Logger.getLogger(getClass().getName());
	
	private final Map<Integer, DailyMissionPlayerEntry> _entries = new ConcurrentHashMap<>();
	private final DailyMissionDataHolder _holder;
	
	protected AbstractDailyMissionHandler(DailyMissionDataHolder holder)
	{
		_holder = holder;
		init();
	}
	
	public DailyMissionDataHolder getHolder()
	{
		return _holder;
	}
	
	public abstract boolean isAvailable(Player player);
	
	public abstract void init();
	
	public int getStatus(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return entry != null ? entry.getStatus().getClientId() : DailyMissionStatus.진행중.getClientId();
	}
	
	public int getProgress(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return entry != null ? entry.getProgress() : 0;
	}
	
	public boolean isRecentlyCompleted(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return (entry != null) && entry.isRecentlyCompleted();
	}
	
	public synchronized void reset()
	{
		if (_holder.dailyReset())
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM character_daily_rewards WHERE rewardId = ?"))
			{
				ps.setInt(1, _holder.getId());
				ps.execute();
				
				for (Player players : World.getInstance().getPlayers())
				{
					final DailyMissionPlayerEntry entry = getPlayerEntry(players.getObjectId(), true);
					_entries.computeIfAbsent(entry.getObjectId(), id -> entry);
				}
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "Error while clearing data for: " + getClass().getSimpleName(), e);
			}
			finally
			{
				_entries.clear();
			}
		}
	}
	
	public synchronized void resetWeekly()
	{
		if (_holder.weeklyReset())
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM character_daily_rewards WHERE rewardId = ?"))
			{
				ps.setInt(1, _holder.getId());
				ps.execute();
				
				for (Player players : World.getInstance().getPlayers())
				{
					final DailyMissionPlayerEntry entry = getPlayerEntry(players.getObjectId(), true);
					_entries.computeIfAbsent(entry.getObjectId(), id -> entry);
				}
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "Error while clearing data for: " + getClass().getSimpleName(), e);
			}
			finally
			{
				_entries.clear();
			}
		}
	}
	
	public synchronized void resetMonth()
	{
		if (_holder.monthReset())
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM character_daily_rewards WHERE rewardId = ?"))
			{
				ps.setInt(1, _holder.getId());
				ps.execute();
				
				for (Player players : World.getInstance().getPlayers())
				{
					final DailyMissionPlayerEntry entry = getPlayerEntry(players.getObjectId(), true);
					_entries.computeIfAbsent(entry.getObjectId(), id -> entry);
				}
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "Error while clearing data for: " + getClass().getSimpleName(), e);
			}
			finally
			{
				_entries.clear();
			}
		}
	}
	
	public boolean requestReward(Player player)
	{
		if (isAvailable(player))
		{
			giveRewards(player);
			
			final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
			entry.setStatus(DailyMissionStatus.수령완료);
			entry.setLastCompleted(System.currentTimeMillis());
			entry.setRecentlyCompleted(true);
			storePlayerEntry(entry);
			
			if (_holder.isAccountMission())
			{
				requestRewardAccount(player, entry.getRewardId());
			}
			return true;
		}
		return false;
	}
	
	private void requestRewardAccount(Player player, int rewardId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			// 해당 캐릭터의 account_name 가져오기
			String accountName = null;
			try (PreparedStatement ps = con.prepareStatement("SELECT account_name FROM characters WHERE charId = ?"))
			{
				ps.setInt(1, player.getObjectId());
				try (ResultSet rs = ps.executeQuery())
				{
					if (rs.next())
					{
						accountName = rs.getString("account_name");
					}
				}
			}
			
			// 동일 계정 내 모든 캐릭터 업데이트
			if (accountName != null)
			{
				try (PreparedStatement charPs = con.prepareStatement("SELECT charId FROM characters WHERE account_name = ?"))
				{
					charPs.setString(1, accountName);
					
					try (ResultSet rs = charPs.executeQuery())
					{
						while (rs.next())
						{
							int charId = rs.getInt("charId");
							
							// 캐시 업데이트 및 데이터 저장
							DailyMissionPlayerEntry charEntry = getPlayerEntry(charId, true);
							charEntry.setStatus(DailyMissionStatus.수령완료);
							charEntry.setLastCompleted(System.currentTimeMillis());
							charEntry.setRecentlyCompleted(true);
							storePlayerEntry(charEntry);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "계정내 모든 캐릭터에 대한 미션 (" + rewardId + ") 완료 중 오류 발생. 이름: " + player.getName(), e);
		}
	}
	
	protected void giveRewards(Player player)
	{
		for (ItemHolder reward : _holder.getRewards())
		{
			if (reward.getId() == SpecialItemType.CLAN_REPUTATION.getClientId())
			{
				player.getClan().addReputationScore((int) reward.getCount());
			}
			else if (reward.getId() == SpecialItemType.FAME.getClientId())
			{
				player.setFame(player.getFame() + (int) reward.getCount());
				player.broadcastUserInfo();
			}
			else
			{
				player.addItem("Daily Reward", reward, player, true);
			}
		}
	}
	
	protected void storePlayerEntry(DailyMissionPlayerEntry entry)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_daily_rewards (charId, rewardId, status, progress, lastCompleted) VALUES (?, ?, ?, ?, ?)"))
		{
			ps.setInt(1, entry.getObjectId());
			ps.setInt(2, entry.getRewardId());
			ps.setInt(3, entry.getStatus().getClientId());
			ps.setInt(4, entry.getProgress());
			ps.setLong(5, entry.getLastCompleted());
			ps.execute();
			
			// Cache if not exists
			_entries.computeIfAbsent(entry.getObjectId(), id -> entry);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while saving reward " + entry.getRewardId() + " for player: " + entry.getObjectId() + " in database: ", e);
		}
	}
	
	protected void missionComplete(Player player, String name)
	{
		if (_holder.missionMania())
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMissionMania(player), player);
		}
		if (_holder.missionEvent())
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMissionEvent(player), player);
		}
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "[" + name + "] 미션이 완료되었습니다."));
		Broadcast.toPlayerScreenMessageS(player, "[" + name + "] 미션이 완료되었습니다.");
		return;
	}
	
	protected DailyMissionPlayerEntry getPlayerEntry(int objectId, boolean createIfNone)
	{
		final DailyMissionPlayerEntry existingEntry = _entries.get(objectId);
		if (existingEntry != null)
		{
			return existingEntry;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM character_daily_rewards WHERE charId = ? AND rewardId = ?"))
		{
			ps.setInt(1, objectId);
			ps.setInt(2, _holder.getId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					final DailyMissionPlayerEntry entry = new DailyMissionPlayerEntry(rs.getInt("charId"), rs.getInt("rewardId"), rs.getInt("status"), rs.getInt("progress"), rs.getLong("lastCompleted"));
					_entries.put(objectId, entry);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while loading reward " + _holder.getId() + " for player: " + objectId + " in database: ", e);
		}
		
		if (createIfNone)
		{
			final DailyMissionPlayerEntry entry = new DailyMissionPlayerEntry(objectId, _holder.getId());
			_entries.put(objectId, entry);
			return entry;
		}
		return null;
	}
	
	public long[] checkReward(Player player, int rewardId)
	{
		int status = 0;
		long date = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT status, lastCompleted FROM character_daily_rewards WHERE charId = ? AND rewardId = ?"))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, rewardId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					status = rs.getInt("status");
					date = rs.getLong("lastCompleted");
				}
			}
		}
		catch (Exception e)
		{
		}
		return new long[]
		{
			status,
			date
		};
	}
}
