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
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;

public class ItemLog
{
	private static final Logger LOGGER = Logger.getLogger(ItemLog.class.getName());
	private final ReentrantLock _dbLock = new ReentrantLock();
	
	protected ItemLog()
	{
		final long currentTime = System.currentTimeMillis();
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 4); // 새벽 4시로 설정
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1); // 현재 시간이 4시 이전이면 내일로 설정
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(ItemLog::deleteOldTables, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1일 간격으로 호출
		
		LOGGER.info("아이템 로그: 7일전 로그테이블 삭제가 매일 새벽4시에 진행됩니다.");
	}
	
	private void logItem(String process, String characterName, int characterId, String itemName, long currentQuantity, long previousQuantity, String npcName, int enchantLevel, Item item)
	{
		// 오늘 날짜 기반으로 테이블 이름 생성 (yyyy_MM_dd 형식)
		String tableName = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
		// 테이블이 존재하지 않으면 생성
		createTableIfNotExists(tableName);
		
		// DB에 로그 기록
		String query = "INSERT INTO " + tableName + " (process_status, character_name, character_id, item_name, current_quantity, previous_quantity, npc_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection connection = DatabaseFactory.getConnectionLog();
			PreparedStatement ps = connection.prepareStatement(query))
		{
			ps.setString(1, process);
			ps.setString(2, characterName);
			ps.setInt(3, characterId);
			
			// 인챈트 레벨이 0보다 클 경우, 아이템 이름에 +인챈트 레벨을 추가
			if (enchantLevel > 0)
			{
				ps.setString(4, "+" + enchantLevel + " " + itemName);
			}
			else
			{
				ps.setString(4, itemName);
			}
			
			ps.setLong(5, currentQuantity);
			ps.setLong(6, previousQuantity);
			ps.setString(7, npcName);
			
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void createTableIfNotExists(String tableName)
	{
		// 테이블이 존재하지 않을 경우 테이블 생성
		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + "log_id INT AUTO_INCREMENT PRIMARY KEY, " + "log_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "process_status VARCHAR(255), " + "character_name VARCHAR(255), " + "character_id INT, " + "item_name VARCHAR(255), " + "current_quantity BIGINT, " + "previous_quantity BIGINT, " + "npc_name VARCHAR(255))";
		
		try (Connection con = DatabaseFactory.getConnectionLog();
			Statement stmt = con.createStatement()) // Statement 사용
		{
			stmt.execute(createTableQuery);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void deleteOldTables()
	{
		// 7일 전의 날짜를 yyyy_MM_dd 형식으로 가져옴
		LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
		String oldTableName = sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
		
		// 테이블 존재 여부 확인 쿼리
		String checkTableQuery = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '" + oldTableName + "'";
		
		// 7일전 테이블 삭제 쿼리
		String dropTableQuery = "DROP TABLE IF EXISTS " + oldTableName;
		
		try (Connection connection = DatabaseFactory.getConnectionLog();
			Statement statement = connection.createStatement())
		{
			// 테이블 존재 여부 확인
			ResultSet resultSet = statement.executeQuery(checkTableQuery);
			if (resultSet.next() && (resultSet.getInt(1) > 0))
			{
				// 테이블이 존재할 경우에만 삭제 실행
				statement.executeUpdate(dropTableQuery);
				LOGGER.info("아이템 로그: 테이블 삭제 - [" + oldTableName + "]");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void insertItemInDB(String process, Player player, Item item, long old, Object reference)
	{
		if (item == null)
		{
			return;
		}
		
		// 낚시로 습득하는 아이템 로그기록 조건문
		if (!Config.FISING_REWARD_ITEM_LOG_ENABLE && (process == "Fishing Reward"))
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
			String processing = String.valueOf(process);
			String characterName = player.getName();
			int characterId = player.getObjectId();
			String itemName = item.getTemplate().getName();
			String npcName = (reference != null) ? String.valueOf(reference) : "None";
			
			_dbLock.lock();
			try
			{
				logItem(processing, characterName, characterId, itemName, item.getCount(), old, npcName, item.getEnchantLevel(), item);
			}
			finally
			{
				_dbLock.unlock();
			}
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
