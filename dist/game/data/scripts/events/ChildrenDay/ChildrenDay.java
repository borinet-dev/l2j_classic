package events.ChildrenDay;

import java.util.Calendar;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class ChildrenDay extends LongTimeEvent
{
	private static final String EVENT_NAME = "어린이날선물";
	
	public ChildrenDay()
	{
	}
	
	private void checkChildrenDay(Player player)
	{
		String topic = "오늘은 어린이날!";
		String body = "오늘도 어김없이 " + Config.SERVER_NAME_KOR + "에 오신 분들을 위로하며, 어린이날 선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
		String items = "41272,1;41273,1";
		int checkGift = player.getAccountVariables().getInt(EVENT_NAME, 0);
		
		if ((player.getLevel() >= 20) && (checkGift != 1))
		{
			BorinetUtil.getInstance().sendEventMail(player, topic, body, items, EVENT_NAME, false);
		}
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "어린이날 이벤트가 진행 중 입니다!"));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (isChildrenDay())
		{
			if (event.getPlayer().isInCategory(CategoryType.FIRST_CLASS_GROUP) && (event.getPlayer().getLevel() >= 20))
			{
				ThreadPool.schedule(() -> checkChildrenDay(event.getPlayer()), 1000);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (isChildrenDay())
		{
			ThreadPool.schedule(() -> checkChildrenDay(event.getPlayer()), 1000);
		}
	}
	
	private boolean isChildrenDay()
	{
		return (BorinetTask.Month() == Calendar.MAY) && (BorinetTask.Days() == 5);
	}
	
	public static void main(String[] args)
	{
		new ChildrenDay();
	}
}
