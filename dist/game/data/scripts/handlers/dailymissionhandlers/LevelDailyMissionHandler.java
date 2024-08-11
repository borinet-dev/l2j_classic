package handlers.dailymissionhandlers;

import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author 보리넷 가츠
 */
public class LevelDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _level;
	private final String _name;
	
	public LevelDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_level = holder.getParams().getInt("level");
		_name = holder.getName();
	}
	
	@Override
	public void init()
	{
		Containers.Players().addListener(new ConsumerEventListener(this, EventType.ON_PLAYER_LEVEL_CHANGED, (OnPlayerLevelChanged event) -> onPlayerLevelChanged(event), this));
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if ((entry != null) && (player.isMainClassActive()))
		{
			switch (entry.getStatus())
			{
				case 수령가능:
				case 수령완료:
				{
					return true;
				}
				case 진행중:
				{
					if (player.getLevel() >= _level)
					{
						entry.setStatus(DailyMissionStatus.수령가능);
						storePlayerEntry(entry);
					}
					break;
				}
			}
		}
		return false;
	}
	
	@Override
	public void reset()
	{
	}
	
	@Override
	public int getProgress(Player player)
	{
		return _level;
	}
	
	private void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final Player player = event.getPlayer();
		if (player.isMainClassActive() && (player.getLevel() >= _level))
		{
			final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
			if (entry.getStatus() == DailyMissionStatus.진행중)
			{
				entry.setStatus(DailyMissionStatus.수령가능);
				storePlayerEntry(entry);
				missionComplete(player, _name);
			}
		}
	}
}
