package org.l2jmobius.gameserver.model.actor.instance;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.ai.BoatAI;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Vehicle;
import org.l2jmobius.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jmobius.gameserver.network.serverpackets.VehicleDeparture;
import org.l2jmobius.gameserver.network.serverpackets.VehicleInfo;
import org.l2jmobius.gameserver.network.serverpackets.VehicleStarted;

public class Boat extends Vehicle
{
	protected static final Logger LOGGER_BOAT = Logger.getLogger(Boat.class.getName());
	
	private String _boatName;
	
	public Boat(CreatureTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.Boat);
		setAI(new BoatAI(this));
	}
	
	public void setBoatName(String boatNema)
	{
		_boatName = boatNema;
	}
	
	public String getBoatName()
	{
		return _boatName;
	}
	
	@Override
	public boolean isBoat()
	{
		return true;
	}
	
	@Override
	public int getId()
	{
		return 0;
	}
	
	@Override
	public boolean moveToNextRoutePoint()
	{
		final boolean result = super.moveToNextRoutePoint();
		if (result)
		{
			broadcastPacket(new VehicleDeparture(this));
		}
		return result;
	}
	
	@Override
	public void oustPlayer(Player player)
	{
		super.oustPlayer(player);
		
		final Location loc = getOustLoc();
		if (player.isOnline())
		{
			player.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
		}
		else
		{
			player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ()); // disconnects handling
		}
	}
	
	@Override
	public void stopMove(Location loc)
	{
		super.stopMove(loc);
		
		broadcastPacket(new VehicleStarted(this, 0));
		broadcastPacket(new VehicleInfo(this));
	}
	
	@Override
	public void sendInfo(Player player)
	{
		player.sendPacket(new VehicleInfo(this));
	}
}
