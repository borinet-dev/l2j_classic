package handlers.communityboard.borinet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.handler.IWriteBoardHandler;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.instancemanager.PremiumManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.CustomStats;
import org.l2jmobius.gameserver.model.item.LunaShopItemInfo;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.BuyList;
import org.l2jmobius.gameserver.network.serverpackets.ExBuySellList;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.network.serverpackets.SiegeInfo;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.SendEmailDonate;
import org.l2jmobius.gameserver.util.Util;

import handlers.voicedcommandhandlers.LunaDelivery;

public class LunaShop implements IWriteBoardHandler
{
	private static final Logger LOGGER = Logger.getLogger(LunaShop.class.getName());
	private final Map<String, ScheduledFuture<?>> _expiretasks = new ConcurrentHashMap<>();
	private final Map<String, Long> _premiumData = new ConcurrentHashMap<>();
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.PREMIUM_ACCOUNT_PRICE));
	
	private static final String[] COMMANDS =
	{
		"_lunashop",
		"_bbsLuna"
	};
	
	private static final String[] CUSTOM_COMMANDS =
	{
		"_lunaMain",
		"_bbspremium",
		"delpremium",
		"_bbsteleport",
		"_lunaShopAppearance",
		"_lunaShopWeapon",
		"_lunaShopJewelry",
		"_lunaexcmultisell",
		"_lunasell",
		"_bbsfantasy",
		"statAdd",
		"statSub",
		"buyStatPoint",
		"specAdd",
		"specSub",
		"buySpecPoint",
		"buyLunaIndex",
		"buyLunaStart",
		"buyLunaFinish",
		"buyLunaAgain",
		"deliveryLuna",
		"SiegeInfo1",
		"SiegeInfo2",
		"SiegeInfo3",
		"SiegeInfo4",
		"SiegeInfo5",
		"sendMail",
		"buyItemIndex",
		"buyItemStart",
		"addItem",
		"addSudo",
		"subSudo",
		"addForden",
		"subForden",
		"buyItemFinish",
		"sendMail_Item",
		"buyItemAgain"
	};
	
	public static final Location[] POINTS =
	{
		new Location(-60695, -56896, -2032),
		new Location(-59716, -55920, -2032),
		new Location(-58752, -56896, -2032),
		new Location(-59716, -57864, -2032)
	};
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		final List<String> commands = new ArrayList<>();
		commands.addAll(Arrays.asList(COMMANDS));
		commands.addAll(Arrays.asList(CUSTOM_COMMANDS));
		return commands.stream().filter(Objects::nonNull).toArray(String[]::new);
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		switch (command)
		{
			case "SiegeInfo1":
				player.sendPacket(new SiegeInfo(CastleManager.getInstance().getCastleById(1), player));
				break;
			case "SiegeInfo2":
				player.sendPacket(new SiegeInfo(CastleManager.getInstance().getCastleById(2), player));
				break;
			case "SiegeInfo3":
				player.sendPacket(new SiegeInfo(CastleManager.getInstance().getCastleById(3), player));
				break;
			case "SiegeInfo4":
				player.sendPacket(new SiegeInfo(CastleManager.getInstance().getCastleById(4), player));
				break;
			case "SiegeInfo5":
				player.sendPacket(new SiegeInfo(CastleManager.getInstance().getCastleById(5), player));
				break;
		}
		
		String buyer = player.getVariables().getString("buyLunaBuyer", "");
		String price = player.getVariables().getString("buyLunaPrice", "");
		String buyluna = player.getVariables().getString("buyLuna", "");
		String buyerName = player.getVariables().getString("buyItemBuyer", "");
		String TotalPrice = player.getVariables().getString("buyItemPrice", "");
		int sudoNum = player.getVariables().getInt("sudoItem", 0);
		int fordenNum = player.getVariables().getInt("fordenItem", 0);
		
		if (command.equals("_lunaMain"))
		{
			BorinetHtml.getInstance().showLunaMainHtml(player);
		}
		else if (command.equals("buyItemIndex"))
		{
			if (player.getVariables().getBoolean("buyItemSendMail", false))
			{
				BorinetHtml.getInstance().showShopHtml(player, "_finish_Item", buyerName, TotalPrice, "");
			}
			else if (player.getVariables().getBoolean("reciveItem", false))
			{
				BorinetHtml.getInstance().showShopHtml(player, "_wait", "", "", "");
			}
			else
			{
				BorinetHtml.getInstance().showShopHtml(player, "_Item", "", "", "");
				LunaShopItemInfo.removeItemVariables(player);
			}
		}
		else if (command.equals("buyItemStart"))
		{
			BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
		}
		else if (command.equals("buyLunaIndex"))
		{
			if (player.getVariables().getBoolean("buyLunaSendMail", false))
			{
				BorinetHtml.getInstance().showShopHtml(player, "_finish", buyer, price, buyluna);
			}
			else if (player.getVariables().getBoolean("reciveLuna", false))
			{
				BorinetHtml.getInstance().showShopHtml(player, "_wait", "", "", "");
			}
			else
			{
				BorinetHtml.getInstance().showShopHtml(player, "_Luna", "", "", "");
				LunaShopItemInfo.removeLunaVariables(player);
			}
		}
		else if (command.equals("sendMail"))
		{
			if ((buyer != null) && (price != null))
			{
				BorinetHtml.getInstance().showShopHtml(player, "_sendMail", buyer, price, buyluna);
				SendEmailDonate.sendMail(player, buyer, price, buyluna);
				player.getVariables().remove("buyLunaSendMail");
			}
			else
			{
				player.sendPacket(SystemMessageId.THERE_IS_SOME_INFORMATION_MISSING_PLEASE_TRY_AGAIN);
				player.sendMessage("누락된 정보가 있습니다. 다시 시도해주시기 바랍니다.");
				BorinetHtml.showHtml(player, "LunaShop/buyLuna/selectList.htm", 0, "");
			}
		}
		else if (command.equals("sendMail_Item"))
		{
			final String ItemList = LunaShopItemInfo.BuyItemList(player);
			
			if ((buyerName != null) && (TotalPrice != null) && (ItemList != null))
			{
				BorinetHtml.getInstance().showShopHtml(player, "_sendMail_Item", buyerName, TotalPrice, ItemList);
				SendEmailDonate.sendMail(player, buyerName, TotalPrice, ItemList);
				LunaShopItemInfo.removeItemVariables(player);
			}
			else
			{
				player.sendPacket(SystemMessageId.THERE_IS_SOME_INFORMATION_MISSING_PLEASE_TRY_AGAIN);
				player.sendMessage("누락된 정보가 있습니다. 다시 시도해주시기 바랍니다.");
				BorinetHtml.showHtml(player, "LunaShop/buyLuna/selectList.htm", 0, "");
			}
		}
		else if (command.equals("buyLunaStart"))
		{
			BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_event" : "_start", "", "", "");
		}
		else if (command.equals("buyLunaAgain"))
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				PreparedStatement rsc = con.prepareStatement("UPDATE auto_lunabuy SET checked = 3, reward_time = 0 WHERE charId = '" + player.getObjectId() + "' AND checked = 0");
				rsc.executeUpdate();
			}
			catch (SQLException e)
			{
			}
			LunaShopItemInfo.removeLunaVariables(player);
			BorinetHtml.showHtml(player, "LunaShop/buyLuna/selectList.htm", 0, "");
		}
		else if (command.equals("buyItemAgain"))
		{
			LunaShopItemInfo.removeItemVariables(player);
			BorinetHtml.showHtml(player, "LunaShop/buyLuna/selectList.htm", 0, "");
		}
		else if (command.startsWith("addItem"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				final String number = st.nextToken();
				
				if (player.getVariables().getBoolean("buyItem_" + number, false))
				{
					player.getVariables().remove("buyItem_" + number);
				}
				else
				{
					player.getVariables().set("buyItem_" + number, true);
				}
				
				BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
			}
			catch (Exception e)
			{
				player.sendPacket(SystemMessageId.NAME_MUST_TO_BETWEEN_1_AND_10_CHARACTERS);
				player.sendMessage("이름은 1자 이상 10자 이하로 입력하셔야 합니다.");
				BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
			}
		} // fordenItem
		else if (command.startsWith("addSudo"))
		{
			if (player.getVariables().getInt("sudoItem", 0) > 0)
			{
				sudoNum += 1;
			}
			else
			{
				sudoNum = 1;
			}
			player.getVariables().set("sudoItem", sudoNum);
			BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", buyer, "", "");
		}
		else if (command.startsWith("subSudo"))
		{
			if (player.getVariables().getInt("sudoItem", 0) > 0)
			{
				sudoNum -= 1;
			}
			else
			{
				sudoNum = 0;
			}
			player.getVariables().set("sudoItem", sudoNum);
			BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", buyer, "", "");
		}
		else if (command.startsWith("addForden"))
		{
			if (player.getVariables().getInt("fordenItem", 0) > 0)
			{
				fordenNum += 1;
			}
			else
			{
				fordenNum = 1;
			}
			player.getVariables().set("fordenItem", fordenNum);
			BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", buyer, "", "");
		}
		else if (command.startsWith("subForden"))
		{
			if (player.getVariables().getInt("fordenItem", 0) > 0)
			{
				fordenNum -= 1;
			}
			else
			{
				fordenNum = 0;
			}
			player.getVariables().set("fordenItem", fordenNum);
			BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", buyer, "", "");
		}
		else if (command.startsWith("buyItemFinish"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			String buyerInputName = null;
			String buyerInputName2 = null;
			try
			{
				buyerInputName = st.nextToken();
				buyerInputName2 = st.nextToken();
				if (buyerInputName2 != null)
				{
					BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
					player.sendPacket(SystemMessageId.NAME_CONTAINS_SPACE_Please_REMOVE_SPACES_WHEN_ENTERING_NAME);
					player.sendMessage("이름에 공백이 있습니다. 이름 입력시 공백을 제거해주세요.");
					return false;
				}
			}
			catch (Exception e)
			{
				if (buyerInputName2 == null)
				{
					TotalPrice = player.getVariables().getString("buyItemPrice", "");
					if (!LunaShopItemInfo.checkBuyItems(player))
					{
						BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
						player.sendPacket(SystemMessageId.PLEASE_SELECT_THE_ITEM_YOU_WISH_TO_PURCHASE);
						player.sendMessage("구매하실 아이템을 선택해주세요.");
						return false;
					}
					
					player.getVariables().set("reciveItem", true);
					String ItemList = LunaShopItemInfo.BuyItemList(player);
					
					buyItem(player, buyerInputName, TotalPrice, ItemList);
					return false;
				}
				
				player.sendPacket(SystemMessageId.NAME_MUST_TO_BETWEEN_1_AND_10_CHARACTERS);
				player.sendMessage("이름은 1자 이상 10자 이하로 입력하셔야 합니다.");
				BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
				LunaShopItemInfo.removeItemVariables(player);
			}
		}
		else if (command.startsWith("buyLunaFinish"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			String buyerInputName = null;
			String priceMoney = null;
			String buyerName2 = null;
			try
			{
				buyerInputName = st.nextToken();
				priceMoney = st.nextToken();
				buyerName2 = st.nextToken();
				
				player.sendPacket(SystemMessageId.NAME_CONTAINS_SPACE_Please_REMOVE_SPACES_WHEN_ENTERING_NAME);
				player.sendMessage("이름에 공백이 있습니다. 이름 입력시 공백을 제거해주세요.");
				BorinetHtml.getInstance().showShopHtml(player, "_start", "", "", "");
				LunaShopItemInfo.removeLunaVariables(player);
				return false;
			}
			catch (Exception e)
			{
				if (buyerName2 == null)
				{
					player.getVariables().set("reciveLuna", true);
					player.getVariables().set("buyLunaBuyer", buyerInputName);
					player.getVariables().set("buyLunaPrice", priceMoney);
					
					buyLuna(player, buyerInputName, priceMoney);
					return false;
				}
				player.sendPacket(SystemMessageId.NAME_MUST_TO_BETWEEN_1_AND_10_CHARACTERS);
				player.sendMessage("이름은 1자 이상 10자 이하로 입력하셔야 합니다.");
				BorinetHtml.getInstance().showShopHtml(player, "_start", "", "", "");
				LunaShopItemInfo.removeLunaVariables(player);
			}
		}
		else if (command.startsWith("deliveryLuna"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			boolean nameLength = true;
			boolean name = true;
			boolean luna = true;
			boolean adena = true;
			try
			{
				final String recivedName = st.nextToken();
				final String priceMoney = st.nextToken();
				int sendLuna = Integer.parseInt(priceMoney);
				
				if (recivedName.matches(".*[\\uAC00-\\uD7A3]+.*"))
				{
					if ((recivedName.length() < 1) || (recivedName.length() > 8))
					{
						nameLength = false;
						return true;
					}
				}
				else
				{
					if ((recivedName.length() < 1) || (recivedName.length() > 16))
					{
						nameLength = false;
						return true;
					}
				}
				
				if (!CharInfoTable.getInstance().doesCharNameExist(recivedName))
				{
					name = false;
					player.sendPacket(SystemMessageId.THIS_CHARACTER_NAME_DOES_NOT_EXIST_PLEASE_ENTER_AGAIN);
					player.sendMessage("존재하지 않는 캐릭터이름 입니다. 다시 입력하세요.");
					return true;
				}
				int minLuna = 100;
				if (sendLuna < minLuna)
				{
					luna = false;
					player.sendPacket(new SystemMessage(SystemMessageId.GIFTS_OF_AT_LEAST_S1_LUNA).addInt(minLuna));
					player.sendMessage("최소 " + minLuna + " 루나 이상 선물이 가능합니다.");
					return true;
				}
				if (player.getLuna() < (sendLuna * 1.1))
				{
					luna = false;
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_DOOT_HAVE_ENOUGH_S1).addString(itemName));
					player.sendMessage(itemName + "가 부족합니다.");
					return true;
				}
				if (player.getInventory().getAdena() < 30000000)
				{
					adena = false;
					player.sendPacket(SystemMessageId.YOU_DOOT_HAVE_ENOUGH_ADENA);
					player.sendMessage("아데나가 부족합니다.");
					return true;
				}
				
				int LunaTex = (int) (sendLuna * 0.1);
				
				LunaManager.getInstance().useLunaPoint(player, sendLuna, "루나선물");
				player.destroyItemByItemId("루나선물 전송", 41000, LunaTex, player, true);
				player.destroyItemByItemId("루나선물 전송", 57, 30000000, player, true);
				LunaDelivery.sendLuna(recivedName, sendLuna, player.getName());
				
				String lunas = Util.formatAdena(sendLuna);
				player.sendMessage(lunas + " 루나를 " + recivedName + "님에게 전송하였습니다.");
				String html = HtmCache.getInstance().getHtm(null, "data/html/guide/lunadelivery_finish.htm");
				html = html.replace("%charname%", recivedName);
				html = html.replace("%luna%", Integer.toString(sendLuna));
				html = html.replace("%lunaTex%", Integer.toString(LunaTex));
				player.sendPacket(new NpcHtmlMessage(html));
			}
			catch (Exception e)
			{
				if (nameLength)
				{
					player.sendPacket(SystemMessageId.PLEASE_SPECIFY_BETWEEN_1_AND_8_CHARACTERS_IN_KOREAN_AND_1_AND_16_CHARACTERS_IN_ENGLISH);
					player.sendMessage("한글 1자 이상 8자 이내, 영문 1자 이상 16자 이내로 정해주십시오.");
				}
				else if (name)
				{
					player.sendPacket(SystemMessageId.THIS_CHARACTER_NAME_DOES_NOT_EXIST_PLEASE_ENTER_AGAIN);
					player.sendMessage("존재하지 않는 캐릭터이름 입니다. 다시 입력하세요.");
				}
				else if (luna)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_DOOT_HAVE_ENOUGH_S1).addString(itemName));
					player.sendMessage(itemName + "가 부족합니다.");
				}
				else if (adena)
				{
					player.sendPacket(SystemMessageId.YOU_DOOT_HAVE_ENOUGH_ADENA);
					player.sendMessage("아데나가 부족합니다.");
				}
				else
				{
					player.sendPacket(SystemMessageId.INVALID_INPUT_PLEASE_CHECK_AGAIN);
					player.sendMessage("입력이 올바르지 않습니다. 다시 확인해주세요.");
				}
			}
		}
		else if (command.startsWith("_bbsLuna;"))
		{
			final String path = command.replace("_bbsLuna;", "");
			if ((path.length() > 0) && path.endsWith(".htm"))
			{
				BorinetHtml.showHtml(player, path, 0, "");
			}
		}
		else if (command.startsWith("_lunaShopAppearance"))
		{
			final String fullBypass = command.replace("_lunaShopAppearance ", "");
			final int multisellId = Integer.parseInt(fullBypass);
			BorinetHtml.showHtml(player, "LunaShop/appearance.htm", 0, "");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		}
		else if (command.startsWith("_lunaShopWeapon"))
		{
			final String fullBypass = command.replace("_lunaShopWeapon ", "");
			final int multisellId = Integer.parseInt(fullBypass);
			BorinetHtml.showHtml(player, "LunaShop/weapon.htm", 0, "");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		}
		else if (command.startsWith("_lunaShopJewelry"))
		{
			final String fullBypass = command.replace("_lunaShopJewelry ", "");
			final int multisellId = Integer.parseInt(fullBypass);
			BorinetHtml.showHtml(player, "LunaShop/jewelry.htm", 0, "");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		}
		else if (command.startsWith("_lunaexcmultisell"))
		{
			final String fullBypass = command.replace("_lunaexcmultisell ", "");
			final int multisellId = Integer.parseInt(fullBypass);
			BorinetHtml.showHtml(player, "LunaShop/weapon.htm", 0, "");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, true);
		}
		else if (command.equals("_lunasell"))
		{
			player.sendPacket(new ShowBoard());
			player.sendPacket(new BuyList(BuyListData.getInstance().getBuyList(423), player, 0));
			player.sendPacket(new ExBuySellList(player, false));
		}
		else if (command.startsWith("_bbsteleport"))
		{
			final String teleBuypass = command.replace("_bbsteleport;", "");
			
			final String[] loc = teleBuypass.split(" ");
			int x = Integer.parseInt(loc[0]);
			int y = Integer.parseInt(loc[1]);
			int z = Integer.parseInt(loc[2]);
			
			player.disableAllSkills();
			player.sendPacket(new ShowBoard());
			player.destroyItemByItemId("CB_Teleport", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_TELEPORT_PRICE, player, true);
			player.setInstanceById(0);
			player.teleToLocation(x, y, z, 0);
			ThreadPool.schedule(player::enableAllSkills, 1000);
		}
		else if (command.startsWith("_bbspremium;"))
		{
			final String fullBypass = command.replace("_bbspremium;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int premiumDays = Integer.parseInt(buypassOptions[0]);
			final int premiumPrice = Integer.parseInt(buypassOptions[1]);
			if (player.getLuna() < (itemCount * premiumPrice))
			{
				BorinetHtml.showHtml(player, "LunaShop/premium_lowAdena.htm", 0, "");
			}
			else
			{
				LunaManager.getInstance().useLunaPoint(player, itemCount * premiumPrice, "프리미엄계정 구매");
				PremiumManager.getInstance().addPremiumTime(player.getAccountName(), premiumDays, TimeUnit.DAYS);
				player.sendMessage(new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(PremiumManager.getInstance().getPremiumExpiration(player.getAccountName())) + "까지 프리미엄 계정의 혜택을 받을 수 있습니다.");
				
				BorinetHtml.showHtml(player, "LunaShop/premium_sucess.htm", 0, "");
			}
		}
		else if (command.equals("delpremium"))
		{
			String accountName = player.getAccountName();
			player.setPremiumStatus(false);
			stopExpireTask(player);
			_premiumData.remove(accountName);
		}
		else if (command.equals("_bbsfantasy"))
		{
			player.getVariables().set("FANTASY_RETURN", player.getLocation());
			player.teleToLocation(POINTS[Rnd.get(POINTS.length)]);
		}
		// 스탯 포인트 시작
		else if (command.startsWith("buyStatPoint"))
		{
			try
			{
				final String buyStatPoint = command.replace("buyStatPoint ", "");
				CustomStats.getInstance().buyStatPoint(player, buyStatPoint);
			}
			catch (NumberFormatException e)
			{
			}
		}
		else if (command.startsWith("statAdd"))
		{
			final String statAdd = command.replace("statAdd ", "");
			final int stats = Integer.parseInt(statAdd);
			
			if (player.getStatPoint() >= 1)
			{
				switch (stats)
				{
					case 1:
					{
						if (player.getStr() < 5)
						{
							player.setStatPoint(player.getStatPoint() - 1);
							player.setStr(player.getStr() + 1);
							player.sendMessage("STR에 스탯 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 2:
					{
						if (player.getDex() < 5)
						{
							player.setStatPoint(player.getStatPoint() - 1);
							player.setDex(player.getDex() + 1);
							player.sendMessage("DEX에 스탯 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 3:
					{
						if (player.getCon() < 5)
						{
							player.setStatPoint(player.getStatPoint() - 1);
							player.setCon(player.getCon() + 1);
							player.sendMessage("CON에 스탯 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 4:
					{
						if (player.getMpw() < 5)
						{
							player.setStatPoint(player.getStatPoint() - 1);
							player.setMpw(player.getMpw() + 1);
							player.sendMessage("INT에 스탯 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 5:
					{
						if (player.getWit() < 5)
						{
							player.setStatPoint(player.getStatPoint() - 1);
							player.setWit(player.getWit() + 1);
							player.sendMessage("WIT에 스탯 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 6:
					{
						if (player.getMen() < 5)
						{
							player.setStatPoint(player.getStatPoint() - 1);
							player.setMen(player.getMen() + 1);
							player.sendMessage("MEN에 스탯 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
				}
			}
			else
			{
				player.sendMessage("스탯 포인트가 부족하여 더이상 올릴 수 없습니다.");
			}
		}
		else if (command.startsWith("statSub"))
		{
			final String statSub = command.replace("statSub ", "");
			final int stats = Integer.parseInt(statSub);
			
			switch (stats)
			{
				case 1:
				{
					if (player.getStr() != 0)
					{
						player.setStatPoint(player.getStatPoint() + 1);
						player.setStr(player.getStr() - 1);
						player.sendMessage("STR에 추가한 스탯 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 스탯 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 2:
				{
					if (player.getDex() != 0)
					{
						player.setStatPoint(player.getStatPoint() + 1);
						player.setDex(player.getDex() - 1);
						player.sendMessage("DEX에 추가한 스탯 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 스탯 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 3:
				{
					if (player.getCon() != 0)
					{
						player.setStatPoint(player.getStatPoint() + 1);
						player.setCon(player.getCon() - 1);
						player.sendMessage("CON에 추가한 스탯 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 스탯 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 4:
				{
					if (player.getMpw() != 0)
					{
						player.setStatPoint(player.getStatPoint() + 1);
						player.setMpw(player.getMpw() - 1);
						player.sendMessage("INT에 추가한 스탯 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 스탯 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 5:
				{
					if (player.getWit() != 0)
					{
						player.setStatPoint(player.getStatPoint() + 1);
						player.setWit(player.getWit() - 1);
						player.sendMessage("WIT에 추가한 스탯 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 스탯 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 6:
				{
					if (player.getMen() != 0)
					{
						player.setStatPoint(player.getStatPoint() + 1);
						player.setMen(player.getMen() - 1);
						player.sendMessage("MEN에 추가한 스탯 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 스탯 포인트를 내릴 수 없습니다.");
					}
					break;
				}
			}
		}
		// 스펙 포인트 시작
		else if (command.startsWith("buySpecPoint"))
		{
			try
			{
				final String buySpecPoint = command.replace("buySpecPoint ", "");
				CustomStats.getInstance().buySpecPoint(player, buySpecPoint);
			}
			catch (NumberFormatException e)
			{
			}
		}
		else if (command.startsWith("specAdd"))
		{
			final String specAdd = command.replace("specAdd ", "");
			final int specs = Integer.parseInt(specAdd);
			
			if (player.getSpecPoint() >= 1)
			{
				switch (specs)
				{
					case 1:
					{
						if (player.getSpec1() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec1(player.getSpec1() + 1);
							player.sendMessage("공격력에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 2:
					{
						if (player.getSpec2() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec2(player.getSpec2() + 1);
							player.sendMessage("마법력에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 3:
					{
						if (player.getSpec3() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec3(player.getSpec3() + 1);
							player.sendMessage("방어력에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 4:
					{
						if (player.getSpec4() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec4(player.getSpec4() + 1);
							player.sendMessage("마법저항에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 5:
					{
						if (player.getSpec5() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec5(player.getSpec5() + 1);
							player.sendMessage("명중에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 6:
					{
						if (player.getSpec6() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec6(player.getSpec6() + 1);
							player.sendMessage("회피에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 7:
					{
						if (player.getSpec7() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec7(player.getSpec7() + 1);
							player.sendMessage("공격속도에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
					case 8:
					{
						if (player.getSpec8() < 10)
						{
							player.setSpecPoint(player.getSpecPoint() - 1);
							player.setSpec8(player.getSpec8() + 1);
							player.sendMessage("마법속도에 특성 포인트를 추가하였습니다.");
						}
						else
						{
							player.sendMessage("더이상 포인트를 올릴 수 없습니다.");
						}
						break;
					}
				}
			}
			else
			{
				player.sendMessage("특성 포인트가 부족하여 더이상 올릴 수 없습니다.");
			}
		}
		else if (command.startsWith("specSub"))
		{
			final String specSub = command.replace("specSub ", "");
			final int specs = Integer.parseInt(specSub);
			
			switch (specs)
			{
				case 1:
				{
					if (player.getSpec1() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec1(player.getSpec1() - 1);
						player.sendMessage("공격력에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 2:
				{
					if (player.getSpec2() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec2(player.getSpec2() - 1);
						player.sendMessage("마법력에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 3:
				{
					if (player.getSpec3() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec3(player.getSpec3() - 1);
						player.sendMessage("방어력에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 4:
				{
					if (player.getSpec4() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec4(player.getSpec4() - 1);
						player.sendMessage("마법저항에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 5:
				{
					if (player.getSpec5() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec5(player.getSpec5() - 1);
						player.sendMessage("명중에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 6:
				{
					if (player.getSpec6() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec6(player.getSpec6() - 1);
						player.sendMessage("회피에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 7:
				{
					if (player.getSpec7() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec7(player.getSpec7() - 1);
						player.sendMessage("공격속도에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
				case 8:
				{
					if (player.getSpec8() != 0)
					{
						player.setSpecPoint(player.getSpecPoint() + 1);
						player.setSpec8(player.getSpec8() - 1);
						player.sendMessage("마법속도에 추가한 특성 포인트를 회수했습니다.");
					}
					else
					{
						player.sendMessage("더이상 특성 포인트를 내릴 수 없습니다.");
					}
					break;
				}
			}
		}
		return true;
	}
	
	private void buyLuna(Player player, String buyerName, String priceMoney)
	{
		try
		{
			if (!Util.isMatchingRegexp(buyerName, Config.NAME_TEMPLATE) || !Util.isAlphaNumeric(buyerName) || !Util.isValidName(buyerName))
			{
				player.sendPacket(SystemMessageId.TYPE_NAME_PLEASE_TRY_AGAIN);
				player.sendMessage("잘못된 이름입니다. 다시 입력해주세요.");
				BorinetHtml.getInstance().showShopHtml(player, "_start", "", "", "");
			}
			
			int luna = 0;
			switch (priceMoney)
			{
				case "1만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 130 : 100;
					break;
				case "3만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 430 : 330;
					break;
				case "5만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 780 : 600;
					break;
				case "7만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 1170 : 900;
					break;
				case "10만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 1690 : 1300;
					break;
				case "15만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 2600 : 2000;
					break;
				case "20만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 3900 : 3000;
					break;
				case "30만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 6500 : 5000;
					break;
				case "50만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 13000 : 10000;
					break;
				case "100만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 29900 : 23000;
					break;
				case "150만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 46800 : 36000;
					break;
				case "200만원":
					luna = (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? 65000 : 50000;
					break;
			}
			
			long send_time = System.currentTimeMillis() + 3600000;
			String request_time = BorinetUtil.dataDateFormat.format(new Date(System.currentTimeMillis()));
			insertDB(player, buyerName, priceMoney, luna, "", send_time, request_time);
			
			Message msg = new Message(IdManager.getInstance().getNextId(), player.getObjectId(), System.currentTimeMillis(), "루나 구매요청", "안녕하세요. " + Config.SERVER_NAME_KOR + "입니다.\n현재 루나 구매요청이 진행 중 입니다.\n\n[커뮤니티보드 -> 루나상점 -> 루나 또는 아이템 구매하기]메뉴로 이동 후 [아이템 구매하기] 버튼을 누른 후 [입금완료 및 확인요청]을 해주시기 바랍니다.\n\n입금액: " + priceMoney + "\n구매 아이템: " + luna + " 루나\n\n루나를 구매해 주셔서 감사합니다.\n" + Config.SERVER_NAME_KOR + "과 함께 즐거운 시간 되세요.", MailType.SERVER, false);
			MailManager.getInstance().sendMessage(msg);
			BorinetHtml.getInstance().showShopHtml(player, "_finish", buyerName, priceMoney, luna + "루나");
			Npc.playTutorialVoice(player, "borinet/LunaRequest");
			player.getVariables().set("buyLunaSendMail", true);
			player.getVariables().set("buyLuna", luna + "루나");
		}
		catch (Exception e)
		{
			player.sendPacket(SystemMessageId.NAME_MUST_TO_BETWEEN_1_AND_10_CHARACTERS);
			player.sendMessage("이름은 1자 이상 10자 이하로 입력하셔야 합니다.");
			BorinetHtml.getInstance().showShopHtml(player, "_start", "", "", "");
			LunaShopItemInfo.removeLunaVariables(player);
		}
	}
	
	private void buyItem(Player player, String buyerName, String TotalPrice, String ItemList)
	{
		try
		{
			if (!Util.isMatchingRegexp(buyerName, Config.NAME_TEMPLATE) || !Util.isAlphaNumeric(buyerName) || !Util.isValidName(buyerName))
			{
				player.sendPacket(SystemMessageId.TYPE_NAME_PLEASE_TRY_AGAIN);
				player.sendMessage("잘못된 이름입니다. 다시 입력해주세요.");
				BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
				return;
			}
			
			long send_time = System.currentTimeMillis() + 3600000;
			String request_time = BorinetUtil.dataDateFormat.format(new Date(System.currentTimeMillis()));
			String ItemIds = LunaShopItemInfo.BuyItemIds(player);
			insertDB(player, buyerName, TotalPrice, 0, ItemIds, send_time, request_time);
			
			Message msg = new Message(IdManager.getInstance().getNextId(), player.getObjectId(), System.currentTimeMillis(), "아이템 구매요청", "안녕하세요. " + Config.SERVER_NAME_KOR + "입니다.\n현재 아이템 구매요청이 진행 중 입니다.\n\n[커뮤니티보드 -> 루나상점 -> 루나 또는 아이템 구매하기]메뉴로 이동 후 [아이템 구매하기] 버튼을 누른 후 [입금완료 및 확인요청]을 해주시기 바랍니다.\n\n입금액: " + TotalPrice + "\n\n구매 아이템: " + ItemList + "\n\n아이템을 구매해 주셔서 감사합니다.\n" + Config.SERVER_NAME_KOR + "과 함께 즐거운 시간 되세요.", MailType.SERVER, false);
			MailManager.getInstance().sendMessage(msg);
			BorinetHtml.getInstance().showShopHtml(player, "_finish_Item", buyerName, TotalPrice, "");
			Npc.playTutorialVoice(player, "borinet/LunaRequest");
			player.getVariables().set("buyItemSendMail", true);
			player.getVariables().set("buyItemBuyer", buyerName);
			player.getVariables().set("buyItemPrice", TotalPrice);
		}
		catch (Exception e)
		{
			player.sendPacket(SystemMessageId.NAME_MUST_TO_BETWEEN_1_AND_10_CHARACTERS);
			player.sendMessage("이름은 1자 이상 10자 이하로 입력하셔야 합니다.");
			BorinetHtml.getInstance().showShopHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.MemorialDayCheck()) ? "_start_Item_event" : "_start_Item", "", "", "");
		}
	}
	
	private void stopExpireTask(Player player)
	{
		ScheduledFuture<?> task = _expiretasks.remove(player.getAccountName());
		if (task != null)
		{
			task.cancel(false);
			task = null;
		}
	}
	
	@Override
	public boolean writeCommunityBoardCommand(Player player, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		// TODO: Implement.
		return false;
	}
	
	private void insertDB(Player player, String buyer, String priceMoney, int luna, String itemList, Long sendTime, String requestTime)
	{
		String query = "INSERT INTO auto_lunabuy (charId, char_name, buyer, price, luna, item, send_time, request_time) VALUES (?,?,?,?,?,?,?,?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, player.getObjectId());
			ps.setString(2, player.getName());
			ps.setString(3, buyer);
			ps.setString(4, priceMoney);
			ps.setInt(5, luna);
			ps.setString(6, itemList);
			ps.setLong(7, sendTime);
			ps.setString(8, requestTime);
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "루나 구매요청에 오류가 발생했습니다. 플레이어 ID: " + player.getObjectId() + ", 구매자: " + buyer + ", 가격: " + priceMoney + ", 루나: " + luna + ", 아이템 목록: " + itemList + ", 전송 시간: " + sendTime + ", 요청 시간: " + requestTime, e);
			player.sendMessage("루나 구매요청에 오류가 발생했습니다. 운영자에게 문의바랍니다.");
		}
	}
	
	private static final LunaShop _instance = new LunaShop();
	
	public static LunaShop getInstance()
	{
		return _instance;
	}
}
