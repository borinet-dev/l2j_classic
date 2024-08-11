package org.l2jmobius.gameserver.util;

import java.sql.Date;

public class EnchantEntry
{
	private int itemObjId;
	private int itemId;
	private int charId;
	private String itemName;
	private int enchantLevel;
	private Date enchantDate;
	private boolean blessed;
	
	public EnchantEntry(int itemObjId, int itemId, int charId, String itemName, int enchantLevel, Date enchantDate, boolean blessed)
	{
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.charId = charId;
		this.itemName = itemName;
		this.enchantLevel = enchantLevel;
		this.enchantDate = enchantDate;
		this.blessed = blessed;
	}
	
	public boolean getBlessed()
	{
		return blessed;
	}
	
	public void setBlessed(boolean blessed)
	{
		this.blessed = blessed;
	}
	
	public int getItemObjId()
	{
		return itemObjId;
	}
	
	public void setItemObjId(int itemObjId)
	{
		this.itemObjId = itemObjId;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public void setItemId(int itemId)
	{
		this.itemId = itemId;
	}
	
	public int getCharId()
	{
		return charId;
	}
	
	public void setCharId(int charId)
	{
		this.charId = charId;
	}
	
	public String getItemName()
	{
		return itemName;
	}
	
	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}
	
	public int getEnchantLevel()
	{
		return enchantLevel;
	}
	
	public void setEnchantLevel(int enchantLevel)
	{
		this.enchantLevel = enchantLevel;
	}
	
	public Date getEnchantDate()
	{
		return enchantDate;
	}
	
	public void setEnchantDate(Date enchantDate)
	{
		this.enchantDate = enchantDate;
	}
	
	@Override
	public String toString()
	{
		return "EnchantEntry{" + "itemObjId=" + itemObjId + ", itemId=" + itemId + ", charId=" + charId + ", itemName='" + itemName + '\'' + ", enchantLevel=" + enchantLevel + ", enchantDate=" + enchantDate + '}';
	}
}
