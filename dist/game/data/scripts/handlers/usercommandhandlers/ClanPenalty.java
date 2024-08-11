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
package handlers.usercommandhandlers;

import java.text.SimpleDateFormat;

import org.l2jmobius.gameserver.handler.IUserCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Clan Penalty user command.
 * @author Tempy
 */
public class ClanPenalty implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		100
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		boolean penalty = false;
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		final StringBuilder htmlContent = new StringBuilder(500);
		htmlContent.append("<html><body><center><table width=270 border=0 bgcolor=111111><tr><td width=170>페널티</td><td width=100 align=center>만료 날짜</td></tr></table><table width=270 border=0><tr>");
		
		if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>혈맹에 가입할 수 없습니다.</td><td width=100 align=center>");
			htmlContent.append(format.format(player.getClanJoinExpiryTime()));
			htmlContent.append("</td>");
			penalty = true;
		}
		
		if (player.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>혈맹을 생성할 수 없습니다..</td><td width=100 align=center>");
			htmlContent.append(format.format(player.getClanCreateExpiryTime()));
			htmlContent.append("</td>");
			penalty = true;
		}
		
		if ((player.getClan() != null) && (player.getClan().getCharPenaltyExpiryTime() > System.currentTimeMillis()))
		{
			htmlContent.append("<td width=170>혈맹원을 초대할 수 없습니다.</td><td width=100 align=center>");
			htmlContent.append(format.format(player.getClan().getCharPenaltyExpiryTime()));
			htmlContent.append("</td>");
			penalty = true;
		}
		
		if (!penalty)
		{
			htmlContent.append("<td width=170>페널티가 부과되지 않음.</td><td width=100 align=center></td>");
		}
		
		htmlContent.append("</tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1></center></body></html>");
		
		final NpcHtmlMessage penaltyHtml = new NpcHtmlMessage();
		penaltyHtml.setHtml(htmlContent.toString());
		player.sendPacket(penaltyHtml);
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
