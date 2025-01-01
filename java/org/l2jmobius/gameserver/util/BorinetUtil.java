package org.l2jmobius.gameserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.GameServer;
import org.l2jmobius.gameserver.Shutdown;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.xml.CombinationItemsData;
import org.l2jmobius.gameserver.data.xml.EnchantItemData;
import org.l2jmobius.gameserver.data.xml.EnchantItemGroupsData;
import org.l2jmobius.gameserver.data.xml.EnchantItemOptionsData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;
import org.l2jmobius.gameserver.taskmanager.auto.AutoItemTaskManager;
import org.l2jmobius.loginserver.network.LoginClient;

import smartguard.api.integration.AbstractSmartClient;
import smartguard.spi.SmartGuardSPI;

public class BorinetUtil
{
	private static final Logger LOGGER = Logger.getLogger(BorinetUtil.class.getName());
	public static final SimpleDateFormat dataDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dataDateFormatKor = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
	public static final SimpleDateFormat dataDateFormatAuction = new SimpleDateFormat("yyyy년 MM월 dd일 HH시");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("a HH시 mm분");
	public static boolean chackFailed = false;
	public static Map<Integer, byte[]> _images;
	public static final int MILLIS_PER_DAY = 86400000; // 1 day in milliseconds
	
	private static HttpURLConnection con = null;
	private static BufferedReader in = null;
	
	public static String build_date = null;
	public int shutDown = 0;
	public static int total = 0;
	public static int online = 0;
	public static int offline = 0;
	public static int fishing = 0;
	
	// 이벤트
	public static boolean _WendEventStarted = false;
	public static boolean _MDEventStarted = false;
	
	BorinetUtil()
	{
		_images = new HashMap<>();
	}
	
	public static int serviceDays(boolean considerAllTime)
	{
		final long startTimeAll = 1627160400L;
		final long startTimeNew = 1711661380L;
		final long currentTimeInSeconds = System.currentTimeMillis() / 1000;
		
		long serviceDays = (currentTimeInSeconds - (considerAllTime ? startTimeAll : startTimeNew)) / 60 / 60 / 24;
		serviceDays = (long) Math.floor(serviceDays);
		serviceDays = Math.round(serviceDays);
		
		return (int) serviceDays + 1;
	}
	
	public boolean checkDB(Player player, String name)
	{
		String hwid = player.getHWID();
		String query = "SELECT name FROM event_hwid WHERE name = ? AND HWID = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, name);
			ps.setString(2, hwid);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					String check = rs.getString("name");
					if (check != null)
					{
						return false;
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "HWID를 검사할 수 없습니다. 계정 이름: " + name + ", HWID: " + hwid, e);
		}
		return true;
	}
	
	public long getReuseTime(Player player, String name)
	{
		String hwid = player.getHWID();
		String query = "SELECT reuse FROM event_hwid WHERE name = ? AND HWID = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, name);
			ps.setString(2, hwid);
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return rs.getLong("reuse"); // reuse 값을 반환
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "HWID를 검사할 수 없습니다. 계정 이름: " + name + ", HWID: " + hwid, e);
		}
		return 0L; // 데이터가 없거나 오류가 발생하면 0 반환
	}
	
	public void insertDB(Player player, String name, long reuse)
	{
		String query = "REPLACE INTO event_hwid (name, HWID, reuse) VALUES (?, ?, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, name);
			ps.setString(2, player.getHWID());
			ps.setLong(3, reuse);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "insertDB 데이터 저장에 실패했습니다. 계정 이름: " + name + ", HWID: " + player.getHWID());
		}
	}
	
	public void insertCustomMail(Player player, String subject, String bodyText, String itemIds)
	{
		String query = "INSERT INTO custom_mail (receiver, subject, message, items) VALUES (?, ?, ?, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, player.getObjectId());
			ps.setString(2, subject);
			ps.setString(3, bodyText);
			ps.setString(4, itemIds);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "커스텀 메일 데이터 저장에 실패했습니다. 수신자 ID: " + player.getObjectId() + ", 제목: " + subject + ", 메시지: " + bodyText + ", 아이템 IDs: " + itemIds);
		}
	}
	
	public static void insertEname(String eName)
	{
		// INSERT 쿼리로 데이터 삽입
		String replaceQuery = "REPLACE INTO event_name (id, name) VALUES (1, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement replaceStatement = con.prepareStatement(replaceQuery))
		{
			// 새로운 데이터 삽입
			replaceStatement.setString(1, eName);
			replaceStatement.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "이벤트 이름 데이터 저장에 실패했습니다. 이름: " + eName, e);
		}
	}
	
	public String getEventName()
	{
		String eName = "";
		if (BorinetTask.SpecialEvent())
		{
			String query = "SELECT name FROM event_name";
			
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement(query);
				ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					eName = rset.getString("name");
				}
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "이벤트 이름을 조회하는데 실패했습니다.", e);
			}
		}
		
		if (eName.isEmpty())
		{
			if (BorinetTask.WeekendCheck())
			{
				eName = "주말 배율";
			}
			else if (BorinetTask.MemorialDayCheck())
			{
				eName = "현충일";
			}
		}
		
		return eName;
	}
	
	public String sendEventName()
	{
		if (BorinetTask.ChristmasEvent())
		{
			return "크리스마스";
		}
		
		long currentTime = System.currentTimeMillis();
		BorinetTask task = BorinetTask.getInstance();
		
		if ((task.NewYearEventStart().getTimeInMillis() <= currentTime) && (task.NewYearEventEnd().getTimeInMillis() > currentTime))
		{
			return "새해";
		}
		
		switch (Config.CUSTOM_EVENT_NAME)
		{
			case 1:
				return "설날";
			case 2:
				return "추석";
			case 3:
				return "가정의 달";
			case 4:
				return Config.CUSTOM_EVENT_CUSTOM_NAME;
			default:
				return "";
		}
	}
	
	public void reloadEventData()
	{
		MultisellData.getInstance().load();
		EnchantItemOptionsData.getInstance().load();
		EnchantItemGroupsData.getInstance().load();
		CombinationItemsData.getInstance().load();
		EnchantItemData.getInstance().load();
	}
	
	private static final long BYTES_IN_GB = 1073741824;
	
	public double getUsedMemoryGB()
	{
		Runtime runtime = Runtime.getRuntime();
		double usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
		return Math.round((usedMemoryBytes / BYTES_IN_GB) * 10.0) / 10.0;
	}
	
	public long getTotalMemoryGB()
	{
		return (Runtime.getRuntime().maxMemory()) / BYTES_IN_GB;
	}
	
	public static int GetLisence()
	{
		int getLicenseTime = 0;
		if (!chackFailed)
		{
			try
			{
				URL getIp = new URL("http://110.45.203.91/license/" + CheckMyIp() + "/endTime.txt");
				con = (HttpURLConnection) getIp.openConnection();
				con.setRequestMethod("GET");
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				getLicenseTime = Integer.parseInt(in.readLine());
			}
			catch (Exception e)
			{
				chackFailed = true;
			}
		}
		closeSockets();
		return getLicenseTime;
	}
	
	public static String CheckMyIp()
	{
		String myIp = null;
		try
		{
			URL getIp = new URL("https://checkip.amazonaws.com/");
			con = (HttpURLConnection) getIp.openConnection();
			con.setRequestMethod("GET");
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			myIp = in.readLine();
		}
		catch (Exception e)
		{
			LOGGER.info("라이센스 검사를 할 수 없습니다. 인터넷연결을 확인하세요.");
		}
		closeSockets();
		return myIp;
	}
	
	public final static void closeSockets()
	{
		try
		{
			if (in != null)
			{
				in.close();
			}
		}
		catch (IOException e)
		{
			LOGGER.warning("IOException on close - " + e);
		}
	}
	
	public String CheckLicense()
	{
		String isCheck = "검사 중";
		
		if (!chackFailed)
		{
			int license = GetLisence();
			final int CurrentTimes = (int) (System.currentTimeMillis() / 1000L);
			shutDown = license - CurrentTimes;
			if (license == 0)
			{
				isCheck = "없음. 서버 종료 중";
				LOGGER.info("해당 PC의 라이센스를 보유하고 있지 않습니다.");
				LOGGER.info("https://l2wassub.org 에 방문하여 문의하시기 바랍니다.");
			}
			else if (CurrentTimes > license)
			{
				isCheck = "만료됨. 서버 종료 중";
				LOGGER.info("해당 PC의 라이센스 기간이 만료되었습니다.");
				LOGGER.info("https://l2wassub.org 에 방문하여 문의하시기 바랍니다.");
			}
			else
			{
				isCheck = "보유 중";
				if (!GameServer.checkLicense)
				{
					LOGGER.info("라이센스를 보유 중 입니다. 서버를 정상적으로 로딩합니다.");
				}
				return isCheck;
			}
			if (GameServer.checkLicense)
			{
				Shutdown.getInstance().scheduleLicense(5);
			}
		}
		return isCheck;
	}
	
	public static void jarFile()
	{
		try
		{
			final File jarName = Locator.getClassSource(GameServer.class);
			final JarFile jarFile = new JarFile(jarName);
			final Attributes attrs = jarFile.getManifest().getMainAttributes();
			build_date = attrs.getValue("Build-Date").split(" ")[0];
			jarFile.close();
		}
		catch (Exception e)
		{
			// Handled above.
		}
	}
	
	public static void getCounts()
	{
		total = 0;
		online = 0;
		offline = 0;
		fishing = 0;
		for (Player players : World.getInstance().getPlayers())
		{
			if (!players.isInOfflineMode())
			{
				total++;
			}
			if (players.isInOfflineMode())
			{
				offline++;
			}
			else if (players.isOnline())
			{
				online++;
				if (players.isFishing())
				{
					fishing++;
				}
			}
		}
		final int playerCount = World.getInstance().getPlayers().size() - offline;
		if (World.MAX_CONNECTED_COUNT < playerCount)
		{
			World.MAX_CONNECTED_COUNT = playerCount;
		}
	}
	
	private void sendMailToNewbie(Player player)
	{
		int msgId = IdManager.getInstance().getNextId();
		String topic = "환영합니다!";
		String body = Config.SERVER_NAME_KOR + "에 오신것을 환영하며, 선물을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
		String charMsg = "선물이 배달 왔어요. 어서 확인해 주세요!";
		String items = Config.NEWBIE_GIFT;
		
		Message msg = new Message(msgId, player.getObjectId(), topic, body, 7, MailType.PRIME_SHOP_GIFT, false);
		final List<ItemHolder> itemHolders = new ArrayList<>();
		for (String str : items.split(";"))
		{
			if (str.contains(","))
			{
				final String itemId = str.split(",")[0];
				final String itemCount = str.split(",")[1];
				if (Util.isDigit(itemId) && Util.isDigit(itemCount))
				{
					itemHolders.add(new ItemHolder(Integer.parseInt(itemId), Long.parseLong(itemCount)));
				}
			}
			else if (Util.isDigit(str))
			{
				itemHolders.add(new ItemHolder(Integer.parseInt(str), 1));
			}
		}
		if (!itemHolders.isEmpty())
		{
			final Mail attachments = msg.createAttachments();
			for (ItemHolder itemHolder : itemHolders)
			{
				attachments.addItem("신규자선물 메일발송", itemHolder.getId(), itemHolder.getCount(), null, null);
			}
		}
		MailManager.getInstance().sendMessage(msg);
		player.sendMessage("" + charMsg);
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, charMsg));
		player.getAccountVariables().set("신규자선물", 1);
		insertDB(player, "신규자선물", 0);
	}
	
	public void checkGift(Player player)
	{
		int checkGift = player.getAccountVariables().getInt("신규자선물", 0);
		if ((player.getLevel() < 11) && checkDB(player, "신규자선물") && (checkGift != 1))
		{
			sendMailToNewbie(player);
		}
	}
	
	public String[] getAutoFollow(Player player)
	{
		String leaderName = null;
		String range = "0";
		if (player.getVariables().getBoolean("자동따라가기", false))
		{
			String[] getLeader = player.getVariables().getString("자동따라가기설정").split(", ");
			leaderName = getLeader[0];
			range = getLeader[1];
		}
		
		return new String[]
		{
			leaderName,
			range
		};
	}
	
	public boolean canTeleport(Player player)
	{
		if ((player.getBlockCheckerArena() > -1) || player.isOnEvent())
		{
			player.sendMessage("이벤트 참가 중에는 텔레포트 할 수 없습니다.");
			return false;
		}
		if (player.isCursedWeaponEquipped() || (player.getReputation() < 0))
		{
			player.sendMessage("저주받은 무기를 소유하거나 카오틱 캐릭터 상태에서는 텔레포트 할 수 없습니다.");
			return false;
		}
		if (player.isOnBoat())
		{
			player.sendMessage("정기선 탑승 상태에서는 텔레포트 할 수 없습니다.");
			return false;
		}
		if (player.isInDuel())
		{
			player.sendMessage("결투 중에는 텔레포트 할 수 없습니다.");
			return false;
		}
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("올림피아드에 등록된 상태에서는 텔레포트 할 수 없습니다.");
			return false;
		}
		if (player.isInInstance())
		{
			player.sendMessage("인스턴스에 있는 동안에는 텔레포트 할 수 없습니다.");
			return false;
		}
		if (player.isInSiege() || player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendMessage("공성 중에는 텔레포트 할 수 없습니다.");
			return false;
		}
		if (player.isFishing())
		{
			player.sendMessage("낚시 중에는 텔레포트 할 수 없습니다.");
			return false;
		}
		return true;
	}
	
	public void AutoItem(Player player, boolean value)
	{
		if (value)
		{
			if (usingAutoItem(player))
			{
				AutoItemTaskManager.getInstance().removeAutoItem(player);
				player.getVariables().remove("자동아이템사용");
				player.getVariables().set("자동아이템사용_일시정지", true);
				player.sendMessage("자동 아이템 사용이 일시중지 되었습니다.");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동 아이템 사용이 일시중지 되었습니다."));
			}
		}
		else
		{
			if (player.getVariables().getBoolean("자동아이템사용_일시정지", false))
			{
				AutoItemTaskManager.getInstance().addAutoItem(player);
				player.getVariables().remove("자동아이템사용_일시정지");
				player.getVariables().set("자동아이템사용", true);
				player.sendMessage("자동 아이템 사용이 시작 되었습니다.");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동 아이템 사용이 시작 되었습니다."));
			}
		}
	}
	
	public static void sendEventMessage(boolean start, String eventName)
	{
		String message = start ? eventName + " 이벤트가 시작되었습니다. 커뮤니티 보드에서 상향된 배율을 확인하세요!" : eventName + " 이벤트가 종료되었습니다!";
		Broadcast.toAllOnlinePlayersOnScreen(message);
	}
	
	public static String uptime()
	{
		return BorinetUtil.dataDateFormatKor.format(GameServer.server_started);
	}
	
	public boolean checkAH(GameClient client)
	{
		String accountName = client.getAccountName();
		String hwid = getHWID(client);
		String[] getStrings = getAccountName(hwid);
		String name = getStrings[0];
		
		if ((name == null) || name.isEmpty())
		{
			showCharCreateFirstHtml(client, accountName);
			insertAccountHwid(accountName, hwid);
		}
		else if (!accountName.equals(name))
		{
			showCannotCreateCharHtml(client, name);
			return false;
		}
		
		return true;
	}
	
	public boolean createCharJoinHwid(GameClient client)
	{
		String accountName = client.getAccountName();
		String hwid = getHWID(client);
		String[] getStrings = getAccountName(hwid);
		String getName = getStrings[0];
		String getHwid = getStrings[1];
		
		if ((getHwid == null) || getHwid.isEmpty())
		{
			insertAccountHwid(accountName, hwid);
		}
		else if (Config.ALLOWED_CREATE_JOIN_HWID.contains(hwid))
		{
			return true;
		}
		else if (!accountName.equals(getName))
		{
			showCannotJoinCreateCharHtml(client, getName);
			return false;
		}
		
		return true;
	}
	
	private void showCharCreateFirstHtml(GameClient client, String accountName)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/html/mods/OnlyCreateChar/OnlyCreateChar-3.htm");
		html = html.replace("<?active_account?>", String.valueOf(accountName));
		client.close(new NpcHtmlMessage(html));
	}
	
	private void showCannotCreateCharHtml(GameClient client, String name)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/html/mods/OnlyCreateChar/OnlyCreateChar-4.htm");
		html = html.replace("<?active_account?>", String.valueOf(name));
		client.close(new NpcHtmlMessage(html));
	}
	
	private void showCannotJoinCreateCharHtml(GameClient client, String name)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/html/CannotLogin.htm");
		html = html.replace("<?active_account?>", String.valueOf(name));
		client.close(new NpcHtmlMessage(html));
	}
	
	public boolean isPlayerDropPenalty(Player player)
	{
		return player.isPlayable() && (player.getActiveWeaponItem() != null) && ((player.getActiveWeaponItem().getItemType() == WeaponType.BOW) || (player.getActiveWeaponItem().getItemType() == WeaponType.POLE) || (player.getActiveWeaponItem().getItemType() == WeaponType.BIGSWORD));
	}
	
	public String[] getAccountName(String var)
	{
		String account = "";
		String hwid = "";
		
		String query = "SELECT account, HWID FROM accounts_hwid WHERE HWID = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setString(1, var);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					account = rset.getString("account");
					hwid = rset.getString("HWID");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "Failed to get account name for HWID: " + var);
		}
		
		return new String[]
		{
			account,
			hwid
		};
	}
	
	public void insertAccountHwid(String account, String hwid)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO accounts_hwid (account, HWID) VALUES (?,?)"))
		{
			ps.setString(1, account);
			ps.setString(2, hwid);
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "account_hwid 데이터 저장에 실패했습니다. Account: " + account + ", HWID: " + hwid);
		}
	}
	
	public String getHWID(GameClient client)
	{
		String hwid = "";
		AbstractSmartClient smartClient = SmartGuardSPI.getSmartGuardService().getSessionManager().getClientByNativeHandle(client);
		if (smartClient != null)
		{
			hwid = smartClient.getClientData().getHwid().getPlain();
		}
		
		return hwid;
	}
	
	public String getHWIDL(LoginClient client)
	{
		String hwid = "";
		AbstractSmartClient smartClient = SmartGuardSPI.getSmartGuardService().getSessionManager().getClientByNativeHandle(client);
		if (smartClient != null)
		{
			hwid = smartClient.getClientData().getHwid().getPlain();
		}
		
		return hwid;
	}
	
	public void printSection(String section)
	{
		String s = "=[ " + section + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		LOGGER.info(s);
	}
	
	public void subChangeHideSkill(Player player)
	{
		player.skillEffectReload();
		
		if (player.getDeathPenaltyLevel() > 0)
		{
			player.castDeathPenaltyBuff(player.getDeathPenaltyLevel());
		}
		
		if (player.getDeathPenaltyLevel() > 0)
		{
			player.startPenalty(false);
		}
	}
	
	/**
	 * 가장 가까운 벽으로부터 'safeDistance' 단위 이상 떨어진 안전한 스폰 위치를 찾습니다.
	 * @param x 스폰 위치의 초기 x 좌표
	 * @param y 스폰 위치의 초기 y 좌표
	 * @param z 스폰 위치의 초기 z 좌표
	 * @param safeDistance 새로운 스폰 위치의 가장 가까운 벽으로부터의 최소 거리
	 * @return 안전한 스폰 위치를 포함하는 새로운 Location 객체
	 */
	public Location findSafeLocation(int x, int y, int z, int safeDistance)
	{
		// 새로운 Location 객체를 생성하고 초기 위치를 설정합니다.
		Location newLocation = new Location(x, y, z);
		// GeoEngine 인스턴스를 얻어옵니다.
		GeoEngine geoEngine = GeoEngine.getInstance();
		
		// 원래 스폰 위치 주변의 안전한 스폰 위치를 탐색합니다.
		for (int radius = safeDistance; radius < 5000; radius += safeDistance)
		{
			for (int angle = 0; angle < 360; angle += 15)
			{
				// 현재 반지름과 각도를 사용하여 새로운 위치를 계산합니다.
				int newX = x + (int) (radius * Math.cos(Math.toRadians(angle)));
				int newY = y + (int) (radius * Math.sin(Math.toRadians(angle)));
				// 새로운 위치의 높이를 가져옵니다.
				int newZ = geoEngine.getHeight(newX, newY, z);
				
				int checkX = z;
				int checkY = y;
				int checkZ = z;
				
				// 벽을 볼 수 있는지 확인하는 변수를 초기화합니다.
				boolean canSeeWall = false;
				// 새로운 위치에서 안전한 스폰 위치를 찾을 때까지 반복합니다.
				for (int checkDistance = 0; checkDistance <= safeDistance; checkDistance += 2)
				{
					// 현재 반지름과 각도에 따라 새로운 위치를 계산합니다.
					checkX = newX + (int) (checkDistance * Math.cos(Math.toRadians(angle)));
					checkY = newY + (int) (checkDistance * Math.sin(Math.toRadians(angle)));
					// 새로운 위치의 높이를 가져옵니다.
					checkZ = geoEngine.getSpawnHeight(checkX, checkY, newZ);
					
					// GeoEngine을 사용하여 벽을 볼 수 있는지와 이동 가능한지 확인합니다.
					if (!geoEngine.canSeeTarget(newX, newY, newZ, checkX, checkY, checkZ, null) //
						|| !geoEngine.canMoveToTarget(newX, newY, newZ, checkX, checkY, checkZ, null))
					{
						// 벽을 볼 수 있는 상황이거나 이동 불가능한 경우, canSeeWall을 true로 설정하고 반복문을 종료합니다.
						canSeeWall = true;
						break;
					}
				}
				
				// 안전한 스폰 위치를 찾았으면 해당 위치를 설정하고 반환합니다.
				if (!canSeeWall)
				{
					newLocation.setXYZ(checkX, checkY, checkZ);
					return newLocation;
				}
			}
		}
		// 안전한 스폰 위치를 찾지 못한 경우 원래 위치를 반환합니다.
		return newLocation;
	}
	
	public static int checkRift(Player player)
	{
		int val = 0;
		String query = "SELECT val FROM character_heavenly_rift WHERE charId = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					val = rset.getInt("val");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "character_heavenly_rift를 확인하는데 실패했습니다. 플레이어 ID: " + player.getObjectId());
		}
		return val;
	}
	
	public static void insertRift(Player player)
	{
		int count = checkRift(player) + 1;
		String query = "REPLACE INTO character_heavenly_rift(charId, val) VALUES (?, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, count);
			statement.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "플레이어의 리프트 상태를 삽입하는데 실패했습니다. 플레이어 ID: " + player.getObjectId());
		}
	}
	
	public static final Location[] MITHRIL_RAID_ZONE_POINT =
	{
		new Location(176979 + Rnd.get(-100, 100), -185250 + Rnd.get(-100, 100), -3720),
		new Location(176282 + Rnd.get(-100, 100), -184496 + Rnd.get(-100, 100), -3720),
		new Location(175521 + Rnd.get(-100, 100), -185088 + Rnd.get(-100, 100), -3720),
		new Location(175928 + Rnd.get(-100, 100), -186045 + Rnd.get(-100, 100), -3720)
	};
	
	public static int[] getSkillLevel()
	{
		int firstLevel;
		int secondLevel;
		
		do
		{
			firstLevel = Rnd.get(1, 9);
			secondLevel = Rnd.get(1, 9);
		}
		while (secondLevel == firstLevel);
		
		return new int[]
		{
			firstLevel,
			secondLevel
		};
	}
	
	public static String getSkillName(int var)
	{
		switch (var)
		{
			case 1:
				return "한손검";
			case 2:
				return "양손검";
			case 3:
				return "이도류";
			case 4:
				return "단검류";
			case 5:
				return "둔기류";
			case 6:
				return "격투";
			case 7:
				return "창";
			case 8:
				return "활";
			case 9:
				return "공격 마법";
			default:
				return "";
		}
	}
	
	public void sendEventMail(Player player, String topic, String body, String items, String evenName, boolean DBsave)
	{
		int msgId = IdManager.getInstance().getNextId();
		String charMsg = "선물이 배달 왔어요. 어서 확인해 주세요!";
		
		Message msg = new Message(msgId, player.getObjectId(), topic, body, 7, MailType.PRIME_SHOP_GIFT, false);
		final List<ItemHolder> itemHolders = new ArrayList<>();
		for (String str : items.split(";"))
		{
			if (str.contains(","))
			{
				final String itemId = str.split(",")[0];
				final String itemCount = str.split(",")[1];
				if (Util.isDigit(itemId) && Util.isDigit(itemCount))
				{
					itemHolders.add(new ItemHolder(Integer.parseInt(itemId), Long.parseLong(itemCount)));
				}
			}
			else if (Util.isDigit(str))
			{
				itemHolders.add(new ItemHolder(Integer.parseInt(str), 1));
			}
		}
		if (!itemHolders.isEmpty())
		{
			final Mail attachments = msg.createAttachments();
			for (ItemHolder itemHolder : itemHolders)
			{
				attachments.addItem(evenName + " 메일발송", itemHolder.getId(), itemHolder.getCount(), null, null);
			}
		}
		MailManager.getInstance().sendMessage(msg);
		player.sendMessage("" + charMsg);
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, charMsg));
		player.getAccountVariables().set(evenName, 1);
		if (DBsave)
		{
			insertDB(player, evenName, 0);
		}
	}
	
	public String getHtm(Player player, String fileName)
	{
		final HtmCache hc = HtmCache.getInstance();
		String content = hc.getHtm(player, fileName.startsWith("data/") ? fileName : "data/html/" + fileName);
		if (content == null)
		{
			content = hc.getHtm(player, "data/html/" + fileName);
		}
		return content;
	}
	
	public int checkClanId(Player target)
	{
		int clanId = 0;
		String query = "SELECT clanId FROM characters WHERE account_name = ? AND clanId > 0 AND clanId != 269357273";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setString(1, target.getAccountName());
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					clanId = rset.getInt("clanId");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "clanId를 검사할 수 없습니다. 계정 이름: " + target.getAccountName());
		}
		
		return clanId;
	}
	
	public boolean conditionClan(Player target, int clanId)
	{
		int playerClanId = checkClanId(target);
		if ((playerClanId > 0) && (playerClanId != clanId))
		{
			return true;
		}
		return false;
	}
	
	public static boolean SelfResurrectBuff(Player player)
	{
		boolean SRB = false;
		for (int buff : Config.SELF_LIST_RESURRECTION_BUFFS)
		{
			if (player.isAffectedBySkill(buff))
			{
				SRB = true;
			}
		}
		return SRB;
	}
	
	public static boolean isEventDay()
	{
		if ((BorinetTask.Month() == Calendar.FEBRUARY) && (BorinetTask.Days() == 14)) // 2월 14일 발렌타인 데이
		{
			return true;
		}
		if ((BorinetTask.Month() == Calendar.MARCH) && (BorinetTask.Days() == 1)) // 3월 1일 3.1절
		{
			return true;
		}
		if ((BorinetTask.Month() == Calendar.MARCH) && (BorinetTask.Days() == 14)) // 3월 13일 화이트 데이
		{
			return true;
		}
		if (((BorinetTask.Month() == Calendar.MARCH) && ((BorinetTask.Days() >= 1) && (BorinetTask.Days() <= 31))) || // 3월 1일부터 31일까지
			((BorinetTask.Month() == Calendar.APRIL) && ((BorinetTask.Days() >= 1) && (BorinetTask.Days() <= 7)))) // 4월 1일부터 7일까지
		{
			return true;
		}
		if ((BorinetTask.Month() == Calendar.APRIL) && (BorinetTask.Days() == 1)) // 4월 1일 만우절
		{
			return true;
		}
		if ((BorinetTask.Month() == Calendar.APRIL) && (BorinetTask.Days() == 14)) // 4월 14일 블랙 데이
		{
			return true;
		}
		if ((BorinetTask.Month() == Calendar.MAY) && (BorinetTask.Days() == 5)) // 5월 5일 어린이날
		{
			return true;
		}
		return false;
	}
	
	public static boolean usingAutoItem(Player player)
	{
		if (player.getVariables().getBoolean("자동아이템사용", false))
		{
			return true;
		}
		return false;
	}
	
	// 메시지 생성
	public String createMessage(String playerName, String oldItemName, int rewardItemId, int rewardCount, boolean isLucky)
	{
		String action = isLucky ? "님의 추가획득!" : "님이";
		String suffix = isLucky ? " 추가로 획득했습니다!" : " 획득했습니다!";
		
		String rewardName = ItemNameTable.getInstance().getItemNameKor(rewardItemId);
		String itemCountText = rewardCount > 1 ? "] " + rewardCount + "개를" : "]";
		String rewardText = rewardCount > 1 ? rewardName + itemCountText : KorNameUtil.getName(rewardName, "]을", "]를");
		
		return playerName + action + " [" + KorNameUtil.getName(oldItemName, "]을", "]를") + " 개봉하여 [" + rewardText + suffix;
	}
	
	// 모든 플레이어에게 메시지 브로드캐스트
	public void broadcastMessageToAllPlayers(String message)
	{
		Broadcast.toAllOnlinePlayersOnScreen(message);
	}
	
	// 튜토리얼 사망
	public void teleToDeathPoint(Player player)
	{
		// 부활 후 5초 지연 후 퀘스쳔 마크를 표시합니다.
		ThreadPool.schedule(() ->
		{
			if ((player.getLevel() < 37) && player.isInsideZone(ZoneId.PEACE))
			{
				player.sendMessage("튜토리얼 퀘스트 진행 중 사망하여 부활 후 사망장소로 이동이 가능합니다.");
				Broadcast.toPlayerScreenMessage(player, "최근 사망한 장소로 이동이 가능합니다. 물음표 아이콘을 클릭하세요!");
				player.sendPacket(QuestSound.getSound("ItemSound.quest_tutorial"));
				player.sendPacket(new TutorialShowQuestionMark(3, 0));
			}
		}, 5000);
	}
	
	public boolean isQuestActive(Player player)
	{
		// 포함할 퀘스트 목록에서 Q00255_Tutorial을 제외
		List<String> questList = Arrays.asList("Q00401_BorinetNewQuestPart1", "Q00402_BorinetNewQuestPart2", "Q00403_BorinetNewQuestPart3", "Q00404_BorinetNewQuestPart4", "Q11000_MoonKnight", "Q11001_TombsOfAncestors", "Q11002_HelpWithTempleRestoration", "Q11003_PerfectLeatherArmor1", "Q11004_PerfectLeatherArmor2", "Q11005_PerfectLeatherArmor3");
		
		// 현재 플레이어가 진행 중인 퀘스트가 있는지 확인
		return questList.stream().anyMatch(questId ->
		{
			QuestState qs = player.getQuestState(questId);
			return ((qs != null) && qs.isStarted());
		});
	}
	
	public void checkTeleCond(Player player, boolean html)
	{
		if (isQuestActive(player) && (player.getLevel() < 37) && player.isInsideZone(ZoneId.PEACE))
		{
			if (html)
			{
				player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtm(player, "data/html/guide/tutorial_TeleTo_DeadPoint.htm")));
			}
			else
			{
				String deathLocationString = player.getVariables().getString("DeathLocation", null);
				if (deathLocationString != null)
				{
					String[] coords = deathLocationString.split(",");
					int x = Integer.parseInt(coords[0]);
					int y = Integer.parseInt(coords[1]);
					int z = Integer.parseInt(coords[2]);
					Location deathLocation = new Location(x, y, z);
					player.teleToLocation(deathLocation);
					player.getVariables().remove("DeathLocation");
					player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
					Broadcast.toPlayerScreenMessage(player, "최근 사망한 장소로 이동합니다.");
				}
				else
				{
					player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
					Broadcast.toPlayerScreenMessageS(player, "최근 사망한 좌표가 존재하지 않습니다.");
				}
				return;
			}
		}
		else
		{
			if (player.getLevel() > 36)
			{
				player.getVariables().remove("DeathLocation");
				Broadcast.toPlayerScreenMessageS(player, "레벨이 초과하여 사망좌표를 제거합니다.");
			}
			else if (!isQuestActive(player))
			{
				player.getVariables().remove("DeathLocation");
				Broadcast.toPlayerScreenMessageS(player, "퀘스트 진행 중이 아니므로 사망좌표를 제거합니다.");
			}
			else
			{
				Broadcast.toPlayerScreenMessageS(player, "조건에 맞지않아 이동할 수 없습니다.");
			}
		}
	}
	
	public boolean isWinterActive()
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = LocalDateTime.of(now.getYear(), Month.DECEMBER, 1, 0, 0);
		LocalDateTime end = LocalDateTime.of(now.getMonthValue() >= Month.DECEMBER.getValue() ? now.getYear() + 1 : now.getYear(), Month.FEBRUARY, 15, 23, 59);
		
		return now.isAfter(start) && now.isBefore(end);
	}
	
	public static BorinetUtil getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BorinetUtil INSTANCE = new BorinetUtil();
	}
}