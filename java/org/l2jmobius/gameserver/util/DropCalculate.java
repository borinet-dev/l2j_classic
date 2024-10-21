package org.l2jmobius.gameserver.util;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.stats.Stat;

public class DropCalculate
{
	public static double[] calculateDropRates(Player player, CustomDropHolder cbDropHolder, ItemTemplate item)
	{
		double rateChance = 1;
		double rateAmount = 1;
		double dropAmountAdenaEffectBonus = player.getStat().getMul(Stat.BONUS_DROP_ADENA, 1);
		double dropAmountEffectBonus = player.getStat().getMul(Stat.BONUS_DROP_AMOUNT, 1);
		double dropRateEffectBonus = player.getStat().getMul(Stat.BONUS_DROP_RATE, 1);
		double spoilRateEffectBonus = player.getStat().getMul(Stat.BONUS_SPOIL_RATE, 1);
		boolean usingPole = (player.getActiveWeaponItem() != null) && (player.getActiveWeaponItem().getItemType() == WeaponType.POLE);
		
		if (cbDropHolder.isSpoil)
		{
			rateChance = (BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_SPOIL_DROP_CHANCE_MULTIPLIER : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER_WEEKEND : Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER);
			rateAmount = Config.RATE_SPOIL_DROP_AMOUNT_MULTIPLIER;
			
			if (Config.PREMIUM_SYSTEM_ENABLED && player.hasPremiumStatus())
			{
				rateChance *= Config.PREMIUM_RATE_SPOIL_CHANCE;
				rateAmount *= Config.PREMIUM_RATE_SPOIL_AMOUNT;
			}
			
			rateChance *= spoilRateEffectBonus;
		}
		else
		{
			if (item.isArmors() || item.isWeapons() || item.isAccessorys())
			{
				rateChance *= (BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_FINISHED_ITEM : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_FINISHED_ITEM_WEEKEND : Config.RATE_FINISHED_ITEM);
			}
			if (Config.RATE_DROP_CHANCE_BY_ID.get(cbDropHolder.itemId) != null)
			{
				rateChance *= Config.RATE_DROP_CHANCE_BY_ID.get(cbDropHolder.itemId);
			}
			else if (item.hasExImmediateEffect())
			{
				rateChance *= Config.RATE_HERB_DROP_CHANCE_MULTIPLIER;
			}
			else if (cbDropHolder.isRaid)
			{
				rateAmount *= Config.RATE_RAID_DROP_CHANCE_MULTIPLIER;
			}
			else
			{
				rateChance *= (BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_DEATH_DROP_CHANCE_MULTIPLIER : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER_WEEKEND : Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER);
			}
			
			if (Config.RATE_DROP_AMOUNT_BY_ID.get(cbDropHolder.itemId) != null)
			{
				rateAmount *= Config.RATE_DROP_AMOUNT_BY_ID.get(cbDropHolder.itemId);
			}
			else if (item.hasExImmediateEffect())
			{
				rateAmount *= Config.RATE_HERB_DROP_AMOUNT_MULTIPLIER;
			}
			else if (cbDropHolder.isRaid)
			{
				rateAmount *= Config.RATE_RAID_DROP_AMOUNT_MULTIPLIER;
			}
			else
			{
				rateAmount *= Config.RATE_DEATH_DROP_AMOUNT_MULTIPLIER;
			}
			
			if (Config.PREMIUM_SYSTEM_ENABLED && player.hasPremiumStatus())
			{
				if (Config.PREMIUM_RATE_DROP_CHANCE_BY_ID.get(cbDropHolder.itemId) != null)
				{
					rateChance *= Config.PREMIUM_RATE_DROP_CHANCE_BY_ID.get(cbDropHolder.itemId);
				}
				else if (item.hasExImmediateEffect())
				{
					// Premium herb chance can be implemented if needed
				}
				else if (cbDropHolder.isRaid)
				{
					// Premium raid chance can be implemented if needed
				}
				else
				{
					rateChance *= (item.isArmors() || item.isWeapons() || item.isAccessorys()) ? Config.PREMIUM_RATE_FINISHED_ITEM : Config.PREMIUM_RATE_DROP_CHANCE;
				}
				
				if (Config.PREMIUM_RATE_DROP_AMOUNT_BY_ID.get(cbDropHolder.itemId) != null)
				{
					rateAmount *= Config.PREMIUM_RATE_DROP_AMOUNT_BY_ID.get(cbDropHolder.itemId);
				}
				else if (item.hasExImmediateEffect())
				{
					// Premium herb amount can be implemented if needed
				}
				else if (cbDropHolder.isRaid)
				{
					// Premium raid amount can be implemented if needed
				}
				else
				{
					rateAmount *= Config.PREMIUM_RATE_DROP_AMOUNT;
				}
			}
			
			rateAmount *= dropAmountEffectBonus;
			if (item.getId() == Inventory.ADENA_ID)
			{
				rateAmount *= dropAmountAdenaEffectBonus;
			}
			rateChance *= dropRateEffectBonus;
			rateChance *= Config.ENABLE_POLE_RATE ? (usingPole ? Config.POLE_ITEM_RATE : 1) : 1;
		}
		
		return new double[]
		{
			rateChance,
			rateAmount
		};
	}
	
	public static double calculateRateChance(CustomDropHolder dropHolder, Player player, ItemTemplate item)
	{
		double rateChance = 1;
		double spoilRateEffectBonus = player.getStat().getMul(Stat.BONUS_SPOIL_RATE, 1);
		double dropRateEffectBonus = player.getStat().getMul(Stat.BONUS_DROP_RATE, 1);
		
		if (dropHolder.isSpoil)
		{
			rateChance *= spoilRateEffectBonus;
		}
		else
		{
			boolean usingPole = (player.getActiveWeaponItem() != null) && (player.getActiveWeaponItem().getItemType() == WeaponType.POLE);
			
			if (item.isArmors() || item.isWeapons() || item.isAccessorys())
			{
				rateChance *= Config.RATE_FINISHED_ITEM;
			}
			
			if (Config.RATE_DROP_CHANCE_BY_ID.get(dropHolder.itemId) != null)
			{
				rateChance *= Config.RATE_DROP_CHANCE_BY_ID.get(dropHolder.itemId);
			}
			else if (item.hasExImmediateEffect())
			{
				rateChance *= Config.RATE_HERB_DROP_CHANCE_MULTIPLIER;
			}
			else if (dropHolder.isRaid)
			{
				rateChance *= Config.RATE_RAID_DROP_CHANCE_MULTIPLIER;
			}
			
			rateChance *= dropRateEffectBonus;
			rateChance *= Config.ENABLE_POLE_RATE ? (usingPole ? Config.POLE_ITEM_RATE : 1) : 1;
		}
		return rateChance;
	}
	
	public static double calculateRateAmount(CustomDropHolder dropHolder, Player player, ItemTemplate item)
	{
		double rateAmount = 1;
		double dropAmountEffectBonus = player.getStat().getMul(Stat.BONUS_DROP_AMOUNT, 1);
		double dropAmountAdenaEffectBonus = player.getStat().getMul(Stat.BONUS_DROP_ADENA, 1);
		
		if (dropHolder.isRaid)
		{
			rateAmount *= Config.RATE_RAID_DROP_AMOUNT_MULTIPLIER;
		}
		else
		{
			rateAmount *= dropAmountEffectBonus;
			if (item.getId() == Inventory.ADENA_ID)
			{
				rateAmount *= dropAmountAdenaEffectBonus;
			}
		}
		return rateAmount;
	}
}