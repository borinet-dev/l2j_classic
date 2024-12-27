package custom.ChangeMaterial;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.SpecialEvents;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class ChangeMaterial extends AbstractNpcAI
{
	// NPC
	private static final int NPC = 34330;
	
	private ChangeMaterial()
	{
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (player == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "change_items":
			{
				htmltext = getHtm(player, "change_items.htm");
				htmltext = htmltext.replace("%evenNpcName%", npc.getName());
				htmltext = htmltext.replace("%eventName%", BorinetUtil.getInstance().getEventName());
				
				// @formatter:off
				// 플레이어 인벤토리에서 아이템 개수 가져오기
				StringBuilder itemsInfo = new StringBuilder();
				itemsInfo.append("<font color=B59A75>떡국떡:</font> <font color=LEVEL>").append(player.getInventory().getInventoryItemCount(41396, -1)).append("개</font> <font color=FF8000>|</font> ")
					.append("<font color=B59A75>소고기:</font> <font color=LEVEL>").append(player.getInventory().getInventoryItemCount(41397, -1)).append("개</font><br><br>")
					.append("<font color=B59A75>달걀:</font> <font color=LEVEL>").append(player.getInventory().getInventoryItemCount(41398, -1)).append("개</font> <font color=FF8000>|</font> ")
					.append("<font color=B59A75>대파:</font> <font color=LEVEL>").append(player.getInventory().getInventoryItemCount(41399, -1)).append("개</font> <font color=FF8000>|</font> ")
					.append("<font color=B59A75>오이:</font> <font color=LEVEL>").append(player.getInventory().getInventoryItemCount(41400, -1)).append("개</font>");
				// @formatter:on
				
				// HTML 파일에 재료 정보 삽입
				htmltext = htmltext.replace("%itemsInfo%", itemsInfo.toString());
				break;
			}
			case "exchange_41396":
			{
				exchangeSpecificItem(player, 41396, 2, new int[]
				{
					41397,
					41398,
					41399,
					41400
				});
				return onAdvEvent("change_items", npc, player);
			}
			case "exchange_41397":
			{
				exchangeSpecificItem(player, 41397, 2, new int[]
				{
					41396,
					41398,
					41399,
					41400
				});
				return onAdvEvent("change_items", npc, player);
			}
			case "exchange_41398":
			{
				exchangeSpecificItem(player, 41398, 2, new int[]
				{
					41396,
					41397,
					41399,
					41400
				});
				return onAdvEvent("change_items", npc, player);
			}
			case "exchange_41399":
			{
				exchangeSpecificItem(player, 41399, 2, new int[]
				{
					41396,
					41397,
					41398,
					41400
				});
				return onAdvEvent("change_items", npc, player);
			}
			case "exchange_41400":
			{
				exchangeSpecificItem(player, 41400, 2, new int[]
				{
					41396,
					41397,
					41398,
					41399
				});
				return onAdvEvent("change_items", npc, player);
			}
		}
		return htmltext;
	}
	
	private void exchangeSpecificItem(Player player, int sourceItemId, long requiredCount, int[] possibleItems)
	{
		long itemCount = player.getInventory().getInventoryItemCount(sourceItemId, -1);
		
		if (itemCount < requiredCount)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}
		
		// 랜덤으로 대상 아이템 선택
		int randomIndex = (int) (Math.random() * possibleItems.length);
		int targetItemId = possibleItems[randomIndex];
		
		// 아이템 제거 및 추가
		player.destroyItemByItemId("재료 교환 - 지급", sourceItemId, requiredCount, player, true);
		player.addItem("재료 교환 - 획득", targetItemId, 1, player, true);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (BorinetTask.SpecialEvent() && BorinetUtil.getInstance().getEventName().equals("설날"))
		{
			ThreadPool.schedule(() -> SpecialEvents.checkEventDay(event.getPlayer()), 1000);
		}
	}
	
	public static void main(String[] args)
	{
		new ChangeMaterial();
	}
}