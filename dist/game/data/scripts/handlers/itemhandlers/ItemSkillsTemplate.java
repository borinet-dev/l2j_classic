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
package handlers.itemhandlers;

import java.util.List;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ItemSkillType;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.holders.ItemSkillHolder;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.skill.targets.TargetType;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Template for item skills handler.
 * @author Zoey76
 */
public class ItemSkillsTemplate implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer() && !playable.isPet())
		{
			return false;
		}
		
		// Pets can use items only when they are tradable.
		if (playable.isPet() && !item.isTradeable())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final List<ItemSkillHolder> skills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
		if (skills == null)
		{
			LOGGER.info("Item " + item + " does not have registered any skill for handler.");
			return false;
		}
		
		boolean hasConsumeSkill = false;
		boolean successfulUse = false;
		for (SkillHolder skillInfo : skills)
		{
			if (skillInfo == null)
			{
				continue;
			}
			
			final Skill itemSkill = skillInfo.getSkill();
			if (itemSkill != null)
			{
				if (itemSkill.hasEffectType(EffectType.EXTRACT_ITEM) && (playable.getActingPlayer() != null) && !playable.getActingPlayer().isInventoryUnder80(false))
				{
					playable.getActingPlayer().sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
					return false;
				}
				
				if (itemSkill.getItemConsumeId() > 0)
				{
					hasConsumeSkill = true;
				}
				
				final Player player = playable.getActingPlayer();
				// Verify that skill is not under reuse.
				final int reuseDelay = itemSkill.getReuseDelay();
				if (reuseDelay > 0)
				{
					final long reuse = player.getVariables().getLong(itemSkill.getId() + "_재사용시간", 0);
					if (reuse > System.currentTimeMillis())
					{
						checkReuse(player, itemSkill);
					}
				}
				
				if (!itemSkill.hasEffectType(EffectType.SUMMON_PET) && !itemSkill.checkCondition(playable, playable.getTarget(), true))
				{
					continue;
				}
				
				if (playable.isSkillDisabled(itemSkill))
				{
					continue;
				}
				
				if (!item.isPotion() && !item.isElixir() && !item.isScroll() && playable.isCastingNow())
				{
					continue;
				}
				
				// Send message to the master.
				if (playable.isPet())
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_PET_USES_S1);
					sm.addSkillName(itemSkill);
					playable.sendPacket(sm);
				}
				
				if (playable.isPlayer() && itemSkill.hasEffectType(EffectType.SUMMON_PET))
				{
					playable.doCast(itemSkill);
					successfulUse = true;
				}
				else if (itemSkill.isWithoutAction() || item.getTemplate().hasImmediateEffect() || item.getTemplate().hasExImmediateEffect())
				{
					SkillCaster.triggerCast(playable, itemSkill.getTargetType() == TargetType.OTHERS ? playable.getTarget() : null, itemSkill, item, false);
					successfulUse = true;
				}
				else
				{
					playable.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (playable.useMagic(itemSkill, item, forceUse, false))
					{
						successfulUse = true;
					}
					else
					{
						continue;
					}
				}
				
				if (successfulUse)
				{
					if (item.getReuseDelay() > 0)
					{
						final long reuse = player.getVariables().getLong(item.getId() + "_재사용시간", 0);
						if (reuse <= System.currentTimeMillis())
						{
							player.getVariables().set(item.getId() + "_재사용시간", (System.currentTimeMillis() + item.getReuseDelay()));
						}
					}
					
					if (itemSkill.getReuseDelay() > 0)
					{
						final long reuse = player.getVariables().getLong(itemSkill.getId() + "_재사용시간", 0);
						if (reuse <= System.currentTimeMillis())
						{
							player.getVariables().set(itemSkill.getId() + "_재사용시간", (System.currentTimeMillis() + itemSkill.getReuseDelay()));
						}
						// player.addTimeStamp(itemSkill, itemSkill.getReuseDelay());
					}
				}
			}
		}
		
		if (successfulUse && checkConsume(item, hasConsumeSkill) && !playable.destroyItem("사용", item.getObjectId(), 1, playable, false))
		{
			playable.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			return false;
		}
		
		return successfulUse;
	}
	
	/**
	 * @param item the item being used
	 * @param hasConsumeSkill
	 * @return {@code true} check if item use consume item, {@code false} otherwise
	 */
	private boolean checkConsume(Item item, boolean hasConsumeSkill)
	{
		switch (item.getTemplate().getDefaultAction())
		{
			case CAPSULE:
			case SKILL_REDUCE:
			{
				if (!hasConsumeSkill && item.getTemplate().hasImmediateEffect())
				{
					return true;
				}
				break;
			}
			case SKILL_REDUCE_ON_SKILL_SUCCESS:
			{
				return false;
			}
		}
		return hasConsumeSkill;
	}
	
	/**
	 * @param player the character using the item or skill
	 * @param skill the skill being used, can be null
	 * @return {@code true} if the the item or skill to check is available, {@code false} otherwise
	 */
	private boolean checkReuse(Player player, Skill skill)
	{
		final long remainingTime = player.getVariables().getLong(skill.getId() + "_재사용시간", 0);
		
		if (remainingTime > System.currentTimeMillis())
		{
			final long remaining = ((remainingTime - System.currentTimeMillis()) / 1000);
			final int hours = (int) (remaining / 3600);
			final int minutes = (int) ((remaining % 3600) / 60);
			final int seconds = (int) (remaining % 60);
			if (hours > 0)
			{
				player.sendMessage(skill.getName() + "의 재사용 시간이 " + hours + "시간 " + minutes + "분 " + seconds + "초 남았습니다.");
			}
			else if (minutes > 0)
			{
				player.sendMessage(skill.getName() + "의 재사용 시간이 " + minutes + "분 " + seconds + "초 남았습니다.");
			}
			else
			{
				player.sendMessage(skill.getName() + "의 재사용 시간이 " + seconds + "초 남았습니다.");
			}
			return false;
		}
		return true;
	}
}
