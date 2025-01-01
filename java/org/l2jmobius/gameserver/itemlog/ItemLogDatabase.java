package org.l2jmobius.gameserver.itemlog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class ItemLogDatabase
{
	private static final Logger LOGGER = Logger.getLogger(ItemLogDatabase.class.getName());
	private static String currentTableName;
	
	public void Item_Log()
	{
		createTableIfNotExists(false);
		updateTable();
		scheduleTask(this::updateTable, 0, 0); // 자정에 실행
		scheduleTask(this::deleteOldTables, 4, 0); // 매일 새벽 4시에 실행
	}
	
	private void updateTable()
	{
		if (!Config.LOG_TABLE_ITEMS)
		{
			return;
		}
		currentTableName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
	}
	
	private void scheduleTask(Runnable task, int hour, int minute)
	{
		if (!Config.LOG_TABLE_ITEMS)
		{
			return;
		}
		final long currentTime = System.currentTimeMillis();
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(task, startDelay, BorinetUtil.MILLIS_PER_DAY);
	}
	
	private void deleteOldTables()
	{
		// 7일 전의 날짜를 yyyy_MM_dd 형식으로 가져옴
		LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
		String oldTableName = sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
		
		String dropTableQuery = "DROP TABLE IF EXISTS " + oldTableName;
		
		try (Connection connection = DatabaseFactory.getConnection();
			Statement statement = connection.createStatement())
		{
			statement.executeUpdate(dropTableQuery);
			LOGGER.info("Item Log: [" + oldTableName + "] 테이블이 삭제되었습니다.");
		}
		catch (SQLException e)
		{
			LOGGER.warning("오래된 테이블 삭제 중 오류 발생: " + e.getMessage());
		}
		createTableIfNotExists(true);
	}
	
	private void createTableIfNotExists(boolean nextDay)
	{
		if (!Config.LOG_TABLE_ITEMS)
		{
			return;
		}
		// 내일 날짜 계산
		Calendar calendar = Calendar.getInstance();
		if (nextDay)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1); // 현재 날짜에 하루 더함
		}
		String tableName = new SimpleDateFormat("yyyy_MM_dd").format(calendar.getTime());
		
		// 테이블이 존재하지 않을 경우 테이블 생성
		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
			"id INT AUTO_INCREMENT PRIMARY KEY, " + //
			"timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " + //
			"process VARCHAR(50), " + //
			"item_id INT, " + //
			"enchant_level INT DEFAULT 0, " + //
			"item_name VARCHAR(255), " + //
			"old_count BIGINT DEFAULT 0, " + //
			"new_count BIGINT DEFAULT 0, " + //
			"actor_name VARCHAR(100), " + //
			"actor_id INT, " + //
			"reference_name VARCHAR(255))";
		try (Connection con = DatabaseFactory.getConnection())
		{
			// 테이블 존재 여부 확인
			boolean tableExists;
			try (ResultSet rs = con.getMetaData().getTables(null, null, tableName, null))
			{
				tableExists = rs.next();
			}
			
			if (!tableExists) // 테이블이 존재하지 않을 경우에만 생성
			{
				try (Statement stmt = con.createStatement())
				{
					stmt.execute(createTableQuery);
					if (!nextDay)
					{
						BorinetUtil.getInstance().printSection("로그 시스템");
					}
					LOGGER.info("Item Log: [" + tableName + "] 테이블이 생성되었습니다.");
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getTableName()
	{
		return currentTableName;
	}
	
	public static ItemLogDatabase getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemLogDatabase INSTANCE = new ItemLogDatabase();
	}
}
