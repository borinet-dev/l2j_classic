package handlers.dailymissionhandlers;

import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerGamble;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

public class GambleDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;
	private final int _minLevel;
	private final String _name;
	
	public GambleDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
		_minLevel = holder.getParams().getInt("minLevel", 0);
		_name = holder.getName();
	}
	
	@Override
	public void init()
	{
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_GAMBLE, (OnPlayerGamble event) -> onPlayerGamble(event), this));
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if (entry != null)
		{
			switch (entry.getStatus())
			{
				case 진행중:
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
	
	private void onPlayerGamble(OnPlayerGamble event)
	{
		final Player player = event.getPlayer();
		final boolean isSuccess = event.isSuccess();
		
		if (player.getLevel() < _minLevel)
		{
			return;
		}
		if (isSuccess)
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
