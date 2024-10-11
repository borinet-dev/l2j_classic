package ai.others.Raphy;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerGamble;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.KorNameUtil;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class Raphy extends AbstractNpcAI
{
	private static final int RAPHY = 31758;
	
	private Raphy()
	{
		addStartNpc(RAPHY);
		addTalkId(RAPHY);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "GAMBLE_START":
				showCount(player, 1);
				startQuestTimer("GAMBLE_COUNT1", 1000, npc, player);
				break;
			case "GAMBLE_COUNT1":
				showCount(player, 2);
				startQuestTimer("GAMBLE_COUNT2", 1000, npc, player);
				break;
			case "GAMBLE_COUNT2":
				showCount(player, 3);
				startQuestTimer("GAMBLE_RESULT", 1000, npc, player);
				break;
			case "GAMBLE_RESULT":
				setResult(player);
				break;
			default:
				handleGambling(player, event, npc);
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	private void handleGambling(Player player, String event, Npc npc)
	{
		int itemId = 0;
		int itemCount = 0;
		int requiredAdena = 0;
		
		// 각 이벤트에 따라 아이템 ID, 개수, 아데나 요구량 설정
		switch (event)
		{
			case "WtoC":
				itemId = 955;
				itemCount = 2;
				requiredAdena = 100000;
				break;
			case "WtoB":
				itemId = 951;
				itemCount = 2;
				requiredAdena = 300000;
				break;
			case "WtoA":
				itemId = 947;
				itemCount = 2;
				requiredAdena = 500000;
				break;
			case "WtoS":
				itemId = 729;
				itemCount = 2;
				requiredAdena = 800000;
				break;
			case "WtoR":
				itemId = 959;
				itemCount = 2;
				requiredAdena = 1500000;
				break;
			case "AtoC":
				itemId = 956;
				itemCount = 2;
				requiredAdena = 50000;
				break;
			case "AtoB":
				itemId = 952;
				itemCount = 2;
				requiredAdena = 150000;
				break;
			case "AtoA":
				itemId = 948;
				itemCount = 2;
				requiredAdena = 250000;
				break;
			case "AtoS":
				itemId = 730;
				itemCount = 2;
				requiredAdena = 400000;
				break;
			case "AtoR":
				itemId = 960;
				itemCount = 2;
				requiredAdena = 800000;
				break;
			case "BWtoC":
				itemId = 6575;
				itemCount = 2;
				requiredAdena = 300000;
				break;
			case "BWtoB":
				itemId = 6573;
				itemCount = 2;
				requiredAdena = 900000;
				break;
			case "BWtoA":
				itemId = 6571;
				itemCount = 2;
				requiredAdena = 1500000;
				break;
			case "BWtoS":
				itemId = 6569;
				itemCount = 2;
				requiredAdena = 2400000;
				break;
			case "BWtoR":
				itemId = 6577;
				itemCount = 2;
				requiredAdena = 5500000;
				break;
			case "BAtoC":
				itemId = 6576;
				itemCount = 2;
				requiredAdena = 150000;
				break;
			case "BAtoB":
				itemId = 6574;
				itemCount = 2;
				requiredAdena = 450000;
				break;
			case "BAtoA":
				itemId = 6572;
				itemCount = 2;
				requiredAdena = 750000;
				break;
			case "BAtoS":
				itemId = 6570;
				itemCount = 2;
				requiredAdena = 1200000;
				break;
			case "BAtoR":
				itemId = 6578;
				itemCount = 2;
				requiredAdena = 3000000;
				break;
			default:
				break;
		}
		
		if (!checkAndHandleRequirements(player, itemId, itemCount, requiredAdena))
		{
			player.sendMessage("도박을 시작할 수 없습니다.");
			return;
		}
		
		player.getVariables().set("GAMBLE", event);
		startQuestTimer("GAMBLE_START", 0, npc, player);
	}
	
	private boolean checkAndHandleRequirements(Player player, int itemId, int itemCount, int requiredAdena)
	{
		// 필요한 아이템과 아데나가 충분한지 확인하고 부족하면 처리
		if ((player.getInventory().getInventoryItemCount(itemId, 0) < itemCount) || (player.getInventory().getInventoryItemCount(57, 0) < requiredAdena))
		{
			player.sendMessage("도박에 필요한 아이템 또는 아데나가 부족합니다.");
			return false;
		}
		
		player.destroyItemByItemId("도박_라피", itemId, itemCount, player, true);
		player.destroyItemByItemId("도박_라피", 57, requiredAdena, player, true);
		return true;
	}
	
	private void setResult(Player player)
	{
		String val = player.getVariables().getString("GAMBLE");
		int chanceRate = Rnd.get(100);
		boolean successRate = chanceRate < (player.isPremium() ? 25 : 20);
		
		int successItem = 0;
		boolean premiumChance = false;
		boolean isMission = true;
		
		switch (val)
		{
			case "WtoC":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 65 : 60);
				successItem = premiumChance ? (successRate ? 6573 : 951) : 0;
				break;
			case "WtoB":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 45 : 40);
				successItem = premiumChance ? (successRate ? 6571 : 947) : 0;
				break;
			case "WtoA":
				premiumChance = chanceRate < (player.isPremium() ? 35 : 30);
				successItem = premiumChance ? (successRate ? 6569 : 729) : 0;
				break;
			case "WtoS":
				premiumChance = chanceRate < (player.isPremium() ? 25 : 20);
				successItem = premiumChance ? (successRate ? 6577 : 959) : 0;
				break;
			case "WtoR":
				premiumChance = chanceRate < (player.isPremium() ? 20 : 15);
				successItem = premiumChance ? (successRate ? 19447 : 17526) : 0;
				break;
			case "AtoC":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 65 : 60);
				successItem = premiumChance ? (successRate ? 6574 : 952) : 0;
				break;
			case "AtoB":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 45 : 40);
				successItem = premiumChance ? (successRate ? 6572 : 948) : 0;
				break;
			case "AtoA":
				premiumChance = chanceRate < (player.isPremium() ? 35 : 30);
				successItem = premiumChance ? (successRate ? 6570 : 730) : 0;
				break;
			case "AtoS":
				premiumChance = chanceRate < (player.isPremium() ? 25 : 20);
				successItem = premiumChance ? (successRate ? 6578 : 960) : 0;
				break;
			case "AtoR":
				premiumChance = chanceRate < (player.isPremium() ? 20 : 15);
				successItem = premiumChance ? (successRate ? 19448 : 17527) : 0;
				break;
			case "BWtoC":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 45 : 40);
				successItem = premiumChance ? (successRate ? 22227 : 6573) : 0;
				break;
			case "BWtoB":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 35 : 30);
				successItem = premiumChance ? (successRate ? 22225 : 6571) : 0;
				break;
			case "BWtoA":
				premiumChance = chanceRate < (player.isPremium() ? 25 : 20);
				successItem = premiumChance ? (successRate ? 22223 : 6569) : 0;
				break;
			case "BWtoS":
				premiumChance = chanceRate < (player.isPremium() ? 15 : 10);
				successItem = premiumChance ? (successRate ? 22221 : 6577) : 0;
				break;
			case "BWtoR":
				premiumChance = chanceRate < (player.isPremium() ? 15 : 10);
				successItem = premiumChance ? (successRate ? 33478 : 19447) : 0;
				break;
			case "BAtoC":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 45 : 40);
				successItem = premiumChance ? (successRate ? 22228 : 6574) : 0;
				break;
			case "BAtoB":
				isMission = false;
				premiumChance = chanceRate < (player.isPremium() ? 35 : 30);
				successItem = premiumChance ? (successRate ? 22226 : 6572) : 0;
				break;
			case "BAtoA":
				premiumChance = chanceRate < (player.isPremium() ? 25 : 20);
				successItem = premiumChance ? (successRate ? 22224 : 6570) : 0;
				break;
			case "BAtoS":
				premiumChance = chanceRate < (player.isPremium() ? 15 : 10);
				successItem = premiumChance ? (successRate ? 22222 : 6578) : 0;
				break;
			case "BAtoR":
				premiumChance = chanceRate < (player.isPremium() ? 15 : 10);
				successItem = premiumChance ? (successRate ? 33479 : 19448) : 0;
				break;
			// 추가 case에 대한 처리
			default:
				// 다른 모든 케이스에 대한 처리
				break;
		}
		
		if (successItem > 0)
		{
			showScreen(player, successItem, successRate, isMission);
		}
		else
		{
			Broadcast.toPlayerScreenMessage(player, "도박에 실패 하였습니다.");
			showHtml(player, false);
		}
	}
	
	private void showScreen(Player player, int itemId, boolean isBless, boolean isMission)
	{
		String itemName = ItemNameTable.getInstance().getItemNameKor(itemId);
		String message = "도박에 성공하여 [" + KorNameUtil.getName(itemName, "]을", "]를") + " 획득했습니다.";
		
		if (isBless)
		{
			if (isMission)
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerGamble(player, true));
			}
			Broadcast.toPlayerScreenMessage(player, message);
		}
		else
		{
			Broadcast.toPlayerScreenMessage(player, "도박에 성공 하였습니다.");
		}
		player.addItem("도박_라피", itemId, 1, player, true);
		player.sendMessage("도박에 성공 하였습니다.");
		showHtml(player, true);
	}
	
	private void showHtml(Player player, boolean success)
	{
		String htmlPath = success ? "data/html/default/31758_success.htm" : "data/html/default/31758_fail.htm";
		String html = HtmCache.getInstance().getHtm(null, htmlPath);
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	private void showCount(Player player, int count)
	{
		int num = Rnd.get(1, 10);
		String htmlPath = "data/html/default/31758_" + (4 - count) + ".htm";
		String html = HtmCache.getInstance().getHtm(null, htmlPath);
		html = html.replace("<?photo?>", "<img src=\"borinet.gamble_num" + num + "\" width=256 height=150>");
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	public static void main(String[] args)
	{
		new Raphy();
	}
}