package org.l2jmobius.gameserver.model.events.impl.item;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.IBaseEvent;
import org.l2jmobius.gameserver.model.item.instance.Item;

public class OnItemEnchant implements IBaseEvent
{
	private final Player _player;
	private final Item _item;
	
	public OnItemEnchant(Player player, Item item)
	{
		_player = player;
		_item = item;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Item getItem()
	{
		return _item;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_ITEM_ENCHANT;
	}
}
