package handlers.bypasshandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class Cupon implements IBypassHandler
{
	public final Map<Integer, DailyMissionPlayerEntry> _entries = new ConcurrentHashMap<>();
	
	private static final String[] COMMANDS =
	{
		"쿠폰입력",
		"쿠폰입력완료"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (command.equals("쿠폰입력"))
		{
			String html = HtmCache.getInstance().getHtm(null, "data/html/guide/Cupon.htm");
			if (html == null)
			{
				html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
			}
			player.sendPacket(new NpcHtmlMessage(html));
		}
		if (command.startsWith("쿠폰입력완료 "))
		{
			String getCupon = command.substring(7).toUpperCase();
			long current_time = System.currentTimeMillis();
			
			String itemIds = checkDB(player, getCupon);
			long cupon_times = checkDBreuse(player, getCupon);
			String eventName = "CUPON_" + getCupon;
			
			if ((itemIds != null) && (cupon_times > current_time))
			{
				int checkGift = player.getAccountVariables().getInt(eventName, 0);
				if (checkGift == 1)
				{
					player.sendMessage("해당 계정에서 이미 사용한 쿠폰입니다.");
				}
				else
				{
					String topic = "쿠폰을 사용하여 아이템을 획득했습니다.";
					String body = "아이템을 첨부하였으니 확인하여 수령해 주시기 바랍니다!\n\n" + Config.SERVER_NAME_KOR + "과 함께 즐거운 시간 되시기 바랍니다.\n감사합니다.";
					BorinetUtil.getInstance().sendEventMail(player, topic, body, itemIds, eventName, false);
				}
			}
			else
			{
				player.sendMessage("유효한 쿠폰번호가 아니거나 사용기한이 종료되었습니다.");
			}
		}
		return true;
	}
	
	public String checkDB(Player player, String getCupon)
	{
		String itemIds = "";
		String query = "SELECT reward_item FROM cupon WHERE cupon_id = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, getCupon);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					itemIds = rs.getString("reward_item");
					if (itemIds != null)
					{
						return itemIds;
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "쿠폰을 확인하는 도중 오류가 발생했습니다. 쿠폰 ID: " + getCupon, e);
		}
		return null;
	}
	
	public long checkDBreuse(Player player, String getCupon)
	{
		long usingTime = 0;
		String query = "SELECT reuseDelay FROM cupon WHERE cupon_id = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, getCupon);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					usingTime = rs.getLong("reuseDelay");
					if (usingTime > 0)
					{
						return usingTime;
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "쿠폰 재사용 시간을 확인하는 도중 오류가 발생했습니다. 쿠폰 ID: " + getCupon, e);
		}
		return usingTime;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
