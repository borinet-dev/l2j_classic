package events.Hangul;

import java.time.LocalDateTime;
import java.time.Month;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

public class Hangul extends LongTimeEvent
{
	private int WordCount = 0;
	private static final int NPC = 33829;
	private static final int 한 = 41387;
	private static final int 글 = 41388;
	private static final int 사 = 41389;
	private static final int 랑 = 41390;
	
	public Hangul()
	{
		if (isHangulEventActive())
		{
			addStartNpc(NPC);
			addFirstTalkId(NPC);
			addTalkId(NPC);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		int itemCount = (int) getQuestItemsCount(player, 41386);
		
		switch (event)
		{
			case "change_bc":
				return processItemExchange(player, itemCount, 200, 41365);
			case "change_wu":
				return processItemExchange(player, itemCount, 300, 41233);
			case "change_au":
				return processItemExchange(player, itemCount, 100, 41234);
			case "한글사랑":
				if (checkLetters(player))
				{
					WordCount++;
					takeItems(player, 한, 1);
					takeItems(player, 글, 1);
					takeItems(player, 사, 1);
					takeItems(player, 랑, 1);
					getReward(player);
					startQuestTimer("changeAgain", 0, null, player);
				}
				else if (WordCount > 1)
				{
					player.sendMessage("총 " + WordCount + "번 교환하였습니다!");
					WordCount = 0;
				}
				else
				{
					player.sendMessage("한글사랑 문자가 부족합니다.");
				}
				return null;
			case "changeAgain":
				startQuestTimer("한글사랑", 0, null, player);
				return null;
			default:
				return event;
		}
	}
	
	private String processItemExchange(Player player, int itemCount, int requiredCount, int rewardItemId)
	{
		if (itemCount < requiredCount)
		{
			player.sendMessage("훈민정음 사본이 " + requiredCount + "개가 필요합니다.");
			return null;
		}
		takeItems(player, 41386, requiredCount);
		giveItems(player, rewardItemId, 1);
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	private boolean isHangulEventActive()
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = LocalDateTime.of(now.getYear(), Month.OCTOBER, 8, 7, 0); // 10월 8일 오전 7시
		LocalDateTime end = LocalDateTime.of(now.getYear(), Month.OCTOBER, 10, 20, 0); // 10월 10일 오후 8시
		
		return now.isAfter(start) && now.isBefore(end);
	}
	
	private boolean checkLetters(Player player)
	{
		if ((getQuestItemsCount(player, 한) >= 1) && //
			(getQuestItemsCount(player, 글) >= 1) && //
			(getQuestItemsCount(player, 사) >= 1) && //
			(getQuestItemsCount(player, 랑) >= 1))
		{
			return true;
		}
		return false;
	}
	
	private void getReward(Player player)
	{
		int randomValue = Rnd.get(100);
		int rewardId = 0;
		int rewardCount = 0;
		
		if (randomValue < 19)
		{
			rewardId = 22223; // 파멸의 무기 강화 주문서 - A그레이드 (19%)
			rewardCount = 1;
		}
		else if (randomValue < 34) // 19 + 15 = 34
		{
			rewardId = 22224; // 파멸의 갑옷 강화 주문서 - A그레이드 (15%)
			rewardCount = 1;
		}
		else if (randomValue < 50) // 34 + 16 = 50
		{
			rewardId = 6577; // 축복받은 무기 강화 주문서 - S그레이드 (16%)
			rewardCount = 1;
		}
		else if (randomValue < 65) // 50 + 15 = 65
		{
			rewardId = 6578; // 축복받은 갑옷 강화 주문서 - S그레이드 (15%)
			rewardCount = 1;
		}
		else if (randomValue < 76) // 65 + 11 = 76
		{
			rewardId = 19447; // 축복받은 무기 강화 주문서 - R그레이드 (11%)
			rewardCount = 1;
		}
		else if (randomValue < 86) // 76 + 10 = 86
		{
			rewardId = 19448; // 축복받은 갑옷 강화 주문서 - R그레이드 (10%)
			rewardCount = 1;
		}
		else if (randomValue < 93) // 86 + 7 = 93
		{
			rewardId = 41031; // 레어 액세서리 강화 주문서 (7%)
			rewardCount = 1;
		}
		else if (randomValue < 98) // 93 + 5 = 98
		{
			rewardId = 41032; // 축복받은 레어 액세서리 강화 주문서 (5%)
			rewardCount = 1;
		}
		else if (randomValue < 99) // 98 + 1 = 99
		{
			rewardId = 41233; // 무기 강화석 (1%)
			rewardCount = 1;
		}
		else // randomValue == 99
		{
			rewardId = 41234; // 방어구 강화석 (1%)
			rewardCount = 1;
		}
		
		giveItems(player, rewardId, rewardCount);
	}
	
	public static void main(String[] args)
	{
		new Hangul();
	}
}
