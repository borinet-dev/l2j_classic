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
package org.l2jmobius.gameserver.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.RelationCache;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.RelationChanged;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class Broadcast
{
	private static final Logger LOGGER = Logger.getLogger(Broadcast.class.getName());
	
	/**
	 * Send a packet to all Player in the _KnownPlayers of the Creature that have the Character targeted.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param creature
	 * @param mov
	 */
	public static void toPlayersTargettingMyself(Creature creature, IClientOutgoingPacket mov)
	{
		World.getInstance().forEachVisibleObject(creature, Player.class, player ->
		{
			if (player.getTarget() == creature)
			{
				player.sendPacket(mov);
			}
		});
	}
	
	/**
	 * Send a packet to all Player in the _KnownPlayers of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param creature
	 * @param mov
	 */
	public static void toKnownPlayers(Creature creature, IClientOutgoingPacket mov)
	{
		World.getInstance().forEachVisibleObject(creature, Player.class, player ->
		{
			try
			{
				player.sendPacket(mov);
				if ((mov instanceof CharInfo) && (creature.isPlayer()))
				{
					final int relation = ((Player) creature).getRelation(player);
					final boolean isAutoAttackable = creature.isAutoAttackable(player);
					final RelationCache oldrelation = creature.getKnownRelations().get(player.getObjectId());
					if ((oldrelation == null) || (oldrelation.getRelation() != relation) || (oldrelation.isAutoAttackable() != isAutoAttackable))
					{
						final RelationChanged rc = new RelationChanged();
						rc.addRelation((Player) creature, relation, isAutoAttackable);
						if (creature.hasSummon())
						{
							final Summon pet = creature.getPet();
							if (pet != null)
							{
								rc.addRelation(pet, relation, isAutoAttackable);
							}
							if (creature.hasServitors())
							{
								creature.getServitors().values().forEach(s -> rc.addRelation(s, relation, isAutoAttackable));
							}
						}
						player.sendPacket(rc);
						creature.getKnownRelations().put(player.getObjectId(), new RelationCache(relation, isAutoAttackable));
					}
				}
			}
			catch (NullPointerException e)
			{
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}
		});
	}
	
	/**
	 * Send a packet to all Player in the _KnownPlayers (in the specified radius) of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just needs to go through _knownPlayers to send Server->Client Packet and check the distance between the targets.<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param creature
	 * @param packet
	 * @param radiusValue
	 */
	public static void toKnownPlayersInRadius(Creature creature, IClientOutgoingPacket packet, int radiusValue)
	{
		int radius = radiusValue;
		if (radius < 0)
		{
			radius = 1500;
		}
		
		World.getInstance().forEachVisibleObjectInRange(creature, Player.class, radius, player -> player.sendPacket(packet));
	}
	
	/**
	 * Send a packet to all Player in the _KnownPlayers of the Creature and to the specified character.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet
	 * @param creature
	 * @param mov
	 */
	public static void toSelfAndKnownPlayers(Creature creature, IClientOutgoingPacket mov)
	{
		if (creature.isPlayer())
		{
			creature.sendPacket(mov);
		}
		
		toKnownPlayers(creature, mov);
	}
	
	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(Creature creature, IClientOutgoingPacket packet, int radiusValue)
	{
		int radius = radiusValue;
		if (radius < 0)
		{
			radius = 600;
		}
		
		if (creature.isPlayer())
		{
			creature.sendPacket(packet);
		}
		
		World.getInstance().forEachVisibleObjectInRange(creature, Player.class, radius, player -> player.sendPacket(packet));
	}
	
	/**
	 * Send a packet to all Player present in the world.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _allPlayers to send Server->Client Packet<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param packet
	 */
	public static void toAllOnlinePlayers(IClientOutgoingPacket packet)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
			{
				player.sendPacket(packet);
			}
		}
	}
	
	public static void toAllOnlinePlayersForMsg(String text)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
			{
				player.sendMessage(text);
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, text));
			}
		}
	}
	
	public static void toAllOnlinePlayers(String text)
	{
		toAllOnlinePlayers(text, false);
	}
	
	public static void toAllOnlinePlayers(String text, boolean isCritical)
	{
		toAllOnlinePlayers(new CreatureSay(null, isCritical ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT, "", text));
	}
	
	public static void announcementCritical(String text, boolean isCritical)
	{
		toAllOnlinePlayers(new CreatureSay(null, isCritical ? ChatType.BATTLEFIELD : ChatType.ANNOUNCEMENT, "", text));
	}
	
	public static void announcementMsg(String text)
	{
		toAllOnlinePlayersForMsg(text);
	}
	
	public static void toAllOnlinePlayersOnScreen(String text)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
		sm.addString(text);
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}
	
	public static void toAllOnlinePlayersOnScreenS(String text)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_M);
		sm.addString(text);
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}
	
	public static void toPlayerScreenMessage(Player player, String text)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
		sm.addString(text);
		player.sendPacket(sm);
	}
	
	public static void toPlayerScreenMessageS(Player player, String text)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_M);
		sm.addString(text);
		player.sendPacket(sm);
	}
	
	// GM에게 메세지
	public static void toGMsendMessage(String message)
	{
		for (Player gm : World.getInstance().getPlayers())
		{
			if (gm.isGM())
			{
				gm.sendMessage(message);
			}
		}
	}
	
	/**
	 * Send a packet to all players in a specific zone type.
	 * @param <T> ZoneType.
	 * @param zoneType : The zone type to send packets.
	 * @param packets : The packets to send.
	 */
	public static <T extends ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, IClientOutgoingPacket... packets)
	{
		for (ZoneType zone : ZoneManager.getInstance().getAllZones(zoneType))
		{
			for (Creature creature : zone.getCharactersInside())
			{
				if (creature == null)
				{
					continue;
				}
				
				for (IClientOutgoingPacket packet : packets)
				{
					creature.sendPacket(packet);
				}
			}
		}
	}
}
