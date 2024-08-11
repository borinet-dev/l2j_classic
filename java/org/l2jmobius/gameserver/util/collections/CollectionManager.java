package org.l2jmobius.gameserver.util.collections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jmobius.commons.database.DatabaseFactory;

public class CollectionManager
{
	private static final CollectionManager _instance = new CollectionManager();
	private final static Map<Integer, CollectionData> collections = new HashMap<>();
	
	public static CollectionManager getInstance()
	{
		return _instance;
	}
	
	private CollectionManager()
	{
		loadCollections();
	}
	
	public static void loadCollections()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM collections WHERE item_entries IS NOT NULL AND item_entries;");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String reward = rs.getString("reward");
				
				List<CollectionData.ItemEntry> items = new ArrayList<>();
				String[] itemEntries = rs.getString("item_entries").split(",");
				for (String itemEntry : itemEntries)
				{
					String[] parts = itemEntry.split(";");
					int itemId = Integer.parseInt(parts[0]);
					int enchantLevel = parts.length > 1 ? Integer.parseInt(parts[1]) : 0; // 인챈트 수치가 없는 경우 0으로 처리
					items.add(new CollectionData.ItemEntry(itemId, enchantLevel));
				}
				
				if (collections != null)
				{
					collections.put(id, new CollectionData(id, name, items, reward));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public CollectionData getCollection(int id)
	{
		return collections.get(id);
	}
	
	public Map<Integer, CollectionData> getCollections()
	{
		return collections;
	}
}