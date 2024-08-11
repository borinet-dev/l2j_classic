package events.ValentineDay;

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
import org.l2jmobius.gameserver.util.Util;

public class ValentineDay extends LongTimeEvent
{
	private static final int NPC = 4301;
	private static final int CAKE = 20198;
	private static final int FANTASY_CAKE = 20199;
	private static final String EVENT_NAME = "발렌타인데이선물";
	
	public ValentineDay()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ("4301-3.htm".equalsIgnoreCase(event))
		{
			if (getQuestItemsCount(player, 57) < 100000000)
			{
				int fee = 100000000;
				player.sendMessage("환상의 발렌타인 케이크를 만들기 위해서는 " + Util.formatAdena(fee) + " 아데나가 필요합니다.");
				String htmltext = getHtm(player, "4301-noAdena.htm");
				return htmltext.replace("%fee%", Util.formatAdena(fee));
			}
			else if (getQuestItemsCount(player, CAKE) < 1)
			{
				player.sendMessage("환상의 발렌타인 케이크를 만들기 위해서는 [완벽한 발렌타인 케이크]가 필요합니다.");
				return getHtm(player, "4301-noCake.htm");
			}
			else
			{
				takeItems(player, 57, 100000000);
				takeItems(player, CAKE, 1);
				giveItems(player, FANTASY_CAKE, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				return "4301-3.htm";
			}
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	private void checkValentine(Player player)
	{
		String topic = "오늘은 발렌타인 데이!";
		String body = "오늘도 어김없이 " + Config.SERVER_NAME_KOR + "에 오신것을 환영하며, 발렌타인 데이 기념선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
		String items = "20214,5;23627,10;22235,10;37705,3;37706,3;20191,1";
		int checkGift = player.getAccountVariables().getInt(EVENT_NAME, 0);
		
		if ((player.getLevel() >= 20) && (checkGift != 1))
		{
			BorinetUtil.getInstance().sendEventMail(player, topic, body, items, EVENT_NAME, false);
		}
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "발렌타인 데이 이벤트가 진행 중 입니다!"));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (isValentine())
		{
			if (event.getPlayer().isInCategory(CategoryType.FIRST_CLASS_GROUP) && (event.getPlayer().getLevel() >= 20))
			{
				ThreadPool.schedule(() -> checkValentine(event.getPlayer()), 1000);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (isValentine())
		{
			ThreadPool.schedule(() -> checkValentine(event.getPlayer()), 1000);
		}
	}
	
	private boolean isValentine()
	{
		return (BorinetTask.Month() == Calendar.FEBRUARY) && (BorinetTask.Days() == 14);
	}
	
	public static void main(String[] args)
	{
		new ValentineDay();
	}
}
