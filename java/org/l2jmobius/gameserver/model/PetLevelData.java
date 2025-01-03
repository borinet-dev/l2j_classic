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
package org.l2jmobius.gameserver.model;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * Stats definition for each pet level.
 * @author JIV, Zoey76
 */
public class PetLevelData
{
	private final int _ownerExpTaken;
	private final int _petFeedBattle;
	private final int _petFeedNormal;
	private final double _petMAtk;
	private final long _petMaxExp;
	private final int _petMaxFeed;
	private final double _petMaxHP;
	private final double _petMaxMP;
	private final double _petMDef;
	private final double _petPAtk;
	private final double _petPDef;
	private final float _petRegenHP;
	private final float _petRegenMP;
	private final short _petSoulShot;
	private final short _petSpiritShot;
	private final double _walkSpeedOnRide;
	private final double _runSpeedOnRide;
	private final double _slowSwimSpeedOnRide;
	private final double _fastSwimSpeedOnRide;
	private final double _slowFlySpeedOnRide;
	private final double _fastFlySpeedOnRide;
	
	public PetLevelData(StatSet set)
	{
		_ownerExpTaken = set.getInt("get_exp_type");
		_petMaxExp = set.getLong("exp");
		_petMaxHP = set.getFloat("org_hp") * Config.PET_SUMMON_CUSTOM_HP_MULTIPLIER;
		_petMaxMP = set.getFloat("org_mp") * Config.PET_SUMMON_CUSTOM_MP_MULTIPLIER;
		_petPAtk = set.getFloat("org_pattack") * Config.PET_SUMMON_CUSTOM_PATK_MULTIPLIER;
		_petPDef = set.getFloat("org_pdefend") * Config.PET_SUMMON_CUSTOM_PDEF_MULTIPLIER;
		_petMAtk = set.getFloat("org_mattack") * Config.PET_SUMMON_CUSTOM_MATK_MULTIPLIER;
		_petMDef = set.getFloat("org_mdefend") * Config.PET_SUMMON_CUSTOM_MDEF_MULTIPLIER;
		_petMaxFeed = set.getInt("max_meal");
		_petFeedBattle = set.getInt("consume_meal_in_battle");
		_petFeedNormal = set.getInt("consume_meal_in_normal");
		_petRegenHP = set.getFloat("org_hp_regen");
		_petRegenMP = set.getFloat("org_mp_regen");
		_petSoulShot = set.getShort("soulshot_count");
		_petSpiritShot = set.getShort("spiritshot_count");
		_walkSpeedOnRide = set.getDouble("walkSpeedOnRide", 0);
		_runSpeedOnRide = set.getDouble("runSpeedOnRide", 0) * Config.PET_SUMMON_CUSTOM_RUN_SPEED_MULTIPLIER;
		_slowSwimSpeedOnRide = set.getDouble("slowSwimSpeedOnRide", 0);
		_fastSwimSpeedOnRide = set.getDouble("fastSwimSpeedOnRide", 0) * Config.PET_SUMMON_CUSTOM_RUN_SPEED_MULTIPLIER;
		_slowFlySpeedOnRide = set.getDouble("slowFlySpeedOnRide", 0);
		_fastFlySpeedOnRide = set.getDouble("fastFlySpeedOnRide", 0);
	}
	
	/**
	 * @return the owner's experience points consumed by the pet.
	 */
	public int getOwnerExpTaken()
	{
		return _ownerExpTaken;
	}
	
	/**
	 * @return the pet's food consume rate at battle state.
	 */
	public int getPetFeedBattle()
	{
		return _petFeedBattle;
	}
	
	/**
	 * @return the pet's food consume rate at normal state.
	 */
	public int getPetFeedNormal()
	{
		return _petFeedNormal;
	}
	
	/**
	 * @return the pet's Magical Attack.
	 */
	public double getPetMAtk()
	{
		return _petMAtk;
	}
	
	/**
	 * @return the pet's maximum experience points.
	 */
	public long getPetMaxExp()
	{
		return _petMaxExp;
	}
	
	/**
	 * @return the pet's maximum feed points.
	 */
	public int getPetMaxFeed()
	{
		return _petMaxFeed;
	}
	
	/**
	 * @return the pet's maximum HP.
	 */
	public double getPetMaxHP()
	{
		return _petMaxHP;
	}
	
	/**
	 * @return the pet's maximum MP.
	 */
	public double getPetMaxMP()
	{
		return _petMaxMP;
	}
	
	/**
	 * @return the pet's Magical Defense.
	 */
	public double getPetMDef()
	{
		return _petMDef;
	}
	
	/**
	 * @return the pet's Physical Attack.
	 */
	public double getPetPAtk()
	{
		return _petPAtk;
	}
	
	/**
	 * @return the pet's Physical Defense.
	 */
	public double getPetPDef()
	{
		return _petPDef;
	}
	
	/**
	 * @return the pet's HP regeneration rate.
	 */
	public float getPetRegenHP()
	{
		return _petRegenHP;
	}
	
	/**
	 * @return the pet's MP regeneration rate.
	 */
	public float getPetRegenMP()
	{
		return _petRegenMP;
	}
	
	/**
	 * @return the pet's soulshot use count.
	 */
	public short getPetSoulShot()
	{
		return _petSoulShot;
	}
	
	/**
	 * @return the pet's spiritshot use count.
	 */
	public short getPetSpiritShot()
	{
		return _petSpiritShot;
	}
	
	/**
	 * @param stat movement type
	 * @return the base riding speed of given movement type.
	 */
	public double getSpeedOnRide(Stat stat)
	{
		switch (stat)
		{
			case WALK_SPEED:
			{
				return _walkSpeedOnRide;
			}
			case RUN_SPEED:
			{
				return _runSpeedOnRide;
			}
			case SWIM_WALK_SPEED:
			{
				return _slowSwimSpeedOnRide;
			}
			case SWIM_RUN_SPEED:
			{
				return _fastSwimSpeedOnRide;
			}
			case FLY_RUN_SPEED:
			{
				return _slowFlySpeedOnRide;
			}
			case FLY_WALK_SPEED:
			{
				return _fastFlySpeedOnRide;
			}
		}
		return 0;
	}
}
