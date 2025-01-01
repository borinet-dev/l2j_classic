package org.l2jmobius.gameserver.itemlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;

public class ItemLogManager
{
	private static final BlockingQueue<ItemLogEntry> logQueue = new LinkedBlockingQueue<>();
	private static final int BATCH_SIZE = 100; // 한 번에 처리할 로그 개수
	
	static
	{
		Thread logProcessor = new Thread(() ->
		{
			while (true)
			{
				try
				{
					processLogs();
				}
				catch (Exception e)
				{
					Logger.getLogger(ItemLogManager.class.getName()).log(Level.WARNING, "Failed to process item logs", e);
				}
			}
		});
		logProcessor.setDaemon(true);
		logProcessor.start();
	}
	
	public static void addLog(String process, Item item, long oldCount, long newCount, String actorName, int actorId, String referenceName)
	{
		if (!Config.LOG_TABLE_ITEMS)
		{
			return;
		}
		
		if (!process.contains("메일"))
		{
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
		}
		
		logQueue.add(new ItemLogEntry(process, item, oldCount, newCount, actorName, actorId, referenceName));
	}
	
	private static void processLogs() throws SQLException
	{
		if (logQueue.isEmpty())
		{
			try
			{
				Thread.sleep(10); // 부하 감소를 위해 대기
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO " + ItemLogDatabase.getInstance().getTableName() + " (process, item_id, enchant_level, item_name, old_count, new_count, actor_name, actor_id, reference_name, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)"))
		{
			int count = 0;
			while (!logQueue.isEmpty() && (count < BATCH_SIZE))
			{
				ItemLogEntry log = logQueue.poll();
				if (log != null)
				{
					ps.setString(1, log.getProcess());
					ps.setInt(2, log.getObjectId());
					ps.setInt(3, log.getEnchantLevel());
					ps.setString(4, log.getItemName());
					ps.setLong(5, log.getOldCount());
					ps.setLong(6, log.getNewCount());
					ps.setString(7, log.getActorName());
					ps.setInt(8, log.getActorId());
					ps.setString(9, log.getReferenceName());
					ps.addBatch();
					count++;
				}
			}
			ps.executeBatch(); // 배치 삽입 실행
		}
	}
}
