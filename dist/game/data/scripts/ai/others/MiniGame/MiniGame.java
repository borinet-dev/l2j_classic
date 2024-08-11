package ai.others.MiniGame;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class MiniGame extends AbstractNpcAI
{
	private static final Logger LOGGER = Logger.getLogger(MiniGame.class.getName());
	private static final String URL = "data/html/borinet/MiniGame/";
	private static final int MANAGER = 40016;
	
	private MiniGame()
	{
		addFirstTalkId(MANAGER);
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
	}
	
	private static class MiniGameManager
	{
		private String[] RankingMGName = new String[10];
		private long[] RankingMG = new long[10];
	}
	
	static MiniGameManager MinigameManagerStats = new MiniGameManager();
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getId() == MANAGER)
		{
			String html = HtmCache.getInstance().getHtm(null, URL + "index.htm");
			player.sendPacket(new NpcHtmlMessage(html));
		}
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "show":
			{
				show(player);
				break;
			}
			case "multisell":
			{
				MultisellData.getInstance().separateAndSend(4001600, player, null, false);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public void show(Player player)
	{
		MinigameManagerStats.RankingMGName = new String[10];
		MinigameManagerStats.RankingMG = new long[10];
		
		selectRankingMG();
		int number = 0;
		String html = HtmCache.getInstance().getHtm(null, URL + "MiniGame.htm");
		
		while (number < 10)
		{
			if (MinigameManagerStats.RankingMGName[number] != null)
			{
				html = html.replace("<?name_" + number + "?>", MinigameManagerStats.RankingMGName[number]);
				html = html.replace("<?count_" + number + "?>", Util.formatAdena(MinigameManagerStats.RankingMG[number]));
			}
			else
			{
				html = html.replace("<?name_" + number + "?>", "...");
				html = html.replace("<?count_" + number + "?>", "...");
			}
			number++;
		}
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	public static void selectRankingMG()
	{
		int number = 0;
		String query = "SELECT char_name, it.score FROM characters AS c " + "LEFT JOIN character_minigame_score AS it ON (c.charId = it.object_id) " + "WHERE it.score >= 1 ORDER BY it.score DESC LIMIT 10";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				String charName = rset.getString("char_name");
				long score = rset.getLong("score");
				
				if ((charName != null) && !charName.isEmpty())
				{
					MinigameManagerStats.RankingMGName[number] = charName;
					MinigameManagerStats.RankingMG[number] = score;
				}
				else
				{
					MinigameManagerStats.RankingMGName[number] = null;
					MinigameManagerStats.RankingMG[number] = 0;
				}
				number++;
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "미니게임 순위를 조회하는데 실패했습니다.", e);
		}
	}
	
	public static void main(String[] args)
	{
		new MiniGame();
	}
}