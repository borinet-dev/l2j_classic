package org.l2jmobius.gameserver.taskmanager.auto;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

/**
 * @author 보리넷 가츠
 */
public class AutoPlayTaskManager implements Runnable
{
	private static final Set<Player> PLAYERS = ConcurrentHashMap.newKeySet();
	
	private static boolean _working = false;
	int count = 0;
	
	protected AutoPlayTaskManager()
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
		
		PLAY: for (Player player : PLAYERS)
		{
			if ((player == null) || !player.isOnline() || (player.isOnlineInt() != 1) || player.isInOfflineMode() || (player.isInOlympiadMode()))
			{
				stopAutoPlay(player);
				continue PLAY;
			}
			
			if (player.isAlikeDead() || player.isAttackingNow() || player.isCastingNow() || (player.getQueuedSkill() != null))
			{
				continue PLAY;
			}
			
			final boolean isInPeaceZone = player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.SAYUNE);
			if (!player.getAutoPlay())
			{
				if (isInPeaceZone)
				{
					player.sendMessage("피스존에 입장하여 자동사냥을 종료합니다.");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "자동 사냥", "피스존에 입장하여 자동사냥을 종료합니다."));
					stopAutoPlay(player);
				}
				else
				{
					player.setAutoPlay(true);
					AutoSkillTaskManager.getInstance().doAutoSkill(player);
				}
				continue PLAY;
			}
			
			if (player.getTarget() == player)
			{
				player.setTarget(null);
			}
			
			final ClassId classId = player.getClassId();
			boolean archer = CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId.getId());
			if (player.getVariables().getBoolean("자동사냥_제자리말뚝딜", false) && archer)
			{
				if (player.getActiveWeaponItem().getItemType() != WeaponType.BOW)
				{
					player.sendMessage("제자리 말뚝 딜 모드에 적합한 무기가 아닙니다.");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "자동 사냥", "제자리 말뚝 딜 모드에 적합한 무기가 아닙니다."));
					stopAutoPlay(player);
					continue PLAY;
				}
			}
			
			if (player.getVariables().getBoolean("자동사냥_제자리이동중", false))
			{
				if (player.isMoving())
				{
					continue PLAY;
				}
				
				if (!player.isMoving())
				{
					player.getVariables().remove("자동사냥_제자리이동중");
				}
			}
			
			if (player.getVariables().getBoolean("자동사냥_이동중", false))
			{
				if (!player.isMoving())
				{
					player.getVariables().remove("자동사냥_이동중");
				}
			}
			
			// Skip thinking.
			final WorldObject target = player.getTarget();
			if ((target != null) && (target instanceof Monster))
			{
				final Monster monster = (Monster) target;
				if (monster.isAlikeDead())
				{
					if (CategoryData.getInstance().isInCategory(CategoryType.DWARF_BOUNTY_CLASS, classId.getId()) && monster.isSweepActive())
					{
						final Integer skillId = player.getSweeperSkillId();
						final Skill skill = player.getKnownSkill(skillId.intValue());
						
						player.useMagic(skill, null, true, false);
					}
					else
					{
						player.setTarget(null);
					}
				}
				else if (monster.getTarget() == player)
				{
					if (isWizard(player))
					{
						continue PLAY;
					}
					
					// Check if actually attacking.
					if (player.hasAI() && player.getAI().isAutoAttacking() && !player.isAttackingNow() && !player.isCastingNow() && !player.isMoving())
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, monster);
					}
					continue PLAY;
				}
			}
			
			// Pickup.
			PICKUP: for (Item droppedItem : World.getInstance().getVisibleObjectsInRange(player, Item.class, 200))
			{
				// Check if item is reachable.
				if ((droppedItem == null) //
					|| (!droppedItem.isSpawned()) //
					|| !GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), droppedItem.getX(), droppedItem.getY(), droppedItem.getZ(), player.getInstanceWorld()))
				{
					continue PICKUP;
				}
				
				// Move to item.
				if (player.calculateDistance2D(droppedItem) > 70)
				{
					if (!player.isMoving())
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, droppedItem);
					}
					continue PICKUP;
				}
				
				// Try to pick it up.
				if (!droppedItem.isProtected() || (droppedItem.getOwnerId() == player.getObjectId()))
				{
					player.doPickupItem(droppedItem);
					continue PICKUP;
				}
			}
			
			if ((player.getTarget() == null) && player.getVariables().getBoolean("자동사냥_제자리가기", false))
			{
				String[] loc = player.getVariables().getString("자동사냥_현재위치").split(" ");
				int _x = Integer.parseInt(loc[0]);
				int _y = Integer.parseInt(loc[1]);
				int _z = Integer.parseInt(loc[2]);
				if (!player.isInsideRadius3D(new Location(_x, _y, _z), player.getVariables().getBoolean("자동사냥_거리설정", false) ? 1200 : 2500))
				{
					player.getVariables().set("자동사냥_제자리이동중", true);
					player.setTarget(null);
					player.sendMessage("자동 사냥: 제자리로 이동합니다.");
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_x, _y, _z));
					continue PLAY;
				}
			}
			
			// Find target.
			Monster monster = null;
			double closestDistance = Double.MAX_VALUE;
			TARGET: for (Monster nearby : World.getInstance().getVisibleObjectsInRange(player, Monster.class, player.getVariables().getBoolean("자동사냥_제자리말뚝딜", false) ? (archer ? player.getPhysicalAttackRange() : 900) : (player.getVariables().getBoolean("자동사냥_거리설정", false) ? 600 : 1400)))
			{
				// Skip unavailable monsters.
				if ((nearby == null) || nearby.isDead() || nearby.isRaid() || nearby.isRaidMinion())
				{
					continue TARGET;
				}
				// Check monster target.
				if ((nearby.getTarget() != null) && (nearby.getTarget() != player))
				{
					Party party = player.getParty();
					if (party != null)
					{
						if (nearby.getTarget() == party.getMembers())
						{
							// Check if monster is reachable.
							if (nearby.isAutoAttackable(player) //
								&& GeoEngine.getInstance().canSeeTarget(player, nearby)//
								&& GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), nearby.getX(), nearby.getY(), nearby.getZ(), player.getInstanceWorld()))
							{
								final double monsterDistance = player.calculateDistance2D(nearby);
								if (monsterDistance < closestDistance)
								{
									monster = nearby;
									closestDistance = monsterDistance;
								}
							}
						}
						else
						{
							continue TARGET;
						}
					}
					else
					{
						continue TARGET;
					}
				}
				// Check if monster is reachable.
				if (nearby.isAutoAttackable(player) //
					&& GeoEngine.getInstance().canSeeTarget(player, nearby)//
					&& GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), nearby.getX(), nearby.getY(), nearby.getZ(), player.getInstanceWorld()))
				{
					final double monsterDistance = player.calculateDistance2D(nearby);
					if (monsterDistance < closestDistance)
					{
						monster = nearby;
						closestDistance = monsterDistance;
					}
				}
			}
			
			// New target was assigned.
			if (monster != null)
			{
				player.setTarget(monster);
				
				if (isWizard(player))
				{
					continue PLAY;
				}
				
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, monster);
			}
			else
			{
				if (!player.getVariables().getBoolean("자동사냥_제자리말뚝딜", false) && !player.getVariables().getBoolean("자동사냥_이동중", false))
				{
					int x1 = player.getX();
					int y1 = player.getY();
					int z1 = player.getZ();
					
					final int range = 400;
					final int deltaX = Rnd.get(range * 2); // x
					int deltaY = Rnd.get(deltaX, range * 2); // distance
					deltaY = (int) Math.sqrt((deltaY * deltaY) - (deltaX * deltaX)); // y
					x1 = (deltaX + x1) - range;
					y1 = (deltaY + y1) - range;
					z1 = player.getZ() + 200;
					
					player.getVariables().set("자동사냥_이동중", true);
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x1, y1, z1));
				}
			}
		}
		
		_working = false;
	}
	
	public void doAutoPlay(Player player)
	{
		if (!PLAYERS.contains(player))
		{
			player.onActionRequest();
			PLAYERS.add(player);
		}
		AutoSkillTaskManager.getInstance().doAutoSkill(player);
		
		player.setAutoPlay(true);
		player.sendMessage("자동 사냥: 자동사냥을 시작 하였습니다.");
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "자동 사냥", "자동사냥을 시작 하였습니다."));
	}
	
	public void stopAutoPlay(Player player)
	{
		if ((!player.isAttackingNow() && !player.isCastingNow()) || player.isMoving())
		{
			player.setTarget(null);
			player.stopMove(null);
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		player.setAutoPlay(false);
		player.sendMessage("자동 사냥: 자동사냥을 종료 하였습니다.");
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "자동 사냥", "자동사냥을 종료 하였습니다."));
		
		PLAYERS.remove(player);
		AutoSkillTaskManager.getInstance().stopAutoSkill(player);
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
	
	public static AutoPlayTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoPlayTaskManager INSTANCE = new AutoPlayTaskManager();
	}
}
