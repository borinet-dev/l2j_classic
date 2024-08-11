package events.InstanceOut;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

/**
 * @author 보리넷 가츠
 */
public class InstanceOut extends LongTimeEvent
{
	public static final int 텔레포터 = 34185;
	
	private InstanceOut()
	{
		addStartNpc(텔레포터);
		addFirstTalkId(텔레포터);
		addTalkId(텔레포터);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (!isEventPeriod())
		{
			return null;
		}
		
		switch (event)
		{
			case "ExitInstance":
			{
				if ((npc != null) && (npc.getId() == 텔레포터) && npc.isInInstance())
				{
					npc.getInstanceWorld().ejectPlayer(player);
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new InstanceOut();
	}
}