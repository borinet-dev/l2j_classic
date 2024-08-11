package org.l2jmobius.gameserver.model;

/**
 * @author 보리넷 가츠
 */
public class HarborNearLocation extends Location
{
	public HarborNearLocation(Location loc)
	{
		this(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public HarborNearLocation(int x, int y, int z)
	{
		super(x, y, z);
	}
	
	public static final HarborNearLocation[] TALKING =
	{
		new HarborNearLocation(-96817, 259982, -3616)
	};
	
	public static final HarborNearLocation[] GLUDIN =
	{
		new HarborNearLocation(-91737, 150510, -3624)
	};
	
	public static final HarborNearLocation[] GIRAN =
	{
		new HarborNearLocation(47347, 187790, -3552)
	};
}
