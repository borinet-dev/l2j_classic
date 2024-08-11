package org.l2jmobius.gameserver.util;

import java.util.Calendar;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.events.SpecialEvents;

public class BorinetTask
{
	private static final int RACE_START_TIME = Config.RACE_START_TIME;
	private static final int START_TIME_LOW = Config.TvT_START_TIME_LOW;
	private static final int START_TIME_MIDDLE = Config.TvT_START_TIME_MIDDLE;
	private static final int START_TIME_HIGH = Config.TvT_START_TIME_HIGH;
	public static boolean _isActive = false;
	public int _boatCycle = 1;
	
	BorinetTask()
	{
	}
	
	public static long RaceEventStart()
	{
		final Calendar starttime = Calendar.getInstance();
		// Daily task to start event at 17:00.
		if (starttime.get(Calendar.HOUR_OF_DAY) >= RACE_START_TIME)
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		starttime.set(Calendar.HOUR_OF_DAY, RACE_START_TIME);
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 0);
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - System.currentTimeMillis());
		
		return startDelay;
	}
	
	public static long TvTLowEventStart()
	{
		final Calendar starttime = Calendar.getInstance();
		// Daily task to start event at 19:00.
		if (starttime.get(Calendar.HOUR_OF_DAY) >= START_TIME_LOW)
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		starttime.set(Calendar.HOUR_OF_DAY, START_TIME_LOW);
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 0);
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - System.currentTimeMillis());
		
		return startDelay;
	}
	
	public static long TvTMiddleEventStart()
	{
		final Calendar starttime = Calendar.getInstance();
		// Daily task to start event at 20:00.
		if (starttime.get(Calendar.HOUR_OF_DAY) >= START_TIME_MIDDLE)
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		starttime.set(Calendar.HOUR_OF_DAY, START_TIME_MIDDLE);
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 0);
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - System.currentTimeMillis());
		
		return startDelay;
	}
	
	public static long TvTHighEventStart()
	{
		final Calendar starttime = Calendar.getInstance();
		// Daily task to start event at 21:00.
		if (starttime.get(Calendar.HOUR_OF_DAY) >= START_TIME_HIGH)
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		starttime.set(Calendar.HOUR_OF_DAY, START_TIME_HIGH);
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 0);
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - System.currentTimeMillis());
		
		return startDelay;
	}
	
	public Calendar SpecialEventStart()
	{
		final Calendar startEvent = Calendar.getInstance();
		startEvent.set(Calendar.YEAR, Config.CUSTOM_EVENT_START_YEAR);
		startEvent.set(Calendar.MONTH, Config.CUSTOM_EVENT_START_MONTH - 1);
		startEvent.set(Calendar.DAY_OF_MONTH, Config.CUSTOM_EVENT_START_DAY);
		startEvent.set(Calendar.HOUR_OF_DAY, Config.CUSTOM_EVENT_START_TIME);
		startEvent.set(Calendar.MINUTE, 0);
		startEvent.set(Calendar.SECOND, 0);
		
		return startEvent;
	}
	
	public Calendar SpecialEventEnd()
	{
		final Calendar endtEvent = Calendar.getInstance();
		endtEvent.set(Calendar.YEAR, Config.CUSTOM_EVENT_LAST_YEAR);
		endtEvent.set(Calendar.MONTH, Config.CUSTOM_EVENT_LAST_MONTH - 1);
		endtEvent.set(Calendar.DAY_OF_MONTH, Config.CUSTOM_EVENT_LAST_DAY);
		endtEvent.set(Calendar.HOUR_OF_DAY, Config.CUSTOM_EVENT_LAST_TIME);
		endtEvent.set(Calendar.MINUTE, 0);
		endtEvent.set(Calendar.SECOND, 0);
		
		return endtEvent;
	}
	
	public Calendar ChristmasEventStart()
	{
		final Calendar startEvent = Calendar.getInstance();
		startEvent.set(Calendar.MONTH, Calendar.DECEMBER);
		startEvent.set(Calendar.DAY_OF_MONTH, Config.CHRISTMAS_EVENT_START_DAY);
		startEvent.set(Calendar.HOUR_OF_DAY, Config.CHRISTMAS_EVENT_START_TIME);
		startEvent.set(Calendar.MINUTE, 0);
		startEvent.set(Calendar.SECOND, 0);
		
		return startEvent;
	}
	
	public Calendar ChristmasEventEnd()
	{
		final Calendar endtEvent = Calendar.getInstance();
		endtEvent.set(Calendar.MONTH, Calendar.DECEMBER);
		endtEvent.set(Calendar.DAY_OF_MONTH, Config.CHRISTMAS_EVENT_STOP_DAY);
		endtEvent.set(Calendar.HOUR_OF_DAY, Config.CHRISTMAS_EVENT_STOP_TIME);
		endtEvent.set(Calendar.MINUTE, 0);
		endtEvent.set(Calendar.SECOND, 0);
		
		return endtEvent;
	}
	
	public Calendar NewYearEventStart()
	{
		final Calendar startEvent = Calendar.getInstance();
		startEvent.set(Calendar.MONTH, Calendar.JANUARY);
		startEvent.set(Calendar.DAY_OF_MONTH, 1);
		startEvent.set(Calendar.HOUR_OF_DAY, 0);
		startEvent.set(Calendar.MINUTE, 0);
		startEvent.set(Calendar.SECOND, 0);
		
		return startEvent;
	}
	
	public Calendar NewYearEventEnd()
	{
		final Calendar endtEvent = Calendar.getInstance();
		endtEvent.set(Calendar.MONTH, Calendar.JANUARY);
		endtEvent.set(Calendar.DAY_OF_MONTH, Config.NEWYEAR_EVENT_STOP_DAY);
		endtEvent.set(Calendar.HOUR_OF_DAY, Config.NEWYEAR_EVENT_STOP_TIME);
		endtEvent.set(Calendar.MINUTE, 0);
		endtEvent.set(Calendar.SECOND, 0);
		
		return endtEvent;
	}
	
	public boolean GoldenPig()
	{
		boolean GOLEDN_PIG = false;
		final long currentTime = System.currentTimeMillis();
		
		if ((GoldenPigStart().getTimeInMillis() < currentTime) && (GoldenPigEnd().getTimeInMillis() > currentTime))
		{
			GOLEDN_PIG = true;
		}
		return GOLEDN_PIG;
	}
	
	public Calendar GoldenPigStart()
	{
		final Calendar startEvent = Calendar.getInstance();
		startEvent.set(Calendar.YEAR, Config.GOLDEN_PIG_START_YEAR);
		startEvent.set(Calendar.MONTH, Config.GOLDEN_PIG_START_MONTH - 1);
		startEvent.set(Calendar.DAY_OF_MONTH, Config.GOLDEN_PIG_START_DAY);
		startEvent.set(Calendar.HOUR_OF_DAY, Config.GOLDEN_PIG_START_TIME);
		startEvent.set(Calendar.MINUTE, 0);
		startEvent.set(Calendar.SECOND, 0);
		
		return startEvent;
	}
	
	public Calendar GoldenPigEnd()
	{
		final Calendar endtEvent = Calendar.getInstance();
		endtEvent.set(Calendar.YEAR, Config.GOLDEN_PIG_LAST_YEAR);
		endtEvent.set(Calendar.MONTH, Config.GOLDEN_PIG_LAST_MONTH - 1);
		endtEvent.set(Calendar.DAY_OF_MONTH, Config.GOLDEN_PIG_LAST_DAY);
		endtEvent.set(Calendar.HOUR_OF_DAY, Config.GOLDEN_PIG_LAST_TIME);
		endtEvent.set(Calendar.MINUTE, 0);
		endtEvent.set(Calendar.SECOND, 0);
		
		return endtEvent;
	}
	
	public static int Year()
	{
		final int month = Calendar.getInstance().get(Calendar.YEAR);
		return month;
	}
	
	public static int Month()
	{
		final int month = Calendar.getInstance().get(Calendar.MONTH);
		return month;
	}
	
	public static int Days()
	{
		final int days = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		return days;
	}
	
	public static int Hour()
	{
		final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		return hour;
	}
	
	public static int Min()
	{
		final int min = Calendar.getInstance().get(Calendar.MINUTE);
		return min;
	}
	
	public static int Day()
	{
		final int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		return day;
	}
	
	public static boolean SpecialEvent()
	{
		if (SpecialEvents._SpecialEventStarted)
		{
			return true;
		}
		return false;
	}
	
	public static boolean ChristmasEvent()
	{
		if (SpecialEvents._ChristmasStarted)
		{
			return true;
		}
		return false;
	}
	
	public static boolean WeekendCheck()
	{
		if (BorinetUtil._MDEventStarted)
		{
			return false;
		}
		else if (((Day() == Calendar.FRIDAY) && (Hour() >= Config.WEEKEND_START_TIME)) || (Day() == Calendar.SATURDAY) || (Day() == Calendar.SUNDAY))
		{
			return true;
		}
		
		return false;
	}
	
	public static boolean MemorialDayCheck()
	{
		if ((Month() == Calendar.JUNE) && (((Days() == 5) && (Hour() >= 20)) || (Days() == 6)))
		{
			return true;
		}
		return false;
	}
	
	public static long WeekendStartDelay()
	{
		final Calendar starttime = Calendar.getInstance();
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 0);
		starttime.set(Calendar.HOUR_OF_DAY, Config.WEEKEND_START_TIME);
		
		if (starttime.getTimeInMillis() < System.currentTimeMillis())
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - System.currentTimeMillis());
		
		return startDelay;
	}
	
	public static long WeekendEndDelay()
	{
		final Calendar endtime = Calendar.getInstance();
		endtime.set(Calendar.MINUTE, 0);
		endtime.set(Calendar.SECOND, 5);
		endtime.set(Calendar.HOUR_OF_DAY, 0);
		
		if (endtime.getTimeInMillis() < System.currentTimeMillis())
		{
			endtime.add(Calendar.DAY_OF_YEAR, 1);
		}
		final long endDelay = Math.max(0, endtime.getTimeInMillis() - System.currentTimeMillis());
		
		return endDelay;
	}
	
	public static long MemorialDayStartDelay()
	{
		final Calendar starttime = Calendar.getInstance();
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 0);
		starttime.set(Calendar.HOUR_OF_DAY, 20);
		
		if (starttime.getTimeInMillis() < System.currentTimeMillis())
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - System.currentTimeMillis());
		
		return startDelay;
	}
	
	public static long MemorialDayEndDelay()
	{
		final Calendar endtime = Calendar.getInstance();
		endtime.set(Calendar.MINUTE, 0);
		endtime.set(Calendar.SECOND, 5);
		endtime.set(Calendar.HOUR_OF_DAY, 0);
		
		if (endtime.getTimeInMillis() < System.currentTimeMillis())
		{
			endtime.add(Calendar.DAY_OF_YEAR, 1);
		}
		final long endDelay = Math.max(0, endtime.getTimeInMillis() - System.currentTimeMillis());
		
		return endDelay;
	}
	
	public static long BossEventDelay()
	{
		final Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.MINUTE) >= 10)
		{
			calendar.add(Calendar.HOUR_OF_DAY, 1);
			calendar.set(Calendar.MINUTE, 10);
			calendar.set(Calendar.SECOND, 0);
		}
		else
		{
			calendar.set(Calendar.MINUTE, 10);
			calendar.set(Calendar.SECOND, 0);
		}
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - System.currentTimeMillis());
		
		return startDelay;
	}
	
	public static long GoldenPigEventDelay()
	{
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
		
		return startDelay;
	}
	
	public long setBoatSchedule()
	{
		final Calendar calendar = Calendar.getInstance();
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		
		if ((minute >= 30) && (minute < 55))
		{
			setCalendar(calendar, 55, 0);
		}
		else if (minute >= 55)
		{
			handleLateMinutes(calendar, minute, second);
		}
		else if (minute < 30)
		{
			handleEarlyMinutes(calendar, minute, second);
		}
		
		long calendarTimeInMillis = calendar.getTimeInMillis();
		// final long startDelay = Math.max(0, calendarTimeInMillis - System.currentTimeMillis());
		return calendarTimeInMillis;
	}
	
	private void handleLateMinutes(Calendar calendar, int minute, int second)
	{
		if (minute == 59)
		{
			if (second >= 40)
			{
				_boatCycle = 4;
				setCalendar(calendar, 0, 0, 1);
			}
			else
			{
				_boatCycle = 3;
				setCalendar(calendar, 59, 40);
			}
		}
		else
		{
			_boatCycle = 2;
			setCalendar(calendar, 59, 0);
		}
	}
	
	private void handleEarlyMinutes(Calendar calendar, int minute, int second)
	{
		if (minute == 29)
		{
			if (second >= 40)
			{
				_boatCycle = 4;
				setCalendar(calendar, 30, 0);
			}
			else
			{
				_boatCycle = 3;
				setCalendar(calendar, 29, 40);
			}
		}
		else if (minute >= 25)
		{
			_boatCycle = 2;
			setCalendar(calendar, 29, 0);
		}
		else
		{
			_boatCycle = 1;
			setCalendar(calendar, 25, 0);
		}
	}
	
	private void setCalendar(Calendar calendar, int minute, int second)
	{
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
	}
	
	private void setCalendar(Calendar calendar, int minute, int second, int hourOffset)
	{
		calendar.add(Calendar.HOUR_OF_DAY, hourOffset);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
	}
	
	public static BorinetTask getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BorinetTask INSTANCE = new BorinetTask();
	}
}