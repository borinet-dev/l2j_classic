/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.model.stats.finalizers;

import java.util.OptionalDouble;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.IStatFunction;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author UnAfraid
 */
public class PAttackFinalizer implements IStatFunction
{
	@Override
	public double calc(Creature creature, OptionalDouble base, Stat stat)
	{
		throwIfPresent(base);
		
		double baseValue = calcWeaponBaseValue(creature, stat);
		baseValue += calcEnchantedItemBonus(creature, stat);
		if (creature.isPlayer())
		{
			// Enchanted chest bonus
			if (Config.ENCHANT_LEVEL_FOR_ABILITY)
			{
				baseValue += calcEnchantBodyPart(creature, ItemTemplate.SLOT_CHEST, ItemTemplate.SLOT_FULL_ARMOR);
			}
			if ((creature.getActiveWeaponItem() != null) && (creature.getActiveWeaponItem().getId() == 32773))
			{
				return validateValue(creature, 0, 0, Config.MAX_PATK);
			}
		}
		
		if (Config.CHAMPION_ENABLE && creature.isChampion())
		{
			baseValue *= Config.CHAMPION_ATK;
		}
		if (creature.isRaid())
		{
			baseValue *= Config.RAID_PATTACK_MULTIPLIER;
		}
		final Item weapon = creature.getActiveWeaponInstance();
		if ((weapon != null))
		{
			int classId = creature.getActingPlayer().getClassId().getId();
			WeaponType weaponType = creature.getActiveWeaponItem().getItemType();
			if (creature.isPlayer() && (weaponType == WeaponType.BOW) && ClassId.isBow(classId))
			{
				// 주석: 활을 사용하는 경우 STR, DEX와 레벨 보정을 적용
				baseValue *= BaseStat.DEX.calcBonus(creature) * creature.getLevelMod() * 0.8;
				baseValue *= BaseStat.STR.calcBonus(creature) * creature.getLevelMod() * 0.6;
			}
			else
			{
				// 주석: 활을 사용하지 않는 경우 STR와 레벨 보정을 적용
				baseValue *= BaseStat.STR.calcBonus(creature) * creature.getLevelMod();
			}
		}
		else
		{
			// 주석: 활을 사용하지 않는 경우 STR와 레벨 보정을 적용
			baseValue *= BaseStat.STR.calcBonus(creature) * creature.getLevelMod();
		}
		return validateValue(creature, Stat.defaultValue(creature, stat, baseValue), 0, creature.isPlayable() ? Config.MAX_PATK : Double.MAX_VALUE);
	}
	
	@Override
	public double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		if (isBlessed)
		{
			return (3 * Math.max(enchantLevel - 3, 0)) + (3 * Math.max(enchantLevel - 6, 0));
		}
		return (2 * Math.max(enchantLevel - 3, 0)) + (2 * Math.max(enchantLevel - 6, 0));
	}
}
