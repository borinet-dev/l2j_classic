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
 * @author Mobius, 보리넷 가츠
 */
public class SpecialDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;
	private final int _minLevel;
	private final int _maxLevel;
	private final String _name;
	private final List<Integer> _ids = new ArrayList<>();
	private final DailyMissionDataHolder _holder;
	
	public SpecialDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
		_minLevel = holder.getParams().getInt("minLevel", 0);
		_maxLevel = holder.getParams().getInt("maxLevel", 0);
		_name = holder.getName();
		final String ids = holder.getParams().getString("ids", "");
		if (!ids.isEmpty())
		{
			for (String s : ids.split(","))
			{
				final int id = Integer.parseInt(s);
				if (!_ids.contains(id))
				{
					_ids.add(id);
				}
			}
		}
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
		if ((entry != null) && player.isMainClassActive())
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
		if (!_ids.isEmpty() && !_ids.contains(monster.getId()))
		{
			return;
		}
		
		final Player player = event.getAttacker();
		if (_holder.isMainClassOnly() && player.isSubClassActive())
		{
			// 메인 클래스만 수행 가능한 미션인데, 플레이어가 메인 클래스가 아닌 경우에는 아무것도 하지 않음
			return;
		}
		
		if (player.getParty() == null)
		{
			processPlayerProgress(player);
		}
		else if (player.isInParty())
		{
			final Party party = player.getParty();
			final boolean isInCC = party.isInCommandChannel();
			final List<Player> members = isInCC ? party.getCommandChannel().getMembers() : party.getMembers();
			
			for (Player member : members)
			{
				if (member.calculateDistance3D(monster) <= Config.ALT_PARTY_RANGE)
				{
					processPlayerProgress(member);
				}
			}
		}
	}
	
	private void processPlayerProgress(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
		if (player.isSubClassActive() || (player.getLevel() < _minLevel) || (player.getLevel() > _maxLevel))
		{
			return;
		}
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
