package org.l2jmobius.gameserver.model.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

public class SpecialEvents
{
	private static final Logger LOGGER = Logger.getLogger(SpecialEvents.class.getName());
	public static boolean _SpecialEventStarted = false;
	public static boolean _ChristmasStarted = false;
	
	private static final int NPC = Config.CUSTOM_EVENT_NAME == 3 ? 40023 : (Config.CUSTOM_EVENT_NAME == 4 ? Config.CUSTOM_EVENT_NPC_ID : 34330);
	private static List<Npc> _npclist;
	
	protected SpecialEvents()
	{
		LOGGER.info("커스텀 이벤트: 스크립트 엔진을 로드하였습니다.");
		
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH);
		if (month == Calendar.DECEMBER)
		{
			NewYear();
			ChristmasEvent();
		}
		if (Config.CUSTOM_EVENT_ENABLE && ((month == (Config.CUSTOM_EVENT_START_MONTH - 1)) || (month == (Config.CUSTOM_EVENT_LAST_MONTH - 1))))
		{
			EventSpecial();
		}
		if (month == Calendar.JANUARY)
		{
			NewYearStart();
			NewYearEvent();
		}
	}
	
	private void EventSpecial()
	{
		final long currentTime = System.currentTimeMillis();
		if (BorinetTask.getInstance().SpecialEventStart().getTimeInMillis() > currentTime)
		{
			ThreadPool.schedule(this::startEvent, BorinetTask.getInstance().SpecialEventStart().getTimeInMillis() - currentTime);
		}
		else if ((BorinetTask.getInstance().SpecialEventStart().getTimeInMillis() <= currentTime) && (BorinetTask.getInstance().SpecialEventEnd().getTimeInMillis() >= currentTime))
		{
			if (Config.CUSTOM_EVENT_ENABLE)
			{
				npcSpawn();
				_SpecialEventStarted = true;
				BorinetUtil.getInstance().reloadEventData();
				ThreadPool.schedule(this::stopEvent, BorinetTask.getInstance().SpecialEventEnd().getTimeInMillis() - currentTime);
				
				BorinetUtil.insertEname(BorinetUtil.getInstance().sendEventName());
				
				if (BorinetUtil.getInstance().getEventName().equals("추석"))
				{
					giftReset();
				}
			}
		}
	}
	
	private void ChristmasEvent()
	{
		final long currentTime = System.currentTimeMillis();
		if (BorinetTask.getInstance().ChristmasEventStart().getTimeInMillis() > currentTime)
		{
			ThreadPool.schedule(this::startChristmasEvent, BorinetTask.getInstance().ChristmasEventStart().getTimeInMillis() - currentTime);
		}
		else if ((BorinetTask.getInstance().ChristmasEventStart().getTimeInMillis() <= System.currentTimeMillis()) && (BorinetTask.getInstance().ChristmasEventEnd().getTimeInMillis() > System.currentTimeMillis()))
		{
			_SpecialEventStarted = true;
			_ChristmasStarted = true;
			BorinetUtil.getInstance().reloadEventData();
			ThreadPool.schedule(this::stopChristmasEvent, BorinetTask.getInstance().ChristmasEventEnd().getTimeInMillis() - currentTime);
			BorinetUtil.insertEname(BorinetUtil.getInstance().sendEventName());
		}
	}
	
	private void NewYear()
	{
		final Calendar newYear = Calendar.getInstance();
		newYear.set(Calendar.YEAR, newYear.get(Calendar.YEAR));
		newYear.set(Calendar.MONTH, Calendar.JANUARY);
		newYear.set(Calendar.DAY_OF_MONTH, 1);
		newYear.set(Calendar.HOUR_OF_DAY, 0);
		newYear.set(Calendar.MINUTE, 0);
		newYear.set(Calendar.SECOND, 0);
		newYear.set(Calendar.MILLISECOND, 0);
		
		while (getDelay(newYear) < 0)
		{
			newYear.set(Calendar.YEAR, newYear.get(Calendar.YEAR) + 1);
		}
		
		ThreadPool.schedule(this::NewYearStart, getDelay(newYear) + 10000);
		ThreadPool.schedule(new NewYearAnnouncer(newYear.get(Calendar.YEAR) + "년이 밝았습니다! 모두 새해 복 많이 받으시고, " + Config.SERVER_NAME_KOR + "과 함께 즐거운 시간 되세요!"), getDelay(newYear));
		newYear.add(Calendar.SECOND, -1);
		ThreadPool.schedule(new NewYearAnnouncer("새해 카운트 다운: 1!"), getDelay(newYear));
		newYear.add(Calendar.SECOND, -1);
		ThreadPool.schedule(new NewYearAnnouncer("새해 카운트 다운: 2!"), getDelay(newYear));
		newYear.add(Calendar.SECOND, -1);
		ThreadPool.schedule(new NewYearAnnouncer("새해 카운트 다운: 3!"), getDelay(newYear));
		newYear.add(Calendar.SECOND, -1);
		ThreadPool.schedule(new NewYearAnnouncer("새해 카운트 다운: 4!"), getDelay(newYear));
		newYear.add(Calendar.SECOND, -1);
		ThreadPool.schedule(new NewYearAnnouncer("새해 카운트 다운: 5!"), getDelay(newYear));
	}
	
	private long getDelay(Calendar c)
	{
		final long currentTime = System.currentTimeMillis();
		return c.getTime().getTime() - currentTime;
	}
	
	private void startEvent()
	{
		final long currentTime = System.currentTimeMillis();
		_SpecialEventStarted = true;
		BorinetUtil.getInstance().reloadEventData();
		
		BorinetUtil.insertEname(BorinetUtil.getInstance().sendEventName());
		startEventMessage(1, BorinetUtil.getInstance().sendEventName());
		
		if (BorinetUtil.getInstance().getEventName().equals("추석"))
		{
			giftReset();
		}
		ThreadPool.schedule(this::stopEvent, BorinetTask.getInstance().SpecialEventEnd().getTimeInMillis() - currentTime);
	}
	
	private void stopEvent()
	{
		endEventMessage();
		_SpecialEventStarted = false;
		BorinetUtil.getInstance().reloadEventData();
	}
	
	private void NewYearEvent()
	{
		final long currentTime = System.currentTimeMillis();
		if (BorinetTask.getInstance().NewYearEventStart().getTimeInMillis() > currentTime)
		{
			ThreadPool.schedule(this::NewYearStart, BorinetTask.getInstance().NewYearEventStart().getTimeInMillis() - currentTime);
		}
		else if ((BorinetTask.getInstance().NewYearEventStart().getTimeInMillis() <= System.currentTimeMillis()) && (BorinetTask.getInstance().NewYearEventEnd().getTimeInMillis() > System.currentTimeMillis()))
		{
			npcSpawn();
			_SpecialEventStarted = true;
			BorinetUtil.getInstance().reloadEventData();
			ThreadPool.schedule(this::NewYearStop, BorinetTask.getInstance().NewYearEventEnd().getTimeInMillis() - currentTime);
			BorinetUtil.insertEname(BorinetUtil.getInstance().sendEventName());
		}
	}
	
	private void NewYearStart()
	{
		final long currentTime = System.currentTimeMillis();
		_SpecialEventStarted = true;
		BorinetUtil.getInstance().reloadEventData();
		BorinetUtil.insertEname(BorinetUtil.getInstance().sendEventName());
		startEventMessage(2, BorinetUtil.getInstance().sendEventName());
		ThreadPool.schedule(this::NewYearStop, BorinetTask.getInstance().NewYearEventEnd().getTimeInMillis() - currentTime);
	}
	
	private void NewYearStop()
	{
		endEventMessage();
		_SpecialEventStarted = false;
		BorinetUtil.getInstance().reloadEventData();
	}
	
	private void startChristmasEvent()
	{
		final long currentTime = System.currentTimeMillis();
		_SpecialEventStarted = true;
		_ChristmasStarted = true;
		BorinetUtil.getInstance().reloadEventData();
		BorinetUtil.insertEname(BorinetUtil.getInstance().sendEventName());
		startEventMessage(3, BorinetUtil.getInstance().sendEventName());
		ThreadPool.schedule(this::stopEvent, BorinetTask.getInstance().ChristmasEventEnd().getTimeInMillis() - currentTime);
	}
	
	private void stopChristmasEvent()
	{
		endEventMessage();
		_SpecialEventStarted = false;
		_ChristmasStarted = false;
		BorinetUtil.getInstance().reloadEventData();
	}
	
	private class NewYearAnnouncer implements Runnable
	{
		private final String message;
		
		private NewYearAnnouncer(String message)
		{
			this.message = message;
		}
		
		@Override
		public void run()
		{
			for (Player player : World.getInstance().getPlayers())
			{
				player.sendPacket(new ExShowScreenMessage(message, 10000));
			}
			
			if (message.length() == 1)
			{
				return;
			}
		}
	}
	
	private static void eventEnd()
	{
		if (_npclist != null)
		{
			for (Npc npc : _npclist)
			{
				if (npc != null)
				{
					npc.deleteMe();
				}
			}
			_npclist.clear();
		}
	}
	
	private static void npcSpawn()
	{
		_npclist = new ArrayList<>();
		
		Npc npc1 = AbstractScript.addSpawn(NPC, 83452, 148622, -3457, 32324, false, 0); // <!-- 기란 -->
		Npc npc2 = AbstractScript.addSpawn(NPC, -83810, 243137, -3678, 14970, false, 0); // <!-- 말섬 -->
		Npc npc3 = AbstractScript.addSpawn(NPC, 18272, 145158, -3064, 6316, false, 0); // <!-- 디온 -->
		Npc npc4 = AbstractScript.addSpawn(NPC, -14587, 124003, -3112, 53029, false, 0); // <!-- 글성 -->
		Npc npc5 = AbstractScript.addSpawn(NPC, 117165, 76746, -2688, 38521, false, 0); // <!-- 사냥꾼 -->
		Npc npc6 = AbstractScript.addSpawn(NPC, 147449, 25875, -2008, 15813, false, 0); // <!-- 아덴 -->
		Npc npc7 = AbstractScript.addSpawn(NPC, 82752, 53573, -1488, 32767, false, 0); // <!-- 오렌 -->
		Npc npc8 = AbstractScript.addSpawn(NPC, -80782, 150146, -3040, 32257, false, 0); // <!-- 글루딘 -->
		Npc npc9 = AbstractScript.addSpawn(NPC, 17723, 170096, -3504, 36948, false, 0); // <!-- 플로란 -->
		Npc npc10 = AbstractScript.addSpawn(NPC, 147689, -55393, -2728, 49356, false, 0); // <!-- 고다드 -->
		
		_npclist.add(npc1);
		_npclist.add(npc2);
		_npclist.add(npc3);
		_npclist.add(npc4);
		_npclist.add(npc5);
		_npclist.add(npc6);
		_npclist.add(npc7);
		_npclist.add(npc8);
		_npclist.add(npc9);
		_npclist.add(npc10);
	}
	
	public static void startEventMessage(int event, String eName)
	{
		npcSpawn();
		
		Broadcast.toAllOnlinePlayersOnScreenS(eName + " 이벤트가 시작되었습니다. 커뮤니티 보드를 통하여 상향된 배율을 확인하세요!");
		for (Player player : World.getInstance().getPlayers())
		{
			String AMPA = "오후";
			
			player.sendMessage("이벤트 NPC를 찾아가세요!");
			player.sendMessage("루나 구매시 30% 보너스와 20% 할인된 아이템가격!");
			player.sendMessage("아이템 뽑기 및 강화 확률이 증가합니다.");
			switch (event)
			{
				case 1:
				{
					if ((Config.CUSTOM_EVENT_LAST_TIME >= 1) && (Config.CUSTOM_EVENT_LAST_TIME < 12))
					{
						AMPA = "오전";
					}
					player.sendMessage("지금부터 " + (Config.CUSTOM_EVENT_LAST_MONTH) + "월 " + Config.CUSTOM_EVENT_LAST_DAY + "일 " + AMPA + " " + Config.CUSTOM_EVENT_LAST_TIME + "시 까지 진행됩니다!");
					break;
				}
				case 2:
				{
					if ((Config.NEWYEAR_EVENT_STOP_TIME >= 1) && (Config.NEWYEAR_EVENT_STOP_TIME < 12))
					{
						AMPA = "오전";
					}
					player.sendMessage("지금부터 " + Config.NEWYEAR_EVENT_STOP_DAY + "일 " + AMPA + " " + Config.NEWYEAR_EVENT_STOP_TIME + "시 까지 진행됩니다!");
					break;
				}
				case 3:
				{
					if ((Config.CHRISTMAS_EVENT_STOP_TIME >= 1) && (Config.CHRISTMAS_EVENT_STOP_TIME < 12))
					{
						AMPA = "오전";
					}
					player.sendMessage("지금부터 " + Config.CHRISTMAS_EVENT_STOP_DAY + "일 " + AMPA + " " + Config.CHRISTMAS_EVENT_STOP_TIME + "시 까지 진행됩니다!");
					break;
				}
			}
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, eName + " 이벤트가 시작되었습니다. 커뮤니티 보드를 통하여 상향된 배율을 확인하세요!"));
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "이벤트 NPC를 찾아가세요!"));
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "루나 구매시 30% 보너스와 20% 할인된 아이템가격!"));
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "아이템 뽑기 및 강화 확률이 증가합니다."));
			switch (event)
			{
				case 1:
				{
					if ((Config.CUSTOM_EVENT_LAST_TIME >= 1) && (Config.CUSTOM_EVENT_LAST_TIME < 12))
					{
						AMPA = "오전";
					}
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "지금부터 " + (Config.CUSTOM_EVENT_LAST_MONTH) + "월 " + Config.CUSTOM_EVENT_LAST_DAY + "일 " + AMPA + " " + Config.CUSTOM_EVENT_LAST_TIME + "시 까지  진행됩니다!"));
					break;
				}
				case 2:
				{
					if ((Config.NEWYEAR_EVENT_STOP_TIME >= 1) && (Config.NEWYEAR_EVENT_STOP_TIME < 12))
					{
						AMPA = "오전";
					}
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "지금부터 " + Config.NEWYEAR_EVENT_STOP_DAY + "일 " + AMPA + " " + Config.NEWYEAR_EVENT_STOP_TIME + "시 까지 진행됩니다!"));
					break;
				}
				case 3:
				{
					if ((Config.CHRISTMAS_EVENT_STOP_TIME >= 1) && (Config.CHRISTMAS_EVENT_STOP_TIME < 12))
					{
						AMPA = "오전";
					}
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "지금부터 " + Config.CHRISTMAS_EVENT_STOP_DAY + "일 " + AMPA + " " + Config.CHRISTMAS_EVENT_STOP_TIME + "시 까지 진행됩니다!"));
					break;
				}
			}
			resetVar(player);
		}
	}
	
	public static void endEventMessage()
	{
		String eName = BorinetUtil.getInstance().getEventName();
		
		for (Player player : World.getInstance().getPlayers())
		{
			player.sendMessage(eName + " 이벤트가 종료되었습니다!");
			player.sendPacket(new ExShowScreenMessage(eName + " 이벤트가 종료되었습니다!", 10000));
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, eName + " 이벤트가 종료되었습니다!"));
			resetVar(player);
		}
		eventEnd();
		
		if (BorinetUtil.getInstance().getEventName().equals("추석"))
		{
			for (Player player : World.getInstance().getPlayers())
			{
				player.stopSkillEffects(SkillFinishType.REMOVED, 30296);
				player.getVariables().remove("CHUSEOK_BUFF");
			}
			try (Connection con = DatabaseFactory.getConnection();
				Statement statement = con.createStatement())
			{
				statement.executeUpdate("DELETE FROM character_skills_save WHERE skill_id = '30296';");
				statement.executeUpdate("DELETE FROM character_variables WHERE var = 'CHUSEOK_BUFF';");
			}
			catch (SQLException e)
			{
				LOGGER.warning(e.toString());
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM event_name"))
			{
				ps.execute();
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public static void resetVar(Player player)
	{
		player.getAccountVariables().remove("묵찌빠");
		player.getAccountVariables().remove("CUSTOM_EVENT_GIFT");
		player.getAccountVariables().remove("CUSTOM_EVENT_WEAPON_D");
		player.getAccountVariables().remove("CUSTOM_EVENT_ARMOR_D");
		player.getAccountVariables().remove("CUSTOM_EVENT_WEAPON_C");
		player.getAccountVariables().remove("CUSTOM_EVENT_ARMOR_C");
		player.getAccountVariables().remove("CUSTOM_EVENT_WEAPON_B");
		player.getAccountVariables().remove("CUSTOM_EVENT_ARMOR_B");
		player.getAccountVariables().remove("CUSTOM_EVENT_GIFT_SCROLL_TIMES");
		player.getAccountVariables().remove("CUSTOM_EVENT_BOX");
		player.getAccountVariables().remove("CHUSEOK_ITEM");
		
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("DELETE from account_gsdata WHERE var = '묵찌빠';");
			statement.executeUpdate("DELETE from account_gsdata WHERE var LIKE '%CUSTOM_EVENT_%';");
			statement.executeUpdate("DELETE from event_hwid WHERE name LIKE '%CUSTOM_EVENT_%';");
			statement.executeUpdate("DELETE from account_gsdata WHERE var = 'CHUSEOK_ITEM';");
		}
		catch (Exception e)
		{
			LOGGER.warning("커스텀이벤트 데이터베이스 정리 오류" + e);
		}
	}
	
	private void giftReset()
	{
		final long currentTime = System.currentTimeMillis();
		// Schedule reset everyday at 6:30.
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onGiftReset, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
	}
	
	private void onGiftReset()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			player.getAccountVariables().remove("CHUSEOK_ITEM");
		}
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("DELETE FROM account_gsdata WHERE var = 'CHUSEOK_ITEM';");
		}
		catch (Exception e)
		{
			LOGGER.warning("이벤트 데이터베이스 정리 오류" + e);
		}
	}
	
	public static SpecialEvents getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SpecialEvents INSTANCE = new SpecialEvents();
	}
}