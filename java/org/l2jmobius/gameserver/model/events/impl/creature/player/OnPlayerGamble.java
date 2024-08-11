package org.l2jmobius.gameserver.model.events.impl.creature.player;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.IBaseEvent;

public class OnPlayerGamble implements IBaseEvent
{
	private final Player _player;
	private final boolean _isSuccess;
	
	public OnPlayerGamble(Player player, boolean isSuccess)
	{
		_player = player;
		_isSuccess = isSuccess;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public boolean isSuccess()
	{
		return _isSuccess;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_GAMBLE;
	}
}
