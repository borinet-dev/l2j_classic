package org.l2jmobius.gameserver.util.collections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.database.DatabaseFactory;

public class CollectionDatabaseManager
{
	
	// 수집 메뉴 완료 여부를 데이터베이스에서 로드하는 메서드
	public static boolean loadCollectionCompletionFromDatabase(String account, int collectionId)
	{
		boolean completed = false;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT completed FROM collection_completion WHERE accounts = ? AND collection_id = ?"))
		{
			ps.setString(1, account);
			ps.setInt(2, collectionId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					completed = rs.getBoolean("completed");
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return completed;
	}
	
	// 수집 메뉴 완료 여부를 데이터베이스에 저장하는 메서드
	public static void saveCollectionCompletionToDatabase(String account, int collectionId, boolean completed)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO collection_completion (accounts, collection_id, completed) VALUES (?, ?, ?)"))
		{
			ps.setString(1, account);
			ps.setInt(2, collectionId);
			ps.setBoolean(3, completed);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static List<String> loadCollectionRewardsForPlayer(String account)
	{
		List<String> rewards = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT c.reward FROM collection_completion cc JOIN collections c ON cc.collection_id = c.id WHERE cc.accounts = ?"))
		{
			ps.setString(1, account);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					rewards.add(rs.getString("reward"));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return rewards;
	}
}