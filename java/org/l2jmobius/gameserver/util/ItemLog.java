package org.l2jmobius.gameserver.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactoryLog;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;

public class ItemLog
{
	private static final Logger LOGGER = Logger.getLogger(ItemLog.class.getName());
	private final ReentrantLock _dbLock = new ReentrantLock();
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
		currentTableName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
	}
	
	private void scheduleTask(Runnable task, int hour, int minute)
	{
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
	
	public void insertItemInDB(String process, Player player, Item item, long old, String reference)
	{
		if (item == null)
		{
			return;
		}
		
		// 낚시로 습득하는 아이템 로그기록 조건문
		if (!Config.FISING_REWARD_ITEM_LOG_ENABLE && (process.equals("Fishing Reward")))
		{
			return;
		}
		
		// 로그 기록에서 제외할 아이템 필터링
		// MATERIAL 및 RECIPE면 로그 기록하지 않음
		// 특정 ID 또는 이름이 제외 목록에 있는 경우 로그 기록하지 않음
		if ((item.getItemType() == EtcItemType.MATERIAL) || (item.getItemType() == EtcItemType.RECIPE) || //
			Config.NO_ITEM_LOG_ITEM_IDS.contains(item.getId()) || //
			((item.getName() != null) && Config.NO_ITEM_LOG_NAMES.stream().anyMatch(name -> item.getName().matches(".*\\b" + Pattern.quote(name) + "\\b.*"))))
		{
			return;
		}
		
		if (player != null)
		{
			_dbLock.lock();
			try
			{
				logItem(process, player, item, old, reference);
			}
			finally
			{
				_dbLock.unlock();
			}
		}
	}
	
	private void logItem(String process, Player player, Item item, long old, String reference)
	{
		// DB에 로그 기록
		String query = "INSERT INTO " + currentTableName + " (process_status, character_name, character_id, item_name, current_quantity, previous_quantity, npc_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		try (Connection connection = DatabaseFactoryLog.getConnection();
			PreparedStatement ps = connection.prepareStatement(query))
		{
			ps.setString(1, process);
			ps.setString(2, player.getName());
			ps.setInt(3, player.getObjectId());
			ps.setString(4, item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() + " " + item.getName() : item.getName());
			ps.setLong(5, item.getCount());
			ps.setLong(6, old);
			ps.setString(7, (reference != null) ? reference : "None");
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.warning("아이템 로그 기록 중 오류 발생: " + e.getMessage());
		}
	}
	
	private void deleteOldTables()
	{
		// 7일 전의 날짜를 yyyy_MM_dd 형식으로 가져옴
		LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
		String oldTableName = sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
		
		String dropTableQuery = "DROP TABLE IF EXISTS " + oldTableName;
		
		try (Connection connection = DatabaseFactoryLog.getConnection();
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
		// 내일 날짜 계산
		Calendar calendar = Calendar.getInstance();
		if (nextDay)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1); // 현재 날짜에 하루 더함
		}
		String tableName = new SimpleDateFormat("yyyy_MM_dd").format(calendar.getTime());
		
		// 테이블이 존재하지 않을 경우 테이블 생성
		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
			"log_id INT AUTO_INCREMENT PRIMARY KEY, " + //
			"log_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + //
			"process_status VARCHAR(255), " + //
			"character_name VARCHAR(255), " + //
			"character_id INT, " + //
			"item_name VARCHAR(255), " + //
			"current_quantity BIGINT, " + //
			"previous_quantity BIGINT, " + //
			"npc_name VARCHAR(255))";
		try (Connection con = DatabaseFactoryLog.getConnection())
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
	
	public static ItemLog getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemLog INSTANCE = new ItemLog();
	}
}
