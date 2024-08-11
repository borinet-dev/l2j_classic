package handlers.dailymissionhandlers;

import java.util.function.Consumer;

import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerClanCreate;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerClanJoin;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

public class ClanDailyMissionHandler extends AbstractDailyMissionHandler
{
	public ClanDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return (entry != null) && (entry.getStatus() == DailyMissionStatus.수령가능);
	}
	
	@Override
	public void init()
	{
		Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_PLAYER_CLAN_JOIN, (Consumer<OnPlayerClanJoin>) this::onPlayerClanJoin, this));
		Containers.Global().addListener(new ConsumerEventListener(this, EventType.ON_PLAYER_CLAN_CREATE, (Consumer<OnPlayerClanCreate>) this::onPlayerClanCreate, this));
	}
	
	private void onPlayerClanJoin(OnPlayerClanJoin event)
	{
		final DailyMissionPlayerEntry missionData = getPlayerEntry(event.getClanMember().getObjectId(), true);
		processMission(missionData);
	}
	
	private void onPlayerClanCreate(OnPlayerClanCreate event)
	{
		final DailyMissionPlayerEntry missionData = getPlayerEntry(event.getPlayer().getObjectId(), true);
		processMission(missionData);
	}
	
	private void processMission(DailyMissionPlayerEntry missionData)
	{
		if (DailyMissionStatus.수령완료.equals(missionData.getStatus()))
		{
			return;
		}
		missionData.setProgress(1);
		missionData.setStatus(DailyMissionStatus.수령가능);
		storePlayerEntry(missionData);
	}
}