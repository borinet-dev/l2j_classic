package handlers.communityboard.borinet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.Util;

public class RankingBoard implements IParseBoardHandler
{
	private static final String HEADER_PATH = "data/html/CommunityBoard/Custom/header.htm";
	private static final String FOOTER_PATH = "data/html/CommunityBoard/Custom/ranking/footer.htm";
	private static final String DEFAULT_PATH = "data/html/CommunityBoard/Custom/ranking/";
	
	public RankingBoard()
	{
		rankUpdate();
		selectRankingPK();
		selectRankingPVP();
		selectRankingRK();
		selectRankingAdena();
	}
	
	private static final String[] COMMANDS =
	{
		"_bbsranking"
	};
	
	private static class RankingManager
	{
		private final String[] RankingPvPName = new String[10];
		private final String[] RankingPvPClan = new String[10];
		private final int[] RankingPvPClass = new int[10];
		private final int[] RankingPvPOn = new int[10];
		private final int[] RankingPvP = new int[10];
		
		private final String[] RankingPkName = new String[10];
		private final String[] RankingPkClan = new String[10];
		private final int[] RankingPkClass = new int[10];
		private final int[] RankingPkOn = new int[10];
		private final int[] RankingPk = new int[10];
		
		private final String[] RankingRaidName = new String[10];
		private final String[] RankingRaidClan = new String[10];
		private final int[] RankingRaidClass = new int[10];
		private final int[] RankingRaidOn = new int[10];
		private final int[] RankingRaid = new int[10];
		
		private final String[] RankingAdenaName = new String[10];
		private final String[] RankingAdenaClan = new String[10];
		private final int[] RankingAdenaClass = new int[10];
		private final int[] RankingAdenaOn = new int[10];
		private final long[] RankingAdena = new long[10];
	}
	
	static RankingManager RankingManagerStats = new RankingManager();
	private long update = System.currentTimeMillis() / 1000;
	private final int time_update = 30;
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		return COMMANDS;
	}
	
	private void rankUpdate()
	{
		// Schedule reset every 30 mins.
		final Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.MINUTE) >= 30)
		{
			calendar.add(Calendar.HOUR_OF_DAY, 1);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		}
		else
		{
			calendar.set(Calendar.MINUTE, 30);
			calendar.set(Calendar.SECOND, 0);
		}
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - System.currentTimeMillis());
		ThreadPool.scheduleAtFixedRate(this::onUpdate, startDelay, 1800000); // 1800000 = 30 minutes
	}
	
	private void onUpdate()
	{
		selectRankingPK();
		selectRankingPVP();
		selectRankingRK();
		selectRankingAdena();
		update = System.currentTimeMillis() / 1000;
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		if (command.equals("_bbsranking") || command.equals("_bbsranking:pk"))
		{
			show(player, 1);
		}
		else if (command.equals("_bbsranking:pvp"))
		{
			show(player, 2);
		}
		else if (command.equals("_bbsranking:rk"))
		{
			show(player, 3);
		}
		else if (command.equals("_bbsranking:adena"))
		{
			show(player, 4);
		}
		
		return true;
	}
	
	private void show(Player player, int page)
	{
		int number = 0;
		String html = null;
		final String header = HtmCache.getInstance().getHtm(player, HEADER_PATH);
		final String footer = HtmCache.getInstance().getHtm(player, FOOTER_PATH);
		
		if (page == 1)
		{
			html = HtmCache.getInstance().getHtm(player, DEFAULT_PATH + "index.htm");
			while (number < 10)
			{
				if (RankingManagerStats.RankingPkName[number] != null)
				{
					html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingPkName[number]);
					html = html.replace("<?clan_" + number + "?>", RankingManagerStats.RankingPkClan[number] == null ? "<font color=\"B59A75\">혈맹 없음</font>" : RankingManagerStats.RankingPkClan[number]);
					html = html.replace("<?class_" + number + "?>", Util.getFullClassName(RankingManagerStats.RankingPkClass[number]));
					html = html.replace("<?on_" + number + "?>", RankingManagerStats.RankingPkOn[number] == 1 ? "<font color=\"66FF33\">온라인</font>" : "<font color=\"B59A75\">오프라인</font>");
					html = html.replace("<?count_" + number + "?>", Util.formatAdena(RankingManagerStats.RankingPk[number]));
				}
				else
				{
					html = html.replace("<?name_" + number + "?>", "...");
					html = html.replace("<?clan_" + number + "?>", "...");
					html = html.replace("<?class_" + number + "?>", "...");
					html = html.replace("<?on_" + number + "?>", "...");
					html = html.replace("<?count_" + number + "?>", "...");
				}
				
				number++;
			}
		}
		else if (page == 2)
		{
			html = HtmCache.getInstance().getHtm(player, DEFAULT_PATH + "pvp.htm");
			while (number < 10)
			{
				if (RankingManagerStats.RankingPvPName[number] != null)
				{
					html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingPvPName[number]);
					html = html.replace("<?clan_" + number + "?>", RankingManagerStats.RankingPvPClan[number] == null ? "<font color=\"B59A75\">혈맹 없음</font>" : RankingManagerStats.RankingPvPClan[number]);
					html = html.replace("<?class_" + number + "?>", Util.getFullClassName(RankingManagerStats.RankingPvPClass[number]));
					html = html.replace("<?on_" + number + "?>", RankingManagerStats.RankingPvPOn[number] == 1 ? "<font color=\"66FF33\">온라인</font>" : "<font color=\"B59A75\">오프라인</font>");
					html = html.replace("<?count_" + number + "?>", Util.formatAdena(RankingManagerStats.RankingPvP[number]));
				}
				else
				{
					html = html.replace("<?name_" + number + "?>", "...");
					html = html.replace("<?clan_" + number + "?>", "...");
					html = html.replace("<?class_" + number + "?>", "...");
					html = html.replace("<?on_" + number + "?>", "...");
					html = html.replace("<?count_" + number + "?>", "...");
				}
				number++;
			}
		}
		else if (page == 3)
		{
			html = HtmCache.getInstance().getHtm(player, DEFAULT_PATH + "rk.htm");
			while (number < 10)
			{
				if (RankingManagerStats.RankingRaidName[number] != null)
				{
					html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingRaidName[number]);
					html = html.replace("<?clan_" + number + "?>", RankingManagerStats.RankingRaidClan[number] == null ? "<font color=\"B59A75\">혈맹 없음</font>" : RankingManagerStats.RankingRaidClan[number]);
					html = html.replace("<?class_" + number + "?>", Util.getFullClassName(RankingManagerStats.RankingRaidClass[number]));
					html = html.replace("<?on_" + number + "?>", RankingManagerStats.RankingRaidOn[number] == 1 ? "<font color=\"66FF33\">온라인</font>" : "<font color=\"B59A75\">오프라인</font>");
					html = html.replace("<?count_" + number + "?>", Util.formatAdena(RankingManagerStats.RankingRaid[number]));
				}
				else
				{
					html = html.replace("<?name_" + number + "?>", "...");
					html = html.replace("<?clan_" + number + "?>", "...");
					html = html.replace("<?class_" + number + "?>", "...");
					html = html.replace("<?on_" + number + "?>", "...");
					html = html.replace("<?count_" + number + "?>", "...");
				}
				number++;
			}
		}
		else if (page == 4)
		{
			html = HtmCache.getInstance().getHtm(player, DEFAULT_PATH + "adena.htm");
			long limit = 99999999;
			while (number < 10)
			{
				if (RankingManagerStats.RankingAdenaName[number] != null)
				{
					html = html.replace("<?name_" + number + "?>", RankingManagerStats.RankingAdenaName[number]);
					html = html.replace("<?clan_" + number + "?>", RankingManagerStats.RankingAdenaClan[number] == null ? "<font color=\"B59A75\">혈맹 없음</font>" : RankingManagerStats.RankingAdenaClan[number]);
					html = html.replace("<?class_" + number + "?>", Util.getFullClassName(RankingManagerStats.RankingAdenaClass[number]));
					html = html.replace("<?on_" + number + "?>", RankingManagerStats.RankingAdenaOn[number] == 1 ? "<font color=\"66FF33\">온라인</font>" : "<font color=\"B59A75\">오프라인</font>");
					if (RankingManagerStats.RankingAdena[number] > limit)
					{
						long adenarank = RankingManagerStats.RankingAdena[number] / 100000000;
						html = html.replace("<?count_" + number + "?>", Util.formatAdena(adenarank) + " 억");
					}
					else
					{
						html = html.replace("<?count_" + number + "?>", Util.formatAdena(RankingManagerStats.RankingAdena[number]));
					}
				}
				else
				{
					html = html.replace("<?name_" + number + "?>", "...");
					html = html.replace("<?clan_" + number + "?>", "...");
					html = html.replace("<?class_" + number + "?>", "...");
					html = html.replace("<?on_" + number + "?>", "...");
					html = html.replace("<?count_" + number + "?>", "...");
				}
				number++;
			}
		}
		else
		{
			return;
		}
		
		html = html.replace("<?update?>", String.valueOf(time_update));
		html = html.replace("<?last_update?>", String.valueOf(time(update)));
		html = html.replace("%header%", header);
		html = html.replace("<?footer?>", footer);
		html = html.replace("%mainbanner%", BorinetHtml.getBannerForRace(player));
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	private static String time(long time)
	{
		return TIME_FORMAT.format(new Date(time * 1000));
	}
	
	public static void selectRankingPVP()
	{
		int number = 0;
		RankingManagerStats.RankingPvPName[number] = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name, base_class, clanid, online, pvpkills FROM characters WHERE pvpkills >= 1 AND accesslevel = 0 ORDER BY pvpkills DESC LIMIT " + 10))
		{
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (!rset.getString("char_name").isEmpty())
				{
					RankingManagerStats.RankingPvPName[number] = rset.getString("char_name");
					int clan_id = rset.getInt("clanid");
					Clan clan = clan_id == 0 ? null : ClanTable.getInstance().getClan(clan_id);
					RankingManagerStats.RankingPvPClan[number] = clan == null ? null : clan.getName();
					RankingManagerStats.RankingPvPClass[number] = rset.getInt("base_class");
					RankingManagerStats.RankingPvPOn[number] = rset.getInt("online");
					RankingManagerStats.RankingPvP[number] = rset.getInt("pvpkills");
				}
				else
				{
					RankingManagerStats.RankingPvPName[number] = null;
					RankingManagerStats.RankingPvPClan[number] = null;
					RankingManagerStats.RankingPvPClass[number] = 0;
					RankingManagerStats.RankingPvPOn[number] = 0;
					RankingManagerStats.RankingPvP[number] = 0;
				}
				number++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}
	
	public static void selectRankingPK()
	{
		int number = 0;
		RankingManagerStats.RankingPkName[number] = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name, base_class, clanid, online, pkkills FROM characters WHERE pkkills >= 1 AND accesslevel = 0 ORDER BY pkkills DESC LIMIT " + 10))
		{
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (!rset.getString("char_name").isEmpty())
				{
					RankingManagerStats.RankingPkName[number] = rset.getString("char_name");
					int clan_id = rset.getInt("clanid");
					Clan clan = clan_id == 0 ? null : ClanTable.getInstance().getClan(clan_id);
					RankingManagerStats.RankingPkClan[number] = clan == null ? null : clan.getName();
					RankingManagerStats.RankingPkClass[number] = rset.getInt("base_class");
					RankingManagerStats.RankingPkOn[number] = rset.getInt("online");
					RankingManagerStats.RankingPk[number] = rset.getInt("pkkills");
				}
				else
				{
					RankingManagerStats.RankingPkName[number] = null;
					RankingManagerStats.RankingPkClan[number] = null;
					RankingManagerStats.RankingPkClass[number] = 0;
					RankingManagerStats.RankingPkOn[number] = 0;
					RankingManagerStats.RankingPk[number] = 0;
				}
				number++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}
	
	public static void selectRankingRK()
	{
		int number = 0;
		RankingManagerStats.RankingRaidName[number] = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name, base_class, clanid, online, raidbossPoints FROM characters WHERE raidbossPoints >= 1 AND accesslevel = 0 ORDER BY raidbossPoints DESC LIMIT " + 10))
		{
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (!rset.getString("char_name").isEmpty())
				{
					RankingManagerStats.RankingRaidName[number] = rset.getString("char_name");
					int clan_id = rset.getInt("clanid");
					Clan clan = clan_id == 0 ? null : ClanTable.getInstance().getClan(clan_id);
					RankingManagerStats.RankingRaidClan[number] = clan == null ? null : clan.getName();
					RankingManagerStats.RankingRaidClass[number] = rset.getInt("base_class");
					RankingManagerStats.RankingRaidOn[number] = rset.getInt("online");
					RankingManagerStats.RankingRaid[number] = rset.getInt("raidbossPoints");
				}
				else
				{
					RankingManagerStats.RankingRaidName[number] = null;
					RankingManagerStats.RankingRaidClan[number] = null;
					RankingManagerStats.RankingRaidClass[number] = 0;
					RankingManagerStats.RankingRaidOn[number] = 0;
					RankingManagerStats.RankingRaid[number] = 0;
				}
				number++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}
	
	public static void selectRankingAdena()
	{
		int number = 0;
		RankingManagerStats.RankingAdenaName[number] = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name, base_class, clanid, online, it.count FROM characters AS c LEFT JOIN items AS it ON (c.charId=it.owner_id) WHERE it.item_id=57 AND loc = 'INVENTORY' AND accesslevel = 0 ORDER BY it.count DESC LIMIT " + 10))
		{
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				if (!rset.getString("char_name").isEmpty())
				{
					RankingManagerStats.RankingAdenaName[number] = rset.getString("char_name");
					int clan_id = rset.getInt("clanid");
					Clan clan = clan_id == 0 ? null : ClanTable.getInstance().getClan(clan_id);
					RankingManagerStats.RankingAdenaClan[number] = clan == null ? null : clan.getName();
					RankingManagerStats.RankingAdenaClass[number] = rset.getInt("base_class");
					RankingManagerStats.RankingAdenaOn[number] = rset.getInt("online");
					RankingManagerStats.RankingAdena[number] = rset.getLong("count");
				}
				else
				{
					RankingManagerStats.RankingAdenaName[number] = null;
					RankingManagerStats.RankingAdenaClan[number] = null;
					RankingManagerStats.RankingAdenaClass[number] = 0;
					RankingManagerStats.RankingAdenaOn[number] = 0;
					RankingManagerStats.RankingAdena[number] = 0;
				}
				number++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}
}
