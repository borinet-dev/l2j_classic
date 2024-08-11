package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

public class Tower extends FriendlyNpc
{
	public Tower(NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public int getHateBaseAmount()
	{
		return 1000;
	}
}
