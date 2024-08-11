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
package handlers.admincommandhandlers;

import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * @author Mobius
 */
public class AdminOnline implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_online"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equalsIgnoreCase("admin_online"))
		{
			BorinetUtil.getCounts();
			BuilderUtil.sendSysMessage(activeChar, "접속자 보고서");
			BuilderUtil.sendSysMessage(activeChar, "현재 접속자: " + BorinetUtil.online + "명 | 오프라인 상점: " + BorinetUtil.offline + "명 | 낚시: " + BorinetUtil.fishing + "명");
			BuilderUtil.sendSysMessage(activeChar, "접속자 통계: " + BorinetUtil.total + "명 | 최대 접속자: " + World.MAX_CONNECTED_COUNT + "명");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
