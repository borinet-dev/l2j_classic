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
package org.l2jmobius.gameserver.model;

import java.util.List;
import java.util.function.Function;

import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.handler.DailyMissionHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;

/**
 * @author Sdw
 */
public class DailyMissionDataHolder
{
	private final int _id;
	private final String _name;
	private final int _classRace;
	private final List<ItemHolder> _rewardsItems;
	private final List<ClassId> _classRestriction;
	private final int _requiredCompletions;
	private final StatSet _params;
	private final boolean _dailyReset;
	private final boolean _weeklyReset;
	private final boolean _monthReset;
	private final boolean _isOneTime;
	private final boolean _isMainClassOnly;
	private final boolean _isAccountMission;
	private final boolean _isDisplayedWhenNotAvailable;
	private final boolean _missionMania;
	private final boolean _missionEvent;
	private final AbstractDailyMissionHandler _handler;
	
	public DailyMissionDataHolder(StatSet set)
	{
		final Function<DailyMissionDataHolder, AbstractDailyMissionHandler> handler = DailyMissionHandler.getInstance().getHandler(set.getString("handler"));
		_id = set.getInt("id");
		_name = set.getString("name");
		_classRace = set.getInt("classRace", 0);
		_requiredCompletions = set.getInt("requiredCompletion", 0);
		_rewardsItems = set.getList("items", ItemHolder.class);
		_classRestriction = set.getList("classRestriction", ClassId.class);
		_params = set.getObject("params", StatSet.class);
		_dailyReset = set.getBoolean("dailyReset", true);
		_weeklyReset = set.getBoolean("weeklyReset", false);
		_monthReset = set.getBoolean("monthReset", false);
		_isOneTime = set.getBoolean("isOneTime", true);
		_isMainClassOnly = set.getBoolean("isMainClassOnly", true);
		_isAccountMission = set.getBoolean("isAccountMission", false);
		_isDisplayedWhenNotAvailable = set.getBoolean("isDisplayedWhenNotAvailable", true);
		_missionMania = set.getBoolean("missionMania", false);
		_missionEvent = set.getBoolean("missionEvent", false);
		_handler = handler != null ? handler.apply(this) : null;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public List<ClassId> getClassRestriction()
	{
		return _classRestriction;
	}
	
	public List<ItemHolder> getRewards()
	{
		return _rewardsItems;
	}
	
	public int getRequiredCompletions()
	{
		return _requiredCompletions;
	}
	
	public StatSet getParams()
	{
		return _params;
	}
	
	public boolean dailyReset()
	{
		return _dailyReset;
	}
	
	public boolean weeklyReset()
	{
		return _weeklyReset;
	}
	
	public boolean monthReset()
	{
		return _monthReset;
	}
	
	public boolean isOneTime()
	{
		return _isOneTime;
	}
	
	public boolean isMainClassOnly()
	{
		return _isMainClassOnly;
	}
	
	public boolean isAccountMission()
	{
		return _isAccountMission;
	}
	
	public boolean isDisplayedWhenNotAvailable()
	{
		return _isDisplayedWhenNotAvailable;
	}
	
	public boolean missionMania()
	{
		return _missionMania;
	}
	
	public boolean missionEvent()
	{
		return _missionEvent;
	}
	
	public boolean isDisplayable(Player player)
	{
		// Check if its main class only
		if (isMainClassOnly() && player.isSubClassActive())
		{
			return false;
		}
		if (((_classRace == 1) && !player.isMage()) || ((_classRace == 2) && player.isMage()))
		{
			return false;
		}
		
		// Check for specific class restrictions
		if (!_classRestriction.isEmpty() && !_classRestriction.contains(player.getClassId()))
		{
			return false;
		}
		
		final int status = getStatus(player);
		if (!isDisplayedWhenNotAvailable() && (status == DailyMissionStatus.진행중.getClientId()))
		{
			return false;
		}
		
		// Show only if its repeatable, recently completed or incompleted that has met the checks above.
		return (!isOneTime() || isRecentlyCompleted(player) || (status != DailyMissionStatus.수령완료.getClientId()));
	}
	
	public void requestReward(Player player)
	{
		if ((_handler != null) && isDisplayable(player))
		{
			_handler.requestReward(player);
		}
	}
	
	public int getStatus(Player player)
	{
		return _handler != null ? _handler.getStatus(player) : DailyMissionStatus.진행중.getClientId();
	}
	
	public int getProgress(Player player)
	{
		return _handler != null ? _handler.getProgress(player) : DailyMissionStatus.진행중.getClientId();
	}
	
	public boolean isRecentlyCompleted(Player player)
	{
		return (_handler != null) && _handler.isRecentlyCompleted(player);
	}
	
	public void reset()
	{
		if (_handler != null)
		{
			_handler.reset();
		}
	}
	
	public void resetWeekly()
	{
		if (_handler != null)
		{
			_handler.resetWeekly();
			_handler.reset();
		}
	}
	
	public void resetMonth()
	{
		if (_handler != null)
		{
			_handler.resetMonth();
			_handler.reset();
		}
	}
	
	public void resetMonthWeekly()
	{
		if (_handler != null)
		{
			_handler.resetWeekly();
			_handler.resetMonth();
			_handler.reset();
		}
	}
}
