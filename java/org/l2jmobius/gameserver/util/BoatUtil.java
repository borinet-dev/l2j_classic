package org.l2jmobius.gameserver.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;

public class BoatUtil
{
	public static final Logger LOGGER = Logger.getLogger(BoatUtil.class.getName());
	public static boolean _stopRequested = false;
	public int _cycleBorinet = 0;
	public static boolean isStartedBoatBaikal = false;
	public static boolean isStartedBoatBorinet = false;
	
	public static boolean isComplatedBaikal = false;
	public static boolean isComplatedBorinet = false;
	
	// 상태 저장
	public void setBoatStatus(String name, String status)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE boat_schedule SET status = ? WHERE name = ?"))
		{
			ps.setString(1, status);
			ps.setString(2, name);
			int rowsUpdated = ps.executeUpdate();
			if (rowsUpdated == 0)
			{
				LOGGER.warning("보트 상태 업데이트 실패: 해당 보트가 존재하지 않습니다 - " + name);
			}
		}
		catch (SQLException e)
		{
			LOGGER.severe("보트 상태 업데이트에 실패했습니다: " + e.getMessage());
		}
	}
	
	// 상태 정보
	public String getBoatStatus(String name)
	{
		String status = "정박 중";
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT status FROM boat_schedule WHERE name = ?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					status = rs.getString("status");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.severe("보트 상태 조회에 실패했습니다: " + e.getMessage());
		}
		return status;
	}
	
	// 도착시간 저장
	public void setBoatArrival(String name, long arrival)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE boat_schedule SET arrivalTime = ? WHERE name = ?"))
		{
			ps.setLong(1, arrival);
			ps.setString(2, name);
			int rowsUpdated = ps.executeUpdate();
			if (rowsUpdated == 0)
			{
				LOGGER.warning("보트 도착 시간 업데이트 실패: 해당 보트가 존재하지 않습니다 - " + name);
			}
		}
		catch (SQLException e)
		{
			LOGGER.severe("보트 도착 시간 업데이트에 실패했습니다: " + e.getMessage());
		}
	}
	
	// 도착시간 정보
	public long getBoatArrival(String name)
	{
		long arrival = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT arrivalTime FROM boat_schedule WHERE name = ?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					arrival = rs.getLong("arrivalTime");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.severe("보트 도착 시간 조회에 실패했습니다: " + e.getMessage());
		}
		return arrival;
	}
	
	// 상태 저장
	public void setBoatDestination(String name, String destination)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE boat_schedule SET destination = ? WHERE name = ?"))
		{
			ps.setString(1, destination);
			ps.setString(2, name);
			int rowsUpdated = ps.executeUpdate();
			if (rowsUpdated == 0)
			{
				LOGGER.warning("보트 목적지 업데이트 실패: 해당 보트가 존재하지 않습니다 - " + name);
			}
		}
		catch (SQLException e)
		{
			LOGGER.severe("보트 목적지 업데이트에 실패했습니다: " + e.getMessage());
		}
	}
	
	// 상태 정보
	public String getBoatDestination(String name)
	{
		String destination = "";
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT destination FROM boat_schedule WHERE name = ?"))
		{
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					destination = rs.getString("destination");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.severe("보트 목적지 조회에 실패했습니다: " + e.getMessage());
		}
		return destination;
	}
	
	// 출발시간 한글로 (보트가 정박시 출력되는 메세지에 필요함)
	public String getBoatDepartureTime(String boatName)
	{
		long[] result = nextScheduler();
		long departurelTimeMillis = result[1] + 300000;
		// 현재 시간 가져오기
		long currentTimeMillis = System.currentTimeMillis();
		// 현재 시간에서 출발 시간까지 남은 시간 계산 (밀리초 단위)
		long remainingMillis = departurelTimeMillis - currentTimeMillis;
		
		// 남은 시간을 분 단위로 변환
		long remainingMinutes = remainingMillis / (60 * 1000);
		
		Calendar departureTime = Calendar.getInstance();
		departureTime.setTimeInMillis(departurelTimeMillis);
		
		// 시간과 분을 추출
		int hour = departureTime.get(Calendar.HOUR_OF_DAY); // 24시간 형식
		int minute = departureTime.get(Calendar.MINUTE);
		
		// 24시간 형식을 12시간 형식으로 변환
		int displayHour = hour % 12;
		if (displayHour == 0)
		{
			displayHour = 12; // 0시는 12시로 변환
		}
		
		// 분이 0인 경우 분을 제외하고 반환
		if ((minute == 0) || ((minute >= 54) && (minute <= 59)))
		{
			return String.format("%d분 동안 정박한 후 %d시 정각", remainingMinutes, ((minute >= 54) && (minute <= 59)) ? (displayHour % 12) + 1 : displayHour);
		}
		return String.format("%d분 동안 정박한 후 %d시 30분", remainingMinutes, displayHour);
	}
	
	public long[] nextScheduler()
	{
		Calendar now = Calendar.getInstance();
		int currentMinute = now.get(Calendar.MINUTE);
		
		// 계산할 다음 시간을 위한 Calendar 객체 생성
		Calendar nextTime = (Calendar) now.clone();
		
		// 현재 시간에서 매시 25분 또는 55분까지의 남은 시간을 계산
		if (currentMinute < 25)
		{
			nextTime.set(Calendar.MINUTE, 25);
		}
		else if (currentMinute < 55)
		{
			nextTime.set(Calendar.MINUTE, 55);
		}
		else
		{
			// 현재 시간이 55분 이후인 경우 다음 시간으로 설정
			nextTime.add(Calendar.HOUR_OF_DAY, 1);
			nextTime.set(Calendar.MINUTE, 25);
		}
		nextTime.set(Calendar.SECOND, 0);
		nextTime.set(Calendar.MILLISECOND, 0);
		
		// 다음 시간까지 남은 시간을 계산
		long delay = nextTime.getTimeInMillis() - now.getTimeInMillis();
		
		return new long[]
		{
			delay,
			nextTime.getTimeInMillis()
		};
	}
	
	public void appendBoatInfo(StringBuilder sb, String boatName)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT status, destination, arrivalTime FROM boat_schedule WHERE name=?"))
		{
			ps.setString(1, boatName);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					String status = rs.getString("status");
					String destination = rs.getString("destination");
					long arrivalTime = rs.getLong("arrivalTime");
					
					sb.append("<tr><td width=140 height=20 align=right><font color=D358F7>").append(boatName).append("</font>&nbsp;</td></tr>");
					if (boatName.equals("바이칼 호") && (!BoatUtil.isStartedBoatBaikal))
					{
						sb.append("<tr><td align=right><font color=FF4000>현재 운행을</font></td><td align=left><font color=FF4000>하지 않습니다.</font></td></font></tr>");
					}
					else if (boatName.equals("보리넷 호") && (!BoatUtil.isStartedBoatBorinet))
					{
						sb.append("<tr><td align=right><font color=FF4000>현재 운행을</font></td><td align=left><font color=FF4000>하지 않습니다.</font></td></font></tr>");
					}
					else
					{
						if ("정박 중".equals(status))
						{
							String current = "기란";
							if ("기란 항구".equals(destination))
							{
								current = "말섬";
							}
							String departureTimeStr = formatTimeDeparture();
							sb.append("<tr><td align=right>상태:&nbsp;</td><td width=140 align=left>").append(status).append(" <font color=DBA901>(").append(current).append(")</font>").append("</td></tr>");
							sb.append("<tr><td align=right height=20>목적지:&nbsp;</td><td align=left>").append(destination).append("</td></tr>");
							sb.append("<tr><td align=right height=20>출발시간:&nbsp;</td><td align=left><font color=40FF00>").append(departureTimeStr).append("</font></td></tr>");
						}
						else if ("운항 중".equals(status))
						{
							String arrivalTimeStr = formatTimeArrival(arrivalTime);
							sb.append("<tr><td align=right>상태:&nbsp;</td><td width=140 align=left>").append(status).append("</td></tr>");
							sb.append("<tr><td align=right height=20>목적지:&nbsp;</td><td align=left>").append(destination).append("</td></tr>");
							sb.append("<tr><td align=right height=20>도착시간:&nbsp;</td><td align=left><font color=FA8258>").append(arrivalTimeStr).append("</font></td></tr>");
						}
					}
					sb.append("<tr><td><br><br></td></tr>");
				}
			}
		}
		catch (SQLException e)
		{
			sb.append("<tr><td colspan=2>데이터베이스 오류 발생</td></tr>");
			e.printStackTrace();
		}
	}
	
	public String formatTimeDeparture()
	{
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(System.currentTimeMillis());
		
		int hour = time.get(Calendar.HOUR_OF_DAY); // 24시간 형식
		int minute = time.get(Calendar.MINUTE);
		
		// 24시간 형식을 12시간 형식으로 변환
		int displayHour = hour % 12;
		if (displayHour == 0)
		{
			displayHour = 12; // 0시는 12시로 변환
		}
		
		if (minute < 30)
		{
			return String.format("%d시 30분", displayHour);
		}
		// 분이 30 이상인 경우 "정각"을 반환
		return String.format("%d시 정각", (displayHour == 12 ? 1 : displayHour + 1));
	}
	
	public String formatTimeArrival(long timeMillis)
	{
		if (timeMillis == 0)
		{
			return "...";
		}
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(timeMillis);
		
		int hour = time.get(Calendar.HOUR_OF_DAY); // 24시간 형식
		int minute = time.get(Calendar.MINUTE);
		
		// 24시간 형식을 12시간 형식으로 변환
		int displayHour = hour % 12;
		if (displayHour == 0)
		{
			displayHour = 12; // 0시는 12시로 변환
		}
		
		return String.format("%d시 %02d분", displayHour, minute);
	}
	
	public static BoatUtil getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BoatUtil INSTANCE = new BoatUtil();
	}
}