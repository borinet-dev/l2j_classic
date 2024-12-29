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
package ai.areas.FantasyIsle;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.HandysBlockCheckerManager;
import org.l2jmobius.gameserver.model.ArenaParticipantsHolder;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExCubeGameChangeTimeToStart;
import org.l2jmobius.gameserver.network.serverpackets.ExCubeGameTeamList;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

/**
 * Handys Block Checker Event AI.
 * @authors BiggBoss, Gigiikun
 */
public class HandysBlockCheckerEvent extends AbstractNpcAI
{
	private static final Logger LOGGER = Logger.getLogger(HandysBlockCheckerEvent.class.getName());
	
	// Arena Managers
	private static final int A_MANAGER_1 = 32521;
	private static final int A_MANAGER_2 = 32522;
	private static final int A_MANAGER_3 = 32523;
	private static final int A_MANAGER_4 = 32524;
	
	public HandysBlockCheckerEvent()
	{
		scheduleSatrtEvent();
		scheduleStopEvent();
		
		addFirstTalkId(A_MANAGER_1, A_MANAGER_2, A_MANAGER_3, A_MANAGER_4);
		HandysBlockCheckerManager.getInstance().startUpParticipantsQueue();
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final long currentTime = System.currentTimeMillis();
		
		final Calendar starttime = Calendar.getInstance();
		starttime.set(Calendar.HOUR_OF_DAY, 19);
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 0);
		
		final Calendar endtime = Calendar.getInstance();
		endtime.set(Calendar.HOUR_OF_DAY, 23);
		endtime.set(Calendar.MINUTE, 0);
		endtime.set(Calendar.SECOND, 0);
		
		if ((npc == null) || (player == null))
		{
			return null;
		}
		if ((currentTime < starttime.getTimeInMillis()) || (currentTime > endtime.getTimeInMillis()))
		{
			return "data/html/guide/HandyEvent-no.htm";
		}
		
		final int arena = npc.getId() - A_MANAGER_1;
		if (eventIsFull(arena))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_REGISTER_BECAUSE_CAPACITY_HAS_BEEN_EXCEEDED);
			return null;
		}
		
		if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(arena))
		{
			player.sendPacket(SystemMessageId.THE_MATCH_IS_BEING_PREPARED_PLEASE_TRY_AGAIN_LATER);
			return null;
		}
		
		if (HandysBlockCheckerManager.getInstance().addPlayerToArena(player, arena))
		{
			final ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
			player.sendPacket(new ExCubeGameTeamList(holder.getRedPlayers(), holder.getBluePlayers(), arena));
			
			final int countBlue = holder.getBlueTeamSize();
			final int countRed = holder.getRedTeamSize();
			final int minMembers = Config.MIN_BLOCK_CHECKER_TEAM_MEMBERS;
			if ((countBlue >= minMembers) && (countRed >= minMembers) && (countBlue == countRed))
			{
				holder.updateEvent();
				holder.broadCastPacketToTeam(new ExCubeGameChangeTimeToStart(10));
				HandysBlockCheckerManager.getInstance().startCountdown(arena);
			}
		}
		return null;
	}
	
	private boolean eventIsFull(int arena)
	{
		return HandysBlockCheckerManager.getInstance().getHolder(arena).getAllPlayers().size() == 12;
	}
	
	private void scheduleSatrtEvent()
	{
		final long currentTime = System.currentTimeMillis();
		final Calendar starttime = Calendar.getInstance();
		starttime.set(Calendar.HOUR_OF_DAY, 19);
		starttime.set(Calendar.MINUTE, 0);
		starttime.set(Calendar.SECOND, 5);
		
		if (starttime.getTimeInMillis() < currentTime)
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::startAnnount, startDelay, BorinetUtil.MILLIS_PER_DAY);
	}
	
	private void scheduleStopEvent()
	{
		final long currentTime = System.currentTimeMillis();
		final Calendar endtime = Calendar.getInstance();
		endtime.set(Calendar.HOUR_OF_DAY, 23);
		endtime.set(Calendar.MINUTE, 0);
		endtime.set(Calendar.SECOND, 0);
		
		if (endtime.getTimeInMillis() < currentTime)
		{
			endtime.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long endDelay = Math.max(0, endtime.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::stopAnnount, endDelay, BorinetUtil.MILLIS_PER_DAY);
	}
	
	protected void startAnnount()
	{
		Broadcast.toAllOnlinePlayersOnScreenS("핸디의 블록체커 경기 진행이 가능합니다!");
		for (Player player : World.getInstance().getPlayers())
		{
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "핸디의 블록체커 경기 진행이 가능합니다!"));
		}
	}
	
	protected void stopAnnount()
	{
		Broadcast.toAllOnlinePlayersOnScreenS("핸디의 블록체커 경기가 종료되었습니다.");
		for (Player player : World.getInstance().getPlayers())
		{
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "핸디의 블록체커 경기가 종료되었습니다."));
		}
		
		for (Player player : World.getInstance().getPlayers())
		{
			player.getAccountVariables().remove("BLOCK_CHECKER");
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("DELETE FROM account_gsdata WHERE var = 'BLOCK_CHECKER';");
		}
		catch (Exception e)
		{
			LOGGER.warning("커스텀이벤트 데이터베이스 정리 오류" + e);
		}
	}
	
	public static void main(String[] args)
	{
		if (Config.ENABLE_BLOCK_CHECKER_EVENT)
		{
			new HandysBlockCheckerEvent();
			LOGGER.info("핸디의 블록체커: 활성화");
		}
		else
		{
			LOGGER.info("핸디의 블록체커: 비활성화");
		}
	}
}
