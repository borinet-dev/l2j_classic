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
package handlers.effecthandlers;

import java.util.logging.Level;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.handler.TargetHandler;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureDamageReceived;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.skill.targets.TargetType;

/**
 * Trigger Skill By Damage effect implementation.
 * @author UnAfraid
 */
public class TriggerSkillByDamage extends AbstractEffect
{
	private final int _minAttackerLevel;
	private final int _maxAttackerLevel;
	private final int _minDamage;
	private final int _chance;
	private final int _hpPercent;
	private final SkillHolder _skill;
	private final TargetType _targetType;
	private final InstanceType _attackerType;
	private final int _skillLevelScaleTo;
	
	public TriggerSkillByDamage(StatSet params)
	{
		_minAttackerLevel = params.getInt("minAttackerLevel", 1);
		_maxAttackerLevel = params.getInt("maxAttackerLevel", Integer.MAX_VALUE);
		_minDamage = params.getInt("minDamage", 1);
		_chance = params.getInt("chance", 100);
		_hpPercent = params.getInt("hpPercent", 100);
		_skill = new SkillHolder(params.getInt("skillId"), params.getInt("skillLevel", 1));
		_targetType = params.getEnum("targetType", TargetType.class, TargetType.SELF);
		_attackerType = params.getEnum("attackerType", InstanceType.class, InstanceType.Creature);
		_skillLevelScaleTo = params.getInt("skillLevelScaleTo", 0);
	}
	
	private void onDamageReceivedEvent(OnCreatureDamageReceived event)
	{
		if (event.isDamageOverTime() || (_chance == 0) || (_skill.getSkillLevel() == 0))
		{
			return;
		}
		
		if (event.getAttacker() == event.getTarget())
		{
			return;
		}
		
		if ((event.getAttacker().getLevel() < _minAttackerLevel) || (event.getAttacker().getLevel() > _maxAttackerLevel))
		{
			return;
		}
		
		if (event.getDamage() < _minDamage)
		{
			return;
		}
		
		if ((_chance < 100) && (Rnd.get(100) > _chance))
		{
			return;
		}
		
		if ((_hpPercent < 100) && (event.getAttacker().getCurrentHpPercent() > _hpPercent))
		{
			return;
		}
		
		if (event.getAttacker().isPet() || event.getAttacker().isSummon())
		{
			return;
		}
		
		if (!event.getAttacker().getInstanceType().isType(_attackerType))
		{
			return;
		}
		
		WorldObject target = null;
		try
		{
			target = TargetHandler.getInstance().getHandler(_targetType).getTarget(event.getTarget(), event.getAttacker(), _skill.getSkill(), false, false, false);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception in ITargetTypeHandler.getTarget(): " + e.getMessage(), e);
		}
		if ((target == null) || !target.isCreature())
		{
			return;
		}
		
		final Skill s = event.getAttacker().getKnownSkill(_skill.getSkillId());
		if (s != null)
		{
			final long remainingTime = event.getAttacker().getSkillRemainingReuseTime(s.getReuseHashCode());
			if (remainingTime > 0)
			{
				return;
			}
		}
		
		final Skill triggerSkill;
		if (_skillLevelScaleTo <= 0)
		{
			triggerSkill = _skill.getSkill();
		}
		else
		{
			final BuffInfo buffInfo = ((Creature) target).getEffectList().getBuffInfoBySkillId(_skill.getSkillId());
			if (buffInfo != null)
			{
				triggerSkill = SkillData.getInstance().getSkill(_skill.getSkillId(), Math.min(_skillLevelScaleTo, buffInfo.getSkill().getLevel() + 1));
			}
			else
			{
				triggerSkill = _skill.getSkill();
			}
		}
		
		if (target.isPlayer())
		{
			switch (triggerSkill.getAbnormalType())
			{
				case PARALYZE:
				{
					int per = ((Creature) target).getParalyzeResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case TURN_STONE:
				{
					int per = ((Creature) target).getTurnStoneResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case DERANGEMENT:
				case TURN_FLEE:
				{
					int per = ((Creature) target).getDerangementResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case STUN:
				{
					int per = ((Creature) target).getStunResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case ROOT_PHYSICALLY:
				case ROOT_MAGICALLY:
				{
					int per = ((Creature) target).getHoldResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case SLEEP:
				{
					int per = ((Creature) target).getSleepResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case BLEEDING:
				{
					int per = ((Creature) target).getBleedResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case POISON:
				{
					int per = ((Creature) target).getPoisonResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
				case SILENCE:
				case SILENCE_ALL:
				case SILENCE_PHYSICAL:
				{
					int per = ((Creature) target).getSilenceResist();
					if (per >= 100)
					{
						return;
					}
					else if (Rnd.chance(per))
					{
						return;
					}
					break;
				}
			}
		}
		
		SkillCaster.triggerCast(event.getAttacker(), (Creature) target, triggerSkill);
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.removeListenerIf(EventType.ON_CREATURE_DAMAGE_RECEIVED, listener -> listener.getOwner() == this);
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.addListener(new ConsumerEventListener(effected, EventType.ON_CREATURE_DAMAGE_RECEIVED, (OnCreatureDamageReceived event) -> onDamageReceivedEvent(event), this));
	}
}
