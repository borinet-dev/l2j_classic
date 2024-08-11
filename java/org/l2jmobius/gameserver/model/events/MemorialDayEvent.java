package org.l2jmobius.gameserver.model.events;

import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.CombinationItemsData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

public class MemorialDayEvent
{
	private static final Logger LOGGER = Logger.getLogger(MemorialDayEvent.class.getName());
	
	protected MemorialDayEvent()
	{
		if ((BorinetTask.Month() == Calendar.JUNE) && (BorinetTask.Days() < 7))
		{
			LOGGER.info("현충일 이벤트: 스크립트 엔진을 로드하였습니다.");
			
			if (BorinetTask.MemorialDayCheck())
			{
				startEvent();
			}
			
			ThreadPool.scheduleAtFixedRate(this::startEvent, BorinetTask.MemorialDayStartDelay(), BorinetUtil.MILLIS_PER_DAY);
			ThreadPool.scheduleAtFixedRate(this::stopEvent, BorinetTask.MemorialDayEndDelay(), BorinetUtil.MILLIS_PER_DAY);
		}
	}
	
	private void startEvent()
	{
		if (!BorinetTask.SpecialEvent() && !BorinetUtil._MDEventStarted)
		{
			if (BorinetTask.MemorialDayCheck())
			{
				if (BorinetTask.WeekendCheck())
				{
					BorinetUtil._WendEventStarted = false;
					MultisellData.getInstance().load();
					CombinationItemsData.getInstance().load();
					BorinetUtil.sendEventMessage(false, "주말배율");
					LOGGER.info("주말 배율 이벤트: 주말배율 이벤트가 종료 되었습니다.");
				}
				
				BorinetUtil._MDEventStarted = true;
				BorinetUtil.getInstance().reloadEventData();
				
				Broadcast.toAllOnlinePlayersOnScreenS("현충일 이벤트가 시작되었습니다. 커뮤니티 보드를 통하여 상향된 배율을 확인하세요!");
				for (Player player : World.getInstance().getPlayers())
				{
					player.sendMessage("루나 구매시 30% 보너스와 20% 할인된 아이템가격!");
					player.sendMessage("아이템 뽑기 및 강화 확률이 증가합니다.");
					
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "현충일 이벤트가 시작되었습니다. 커뮤니티 보드를 통하여 상향된 배율을 확인하세요!"));
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "루나 구매시 30% 보너스와 20% 할인된 아이템가격!"));
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "아이템 뽑기 및 강화 확률이 증가합니다."));
				}
				LOGGER.info("커스텀 이벤트: " + "현충일  이벤트가 시작 되었습니다.");
			}
		}
	}
	
	private void stopEvent()
	{
		if (!BorinetTask.SpecialEvent() && BorinetUtil._MDEventStarted)
		{
			if (!BorinetTask.MemorialDayCheck())
			{
				BorinetUtil._MDEventStarted = false;
				BorinetUtil.sendEventMessage(false, "현충일");
				BorinetUtil.getInstance().reloadEventData();
				LOGGER.info("현충일 이벤트: 현충일 이벤트가 종료 되었습니다.");
				WeekendEvent.getInstance().startEvent();
			}
		}
	}
	
	public static MemorialDayEvent getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MemorialDayEvent INSTANCE = new MemorialDayEvent();
	}
}
