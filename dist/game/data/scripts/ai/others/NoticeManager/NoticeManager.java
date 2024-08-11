package ai.others.NoticeManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetUtil;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class NoticeManager extends AbstractNpcAI
{
	private static final Logger LOGGER = Logger.getLogger(NoticeManager.class.getName());
	private static final String URL = "data/html/borinet/Notice/";
	private static final int MANAGER = 40019;
	
	private NoticeManager()
	{
		addFirstTalkId(MANAGER);
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
	}
	
	private static class AnnouncementManager
	{
		private String[] NoticeDate = new String[10];
		private String[] NoticeBody = new String[10];
	}
	
	static AnnouncementManager AnnouncementManagerStats = new AnnouncementManager();
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getId() == MANAGER)
		{
			AnnouncementManagerStats.NoticeDate = new String[10];
			AnnouncementManagerStats.NoticeBody = new String[10];
			
			selectNoticeBody();
			int number = 0;
			int main = Rnd.get(1, 76);
			
			String html = HtmCache.getInstance().getHtm(null, URL + "notice.htm");
			html = html.replace("<?server_uptime?>", String.valueOf(BorinetUtil.uptime()));
			html = html.replace("%main%", "borinet.lunashop_" + main);
			
			while (number < 10)
			{
				if (AnnouncementManagerStats.NoticeDate[number] != null)
				{
					html = html.replace("<?date_" + number + "?>", "<Button width=\"210\"ALIGN=\"LEFT\" ICON=\"NORMAL\" action=\"bypass -h Quest NoticeManager view " + number + " \">" + AnnouncementManagerStats.NoticeDate[number] + "</Button>");
				}
				else
				{
					html = html.replace("<?date_" + number + "?>", "...");
				}
				number++;
			}
			CommunityBoardHandler.separateAndSend(html, player);
		}
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("main"))
		{
			AnnouncementManagerStats.NoticeDate = new String[10];
			AnnouncementManagerStats.NoticeBody = new String[10];
			
			selectNoticeBody();
			int number = 0;
			int main = Rnd.get(1, 76);
			
			String html = HtmCache.getInstance().getHtm(null, URL + "notice.htm");
			html = html.replace("<?server_uptime?>", String.valueOf(BorinetUtil.uptime()));
			html = html.replace("%main%", "borinet.lunashop_" + main);
			
			while (number < 10)
			{
				if (AnnouncementManagerStats.NoticeDate[number] != null)
				{
					html = html.replace("<?date_" + number + "?>", "<Button width=\"210\"ALIGN=\"LEFT\" ICON=\"NORMAL\" action=\"bypass -h Quest NoticeManager view " + number + " \">" + AnnouncementManagerStats.NoticeDate[number] + "</Button>");
				}
				else
				{
					html = html.replace("<?date_" + number + "?>", "...");
				}
				number++;
			}
			CommunityBoardHandler.separateAndSend(html, player);
			// player.sendPacket(new ShowBoard());
		}
		else if (event.startsWith("view"))
		{
			final String fullBypass = event.replace("view ", "");
			final int number = Integer.parseInt(fullBypass);
			int previous = number - 1;
			int next = number + 1;
			
			String html = HtmCache.getInstance().getHtm(null, URL + "view_notice.htm");
			html = html.replace("<?content?>", AnnouncementManagerStats.NoticeBody[number]);
			html = html.replace("</div>", "</div><br1>");
			html = html.replace("<?list?>", ((number >= 0) && (number <= 9)) ? "<Button width=\"195\" ALIGN=\"LEFT\" ICON=\"NORMAL\" action=\"bypass -h Quest NoticeManager main\"><font color=\"FF9200\">업데이트 목록 보기</font></Button>" : "");
			html = html.replace("<?previous?>", (number > 0) && (AnnouncementManagerStats.NoticeDate[previous] != null) ? "<Button width=\"195\" ALIGN=\"LEFT\" ICON=\"NORMAL\" action=\"bypass -h Quest NoticeManager view " + previous + " \">" + AnnouncementManagerStats.NoticeDate[previous] + "</Button>" : "");
			html = html.replace("<?next?>", (number < 9) && (AnnouncementManagerStats.NoticeDate[next] != null) ? "<Button width=\"195\" ALIGN=\"LEFT\" ICON=\"NORMAL\" action=\"bypass -h Quest NoticeManager view " + next + " \">" + AnnouncementManagerStats.NoticeDate[next] + "</Button>" : "");
			CommunityBoardHandler.separateAndSend(html, player);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void selectNoticeBody()
	{
		int number = 0;
		String query = "SELECT title, body FROM patch ORDER BY no DESC LIMIT 10";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				String title = rset.getString("title");
				String body = rset.getString("body");
				
				if ((title != null) && !title.isEmpty())
				{
					AnnouncementManagerStats.NoticeDate[number] = title;
					AnnouncementManagerStats.NoticeBody[number] = body;
				}
				else
				{
					AnnouncementManagerStats.NoticeDate[number] = null;
					AnnouncementManagerStats.NoticeBody[number] = null;
				}
				number++;
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "공지사항 본문을 조회하는데 실패했습니다.", e);
		}
	}
	
	public static void main(String[] args)
	{
		new NoticeManager();
	}
}