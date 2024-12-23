/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.commons.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.l2jmobius.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class TimeUtil
{
	private static int findIndexOfNonDigit(CharSequence text)
	{
		for (int i = 0; i < text.length(); i++)
		{
			if (Character.isDigit(text.charAt(i)))
			{
				continue;
			}
			return i;
		}
		return -1;
	}
	
	/**
	 * Parses patterns like:
	 * <ul>
	 * <li>1min or 10mins</li>
	 * <li>1day or 10days</li>
	 * <li>1week or 4weeks</li>
	 * <li>1month or 12months</li>
	 * <li>1year or 5years</li>
	 * </ul>
	 * @param datePattern
	 * @return {@link Duration} object converted by the date pattern specified.
	 * @throws IllegalStateException when malformed pattern specified.
	 */
	public static Duration parseDuration(String datePattern)
	{
		final int index = findIndexOfNonDigit(datePattern);
		if (index == -1)
		{
			throw new IllegalStateException("Incorrect time format given: " + datePattern);
		}
		try
		{
			final int val = Integer.parseInt(datePattern.substring(0, index));
			final String type = datePattern.substring(index);
			final ChronoUnit unit;
			switch (type.toLowerCase())
			{
				case "sec":
				case "secs":
				{
					unit = ChronoUnit.SECONDS;
					break;
				}
				case "min":
				case "mins":
				{
					unit = ChronoUnit.MINUTES;
					break;
				}
				case "hour":
				case "hours":
				{
					unit = ChronoUnit.HOURS;
					break;
				}
				case "day":
				case "days":
				{
					unit = ChronoUnit.DAYS;
					break;
				}
				case "week":
				case "weeks":
				{
					unit = ChronoUnit.WEEKS;
					break;
				}
				case "month":
				case "months":
				{
					unit = ChronoUnit.MONTHS;
					break;
				}
				case "year":
				case "years":
				{
					unit = ChronoUnit.YEARS;
					break;
				}
				default:
				{
					unit = ChronoUnit.valueOf(type);
					if (unit == null)
					{
						throw new IllegalStateException("Incorrect format: " + type + " !!");
					}
				}
			}
			return Duration.of(val, unit);
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Incorrect time format given: " + datePattern + " val: " + datePattern.substring(0, index));
		}
	}
	
	public static String formatTimes(int time, boolean cut)
	{
		int days = 0;
		int hours = 0;
		int minutes = 0;
		
		days = time / 1440;
		hours = (time - (days * 24 * 60)) / 60;
		minutes = (time - (days * 24 * 60) - (hours * 60)) / 1;
		
		String result;
		
		if (days >= 1)
		{
			if ((hours < 1) || (cut))
			{
				result = days + "" + Util.declension(days, DeclensionKey.DAYS);
			}
			else
			{
				result = days + "" + Util.declension(days, DeclensionKey.DAYS) + " " + hours + "" + Util.declension(hours, DeclensionKey.HOUR) + " " + minutes + "" + Util.declension(minutes, DeclensionKey.MINUTES);
			}
		}
		else
		{
			if (hours >= 1)
			{
				if ((minutes < 1) || (cut))
				{
					result = hours + "" + Util.declension(hours, DeclensionKey.HOUR);
				}
				else
				{
					result = hours + "" + Util.declension(hours, DeclensionKey.HOUR) + " " + minutes + "" + Util.declension(minutes, DeclensionKey.MINUTES);
				}
			}
			else
			{
				result = minutes + "" + Util.declension(minutes, DeclensionKey.MINUTES);
			}
		}
		return result;
	}
}
