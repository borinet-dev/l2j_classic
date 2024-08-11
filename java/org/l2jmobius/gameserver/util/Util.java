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
package org.l2jmobius.gameserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.DeclensionKey;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.HtmlActionScope;
import org.l2jmobius.gameserver.enums.IllegalActionPunishmentType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.tasks.player.IllegalPlayerActionTask;
import org.l2jmobius.gameserver.model.interfaces.ILocational;
import org.l2jmobius.gameserver.network.serverpackets.AbstractHtmlPacket;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;

/**
 * General Utility functions related to game server.
 */
public class Util
{
	private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
	private static final NumberFormat ADENA_FORMATTER = NumberFormat.getIntegerInstance(Locale.KOREAN);
	
	public static void handleIllegalPlayerAction(Player actor, String message, IllegalActionPunishmentType punishment)
	{
		ThreadPool.schedule(new IllegalPlayerActionTask(actor, message, punishment), 5000);
	}
	
	/**
	 * @param from
	 * @param to
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin.
	 */
	public static double calculateAngleFrom(ILocational from, ILocational to)
	{
		return calculateAngleFrom(from.getX(), from.getY(), to.getX(), to.getY());
	}
	
	/**
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @return degree value of object 2 to the horizontal line with object 1 being the origin
	 */
	public static double calculateAngleFrom(int fromX, int fromY, int toX, int toY)
	{
		double angleTarget = Math.toDegrees(Math.atan2(toY - fromY, toX - fromX));
		if (angleTarget < 0)
		{
			angleTarget += 360;
		}
		return angleTarget;
	}
	
	public static String declension(long count, DeclensionKey word)
	{
		String one = "";
		String two = "";
		String five = "";
		switch (word)
		{
			case DAYS:
				one = new String("일");
				two = new String("일");
				five = new String("일");
				break;
			case HOUR:
				one = new String("시간");
				two = new String("시간");
				five = new String("시간");
				break;
			case MINUTES:
				one = new String("분");
				two = new String("분");
				five = new String("분");
				break;
		}
		if (count == 1L)
		{
			return one.toString();
		}
		if ((count == 2L) || (count == 3L) || (count == 4L))
		{
			return two.toString();
		}
		return five.toString();
	}
	
	/**
	 * Gets a random position around the specified location.
	 * @param loc the center location
	 * @param minRange the minimum range from the center to pick a point.
	 * @param maxRange the maximum range from the center to pick a point.
	 * @return a random location between minRange and maxRange of the center location.
	 */
	public static Location getRandomPosition(ILocational loc, int minRange, int maxRange)
	{
		final int randomX = Rnd.get(minRange, maxRange);
		final int randomY = Rnd.get(minRange, maxRange);
		final double rndAngle = Math.toRadians(Rnd.get(360));
		final int newX = (int) (loc.getX() + (randomX * Math.cos(rndAngle)));
		final int newY = (int) (loc.getY() + (randomY * Math.sin(rndAngle)));
		return new Location(newX, newY, loc.getZ());
	}
	
	public static String getFullClassName(int classId)
	{
		String name = null;
		switch (classId)
		{
			case 0:
				name = "파이터";
				break;
			case 1:
				name = "워리어";
				break;
			case 2:
				name = "글라디에이터";
				break;
			case 3:
				name = "워로드";
				break;
			case 4:
				name = "휴먼 나이트";
				break;
			case 5:
				name = "팰러딘";
				break;
			case 6:
				name = "다크 어벤저";
				break;
			case 7:
				name = "로그";
				break;
			case 8:
				name = "트레져 헌터";
				break;
			case 9:
				name = "호크아이";
				break;
			case 10:
				name = "메이지";
				break;
			case 11:
				name = "위저드";
				break;
			case 12:
				name = "소서러";
				break;
			case 13:
				name = "네크로맨서";
				break;
			case 14:
				name = "워록";
				break;
			case 15:
				name = "클레릭";
				break;
			case 16:
				name = "비숍";
				break;
			case 17:
				name = "프로핏";
				break;
			case 18:
				name = "엘븐 파이터";
				break;
			case 19:
				name = "엘븐 나이트";
				break;
			case 20:
				name = "템플 나이트";
				break;
			case 21:
				name = "소드싱어";
				break;
			case 22:
				name = "엘븐 스카우트";
				break;
			case 23:
				name = "플래인 워커";
				break;
			case 24:
				name = "실버 레인져";
				break;
			case 25:
				name = "엘븐 메이지";
				break;
			case 26:
				name = "엘븐 위저드";
				break;
			case 27:
				name = "스펠싱어";
				break;
			case 28:
				name = "엘레멘탈 서머너";
				break;
			case 29:
				name = "오라클";
				break;
			case 30:
				name = "엘더";
				break;
			case 31:
				name = "다크 파이터";
				break;
			case 32:
				name = "팰러스 나이트";
				break;
			case 33:
				name = "실리엔 나이트";
				break;
			case 34:
				name = "블레이드댄서";
				break;
			case 35:
				name = "어쌔신";
				break;
			case 36:
				name = "어비스 워커";
				break;
			case 37:
				name = "팬텀 레인져";
				break;
			case 38:
				name = "다크 메이지";
				break;
			case 39:
				name = "다크 위저드";
				break;
			case 40:
				name = "스펠하울러";
				break;
			case 41:
				name = "팬텀 서머너";
				break;
			case 42:
				name = "실리엔 오라클";
				break;
			case 43:
				name = "실리엔 엘더";
				break;
			case 44:
				name = "오크 파이터";
				break;
			case 45:
				name = "오크 레이더";
				break;
			case 46:
				name = "디스트로이어";
				break;
			case 47:
				name = "오크 몽크";
				break;
			case 48:
				name = "타이런트";
				break;
			case 49:
				name = "오크 메이지";
				break;
			case 50:
				name = "오크 샤먼";
				break;
			case 51:
				name = "오버로드";
				break;
			case 52:
				name = "워크라이어";
				break;
			case 53:
				name = "드워븐 파이터";
				break;
			case 54:
				name = "스캐빈져";
				break;
			case 55:
				name = "바운티 헌터";
				break;
			case 56:
				name = "아티산";
				break;
			case 57:
				name = "워스미스";
				break;
			case 88:
				name = "듀얼리스트";
				break;
			case 89:
				name = "드레드노트";
				break;
			case 90:
				name = "피닉스 나이트";
				break;
			case 91:
				name = "헬 나이트";
				break;
			case 92:
				name = "사지타리우스";
				break;
			case 93:
				name = "어드벤쳐러";
				break;
			case 94:
				name = "아크메이지";
				break;
			case 95:
				name = "소울테이커";
				break;
			case 96:
				name = "아르카나 로드";
				break;
			case 97:
				name = "카디날";
				break;
			case 98:
				name = "하이로펀트";
				break;
			case 99:
				name = "에바스 템플러";
				break;
			case 100:
				name = "소드 뮤즈";
				break;
			case 101:
				name = "윈드 라이더";
				break;
			case 102:
				name = "문라이트 센티넬";
				break;
			case 103:
				name = "미스틱 뮤즈";
				break;
			case 104:
				name = "엘레멘탈 마스터";
				break;
			case 105:
				name = "에바스 세인트";
				break;
			case 106:
				name = "실리엔 템플러";
				break;
			case 107:
				name = "스펙트럴 댄서";
				break;
			case 108:
				name = "고스트 헌터";
				break;
			case 109:
				name = "고스트 센티넬";
				break;
			case 110:
				name = "스톰 스크리머";
				break;
			case 111:
				name = "스펙트럴 마스터";
				break;
			case 112:
				name = "실리엔 세인트";
				break;
			case 113:
				name = "타이탄";
				break;
			case 114:
				name = "그랜드 카바타리";
				break;
			case 115:
				name = "도미네이터";
				break;
			case 116:
				name = "둠크라이어";
				break;
			case 117:
				name = "포츈 시커";
				break;
			case 118:
				name = "마에스트로";
				break;
			case 123:
				name = "카마엘 솔져";
				break;
			case 124:
				name = "카마엘 솔져";
				break;
			case 125:
				name = "트루퍼";
				break;
			case 126:
				name = "워더";
				break;
			case 127:
				name = "버서커";
				break;
			case 128:
				name = "소울 브레이커";
				break;
			case 129:
				name = "소울 브레이커";
				break;
			case 130:
				name = "아바레스터";
				break;
			case 131:
				name = "둠브링거";
				break;
			case 132:
				name = "소울 하운드";
				break;
			case 133:
				name = "소울 하운드";
				break;
			case 134:
				name = "트릭스터";
				break;
			case 135:
				name = "인스펙터";
				break;
			case 136:
				name = "쥬디케이터";
				break;
			default:
				name = "Unknown";
		}
		return name;
	}
	
	public static double convertHeadingToDegree(int clientHeading)
	{
		final double degree = clientHeading / 182.044444444;
		return degree;
	}
	
	public static int calculateHeadingFrom(ILocational from, ILocational to)
	{
		return calculateHeadingFrom(from.getX(), from.getY(), to.getX(), to.getY());
	}
	
	public static int calculateHeadingFrom(int fromX, int fromY, int toX, int toY)
	{
		double angleTarget = Math.toDegrees(Math.atan2(toY - fromY, toX - fromX));
		if (angleTarget < 0)
		{
			angleTarget += 360;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	public static int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0)
		{
			angleTarget += 360;
		}
		return (int) (angleTarget * 182.044444444);
	}
	
	/**
	 * Calculates distance between one set of x, y, z and another set of x, y, z.
	 * @param x1 - X coordinate of first point.
	 * @param y1 - Y coordinate of first point.
	 * @param z1 - Z coordinate of first point.
	 * @param x2 - X coordinate of second point.
	 * @param y2 - Y coordinate of second point.
	 * @param z2 - Z coordinate of second point.
	 * @param includeZAxis - If set to true, Z coordinates will be included.
	 * @param squared - If set to true, distance returned will be squared.
	 * @return {@code double} - Distance between object and given x, y , z.
	 */
	public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2, boolean includeZAxis, boolean squared)
	{
		final double distance = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + (includeZAxis ? Math.pow(z1 - z2, 2) : 0);
		return (squared) ? distance : Math.sqrt(distance);
	}
	
	/**
	 * Calculates distance between 2 locations.
	 * @param loc1 - First location.
	 * @param loc2 - Second location.
	 * @param includeZAxis - If set to true, Z coordinates will be included.
	 * @param squared - If set to true, distance returned will be squared.
	 * @return {@code double} - Distance between object and given location.
	 */
	public static double calculateDistance(ILocational loc1, ILocational loc2, boolean includeZAxis, boolean squared)
	{
		return calculateDistance(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ(), includeZAxis, squared);
	}
	
	/**
	 * @param range
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis
	 * @return {@code true} if the two objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInRange(int range, WorldObject obj1, WorldObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null) || (obj1.getInstanceWorld() != obj2.getInstanceWorld()))
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}
		
		int radius = 0;
		if (obj1.isCreature())
		{
			radius += ((Creature) obj1).getTemplate().getCollisionRadius();
		}
		if (obj2.isCreature())
		{
			radius += ((Creature) obj2).getTemplate().getCollisionRadius();
		}
		
		return calculateDistance(obj1, obj2, includeZAxis, false) <= (range + radius);
	}
	
	/**
	 * Checks if object is within short (sqrt(int.max_value)) radius, not using collisionRadius. Faster calculation than checkIfInRange if distance is short and collisionRadius isn't needed. Not for long distance checks (potential teleports, far away castles etc).
	 * @param range
	 * @param obj1
	 * @param obj2
	 * @param includeZAxis if true, check also Z axis (3-dimensional check), otherwise only 2D
	 * @return {@code true} if objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInShortRange(int range, WorldObject obj1, WorldObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (range == -1)
		{
			return true; // not limited
		}
		return calculateDistance(obj1, obj2, includeZAxis, false) <= range;
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only numbers, {@code false} otherwise
	 */
	public static boolean isDigit(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} is integer, {@code false} otherwise
	 */
	public static boolean isInteger(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		try
		{
			Integer.parseInt(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} is float, {@code false} otherwise
	 */
	public static boolean isFloat(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		try
		{
			Float.parseFloat(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} is double, {@code false} otherwise
	 */
	public static boolean isDouble(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		try
		{
			Double.parseDouble(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param <T>
	 * @param name - the text to check
	 * @param enumType
	 * @return {@code true} if {@code text} is enum, {@code false} otherwise
	 */
	public static <T extends Enum<T>> boolean isEnum(String name, Class<T> enumType)
	{
		if ((name == null) || name.isEmpty())
		{
			return false;
		}
		try
		{
			return Enum.valueOf(enumType, name) != null;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * @param text - the text to check
	 * @return {@code true} if {@code text} contains only letters and/or numbers, {@code false} otherwise
	 */
	public static boolean isAlphaNumeric(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isLetterOrDigit(c))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch (PatternSyntaxException e) // invalid template
		{
			e.printStackTrace();
		}
		if (pattern == null)
		{
			return false;
		}
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}
	
	public static boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e)
		{
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	/**
	 * Format the specified digit using the digit grouping symbol "," (comma).<br>
	 * For example, 123456789 becomes 123,456,789.
	 * @param amount - the amount of adena
	 * @return the formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		synchronized (ADENA_FORMATTER)
		{
			return ADENA_FORMATTER.format(amount);
		}
	}
	
	/**
	 * @param value
	 * @param format
	 * @return formatted double value by specified format.
	 */
	public static String formatDouble(double value, String format)
	{
		final DecimalFormat formatter = new DecimalFormat(format, new DecimalFormatSymbols(Locale.KOREAN));
		return formatter.format(value);
	}
	
	/**
	 * Format the given date on the given format
	 * @param date : the date to format.
	 * @param format : the format to correct by.
	 * @return a string representation of the formatted date.
	 */
	public static String formatDate(Date date, String format)
	{
		if (date == null)
		{
			return null;
		}
		final DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}
	
	public static String getDateString(Date date)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date.getTime());
	}
	
	private static void buildHtmlBypassCache(Player player, HtmlActionScope scope, String html)
	{
		final String htmlLower = html.toLowerCase(Locale.KOREAN);
		int bypassEnd = 0;
		int bypassStart = htmlLower.indexOf("=\"bypass ", bypassEnd);
		int bypassStartEnd;
		while (bypassStart != -1)
		{
			bypassStartEnd = bypassStart + 9;
			bypassEnd = htmlLower.indexOf("\"", bypassStartEnd);
			if (bypassEnd == -1)
			{
				break;
			}
			
			final int hParamPos = htmlLower.indexOf("-h ", bypassStartEnd);
			String bypass;
			if ((hParamPos != -1) && (hParamPos < bypassEnd))
			{
				bypass = html.substring(hParamPos + 3, bypassEnd).trim();
			}
			else
			{
				bypass = html.substring(bypassStartEnd, bypassEnd).trim();
			}
			
			final int firstParameterStart = bypass.indexOf(AbstractHtmlPacket.VAR_PARAM_START_CHAR);
			if (firstParameterStart != -1)
			{
				bypass = bypass.substring(0, firstParameterStart + 1);
			}
			
			if (Config.HTML_ACTION_CACHE_DEBUG)
			{
				LOGGER.info("Cached html bypass(" + scope + "): '" + bypass + "'");
			}
			player.addHtmlAction(scope, bypass);
			bypassStart = htmlLower.indexOf("=\"bypass ", bypassEnd);
		}
	}
	
	private static void buildHtmlLinkCache(Player player, HtmlActionScope scope, String html)
	{
		final String htmlLower = html.toLowerCase(Locale.KOREAN);
		int linkEnd = 0;
		int linkStart = htmlLower.indexOf("=\"link ", linkEnd);
		int linkStartEnd;
		while (linkStart != -1)
		{
			linkStartEnd = linkStart + 7;
			linkEnd = htmlLower.indexOf("\"", linkStartEnd);
			if (linkEnd == -1)
			{
				break;
			}
			
			final String htmlLink = html.substring(linkStartEnd, linkEnd).trim();
			if (htmlLink.isEmpty())
			{
				LOGGER.warning("Html의 경로가 없습니다.");
				continue;
			}
			
			if (htmlLink.contains(".."))
			{
				LOGGER.warning("Html link path is invalid: " + htmlLink);
				continue;
			}
			
			if (Config.HTML_ACTION_CACHE_DEBUG)
			{
				LOGGER.info("Cached html link(" + scope + "): '" + htmlLink + "'");
			}
			// let's keep an action cache with "link " lowercase literal kept
			player.addHtmlAction(scope, "link " + htmlLink);
			linkStart = htmlLower.indexOf("=\"link ", linkEnd);
		}
	}
	
	/**
	 * Builds the html action cache for the specified scope.<br>
	 * An {@code npcObjId} of 0 means, the cached actions can be clicked<br>
	 * without beeing near an npc which is spawned in the world.
	 * @param player the player to build the html action cache for
	 * @param scope the scope to build the html action cache for
	 * @param npcObjId the npc object id the html actions are cached for
	 * @param html the html code to parse
	 */
	public static void buildHtmlActionCache(Player player, HtmlActionScope scope, int npcObjId, String html)
	{
		if ((player == null) || (scope == null) || (npcObjId < 0) || (html == null))
		{
			throw new IllegalArgumentException();
		}
		
		if (Config.HTML_ACTION_CACHE_DEBUG)
		{
			LOGGER.info("Set html action npc(" + scope + "): " + npcObjId);
		}
		player.setHtmlActionOriginObjectId(scope, npcObjId);
		buildHtmlBypassCache(player, scope, html);
		buildHtmlLinkCache(player, scope, html);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * HtmlActionCache will be build with npc origin 0 which means the<br>
	 * links on the html are not bound to a specific npc.
	 * @param player the player
	 * @param html the html content
	 */
	public static void sendCBHtml(Player player, String html)
	{
		sendCBHtml(player, html, 0);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * When {@code npcObjId} is greater -1 the HtmlActionCache will be build<br>
	 * with the npcObjId as origin. An origin of 0 means the cached bypasses<br>
	 * are not bound to a specific npc.
	 * @param player the player to send the html content to
	 * @param html the html content
	 * @param npcObjId bypass origin to use
	 */
	public static void sendCBHtml(Player player, String html, int npcObjId)
	{
		sendCBHtml(player, html, null, npcObjId);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * HtmlActionCache will be build with npc origin 0 which means the<br>
	 * links on the html are not bound to a specific npc. It also fills a<br>
	 * multiedit field in the send html if fillMultiEdit is not null.
	 * @param player the player
	 * @param html the html content
	 * @param fillMultiEdit text to fill the multiedit field with(may be null)
	 */
	public static void sendCBHtml(Player player, String html, String fillMultiEdit)
	{
		sendCBHtml(player, html, fillMultiEdit, 0);
	}
	
	/**
	 * Helper method to send a community board html to the specified player.<br>
	 * It fills a multiedit field in the send html if {@code fillMultiEdit}<br>
	 * is not null. When {@code npcObjId} is greater -1 the HtmlActionCache will be build<br>
	 * with the npcObjId as origin. An origin of 0 means the cached bypasses<br>
	 * are not bound to a specific npc.
	 * @param player the player
	 * @param html the html content
	 * @param fillMultiEdit text to fill the multiedit field with(may be null)
	 * @param npcObjId bypass origin to use
	 */
	public static void sendCBHtml(Player player, String html, String fillMultiEdit, int npcObjId)
	{
		if ((player == null) || (html == null))
		{
			return;
		}
		
		player.clearHtmlActions(HtmlActionScope.COMM_BOARD_HTML);
		
		if (npcObjId > -1)
		{
			buildHtmlActionCache(player, HtmlActionScope.COMM_BOARD_HTML, npcObjId, html);
		}
		
		if (fillMultiEdit != null)
		{
			player.sendPacket(new ShowBoard(html, "1001"));
			fillMultiEditContent(player, fillMultiEdit);
		}
		else if (html.length() < 16250)
		{
			player.sendPacket(new ShowBoard(html, "101"));
			player.sendPacket(new ShowBoard(null, "102"));
			player.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (16250 * 2))
		{
			player.sendPacket(new ShowBoard(html.substring(0, 16250), "101"));
			player.sendPacket(new ShowBoard(html.substring(16250), "102"));
			player.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (16250 * 3))
		{
			player.sendPacket(new ShowBoard(html.substring(0, 16250), "101"));
			player.sendPacket(new ShowBoard(html.substring(16250, 16250 * 2), "102"));
			player.sendPacket(new ShowBoard(html.substring(16250 * 2), "103"));
		}
		else
		{
			player.sendPacket(new ShowBoard("<html><body><br><center>Error: HTML was too long!</center></body></html>", "101"));
			player.sendPacket(new ShowBoard(null, "102"));
			player.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	 * Fills the community board's multiedit window with text. Must send after sendCBHtml
	 * @param player
	 * @param text
	 */
	public static void fillMultiEditContent(Player player, String text)
	{
		player.sendPacket(new ShowBoard(Arrays.asList("0", "0", "0", "0", "0", "0", player.getName(), Integer.toString(player.getObjectId()), player.getAccountName(), "9", " ", " ", text.replace("<br>", Config.EOL), "0", "0", "0", "0")));
	}
	
	public static boolean isInsideRangeOfObjectId(WorldObject obj, int targetObjId, int radius)
	{
		final WorldObject target = World.getInstance().findObject(targetObjId);
		return (target != null) && (obj.calculateDistance3D(target) <= radius);
	}
	
	public static String readAllLines(File file, Charset cs, String newLineDelimiter) throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		try (InputStream in = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(in, cs);
			BufferedReader buffer = new BufferedReader(reader))
		{
			String line;
			while ((line = buffer.readLine()) != null)
			{
				sb.append(line);
				if (newLineDelimiter != null)
				{
					sb.append(newLineDelimiter);
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static int map(int input, int inputMin, int inputMax, int outputMin, int outputMax)
	{
		return (((constrain(input, inputMin, inputMax) - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static long map(long input, long inputMin, long inputMax, long outputMin, long outputMax)
	{
		return (((constrain(input, inputMin, inputMax) - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Re-Maps a value from one range to another.
	 * @param input
	 * @param inputMin
	 * @param inputMax
	 * @param outputMin
	 * @param outputMax
	 * @return The mapped value
	 */
	public static double map(double input, double inputMin, double inputMax, double outputMin, double outputMax)
	{
		return (((constrain(input, inputMin, inputMax) - inputMin) * (outputMax - outputMin)) / (inputMax - inputMin)) + outputMin;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static int constrain(int input, int min, int max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static long constrain(long input, long min, long max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/**
	 * Constrains a number to be within a range.
	 * @param input the number to constrain, all data types
	 * @param min the lower end of the range, all data types
	 * @param max the upper end of the range, all data types
	 * @return input: if input is between min and max, min: if input is less than min, max: if input is greater than max
	 */
	public static double constrain(double input, double min, double max)
	{
		return (input < min) ? min : (input > max) ? max : input;
	}
	
	/**
	 * This will sort a Map according to the values. Default sort direction is ascending.
	 * @param <K> keyType
	 * @param <V> valueType
	 * @param map Map to be sorted.
	 * @param descending If you want to sort descending.
	 * @return A new Map sorted by the values.
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending)
	{
		if (descending)
		{
			return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		}
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	public static String boolToString(boolean b)
	{
		return b ? "선공" : "비선공";
	}
	
	public static String formatTime(int time)
	{
		int times = 0;
		if (time == 0)
		{
			return "지금";
		}
		times = Math.abs(time);
		String ret = "";
		long numDays = times / 86400;
		times -= numDays * 86400;
		long numHours = times / 3600;
		times -= numHours * 3600;
		long numMins = times / 60;
		times -= numMins * 60;
		long numSeconds = times;
		if (numDays > 0)
		{
			ret += numDays + "일 ";
		}
		if (numHours > 0)
		{
			ret += numHours + "시간 ";
		}
		if (numMins > 0)
		{
			ret += numMins + "분 ";
		}
		if (numSeconds > 0)
		{
			ret += numSeconds + "초";
		}
		return ret.trim();
	}
	
}
