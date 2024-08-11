package events.WhiteDay;

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

public class WhiteDay extends LongTimeEvent
{
	private static final int NPC = 33829;
	private static final int CANDY_BOX = 36081;
	private static final String EVENT_NAME = "화이트데이선물";
	
	public WhiteDay()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ("give".equalsIgnoreCase(event))
		{
			int whiteDayGift = player.getVariables().getInt(EVENT_NAME, 0);
			if (whiteDayGift >= 1)
			{
				player.sendMessage("알콜달콩 선물 상자는 이미 지급해드렸답니다~");
				return getHtm(player, "33829-noItem.htm");
			}
			player.getVariables().set(EVENT_NAME, 1);
			giveItems(player, CANDY_BOX, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			return "33829-1.htm";
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	private void checkWhiteDay(Player player)
	{
		String topic = "오늘은 화이트 데이!";
		String body = "오늘도 어김없이 " + Config.SERVER_NAME_KOR + "에 오신것을 환영하며, 화이트 데이 기념선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
		String items = "14766,3;37705,3;37706,3;14767,10;14768,1;9140,1";
		int checkGift = player.getAccountVariables().getInt(EVENT_NAME, 0);
		
		if ((player.getLevel() >= 20) && (checkGift != 1))
		{
			BorinetUtil.getInstance().sendEventMail(player, topic, body, items, EVENT_NAME, false);
		}
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "화이트 데이 이벤트가 진행 중 입니다!"));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (isWhiteDay())
		{
			if (event.getPlayer().isInCategory(CategoryType.FIRST_CLASS_GROUP) && (event.getPlayer().getLevel() >= 20))
			{
				ThreadPool.schedule(() -> checkWhiteDay(event.getPlayer()), 1000);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (isWhiteDay())
		{
			ThreadPool.schedule(() -> checkWhiteDay(event.getPlayer()), 1000);
		}
	}
	
	private boolean isWhiteDay()
	{
		return (BorinetTask.Month() == Calendar.MARCH) && (BorinetTask.Days() == 14);
	}
	
	public static void main(String[] args)
	{
		new WhiteDay();
	}
}
