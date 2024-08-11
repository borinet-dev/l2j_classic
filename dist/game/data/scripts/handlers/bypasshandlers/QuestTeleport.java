package handlers.bypasshandlers;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.HtmlActionScope;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;

public class QuestTeleport implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"quest_teleport"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (command.startsWith("quest_teleport"))
		{
			final String teleBuypass = command.replace("quest_teleport ", "");
			
			final String[] loc = teleBuypass.split(" ");
			int x = Integer.parseInt(loc[0]);
			int y = Integer.parseInt(loc[1]);
			int z = Integer.parseInt(loc[2]);
			
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
			player.disableAllSkills();
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
