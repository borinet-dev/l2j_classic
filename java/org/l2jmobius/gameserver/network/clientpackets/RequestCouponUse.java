package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.CouponManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;

public class RequestCouponUse implements IClientIncomingPacket
{
	private String _coupon_code;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_coupon_code = packet.readS();
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
		String getCoupon = _coupon_code.toUpperCase();
		CouponManager.getInstance().tryUseCoupon(player, getCoupon);
	}
}
