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
package handlers.targethandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.ITargetTypeHandler;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.targets.TargetType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneRegion;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Target ground location. Returns yourself if your current skill's ground location meets the conditions.
 * @author Nik
 */
public class Ground implements ITargetTypeHandler
{
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.GROUND;
	}
	
	@Override
	public WorldObject getTarget(Creature creature, WorldObject selectedTarget, Skill skill, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		if (creature.isPlayer())
		{
			final Location worldPosition = creature.getActingPlayer().getCurrentSkillWorldPosition();
			if (worldPosition != null)
			{
				if (dontMove && !creature.isInsideRadius2D(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + creature.getTemplate().getCollisionRadius()))
				{
					return null;
				}
				
				if (!GeoEngine.getInstance().canSeeTarget(creature, worldPosition))
				{
					if (sendMessage)
					{
						creature.sendPacket(SystemMessageId.CANNOT_SEE_TARGET);
					}
					return null;
				}
				
				final ZoneRegion zoneRegion = ZoneManager.getInstance().getRegion(creature);
				if (skill.isBad() && !creature.isInInstance() && !zoneRegion.checkEffectRangeInsidePeaceZone(skill, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()))
				{
					if (sendMessage)
					{
						creature.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILLS_THAT_MAY_HARM_OTHER_PLAYERS_IN_HERE);
					}
					return null;
				}
				if (!Config.ENABLE_PK && skill.isBad())
				{
					final Creature target = (Creature) selectedTarget;
					
					if ((target != null) && (!creature.isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && target.isPlayable())
					{
						final Clan leaderClan = creature.getClan();
						final Clan targetClan = target.getClan();
						if (((leaderClan == null) || (targetClan == null) || !leaderClan.isAtWarWith(targetClan.getId())) && //
							!creature.getActingPlayer().isInOlympiadMode() && //
							!creature.getActingPlayer().isInSiege() && //
							!creature.getActingPlayer().isOnEvent())
						{
							if (sendMessage)
							{
								creature.sendMessage("PVP존이 아닌곳에 있어서 공격할 수 없습니다.");
							}
							return null;
						}
					}
				}
				
				return creature; // Return yourself to know that your ground location is legit.
			}
		}
		
		return null;
	}
}