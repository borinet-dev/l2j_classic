package org.l2jmobius.gameserver.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;

public class ItemLog
{
	private static final Logger LOGGER = Logger.getLogger(ItemLog.class.getName());
	
	public static void main(String[] args)
	{
		final long currentTime = System.currentTimeMillis();
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 4);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(ItemLog::deleteOldTables, startDelay, BorinetUtil.MILLIS_PER_DAY); // 1 day
	}
	
	private static void logItem(String process, String characterName, int characterId, String itemName, long currentQuantity, long previousQuantity, String npcName, int enchantLevel, Item item)
	{
		// 오늘 날짜 기반으로 테이블 이름 생성 (yyyy_MM_dd 형식)
		String tableName = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
		// 테이블이 존재하지 않으면 생성
		createTableIfNotExists(tableName);
		
		// 로그 기록에서 제외할 아이템 필터링
		if ((item.getItemType() == EtcItemType.MATERIAL) || (item.getItemType() == EtcItemType.RECIPE) || (item.getId() == 57))
		{
			return; // MATERIAL 및 RECIPE면 로그 기록하지 않음
		}
		
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
	
	private static void createTableIfNotExists(String tableName)
	{
		// 테이블이 존재하지 않을 경우 테이블 생성
		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + "log_id INT AUTO_INCREMENT PRIMARY KEY, " + "log_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "process_status VARCHAR(255), " + "character_name VARCHAR(255), " + "character_id INT, " + "item_name VARCHAR(255), " + "current_quantity BIGINT, " + "previous_quantity BIGINT, " + "npc_name VARCHAR(255))";
		
		try (Connection con = DatabaseFactory.getConnectionLog();
			Statement stmt = con.createStatement()) // Statement 사용
		{
			// 테이블 생성 쿼리 실행
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
		
		// oldTableName이 null인지 확인
		if ((oldTableName == null) || oldTableName.isEmpty())
		{
			return;
		}
		
		// 오래된 테이블 삭제
		String dropTableQuery = "DROP TABLE IF EXISTS " + oldTableName;
		
		try (Connection connection = DatabaseFactory.getConnectionLog();
			Statement statement = connection.createStatement())
		{
			statement.executeUpdate(dropTableQuery);
			LOGGER.info("아이템로그 테이블 [" + oldTableName + "]이 성공적으로 삭제되었습니다.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// 아이템 db 저장
	public static void insertItemInDB(String process, Player player, Item item, long old, Object reference)
	{
		if (player != null)
		{
			String processing = String.valueOf(process);
			String characterName = player.getName();
			int characterId = player.getObjectId();
			String itemName = item.getTemplate().getName();
			String npcName = (reference != null) ? String.valueOf(reference) : "None";
			
			logItem(processing, characterName, characterId, itemName, item.getCount(), old, npcName, item.getEnchantLevel(), item);
		}
	}
}
