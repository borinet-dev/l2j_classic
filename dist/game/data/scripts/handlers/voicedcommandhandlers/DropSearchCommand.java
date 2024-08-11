package handlers.voicedcommandhandlers;

import java.util.StringTokenizer;

import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.BorinetHtml;

import handlers.communityboard.borinet.DropSearch;

/**
 * @author 보리넷
 */
public class DropSearchCommand implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"아이템검색",
		"_bbssearch_drop"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (target != null)
		{
			try
			{
				int pageNumber = 1;
				StringTokenizer st = new StringTokenizer(target);
				StringBuilder nameBuilder = new StringBuilder();
				
				while (st.hasMoreTokens())
				{
					String token = st.nextToken();
					try
					{
						pageNumber = Integer.parseInt(token);
						break; // 페이지 번호를 찾으면 루프 종료
					}
					catch (NumberFormatException e)
					{
						if (nameBuilder.length() > 0)
						{
							nameBuilder.append(" ");
						}
						nameBuilder.append(token);
					}
				}
				
				String name = nameBuilder.toString().trim();
				
				if (name.isEmpty())
				{
					showHtml(activeChar, "", "");
					activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_PLEASE_TRY_AGAIN);
					return true;
				}
				
				final String result = DropSearch.buildItemSearchResult(name, pageNumber);
				final String pages = DropSearch.buildItemPageResult(name, pageNumber); // 페이지 정보 생성
				showHtml(activeChar, result, pages);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			showHtml(activeChar, "", "");
			return true;
		}
		return true;
	}
	
	public static void showHtml(Player activeChar, String val, String pages)
	{
		String html = null;
		html = HtmCache.getInstance().getHtm(activeChar, "data/html/CommunityBoard/Custom/dropsearch/main.htm");
		final String header = HtmCache.getInstance().getHtm(activeChar, "data/html/CommunityBoard/Custom/header.htm");
		final String dropText = HtmCache.getInstance().getHtm(activeChar, "data/html/CommunityBoard/Custom/dropsearch/dropText.htm");
		html = html.replace("%header%", header);
		html = html.replace("%searchResult%", val.isEmpty() ? dropText : val);
		html = html.replace("%pageResult%", pages);
		html = html.replace("%mainbanner%", BorinetHtml.getBannerForRace(activeChar));
		CommunityBoardHandler.separateAndSend(html, activeChar);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
