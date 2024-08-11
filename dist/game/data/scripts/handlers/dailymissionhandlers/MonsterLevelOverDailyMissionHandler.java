package handlers.dailymissionhandlers;

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
 * MonsterPartyDailyMissionHandler class for daily mission.
 */
public class MonsterLevelOverDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;
	private final int _minLevel;
	private final String _name;
	private final int _classRace; // 클래스 종류를 나타내는 변수 추가
	
	/**
	 * Instantiates a new MonsterPartyDailyMissionHandler.
	 * @param holder the holder
	 */
	public MonsterLevelOverDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
		_minLevel = holder.getParams().getInt("minLevel", 0);
		_name = holder.getName();
		_classRace = holder.getParams().getInt("classRace", 0); // 클래스 종류 설정
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
				case 진행중:
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
		final Player player = event.getAttacker();
		
		if ((player == null) || (monster.getLevel() < player.getLevel()))
		{
			return;
		}
		
		if (((_classRace == 1) && !player.isMage()) || ((_classRace == 2) && player.isMage()))
		{
			return;
		}
		
		final Party party = player.getParty();
		if (party == null)
		{
			processPlayerProgress(player, monster.getLevel());
		}
		else if (player.isInParty())
		{
			final boolean isInCC = party.isInCommandChannel();
			final List<Player> members = isInCC ? party.getCommandChannel().getMembers() : party.getMembers();
			
			for (Player member : members)
			{
				if (member.calculateDistance3D(monster) <= Config.ALT_PARTY_RANGE)
				{
					processPlayerProgress(member, monster.getLevel());
				}
			}
		}
	}
	
	private void processPlayerProgress(Player player, int monsterLevel)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
		if ((player.getLevel() < _minLevel) || (monsterLevel < player.getLevel()))
		{
			return;
		}
		if (((_classRace == 1) && !player.isMage()) || ((_classRace == 2) && player.isMage()))
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
