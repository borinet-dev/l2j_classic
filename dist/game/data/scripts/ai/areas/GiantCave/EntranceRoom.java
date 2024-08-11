package ai.areas.GiantCave;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

public class EntranceRoom extends AbstractNpcAI
{
	private final int[] MONSTER_NPC_IDS =
	{
		20646,
		20647,
		20648,
		20649,
		20650
	};
	
	private EntranceRoom()
	{
		addKillId(MONSTER_NPC_IDS);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Npc spawnMonster;
		if (killer.isInParty() && (killer.getParty().getMemberCount() >= 5))
		{
			if (Rnd.chance(30))
			{
				spawnMonster = addSpawn(24023, npc, false, 300000);
				addAttackPlayerDesire(spawnMonster, killer);
			}
		}
		else
		{
			if (Rnd.chance(10))
			{
				spawnMonster = addSpawn(24017, npc, false, 300000);
				addAttackPlayerDesire(spawnMonster, killer);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new EntranceRoom();
	}
}
