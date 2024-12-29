package ai.areas.FantasyIsle;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetTask;

import ai.AbstractNpcAI;

public class FantasyManager extends AbstractNpcAI
{
	// NPC
	private static final int MANAGER = 32378;
	private static final Location TELEPORT_AREA = new Location(-57328, -60566, -2360);
	
	private FantasyManager()
	{
		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		
		if (event.equals("area"))
		{
			if (!BorinetTask.getInstance().blockCheckerTime())
			{
				htmltext = "32378-1.htm";
			}
			else
			{
				player.teleToLocation(TELEPORT_AREA);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "32378.htm";
	}
	
	public static void main(String[] args)
	{
		new FantasyManager();
	}
}
