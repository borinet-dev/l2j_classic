package events.ChefMonkeyEvent;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

public class ChefMonkeyEvent extends LongTimeEvent
{
	// NPC
	private static final int CHEF_MONKEY = 34292;
	
	private ChefMonkeyEvent()
	{
		addStartNpc(CHEF_MONKEY);
		addFirstTalkId(CHEF_MONKEY);
		addTalkId(CHEF_MONKEY);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34292-01.htm";
	}
	
	public static void main(String[] args)
	{
		new ChefMonkeyEvent();
	}
}
