package org.l2jmobius.gameserver.taskmanager.auto;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Guard;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.holders.AttachSkillHolder;
import org.l2jmobius.gameserver.model.skill.AbnormalType;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.targets.AffectScope;
import org.l2jmobius.gameserver.model.skill.targets.TargetType;
import org.l2jmobius.gameserver.model.zone.ZoneId;

/**
 * @author 보리넷 가츠
 */
public class AutoSkillTaskManager implements Runnable
{
	private static final Set<Player> USE_SKILLS = ConcurrentHashMap.newKeySet();
	
	private static final int REUSE_MARGIN_TIME = 2;
	private static boolean _working = false;
	int count = 0;
	
	protected AutoSkillTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 500, 500);
	}
	
	@Override
	public void run()
	{
		if (_working && (count < 20))
		{
			count++;
			return;
		}
		count = 0;
		_working = true;
		
		PLAY: for (Player player : USE_SKILLS)
		{
			if ((player == null) || (player.isOnlineInt() != 1) || (player.isInOlympiadMode()))
			{
				stopAutoSkill(player);
				continue PLAY;
			}
			
			if (!player.getAutoPlay())
			{
				stopAutoSkill(player);
				continue PLAY;
			}
			
			if (player.getVariables().getBoolean("자동사냥_이동중", false) || player.getVariables().getBoolean("자동사냥_제자리이동중", false) || player.isCastingNow() || player.isTeleporting())
			{
				continue PLAY;
			}
			
			final boolean isInPeaceZone = player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.SAYUNE);
			if (isInPeaceZone)
			{
				continue PLAY;
			}
			
			// Skip thinking.
			final WorldObject target = player.getTarget();
			BUFFS: for (Integer skillId : player.getAutoBuffs())
			{
				Skill skill = player.getKnownSkill(skillId.intValue());
				if (skill == null)
				{
					player.getAutoBuffs().remove(skillId);
					continue BUFFS;
				}
				
				if (canCastBuff(player, target, skill))
				{
					ATTACH_SEARCH: for (AttachSkillHolder holder : skill.getAttachSkills())
					{
						if (player.isAffectedBySkill(holder.getRequiredSkillId()))
						{
							skill = holder.getSkill();
							break ATTACH_SEARCH;
						}
					}
					
					// Playable target cast.
					if ((target != null) && target.isPlayable() && (target.getActingPlayer().getPvpFlag() == 0) && (target.getActingPlayer().getReputation() >= 0))
					{
						player.doCast(skill);
					}
					else // Target self, cast and re-target.
					{
						final WorldObject savedTarget = target;
						player.setTarget(player);
						player.doCast(skill);
						player.setTarget(savedTarget);
					}
				}
			}
			
			if (Rnd.chance(isWizard(player) ? 100 : 20))
			{
				SKILLS:
				{
					// Acquire next skill.
					final Integer skillId = player.getNextSkillId();
					final Skill skill = player.getKnownSkill(skillId.intValue());
					if (skill == null)
					{
						player.getAutoSkills().remove(skillId);
						player.resetSkillOrder();
						break SKILLS;
					}
					
					// Check bad skill target.
					if ((target == player) || (target == null) || !target.isAttackable() || ((Creature) target).isDead() || (target instanceof Guard))
					{
						break SKILLS;
					}
					
					if ((skill.getId() == 254) && ((Monster) target).isSpoiled())
					{
						break SKILLS;
					}
					
					if ((player.getCurrentMp() < skill.getMpConsume()) || !canUseMagic(player, target, skill) || player.useMagic(skill, null, true, false))
					{
						player.incrementSkillOrder();
					}
				}
			}
		}
		_working = false;
	}
	
	public void doAutoSkill(Player player)
	{
		if (!USE_SKILLS.contains(player))
		{
			player.resetSkillOrder();
			player.onActionRequest();
			USE_SKILLS.add(player);
		}
	}
	
	public void stopAutoSkill(Player player)
	{
		player.resetSkillOrder();
		USE_SKILLS.remove(player);
	}
	
	private boolean isWizard(Player player)
	{
		final ClassId classId = player.getClassId();
		if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()))
		{
			return true;
		}
		return false;
	}
	
	private boolean canCastBuff(Player player, WorldObject target, Skill skill)
	{
		// Summon check.
		if (skill.getAffectScope() == AffectScope.SUMMON_EXCEPT_MASTER)
		{
			if (!player.hasServitors())
			{
				return false;
			}
			int occurrences = 0;
			for (Summon servitor : player.getServitors().values())
			{
				if (servitor.isAffectedBySkill(skill.getId()))
				{
					occurrences++;
				}
			}
			if (occurrences == player.getServitors().size())
			{
				return false;
			}
		}
		
		if ((target != null) && target.isCreature() && ((Creature) target).isAlikeDead() && (skill.getTargetType() != TargetType.SELF) && (skill.getTargetType() != TargetType.NPC_BODY) && (skill.getTargetType() != TargetType.PC_BODY))
		{
			return false;
		}
		
		final Playable playableTarget = (target == null) || !target.isPlayable() || (skill.getTargetType() == TargetType.SELF) ? player : (Playable) target;
		if (!canUseMagic(player, playableTarget, skill))
		{
			return false;
		}
		
		final BuffInfo buffInfo = playableTarget.getEffectList().getBuffInfoBySkillId(skill.getId());
		final BuffInfo abnormalBuffInfo = playableTarget.getEffectList().getFirstBuffInfoByAbnormalType(skill.getAbnormalType());
		if (abnormalBuffInfo != null)
		{
			if (buffInfo != null)
			{
				return (abnormalBuffInfo.getSkill().getId() == buffInfo.getSkill().getId()) && ((buffInfo.getTime() <= REUSE_MARGIN_TIME) || (buffInfo.getSkill().getLevel() < skill.getLevel()));
			}
			return (abnormalBuffInfo.getSkill().getAbnormalLevel() < skill.getAbnormalLevel()) || abnormalBuffInfo.isAbnormalType(AbnormalType.NONE);
		}
		return buffInfo == null;
	}
	
	private boolean canUseMagic(Player player, WorldObject target, Skill skill)
	{
		if ((skill.getItemConsumeCount() > 0) && (player.getInventory().getInventoryItemCount(skill.getItemConsumeId(), -1) < skill.getItemConsumeCount()))
		{
			return false;
		}
		
		for (AttachSkillHolder holder : skill.getAttachSkills())
		{
			if (player.isAffectedBySkill(holder.getRequiredSkillId()) //
				&& (player.hasSkillReuse(holder.getSkill().getReuseHashCode()) || player.isAffectedBySkill(holder)))
			{
				return false;
			}
		}
		
		return !player.isSkillDisabled(skill) && skill.checkCondition(player, target, false);
	}
	
	public static AutoSkillTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSkillTaskManager INSTANCE = new AutoSkillTaskManager();
	}
}
