package org.l2jmobius.gameserver.util.collections;

import java.util.List;

public class CollectionData
{
	private final int id;
	private final String name;
	private final List<ItemEntry> items;
	private final String reward;
	
	public CollectionData(int id, String name, List<ItemEntry> items, String reward)
	{
		this.id = id;
		this.name = name;
		this.items = items;
		this.reward = reward;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public List<ItemEntry> getItems()
	{
		return items;
	}
	
	public String getReward()
	{
		return reward;
	}
	
	public static class ItemEntry
	{
		private final int itemId;
		private final int enchantLevel;
		
		public ItemEntry(int itemId, int enchantLevel)
		{
			this.itemId = itemId;
			this.enchantLevel = enchantLevel;
		}
		
		public int getItemId()
		{
			return itemId;
		}
		
		public int getEnchantLevel()
		{
			return enchantLevel;
		}
	}
}