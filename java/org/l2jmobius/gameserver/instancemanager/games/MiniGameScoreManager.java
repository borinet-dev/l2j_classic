package org.l2jmobius.gameserver.instancemanager.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerMiniGame;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.Broadcast;

public class MiniGameScoreManager
{
	private static final Logger LOGGER = Logger.getLogger(MiniGameScoreManager.class.getName());
	
	private static MiniGameScoreManager _instance = new MiniGameScoreManager();
	
	public static MiniGameScoreManager getInstance()
	{
		return _instance;
	}
	
	private MiniGameScoreManager()
	{
		LOGGER.info("미니게임 매니저를 로드하였습니다.");
	}
	
	public void insertScore(Player player, int score)
	{
		int bestScore = score;
		int best = checkScore(player, score);
		
		if (best > score)
		{
			bestScore = best;
			Broadcast.toPlayerScreenMessageS(player, score + "점을 획득하였습니다. 기록 변동은 없습니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, score + "점을 획득하였습니다. 기록 변동은 없습니다."));
		}
		else
		{
			Broadcast.toPlayerScreenMessageS(player, "신기록! " + bestScore + "점을 새롭게 등록하여 새로운 기록으로 등록됩니다!");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "신기록! " + bestScore + "점을 새롭게 등록하여 새로운 기록으로 등록됩니다!"));
		}
		
		int highestScore = getHighestScore();
		if (score > highestScore)
		{
			Broadcast.toAllOnlinePlayersOnScreen(player.getName() + "님이 미니게임에서 최고 점수(" + score + "점)을 달성하여 1위에 올랐습니다!");
		}
		
		if (score >= 5000)
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMiniGame(player), player);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_minigame_score(object_id, score) VALUES (?, ?)"))
		{
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, bestScore);
			statement.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "캐릭터 미니게임 점수를 데이터베이스에 업데이트하는 중 오류가 발생했습니다.", e);
		}
	}
	
	private int checkScore(Player player, int score)
	{
		int best = 0;
		String query = "SELECT score FROM character_minigame_score WHERE object_id = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, player.getObjectId());
			
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					best = rset.getInt("score");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "캐릭터 미니게임 점수를 확인하는 중 오류가 발생했습니다.", e);
		}
		return best;
	}
	
	private int getHighestScore()
	{
		int highestScore = 0;
		String query = "SELECT MAX(score) AS highest_score FROM character_minigame_score";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query);
			ResultSet rset = statement.executeQuery())
		{
			if (rset.next())
			{
				highestScore = rset.getInt("highest_score");
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "최고 미니게임 점수를 확인하는 중 오류가 발생했습니다.", e);
		}
		return highestScore;
	}
}