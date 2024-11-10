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
package handlers.punishmenthandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.TimeUtil;
import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IPunishmentHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.tasks.player.TeleportTask;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.punishment.PunishmentTask;
import org.l2jmobius.gameserver.model.punishment.PunishmentType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.type.JailZone;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * This class handles jail punishment.
 * @author UnAfraid
 */
public class JailHandler implements IPunishmentHandler
{
	private static final Logger LOGGER = Logger.getLogger(JailHandler.class.getName());
	
	public JailHandler()
	{
		// Register global listener
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOGIN, (OnPlayerLogin event) -> onPlayerLogin(event), this));
	}
	
	private void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player.isJailed() && !player.isInsideZone(ZoneId.JAIL))
		{
			goJailToPlayer(player, false);
		}
		else if (!player.isJailed() && player.isInsideZone(ZoneId.JAIL) && !player.isGM())
		{
			removeFromPlayer(player);
		}
		else if (player.isJailed() && player.isInsideZone(ZoneId.JAIL))
		{
			goJailToPlayer(player, true);
		}
	}
	
	@Override
	public void onStart(PunishmentTask task)
	{
		switch (task.getAffect())
		{
			case CHARACTER:
			{
				final int objectId = Integer.parseInt(String.valueOf(task.getKey()));
				final Player player = World.getInstance().getPlayer(objectId);
				if (player != null)
				{
					applyToPlayer(task, player);
				}
				break;
			}
			case ACCOUNT:
			{
				final String account = String.valueOf(task.getKey());
				final GameClient client = LoginServerThread.getInstance().getClient(account);
				if (client != null)
				{
					final Player player = client.getPlayer();
					if (player != null)
					{
						applyToPlayer(task, player);
					}
				}
				break;
			}
			case IP:
			{
				final String ip = String.valueOf(task.getKey());
				for (Player player : World.getInstance().getPlayers())
				{
					if (player.getIPAddress().equals(ip))
					{
						applyToPlayer(task, player);
					}
				}
				break;
			}
		}
	}
	
	@Override
	public void onEnd(PunishmentTask task)
	{
		switch (task.getAffect())
		{
			case CHARACTER:
			{
				final int objectId = Integer.parseInt(String.valueOf(task.getKey()));
				final Player player = World.getInstance().getPlayer(objectId);
				if (player != null)
				{
					removeFromPlayer(player);
				}
				break;
			}
			case ACCOUNT:
			{
				final String account = String.valueOf(task.getKey());
				final GameClient client = LoginServerThread.getInstance().getClient(account);
				if (client != null)
				{
					final Player player = client.getPlayer();
					if (player != null)
					{
						removeFromPlayer(player);
					}
				}
				break;
			}
			case IP:
			{
				final String ip = String.valueOf(task.getKey());
				for (Player player : World.getInstance().getPlayers())
				{
					if (player.getIPAddress().equals(ip))
					{
						removeFromPlayer(player);
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Applies all punishment effects from the player.
	 * @param task
	 * @param player
	 */
	private void applyToPlayer(PunishmentTask task, Player player)
	{
		player.setInstance(null);
		
		if (OlympiadManager.getInstance().isRegisteredInComp(player))
		{
			OlympiadManager.getInstance().removeDisconnectedCompetitor(player);
		}
		
		ThreadPool.schedule(new TeleportTask(player, JailZone.getLocationIn()), 100);
		
		// Open a Html message to inform the player
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		int count = player.getVariables().getInt("BotReported", 0);
		String content = HtmCache.getInstance().getHtm(player, "data/html/jail_in.htm");
		String times = BorinetUtil.dataDateFormatKor.format(new Date(task.getExpirationTime()));
		content = content.replace("%times%", count == 5 ? "무기한" : times);
		content = content.replace("%reason%", task.getReason());
		msg.setHtml(content);
		player.sendPacket(msg);
		
		final int delay = (int) (((task.getExpirationTime() - System.currentTimeMillis()) / 1000L) / 60);
		if ((delay > 0) && (count < 4))
		{
			player.sendMessage("출감까지 남은시간: " + TimeUtil.formatTimes(delay, false));
		}
		else
		{
			player.sendMessage("출감까지 남은시간이 없습니다. 운영자에게 문의하세요.");
		}
		player.setBlockActions(false);
		player.setInvul(false);
	}
	
	private void goJailToPlayer(Player player, boolean isinJailed)
	{
		if (!isinJailed)
		{
			player.setInstance(null);
			
			if (OlympiadManager.getInstance().isRegisteredInComp(player))
			{
				OlympiadManager.getInstance().removeDisconnectedCompetitor(player);
			}
			
			ThreadPool.schedule(new TeleportTask(player, JailZone.getLocationIn()), 100);
			
		}
		String[] account = getJailAccount(player);
		String endTime = account[0];
		String reason = account[1];
		
		String[] name = getJailCharName(player);
		endTime += name[0];
		reason += name[1];
		
		String[] ip = getJailIP(player);
		endTime += ip[0];
		reason += ip[1];
		
		// Open a Html message to inform the player
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		int count = player.getVariables().getInt("BotReported", 0);
		String content = HtmCache.getInstance().getHtm(player, "data/html/jail_in.htm");
		long endTimes = Long.parseLong(endTime);
		String times = BorinetUtil.dataDateFormatKor.format(new Date(endTimes));
		content = content.replace("%times%", count >= 5 ? "없음" : times);
		content = content.replace("%reason%", reason);
		msg.setHtml(content);
		player.sendPacket(msg);
		
		final int delay = (int) (((endTimes - System.currentTimeMillis()) / 1000L) / 60);
		if ((delay > 0) && (count < 5))
		{
			player.sendMessage("출감까지 남은시간: " + TimeUtil.formatTimes(delay, false));
		}
		else
		{
			player.sendMessage("출감까지 남은시간이 없습니다. 운영자에게 문의하세요.");
		}
		player.setBlockActions(false);
		player.setInvul(false);
	}
	
	/**
	 * Removes any punishment effects from the player.
	 * @param player
	 */
	private void removeFromPlayer(Player player)
	{
		ThreadPool.schedule(new TeleportTask(player, JailZone.getLocationOut()), 100);
		
		// Open a Html message to inform the player
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		final String content = HtmCache.getInstance().getHtm(player, "data/html/jail_out.htm");
		if (content != null)
		{
			msg.setHtml(content);
		}
		else
		{
			msg.setHtml("<html><title>감옥 출감</title><body><br>출감되었습니다.<br>서버 규칙을 준수해 주시기 바랍니다.</body></html>");
		}
		player.sendPacket(msg);
	}
	
	public String[] getJailCharName(Player player)
	{
		String expiration = "";
		String reason = "";
		String query = "SELECT expiration, reason FROM punishments WHERE `key` = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, player.getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					expiration = rset.getString("expiration");
					reason = rset.getString("reason");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "플레이어의 감옥 정보를 조회하는 도중 오류가 발생했습니다. 플레이어 ID: " + player.getObjectId(), e);
		}
		return new String[]
		{
			expiration,
			reason
		};
	}
	
	public String[] getJailAccount(Player player)
	{
		String expiration = "";
		String reason = "";
		String query = "SELECT expiration, reason FROM punishments WHERE `key` = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setString(1, player.getAccountName());
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					expiration = rset.getString("expiration");
					reason = rset.getString("reason");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "계정의 감옥 정보를 조회하는 도중 오류가 발생했습니다. 계정 이름: " + player.getAccountName(), e);
		}
		return new String[]
		{
			expiration,
			reason
		};
	}
	
	public String[] getJailIP(Player player)
	{
		String expiration = "";
		String reason = "";
		String query = "SELECT expiration, reason FROM punishments WHERE `key` = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setString(1, player.getIPAddress());
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					expiration = rset.getString("expiration");
					reason = rset.getString("reason");
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "IP 주소의 감옥 정보를 조회하는 도중 오류가 발생했습니다. IP 주소: " + player.getIPAddress(), e);
		}
		return new String[]
		{
			expiration,
			reason
		};
	}
	
	@Override
	public PunishmentType getType()
	{
		return PunishmentType.JAIL;
	}
}
