package org.l2jmobius.gameserver.taskmanager.auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author 보리넷 가츠
 */
public class AutoAttackPetTaskManager implements Runnable
{
	private static final Set<Player> USE_SKILLS = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	private final List<Skill> _currentBuffs = new ArrayList<>();
	private static final int BUFF_CONTROL = 5771;
	int count = 0;
	
	protected AutoAttackPetTaskManager()
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
		
		ATTACK: for (Player player : USE_SKILLS)
		{
			if ((player == null) || player.isAlikeDead() || (player.isOnlineInt() != 1) || (player.isInOlympiadMode()))
			{
				continue ATTACK;
			}
			
			Pet pet = player.getPet();
			if (pet == null)
			{
				stopAutoAttack(player);
				continue ATTACK;
			}
			final Player owner = pet.getOwner();
			
			if ((owner == null) || owner.isDead() || owner.isInvul() || owner.isInvisible() || ((owner.getLevel() + 10) < pet.getLevel()))
			{
				continue ATTACK;
			}
			
			if (pet.isDead() || pet.isCastingNow() || pet.isBetrayed() || pet.isMuted() || (pet.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST))
			{
				continue ATTACK;
			}
			
			if (pet.getEffectList().getBuffInfoBySkillId(BUFF_CONTROL) != null)
			{
				Skill skill = null;
				for (Skill buff : ROSE_PET_BUFFS)
				{
					skill = buff;
					if (pet.isSkillDisabled(skill))
					{
						continue ATTACK;
					}
					
					if (pet.getCurrentMp() < skill.getMpConsume())
					{
						continue ATTACK;
					}
					
					final BuffInfo buffInfo = owner.getEffectList().getFirstBuffInfoByAbnormalType(skill.getAbnormalType());
					if ((buffInfo != null) && (skill.getAbnormalLevel() <= buffInfo.getSkill().getAbnormalLevel()))
					{
						continue ATTACK;
					}
					_currentBuffs.add(skill);
				}
				
				if (!_currentBuffs.isEmpty())
				{
					skill = _currentBuffs.get(Rnd.get(_currentBuffs.size()));
					castSkill(skill, pet);
					_currentBuffs.clear();
				}
			}
			
			Creature target = (Creature) owner.getTarget();
			if ((target != null) && (pet.getEffectList().getBuffInfoBySkillId(6054) != null))
			{
				if ((target.isMonster() || target.isRaid()))
				{
					pet.doAttack(target);
				}
			}
		}
		_working = false;
	}
	
	public void castSkill(Skill skill, Pet pet)
	{
		final boolean previousFollowStatus = pet.getFollowStatus();
		
		if (!previousFollowStatus && !pet.isInsideRadius3D(pet.getOwner(), skill.getCastRange()))
		{
			return;
		}
		
		pet.setTarget(pet.getOwner());
		pet.useMagic(skill, null, false, false);
		
		final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_PET_USES_S1);
		msg.addSkillName(skill);
		pet.sendPacket(msg);
		
		if (previousFollowStatus != pet.getFollowStatus())
		{
			pet.setFollowStatus(previousFollowStatus);
		}
	}
	
	private static final int Pet_Vampiric_Rage = 5187; // 1-4
	private static final Skill[] ROSE_PET_BUFFS =
	{
		SkillData.getInstance().getSkill(Pet_Vampiric_Rage, 4)
	};
	
	public void doAutoAttack(Player player)
	{
		if (!USE_SKILLS.contains(player))
		{
			player.onActionRequest();
			USE_SKILLS.add(player);
		}
	}
	
	public void stopAutoAttack(Player player)
	{
		USE_SKILLS.remove(player);
	}
	
	public static AutoAttackPetTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoAttackPetTaskManager INSTANCE = new AutoAttackPetTaskManager();
	}
}
