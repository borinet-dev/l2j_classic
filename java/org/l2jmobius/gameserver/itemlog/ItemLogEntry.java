package org.l2jmobius.gameserver.itemlog;

import org.l2jmobius.gameserver.model.item.instance.Item;

public class ItemLogEntry
{
	private final String process;
	private final int itemObjectId;
	private final int enchantLevel;
	private final String itemName;
	private final long oldCount;
	private final long newCount;
	private final String actorName;
	private final int actorId;
	private final String referenceName;
	
	public ItemLogEntry(String process, Item item, long oldCount, long newCount, String actorName, int actorId, String referenceName)
	{
		this.process = process;
		this.itemObjectId = item.getObjectId();
		this.enchantLevel = item.getEnchantLevel();
		this.itemName = item.getName();
		this.oldCount = oldCount;
		this.newCount = newCount;
		this.actorName = actorName;
		this.actorId = actorId;
		this.referenceName = referenceName;
	}
	
	public String getProcess()
	{
		return process;
	}
	
	public int getObjectId()
	{
		return itemObjectId;
	}
	
	public int getEnchantLevel()
	{
		return enchantLevel;
	}
	
	public String getItemName()
	{
		return itemName;
	}
	
	public long getOldCount()
	{
		return oldCount;
	}
	
	public long getNewCount()
	{
		return newCount;
	}
	
	public String getActorName()
	{
		return actorName;
	}
	
	public int getActorId()
	{
		return actorId;
	}
	
	public String getReferenceName()
	{
		return referenceName;
	}
}
