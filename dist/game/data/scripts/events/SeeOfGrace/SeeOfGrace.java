package events.SeeOfGrace;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

public class SeeOfGrace extends LongTimeEvent
{
	// NPC
	private static final int KAVARI = 34061;
	
	private SeeOfGrace()
	{
		addStartNpc(KAVARI);
		addFirstTalkId(KAVARI);
		addTalkId(KAVARI);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34061.htm";
	}
	
	public static void main(String[] args)
	{
		new SeeOfGrace();
	}
}
