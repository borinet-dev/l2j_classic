package handlers.voicedcommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.Util;

public class ChangeVisualRaceCommand implements IVoicedCommandHandler
{
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.CHANGE_RACE_PRICE));
	private static final String[] VOICED_COMMANDS =
	{
		"외형변경"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		String race = "";
		if (activeChar.getVisualClassId() == -2)
		{
			if (activeChar.getRace() == Race.HUMAN)
			{
				if (activeChar.isMageClass())
				{
					race = "ChangeVisualHumanMage";
				}
				else
				{
					race = "ChangeVisualHumanFighter";
				}
			}
			else if (activeChar.getRace() == Race.ELF)
			{
				race = "ChangeVisualElf";
			}
			else if (activeChar.getRace() == Race.DARK_ELF)
			{
				race = "ChangeVisualDelf";
			}
			else if (activeChar.getRace() == Race.ORC)
			{
				if (activeChar.isMageClass())
				{
					race = "ChangeVisualOrcMage";
				}
				else
				{
					race = "ChangeVisualOrcFighter";
				}
			}
			else if (activeChar.getRace() == Race.DWARF)
			{
				race = "ChangeVisualDwarf";
			}
		}
		else
		{
			race = "ReturnVisualRace";
		}
		final String visual = HtmCache.getInstance().getHtm(activeChar, "data/html/CommunityBoard/Custom/LunaShop/VisualRace/" + race + ".htm");
		final String header = HtmCache.getInstance().getHtm(activeChar, "data/html/CommunityBoard/Custom/header.htm");
		final String menu = HtmCache.getInstance().getHtm(activeChar, "data/html/CommunityBoard/Custom/LunaShop/menu.htm");
		String html = HtmCache.getInstance().getHtm(null, "data/html/CommunityBoard/Custom/LunaShop/VisualRace.htm");
		if ((html == null) || (menu == null) || (header == null) || (visual == null))
		{
			html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
			CommunityBoardHandler.separateAndSend(html, activeChar);
			return true;
		}
		html = html.replace("%race%", visual);
		html = html.replace("%header%", header);
		html = html.replace("%menu%", menu);
		html = html.replace("%item%", itemName);
		html = html.replace("%costVisualRace%", Integer.toString(itemCount));
		html = html.replace("%mainbanner%", BorinetHtml.getBannerForRace(activeChar));
		CommunityBoardHandler.separateAndSend(html, activeChar);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
