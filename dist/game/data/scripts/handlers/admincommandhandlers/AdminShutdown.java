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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import org.l2jmobius.gameserver.Shutdown;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * This class handles following admin commands: - server_shutdown [sec] = shows menu or shuts down server in sec seconds
 */
public class AdminShutdown implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_server_edit",
		"admin_server_shutdown",
		"admin_server_restart",
		"admin_server_abort"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_server_edit"))
		{
			sendHtmlForm(activeChar);
		}
		else if (command.startsWith("admin_server_shutdown"))
		{
			try
			{
				final String shutdownBuypass = command.replace("admin_server_shutdown ", "");
				final String[] val = shutdownBuypass.split(" ");
				
				if (val.length == 1)
				{
					serverShutdown(activeChar, Integer.parseInt(val[0]), false);
				}
				else if (val.length == 4)
				{
					setTimes(activeChar, Integer.parseInt(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2]), Integer.parseInt(val[3]), false);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_restart"))
		{
			try
			{
				final String shutdownBuypass = command.replace("admin_server_restart ", "");
				final String[] val = shutdownBuypass.split(" ");
				
				if (val.length == 1)
				{
					serverShutdown(activeChar, Integer.parseInt(val[0]), true);
					sendHtmlForm(activeChar);
				}
				else if (val.length == 4)
				{
					setTimes(activeChar, Integer.parseInt(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2]), Integer.parseInt(val[3]), true);
					sendHtmlForm(activeChar);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_abort"))
		{
			serverAbort(activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void setTimes(Player activeChar, int month, int days, int hour, int mins, boolean restart)
	{
		final Calendar calendar = Calendar.getInstance();
		Instant currentInstant = Instant.now();
		LocalDateTime futureDateTime = LocalDateTime.of(calendar.get(Calendar.YEAR), month, days, hour, mins, 1);
		long millisecondsUntilFuture = ChronoUnit.MILLIS.between(currentInstant, futureDateTime.atZone(ZoneId.systemDefault()).toInstant());
		int secondsUntilFuture = (int) (millisecondsUntilFuture / 1000);
		
		serverShutdown(activeChar, secondsUntilFuture, restart);
	}
	
	private void sendHtmlForm(Player activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(0, 1);
		final int t = GameTimeTaskManager.getInstance().getGameTime();
		final int h = t / 60;
		final int m = t % 60;
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		cal.set(Calendar.SECOND, 0);
		
		int online = 0;
		int fishing = 0;
		for (Player players : World.getInstance().getPlayers())
		{
			if (players.isOnline() && !players.isInOfflineMode())
			{
				online++;
				if (players.isFishing())
				{
					fishing++;
				}
			}
		}
		adminReply.setFile(activeChar, "data/html/admin/shutdown.htm");
		adminReply.replace("%count%", String.valueOf(online));
		adminReply.replace("%fishing%", String.valueOf(fishing));
		adminReply.replace("%used%", String.valueOf(BorinetUtil.getInstance().getUsedMemoryGB() + "/" + BorinetUtil.getInstance().getTotalMemoryGB() + " GB 사용"));
		adminReply.replace("%time%", BorinetUtil.timeFormat.format(cal.getTime()));
		adminReply.replace("%serverTime%", BorinetUtil.timeFormat.format(new Date(System.currentTimeMillis())));
		activeChar.sendPacket(adminReply);
	}
	
	private void serverShutdown(Player activeChar, int seconds, boolean restart)
	{
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}
	
	private void serverAbort(Player activeChar)
	{
		Shutdown.getInstance().abort(activeChar);
	}
}
