package handlers.bypasshandlers;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;

public class Teleport implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"teleport"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (command.startsWith("teleport"))
		{
			final String teleBuypass = command.replace("teleport ", "");
			
			final String[] loc = teleBuypass.split(" ");
			int x = Integer.parseInt(loc[0]);
			int y = Integer.parseInt(loc[1]);
			int z = Integer.parseInt(loc[2]);
			
			if (player.getInventory().getInventoryItemCount(57, -1) < 20000)
			{
				player.sendMessage("아데나가 부족합니다.");
				return true;
			}
			player.disableAllSkills();
			player.sendPacket(new ShowBoard());
			player.destroyItemByItemId("teleport", 57, 20000, player, true);
			player.setInstanceById(0);
			player.teleToLocation(x, y, z, 0);
			ThreadPool.schedule(player::enableAllSkills, 1000);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
