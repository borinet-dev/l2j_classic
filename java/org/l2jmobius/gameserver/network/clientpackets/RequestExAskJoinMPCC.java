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
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExAskJoinMPCC;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author 보리넷 가츠
 */
public class RequestExAskJoinMPCC implements IClientIncomingPacket
{
	private String _name;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_name = packet.readS();
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
		
		if (player.isProcessingRequest())
		{
			player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		if (!player.isInParty())
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
			return;
		}
		
		Player target = World.getInstance().getPlayer(_name);
		if (target == null)
		{
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
			return;
		}
		
		if ((player == target) || (player.getParty() == target.getParty()) || !target.isInParty())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		if (target.isInParty() && !target.getParty().isLeader(target))
		{
			target = target.getParty().getLeader();
		}
		
		if (target == null)
		{
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
			return;
		}
		
		SystemMessage sm;
		if (target.getParty().isInCommandChannel())
		{
			sm = new SystemMessage(SystemMessageId.C1_S_PARTY_IS_ALREADY_A_MEMBER_OF_THE_COMMAND_CHANNEL);
			sm.addString(target.getName());
			player.sendPacket(sm);
			return;
		}
		
		Party activeParty = player.getParty();
		if (activeParty.isInCommandChannel())
		{
			if (player.getCommandChannel().getLeader() != player)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
				return;
			}
		}
		askJoinMPCC(player, target);
	}
	
	private void askJoinMPCC(Player requestor, Player target)
	{
		boolean hasRight = false;
		if (requestor.isClanLeader() && (requestor.getClan().getLevel() >= 5))
		{
			// Clan leader of level5 Clan or higher.
			hasRight = true;
		}
		
		if (requestor.getInventory().getItemByItemId(8871) != null)
		{
			// 8871 Strategy Guide.
			// TODO: Should destroyed after successful invite?
			hasRight = true;
		}
		
		if (requestor.getKnownSkill(391) != null)
		{
			// At least Baron or higher and the skill Clan Imperium
			hasRight = true;
		}
		
		if (!hasRight)
		{
			requestor.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
			return;
		}
		
		// Get the target's party leader, and do whole actions on him.
		final Player targetLeader = target.getParty().getLeader();
		SystemMessage sm;
		if (!targetLeader.isProcessingRequest())
		{
			requestor.onTransactionRequest(targetLeader);
			sm = new SystemMessage(SystemMessageId.C1_IS_INVITING_YOU_TO_A_COMMAND_CHANNEL_DO_YOU_ACCEPT);
			sm.addString(requestor.getName());
			targetLeader.sendPacket(sm);
			targetLeader.sendPacket(new ExAskJoinMPCC(requestor.getName()));
			requestor.sendMessage(targetLeader.getName() + "님의 파티를 연합채널에 초대했습니다.");
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
			sm.addString(targetLeader.getName());
			requestor.sendPacket(sm);
		}
	}
}
