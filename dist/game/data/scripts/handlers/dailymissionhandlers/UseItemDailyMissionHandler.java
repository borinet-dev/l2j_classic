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
import org.l2jmobius.gameserver.model.events.impl.item.OnItemUse;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

public class UseItemDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;
	private final int _minLevel;
	private final String _name;
	private final List<Integer> _ids = new ArrayList<>();
	
	public UseItemDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
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
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_ITEM_USE, (OnItemUse event) -> onItemUse(event), this));
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if (entry != null)
		{
			switch (entry.getStatus())
			{
				case 진행중: // Initial state
				{
					if (entry.getProgress() >= _amount)
					{
						entry.setStatus(DailyMissionStatus.수령가능);
						storePlayerEntry(entry);
					}
					break;
				}
				case 수령가능:
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private void onItemUse(OnItemUse event)
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
			if (entry.increaseProgress() >= _amount)
			{
				entry.setStatus(DailyMissionStatus.수령가능);
				missionComplete(player, _name);
			}
			storePlayerEntry(entry);
		}
	}
}
