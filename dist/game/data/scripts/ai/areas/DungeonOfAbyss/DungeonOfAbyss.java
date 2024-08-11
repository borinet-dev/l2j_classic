package ai.areas.DungeonOfAbyss;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

public class DungeonOfAbyss extends AbstractNpcAI
{
	// NPC
	private static final int 이계의_간수 = 21642;
	private static final int 심연의_간수 = 21648;
	private static final int[] MOB1 =
	{
		21638,
		21639,
		21640,
		21641
	};
	private static final int[] MOB2 =
	{
		21644,
		21645,
		21646,
		21647
	};
	
	public DungeonOfAbyss()
	{
		addKillId(MOB1);
		addKillId(MOB2);
		addKillId(이계의_간수, 심연의_간수);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		int npcId = npc.getId();
		
		if (contains(MOB1, npcId))
		{
			if (Rnd.chance(8))
			{
				addSpawn(이계의_간수, npc, false, 180000);
			}
		}
		if (contains(MOB2, npcId))
		{
			if (Rnd.chance(8))
			{
				addSpawn(심연의_간수, npc, false, 180000);
			}
		}
		if ((npcId == 이계의_간수))
		{
			if (Rnd.chance(35))
			{
				npc.dropItem(killer, 90010, 1);
			}
		}
		if ((npcId == 심연의_간수))
		{
			if (Rnd.chance(35))
			{
				npc.dropItem(killer, 90011, 1);
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	private boolean contains(int[] array, int value)
	{
		for (int element : array)
		{
			if (element == value)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new DungeonOfAbyss();
	}
}
