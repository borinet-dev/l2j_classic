package ai.areas.ElvenForest;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

public class ElvenForest extends AbstractNpcAI
{
	// Gustav
	private static final int GUSTAV = 19327;
	private static final int RESURRECTED_GUSTAV = 19328;
	
	// Mikhail
	private static final int MIKHAIL = 19329;
	private static final int RESURRECTED_MIKHAIL = 19330;
	
	// Dietrich
	private static final int DIETRICH = 19331;
	private static final int RESURRECTED_DIETRICH = 19332;
	
	// Giselle von Hellmann
	private static final int GISELLE_VON_HELLMANN = 19343;
	private static final int RESURRECTED_GISELLE_VON_HELLMANN = 19344;
	
	// Berserker Dark Dragoness
	private static final int BERSERKER = 22546;
	private static final int DARK_DRAGONESS = 22545;
	
	private ElvenForest()
	{
		addKillId(GUSTAV, MIKHAIL, DIETRICH, GISELLE_VON_HELLMANN, BERSERKER);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Npc spawnMonster;
		final Playable attacker;
		
		if (Rnd.chance(65))
		{
			switch (npc.getId())
			{
				case GUSTAV:
					spawnMonster = addSpawn(RESURRECTED_GUSTAV, npc, false, 300000);
					break;
				case MIKHAIL:
					spawnMonster = addSpawn(RESURRECTED_MIKHAIL, npc, false, 300000);
					break;
				case DIETRICH:
					spawnMonster = addSpawn(RESURRECTED_DIETRICH, npc, false, 300000);
					break;
				case GISELLE_VON_HELLMANN:
					spawnMonster = addSpawn(RESURRECTED_GISELLE_VON_HELLMANN, npc, false, 300000);
					break;
				case BERSERKER:
					spawnMonster = addSpawn(DARK_DRAGONESS, npc, false, 300000);
					break;
				default:
					spawnMonster = null;
					break;
			}
			attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
			addAttackPlayerDesire(spawnMonster, attacker);
			npc.deleteMe();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new ElvenForest();
	}
}
