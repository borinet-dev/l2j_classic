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
package handlers.dailymissionhandlers;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.npc.OnAttackableKill;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author UnAfraid
 */
public class BossDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;
	private final String _name;
	private final DailyMissionDataHolder _holder;
	
	public BossDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
		_name = holder.getName();
		_holder = holder;
	}
	
	@Override
	public void init()
	{
		Containers.Monsters().addListener(new ConsumerEventListener(this, EventType.ON_ATTACKABLE_KILL, (OnAttackableKill event) -> onAttackableKill(event), this));
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if (entry != null)
		{
			switch (entry.getStatus())
			{
				case 수령가능:
				case 수령완료:
				{
					return true;
				}
				case 진행중: // Initial state
				{
					if (entry.getProgress() >= _amount)
					{
						entry.setStatus(DailyMissionStatus.수령가능);
						storePlayerEntry(entry);
					}
					break;
				}
			}
		}
		return false;
	}
	
	private void onAttackableKill(OnAttackableKill event)
	{
		final Attackable monster = event.getTarget();
		final Player attacker = event.getAttacker();
		if (_holder.isMainClassOnly() && attacker.isSubClassActive())
		{
			// 메인 클래스만 수행 가능한 미션인데, 플레이어가 메인 클래스가 아닌 경우에는 아무것도 하지 않음
			return;
		}
		
		if (monster.isRaid() && (monster.getInstanceId() > 0) && (attacker != null))
		{
			final Party party = attacker.getParty();
			if (party != null)
			{
				final List<Player> playersInRange = new ArrayList<>();
				final boolean isInCC = party.isInCommandChannel();
				final List<Player> members = isInCC ? party.getCommandChannel().getMembers() : party.getMembers();
				
				for (Player member : members)
				{
					if (member.calculateDistance3D(monster) <= Config.ALT_PARTY_RANGE)
					{
						playersInRange.add(member);
					}
				}
				
				if (!playersInRange.isEmpty())
				{
					for (Player member : playersInRange)
					{
						processPlayerProgress(member);
					}
					return;
				}
			}
			else
			{
				// 파티나 Command Channel 내의 플레이어가 없을 경우 현재 공격한 플레이어만 처리
				processPlayerProgress(attacker);
			}
		}
	}
	
	private void processPlayerProgress(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
		if (entry.getStatus() == DailyMissionStatus.진행중)
		{
			if (entry.increaseProgress() >= _amount)
			{
				entry.setStatus(DailyMissionStatus.수령가능);
				missionComplete(player, _name);
			}
			storePlayerEntry(entry);
		}
	}
}
