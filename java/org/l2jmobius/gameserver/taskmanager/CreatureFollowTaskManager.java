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
package org.l2jmobius.gameserver.taskmanager;

import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CreatureAI;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;

/**
 * @author Mobius, 보리넷 가츠
 */
public class CreatureFollowTaskManager
{
	private static final Map<Creature, Integer> NORMAL_FOLLOW_CREATURES = new ConcurrentHashMap<>();
	private static final Map<Creature, Integer> ATTACK_FOLLOW_CREATURES = new ConcurrentHashMap<>();
	private static final Map<Creature, Integer> AUTO_FOLLOW_CREATURES = new ConcurrentHashMap<>();
	private static boolean _workingNormal = false;
	private static boolean _workingAttack = false;
	private static boolean _workingAuto = false;
	
	protected CreatureFollowTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(new CreatureFollowNormalTask(), 1000, 1000);
		ThreadPool.scheduleAtFixedRate(new CreatureAutoFollowTask(), 500, 500);
		ThreadPool.scheduleAtFixedRate(new CreatureFollowAttackTask(), 500, 500);
	}
	
	protected class CreatureFollowNormalTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingNormal)
			{
				return;
			}
			_workingNormal = true;
			
			for (Entry<Creature, Integer> entry : NORMAL_FOLLOW_CREATURES.entrySet())
			{
				follow(entry.getKey(), entry.getValue().intValue());
			}
			
			_workingNormal = false;
		}
	}
	
	protected class CreatureFollowAttackTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingAttack)
			{
				return;
			}
			_workingAttack = true;
			
			for (Entry<Creature, Integer> entry : ATTACK_FOLLOW_CREATURES.entrySet())
			{
				follow(entry.getKey(), entry.getValue().intValue());
			}
			
			_workingAttack = false;
		}
	}
	
	protected class CreatureAutoFollowTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_workingAuto)
			{
				return;
			}
			_workingAuto = true;
			
			for (Entry<Creature, Integer> entry : AUTO_FOLLOW_CREATURES.entrySet())
			{
				follow(entry.getKey(), entry.getValue().intValue());
			}
			
			_workingAuto = false;
		}
	}
	
	private void follow(Creature creature, int range)
	{
		try
		{
			if (creature.hasAI())
			{
				final CreatureAI ai = creature.getAI();
				if (ai != null)
				{
					final WorldObject followTarget = ai.getTarget();
					if (followTarget == null)
					{
						if (creature.isSummon())
						{
							((Summon) creature).setFollowStatus(false);
						}
						ai.setIntention(AI_INTENTION_IDLE);
						return;
					}
					
					final int followRange = range == -1 ? Rnd.get(50, 100) : range;
					if (!creature.isInsideRadius3D(followTarget, followRange))
					{
						if (!creature.isInsideRadius3D(followTarget, 3000))
						{
							// If the target is too far (maybe also teleported).
							if (creature.isSummon())
							{
								((Summon) creature).setFollowStatus(false);
							}
							ai.setIntention(AI_INTENTION_IDLE);
							return;
						}
						// 리더와 일정 거리를 두고 이동
						final int leaderX = followTarget.getX();
						final int leaderY = followTarget.getY();
						final int leaderZ = followTarget.getZ();
						
						double dx = leaderX - creature.getX();
						double dy = leaderY - creature.getY();
						double dz = leaderZ - creature.getZ();
						
						double distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
						
						// 목표 지점은 리더와의 거리를 followRange로 유지하는 좌표입니다.
						if (distance > followRange)
						{
							double ratio = (distance - followRange) / distance;
							int targetX = creature.getX() + (int) (dx * ratio);
							int targetY = creature.getY() + (int) (dy * ratio);
							int targetZ = creature.getZ() + (int) (dz * ratio);
							
							ai.moveTo(targetX, targetY, targetZ);
						}
					}
				}
				else
				{
					remove(creature);
				}
			}
			else
			{
				remove(creature);
			}
		}
		catch (Exception e)
		{
			// Ignore.
		}
	}
	
	public boolean isFollowing(Creature creature)
	{
		return NORMAL_FOLLOW_CREATURES.containsKey(creature) || ATTACK_FOLLOW_CREATURES.containsKey(creature);
	}
	
	public void addNormalFollow(Creature creature, int range)
	{
		NORMAL_FOLLOW_CREATURES.putIfAbsent(creature, range);
		follow(creature, range);
	}
	
	public void addAttackFollow(Creature creature, int range)
	{
		ATTACK_FOLLOW_CREATURES.putIfAbsent(creature, range);
		follow(creature, range);
	}
	
	public void addAutoFollow(Creature creature, int range)
	{
		AUTO_FOLLOW_CREATURES.putIfAbsent(creature, range);
		follow(creature, range);
	}
	
	public void remove(Creature creature)
	{
		NORMAL_FOLLOW_CREATURES.remove(creature);
		ATTACK_FOLLOW_CREATURES.remove(creature);
		AUTO_FOLLOW_CREATURES.remove(creature);
	}
	
	public static CreatureFollowTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CreatureFollowTaskManager INSTANCE = new CreatureFollowTaskManager();
	}
}
