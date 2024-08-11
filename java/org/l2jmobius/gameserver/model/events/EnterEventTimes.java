package org.l2jmobius.gameserver.model.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;

public class EnterEventTimes
{
	private static final EnterEventTimes _instance = new EnterEventTimes();
	
	public static EnterEventTimes getInstance()
	{
		return _instance;
	}
	
	private static Future<?> _task;
	
	public static void EnterEventTimeStart(Player player)
	{
		_task = ThreadPool.scheduleAtFixedRateMin(() ->
		{
			if (player.isOnline() && !player.isInOfflineMode())
			{
				int[] time = check(player);
				int count = time[0];
				count++;
				updateTimes(player, count);
				
				if (count <= 300)
				{
					CheckTime(player, count);
				}
			}
		}, 1, 1);
	}
	
	public static void stopTask()
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
	}
	
	public static void CheckTime(Player player, int count)
	{
		switch (count)
		{
			case 60:
				player.sendMessage("접속보상 이벤트: 1시간 보상을 받으세요!");
				player.sendPacket(new TutorialShowQuestionMark(201, 1));
				player.sendPacket(new PlaySound("ItemSound.quest_tutorial"));
				break;
			case 120:
				player.sendMessage("접속보상 이벤트: 2시간 보상을 받으세요!");
				player.sendPacket(new TutorialShowQuestionMark(201, 1));
				player.sendPacket(new PlaySound("ItemSound.quest_tutorial"));
				break;
			case 180:
				player.sendMessage("접속보상 이벤트: 3시간 보상을 받으세요!");
				player.sendPacket(new TutorialShowQuestionMark(201, 1));
				player.sendPacket(new PlaySound("ItemSound.quest_tutorial"));
				break;
			case 300:
				player.sendMessage("접속보상 이벤트: 5시간 보상을 받으세요!");
				player.sendPacket(new TutorialShowQuestionMark(201, 1));
				player.sendPacket(new PlaySound("ItemSound.quest_tutorial"));
				player.getVariables().set("DailyLuna", 1);
				break;
		}
	}
	
	public static void index(Player player)
	{
		int[] time = check(player);
		int inTimes = time[0];
		int hour1 = time[1];
		int hour2 = time[2];
		int hour3 = time[3];
		int hour5 = time[4];
		
		String html = HtmCache.getInstance().getHtm(player, "data/html/scripts/EnterEvent.htm");
		
		html = html.replace("<?player_name?>", String.valueOf(player.getName()));
		if (inTimes >= 60)
		{
			html = html.replace("<?1hour?>", hour1 == 1 ? "<font color=\"B59A75\">보상완료</font>" : "<font color=\"LEVEL\">1시간</font>");
			html = html.replace("<?1img?>", hour1 == 1 ? "" : "<button value=\" \" action=\"bypass getItems:1\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>");
			html = html.replace("<?img1?>", hour1 == 1 ? "" : "background=\"icon.panel_2\"");
		}
		else
		{
			html = html.replace("<?1hour?>", hour1 == 1 ? "1시간" : "1시간");
			html = html.replace("<?1img?>", hour1 == 1 ? "" : "");
			html = html.replace("<?img1?>", hour1 == 1 ? "" : "");
		}
		if (inTimes >= 120)
		{
			html = html.replace("<?2hour?>", hour2 == 1 ? "<font color=\"B59A75\">보상완료</font>" : "<font color=\"LEVEL\">2시간</font>");
			html = html.replace("<?2img?>", hour2 == 1 ? "" : "<button value=\" \" action=\"bypass getItems:2\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>");
			html = html.replace("<?img2?>", hour2 == 1 ? "" : "background=\"icon.panel_2\"");
		}
		else
		{
			html = html.replace("<?2hour?>", hour2 == 1 ? "2시간" : "2시간");
			html = html.replace("<?2img?>", hour2 == 1 ? "" : "");
			html = html.replace("<?img2?>", hour2 == 1 ? "" : "");
		}
		if (inTimes >= 180)
		{
			html = html.replace("<?3hour?>", hour3 == 1 ? "<font color=\"B59A75\">보상완료</font>" : "<font color=\"LEVEL\">3시간</font>");
			html = html.replace("<?3img?>", hour3 == 1 ? "" : "<button value=\" \" action=\"bypass getItems:3\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>");
			html = html.replace("<?img3?>", hour3 == 1 ? "" : "background=\"icon.panel_2\"");
		}
		else
		{
			html = html.replace("<?3hour?>", hour3 == 1 ? "3시간" : "3시간");
			html = html.replace("<?3img?>", hour3 == 1 ? "" : "");
			html = html.replace("<?img3?>", hour3 == 1 ? "" : "");
		}
		if (inTimes >= 300)
		{
			html = html.replace("<?5hour?>", hour5 == 1 ? "<font color=\"B59A75\">보상완료</font>" : "<font color=\"LEVEL\">5시간</font>");
			html = html.replace("<?5img?>", hour5 == 1 ? "" : "<button value=\" \" action=\"bypass getItems:4\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>");
			html = html.replace("<?img5?>", hour5 == 1 ? "" : "background=\"icon.panel_2\"");
		}
		else
		{
			html = html.replace("<?5hour?>", hour5 == 1 ? "5시간" : "5시간");
			html = html.replace("<?5img?>", hour5 == 1 ? "" : "");
			html = html.replace("<?img5?>", hour5 == 1 ? "" : "");
		}
		
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	public static void updateTimes(Player player, int arg)
	{
		final int inTimes = arg;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE enter_event SET inTimes = ? WHERE account = (select account_name from characters where char_name = ?)"))
		{
			statement.setInt(1, inTimes);
			statement.setString(2, player.getName());
			statement.executeUpdate();
		}
		
		catch (Exception e)
		{
		}
	}
	
	public static void selectDB(Player player)
	{
		String account = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT account FROM enter_event WHERE account = (select account_name from characters where char_name = ?)"))
		{
			statement.setString(1, player.getName());
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				account = rset.getString("account");
			}
			
			if (account == null)
			{
				try (PreparedStatement statement1 = con.prepareStatement("INSERT INTO enter_event (account, 1hour, 2hour, 3hour, 5hour, inTimes) VALUES ((select account_name from characters where char_name = '" + player.getName() + "'), '0', '0', '0', '0', '0')"))
				{
					statement1.executeUpdate();
					EnterEventTimeStart(player);
				}
			}
			else
			{
				EnterEventTimeStart(player);
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public static int[] check(Player player)
	{
		int inTimes = 0;
		int hour1 = 0;
		int hour2 = 0;
		int hour3 = 0;
		int hour5 = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM enter_event WHERE account = (select account_name from characters where char_name = '" + player.getName() + "')"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				inTimes = rset.getInt("inTimes");
				hour1 = rset.getInt("1hour");
				hour2 = rset.getInt("2hour");
				hour3 = rset.getInt("3hour");
				hour5 = rset.getInt("5hour");
			}
		}
		catch (Exception e)
		{
		}
		return new int[]
		{
			inTimes,
			hour1,
			hour2,
			hour3,
			hour5
		};
	}
	
	public static void update(Player player, int arg)
	{
		final int hours = arg;
		
		int hour1 = 0;
		int hour2 = 0;
		int hour3 = 0;
		int hour5 = 0;
		int[] count = check(player);
		hour1 = count[1];
		hour2 = count[2];
		hour3 = count[3];
		hour5 = count[4];
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE enter_event SET 1hour = ?, 2hour = ?, 3hour = ?, 5hour = ? WHERE account = (select account_name from characters where char_name = '" + player.getName() + "')"))
		{
			statement.setInt(1, hours == 1 ? 1 : hour1);
			statement.setInt(2, hours == 2 ? 1 : hour2);
			statement.setInt(3, hours == 3 ? 1 : hour3);
			statement.setInt(4, hours == 4 ? 1 : hour5);
			statement.executeUpdate();
		}
		
		catch (Exception e)
		{
		}
	}
}