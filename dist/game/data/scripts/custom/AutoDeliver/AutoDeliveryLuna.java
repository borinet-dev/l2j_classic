package custom.AutoDeliver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class AutoDeliveryLuna
{
	private static final Logger LOGGER = Logger.getLogger(AutoDeliveryLuna.class.getName());
	
	static public void main(String[] args)
	{
		ThreadPool.scheduleAtFixedRate(() ->
		{
			AutoBuyLuna();
		}, 0, 10000);
	}
	
	private static void AutoBuyLuna()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			int luna = 0;
			int charId = 0;
			String items = "";
			String selectQuery = "SELECT id, charId, luna, item FROM auto_lunabuy WHERE checked = 1 AND charId = ?";
			String updateQuery = "UPDATE auto_lunabuy SET checked = 2, reward_time = ? WHERE id = ?";
			
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement selectStatement = con.prepareStatement(selectQuery))
			{
				selectStatement.setInt(1, player.getObjectId());
				
				try (ResultSet autoBuy = selectStatement.executeQuery())
				{
					while (autoBuy.next())
					{
						luna = autoBuy.getInt("luna");
						charId = autoBuy.getInt("charId");
						items = autoBuy.getString("item");
						
						try (PreparedStatement updateStatement = con.prepareStatement(updateQuery))
						{
							updateStatement.setString(1, BorinetUtil.dataDateFormat.format(new Date(System.currentTimeMillis())));
							updateStatement.setInt(2, autoBuy.getInt("id"));
							updateStatement.executeUpdate();
						}
						
						Player receiver = World.getInstance().getPlayer(charId);
						LunaManager.getInstance().giveLunaItem(receiver, luna, items);
					}
				}
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "자동 루나 구매를 처리하는데 실패했습니다. 플레이어 ID: " + player.getObjectId(), e);
			}
		}
	}
	
	static void print(Exception e)
	{
		e.printStackTrace();
	}
}