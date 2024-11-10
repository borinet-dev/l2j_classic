package ai.others.Raphy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
 * 도박 매니저 라피 NPC AI 업데이트 - 모든 등급 및 축복받은 강화 주문서를 구분하여 처리, 모든 메서드 통합
 */
public class Raphy extends AbstractNpcAI
{
	private static final int RAPHY = 31758;
	
	private final Map<Player, GambleResult> playerResults = new ConcurrentHashMap<>();
	
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
			// 무기와 방어구에 따른 도박을 각각 처리
			case "GAMBLE_START_ALL_D_WEAPON":
			case "GAMBLE_START_ALL_D_ARMOR":
			case "GAMBLE_START_ALL_C_WEAPON":
			case "GAMBLE_START_ALL_C_ARMOR":
			case "GAMBLE_START_ALL_B_WEAPON":
			case "GAMBLE_START_ALL_B_ARMOR":
			case "GAMBLE_START_ALL_A_WEAPON":
			case "GAMBLE_START_ALL_A_ARMOR":
			case "GAMBLE_START_ALL_S_WEAPON":
			case "GAMBLE_START_ALL_S_ARMOR":
			case "GAMBLE_START_ALL_BW_TO_D":
			case "GAMBLE_START_ALL_BW_TO_C":
			case "GAMBLE_START_ALL_BW_TO_B":
			case "GAMBLE_START_ALL_BW_TO_A":
			case "GAMBLE_START_ALL_BW_TO_S":
			case "GAMBLE_START_ALL_BA_TO_D":
			case "GAMBLE_START_ALL_BA_TO_C":
			case "GAMBLE_START_ALL_BA_TO_B":
			case "GAMBLE_START_ALL_BA_TO_A":
			case "GAMBLE_START_ALL_BA_TO_S":
				checkMulti(player, event, npc, true);
				break;
			case "GAMBLE_COUNT1_ALL":
				showCount(player, 2);
				startQuestTimer("GAMBLE_COUNT2_ALL", 1000, npc, player);
				break;
			case "GAMBLE_COUNT2_ALL":
				showCount(player, 3);
				startQuestTimer("GAMBLE_RESULT_ALL", 1000, npc, player);
				break;
			case "GAMBLE_RESULT_ALL":
				String val = player.getVariables().getString("GAMBLE");
				handleGambling(player, val, npc, true);
				break;
			
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
				setResult(player, false);
				break;
			default:
				handleGambling(player, event, npc, false);
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	private void handleGambling(Player player, String event, Npc npc, boolean multi)
	{
		int[] count = checkMulti(player, event, npc, false);
		int itemId = count[0];
		int requiredAdena = count[1];
		int gameCount = count[2];
		
		if (multi)
		{
			player.destroyItemByItemId("Gamble", itemId, 2 * gameCount, npc, true);
			player.reduceAdena("Gamble", requiredAdena * gameCount, npc, true);
			
			for (int i = 0; i < gameCount; i++)
			{
				setResult(player, true);
			}
			
			showResultMessage(player);
		}
		else
		{
			if (!checkAndHandleRequirements(player, itemId, 2, requiredAdena))
			{
				return;
			}
			
			player.getVariables().set("GAMBLE", event);
			startQuestTimer("GAMBLE_START", 0, npc, player);
		}
	}
	
	private int[] checkMulti(Player player, String event, Npc npc, boolean first)
	{
		int itemId = 0;
		int itemCount = 2;
		int requiredAdena = 0;
		int gameCount = 0;
		
		switch (event)
		{
			// 일반 강화 주문서
			case "WtoC":
			case "GAMBLE_START_ALL_D_WEAPON":
				itemId = 955; // D급 무기 주문서
				requiredAdena = 100000; // 아데나 요구량 고정
				break;
			case "WtoB":
			case "GAMBLE_START_ALL_C_WEAPON":
				itemId = 951; // C급 무기 주문서
				requiredAdena = 300000; // 아데나 요구량 고정
				break;
			case "WtoA":
			case "GAMBLE_START_ALL_B_WEAPON":
				itemId = 947; // B급 무기 주문서
				requiredAdena = 500000; // 아데나 요구량 고정
				break;
			case "WtoS":
			case "GAMBLE_START_ALL_A_WEAPON":
				itemId = 729; // A급 무기 주문서
				requiredAdena = 1000000; // 아데나 요구량 고정
				break;
			case "WtoR":
			case "GAMBLE_START_ALL_S_WEAPON":
				itemId = 959; // S급 무기 주문서
				requiredAdena = 3000000; // 아데나 요구량 고정
				break;
			case "AtoC":
			case "GAMBLE_START_ALL_D_ARMOR":
				itemId = 956; // D급 방어구 주문서
				requiredAdena = 100000; // 아데나 요구량 고정
				break;
			case "AtoB":
			case "GAMBLE_START_ALL_C_ARMOR":
				itemId = 952; // C급 방어구 주문서
				requiredAdena = 300000; // 아데나 요구량 고정
				break;
			case "AtoA":
			case "GAMBLE_START_ALL_B_ARMOR":
				itemId = 948; // B급 방어구 주문서
				requiredAdena = 500000; // 아데나 요구량 고정
				break;
			case "AtoS":
			case "GAMBLE_START_ALL_A_ARMOR":
				itemId = 730; // A급 방어구 주문서
				requiredAdena = 1000000; // 아데나 요구량 고정
				break;
			case "AtoR":
			case "GAMBLE_START_ALL_S_ARMOR":
				itemId = 960; // S급 방어구 주문서
				requiredAdena = 3000000; // 아데나 요구량 고정
				break;
			
			// 축복받은 강화 주문서
			case "BWtoC":
			case "GAMBLE_START_ALL_BW_TO_D":
				itemId = 6575; // 축복받은 D급 무기/방어구 주문서
				requiredAdena = 500000; // 아데나 요구량 고정
				break;
			case "BWtoB":
			case "GAMBLE_START_ALL_BW_TO_C":
				itemId = 6573; // 축복받은 C급 무기/방어구 주문서
				requiredAdena = 1000000; // 아데나 요구량 고정
				break;
			case "BWtoA":
			case "GAMBLE_START_ALL_BW_TO_B":
				itemId = 6571; // 축복받은 B급 무기/방어구 주문서
				requiredAdena = 2000000; // 아데나 요구량 고정
				break;
			case "BWtoS":
			case "GAMBLE_START_ALL_BW_TO_A":
				itemId = 6569; // 축복받은 A급 무기/방어구 주문서
				requiredAdena = 4000000; // 아데나 요구량 고정
				break;
			case "BWtoR":
			case "GAMBLE_START_ALL_BW_TO_S":
				itemId = 6577; // 축복받은 S급 무기/방어구 주문서
				requiredAdena = 6000000; // 아데나 요구량 고정
				break;
			case "BAtoC":
			case "GAMBLE_START_ALL_BA_TO_D":
				itemId = 6576; // 축복받은 D급 방어구 주문서
				requiredAdena = 200000; // 아데나 요구량 고정
				break;
			case "BAtoB":
			case "GAMBLE_START_ALL_BA_TO_C":
				itemId = 6574; // 축복받은 C급 방어구 주문서
				requiredAdena = 500000; // 아데나 요구량 고정
				break;
			case "BAtoA":
			case "GAMBLE_START_ALL_BA_TO_B":
				itemId = 6572; // 축복받은 B급 방어구 주문서
				requiredAdena = 1000000; // 아데나 요구량 고정
				break;
			case "BAtoS":
			case "GAMBLE_START_ALL_BA_TO_A":
				itemId = 6570; // 축복받은 A급 방어구 주문서
				requiredAdena = 2000000; // 아데나 요구량 고정
				break;
			case "BAtoR":
			case "GAMBLE_START_ALL_BA_TO_S":
				itemId = 6578; // 축복받은 S급 방어구 주문서
				requiredAdena = 4000000; // 아데나 요구량 고정
				break;
			default:
				break;
		}
		
		long scrollCount = player.getInventory().getInventoryItemCount(itemId, 0) / itemCount;
		long maxPossibleGames = player.getAdena() / requiredAdena;
		gameCount = (int) Math.min(scrollCount, maxPossibleGames);
		
		if (first)
		{
			if (scrollCount == 0)
			{
				player.sendMessage("필요한 주문서가 부족하다냥!");
				return null;
			}
			
			if (gameCount == 0)
			{
				player.sendMessage("아데나가 부족하다냥!");
				return null;
			}
			
			player.getVariables().set("GAMBLE", event);
			showCount(player, 1);
			startQuestTimer("GAMBLE_COUNT1_ALL", 1000, npc, player);
		}
		
		return new int[]
		{
			itemId,
			requiredAdena,
			gameCount
		};
	}
	
	private boolean checkAndHandleRequirements(Player player, int itemId, int itemCount, int requiredAdena)
	{
		if (player.getInventory().getInventoryItemCount(itemId, 0) < itemCount)
		{
			player.sendMessage("필요한 주문서가 부족하다냥!");
			return false;
		}
		
		if (player.getAdena() < requiredAdena)
		{
			player.sendMessage("아데나가 부족하다냥!");
			return false;
		}
		
		player.destroyItemByItemId("Gamble", itemId, itemCount, null, true);
		player.reduceAdena("Gamble", requiredAdena, null, true);
		return true;
	}
	
	private void setResult(Player player, boolean all)
	{
		GambleResult result = playerResults.getOrDefault(player, new GambleResult());
		String val = player.getVariables().getString("GAMBLE");
		
		boolean successRate = Rnd.get(1, 100) < (player.isPremium() ? 45 : 30);
		boolean premiumChance = Rnd.get(1, 100) < (player.isPremium() ? 40 : 20);
		
		int successItem = 0;
		boolean isMission = true;
		
		switch (val)
		{
			case "WtoC":
			case "GAMBLE_START_ALL_D_WEAPON":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 6573 : 951;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "WtoB":
			case "GAMBLE_START_ALL_C_WEAPON":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 6571 : 947;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "WtoA":
			case "GAMBLE_START_ALL_B_WEAPON":
				if (successRate)
				{
					successItem = premiumChance ? 6569 : 729;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "WtoS":
			case "GAMBLE_START_ALL_A_WEAPON":
				if (successRate)
				{
					successItem = premiumChance ? 6577 : 959;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "WtoR":
			case "GAMBLE_START_ALL_S_WEAPON":
				if (successRate)
				{
					successItem = premiumChance ? 19447 : 17526;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "AtoC":
			case "GAMBLE_START_ALL_D_ARMOR":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 6574 : 952;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "AtoB":
			case "GAMBLE_START_ALL_C_ARMOR":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 6572 : 948;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "AtoA":
			case "GAMBLE_START_ALL_B_ARMOR":
				if (successRate)
				{
					successItem = premiumChance ? 6570 : 730;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "AtoS":
			case "GAMBLE_START_ALL_A_ARMOR":
				if (successRate)
				{
					successItem = premiumChance ? 6578 : 960;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "AtoR":
			case "GAMBLE_START_ALL_S_ARMOR":
				if (successRate)
				{
					successItem = premiumChance ? 19448 : 17527;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
				}
				break;
			case "BWtoC":
			case "GAMBLE_START_ALL_BW_TO_D":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 22227 : 6573;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BWtoB":
			case "GAMBLE_START_ALL_BW_TO_C":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 22225 : 6571;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BWtoA":
			case "GAMBLE_START_ALL_BW_TO_B":
				if (successRate)
				{
					successItem = premiumChance ? 22223 : 6569;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BWtoS":
			case "GAMBLE_START_ALL_BW_TO_A":
				if (successRate)
				{
					successItem = premiumChance ? 22221 : 6577;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BWtoR":
			case "GAMBLE_START_ALL_BW_TO_S":
				if (successRate)
				{
					successItem = premiumChance ? 33478 : 19447;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BAtoC":
			case "GAMBLE_START_ALL_BA_TO_D":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 22228 : 6574;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BAtoB":
			case "GAMBLE_START_ALL_BA_TO_C":
				isMission = false;
				if (successRate)
				{
					successItem = premiumChance ? 22226 : 6572;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BAtoA":
			case "GAMBLE_START_ALL_BA_TO_B":
				if (successRate)
				{
					premiumChance = Rnd.get(1, 100) < (player.isPremium() ? 25 : 20);
					successItem = premiumChance ? 22224 : 6570;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BAtoS":
			case "GAMBLE_START_ALL_BA_TO_A":
				if (successRate)
				{
					premiumChance = Rnd.get(1, 100) < (player.isPremium() ? 18 : 10);
					successItem = premiumChance ? 22222 : 6578;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			case "BAtoR":
			case "GAMBLE_START_ALL_BA_TO_S":
				if (successRate)
				{
					premiumChance = Rnd.get(1, 100) < (player.isPremium() ? 15 : 7);
					successItem = premiumChance ? 33479 : 19448;
					if (premiumChance)
					{
						result.blessingCount.incrementAndGet();
					}
					result.isBlessing = true;
				}
				break;
			default:
				break;
		}
		if (all)
		{
			if (successItem > 0)
			{
				updatePlayerResult(player, true);
				player.addItem("도박_라피", successItem, 1, player, true);
				if (isMission)
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerGamble(player, true));
				}
				
			}
			else
			{
				updatePlayerResult(player, false);
			}
		}
		else
		{
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
		String html = HtmCache.getInstance().getHtm(null, htmlPath).replace("<?photo?>", "<img src=\"borinet.gamble_num" + num + "\" width=256 height=150>");
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	private void showResultMessage(Player player)
	{
		GambleResult result = playerResults.getOrDefault(player, new GambleResult());
		NpcHtmlMessage html = new NpcHtmlMessage(RAPHY);
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>도박 매니저 라피:<br>");
		sb.append("주문서 도박 결과<br>");
		sb.append("총 시도 횟수: ").append(result.successCount.get() + result.failureCount.get()).append(" 회<br>");
		sb.append("<font color=BEF781>성공: ").append(result.successCount.get()).append(" 회</font><br1>");
		sb.append("<font color=DF0101>실패: ").append(result.failureCount.get()).append(" 회</font><br><br>");
		if (result.isBlessing)
		{
			sb.append("<font color=LEVEL>파멸 주문서: ").append(result.blessingCount.get()).append("장 획득</font></body></html>");
		}
		else
		{
			sb.append("<font color=LEVEL>축복 주문서: ").append(result.blessingCount.get()).append("장 획득</font></body></html>");
		}
		html.setHtml(sb.toString());
		player.sendPacket(html);
		resetCounts(player);
	}
	
	private synchronized void resetCounts(Player player)
	{
		GambleResult result = playerResults.get(player);
		if (result != null)
		{
			result.reset();
		}
	}
	
	private void updatePlayerResult(Player player, boolean success)
	{
		playerResults.computeIfAbsent(player, k -> new GambleResult()).update(success);
	}
	
	private static class GambleResult
	{
		private final AtomicInteger successCount = new AtomicInteger(0);
		private final AtomicInteger failureCount = new AtomicInteger(0);
		private final AtomicInteger blessingCount = new AtomicInteger(0);
		private volatile boolean isBlessing = false;
		
		public void update(boolean success)
		{
			if (success)
			{
				successCount.incrementAndGet();
			}
			else
			{
				failureCount.incrementAndGet();
			}
		}
		
		public void reset()
		{
			successCount.set(0);
			failureCount.set(0);
			blessingCount.set(0);
			isBlessing = false;
		}
	}
	
	public static void main(String[] args)
	{
		new Raphy();
	}
}
