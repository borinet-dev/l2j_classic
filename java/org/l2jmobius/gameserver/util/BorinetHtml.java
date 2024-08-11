package org.l2jmobius.gameserver.util;

import java.text.SimpleDateFormat;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.commons.util.TimeUtil;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.instancemanager.PremiumManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EnterEventTimes;
import org.l2jmobius.gameserver.model.item.LunaShopItemInfo;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import smartguard.core.properties.GuardProperties;

public class BorinetHtml
{
	public final static String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final static int itemCountEx = Integer.parseInt(Util.formatAdena(Config.EXPAND_INVENTORY_PRICE));
	final static int itemCountChange = Integer.parseInt(Util.formatAdena(Config.CHANGE_NAME_PRICE));
	final static int itemCountChangeClan = Integer.parseInt(Util.formatAdena(Config.CHANGE_CLAN_NAME_PRICE));
	
	public void showMainHtml(Player player, String adress)
	{
		final String header = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/header.htm");
		String returnHtml = null;
		final SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm 까지");
		final long endDate = PremiumManager.getInstance().getPremiumExpiration(player.getAccountName());
		returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + adress + ".htm");
		returnHtml = returnHtml.replace("%header%", header);
		int inTimes = 0;
		int[] time = EnterEventTimes.check(player);
		inTimes = time[0];
		
		if ((returnHtml == null) || (header == null))
		{
			returnHtml = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
			CommunityBoardHandler.separateAndSend(returnHtml, player);
			return;
		}
		
		if (!Config.CUSTOM_CB_ENABLED)
		{
			returnHtml = returnHtml.replace("%clan_count%", Integer.toString(ClanTable.getInstance().getClanCount()));
		}
		returnHtml = returnHtml.replace("<?player_name?>", player.getName());
		returnHtml = returnHtml.replace("<?player_race?>", raceName(player));
		returnHtml = returnHtml.replace("<?player_class?>", ClassListData.getInstance().getClass(player.getClassId()).getClientCode());//
		returnHtml = returnHtml.replace("<?player_level?>", Integer.toString(player.getLevel()));//
		returnHtml = returnHtml.replace("<?player_clan?>", String.valueOf(player.getClan() != null ? player.getClan().getName() : "<font color=\"FF0000\">무소속</font>"));
		returnHtml = returnHtml.replace("<?player_noobless?>", player.isNoble() ? "O" : "X");
		returnHtml = returnHtml.replace("<?player_premium?>", endDate > 0 ? "<font color=00A5FF>" + format.format(endDate) + "</font>" : "<font color=FF0000>미사용</font>");//
		returnHtml = returnHtml.replace("<?luna_point?>", Util.formatAdena(player.getLuna()));
		returnHtml = returnHtml.replace("<?online_time?>", TimeUtil.formatTimes(inTimes, false));
		
		float xpRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_XP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_XP_WEEKEND : Config.RATE_XP;
		float spRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_SP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_SP_WEEKEND : Config.RATE_SP;
		
		if (BorinetUtil.isEventDay() && Config.ENABLE_EVENT_RATE_CUSTOM)
		{
			float customRate = Config.EVENT_RATE_CUSTOM_XP_SP;
			xpRateMultiplier *= customRate;
			spRateMultiplier *= customRate;
		}
		
		String xpRate = String.format("%.2f", xpRateMultiplier);
		String spRate = String.format("%.2f", spRateMultiplier);
		
		returnHtml = returnHtml.replace("<?server_rate?>", String.valueOf((BorinetTask.SpecialEvent() || BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=LEVEL>- " + BorinetUtil.getInstance().getEventName() + " 이벤트 중</font>" : ""));
		returnHtml = returnHtml.replace("<?xp_rate?>", String.valueOf(Double.parseDouble(xpRate) > 2 ? "<font color=FF8000>" + xpRate + "</font>" : xpRate));
		returnHtml = returnHtml.replace("<?sp_rate?>", String.valueOf(Double.parseDouble(spRate) > 2.3 ? "<font color=FF8000>" + spRate + "</font>" : spRate));
		returnHtml = returnHtml.replace("<?drop_adena_rate?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_DROP_ADENA + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_DROP_ADENA_WEEKEND + "</font>" : Config.RATE_DROP_ADENA));
		returnHtml = returnHtml.replace("<?item_rate?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_DEATH_DROP_CHANCE_MULTIPLIER + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER_WEEKEND + "</font>" : Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER));
		returnHtml = returnHtml.replace("<?item_finished_rate?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_FINISHED_ITEM + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_FINISHED_ITEM_WEEKEND + "</font>" : Config.RATE_FINISHED_ITEM));
		returnHtml = returnHtml.replace("<?drop_enscroll?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_EN_SCROLL_ITEM + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_EN_SCROLL_ITEM_WEEKEND + "</font>" : Config.RATE_EN_SCROLL_ITEM));
		returnHtml = returnHtml.replace("<?quest_drop_rate?>", String.valueOf(Config.RATE_QUEST_REWARD));
		returnHtml = returnHtml.replace("<?drop_spoil_rate?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_SPOIL_DROP_CHANCE_MULTIPLIER + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER_WEEKEND + "</font>" : Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER));
		returnHtml = returnHtml.replace("<?drop_raidboss_rate?>", String.valueOf(Config.RATE_RAID_DROP_CHANCE_MULTIPLIER));
		returnHtml = returnHtml.replace("<?server_uptime?>", String.valueOf(BorinetUtil.uptime()));
		returnHtml = returnHtml.replace("<?max_pc?>", String.valueOf(GuardProperties.MaxInstances));
		returnHtml = returnHtml.replace("%mainbanner%", getBannerForRace(player));
		
		CommunityBoardHandler.separateAndSend(returnHtml, player);
	}
	
	public void showShopHtml(Player player, String page, String buyerName, String priceMoney, String luna)
	{
		final String header = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/header.htm");
		final String menu = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/menu.htm");
		String html = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/buyLuna/index" + page + ".htm");
		if (html == null)
		{
			html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
		}
		// int total_price = Integer.parseInt(priceMoney);
		html = html.replace("%header%", header);
		html = html.replace("%menu%", menu);
		html = html.replace("%charname%", player.getName());
		html = html.replace("%money%", priceMoney);
		html = html.replace("%buyer%", buyerName);
		html = html.replace("%luna%", luna);
		html = html.replace("%eventName%", BorinetUtil.getInstance().getEventName());
		
		// 아이템 구매 페이지
		html = html.replace("%item1%", player.getVariables().getBoolean("buyItem_1", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item2%", player.getVariables().getBoolean("buyItem_2", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item3%", player.getVariables().getBoolean("buyItem_3", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item4%", player.getVariables().getBoolean("buyItem_4", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item5%", player.getVariables().getBoolean("buyItem_5", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item6%", player.getVariables().getBoolean("buyItem_6", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item7%", player.getVariables().getBoolean("buyItem_7", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item8%", player.getVariables().getBoolean("buyItem_8", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item9%", player.getVariables().getBoolean("buyItem_9", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item10%", player.getVariables().getBoolean("buyItem_10", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item11%", player.getVariables().getBoolean("buyItem_11", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item12%", player.getVariables().getBoolean("buyItem_12", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item13%", player.getVariables().getBoolean("buyItem_13", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item14%", player.getVariables().getBoolean("buyItem_14", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item15%", player.getVariables().getBoolean("buyItem_15", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item16%", player.getVariables().getBoolean("buyItem_16", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item17%", player.getVariables().getBoolean("buyItem_17", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item18%", player.getVariables().getBoolean("buyItem_18", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item19%", player.getVariables().getBoolean("buyItem_19", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item20%", player.getVariables().getBoolean("buyItem_20", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item21%", player.getVariables().getBoolean("buyItem_21", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item22%", player.getVariables().getBoolean("buyItem_22", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item23%", player.getVariables().getBoolean("buyItem_23", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item24%", player.getVariables().getBoolean("buyItem_24", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item25%", player.getVariables().getBoolean("buyItem_25", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item26%", player.getVariables().getBoolean("buyItem_26", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item27%", player.getVariables().getBoolean("buyItem_27", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item30%", player.getVariables().getBoolean("buyItem_30", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%item28%", Integer.toString(LunaShopItemInfo.sudoItem(player)));
		html = html.replace("%item29%", Integer.toString(LunaShopItemInfo.forgottenItem(player)));
		
		html = html.replace("%itemColor1%", player.getVariables().getBoolean("buyItem_1", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor2%", player.getVariables().getBoolean("buyItem_2", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor3%", player.getVariables().getBoolean("buyItem_3", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor4%", player.getVariables().getBoolean("buyItem_4", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor5%", player.getVariables().getBoolean("buyItem_5", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor6%", player.getVariables().getBoolean("buyItem_6", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor7%", player.getVariables().getBoolean("buyItem_7", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor8%", player.getVariables().getBoolean("buyItem_8", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor9%", player.getVariables().getBoolean("buyItem_9", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor10%", player.getVariables().getBoolean("buyItem_10", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor11%", player.getVariables().getBoolean("buyItem_11", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor12%", player.getVariables().getBoolean("buyItem_12", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor13%", player.getVariables().getBoolean("buyItem_13", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor14%", player.getVariables().getBoolean("buyItem_14", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor15%", player.getVariables().getBoolean("buyItem_15", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor16%", player.getVariables().getBoolean("buyItem_16", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor17%", player.getVariables().getBoolean("buyItem_17", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor18%", player.getVariables().getBoolean("buyItem_18", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor19%", player.getVariables().getBoolean("buyItem_19", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor20%", player.getVariables().getBoolean("buyItem_20", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor21%", player.getVariables().getBoolean("buyItem_21", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor22%", player.getVariables().getBoolean("buyItem_22", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor23%", player.getVariables().getBoolean("buyItem_23", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor24%", player.getVariables().getBoolean("buyItem_24", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor25%", player.getVariables().getBoolean("buyItem_25", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor26%", player.getVariables().getBoolean("buyItem_26", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor27%", player.getVariables().getBoolean("buyItem_27", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%itemColor30%", player.getVariables().getBoolean("buyItem_30", false) ? "<font color=LEVEL>" : "");
		html = html.replace("%sudoColor%", String.valueOf(player.getVariables().getInt("sudoItem", 0) > 0 ? "<font color=LEVEL>" : ""));
		html = html.replace("%fordenColor%", String.valueOf(player.getVariables().getInt("fordenItem", 0) > 0 ? "<font color=LEVEL>" : ""));
		
		player.getVariables().set("buyItemPrice", LunaShopItemInfo.ItemPrice(player));
		html = html.replace("%TotalPrice%", String.valueOf(LunaShopItemInfo.ItemPrice(player)));
		html = html.replace("%mainbanner%", getBannerForRace(player));
		
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	public void showLunaMainHtml(Player player)
	{
		int main = Rnd.get(1, 76);
		final String header = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/header.htm");
		final String menu = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/menu.htm");
		String html = null;
		html = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/main.htm");
		if ((html == null) || (menu == null) || (header == null))
		{
			html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
			CommunityBoardHandler.separateAndSend(html, player);
			return;
		}
		html = html.replace("%header%", header);
		html = html.replace("%menu%", menu);
		html = html.replace("%main%", "borinet.lunashop_" + main);
		html = html.replace("%mainbanner%", getBannerForRace(player));
		
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	public static void showHtml(Player player, String val, int level, String list)
	{
		final String header = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/header.htm");
		final String menu = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/menu.htm");
		final String spec = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/spec.htm");
		final String stat = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/stat.htm");
		String html = null;
		html = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + val);
		if ((html == null) || (menu == null) || (header == null) || (spec == null) || (stat == null))
		{
			html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
			CommunityBoardHandler.separateAndSend(html, player);
			return;
		}
		
		html = html.replace("%charname%", player.getName());
		html = html.replace("%clanName%", String.valueOf(player.getClan() != null ? player.getClan().getName() : "<font color=\"FF0000\">무소속</font>"));
		html = html.replace("%header%", header);
		html = html.replace("%level%", Integer.toString(level));
		html = html.replace("%list%", list);
		html = html.replace("%nick%", player.getName());
		html = html.replace("%menu%", menu);
		html = html.replace("%spec%", spec);
		html = html.replace("%stat%", stat);
		html = html.replace("%STR%", Integer.toString(player.getStat().getSTR()));
		html = html.replace("%DEX%", Integer.toString(player.getStat().getDEX()));
		html = html.replace("%CON%", Integer.toString(player.getStat().getCON()));
		html = html.replace("%INT%", Integer.toString(player.getStat().getINT()));
		html = html.replace("%WIT%", Integer.toString(player.getStat().getWIT()));
		html = html.replace("%MEN%", Integer.toString(player.getStat().getMEN()));
		html = html.replace("%PAtk%", Integer.toString(player.getPAtk()));
		html = html.replace("%MAtk%", Integer.toString(player.getMAtk()));
		html = html.replace("%PDef%", Integer.toString(player.getPDef()));
		html = html.replace("%MDef%", Integer.toString(player.getMDef()));
		html = html.replace("%Accuracy%", Integer.toString(player.getAccuracy()));
		html = html.replace("%MagicAccuracy%", Integer.toString(player.getMagicAccuracy()));
		html = html.replace("%EvasionRate%", Integer.toString(player.getEvasionRate()));
		html = html.replace("%MagicEvasionRate%", Integer.toString(player.getMagicEvasionRate()));
		html = html.replace("%PAtkSpd%", Integer.toString(player.getPAtkSpd()));
		html = html.replace("%MAtkSpd%", Integer.toString(player.getMAtkSpd()));
		html = html.replace("%fee%", Util.formatAdena(Config.FEE_DELETE_SUBCLASS_SKILLS));
		// 스탯 포인트
		html = html.replaceAll("%statStr%", HtmlUtil.getHpGaugeStat(180, player.getStr(), 5, false));
		html = html.replaceAll("%statDex%", HtmlUtil.getMpGaugeStat(180, player.getDex(), 5, false));
		html = html.replaceAll("%statCon%", HtmlUtil.getHpGaugeStat(180, player.getCon(), 5, false));
		html = html.replaceAll("%statInt%", HtmlUtil.getMpGaugeStat(180, player.getMpw(), 5, false));
		html = html.replaceAll("%statWit%", HtmlUtil.getHpGaugeStat(180, player.getWit(), 5, false));
		html = html.replaceAll("%statMen%", HtmlUtil.getMpGaugeStat(180, player.getMen(), 5, false));
		// 스펙 포인트
		html = html.replaceAll("%statvar1%", HtmlUtil.getHpGaugeSpec(180, player.getSpec1() * 10, 100, false));
		html = html.replaceAll("%statvar2%", HtmlUtil.getMpGaugeSpec(180, player.getSpec2() * 10, 100, false));
		html = html.replaceAll("%statvar3%", HtmlUtil.getHpGaugeSpec(180, player.getSpec3() * 10, 100, false));
		html = html.replaceAll("%statvar4%", HtmlUtil.getMpGaugeSpec(180, player.getSpec4() * 10, 100, false));
		html = html.replaceAll("%statvar5%", HtmlUtil.getHpGaugeSpec(180, player.getSpec5() * 10, 100, false));
		html = html.replaceAll("%statvar6%", HtmlUtil.getMpGaugeSpec(180, player.getSpec6() * 10, 100, false));
		html = html.replaceAll("%statvar7%", HtmlUtil.getHpGaugeSpec(180, player.getSpec7() * 10, 100, false));
		html = html.replaceAll("%statvar8%", HtmlUtil.getMpGaugeSpec(180, player.getSpec8() * 10, 100, false));
		html = html.replaceAll("%specpoint%", String.valueOf(player.getSpecPoint()));
		html = html.replaceAll("%statpoint%", String.valueOf(player.getStatPoint()));
		// 인벤토리 확장
		html = html.replace("%item%", itemName);
		html = html.replace("%slotSize%", Integer.toString(player.getInventoryLimit()));
		html = html.replace("%slots%", player.getOriginRace() != Race.DWARF ? String.valueOf(Config.EXPAND_INVENTORY_MAX) : String.valueOf(Config.EXPAND_INVENTORY_DWARF_MAX));
		html = html.replace("%costEx%", Integer.toString(itemCountEx));
		html = html.replace("%priceSlot%", Integer.toString(Config.EXPAND_INVENTORY_SLOT));
		// 인벤토리 확장 끝
		// 이름/클랜명 변경
		html = html.replace("%costChange%", Integer.toString(itemCountChange));
		html = html.replace("%costClanChange%", Integer.toString(itemCountChangeClan));
		// 이름/클랜명 변경 끝
		html = html.replace("%mainbanner%", getBannerForRace(player));
		
		CommunityBoardHandler.separateAndSend(html, player);
		return;
	}
	
	public String showAutoFollowHtml(Player player)
	{
		String[] leaders = BorinetUtil.getInstance().getAutoFollow(player);
		String leaderName = leaders[0];
		int range = Integer.valueOf(leaders[1]);
		
		String rangeName = "가까운 거리";
		if (range > 50)
		{
			rangeName = "적당한 거리";
		}
		
		String html = HtmCache.getInstance().getHtm(null, "data/html/borinet/autoplay/AutoFollow.htm");
		html = html.replace("%autofollow%", String.valueOf(player.getVariables().getBoolean("자동따라가기", false) ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미사용</font>"));
		html = html.replace("%range%", range > 0 ? "<td align=left width=130><font color=LEVEL>" + rangeName + "</font>" : "<td align=left width=130 height=30><combobox var=\"range\" list=\"가까운 거리;적당한 거리\" width=100>");
		html = html.replace("%targetName%", leaderName != null ? "<font color=LEVEL>" + leaderName + "</font>" : "<font color=FF0000>타겟 없음</font>");
		html = html.replace("%startAutoFollow%", String.valueOf(player.getVariables().getBoolean("자동따라가기", false) ? "<Button width=240 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .자동따라가기종료\">따라가기 모드를 중단한다</Button>" : "<Button width=240 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .자동따라가기시작 $range\">따라가기 모드를 사용한다</Button>"));
		html = html.replace("%autofollowEnable%", String.valueOf(player.getVariables().getBoolean("자동따라가기허용", false) ? "<Button width=180 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .자동따라가기허용\">자동따라가기를 거부한다</Button>" : "<Button width=180 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .자동따라가기허용\">자동따라가기를 허용한다</Button>"));
		html = html.replace("%autofollowStatus%", String.valueOf(player.getVariables().getBoolean("자동따라가기허용", false) ? "<font color=LEVEL>허용 중</font>" : "<font color=FF0000>거부 중</font>"));
		html = html.replace("%teleportFee%", Util.formatAdena(Config.AUTO_FOLLOW_TELEPORT_PEE));
		player.sendPacket(new NpcHtmlMessage(html));
		
		return html;
	}
	
	public static String raceName(Player player)
	{
		String race = null;
		switch (player.getOriginRace())
		{
			case HUMAN:
				race = "휴먼";
				break;
			case ELF:
				race = "엘프";
				break;
			case DARK_ELF:
				race = "다크엘프";
				break;
			case ORC:
				race = "오크";
				break;
			case DWARF:
				race = "드워프";
				break;
		}
		return race;
	}
	
	public static String getBannerForRace(Player player)
	{
		String banner;
		switch (player.getRace())
		{
			case HUMAN:
				banner = player.getAppearance().isFemale() ? "borinet.banner_humanf" : "borinet.banner_humanm";
				break;
			case ELF:
				banner = player.getAppearance().isFemale() ? "borinet.banner_elff" : "borinet.banner_elfm";
				break;
			case DARK_ELF:
				banner = player.getAppearance().isFemale() ? "borinet.banner_delff" : "borinet.banner_delfm";
				break;
			case ORC:
				banner = player.getAppearance().isFemale() ? "borinet.banner_orcf" : "borinet.banner_orcm";
				break;
			case DWARF:
				banner = player.getAppearance().isFemale() ? "borinet.banner_dwarff" : "borinet.banner_dwarfm";
				break;
			default:
				banner = player.getAppearance().isFemale() ? "borinet.banner_humanf" : "borinet.banner_humanm";
		}
		return banner;
	}
	
	public static BorinetHtml getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BorinetHtml INSTANCE = new BorinetHtml();
	}
}