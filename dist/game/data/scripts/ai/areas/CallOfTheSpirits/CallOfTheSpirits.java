/*
 * Copyright © 2019-2021 L2JOrg
 *
 * This file is part of the L2JOrg project.
 *
 * L2JOrg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2JOrg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.areas.CallOfTheSpirits;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetTask;

import ai.AbstractNpcAI;

public class CallOfTheSpirits extends AbstractNpcAI
{
	// Wasteland
	private static final int MONSTEREYE = 20068;
	private static final int GRANITEGOLEM = 20083;
	private static final int GUARDIANGOLEM = 21656;
	
	// Execution ground
	private static final int GOUL = 20201;
	private static final int CORPSETRACKER = 20202;
	private static final int GUARDIANDRECO = 21654;
	
	// Plain of the lizardman
	private static final int LETOWARRIOR = 20580;
	private static final int LETOSHAMAN = 20581;
	private static final int GUARDIANRAIDO = 21655;
	
	// Sea of spores
	private static final int GIANTMONSTEREYE = 20556;
	private static final int DIREWYRM = 20557;
	private static final int GUARDIANWYRM = 21657;
	
	// Forest of mirrors
	private static final int LIZARDWARRIOR = 20643;
	private static final int LIZARDMATRIACH = 20645;
	private static final int GUARDIANHARIT = 21658;
	
	// Seal of shilen
	private static final int CRIMSONDRAKE = 20670;
	private static final int PALIBATI = 20673;
	private static final int GUARDIANPALIBATI = 21660;
	
	private CallOfTheSpirits()
	{
		addKillId(MONSTEREYE, GRANITEGOLEM, GOUL, CORPSETRACKER, LETOWARRIOR, LETOSHAMAN, GIANTMONSTEREYE, DIREWYRM, LIZARDWARRIOR, LIZARDMATRIACH, CRIMSONDRAKE, PALIBATI);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Npc spawnMonster;
		final Playable attacker;
		
		if ((npc.getId() == LIZARDWARRIOR) || (npc.getId() == LIZARDMATRIACH))
		{
			if (Rnd.get(100) <= 50)
			{
				spawnMonster = addSpawn(GUARDIANHARIT, npc, false, 300000);
				attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
				addAttackPlayerDesire(spawnMonster, attacker);
				npc.deleteMe();
			}
		}
		else if ((npc.getId() == CRIMSONDRAKE) || (npc.getId() == PALIBATI))
		{
			if (Rnd.get(100) <= 50)
			{
				spawnMonster = addSpawn(GUARDIANPALIBATI, npc, false, 300000);
				attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
				addAttackPlayerDesire(spawnMonster, attacker);
				npc.deleteMe();
			}
		}
		else
		{
			if (Rnd.get(100) <= 20)
			{
				switch (npc.getId())
				{
					case MONSTEREYE:
					case GRANITEGOLEM:
						spawnMonster = addSpawn(GUARDIANGOLEM, npc, false, 300000);
						break;
					case GOUL:
					case CORPSETRACKER:
						spawnMonster = addSpawn(GUARDIANDRECO, npc, false, 300000);
						break;
					case LETOWARRIOR:
					case LETOSHAMAN:
						spawnMonster = addSpawn(GUARDIANRAIDO, npc, false, 300000);
						break;
					case GIANTMONSTEREYE:
					case DIREWYRM:
						spawnMonster = addSpawn(GUARDIANWYRM, npc, false, 300000);
						break;
					default:
						spawnMonster = null;
						break;
				}
				attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
				addAttackPlayerDesire(spawnMonster, attacker);
				npc.deleteMe();
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		if (BorinetTask._isActive)
		{
			return;
		}
		new CallOfTheSpirits();
	}
}
