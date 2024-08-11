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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.clan.ClanPrivilege;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestOustPledgeMember implements IClientIncomingPacket
{
	private String _target;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_target = packet.readS();
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
		if (player.getClan() == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION);
			return;
		}
		if (!player.hasClanPrivilege(ClanPrivilege.CL_DISMISS))
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (player.getName().equalsIgnoreCase(_target))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF);
			return;
		}
		
		final Clan clan = player.getClan();
		final ClanMember member = clan.getClanMember(_target);
		if (member == null)
		{
			PacketLogger.warning("Target (" + _target + ") is not member of the clan");
			return;
		}
		if (member.isOnline() && member.getPlayer().isInCombat())
		{
			player.sendPacket(SystemMessageId.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}
		
		// this also updates the database
		clan.removeClanMember(member.getObjectId(), System.currentTimeMillis() + (Config.ALT_CLAN_JOIN_DAYS * 86400000)); // 24*60*60*1000 = 86400000
		clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + (Config.ALT_CLAN_JOIN_DAYS * 86400000)); // 24*60*60*1000 = 86400000
		clan.updateClanInDB();
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED);
		sm.addString(member.getName());
		clan.broadcastToOnlineMembers(sm);
		player.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_THE_CLAN_MEMBER);
		if (Config.ALT_CLAN_JOIN_DAYS > 1)
		{
			player.sendMessage("혈맹원 제명 후 " + Config.ALT_CLAN_JOIN_DAYS + "일 이내에는 새 혈맹원을 가입시킬 수 없습니다.");
		}
		else
		{
			player.sendMessage("혈맹원 제명 후 즉시 새 혈맹원을 가입시킬 수 있습니다.");
		}
		
		// Remove the Player From the Member list
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));
		clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
		if (member.isOnline())
		{
			final Player target = member.getPlayer();
			if (Config.ALT_CLAN_JOIN_DAYS > 1)
			{
				target.sendMessage("혈맹에서 제명되었습니다. " + Config.ALT_CLAN_JOIN_DAYS + "일 동안은 다른 혈맹에 가입할 수 없습니다.");
			}
			else
			{
				target.sendMessage("혈맹에서 제명되었습니다. 즉시 다른 혈맹에 가입할 수 있습니다.");
			}
		}
	}
}
