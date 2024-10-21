package org.l2jmobius.gameserver.util;

import org.l2jmobius.gameserver.enums.DropType;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.holders.DropHolder;

public class CustomDropHolder
{
	public final int itemId;
	public final int npcId;
	public final String npcName;
	public final byte npcLevel;
	public final long min;
	public final long max;
	public final double chance;
	public final boolean isSpoil;
	public final boolean isRaid;
	
	public CustomDropHolder(NpcTemplate npcTemplate, DropHolder dropHolder)
	{
		isSpoil = dropHolder.getDropType() == DropType.SPOIL;
		itemId = dropHolder.getItemId();
		npcId = npcTemplate.getId();
		npcName = npcTemplate.getName();
		npcLevel = npcTemplate.getLevel();
		min = dropHolder.getMin();
		max = dropHolder.getMax();
		chance = dropHolder.getChance();
		isRaid = npcTemplate.getType().equals("RaidBoss") || npcTemplate.getType().equals("GrandBoss");
	}
}
