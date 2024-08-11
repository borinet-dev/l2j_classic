package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author VISTALL
 * @date 19:55:45/25.05.2010
 */
public class NotifyStartMiniGame implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		// just trigger
	}
}