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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author Mobius
 */
public class RequestTargetActionMenu implements IClientIncomingPacket
{
	private int _objectId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		packet.readH(); // action?
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		Monster monster = null;
		double closestDistance = Double.MAX_VALUE;
		for (WorldObject object : World.getInstance().getVisibleObjectsInRange(player, WorldObject.class, 700))
		{
			if (_objectId == object.getObjectId())
			{
				if (object.isTargetable() && object.isAutoAttackable(player) && GeoEngine.getInstance().canSeeTarget(player, object) && GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), object.getX(), object.getY(), object.getZ(), player.getInstanceWorld()))
				{
					final double monsterDistance = player.calculateDistance2D(object);
					if (monsterDistance < closestDistance)
					{
						monster = (Monster) object;
						closestDistance = monsterDistance;
					}
				}
				if (monster != null)
				{
					player.setTarget(monster);
				}
				break;
			}
		}
	}
}
