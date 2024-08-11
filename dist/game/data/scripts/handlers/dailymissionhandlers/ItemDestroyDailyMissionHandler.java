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
package handlers.dailymissionhandlers;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerItemDestroy;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author CostyKiller
 */
public class ItemDestroyDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _minLevel;
	private final String _name;
	private final List<Integer> _ids = new ArrayList<>();
	
	public ItemDestroyDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_minLevel = holder.getParams().getInt("minLevel", 0);
		_name = holder.getName();
		final String ids = holder.getParams().getString("itemIds", "");
		if (!ids.isEmpty())
		{
			for (String s : ids.split(","))
			{
				final int id = Integer.parseInt(s);
				if (!_ids.contains(id))
				{
					_ids.add(id);
				}
			}
		}
	}
	
	@Override
	public void init()
	{
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_ITEM_DESTROY, (OnPlayerItemDestroy event) -> onItemDestroy(event), this));
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return (entry != null) && (entry.getStatus() == DailyMissionStatus.수령가능);
	}
	
	private void onItemDestroy(OnPlayerItemDestroy event)
	{
		final Player player = event.getPlayer();
		if ((player.getLevel() < _minLevel) || _ids.isEmpty())
		{
			return;
		}
		if (_ids.contains(event.getItem().getId()))
		{
			processPlayerProgress(player);
		}
	}
	
	private void processPlayerProgress(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
		if (DailyMissionStatus.수령완료.equals(entry.getStatus()))
		{
			return;
		}
		if (DailyMissionStatus.진행중.equals(entry.getStatus()))
		{
			entry.setStatus(DailyMissionStatus.수령가능);
			missionComplete(player, _name);
			storePlayerEntry(entry);
		}
	}
}
