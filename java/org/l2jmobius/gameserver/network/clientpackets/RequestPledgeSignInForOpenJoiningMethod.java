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
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.entry.PledgeRecruitInfo;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import org.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * @author Mobius
 */
public class RequestPledgeSignInForOpenJoiningMethod implements IClientIncomingPacket
{
	private int _clanId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_clanId = packet.readD();
		packet.readD();
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
		
		final PledgeRecruitInfo pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(_clanId);
		if (pledgeRecruitInfo != null)
		{
			final Clan clan = pledgeRecruitInfo.getClan();
			if ((clan != null) && (player.getClan() == null))
			{
				if (Config.ALT_DISABLE_DOUBLE_JOIN_CLAN && !Config.ALLOWED_CREATE_JOIN_HWID.contains(player.getHWID()))
				{
					if (BorinetUtil.getInstance().conditionClan(player, clan.getId()))
					{
						player.sendMessage("다른 혈맹에 가입할 수 없습니다. (이중 가입 금지)");
						return;
					}
				}
				if (clan.getCharPenaltyExpiryTime() > System.currentTimeMillis())
				{
					if (Config.ALT_CLAN_JOIN_DAYS > 1)
					{
						player.sendMessage("혈맹원 제명 후 " + Config.ALT_CLAN_JOIN_DAYS + "일이 지나지 않아 가입할 수 없습니다.");
					}
					return;
				}
				if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.C1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_THEY_LEFT_ANOTHER_CLAN);
					sm.addString(player.getName());
					player.sendPacket(sm);
					return;
				}
				if (clan.getSubPledgeMembersCount(0) >= clan.getMaxNrOfMembers(0))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME);
					sm.addString(clan.getName());
					player.sendPacket(sm);
					return;
				}
				
				player.sendPacket(new JoinPledge(clan.getId()));
				
				// player.setPowerGrade(9); // academy
				player.setPowerGrade(5); // New member starts at 5, not confirmed.
				clan.addClanMember(player);
				player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
				player.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
				sm.addString(player.getName());
				clan.broadcastToOnlineMembers(sm);
				
				if (clan.getCastleId() > 0)
				{
					final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
					if (castle != null)
					{
						castle.giveResidentialSkills(player);
					}
				}
				if (clan.getFortId() > 0)
				{
					final Fort fort = FortManager.getInstance().getFortByOwner(clan);
					if (fort != null)
					{
						fort.giveResidentialSkills(player);
					}
				}
				player.sendSkillList();
				
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(player), player);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
				
				// This activates the clan tab on the new member.
				PledgeShowMemberListAll.sendAllTo(player);
				player.setClanJoinExpiryTime(0);
				player.broadcastUserInfo();
				
				ClanEntryManager.getInstance().removePlayerApplication(_clanId, player.getObjectId());
			}
		}
	}
}