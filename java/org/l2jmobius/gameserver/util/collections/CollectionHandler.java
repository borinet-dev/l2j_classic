package org.l2jmobius.gameserver.util.collections;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class CollectionHandler
{
	public static void showCollectionWindow(Player player, int startId, int endId, String pageNumber, String command)
	{
		CollectionManager.loadCollections();
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body><title>컬렉션</title>");
		// 이전/다음 버튼 추가
		sb.append("<table border=0 cellpadding=0 cellspacing=0 width=270>");
		sb.append("<tr><td align=\"left\" width=70>");
		if (command != "collection_one")
		{
			sb.append("<button value=\"이전\" action=\"bypass -h ").append(getPreviousCommand(command)).append("\" width=70 height=25></button>");
		}
		sb.append("</td>");
		sb.append("<td align=\"center\" width=130>");
		sb.append("<font color=\"LEVEL\">컬렉션 메뉴 ").append(pageNumber).append("</font>");
		sb.append("</td>");
		sb.append("<td align=\"right\" width=70>");
		if (command != "collection_five")
		{
			sb.append("<button value=\"다음\" action=\"bypass -h ").append(getNextCommand(command)).append("\" width=70 height=25></button>");
		}
		sb.append("</td></tr></table>");
		
		for (CollectionData collection : CollectionManager.getInstance().getCollections().values())
		{
			int collectionid = collection.getId();
			if ((collectionid >= startId) && (collectionid <= endId))
			{
				boolean collectionCompleted = CollectionDatabaseManager.loadCollectionCompletionFromDatabase(player.getAccountName(), collectionid);
				if (collectionCompleted)
				{
					sb.append("<table border=0><tr><td><Button width=220 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h collection_select " + collection.getId() + " " + command + "\">" + collection.getName() + "</Button></td>");
					// sb.append("<table border=0><tr><td><Button width=220 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h collection_select " + collection.getId() + "\">" + collection.getName() + "</Button></td>");
					sb.append("<td width=40><font color=\"LEVEL\">- 완료</font></td></tr></table>");
				}
				else
				{
					sb.append("<table border=0><tr><td><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h collection_select " + collection.getId() + " " + command + "\">" + collection.getName() + "</Button></td></tr></table>");
					// sb.append("<table border=0><tr><td><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h collection_select " + collection.getId() + "\">" + collection.getName() + "</Button></td></tr></table>");
				}
			}
		}
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public static void showCollectionDetails(Player player, int collectionId, String backCommand)
	{
		CollectionData collection = CollectionManager.getInstance().getCollection(collectionId);
		boolean collectionCompleted = CollectionDatabaseManager.loadCollectionCompletionFromDatabase(player.getAccountName(), collectionId);
		
		if (collection == null)
		{
			return;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body><title>컬렉션</title>");
		sb.append("<table border=0 cellpadding=0 cellspacing=0 width=294 height=358 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr><td align=center>");
		sb.append("<table border=0 width=285 height=50>");
		sb.append("<tr><td><br>");
		sb.append("<center><font name=\"hs15\" color=\"FF6600\">" + collection.getName() + "</font></center>");
		sb.append("<center><img src=\"L2UI.squaregray\" width=\"280\" height=\"1\"/></center>");
		sb.append("</td></tr>");
		sb.append("</table>");
		sb.append("<table border=0 width=280 height=150>");
		sb.append("<tr><td align=center>");
		
		int itemsPerRow = 5; // 한 행에 표시할 이미지의 수
		
		sb.append("<table border=0 width=280>");
		
		// 이미지와 강화수치를 함께 표시하기 위해 itemEntry 리스트를 가져온다
		List<CollectionData.ItemEntry> items = collection.getItems();
		int itemCount = items.size();
		
		for (int i = 0; i < itemCount; i++)
		{
			// 이미지 행 시작
			if ((i % itemsPerRow) == 0)
			{
				sb.append("<tr>");
			}
			
			sb.append("<td width=32 height=40 align=center><button width=32 height=32 itemtooltip=\"" + items.get(i).getItemId() + "\"></button></td>");
			
			// 아이템 이미지 행 끝
			if ((((i + 1) % itemsPerRow) == 0) || ((i + 1) == itemCount))
			{
				sb.append("</tr><tr>"); // 다음 행 시작
				for (int j = i - (i % itemsPerRow); j <= i; j++)
				{
					sb.append(String.format("<td width=32 height=20 align=center><font color=\"00FF00\">+%d</font></td>", items.get(j).getEnchantLevel()));
				}
				sb.append("</tr><tr>"); // 다음 행 시작
				for (int j = i - (i % itemsPerRow); j <= i; j++)
				{
					Item jInventoryItem = player.getInventory().getItemByItemId(items.get(j).getItemId());
					Item jWarehouseItem = player.getWarehouse().getItemByItemId(items.get(j).getItemId());
					boolean jInvenItemExists = (jInventoryItem != null) && (jInventoryItem.getEnchantLevel() == items.get(j).getEnchantLevel()) && (jInventoryItem.getItemLocation() == ItemLocation.INVENTORY);
					boolean jWareItemExists = (jWarehouseItem != null) && (jWarehouseItem.getEnchantLevel() == items.get(j).getEnchantLevel()) && (jWarehouseItem.getItemLocation() == ItemLocation.WAREHOUSE);
					sb.append(String.format("<td width=32 height=20 align=center>%s</td>", collectionCompleted ? "<font color=\"00FF00\">등록됨</font>" : ((jInvenItemExists || jWareItemExists) ? "<font color=\"FFFF00\">보유 중</font>" : "<font color=\"FF0000\">미보유</font>")));
				}
				sb.append("</tr>");
			}
		}
		sb.append("</table>");
		sb.append("</td></tr>");
		sb.append("</table>");
		sb.append("<table width=280>");
		sb.append("<tr>");
		sb.append("<td align=center valign=center>");
		sb.append("<img src=\"L2UI.squaregray\" width=\"285\" height=\"1\"/>");
		sb.append("</td></tr>");
		sb.append("</table>");
		sb.append("</td></tr>");
		
		// 보상 추가 옵션 표시
		String reward = collection.getReward();
		if ((reward != null) && !reward.isEmpty())
		{
			String[] rewardParts = reward.split(","); // 구분 기호(쉼표)를 기준으로 분할
			StringBuilder rewardBuilder = new StringBuilder();
			for (String rewardPart : rewardParts)
			{
				String[] rewardItem = rewardPart.trim().split("\\s+"); // 공백을 기준으로 옵션과 값을 분할
				if (rewardItem.length == 2)
				{
					String option = rewardItem[0];
					String value = rewardItem[1];
					switch (option)
					{
						case "pAttack":
							rewardBuilder.append("공격력 ").append(value).append(", ");
							break;
						case "pCAttack":
							rewardBuilder.append("물리 크리티컬 대미지 ").append(value).append("%, ");
							break;
						case "pDefense":
							rewardBuilder.append("방어력 ").append(value).append(", ");
							break;
						case "mAttack":
							rewardBuilder.append("마법력 ").append(value).append(", ");
							break;
						case "mDefense":
							rewardBuilder.append("마법저항 ").append(value).append(", ");
							break;
						case "pAccuracy":
							rewardBuilder.append("물리명중 ").append(value).append(", ");
							break;
						case "mAccuracy":
							rewardBuilder.append("마법명중 ").append(value).append(", ");
							break;
						case "pSkillCrtDmg":
							rewardBuilder.append("물리 스킬 크리티컬 대미지 ").append(value).append("%, ");
							break;
						case "mCrtDmg":
							rewardBuilder.append("마법 크리티컬 대미지 ").append(value).append("%, ");
							break;
						case "pCrtChance":
							rewardBuilder.append("물리 스킬 크리티컬 확률 ").append(value).append("%, ");
							break;
						case "mCrtChance":
							rewardBuilder.append("마법 크리티컬 확률 ").append(value).append("%, ");
							break;
						case "resParalyze":
							rewardBuilder.append("마비 저항 ").append(value).append("%, ");
							break;
						case "resTurnstone":
							rewardBuilder.append("석화 저항 ").append(value).append("%, ");
							break;
						case "resDerangement":
							rewardBuilder.append("정신 저항 ").append(value).append("%, ");
							break;
						case "resStun":
							rewardBuilder.append("스턴 저항 ").append(value).append("%, ");
							break;
						case "resHold":
							rewardBuilder.append("루트 저항 ").append(value).append("%, ");
							break;
						case "resSleep":
							rewardBuilder.append("슬립 저항 ").append(value).append("%, ");
							break;
						case "resBleed":
							rewardBuilder.append("출혈 저항 ").append(value).append("%, ");
							break;
						case "resPoison":
							rewardBuilder.append("중독 저항 ").append(value).append("%, ");
							break;
						case "resSilence":
							rewardBuilder.append("침묵 저항 ").append(value).append("%, ");
							break;
						case "bonusHP":
							rewardBuilder.append("최대 HP ").append(value).append(", ");
							break;
						case "bonusMP":
							rewardBuilder.append("최대 MP ").append(value).append(", ");
							break;
						case "bonusCP":
							rewardBuilder.append("최대 CP ").append(value).append(", ");
							break;
						case "bonusRusSpd":
							rewardBuilder.append("이동속도 ").append(value).append(", ");
							break;
						case "bonusPeVas":
							rewardBuilder.append("물리회피 ").append(value).append(", ");
							break;
						case "bonusMeVas":
							rewardBuilder.append("마법회피 ").append(value).append(", ");
							break;
						case "resFire":
							rewardBuilder.append("불 내성 ").append(value).append(", ");
							break;
						case "resWind":
							rewardBuilder.append("바람 내성 ").append(value).append(", ");
							break;
						case "resWater":
							rewardBuilder.append("물 내성 ").append(value).append(", ");
							break;
						case "resEarth":
							rewardBuilder.append("대지 내성 ").append(value).append(", ");
							break;
						case "resHoly":
							rewardBuilder.append("신성 내성 ").append(value).append(", ");
							break;
						case "resDark":
							rewardBuilder.append("암흑 내성 ").append(value).append(", ");
							break;
						case "firePower":
							rewardBuilder.append("불 속성 ").append(value).append(", ");
							break;
						case "windPower":
							rewardBuilder.append("바람 속성 ").append(value).append(", ");
							break;
						case "waterPower":
							rewardBuilder.append("물 속성 ").append(value).append(", ");
							break;
						case "earthPower":
							rewardBuilder.append("대지 속성 ").append(value).append(", ");
							break;
						case "holyPower":
							rewardBuilder.append("신성 속성 ").append(value).append(", ");
							break;
						case "darkPower":
							rewardBuilder.append("암흑 속성 ").append(value).append(", ");
							break;
						case "mpRegen":
							rewardBuilder.append("MP 재생 ").append(value).append(", ");
							break;
					}
				}
			}
			// 마지막 쉼표와 공백 제거
			if (rewardBuilder.length() > 2)
			{
				rewardBuilder.setLength(rewardBuilder.length() - 2);
			}
			sb.append("<tr><td align=center>");
			sb.append("<table border=0 width=280 height=50>");
			sb.append("<tr>");
			sb.append("<td align=center>");
			sb.append("<br><br><button value=\"컬렉션 효과\" height=15></button>");
			sb.append("</td></tr>");
			sb.append("<tr><td>");
			sb.append("<font color=\"ffffff\"><button value=\"" + rewardBuilder.toString() + "\" height=27></button></font><br>");
			sb.append("</td></tr></table>");
			sb.append("<tr><td align=center>");
		}
		
		// 필요 아이템이 모두 있는지 체크
		boolean hasAllItems = true; // 기본값을 true로 설정
		for (CollectionData.ItemEntry itemEntry : collection.getItems())
		{
			Item item = player.getInventory().getItemByItemId(itemEntry.getItemId());
			Item WarehouseItem = player.getWarehouse().getItemByItemId(itemEntry.getItemId());
			
			if ((item != null) && (item.getEnchantLevel() == itemEntry.getEnchantLevel()) && (item.getItemLocation() == ItemLocation.INVENTORY))
			{
				// 아이템이 인벤토리에 있고 강화 레벨이 맞으면 계속 진행
				continue;
			}
			else if ((WarehouseItem != null) && (WarehouseItem.getEnchantLevel() == itemEntry.getEnchantLevel()) && (WarehouseItem.getItemLocation() == ItemLocation.WAREHOUSE))
			{
				// 아이템이 창고에 있고 강화 레벨이 맞으면 계속 진행
				continue;
			}
			else
			{
				// 조건을 만족하지 않는 아이템이 있으면 false로 설정하고 반복문 종료
				hasAllItems = false;
				break;
			}
		}
		
		sb.append("<table width=280 height=80>");
		sb.append("<tr>");
		sb.append("<td align=center>");
		if (collectionCompleted)
		{
			sb.append("<font color=\"FF00FF\"><button value=\"컬렉션 등록 완료\" height=22></button></font>");
		}
		else if (hasAllItems)
		{
			sb.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h collection_collect " + collectionId + " " + backCommand + "\">적용하기</Button>");
		}
		else
		{
			sb.append("<button value=\" \" height=22></button>");
		}
		
		sb.append("<Button ALIGN=LEFT ICON=\"RETURN\" action=\"bypass -h ").append(backCommand).append("\">뒤로가기</Button>");
		sb.append("</td></tr></table>");
		sb.append("</td></tr></table>");
		sb.append("</body></html>");
		
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public static void collectItems(Player player, int collectionId)
	{
		CollectionData collection = CollectionManager.getInstance().getCollection(collectionId);
		
		if (collection == null)
		{
			return;
		}
		
		// 필요 아이템이 모두 있는지 체크
		boolean hasAllItems = true; // 기본값을 true로 설정
		for (CollectionData.ItemEntry itemEntry : collection.getItems())
		{
			Item item = player.getInventory().getItemByItemId(itemEntry.getItemId());
			Item WarehouseItem = player.getWarehouse().getItemByItemId(itemEntry.getItemId());
			
			if ((item != null) && (item.getEnchantLevel() == itemEntry.getEnchantLevel()) && (item.getItemLocation() == ItemLocation.INVENTORY))
			{
				// 아이템이 인벤토리에 있고 강화 레벨이 맞으면 계속 진행
				continue;
			}
			else if ((WarehouseItem != null) && (WarehouseItem.getEnchantLevel() == itemEntry.getEnchantLevel()) && (WarehouseItem.getItemLocation() == ItemLocation.WAREHOUSE))
			{
				// 아이템이 창고에 있고 강화 레벨이 맞으면 계속 진행
				continue;
			}
			else
			{
				// 조건을 만족하지 않는 아이템이 있으면 false로 설정하고 반복문 종료
				hasAllItems = false;
				break;
			}
		}
		
		if (hasAllItems)
		{
			for (CollectionData.ItemEntry itemEntry : collection.getItems())
			{
				// 먼저 인벤토리에서 필요한 아이템을 제거
				if (player.getInventory().getItemByItemId(itemEntry.getItemId()) != null)
				{
					player.destroyItemByItemId("컬렉션", itemEntry.getItemId(), 1, player, true);
				}
				// 인벤토리에 없을 경우 창고에서 아이템 제거
				else if (player.getWarehouse().getItemByItemId(itemEntry.getItemId()) != null)
				{
					player.destroyItemByItemIdInWareHouse("컬렉션", itemEntry.getItemId(), 1, player, true);
				}
			}
			
			List<String> rewards = new ArrayList<>();
			rewards.add(collection.getReward());
			applyRewards(player, rewards);
			
			player.sendMessage("컬렉션에 정상적으로 등록되었습니다.");
			CollectionDatabaseManager.saveCollectionCompletionToDatabase(player.getAccountName(), collectionId, true);
		}
		else
		{
			player.sendMessage("조건이 맞지않아 컬렉션에 등록할 수 없습니다.");
		}
	}
	
	public static void applyRewards(Player player, List<String> rewards)
	{
		for (String reward : rewards)
		{
			applyReward(player, reward);
		}
	}
	
	private static void applyReward(Player player, String reward)
	{
		String[] rewardParts = reward.split(",");
		for (String part : rewardParts)
		{
			String[] rewardItem = part.trim().split("\\+");
			if (rewardItem.length == 2)
			{
				String rewardType = rewardItem[0].trim();
				int rewardValue = Integer.parseInt(rewardItem[1].trim());
				
				switch (rewardType)
				{
					case "pAttack":
						player.addPAtk(rewardValue);
						break;
					case "pCAttack":
						player.addCPatk(rewardValue);
						break;
					case "pDefense":
						player.addPDef(rewardValue);
						break;
					case "mAttack":
						player.addMAtk(rewardValue);
						break;
					case "mDefense":
						player.addMDef(rewardValue);
						break;
					case "pAccuracy":
						player.addAccuracy(rewardValue);
						break;
					case "mAccuracy":
						player.addMAccuracy(rewardValue);
						break;
					case "pSkillCrtDmg":
						player.addPSkillCrtDmg(rewardValue);
						break;
					case "mCrtDmg":
						player.addMCrtDmg(rewardValue);
						break;
					case "pCrtChance":
						player.addPCrtChance(rewardValue);
						break;
					case "mCrtChance":
						player.addMCrtChance(rewardValue);
						break;
					case "resParalyze":
						player.addResistParalyze(rewardValue);
						break;
					case "resTurnstone":
						player.addResistTurnstone(rewardValue);
						break;
					case "resDerangement":
						player.addResistDerangement(rewardValue);
						break;
					case "resStun":
						player.addResistStun(rewardValue);
						break;
					case "resHold":
						player.addResistHold(rewardValue);
						break;
					case "resSleep":
						player.addResistSleep(rewardValue);
						break;
					case "resBleed":
						player.addResistBleed(rewardValue);
						break;
					case "resPoison":
						player.addResistPoison(rewardValue);
						break;
					case "resSilence":
						player.addResistSilence(rewardValue);
						break;
					case "bonusHP":
						player.addBonusHP(rewardValue);
						break;
					case "bonusMP":
						player.addBonusMP(rewardValue);
						break;
					case "bonusCP":
						player.addBonusCP(rewardValue);
						break;
					case "bonusRusSpd":
						player.addBonusRusSpd(rewardValue);
						break;
					case "bonusPeVas":
						player.addBonusPeVas(rewardValue);
						break;
					case "bonusMeVas":
						player.addBonusMeVas(rewardValue);
						break;
					case "resFire":
						player.addResistFire(rewardValue);
						break;
					case "resWind":
						player.addResistWind(rewardValue);
						break;
					case "resWater":
						player.addResistWater(rewardValue);
						break;
					case "resEarth":
						player.addResistEarth(rewardValue);
						break;
					case "resHoly":
						player.addResistHoly(rewardValue);
						break;
					case "resDark":
						player.addResistDark(rewardValue);
						break;
					case "firePower":
						player.addFirePower(rewardValue);
						break;
					case "windPower":
						player.addWindPower(rewardValue);
						break;
					case "waterPower":
						player.addWaterPower(rewardValue);
						break;
					case "earthPower":
						player.addEarthPower(rewardValue);
						break;
					case "holyPower":
						player.addHolyPower(rewardValue);
						break;
					case "darkPower":
						player.addDarkPower(rewardValue);
						break;
					case "mpRegen":
						player.addMpRegen(rewardValue);
						break;
					default:
						System.out.println("Unknown reward type: " + rewardType);
						break;
				}
			}
		}
		player.broadcastUserInfo();
	}
	
	private static String getPreviousCommand(String currentCommand)
	{
		switch (currentCommand)
		{
			case "collection_two":
				return "collection_one";
			case "collection_three":
				return "collection_two";
			case "collection_four":
				return "collection_three";
			case "collection_five":
				return "collection_four";
			case "collection_six":
				return "collection_five";
			default:
				return "collection_one";
		}
	}
	
	private static String getNextCommand(String currentCommand)
	{
		switch (currentCommand)
		{
			case "collection_one":
				return "collection_two";
			case "collection_two":
				return "collection_three";
			case "collection_three":
				return "collection_four";
			case "collection_four":
				return "collection_five";
			case "collection_five":
				return "collection_six";
			default:
				return "collection_six";
		}
	}
}