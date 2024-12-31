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
package org.l2jmobius.gameserver.instancemanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.Team;
import org.l2jmobius.gameserver.instancemanager.tasks.PenaltyRemoveTask;
import org.l2jmobius.gameserver.model.ArenaParticipantsHolder;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExCubeGameAddPlayer;
import org.l2jmobius.gameserver.network.serverpackets.ExCubeGameChangeTeam;
import org.l2jmobius.gameserver.network.serverpackets.ExCubeGameChangeTimeToStart;
import org.l2jmobius.gameserver.network.serverpackets.ExCubeGameCloseUI;
import org.l2jmobius.gameserver.network.serverpackets.ExCubeGameRemovePlayer;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manage the player add/remove, team change and event arena status,<br>
 * as the clearance of the participants list or liberate the arena.
 * @author BiggBoss
 */
public class HandysBlockCheckerManager
{
	// All the participants and their team classified by arena
	private static final ArenaParticipantsHolder[] _arenaPlayers = new ArenaParticipantsHolder[4];
	
	// Arena votes to start the game
	private static final Map<Integer, Integer> _arenaVotes = new HashMap<>();
	
	// Arena Status, True = is being used, otherwise, False
	private static final Map<Integer, Boolean> _arenaStatus = new HashMap<>();
	
	// Registration request penalty (10 seconds)
	protected static Set<Integer> _registrationPenalty = Collections.synchronizedSet(new HashSet<>());
	
	/**
	 * Return the number of event-start votes for the specified arena id
	 * @param arenaId
	 * @return int (number of votes)
	 */
	public synchronized int getArenaVotes(int arenaId)
	{
		return _arenaVotes.get(arenaId);
	}
	
	/**
	 * Will clear the votes queue (of event start) for the specified arena id
	 * @param arena
	 */
	public synchronized void clearArenaVotes(int arena)
	{
		_arenaVotes.put(arena, 0);
	}
	
	protected HandysBlockCheckerManager()
	{
		// Initialize arena status
		_arenaStatus.put(0, false);
		_arenaStatus.put(1, false);
		_arenaStatus.put(2, false);
		_arenaStatus.put(3, false);
		
		// Initialize arena votes
		_arenaVotes.put(0, 0);
		_arenaVotes.put(1, 0);
		_arenaVotes.put(2, 0);
		_arenaVotes.put(3, 0);
	}
	
	/**
	 * Returns the players holder
	 * @param arena
	 * @return ArenaParticipantsHolder
	 */
	public ArenaParticipantsHolder getHolder(int arena)
	{
		return _arenaPlayers[arena];
	}
	
	/**
	 * Initializes the participants holder
	 */
	public void startUpParticipantsQueue()
	{
		for (int i = 0; i < 4; ++i)
		{
			_arenaPlayers[i] = new ArenaParticipantsHolder(i);
		}
	}
	
	/**
	 * Add the player to the specified arena (through the specified arena manager) and send the needed server -> client packets
	 * @param player
	 * @param arenaId
	 * @return
	 */
	public boolean addPlayerToArena(Player player, int arenaId)
	{
		final ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
		
		// 중복 등록 확인
		if (isPlayerAlreadyRegistered(player))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST).addString(player.getName()));
			return false;
		}
		
		// 금지된 상태 확인
		if (player.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_REGISTER_WHILE_IN_POSSESSION_OF_A_CURSED_WEAPON);
			return false;
		}
		
		if (player.isOnEvent() || player.isInOlympiadMode())
		{
			player.sendMessage("다른 이벤트에 참여 중이기 때문에 등록할 수 없습니다.");
			return false;
		}
		
		if (OlympiadManager.getInstance().isRegistered(player))
		{
			OlympiadManager.getInstance().unRegisterNoble(player);
			player.sendPacket(SystemMessageId.APPLICANTS_FOR_THE_OLYMPIAD_UNDERGROUND_COLISEUM_OR_KRATEI_S_CUBE_MATCHES_CANNOT_REGISTER);
			player.sendMessage("올림피아드 등록이 해제되었습니다. 다시 시도해주세요.");
			return false;
		}
		
		if (_registrationPenalty.contains(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_10_SECONDS_BEFORE_ATTEMPTING_TO_REGISTER_AGAIN);
			return false;
		}
		
		if (player.getAccountVariables().getBoolean("BLOCK_CHECKER", false))
		{
			String message = "오늘은 이미 경기를 진행했습니다. 하루에 한번만 참가 가능합니다.";
			player.sendMessage(message);
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, message));
			return false;
		}
		
		synchronized (holder)
		{
			boolean isRed;
			if (holder.getBlueTeamSize() < holder.getRedTeamSize())
			{
				holder.addPlayer(player, 1);
				isRed = false;
			}
			else
			{
				holder.addPlayer(player, 0);
				isRed = true;
			}
			
			// 팀에 플레이어 추가를 알림
			holder.broadCastPacketToTeam(new ExCubeGameAddPlayer(player, isRed));
			return true;
		}
	}
	
	// 플레이어가 이미 등록되었는지 확인
	private boolean isPlayerAlreadyRegistered(Player player)
	{
		for (int i = 0; i < _arenaPlayers.length; i++)
		{
			if (_arenaPlayers[i].getAllPlayers().contains(player))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Will remove the specified player from the specified team and arena and will send the needed packet to all his team mates / enemy team mates
	 * @param player
	 * @param arenaId
	 * @param team
	 */
	public void removePlayer(Player player, int arenaId, int team)
	{
		final ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
		synchronized (holder)
		{
			final boolean isRed = team == 0;
			
			// 플레이어 제거
			holder.removePlayer(player, team);
			holder.broadCastPacketToTeam(new ExCubeGameRemovePlayer(player, isRed));
			
			// 페널티 등록 및 제거 스케줄
			if (!_registrationPenalty.contains(player.getObjectId()))
			{
				_registrationPenalty.add(player.getObjectId());
				schedulePenaltyRemoval(player.getObjectId());
			}
			
			// 팀 상태 확인
			final int countBlue = holder.getBlueTeamSize();
			final int countRed = holder.getRedTeamSize();
			final int minMembers = Config.MIN_BLOCK_CHECKER_TEAM_MEMBERS;
			
			// 팀 조건 만족 여부 판단
			if ((countBlue >= minMembers) && (countRed >= minMembers) && (countBlue == countRed))
			{
				// 이미 카운트다운이 진행 중인 경우 중복 실행 방지
				if (!holder.isCountdownRunning())
				{
					holder.setCountdownRunning(true); // 플래그 설정
					holder.updateEvent();
					holder.broadCastPacketToTeam(new ExCubeGameChangeTimeToStart(10));
					
					// 새로운 스레드에서 카운트다운 시작
					new Thread(() ->
					{
						try
						{
							startCountdown(arenaId);
						}
						finally
						{
							holder.setCountdownRunning(false); // 플래그 해제
						}
					}).start();
				}
			}
			else
			{
				// 조건 미충족 시 진행 중인 이벤트를 종료할 수도 있음 (옵션)
				if (holder.isCountdownRunning())
				{
					holder.setCountdownRunning(false); // 플래그 해제
				}
			}
		}
	}
	
	public void startCountdown(int arenaId)
	{
		final ArenaParticipantsHolder holder = getHolder(arenaId);
		if (holder == null)
		{
			return;
		}
		
		new Thread(() ->
		{
			try
			{
				for (int i = 10; i > 0; i--)
				{
					if (holder.getEvent().isStarted() || //
					(holder.getBlueTeamSize() != holder.getRedTeamSize()) || //
					(holder.getBlueTeamSize() < Config.MIN_BLOCK_CHECKER_TEAM_MEMBERS) || //
					(holder.getRedTeamSize() < Config.MIN_BLOCK_CHECKER_TEAM_MEMBERS))
					{
						return;
					}
					Thread.sleep(1000); // 1초 대기
				}
				
				// 0초가 되었을 때 경기를 실행
				if (Config.HBCE_FAIR_PLAY)
				{
					holder.checkAndShuffle();
				}
				// 경기 시작
				ThreadPool.execute(holder.getEvent().new StartEvent());
			}
			catch (InterruptedException e)
			{
				System.err.println("카운트다운 스레드가 중단되었습니다: " + e.getMessage());
			}
		}).start();
	}
	
	/**
	 * Will change the player from one team to other (if possible) and will send the needed packets
	 * @param player
	 * @param arena
	 */
	public void changePlayerToTeam(Player player, int arena)
	{
		final ArenaParticipantsHolder holder = _arenaPlayers[arena];
		
		synchronized (holder)
		{
			final boolean isFromRed = holder.getRedPlayers().contains(player);
			if (isFromRed && (holder.getBlueTeamSize() == 6))
			{
				player.sendMessage("팀이 가득 찼습니다.");
				return;
			}
			else if (!isFromRed && (holder.getRedTeamSize() == 6))
			{
				player.sendMessage("팀이 가득 찼습니다.");
				return;
			}
			
			final int futureTeam = isFromRed ? 1 : 0;
			holder.addPlayer(player, futureTeam);
			if (isFromRed)
			{
				holder.removePlayer(player, 0);
			}
			else
			{
				holder.removePlayer(player, 1);
			}
			holder.broadCastPacketToTeam(new ExCubeGameChangeTeam(player, isFromRed));
		}
	}
	
	/**
	 * Will erase all participants from the specified holder
	 * @param arenaId
	 */
	public synchronized void clearPaticipantQueueByArenaId(int arenaId)
	{
		_arenaPlayers[arenaId].clearPlayers();
	}
	
	/**
	 * Returns true if arena is holding an event at this momment
	 * @param arenaId
	 * @return boolean
	 */
	public boolean arenaIsBeingUsed(int arenaId)
	{
		if ((arenaId < 0) || (arenaId > 3))
		{
			return false;
		}
		return _arenaStatus.get(arenaId);
	}
	
	/**
	 * Set the specified arena as being used
	 * @param arenaId
	 */
	public void setArenaBeingUsed(int arenaId)
	{
		_arenaStatus.put(arenaId, true);
	}
	
	/**
	 * Set as free the specified arena for future events
	 * @param arenaId
	 */
	public void setArenaFree(int arenaId)
	{
		_arenaStatus.put(arenaId, false);
	}
	
	/**
	 * Called when played logs out while participating in Block Checker Event
	 * @param player
	 */
	public void onDisconnect(Player player)
	{
		final int arena = player.getBlockCheckerArena();
		final int team = getHolder(arena).getPlayerTeam(player);
		getInstance().removePlayer(player, arena, team);
		if (player.getTeam() != Team.NONE)
		{
			player.stopAllEffects();
			// Remove team aura
			player.setTeam(Team.NONE);
			
			// Remove the event items
			final PlayerInventory inv = player.getInventory();
			if (inv.getItemByItemId(13787) != null)
			{
				final long count = inv.getInventoryItemCount(13787, 0);
				inv.destroyItemByItemId("Handys Block Checker", 13787, count, player, player);
			}
			if (inv.getItemByItemId(13788) != null)
			{
				final long count = inv.getInventoryItemCount(13788, 0);
				inv.destroyItemByItemId("Handys Block Checker", 13788, count, player, player);
			}
			player.setInsideZone(ZoneId.PVP, false);
			
			player.sendPacket(ExCubeGameCloseUI.STATIC_PACKET);
			// Teleport Back
			player.teleToLocation(-57478, -60367, -2370);
		}
	}
	
	public void removePenalty(int objectId)
	{
		_registrationPenalty.remove(objectId);
	}
	
	private void schedulePenaltyRemoval(int objId)
	{
		ThreadPool.schedule(new PenaltyRemoveTask(objId), 10000);
	}
	
	/**
	 * Gets the single instance of {@code HandysBlockCheckerManager}.
	 * @return single instance of {@code HandysBlockCheckerManager}
	 */
	public static HandysBlockCheckerManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HandysBlockCheckerManager INSTANCE = new HandysBlockCheckerManager();
	}
}
