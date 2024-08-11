package org.l2jmobius.gameserver.model.item;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetTask;

public class LunaShopItemInfo
{
	public static String BuyItemList(Player player)
	{
		String List = "";
		if (player.getVariables().getBoolean("buyItem_1", false))
		{
			List = "여왕개미의 반지, ";
		}
		if (player.getVariables().getBoolean("buyItem_2", false))
		{
			List += "코어의 반지, ";
		}
		if (player.getVariables().getBoolean("buyItem_3", false))
		{
			List += "오르펜의 귀걸이, ";
		}
		if (player.getVariables().getBoolean("buyItem_4", false))
		{
			List += "자켄의 귀걸이, ";
		}
		if (player.getVariables().getBoolean("buyItem_5", false))
		{
			List += "바이움의 반지, ";
		}
		if (player.getVariables().getBoolean("buyItem_6", false))
		{
			List += "안타라스의 귀걸이, ";
		}
		if (player.getVariables().getBoolean("buyItem_7", false))
		{
			List += "바이움 탈리스만, ";
		}
		if (player.getVariables().getBoolean("buyItem_8", false))
		{
			List += "네불라 목걸이, ";
		}
		if (player.getVariables().getBoolean("buyItem_9", false))
		{
			List += "이그니스 목걸이, ";
		}
		if (player.getVariables().getBoolean("buyItem_10", false))
		{
			List += "프로첼라 목걸이, ";
		}
		if (player.getVariables().getBoolean("buyItem_11", false))
		{
			List += "페트람 목걸이, ";
		}
		if (player.getVariables().getBoolean("buyItem_12", false))
		{
			List += "네불라 아가시온, ";
		}
		if (player.getVariables().getBoolean("buyItem_13", false))
		{
			List += "이그니스 아가시온, ";
		}
		if (player.getVariables().getBoolean("buyItem_14", false))
		{
			List += "프로첼라 아가시온, ";
		}
		if (player.getVariables().getBoolean("buyItem_15", false))
		{
			List += "페트람 아가시온, ";
		}
		if (player.getVariables().getBoolean("buyItem_16", false))
		{
			List += "여왕개미 인형 1단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_17", false))
		{
			List += "여왕개미 인형 2단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_18", false))
		{
			List += "바이움 인형 1단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_19", false))
		{
			List += "바이움 인형 2단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_20", false))
		{
			List += "오르펜 인형 1단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_21", false))
		{
			List += "오르펜 인형 2단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_22", false))
		{
			List += "프린테사 인형 1단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_23", false))
		{
			List += "프린테사 인형 2단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_24", false))
		{
			List += "프린테사 망토, ";
		}
		if (player.getVariables().getBoolean("buyItem_25", false))
		{
			List += "할리샤의 투구, ";
		}
		if (player.getVariables().getBoolean("buyItem_26", false))
		{
			List += "레드 캐츠아이 5단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_27", false))
		{
			List += "블루 캐츠아이 5단계, ";
		}
		if (player.getVariables().getBoolean("buyItem_30", false))
		{
			List += "타천사의 반지, ";
		}
		if (player.getVariables().getInt("sudoItem", 0) > 0)
		{
			List += "수도원 유물 - " + sudoItem(player) + "개, ";
		}
		if (player.getVariables().getInt("fordenItem", 0) > 0)
		{
			List += "잊혀진 유물 - " + forgottenItem(player) + "개";
		}
		
		return List;
	}
	
	public static String BuyItemIds(Player player)
	{
		String ItemId = "";
		if (player.getVariables().getBoolean("buyItem_1", false))
		{
			ItemId = "49577,1;";
		}
		if (player.getVariables().getBoolean("buyItem_2", false))
		{
			ItemId += "49579,1;";
		}
		if (player.getVariables().getBoolean("buyItem_3", false))
		{
			ItemId += "49578,1;";
		}
		if (player.getVariables().getBoolean("buyItem_4", false))
		{
			ItemId += "90765,1;";
		}
		if (player.getVariables().getBoolean("buyItem_5", false))
		{
			ItemId += "49582,1;";
		}
		if (player.getVariables().getBoolean("buyItem_6", false))
		{
			ItemId += "91139,1;";
		}
		if (player.getVariables().getBoolean("buyItem_7", false))
		{
			ItemId += "49683,1;";
		}
		if (player.getVariables().getBoolean("buyItem_8", false))
		{
			ItemId += "91117,1;";
		}
		if (player.getVariables().getBoolean("buyItem_9", false))
		{
			ItemId += "91119,1;";
		}
		if (player.getVariables().getBoolean("buyItem_10", false))
		{
			ItemId += "91121,1;";
		}
		if (player.getVariables().getBoolean("buyItem_11", false))
		{
			ItemId += "91123,1;";
		}
		if (player.getVariables().getBoolean("buyItem_12", false))
		{
			ItemId += "91130,1;";
		}
		if (player.getVariables().getBoolean("buyItem_13", false))
		{
			ItemId += "91129,1;";
		}
		if (player.getVariables().getBoolean("buyItem_14", false))
		{
			ItemId += "91131,1;";
		}
		if (player.getVariables().getBoolean("buyItem_15", false))
		{
			ItemId += "91132,1;";
		}
		if (player.getVariables().getBoolean("buyItem_16", false))
		{
			ItemId += "91257,1;";
		}
		if (player.getVariables().getBoolean("buyItem_17", false))
		{
			ItemId += "91422,1;";
		}
		if (player.getVariables().getBoolean("buyItem_18", false))
		{
			ItemId += "91256,1;";
		}
		if (player.getVariables().getBoolean("buyItem_19", false))
		{
			ItemId += "91423,1;";
		}
		if (player.getVariables().getBoolean("buyItem_20", false))
		{
			ItemId += "91258,1;";
		}
		if (player.getVariables().getBoolean("buyItem_21", false))
		{
			ItemId += "91424,1;";
		}
		if (player.getVariables().getBoolean("buyItem_22", false))
		{
			ItemId += "91604,1;";
		}
		if (player.getVariables().getBoolean("buyItem_23", false))
		{
			ItemId += "91605,1;";
		}
		if (player.getVariables().getBoolean("buyItem_24", false))
		{
			ItemId += "21718,1;";
		}
		if (player.getVariables().getBoolean("buyItem_25", false))
		{
			ItemId += "21893,1;";
		}
		if (player.getVariables().getBoolean("buyItem_26", false))
		{
			ItemId += "47681,1;";
		}
		if (player.getVariables().getBoolean("buyItem_27", false))
		{
			ItemId += "47686,1;";
		}
		if (player.getVariables().getBoolean("buyItem_30", false))
		{
			ItemId += "48864,1;";
		}
		if (player.getVariables().getInt("sudoItem", 0) > 0)
		{
			ItemId += "41079," + sudoItem(player) + ";";
		}
		if (player.getVariables().getInt("fordenItem", 0) > 0)
		{
			ItemId += "41073," + forgottenItem(player) + ";";
		}
		
		return ItemId;
	}
	
	public static void removeLunaVariables(Player player)
	{
		player.getVariables().remove("buyLunaSendMail");
		player.getVariables().remove("buyLunaBuyer");
		player.getVariables().remove("buyLuna");
		player.getVariables().remove("buyLunaPrice");
		player.getVariables().remove("reciveLuna");
	}
	
	public static void removeItemVariables(Player player)
	{
		player.getVariables().remove("buyItemSendMail");
		player.getVariables().remove("buyItemBuyer");
		player.getVariables().remove("buyItemPrice");
		player.getVariables().remove("buyItemTime");
		player.getVariables().remove("buyItemTime");
		player.getVariables().remove("buyItem_1");
		player.getVariables().remove("buyItem_2");
		player.getVariables().remove("buyItem_3");
		player.getVariables().remove("buyItem_4");
		player.getVariables().remove("buyItem_5");
		player.getVariables().remove("buyItem_6");
		player.getVariables().remove("buyItem_7");
		player.getVariables().remove("buyItem_8");
		player.getVariables().remove("buyItem_9");
		player.getVariables().remove("buyItem_10");
		player.getVariables().remove("buyItem_11");
		player.getVariables().remove("buyItem_12");
		player.getVariables().remove("buyItem_13");
		player.getVariables().remove("buyItem_14");
		player.getVariables().remove("buyItem_15");
		player.getVariables().remove("buyItem_16");
		player.getVariables().remove("buyItem_17");
		player.getVariables().remove("buyItem_18");
		player.getVariables().remove("buyItem_19");
		player.getVariables().remove("buyItem_20");
		player.getVariables().remove("buyItem_21");
		player.getVariables().remove("buyItem_22");
		player.getVariables().remove("buyItem_23");
		player.getVariables().remove("buyItem_24");
		player.getVariables().remove("buyItem_25");
		player.getVariables().remove("buyItem_26");
		player.getVariables().remove("buyItem_27");
		player.getVariables().remove("buyItem_28");
		player.getVariables().remove("buyItem_29");
		player.getVariables().remove("buyItem_30");
		player.getVariables().remove("sudoItem");
		player.getVariables().remove("fordenItem");
		player.getVariables().remove("reciveItem");
	}
	
	public static boolean checkBuyItems(Player player)
	{
		if (player.getVariables().getBoolean("buyItem_1", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_2", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_3", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_4", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_5", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_6", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_7", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_8", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_9", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_10", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_11", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_12", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_13", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_14", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_15", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_16", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_17", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_18", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_19", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_20", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_21", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_22", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_23", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_24", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_25", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_26", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_27", false))
		{
			return true;
		}
		if (player.getVariables().getBoolean("buyItem_30", false))
		{
			return true;
		}
		if (player.getVariables().getInt("sudoItem", 0) > 0)
		{
			return true;
		}
		if (player.getVariables().getInt("fordenItem", 0) > 0)
		{
			return true;
		}
		
		return false;
	}
	
	public static String ItemPrice(Player player)
	{
		int TotalPrice = 0;
		
		if (player.getVariables().getBoolean("buyItem_1", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_2", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_3", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_4", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_5", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_6", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_7", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 7 : 8;
		}
		if (player.getVariables().getBoolean("buyItem_8", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_9", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_10", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_11", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_12", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_13", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_14", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_15", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_16", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_17", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_18", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_19", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_20", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_21", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_22", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 8 : 10;
		}
		if (player.getVariables().getBoolean("buyItem_23", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 12 : 15;
		}
		if (player.getVariables().getBoolean("buyItem_24", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 3 : 4;
		}
		if (player.getVariables().getBoolean("buyItem_25", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 4 : 5;
		}
		if (player.getVariables().getBoolean("buyItem_26", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 16 : 20;
		}
		if (player.getVariables().getBoolean("buyItem_27", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 16 : 20;
		}
		if (player.getVariables().getBoolean("buyItem_30", false))
		{
			TotalPrice += (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 12 : 15;
		}
		if (player.getVariables().getInt("sudoItem", 0) >= 1)
		{
			TotalPrice += player.getVariables().getInt("sudoItem", 0);
		}
		if (player.getVariables().getInt("fordenItem", 0) >= 1)
		{
			TotalPrice += player.getVariables().getInt("fordenItem", 0);
		}
		
		return TotalPrice + "만원";
	}
	
	public static int sudoItem(Player player)
	{
		int sudoItem = player.getVariables().getInt("sudoItem", 0);
		int DefaultCount = 30;
		double BonusCount = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 1.2 : 1;
		
		return (int) ((sudoItem * DefaultCount) * BonusCount);
	}
	
	public static int forgottenItem(Player player)
	{
		int forgottenItem = player.getVariables().getInt("fordenItem", 0);
		int DefaultCount = 100;
		double BonusCount = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 1.2 : 1;
		
		return (int) ((forgottenItem * DefaultCount) * BonusCount);
	}
}
