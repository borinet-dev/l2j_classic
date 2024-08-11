package events.HappyHours;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.taskmanager.auto.AutoLuckyCoinTaskManager;

public class HappyHours extends LongTimeEvent
{
	// NPC
	private static final int SIBIS = 34262;
	// Items
	private static final int SUPPLY_BOX = 49782;
	// Other
	private static final int MIN_LEVEL = 20;
	
	private HappyHours()
	{
		addStartNpc(SIBIS);
		addFirstTalkId(SIBIS);
		addTalkId(SIBIS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "34262-1.htm":
			{
				htmltext = event;
				break;
			}
			case "giveSupplyBoxs":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					return "34262-2.htm";
				}
				if (ownsAtLeastOneItem(player, SUPPLY_BOX))
				{
					return "34262-3.htm";
				}
				giveItems(player, SUPPLY_BOX, 1);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34262.htm";
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player.isAffectedBySkill(39171))
		{
			AutoLuckyCoinTaskManager.getInstance().addAutoLuckyCoin(player);
		}
	}
	
	public static void main(String[] args)
	{
		new HappyHours();
	}
}
