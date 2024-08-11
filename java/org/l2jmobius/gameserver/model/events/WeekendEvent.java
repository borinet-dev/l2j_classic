package org.l2jmobius.gameserver.model.events;

import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.CombinationItemsData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class WeekendEvent
{
	private static final Logger LOGGER = Logger.getLogger(WeekendEvent.class.getName());
	
	protected WeekendEvent()
	{
		LOGGER.info("주말 이벤트: 스크립트 엔진을 로드하였습니다.");
		
		if (BorinetTask.WeekendCheck())
		{
			startEvent();
		}
		
		ThreadPool.scheduleAtFixedRate(this::startEvent, BorinetTask.WeekendStartDelay(), BorinetUtil.MILLIS_PER_DAY);
		ThreadPool.scheduleAtFixedRate(this::stopEvent, BorinetTask.WeekendEndDelay(), BorinetUtil.MILLIS_PER_DAY);
	}
	
	public void startEvent()
	{
		if (!BorinetTask.SpecialEvent() && !BorinetUtil._WendEventStarted && !BorinetUtil._MDEventStarted)
		{
			if (BorinetTask.WeekendCheck())
			{
				BorinetUtil._WendEventStarted = true;
				MultisellData.getInstance().load();
				CombinationItemsData.getInstance().load();
				BorinetUtil.sendEventMessage(true, "주말배율");
				LOGGER.info("주말배율 이벤트: 주말배율 이벤트가 시작 되었습니다.");
			}
		}
	}
	
	public void stopEvent()
	{
		if (!BorinetTask.SpecialEvent() && BorinetUtil._WendEventStarted && !BorinetUtil._MDEventStarted)
		{
			if (BorinetTask.Day() == Calendar.MONDAY)
			{
				BorinetUtil._WendEventStarted = false;
				MultisellData.getInstance().load();
				CombinationItemsData.getInstance().load();
				BorinetUtil.sendEventMessage(false, "주말배율");
				LOGGER.info("주말배율 이벤트: 주말배율 이벤트가 종료 되었습니다.");
			}
		}
	}
	
	public static WeekendEvent getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final WeekendEvent INSTANCE = new WeekendEvent();
	}
}
