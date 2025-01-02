package org.l2jmobius.gameserver.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;

/**
 * @author 보리넷 가츠
 */
public class MailSystemOptimizer
{
	private static final Logger LOGGER = Logger.getLogger(MailSystemOptimizer.class.getName());
	private final String backupDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
	
	protected MailSystemOptimizer()
	{
		if (!setupBackupFolders())
		{
			LOGGER.info("백업 폴더가 생성되지 않아 백업을 진행할 수 없습니다.");
			return;
		}
		
		try
		{
			backupDatabase();
		}
		finally
		{
			backupTablesToSqlFile(); // 백업 데이터를 파일로 저장
			truncateBackupTables(); // 초기화
			createIndexes(); // 작업 전 인덱스 생성
			try
			{
				optimizeAndDeleteMessagesWithoutItemsBatch();
				optimizeAndDeleteItemsWithoutMessagesBatch();
			}
			finally
			{
				dropIndexes(); // 작업 종료 후 인덱스 삭제
			}
		}
	}
	
	private boolean setupBackupFolders()
	{
		File rootDir = new File("backup");
		File dateDir = new File(rootDir, backupDate);
		
		// 실제로 폴더를 생성
		return dateDir.exists() || dateDir.mkdirs();
	}
	
	private void backupTablesToSqlFile()
	{
		exportTableToSqlFile("messages_backup", "messages_backup.sql");
		exportTableToSqlFile("items_backup", "items_backup.sql");
	}
	
	private void exportTableToSqlFile(String tableName, String fileName)
	{
		String query = "SELECT * FROM " + tableName;
		String backup_date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
		String fullPath = "backup/" + backup_date + "/" + fileName;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			FileWriter writer = new FileWriter(fullPath))
		{
			int columnCount = rs.getMetaData().getColumnCount();
			
			// Write SQL INSERT statement headers
			while (rs.next())
			{
				StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" VALUES (");
				for (int i = 1; i <= columnCount; i++)
				{
					String value = rs.getString(i);
					if (value == null)
					{
						sql.append("NULL");
					}
					else
					{
						sql.append("'").append(value.replace("'", "''")).append("'");
					}
					if (i < columnCount)
					{
						sql.append(", ");
					}
				}
				sql.append(");\n");
				writer.write(sql.toString());
			}
			// LOGGER.info(tableName + " 테이블이 " + fullPath + " 파일로 SQL 형식으로 백업되었습니다.");
		}
		catch (SQLException | IOException e)
		{
			LOGGER.log(Level.SEVERE, tableName + " 테이블 백업 중 오류 발생:", e);
		}
	}
	
	private void truncateBackupTables()
	{
		String clearMessagesQuery = "TRUNCATE TABLE messages_backup";
		String clearItemsQuery = "TRUNCATE TABLE items_backup";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement clearMessagesStmt = con.prepareStatement(clearMessagesQuery);
			PreparedStatement clearItemsStmt = con.prepareStatement(clearItemsQuery))
		{
			clearMessagesStmt.executeUpdate();
			clearItemsStmt.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "테이블 초기화 중 오류 발생:", e);
		}
	}
	
	private void createIndexes()
	{
		String createMessagesIndex = "CREATE INDEX IF NOT EXISTS idx_messages_subject ON messages (subject)";
		String createItemsIndex = "CREATE INDEX IF NOT EXISTS idx_items_loc_data ON items (loc_data)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement createMessagesIndexStmt = con.prepareStatement(createMessagesIndex);
			PreparedStatement createItemsIndexStmt = con.prepareStatement(createItemsIndex))
		{
			
			createMessagesIndexStmt.executeUpdate();
			createItemsIndexStmt.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "인덱스 생성 중 오류 발생:", e);
		}
	}
	
	private void dropIndexes()
	{
		String dropMessagesIndex = "DROP INDEX IF EXISTS idx_messages_subject ON messages";
		String dropItemsIndex = "DROP INDEX IF EXISTS idx_items_loc_data ON items";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement dropMessagesIndexStmt = con.prepareStatement(dropMessagesIndex);
			PreparedStatement dropItemsIndexStmt = con.prepareStatement(dropItemsIndex))
		{
			
			dropMessagesIndexStmt.executeUpdate();
			dropItemsIndexStmt.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "인덱스 삭제 중 오류 발생:", e);
		}
	}
	
	private void optimizeAndDeleteMessagesWithoutItemsBatch()
	{
		final int batchSize = 1000; // 한 번에 처리할 레코드 수
		String backupMessageQuery = "INSERT INTO messages_backup (messageId, senderId, receiverId, subject, content, expiration, reqAdena, " + "hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, isLocked, sendBySystem, isReturned, " + "itemId, enchantLvl, elementals) " + "SELECT messageId, senderId, receiverId, subject, content, expiration, reqAdena, " + "hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, isLocked, sendBySystem, isReturned, " + "itemId, enchantLvl, elementals FROM messages m " + "LEFT JOIN items i ON m.messageId = i.loc_data " + "WHERE m.subject LIKE ? AND i.loc_data IS NULL LIMIT ?";
		String deleteMessageQuery = "DELETE FROM messages WHERE messageId IN (SELECT messageId FROM messages_backup)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement backupMessageStmt = con.prepareStatement(backupMessageQuery);
			PreparedStatement deleteMessageStmt = con.prepareStatement(deleteMessageQuery))
		{
			
			int totalProcessed = 0;
			
			while (true)
			{
				backupMessageStmt.setString(1, "대금청구 아이템이 수신대기 시간초과로 반송되었습니다.");
				backupMessageStmt.setInt(2, batchSize);
				
				int backupCount = backupMessageStmt.executeUpdate();
				if (backupCount == 0)
				{
					break; // 더 이상 처리할 레코드가 없으면 종료
				}
				
				deleteMessageStmt.executeUpdate();
				totalProcessed += backupCount;
			}
			
			if (totalProcessed > 0)
			{
				LOGGER.info(totalProcessed + "개의 우편을 정리했습니다.");
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "우편 시스템: 메시지 최적화 정리 중 오류 발생:", e);
		}
	}
	
	private void optimizeAndDeleteItemsWithoutMessagesBatch()
	{
		final int batchSize = 1000; // 한 번에 처리할 레코드 수
		String backupItemQuery = "INSERT INTO items_backup (owner_id, object_id, item_id, count, enchant_level, loc, loc_data, time_of_use, " + "custom_type1, custom_type2, mana_left, time) " + "SELECT owner_id, object_id, item_id, count, enchant_level, loc, loc_data, time_of_use, " + "custom_type1, custom_type2, mana_left, time FROM items i " + "LEFT JOIN messages m ON i.loc_data = m.messageId " + "WHERE i.loc = ? AND m.messageId IS NULL LIMIT ?";
		String deleteItemQuery = "DELETE FROM items WHERE loc_data IN (SELECT loc_data FROM items_backup)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement backupItemStmt = con.prepareStatement(backupItemQuery);
			PreparedStatement deleteItemStmt = con.prepareStatement(deleteItemQuery))
		{
			
			int totalProcessed = 0;
			
			while (true)
			{
				backupItemStmt.setString(1, "MAIL");
				backupItemStmt.setInt(2, batchSize);
				
				int backupCount = backupItemStmt.executeUpdate();
				if (backupCount == 0)
				{
					break; // 더 이상 처리할 레코드가 없으면 종료
				}
				
				deleteItemStmt.executeUpdate();
				totalProcessed += backupCount;
			}
			
			if (totalProcessed > 0)
			{
				LOGGER.info(totalProcessed + "개의 첨부 아이템을 정리했습니다.");
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "우편 시스템: 첨부 아이템 최적화 정리 중 오류 발생:", e);
		}
	}
	
	private void backupDatabase()
	{
		ExecutorService executor = Executors.newSingleThreadExecutor(); // 병렬 실행 스레드 풀
		executor.submit(() ->
		{
			String backupDir = "backup/" + backupDate;
			String backupFile = backupDir + "/l2jserver.sql";
			
			String mysqldumpPath = "C:/Program Files/MariaDB 11.4/bin/mysqldump.exe";
			String username = "root";
			String password = Config.DATABASE_PASSWORD;
			String database = "l2jserver";
			String charset = "--default-character-set=euckr";
			String dumpCommand = String.format( //
				"%s -u%s -p%s %s --result-file=\"%s\" %s", //
				mysqldumpPath, username, password, charset, backupFile, database //
			);
			
			try
			{
				Runtime.getRuntime().exec(dumpCommand);
			}
			catch (IOException e)
			{
				LOGGER.log(Level.SEVERE, "백업 작업 중 예외 발생", e);
			}
		});
		
		executor.shutdown(); // 스레드 풀 종료
	}
	
	public static MailSystemOptimizer getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MailSystemOptimizer INSTANCE = new MailSystemOptimizer();
	}
}
