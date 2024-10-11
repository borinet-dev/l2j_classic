package events.IdDay;

import java.util.Calendar;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
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

public class IdDay extends LongTimeEvent
{
	private static final int NPC = 40025;
	private static final String EVENT_NAME = "삼일절선물";
	
	public IdDay()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (!isIndependenceDay())
		{
			return null;
		}
		
		if ("ask".equalsIgnoreCase(event))
		{
			if (getQuestItemsCount(player, 47825) >= 1)
			{
				return getHtm(player, "40025-noItem.htm");
			}
			else if (getQuestItemsCount(player, 57) < 5000000)
			{
				return getHtm(player, "40025-noAdena.htm");
			}
			else
			{
				return "40025-1.htm";
			}
		}
		if ("give".equalsIgnoreCase(event))
		{
			if (getQuestItemsCount(player, 47825) >= 1)
			{
				return getHtm(player, "40025-noItem.htm");
			}
			if (getQuestItemsCount(player, 57) < 5000000)
			{
				return getHtm(player, "40025-noAdena.htm");
			}
			takeItems(player, 57, 5000000);
			giveItems(player, 47825, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			return "40025-2.htm";
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (!isIndependenceDay())
		{
			return null;
		}
		return npc.getId() + ".htm";
	}
	
	private void checkIndependenceDay(Player player)
	{
		String topic = "오늘은 삼일절 입니다";
		String body = "이런날도 어김없이 " + Config.SERVER_NAME_KOR + "에 오신것을 환영하며, 삼일절을 맞이해서 기념선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
		String items = "47825,1";
		int checkGift = player.getAccountVariables().getInt(EVENT_NAME, 0);
		
		if ((player.getLevel() >= 20) && (checkGift != 1))
		{
			BorinetUtil.getInstance().sendEventMail(player, topic, body, items, EVENT_NAME, false);
		}
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "삼일절 이벤트가 진행 중 입니다!"));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (isIndependenceDay())
		{
			if (event.getPlayer().isInCategory(CategoryType.FIRST_CLASS_GROUP) && (event.getPlayer().getLevel() >= 20))
			{
				ThreadPool.schedule(() -> checkIndependenceDay(event.getPlayer()), 1000);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (isIndependenceDay())
		{
			ThreadPool.schedule(() -> checkIndependenceDay(event.getPlayer()), 1000);
		}
	}
	
	private boolean isIndependenceDay()
	{
		return (BorinetTask.Month() == Calendar.MARCH) && (BorinetTask.Days() == 1);
	}
	
	public static void main(String[] args)
	{
		new IdDay();
	}
}
