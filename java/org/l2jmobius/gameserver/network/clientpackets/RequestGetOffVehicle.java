package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.GetOffVehicle;
import org.l2jmobius.gameserver.network.serverpackets.StopMoveInVehicle;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import org.l2jmobius.gameserver.util.Broadcast;

public class RequestGetOffVehicle implements IClientIncomingPacket
{
	private int _boatId;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_boatId = packet.readD();
		_x = packet.readD();
		_y = packet.readD();
		_z = packet.readD();
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
		
		Boat boat = player.getBoat();
		if (boat == null)
		{
			return;
		}
		
		if ((boat.getObjectId() != _boatId))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (boat.isMoving() || player.isInsideZone(ZoneId.WATER))
		{
			player.setLocation(new Location(boat.getLocation().getX(), boat.getLocation().getY(), (boat.getLocation().getZ() + 3)));
			player.sendPacket(new ValidateLocation(player));
			player.broadcastPacket(new ValidateLocation(player));
			player.stopMove(new Location(boat.getLocation().getX(), boat.getLocation().getY(), (boat.getLocation().getZ() + 3)));
			return;
		}
		
		player.broadcastPacket(new StopMoveInVehicle(player, _boatId));
		player.setOnBoat(false);
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.broadcastPacket(new GetOffVehicle(player.getObjectId(), _boatId, _x, _y, _z));
		player.setXYZ(_x, _y, _z);
		player.setInsideZone(ZoneId.PEACE, false);
		player.revalidateZone(true);
		Broadcast.toPlayerScreenMessage(player, boat.getBoatName() + "에서 하선하였습니다.");
		player.deleteQuickVar("boatReward");
	}
}
