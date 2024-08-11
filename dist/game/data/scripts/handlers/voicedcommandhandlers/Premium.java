package handlers.voicedcommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.enums.AttributeType;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.PremiumManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class Premium implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"프리미엄",
		"프리미엄삭제",
		"del_premium",
		"저항보기"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("프리미엄삭제"))
		{
			String html = HtmCache.getInstance().getHtm(activeChar, "data/html/guide/premium_del.htm");
			activeChar.sendPacket(new NpcHtmlMessage(html));
		}
		else if (command.equals("del_premium"))
		{
			String html = HtmCache.getInstance().getHtm(activeChar, "data/html/guide/premium_del_success.htm");
			if (!activeChar.isPremium())
			{
				html = html.replace("<?delete?>", "<br><br><br><br><br><br><br><br>프리미엄 서비스를 이용중이지 않습니다.<br><br><br><br><br><br><br><br><br><br><br><br><br><br>");
			}
			else
			{
				String accountName = activeChar.getAccountName();
				PremiumManager.getInstance().removePremiumStatus(accountName, true);
				html = html.replace("<?delete?>", "<br><br><br><br><br><br><br><br>프리미엄 서비스가 철회되었습니다.<br><br><br><br><br>그동안 프리미엄 서비스를 이용해주셔서 감사합니다.<br><br><br><br><br>");
			}
			activeChar.sendPacket(new NpcHtmlMessage(html));
		}
		else if (command.startsWith("프리미엄") && Config.PREMIUM_SYSTEM_ENABLED)
		{
			String returnHtml = HtmCache.getInstance().getHtm(null, "data/html/guide/account.htm");
			String normal = HtmCache.getInstance().getHtm(activeChar, "data/html/guide/normal.htm");
			String premium = HtmCache.getInstance().getHtm(activeChar, "data/html/guide/premium.htm");
			final long endDate = PremiumManager.getInstance().getPremiumExpiration(activeChar.getAccountName());
			float customRate = Config.EVENT_RATE_CUSTOM_XP_SP;
			
			returnHtml = returnHtml.replace("<?ispremium?>", activeChar.isPremium() ? "<font color=00A5FF>프리미엄 계정</font>" : "일반 계정" + (BorinetTask.SpecialEvent() ? "<font color=\"LEVEL\"> - 이벤트 배율</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=\"LEVEL\"> - 주말배율</font>" : ""));
			if (!activeChar.isPremium())
			{
				float xpRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_XP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_XP_WEEKEND : Config.RATE_XP;
				float spRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_SP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_SP_WEEKEND : Config.RATE_SP;
				
				if (BorinetUtil.isEventDay() && Config.ENABLE_EVENT_RATE_CUSTOM)
				{
					xpRateMultiplier *= customRate;
					spRateMultiplier *= customRate;
				}
				
				String xpRate = String.format("%.2f", xpRateMultiplier);
				String spRate = String.format("%.2f", spRateMultiplier);
				
				returnHtml = returnHtml.replace("<?xp_rate?>", String.valueOf(Double.parseDouble(xpRate) > 2 ? "<font color=FF8000>" + xpRate + "</font>" : xpRate));
				returnHtml = returnHtml.replace("<?sp_rate?>", String.valueOf(Double.parseDouble(spRate) > 2.3 ? "<font color=FF8000>" + spRate + "</font>" : spRate));
				returnHtml = returnHtml.replace("<?item_rate?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_DEATH_DROP_CHANCE_MULTIPLIER + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER_WEEKEND + "</font>" : Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER));
				returnHtml = returnHtml.replace("<?item_finished_rate?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_FINISHED_ITEM + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_FINISHED_ITEM_WEEKEND + "</font>" : Config.RATE_FINISHED_ITEM));
				returnHtml = returnHtml.replace("<?scroll_rate?>", String.valueOf(BorinetTask.SpecialEvent() ? "<font color=FF8000>" + Config.CUSTOM_EVENT_RATE_EN_SCROLL_ITEM + "</font>" : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "<font color=FF8000>" + Config.RATE_EN_SCROLL_ITEM_WEEKEND + "</font>" : Config.RATE_EN_SCROLL_ITEM));
				returnHtml = returnHtml.replace("%premium%", "");
				
				float premium_xpRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_XP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_XP_WEEKEND : Config.RATE_XP;
				float premium_spRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_SP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_SP_WEEKEND : Config.RATE_SP;
				
				if (BorinetUtil.isEventDay() && Config.ENABLE_EVENT_RATE_CUSTOM)
				{
					premium_xpRateMultiplier *= customRate;
					premium_spRateMultiplier *= customRate;
				}
				
				String premium_xpRate = String.format("%.2f", premium_xpRateMultiplier * Config.PREMIUM_RATE_XP);
				String premium_spRate = String.format("%.2f", premium_spRateMultiplier * Config.PREMIUM_RATE_SP);
				
				normal = normal.replace("<?premium_xp_rate?>", "<font color=\"LEVEL\">" + String.valueOf(premium_xpRate) + "</font>");
				normal = normal.replace("<?premium_sp_rate?>", "<font color=\"LEVEL\">" + String.valueOf(premium_spRate) + "</font>");
				normal = normal.replace("<?premium_item_rate?>", "<font color=\"LEVEL\">" + String.valueOf((BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_DEATH_DROP_CHANCE_MULTIPLIER : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER_WEEKEND : Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER) * Config.PREMIUM_RATE_DROP_CHANCE) + "</font>");
				normal = normal.replace("<?premium_item_finished_rate?>", "<font color=\"LEVEL\">" + String.valueOf((BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_FINISHED_ITEM : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_FINISHED_ITEM_WEEKEND : Config.RATE_FINISHED_ITEM) * Config.PREMIUM_RATE_FINISHED_ITEM) + "</font>");
				normal = normal.replace("<?premium_scroll_rate?>", "<font color=\"LEVEL\">" + String.valueOf((BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_EN_SCROLL_ITEM : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_EN_SCROLL_ITEM_WEEKEND : Config.RATE_EN_SCROLL_ITEM) * Config.PREMIUM_RATE_EN_SCROLL_ITEM) + "</font>");
				
				returnHtml = returnHtml.replace("%normal%", normal);
			}
			else
			{
				float xpRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_XP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_XP_WEEKEND : Config.RATE_XP;
				float spRateMultiplier = BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_SP : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_SP_WEEKEND : Config.RATE_SP;
				
				if (BorinetUtil.isEventDay() && Config.ENABLE_EVENT_RATE_CUSTOM)
				{
					xpRateMultiplier *= customRate;
					spRateMultiplier *= customRate;
				}
				
				String xpRate = String.format("%.2f", xpRateMultiplier * Config.PREMIUM_RATE_XP);
				String spRate = String.format("%.2f", spRateMultiplier * Config.PREMIUM_RATE_SP);
				
				returnHtml = returnHtml.replace("<?xp_rate?>", "<font color=\"LEVEL\">" + xpRate + "</font>");
				returnHtml = returnHtml.replace("<?sp_rate?>", "<font color=\"LEVEL\">" + spRate + "</font>");
				returnHtml = returnHtml.replace("<?item_rate?>", "<font color=\"LEVEL\">" + String.valueOf((BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_DEATH_DROP_CHANCE_MULTIPLIER : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER_WEEKEND : Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER) * Config.PREMIUM_RATE_DROP_CHANCE) + "</font>");
				returnHtml = returnHtml.replace("<?item_finished_rate?>", "<font color=\"LEVEL\">" + String.valueOf((BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_FINISHED_ITEM : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_FINISHED_ITEM_WEEKEND : Config.RATE_FINISHED_ITEM) * Config.PREMIUM_RATE_FINISHED_ITEM) + "</font>");
				returnHtml = returnHtml.replace("<?scroll_rate?>", "<font color=\"LEVEL\">" + String.valueOf((BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_EN_SCROLL_ITEM : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_EN_SCROLL_ITEM_WEEKEND : Config.RATE_EN_SCROLL_ITEM) * Config.PREMIUM_RATE_EN_SCROLL_ITEM) + "</font>");
				
				premium = premium.replace("<?end_premium?>", "<font color=00A5FF>" + BorinetUtil.dataDateFormatKor.format(endDate) + "</font>");//
				premium = premium.replace("<?today?>", "<font color=70FFCA>" + BorinetUtil.dataDateFormatKor.format(System.currentTimeMillis()) + "</font>");//
				
				returnHtml = returnHtml.replace("%premium%", premium);
			}
			activeChar.sendPacket(new NpcHtmlMessage(returnHtml));
		}
		else if (command.startsWith("저항보기") && Config.PREMIUM_SYSTEM_ENABLED)
		{
			String returnHtml = HtmCache.getInstance().getHtm(null, "data/html/guide/characterResist.htm");
			
			String max = "MAX";
			returnHtml = returnHtml.replace("<?charName?>", activeChar.getName());
			
			returnHtml = returnHtml.replace("<?AttackFire?>", String.valueOf(activeChar.getAttackElementValue(AttributeType.FIRE)));
			returnHtml = returnHtml.replace("<?AttackWater?>", String.valueOf(activeChar.getAttackElementValue(AttributeType.WATER)));
			returnHtml = returnHtml.replace("<?AttackWind?>", String.valueOf(activeChar.getAttackElementValue(AttributeType.WIND)));
			returnHtml = returnHtml.replace("<?AttackEarth?>", String.valueOf(activeChar.getAttackElementValue(AttributeType.EARTH)));
			returnHtml = returnHtml.replace("<?AttackHoly?>", String.valueOf(activeChar.getAttackElementValue(AttributeType.HOLY)));
			returnHtml = returnHtml.replace("<?AttackDark?>", String.valueOf(activeChar.getAttackElementValue(AttributeType.DARK)));
			
			returnHtml = returnHtml.replace("<?ResistFire?>", String.valueOf(activeChar.getDefenseElementValue(AttributeType.FIRE)));
			returnHtml = returnHtml.replace("<?ResistWater?>", String.valueOf(activeChar.getDefenseElementValue(AttributeType.WATER)));
			returnHtml = returnHtml.replace("<?ResistWind?>", String.valueOf(activeChar.getDefenseElementValue(AttributeType.WIND)));
			returnHtml = returnHtml.replace("<?ResistEarth?>", String.valueOf(activeChar.getDefenseElementValue(AttributeType.EARTH)));
			returnHtml = returnHtml.replace("<?ResistHoly?>", String.valueOf(activeChar.getDefenseElementValue(AttributeType.HOLY)));
			returnHtml = returnHtml.replace("<?ResistDark?>", String.valueOf(activeChar.getDefenseElementValue(AttributeType.DARK)));
			
			returnHtml = returnHtml.replace("<?ResistBleed?>", (activeChar.getBleedResist() < 100 ? activeChar.getBleedResist() + "%" : max));
			returnHtml = returnHtml.replace("<?ResistDerangement?>", (activeChar.getDerangementResist() < 100 ? activeChar.getDerangementResist() + "%" : max));
			returnHtml = returnHtml.replace("<?ResistHold?>", (activeChar.getHoldResist() < 100 ? activeChar.getHoldResist() + "%" : max));
			returnHtml = returnHtml.replace("<?ResistPoison?>", (activeChar.getPoisonResist() < 100 ? activeChar.getPoisonResist() + "%" : max));
			returnHtml = returnHtml.replace("<?ResistSleep?>", (activeChar.getSleepResist() < 100 ? activeChar.getSleepResist() + "%" : max));
			returnHtml = returnHtml.replace("<?ResistStun?>", (activeChar.getStunResist() < 100 ? activeChar.getStunResist() + "%" : max));
			returnHtml = returnHtml.replace("<?ResistParalyze?>", (activeChar.getParalyzeResist() < 100 ? activeChar.getParalyzeResist() + "%" : max));
			returnHtml = returnHtml.replace("<?ResistTurnStone?>", (activeChar.getTurnStoneResist() < 100 ? activeChar.getTurnStoneResist() + "%" : max));
			
			activeChar.sendPacket(new NpcHtmlMessage(returnHtml));
		}
		else
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}