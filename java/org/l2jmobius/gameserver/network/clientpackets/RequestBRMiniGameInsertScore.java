package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.games.MiniGameScoreManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;

public class RequestBRMiniGameInsertScore implements IClientIncomingPacket
{
	private int _score;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_score = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		MiniGameScoreManager.getInstance().insertScore(player, _score);
	}
}