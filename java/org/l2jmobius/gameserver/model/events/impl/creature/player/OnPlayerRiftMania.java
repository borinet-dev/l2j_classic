package org.l2jmobius.gameserver.model.events.impl.creature.player;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.IBaseEvent;

/**
 * @author 보리넷 가츠
 */
public class OnPlayerRiftMania implements IBaseEvent
{
	private final Player _player;
	
	public OnPlayerRiftMania(Player player)
	{
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_RIFT_MANIA;
	}
}