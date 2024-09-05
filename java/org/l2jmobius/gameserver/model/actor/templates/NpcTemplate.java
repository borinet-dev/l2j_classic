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
package org.l2jmobius.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.enums.AISkillScope;
import org.l2jmobius.gameserver.enums.AIType;
import org.l2jmobius.gameserver.enums.DropType;
import org.l2jmobius.gameserver.enums.ElementalType;
import org.l2jmobius.gameserver.enums.MpRewardAffectType;
import org.l2jmobius.gameserver.enums.MpRewardType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.enums.Sex;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.holders.DropGroupHolder;
import org.l2jmobius.gameserver.model.holders.DropHolder;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.interfaces.IIdentifiable;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Util;

/**
 * NPC template.
 * @author NosBit
 */
public class NpcTemplate extends CreatureTemplate implements IIdentifiable
{
	private static final Logger LOGGER = Logger.getLogger(NpcTemplate.class.getName());
	
	private int _id;
	private int _displayId;
	public byte _level;
	private String _type;
	private String _name;
	private boolean _usingServerSideName;
	private String _title;
	private boolean _usingServerSideTitle;
	private StatSet _parameters;
	private Sex _sex;
	private int _chestId;
	private int _rhandId;
	private int _lhandId;
	private int _weaponEnchant;
	private double _exp;
	private double _sp;
	private double _raidPoints;
	private boolean _unique;
	private boolean _attackable;
	private boolean _targetable;
	private boolean _talkable;
	private boolean _isQuestMonster;
	private boolean _undying;
	private boolean _showName;
	private boolean _randomWalk;
	private boolean _randomAnimation;
	private boolean _flying;
	private boolean _fakePlayer;
	private boolean _fakePlayerTalkable;
	private boolean _canMove;
	private boolean _noSleepMode;
	private boolean _passableDoor;
	private boolean _hasSummoner;
	private boolean _canBeSown;
	private boolean _canBeCrt;
	private boolean _isDeathPenalty;
	private int _corpseTime;
	private AIType _aiType;
	private int _aggroRange;
	private int _clanHelpRange;
	private boolean _isChaos;
	private boolean _isAggressive;
	private int _soulShot;
	private int _spiritShot;
	private int _soulShotChance;
	private int _spiritShotChance;
	private int _minSkillChance;
	private int _maxSkillChance;
	private double _hitTimeFactor;
	private double _hitTimeFactorSkill;
	private Map<Integer, Skill> _skills;
	private Map<AISkillScope, List<Skill>> _aiSkillLists;
	private Set<Integer> _clans;
	private Set<Integer> _ignoreClanNpcIds;
	private List<DropGroupHolder> _dropGroups;
	private List<DropHolder> _dropListDeath;
	private List<DropHolder> _dropListSpoil;
	private float _collisionRadiusGrown;
	private float _collisionHeightGrown;
	private int _mpRewardValue;
	private MpRewardType _mpRewardType;
	private int _mpRewardTicks;
	private MpRewardAffectType _mpRewardAffectType;
	private ElementalType _elementalType;
	private long _attributeExp;
	
	/**
	 * Constructor of Creature.
	 * @param set The StatSet object to transfer data to the method
	 */
	public NpcTemplate(StatSet set)
	{
		super(set);
	}
	
	@Override
	public void set(StatSet set)
	{
		super.set(set);
		_id = set.getInt("id");
		_displayId = set.getInt("displayId", _id);
		_level = set.getByte("level", (byte) 70);
		_type = set.getString("type", "Folk");
		_name = set.getString("name", "");
		_usingServerSideName = set.getBoolean("usingServerSideName", false);
		_title = set.getString("title", "");
		_usingServerSideTitle = set.getBoolean("usingServerSideTitle", false);
		setRace(set.getEnum("race", Race.class, Race.NONE));
		_sex = set.getEnum("sex", Sex.class, Sex.ETC);
		_elementalType = set.getEnum("elementalType", ElementalType.class, ElementalType.NONE);
		_chestId = set.getInt("chestId", 0);
		if ((_chestId > 0) && (ItemTable.getInstance().getTemplate(_chestId) == null))
		{
			LOGGER.warning("NpcTemplate " + _id + ": Could not find item for chestId with id " + _chestId + ".");
		}
		_rhandId = set.getInt("rhandId", 0);
		if ((_rhandId > 0) && (ItemTable.getInstance().getTemplate(_rhandId) == null))
		{
			LOGGER.warning("NpcTemplate " + _id + ": Could not find item for rhandId with id " + _rhandId + ".");
		}
		_lhandId = set.getInt("lhandId", 0);
		if ((_lhandId > 0) && (ItemTable.getInstance().getTemplate(_lhandId) == null))
		{
			LOGGER.warning("NpcTemplate " + _id + ": Could not find item for lhandId with id " + _lhandId + ".");
		}
		
		_weaponEnchant = set.getInt("weaponEnchant", 0);
		_exp = set.getDouble("exp", 0);
		_sp = set.getDouble("sp", 0);
		_raidPoints = set.getDouble("raidPoints", 0);
		_attributeExp = set.getLong("attributeExp", 0);
		_unique = set.getBoolean("unique", false);
		_attackable = set.getBoolean("attackable", true);
		_targetable = set.getBoolean("targetable", true);
		_talkable = set.getBoolean("talkable", true);
		_isQuestMonster = _title.contains("Quest");
		_undying = set.getBoolean("undying", !_type.equals("Monster") && !_type.equals("RaidBoss") && !_type.equals("GrandBoss"));
		_showName = set.getBoolean("showName", true);
		_randomWalk = set.getBoolean("randomWalk", !_type.equals("Guard"));
		_randomAnimation = set.getBoolean("randomAnimation", true);
		_flying = set.getBoolean("flying", false);
		_fakePlayer = set.getBoolean("fakePlayer", false);
		_fakePlayerTalkable = set.getBoolean("fakePlayerTalkable", true);
		_canMove = set.getBoolean("canMove", true);
		_noSleepMode = set.getBoolean("noSleepMode", false);
		_passableDoor = set.getBoolean("passableDoor", false);
		_hasSummoner = set.getBoolean("hasSummoner", false);
		_canBeSown = set.getBoolean("canBeSown", false);
		_canBeCrt = set.getBoolean("exCrtEffect", true);
		_isDeathPenalty = set.getBoolean("isDeathPenalty", false);
		_corpseTime = set.getInt("corpseTime", Config.DEFAULT_CORPSE_TIME);
		_aiType = set.getEnum("aiType", AIType.class, AIType.FIGHTER);
		_aggroRange = set.getInt("aggroRange", 0);
		_clanHelpRange = set.getInt("clanHelpRange", 0);
		_isChaos = set.getBoolean("isChaos", false);
		_isAggressive = set.getBoolean("isAggressive", false);
		_soulShot = set.getInt("soulShot", 0);
		_spiritShot = set.getInt("spiritShot", 0);
		_soulShotChance = set.getInt("shotShotChance", 0);
		_spiritShotChance = set.getInt("spiritShotChance", 0);
		_minSkillChance = set.getInt("minSkillChance", 7);
		_maxSkillChance = set.getInt("maxSkillChance", 15);
		_hitTimeFactor = set.getInt("hitTime", 100) / 100d;
		_hitTimeFactorSkill = set.getInt("hitTimeSkill", 100) / 100d;
		_collisionRadiusGrown = set.getFloat("collisionRadiusGrown", 0);
		_collisionHeightGrown = set.getFloat("collisionHeightGrown", 0);
		_mpRewardValue = set.getInt("mpRewardValue", 0);
		_mpRewardType = set.getEnum("mpRewardType", MpRewardType.class, MpRewardType.DIFF);
		_mpRewardTicks = set.getInt("mpRewardTicks", 0);
		_mpRewardAffectType = set.getEnum("mpRewardAffectType", MpRewardAffectType.class, MpRewardAffectType.SOLO);
		if (Config.ENABLE_NPC_STAT_MULTIPLIERS) // Custom NPC Stat Multipliers
		{
			switch (_type)
			{
				case "Pet":
				{
					_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.PET_SUMMON_CUSTOM_HP_MULTIPLIER);
					_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.PET_SUMMON_CUSTOM_MP_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.PET_SUMMON_CUSTOM_PATK_MULTIPLIER);
					_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.PET_SUMMON_CUSTOM_MATK_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.PET_SUMMON_CUSTOM_PDEF_MULTIPLIER);
					_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.PET_SUMMON_CUSTOM_MDEF_MULTIPLIER);
					_baseValues.put(Stat.CRITICAL_RATE, getBaseCritRate() * Config.PET_SUMMON_CUSTOM_CRITICAL_MULTIPLIER);
					_baseValues.put(Stat.MAGIC_CRITICAL_RATE, getBaseMCritRate() * Config.PET_SUMMON_CUSTOM_MAGIC_CRITICAL_MULTIPLIER);
					_baseValues.put(Stat.ACCURACY_COMBAT, getBaseAccCombat() * Config.PET_SUMMON_CUSTOM_ACCURACY_MULTIPLIER);
					_baseValues.put(Stat.ACCURACY_MAGIC, getBaseAccMagic() * Config.PET_SUMMON_CUSTOM_ACCURACY_MAGIC_MULTIPLIER);
					_baseValues.put(Stat.EVASION_RATE, getBaseEvasion() * Config.PET_SUMMON_CUSTOM_EVASION_MULTIPLIER);
					_baseValues.put(Stat.MAGIC_EVASION_RATE, getBaseEvasionMagic() * Config.PET_SUMMON_CUSTOM_EVASION_MAGIC_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_ATTACK_SPEED, getBasePAtkSpd() * Config.PET_SUMMON_CUSTOM_PHYSICAL_ATTACK_SPEED_MULTIPLIER);
					_baseValues.put(Stat.MAGIC_ATTACK_SPEED, getBaseMAtkSpd() * Config.PET_SUMMON_CUSTOM_MAGIC_ATTACK_SPEED_MULTIPLIER);
					_baseValues.put(Stat.RUN_SPEED, getBaseRunSpk() * Config.PET_SUMMON_CUSTOM_RUN_SPEED_MULTIPLIER);
					break;
				}
				case "Monster":
				{
					if (isSevenSignsParty())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_NEC_PARTY);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_NEC_PARTY);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_NEC_PARTY);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_NEC_PARTY);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_NEC_PARTY);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_NEC_PARTY);
					}
					else if (isAlligatorIsland())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_ALLIGATORISLAND);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_ALLIGATORISLAND);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_ALLIGATORISLAND);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_ALLIGATORISLAND);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_ALLIGATORISLAND);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_ALLIGATORISLAND);
					}
					else if (isSevenSignsSolo())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_NEC_SOLO);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_NEC_SOLO);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_NEC_SOLO);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_NEC_SOLO);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_NEC_SOLO);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_NEC_SOLO);
					}
					else if (isForgotten())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_FORGOTTEN);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_FORGOTTEN);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_FORGOTTEN);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_FORGOTTEN);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_FORGOTTEN);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_FORGOTTEN);
						_baseValues.put(Stat.ACCURACY_COMBAT, getBaseAccCombat() * Config.MONSTER_ACCURACY_MULTIPLIER_FORGOTTEN);
						_baseValues.put(Stat.ACCURACY_MAGIC, getBaseAccMagic() * Config.MONSTER_ACCURACY_MAGIC_MULTIPLIER_FORGOTTEN);
						_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER;
						_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER;
					}
					else if (isMonastery())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_MONASTERY);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_MONASTERY);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_MONASTERY);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_MONASTERY);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_MONASTERY);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_MONASTERY);
						_baseValues.put(Stat.ACCURACY_COMBAT, getBaseAccCombat() * Config.MONSTER_ACCURACY_MULTIPLIER_MONASTERY);
						_baseValues.put(Stat.ACCURACY_MAGIC, getBaseAccMagic() * Config.MONSTER_ACCURACY_MAGIC_MULTIPLIER_MONASTERY);
						_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER;
						_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER;
					}
					else if (isElvenFortress())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_ELVENFORTRESS);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_ELVENFORTRESS);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_ELVENFORTRESS);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_ELVENFORTRESS);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_ELVENFORTRESS);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_ELVENFORTRESS);
						_baseValues.put(Stat.ACCURACY_COMBAT, getBaseAccCombat() * Config.MONSTER_ACCURACY_MULTIPLIER_ELVENFORTRESS);
						_baseValues.put(Stat.ACCURACY_MAGIC, getBaseAccMagic() * Config.MONSTER_ACCURACY_MAGIC_MULTIPLIER_ELVENFORTRESS);
						_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER;
						_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER;
					}
					else if (isMithrilMine())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_MITHRIL_MINE);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_MITHRIL_MINE);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_MITHRIL_MINE);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_MITHRIL_MINE);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_MITHRIL_MINE);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_MITHRIL_MINE);
						_baseValues.put(Stat.ACCURACY_COMBAT, getBaseAccCombat() * Config.MONSTER_ACCURACY_MULTIPLIER_MITHRIL_MINE);
						_baseValues.put(Stat.ACCURACY_MAGIC, getBaseAccMagic() * Config.MONSTER_ACCURACY_MAGIC_MULTIPLIER_MITHRIL_MINE);
						_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER;
						_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER;
					}
					else if (isForgeOfTheGods())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_FORGE);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_FORGE);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_FORGE);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_FORGE);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_FORGE);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_FORGE);
						_baseValues.put(Stat.ACCURACY_COMBAT, getBaseAccCombat() * Config.MONSTER_ACCURACY_MULTIPLIER_FORGE);
						_baseValues.put(Stat.ACCURACY_MAGIC, getBaseAccMagic() * Config.MONSTER_ACCURACY_MAGIC_MULTIPLIER_FORGE);
						_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER;
						_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER;
					}
					else if (_level >= 82)
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_82);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_82);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_82);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_82);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_82);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_82);
						_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER_82;
						_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER_82;
					}
					else
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER);
						_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER;
						_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER;
					}
					break;
				}
				case "FeedableBeast":
				{
					_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.CRITICAL_RATE, getBaseCritRate() * Config.MONSTER_CRITICAL_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.MAGIC_CRITICAL_RATE, getBaseMCritRate() * Config.MONSTER_MAGIC_CRITICAL_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.ACCURACY_COMBAT, getBaseAccCombat() * Config.MONSTER_ACCURACY_MULTIPLIER_BEASTFARM);
					_baseValues.put(Stat.ACCURACY_MAGIC, getBaseAccMagic() * Config.MONSTER_ACCURACY_MAGIC_MULTIPLIER_BEASTFARM);
					_aggroRange *= Config.MONSTER_AGRRO_RANGE_MULTIPLIER;
					_clanHelpRange *= Config.MONSTER_CLAN_HELP_RANGE_MULTIPLIER;
					break;
				}
				case "RaidBoss":
				{
					if (RandomBoss())
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.MONSTER_HP_MULTIPLIER_RND_BOSS);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.MONSTER_MP_MULTIPLIER_RND_BOSS);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.MONSTER_PATK_MULTIPLIER_RND_BOSS);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.MONSTER_MATK_MULTIPLIER_RND_BOSS);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.MONSTER_PDEF_MULTIPLIER_RND_BOSS);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.MONSTER_MDEF_MULTIPLIER_RND_BOSS);
					}
					else
					{
						_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.RAIDBOSS_HP_MULTIPLIER);
						_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.RAIDBOSS_MP_MULTIPLIER);
						_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.RAIDBOSS_PATK_MULTIPLIER);
						_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.RAIDBOSS_MATK_MULTIPLIER);
						_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.RAIDBOSS_PDEF_MULTIPLIER);
						_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.RAIDBOSS_MDEF_MULTIPLIER);
						_aggroRange *= Config.RAIDBOSS_AGRRO_RANGE_MULTIPLIER;
						_clanHelpRange *= Config.RAIDBOSS_CLAN_HELP_RANGE_MULTIPLIER;
					}
					break;
				}
				case "GrandBoss":
				{
					_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.GRANDBOSS_HP_MULTIPLIER);
					_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.GRANDBOSS_MP_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.GRANDBOSS_PATK_MULTIPLIER);
					_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.GRANDBOSS_MATK_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.GRANDBOSS_PDEF_MULTIPLIER);
					_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.GRANDBOSS_MDEF_MULTIPLIER);
					_aggroRange *= Config.GRANDBOSS_AGRRO_RANGE_MULTIPLIER;
					_clanHelpRange *= Config.GRANDBOSS_CLAN_HELP_RANGE_MULTIPLIER;
					break;
				}
				case "Guard":
				{
					_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.GUARD_HP_MULTIPLIER);
					_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.GUARD_MP_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.GUARD_PATK_MULTIPLIER);
					_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.GUARD_MATK_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.GUARD_PDEF_MULTIPLIER);
					_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.GUARD_MDEF_MULTIPLIER);
					_aggroRange *= Config.GUARD_AGRRO_RANGE_MULTIPLIER;
					_clanHelpRange *= Config.GUARD_CLAN_HELP_RANGE_MULTIPLIER;
					break;
				}
				case "Defender":
				{
					_baseValues.put(Stat.MAX_HP, getBaseHpMax() * Config.DEFENDER_HP_MULTIPLIER);
					_baseValues.put(Stat.MAX_MP, getBaseMpMax() * Config.DEFENDER_MP_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_ATTACK, getBasePAtk() * Config.DEFENDER_PATK_MULTIPLIER);
					_baseValues.put(Stat.MAGIC_ATTACK, getBaseMAtk() * Config.DEFENDER_MATK_MULTIPLIER);
					_baseValues.put(Stat.PHYSICAL_DEFENCE, getBasePDef() * Config.DEFENDER_PDEF_MULTIPLIER);
					_baseValues.put(Stat.MAGICAL_DEFENCE, getBaseMDef() * Config.DEFENDER_MDEF_MULTIPLIER);
					_aggroRange *= Config.DEFENDER_AGRRO_RANGE_MULTIPLIER;
					_clanHelpRange *= Config.DEFENDER_CLAN_HELP_RANGE_MULTIPLIER;
					break;
				}
			}
		}
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public String getType()
	{
		return _type;
	}
	
	public boolean isType(String type)
	{
		return _type.equalsIgnoreCase(type);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isUsingServerSideName()
	{
		return _usingServerSideName;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isUsingServerSideTitle()
	{
		return _usingServerSideTitle;
	}
	
	public StatSet getParameters()
	{
		return _parameters;
	}
	
	public void setParameters(StatSet set)
	{
		_parameters = set;
	}
	
	public Sex getSex()
	{
		return _sex;
	}
	
	public int getChestId()
	{
		return _chestId;
	}
	
	public int getRHandId()
	{
		return _rhandId;
	}
	
	public int getLHandId()
	{
		return _lhandId;
	}
	
	public int getWeaponEnchant()
	{
		return _weaponEnchant;
	}
	
	public double getExp()
	{
		return _exp;
	}
	
	public double getSP()
	{
		return _sp;
	}
	
	public double getRaidPoints()
	{
		return _raidPoints;
	}
	
	public long getAttributeExp()
	{
		return _attributeExp;
	}
	
	public ElementalType getElementalType()
	{
		return _elementalType;
	}
	
	public boolean isUnique()
	{
		return _unique;
	}
	
	public boolean isAttackable()
	{
		return _attackable;
	}
	
	public boolean isTargetable()
	{
		return _targetable;
	}
	
	public boolean isTalkable()
	{
		return _talkable;
	}
	
	public boolean isQuestMonster()
	{
		return _isQuestMonster;
	}
	
	public boolean isUndying()
	{
		return _undying;
	}
	
	public boolean isShowName()
	{
		return _showName;
	}
	
	public boolean isRandomWalkEnabled()
	{
		return _randomWalk;
	}
	
	public boolean isRandomAnimationEnabled()
	{
		return _randomAnimation;
	}
	
	public boolean isFlying()
	{
		return _flying;
	}
	
	public boolean isFakePlayer()
	{
		return _fakePlayer;
	}
	
	public boolean isFakePlayerTalkable()
	{
		return _fakePlayerTalkable;
	}
	
	public boolean canMove()
	{
		return _canMove;
	}
	
	public boolean isNoSleepMode()
	{
		return _noSleepMode;
	}
	
	public boolean isPassableDoor()
	{
		return _passableDoor;
	}
	
	public boolean hasSummoner()
	{
		return _hasSummoner;
	}
	
	public boolean canBeSown()
	{
		return _canBeSown;
	}
	
	public boolean canBeCrt()
	{
		return _canBeCrt;
	}
	
	public boolean isDeathPenalty()
	{
		return _isDeathPenalty;
	}
	
	public int getCorpseTime()
	{
		return _corpseTime;
	}
	
	public AIType getAIType()
	{
		return _aiType;
	}
	
	public int getAggroRange()
	{
		return _aggroRange;
	}
	
	public int getClanHelpRange()
	{
		return _clanHelpRange;
	}
	
	public boolean isChaos()
	{
		return _isChaos;
	}
	
	public boolean isAggressive()
	{
		return _isAggressive;
	}
	
	public int getSoulShot()
	{
		return _soulShot;
	}
	
	public int getSpiritShot()
	{
		return _spiritShot;
	}
	
	public int getSoulShotChance()
	{
		return _soulShotChance;
	}
	
	public int getSpiritShotChance()
	{
		return _spiritShotChance;
	}
	
	public int getMinSkillChance()
	{
		return _minSkillChance;
	}
	
	public int getMaxSkillChance()
	{
		return _maxSkillChance;
	}
	
	public double getHitTimeFactor()
	{
		return _hitTimeFactor;
	}
	
	public double getHitTimeFactorSkill()
	{
		return _hitTimeFactorSkill;
	}
	
	@Override
	public Map<Integer, Skill> getSkills()
	{
		return _skills;
	}
	
	public void setSkills(Map<Integer, Skill> skills)
	{
		_skills = skills != null ? Collections.unmodifiableMap(skills) : Collections.emptyMap();
	}
	
	public List<Skill> getAISkills(AISkillScope aiSkillScope)
	{
		return _aiSkillLists.getOrDefault(aiSkillScope, Collections.emptyList());
	}
	
	public void setAISkillLists(Map<AISkillScope, List<Skill>> aiSkillLists)
	{
		_aiSkillLists = aiSkillLists != null ? Collections.unmodifiableMap(aiSkillLists) : Collections.emptyMap();
	}
	
	public Set<Integer> getClans()
	{
		return _clans;
	}
	
	public int getMpRewardValue()
	{
		return _mpRewardValue;
	}
	
	public MpRewardType getMpRewardType()
	{
		return _mpRewardType;
	}
	
	public int getMpRewardTicks()
	{
		return _mpRewardTicks;
	}
	
	public MpRewardAffectType getMpRewardAffectType()
	{
		return _mpRewardAffectType;
	}
	
	/**
	 * @param clans A sorted array of clan ids
	 */
	public void setClans(Set<Integer> clans)
	{
		_clans = clans != null ? Collections.unmodifiableSet(clans) : null;
	}
	
	/**
	 * @param clanName clan name to check if it belongs to this NPC template clans.
	 * @param clanNames clan names to check if they belong to this NPC template clans.
	 * @return {@code true} if at least one of the clan names belong to this NPC template clans, {@code false} otherwise.
	 */
	public boolean isClan(String clanName, String... clanNames)
	{
		// Using local variable for the sake of reloading since it can be turned to null.
		final Set<Integer> clans = _clans;
		if (clans == null)
		{
			return false;
		}
		
		int clanId = NpcData.getInstance().getClanId("ALL");
		if (clans.contains(clanId))
		{
			return true;
		}
		
		clanId = NpcData.getInstance().getClanId(clanName);
		if (clans.contains(clanId))
		{
			return true;
		}
		
		for (String name : clanNames)
		{
			clanId = NpcData.getInstance().getClanId(name);
			if (clans.contains(clanId))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param clans A set of clan names to check if they belong to this NPC template clans.
	 * @return {@code true} if at least one of the clan names belong to this NPC template clans, {@code false} otherwise.
	 */
	public boolean isClan(Set<Integer> clans)
	{
		// Using local variable for the sake of reloading since it can be turned to null.
		final Set<Integer> clanSet = _clans;
		if ((clanSet == null) || (clans == null))
		{
			return false;
		}
		
		final int clanId = NpcData.getInstance().getClanId("ALL");
		if (clanSet.contains(clanId))
		{
			return true;
		}
		
		for (Integer id : clans)
		{
			if (clanSet.contains(id))
			{
				return true;
			}
		}
		return false;
	}
	
	public Set<Integer> getIgnoreClanNpcIds()
	{
		return _ignoreClanNpcIds;
	}
	
	/**
	 * @param ignoreClanNpcIds the ignore clan npc ids
	 */
	public void setIgnoreClanNpcIds(Set<Integer> ignoreClanNpcIds)
	{
		_ignoreClanNpcIds = ignoreClanNpcIds != null ? Collections.unmodifiableSet(ignoreClanNpcIds) : null;
	}
	
	public void removeDropGroups()
	{
		_dropGroups = null;
	}
	
	public void removeDrops()
	{
		_dropListDeath = null;
		_dropListSpoil = null;
	}
	
	public void setDropGroups(List<DropGroupHolder> groups)
	{
		_dropGroups = groups;
	}
	
	public void addDrop(DropHolder dropHolder)
	{
		if (_dropListDeath == null)
		{
			_dropListDeath = new ArrayList<>(1);
		}
		_dropListDeath.add(dropHolder);
	}
	
	public void addSpoil(DropHolder dropHolder)
	{
		if (_dropListSpoil == null)
		{
			_dropListSpoil = new ArrayList<>(1);
		}
		_dropListSpoil.add(dropHolder);
	}
	
	public List<DropGroupHolder> getDropGroups()
	{
		return _dropGroups;
	}
	
	public List<DropHolder> getDropList()
	{
		return _dropListDeath;
	}
	
	public List<DropHolder> getSpoilList()
	{
		return _dropListSpoil;
	}
	
	public List<ItemHolder> calculateDrops(DropType dropType, Creature victim, Creature killer)
	{
		if (dropType == DropType.DROP)
		{
			// calculate group drops
			List<ItemHolder> groupDrops = null;
			if (_dropGroups != null)
			{
				groupDrops = calculateGroupDrops(victim, killer);
			}
			
			// calculate ungrouped drops
			List<ItemHolder> ungroupedDrops = null;
			if (_dropListDeath != null)
			{
				ungroupedDrops = calculateUngroupedDrops(dropType, victim, killer);
			}
			
			// return results
			if ((groupDrops != null) && (ungroupedDrops != null))
			{
				groupDrops.addAll(ungroupedDrops);
				ungroupedDrops.clear();
				return groupDrops;
			}
			if (groupDrops != null)
			{
				return groupDrops;
			}
			if (ungroupedDrops != null)
			{
				return ungroupedDrops;
			}
		}
		else if ((dropType == DropType.SPOIL) && (_dropListSpoil != null))
		{
			return calculateUngroupedDrops(dropType, victim, killer);
		}
		
		// no drops
		return null;
	}
	
	private List<ItemHolder> calculateGroupDrops(Creature victim, Creature killer)
	{
		// level difference calculations
		final int levelDifference = victim.getLevel() - killer.getLevel();
		final double levelGapChanceToDropAdena = Util.map(levelDifference, -Config.DROP_ADENA_MAX_LEVEL_DIFFERENCE, -Config.DROP_ADENA_MIN_LEVEL_DIFFERENCE, Config.DROP_ADENA_MIN_LEVEL_GAP_CHANCE, 100d);
		final double levelGapChanceToDrop = Util.map(levelDifference, -Config.DROP_ITEM_MAX_LEVEL_DIFFERENCE, -Config.DROP_ITEM_MIN_LEVEL_DIFFERENCE, Config.DROP_ITEM_MIN_LEVEL_GAP_CHANCE, 100d);
		
		int dropOccurrenceCounter = victim.isRaid() ? Config.DROP_MAX_OCCURRENCES_RAIDBOSS : Config.DROP_MAX_OCCURRENCES_NORMAL;
		List<ItemHolder> calculatedDrops = null;
		for (DropGroupHolder group : _dropGroups)
		{
			if (dropOccurrenceCounter <= 0)
			{
				break;
			}
			
			double groupRate = 1;
			final List<DropHolder> groupDrops = group.getDropList();
			for (DropHolder dropItem : groupDrops)
			{
				final int itemId = dropItem.getItemId();
				final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
				final boolean champion = victim.isChampion();
				
				// chance
				double rateChance = 1;
				final Float itemChance = Config.RATE_DROP_CHANCE_BY_ID.get(itemId);
				if (itemChance != null)
				{
					if (itemChance <= 0)
					{
						continue;
					}
					
					rateChance *= itemChance;
					if (champion && (itemId == Inventory.ADENA_ID))
					{
						rateChance *= Config.CHAMPION_ADENAS_REWARDS_CHANCE;
					}
				}
				else if (item.hasExImmediateEffect())
				{
					rateChance *= Config.RATE_HERB_DROP_CHANCE_MULTIPLIER;
				}
				else if (victim.isRaid())
				{
					rateChance *= Config.RATE_RAID_DROP_CHANCE_MULTIPLIER;
				}
				else
				{
					rateChance *= Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER * (champion ? Config.CHAMPION_REWARDS_CHANCE : 1);
				}
				
				// premium chance
				if (Config.PREMIUM_SYSTEM_ENABLED && (killer.getActingPlayer() != null) && killer.getActingPlayer().hasPremiumStatus())
				{
					if (Config.PREMIUM_RATE_DROP_CHANCE_BY_ID.get(itemId) != null)
					{
						rateChance *= Config.PREMIUM_RATE_DROP_CHANCE_BY_ID.get(itemId);
					}
					else if (item.hasExImmediateEffect())
					{
						// TODO: Premium herb chance? :)
					}
					else if (victim.isRaid())
					{
						// TODO: Premium raid chance? :)
					}
					else
					{
						rateChance *= Config.PREMIUM_RATE_DROP_CHANCE;
					}
				}
				
				// keep lowest to avoid chance by id configuration conflicts
				groupRate = Math.min(groupRate, rateChance);
			}
			
			if ((Rnd.nextDouble() * 100) < (group.getChance() * groupRate))
			{
				double totalChance = 0; // total group chance is 100
				final double dropChance = Rnd.nextDouble() * 100;
				GROUP_DROP: for (DropHolder dropItem : groupDrops)
				{
					if (dropOccurrenceCounter <= 0)
					{
						break;
					}
					
					// calculate if item will drop
					totalChance += dropItem.getChance();
					if (dropChance >= totalChance)
					{
						continue;
					}
					
					// check level gap that may prevent to drop item
					if ((Rnd.nextDouble() * 100) > (dropItem.getItemId() == Inventory.ADENA_ID ? levelGapChanceToDropAdena : levelGapChanceToDrop))
					{
						continue;
					}
					
					// skip zero chance drops
					final Float itemChance = Config.RATE_DROP_CHANCE_BY_ID.get(dropItem.getItemId());
					if ((itemChance != null) && (itemChance <= 0))
					{
						continue;
					}
					
					// create the drop
					final ItemHolder drop = calculateGroupDrop(dropItem, victim, killer);
					
					// create list
					if (calculatedDrops == null)
					{
						calculatedDrops = new ArrayList<>(dropOccurrenceCounter);
					}
					
					// finally
					if (itemChance != null)
					{
						if ((dropItem.getChance() * itemChance) < 100)
						{
							dropOccurrenceCounter--;
						}
					}
					else if (dropItem.getChance() < 100)
					{
						dropOccurrenceCounter--;
					}
					calculatedDrops.add(drop);
					
					// no more drops from this group
					break GROUP_DROP;
				}
			}
		}
		
		// champion extra drop
		if (victim.isChampion())
		{
			if ((victim.getLevel() < killer.getLevel()) && (Rnd.get(100) < Config.CHAMPION_REWARD_LOWER_LEVEL_ITEM_CHANCE))
			{
				return calculatedDrops;
			}
			if ((victim.getLevel() > killer.getLevel()) && (Rnd.get(100) < Config.CHAMPION_REWARD_HIGHER_LEVEL_ITEM_CHANCE))
			{
				return calculatedDrops;
			}
			
			// create list
			if (calculatedDrops == null)
			{
				calculatedDrops = new ArrayList<>();
			}
			
			if (!calculatedDrops.containsAll(Config.CHAMPION_REWARD_ITEMS))
			{
				calculatedDrops.addAll(Config.CHAMPION_REWARD_ITEMS);
			}
		}
		
		return calculatedDrops;
	}
	
	private List<ItemHolder> calculateUngroupedDrops(DropType dropType, Creature victim, Creature killer)
	{
		final List<DropHolder> dropList = dropType == DropType.SPOIL ? _dropListSpoil : _dropListDeath;
		
		// level difference calculations
		final int levelDifference = victim.getLevel() - killer.getLevel();
		final double levelGapChanceToDropAdena = Util.map(levelDifference, -Config.DROP_ADENA_MAX_LEVEL_DIFFERENCE, -Config.DROP_ADENA_MIN_LEVEL_DIFFERENCE, Config.DROP_ADENA_MIN_LEVEL_GAP_CHANCE, 100d);
		final double levelGapChanceToDrop = Util.map(levelDifference, -Config.DROP_ITEM_MAX_LEVEL_DIFFERENCE, -Config.DROP_ITEM_MIN_LEVEL_DIFFERENCE, Config.DROP_ITEM_MIN_LEVEL_GAP_CHANCE, 100d);
		
		int dropOccurrenceCounter = victim.isRaid() ? Config.DROP_MAX_OCCURRENCES_RAIDBOSS : Config.DROP_MAX_OCCURRENCES_NORMAL;
		List<ItemHolder> calculatedDrops = null;
		List<ItemHolder> randomDrops = null;
		ItemHolder cachedItem = null;
		if (dropOccurrenceCounter > 0)
		{
			for (DropHolder dropItem : dropList)
			{
				// check if maximum drop occurrences have been reached
				// items that have 100% drop chance without server rate multipliers drop normally
				if ((dropOccurrenceCounter == 0) && (dropItem.getChance() < 100) && (randomDrops != null) && (calculatedDrops != null))
				{
					// remove highest chance item (temporarily if no other item replaces it)
					cachedItem = randomDrops.remove(0);
					calculatedDrops.remove(cachedItem);
					dropOccurrenceCounter = 1;
				}
				
				// check level gap that may prevent to drop item
				if ((Rnd.nextDouble() * 100) > (dropItem.getItemId() == Inventory.ADENA_ID ? levelGapChanceToDropAdena : levelGapChanceToDrop))
				{
					continue;
				}
				
				// calculate chances
				final ItemHolder drop = calculateUngroupedDrop(dropItem, victim, killer);
				if (drop == null)
				{
					continue;
				}
				
				// create lists
				if (randomDrops == null)
				{
					randomDrops = new ArrayList<>(dropOccurrenceCounter);
				}
				if (calculatedDrops == null)
				{
					calculatedDrops = new ArrayList<>(dropOccurrenceCounter);
				}
				
				// finally
				final Float itemChance = Config.RATE_DROP_CHANCE_BY_ID.get(dropItem.getItemId());
				if (itemChance != null)
				{
					if ((dropItem.getChance() * itemChance) < 100)
					{
						dropOccurrenceCounter--;
						randomDrops.add(drop);
					}
				}
				else if (dropItem.getChance() < 100)
				{
					dropOccurrenceCounter--;
					randomDrops.add(drop);
				}
				calculatedDrops.add(drop);
			}
		}
		// add temporarily removed item when not replaced
		if ((dropOccurrenceCounter > 0) && (cachedItem != null) && (calculatedDrops != null))
		{
			calculatedDrops.add(cachedItem);
		}
		// clear random drops
		if (randomDrops != null)
		{
			randomDrops.clear();
			randomDrops = null;
		}
		
		// champion extra drop
		if (victim.isChampion())
		{
			if ((victim.getLevel() < killer.getLevel()) && (Rnd.get(100) < Config.CHAMPION_REWARD_LOWER_LEVEL_ITEM_CHANCE))
			{
				return calculatedDrops;
			}
			if ((victim.getLevel() > killer.getLevel()) && (Rnd.get(100) < Config.CHAMPION_REWARD_HIGHER_LEVEL_ITEM_CHANCE))
			{
				return calculatedDrops;
			}
			
			// create list
			if (calculatedDrops == null)
			{
				calculatedDrops = new ArrayList<>();
			}
			
			calculatedDrops.addAll(Config.CHAMPION_REWARD_ITEMS);
		}
		
		return calculatedDrops;
	}
	
	/**
	 * @param dropItem
	 * @param victim
	 * @param killer
	 * @return ItemHolder
	 */
	private ItemHolder calculateGroupDrop(DropHolder dropItem, Creature victim, Creature killer)
	{
		final int itemId = dropItem.getItemId();
		final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		final boolean champion = victim.isChampion();
		
		// calculate amount
		double rateAmount = 1;
		if (Config.RATE_DROP_AMOUNT_BY_ID.get(itemId) != null)
		{
			rateAmount *= Config.RATE_DROP_AMOUNT_BY_ID.get(itemId);
			if (champion && (itemId == Inventory.ADENA_ID))
			{
				rateAmount *= Config.CHAMPION_ADENAS_REWARDS_AMOUNT;
			}
		}
		else if (item.hasExImmediateEffect())
		{
			rateAmount *= Config.RATE_HERB_DROP_AMOUNT_MULTIPLIER;
		}
		else if (victim.isRaid())
		{
			rateAmount *= Config.RATE_RAID_DROP_AMOUNT_MULTIPLIER;
		}
		else
		{
			rateAmount *= Config.RATE_DEATH_DROP_AMOUNT_MULTIPLIER * (champion ? Config.CHAMPION_REWARDS_AMOUNT : 1);
		}
		
		// premium amount
		if (Config.PREMIUM_SYSTEM_ENABLED && (killer.getActingPlayer() != null) && killer.getActingPlayer().hasPremiumStatus())
		{
			if (Config.PREMIUM_RATE_DROP_AMOUNT_BY_ID.get(itemId) != null)
			{
				rateAmount *= Config.PREMIUM_RATE_DROP_AMOUNT_BY_ID.get(itemId);
			}
			else if (item.hasExImmediateEffect())
			{
				// TODO: Premium herb amount? :)
			}
			else if (victim.isRaid())
			{
				// TODO: Premium raid amount? :)
			}
			else
			{
				rateAmount *= Config.PREMIUM_RATE_DROP_AMOUNT;
			}
		}
		
		// finally
		return new ItemHolder(itemId, (long) (Rnd.get(dropItem.getMin(), dropItem.getMax()) * rateAmount));
	}
	
	/**
	 * @param dropItem
	 * @param victim
	 * @param killer
	 * @return ItemHolder
	 */
	private ItemHolder calculateUngroupedDrop(DropHolder dropItem, Creature victim, Creature killer)
	{
		switch (dropItem.getDropType())
		{
			case DROP:
			case LUCKY:
			{
				boolean usingPole = false;
				final int itemId = dropItem.getItemId();
				final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
				final boolean champion = victim.isChampion();
				if (killer.getActiveWeaponItem() != null)
				{
					usingPole = killer.getActiveWeaponItem().getItemType() == WeaponType.POLE;
				}
				
				// chance
				double rateChance = 1;
				if (item.isArrows() || item.isCommons() || item.isSpellbook())
				{
					rateChance *= 0;
				}
				
				if (item.isArmors() || item.isWeapons() || item.isAccessorys())
				{
					if (!isSpecialBoss())
					{
						rateChance *= (((BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_FINISHED_ITEM : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_FINISHED_ITEM_WEEKEND : Config.RATE_FINISHED_ITEM) * 0.2) * (champion ? Config.CHAMPION_REWARDS_CHANCE : 1));
					}
				}
				if (Config.RATE_DROP_CHANCE_BY_ID.get(itemId) != null)
				{
					rateChance *= Config.RATE_DROP_CHANCE_BY_ID.get(itemId);
					if (champion && (itemId == Inventory.ADENA_ID))
					{
						rateChance *= Config.CHAMPION_ADENAS_REWARDS_CHANCE;
					}
				}
				else if (item.hasExImmediateEffect())
				{
					rateChance *= Config.RATE_HERB_DROP_CHANCE_MULTIPLIER;
				}
				else if (victim.isRaid())
				{
					rateChance *= Config.RATE_RAID_DROP_CHANCE_MULTIPLIER;
				}
				else
				{
					rateChance *= (BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_DEATH_DROP_CHANCE_MULTIPLIER : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER_WEEKEND : Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER) * (champion ? Config.CHAMPION_REWARDS_CHANCE : 1);
					if (isKetraOrc() || isBarkaSilenos() || isWallOfArgos() || isLandOfWinds() || hotSpring() || isElvenFortress() || isMithrilMine() || isBeastFarm() || isAlligatorIsland())
					{
						rateChance *= Config.RATE_CUSTOM_DROP_ITEM_ADENA;
					}
				}
				
				// premium chance
				if (Config.PREMIUM_SYSTEM_ENABLED && (killer.getActingPlayer() != null) && killer.getActingPlayer().hasPremiumStatus())
				{
					if (Config.PREMIUM_RATE_DROP_CHANCE_BY_ID.get(itemId) != null)
					{
						rateChance *= Config.PREMIUM_RATE_DROP_CHANCE_BY_ID.get(itemId);
					}
					else
					{
						if (item.isArmors() || item.isWeapons() || item.isAccessorys())
						{
							if (!isSpecialBoss())
							{
								rateChance *= Config.PREMIUM_RATE_FINISHED_ITEM;
							}
						}
						else
						{
							rateChance *= Config.PREMIUM_RATE_DROP_CHANCE;
						}
					}
				}
				
				// bonus drop rate effect
				rateChance *= killer.getStat().getMul(Stat.BONUS_DROP_RATE, 1);
				rateChance *= Config.ENABLE_POLE_RATE ? (usingPole ? Config.POLE_ITEM_RATE : 1) : 1;
				
				// calculate if item will drop
				if ((Rnd.nextDouble() * 100) < (dropItem.getChance() * rateChance))
				{
					// amount is calculated after chance returned success
					double rateAmount = 1;
					if (item.isAdena())
					{
						rateAmount *= (BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_DROP_ADENA : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_DROP_ADENA_WEEKEND : Config.RATE_DROP_ADENA);
						rateAmount *= Config.ENABLE_POLE_RATE ? (usingPole ? Config.POLE_ITEM_RATE : 1) : 1;
						if (isKetraOrc() || isBarkaSilenos() || isWallOfArgos() || isLandOfWinds() || hotSpring() || isElvenFortress() || isMithrilMine() || isBeastFarm() || isAlligatorIsland())
						{
							rateChance *= Config.RATE_CUSTOM_DROP_ITEM_ADENA;
						}
					}
					if (Config.RATE_DROP_AMOUNT_BY_ID.get(itemId) != null)
					{
						rateAmount *= Config.RATE_DROP_AMOUNT_BY_ID.get(itemId);
						if (champion && (itemId == Inventory.ADENA_ID))
						{
							rateAmount *= Config.CHAMPION_ADENAS_REWARDS_AMOUNT;
						}
					}
					else if (item.hasExImmediateEffect())
					{
						rateAmount *= Config.RATE_HERB_DROP_AMOUNT_MULTIPLIER;
					}
					else if (victim.isRaid())
					{
						rateAmount *= Config.RATE_RAID_DROP_AMOUNT_MULTIPLIER;
					}
					else
					{
						rateAmount *= Config.RATE_DEATH_DROP_AMOUNT_MULTIPLIER * (champion ? Config.CHAMPION_REWARDS_AMOUNT : 1);
					}
					
					// premium amount
					if (Config.PREMIUM_SYSTEM_ENABLED && (killer.getActingPlayer() != null) && killer.getActingPlayer().hasPremiumStatus())
					{
						if (Config.PREMIUM_RATE_DROP_AMOUNT_BY_ID.get(itemId) != null)
						{
							rateAmount *= Config.PREMIUM_RATE_DROP_AMOUNT_BY_ID.get(itemId);
						}
						else if (item.hasExImmediateEffect())
						{
							// TODO: Premium herb amount? :)
						}
						else if (victim.isRaid())
						{
							// TODO: Premium raid amount? :)
						}
						else
						{
							rateAmount *= Config.PREMIUM_RATE_DROP_AMOUNT;
						}
					}
					
					// bonus drop amount effect
					rateAmount *= killer.getStat().getMul(Stat.BONUS_DROP_AMOUNT, 1);
					if (itemId == Inventory.ADENA_ID)
					{
						rateAmount *= killer.getStat().getMul(Stat.BONUS_DROP_ADENA, 1);
					}
					
					// finally
					return new ItemHolder(itemId, (long) (Rnd.get(dropItem.getMin(), dropItem.getMax()) * rateAmount));
				}
				break;
			}
			case SPOIL:
			{
				// chance
				double rateChance = (BorinetTask.SpecialEvent() ? Config.CUSTOM_EVENT_RATE_SPOIL_DROP_CHANCE_MULTIPLIER : (BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER_WEEKEND : Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER);
				// premium chance
				if (Config.PREMIUM_SYSTEM_ENABLED && (killer.getActingPlayer() != null) && killer.getActingPlayer().hasPremiumStatus())
				{
					rateChance *= Config.PREMIUM_RATE_SPOIL_CHANCE;
				}
				// bonus spoil rate effect
				rateChance *= killer.getStat().getMul(Stat.BONUS_SPOIL_RATE, 1);
				
				// calculate if item will be rewarded
				if ((Rnd.nextDouble() * 100) < (dropItem.getChance() * rateChance))
				{
					// amount is calculated after chance returned success
					double rateAmount = Config.RATE_SPOIL_DROP_AMOUNT_MULTIPLIER;
					// premium amount
					if (Config.PREMIUM_SYSTEM_ENABLED && (killer.getActingPlayer() != null) && killer.getActingPlayer().hasPremiumStatus())
					{
						rateAmount *= Config.PREMIUM_RATE_SPOIL_AMOUNT;
					}
					
					// finally
					return new ItemHolder(dropItem.getItemId(), (long) (Rnd.get(dropItem.getMin(), dropItem.getMax()) * rateAmount));
				}
				break;
			}
		}
		return null;
	}
	
	public float getCollisionRadiusGrown()
	{
		return _collisionRadiusGrown;
	}
	
	public float getCollisionHeightGrown()
	{
		return _collisionHeightGrown;
	}
	
	public static boolean isAssignableTo(Class<?> subValue, Class<?> clazz)
	{
		// If clazz represents an interface
		if (clazz.isInterface())
		{
			// check if obj implements the clazz interface
			for (Class<?> interface1 : subValue.getInterfaces())
			{
				if (clazz.getName().equals(interface1.getName()))
				{
					return true;
				}
			}
		}
		else
		{
			Class<?> sub = subValue;
			do
			{
				if (sub.getName().equals(clazz.getName()))
				{
					return true;
				}
				sub = sub.getSuperclass();
			}
			while (sub != null);
		}
		return false;
	}
	
	public boolean isSevenSignsParty()
	{
		if ((getId() >= 41001) && (getId() <= 41017)) // 세븐사인 파티
		{
			return true;
		}
		return false;
	}
	
	public boolean isSevenSignsSolo()
	{
		if ((getId() >= 42001) && (getId() <= 42017)) // 세븐사인 솔로
		{
			return true;
		}
		return false;
	}
	
	public boolean RandomBoss()
	{
		if ((getId() >= 41018) && (getId() <= 41037)) // 랜덤보스
		{
			return true;
		}
		return false;
	}
	
	public boolean isSpecialBoss()
	{
		switch (getId())
		{
			case 18020:
			case 18029:
			case 29001:
			case 29006:
			case 29014:
			case 29020:
			case 29022:
			case 29047:
			case 29068:
			case 29105:
			case 29106:
			case 29107:
			case 29108:
				return true;
		}
		return false;
	}
	
	public boolean EventMonsters()
	{
		switch (getId())
		{
			case 13183:
			case 13271:
			case 13272:
			case 13273:
			case 13274:
			case 13275:
			case 13276:
			case 13277:
			case 13278:
				return true;
		}
		return false;
	}
	
	public boolean hotSpring()
	{
		if ((getId() >= 21833) && (getId() <= 21842)) // 온천지대
		{
			return true;
		}
		return false;
	}
	
	// 엘프의 지하요새
	public boolean isElvenFortress()
	{
		if (((getId() >= 19306) && (getId() <= 19363)) || //
			((getId() >= 22545) && (getId() <= 22597)) || //
			((getId() >= 22868) && (getId() <= 22894)) || //
			((getId() >= 22900) && (getId() <= 22985)))
		{
			return true;
		}
		return false;
	}
	
	// 미스릴 광산
	public boolean isMithrilMine()
	{
		if ((getId() >= 22678) && (getId() <= 22690))
		{
			return true;
		}
		return false;
	}
	
	// 신들의 화로
	public boolean isForgeOfTheGods()
	{
		if (((getId() >= 21661) && (getId() <= 21676)) || (getId() == 21678) || //
			((getId() >= 21685) && (getId() <= 21686)) || //
			((getId() >= 21761) && (getId() <= 21762)))
		{
			return true;
		}
		return false;
	}
	
	public boolean isBeastFarm()
	{
		if ((getId() >= 18873) && (getId() <= 18899)) // 야수농장
		{
			return true;
		}
		return false;
	}
	
	public boolean isKetraOrc()
	{
		// 케트라 오크 전진기지
		if ((getId() >= 21843) && (getId() <= 21867))
		{
			return true;
		}
		return false;
	}
	
	public boolean isBarkaSilenos()
	{
		// 바르카 실레노스 주둔지
		if ((getId() >= 21869) && (getId() <= 21892))
		{
			return true;
		}
		return false;
	}
	
	public boolean isWallOfArgos()
	{
		// 아르고스의 벽
		if (((getId() >= 21764) && (getId() <= 21766)) || //
			(getId() == 21774) || (getId() == 21775) || (getId() == 21746) || //
			((getId() >= 21813) && (getId() <= 21831)))
		{
			return true;
		}
		return false;
	}
	
	public boolean isLandOfWinds()
	{
		// 바람의 대지
		if ((getId() >= 21770) && (getId() <= 21793))
		{
			return true;
		}
		return false;
	}
	
	public boolean isForgotten()
	{
		if ((getId() >= 21733) && (getId() <= 21760))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isMonastery()
	{
		if (((getId() >= 18909) && (getId() <= 18915)) || ((getId() >= 22789) && (getId() <= 22800)))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isGolem()
	{
		if (isType("Monster") && (getName().contains("Golem") || getName().contains("골렘")))
		{
			return true;
		}
		if ((getId() == 18369) || (getId() == 18370) //
			|| (getId() == 20016) || (getId() == 20083) || (getId() == 20149) //
			|| (getId() == 20333) || (getId() == 20346) || (getId() == 20511) //
			|| (getId() == 20521) || (getId() == 20526) || (getId() == 20559) //
			|| (getId() == 20565) || (getId() == 20566) || (getId() == 21103) //
			|| (getId() == 21128) || (getId() == 21131) || (getId() == 21137) //
			|| (getId() == 21656) || (getId() == 21670) || (getId() == 21671) //
			|| (getId() == 21768) || (getId() == 21817) || (getId() == 25088) //
			|| (getId() == 25431) || (getId() == 27271))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isAlligatorIsland()
	{
		if ((getId() == 20135) || ((getId() >= 20804) && (getId() <= 20808)) || (getId() == 20991))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if obj can be assigned to the Class represented by clazz.<br>
	 * This is true if, and only if, obj is the same class represented by clazz, or a subclass of it or obj implements the interface represented by clazz.
	 * @param obj
	 * @param clazz
	 * @return {@code true} if the object can be assigned to the class, {@code false} otherwise
	 */
	public static boolean isAssignableTo(Object obj, Class<?> clazz)
	{
		return isAssignableTo(obj.getClass(), clazz);
	}
}
