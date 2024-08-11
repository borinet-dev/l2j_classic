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
package ai.areas.AligatorIsland;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;
import org.l2jmobius.gameserver.util.BorinetTask;

import ai.AbstractNpcAI;

public class Nos extends AbstractNpcAI
{
	
	private final int MONSTER_CHANCE_SPAWN = 100;
	private final int NOS = 20793;
	private final int CROKIAN = 20804;
	private final int MONSTER_DESPAWN_DELAY = 300000;
	
	private static final String[] MINION_TEXT =
	{
		"감히 네놈이 대장님을 화나게 한거냐?",
		"네놈이 감히 대장님을 하찮게 보는게냐!!",
		"대장님 저놈 입니까!!!??",
		"병풍뒤에서 향냄새 맡을 준비는 되었는가??",
		"오늘이 네놈 제삿날로 만들어주마!",
		"대장님 저놈은 제가 확실하게 보내버릴께요!!"
	};
	
	private Nos()
	{
		addAggroRangeEnterId(CROKIAN);
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		final Npc spawnMonster1;
		final Npc spawnMonster2;
		final Npc spawnMonster3;
		if (Rnd.chance(MONSTER_CHANCE_SPAWN))
		{
			if (Rnd.chance(20))
			{
				if (npc.getTarget() != null)
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, "이런 건방진 " + player.getName() + "놈아! 나와라 나의 부하들아!"));
				}
				spawnMonster1 = addSpawn(NOS, npc, false, MONSTER_DESPAWN_DELAY);
				spawnMonster2 = addSpawn(NOS, npc, false, MONSTER_DESPAWN_DELAY);
				spawnMonster3 = addSpawn(NOS, npc, false, MONSTER_DESPAWN_DELAY);
				if (Rnd.chance(80))
				{
					spawnMonster1.broadcastPacket(new NpcSay(spawnMonster1, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
				}
				else if (Rnd.chance(60))
				{
					spawnMonster1.broadcastPacket(new NpcSay(spawnMonster1, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
					spawnMonster2.broadcastPacket(new NpcSay(spawnMonster2, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
				}
				else if (Rnd.chance(40))
				{
					spawnMonster1.broadcastPacket(new NpcSay(spawnMonster1, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
					spawnMonster2.broadcastPacket(new NpcSay(spawnMonster2, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
					spawnMonster3.broadcastPacket(new NpcSay(spawnMonster3, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
				}
				addAttackPlayerDesire(spawnMonster1, player);
				addAttackPlayerDesire(spawnMonster2, player);
				addAttackPlayerDesire(spawnMonster3, player);
			}
			else if (Rnd.chance(40))
			{
				if (npc.getTarget() != null)
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, "이런 건방진 " + player.getName() + "놈아! 나와라 나의 부하들아!"));
				}
				spawnMonster1 = addSpawn(NOS, npc, false, MONSTER_DESPAWN_DELAY);
				spawnMonster2 = addSpawn(NOS, npc, false, MONSTER_DESPAWN_DELAY);
				if (Rnd.chance(60))
				{
					spawnMonster1.broadcastPacket(new NpcSay(spawnMonster1, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
				}
				else if (Rnd.chance(40))
				{
					spawnMonster1.broadcastPacket(new NpcSay(spawnMonster1, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
					spawnMonster2.broadcastPacket(new NpcSay(spawnMonster2, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
				}
				addAttackPlayerDesire(spawnMonster1, player);
				addAttackPlayerDesire(spawnMonster2, player);
			}
			else if (Rnd.chance(60))
			{
				if (npc.getTarget() != null)
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, "이런 건방진 " + player.getName() + "놈아! 나와라 나의 부하야!"));
				}
				spawnMonster1 = addSpawn(NOS, npc, false, MONSTER_DESPAWN_DELAY);
				if (Rnd.chance(40))
				{
					spawnMonster1.broadcastPacket(new NpcSay(spawnMonster1, ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(MINION_TEXT.length)]));
				}
				addAttackPlayerDesire(spawnMonster1, player);
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		if (BorinetTask._isActive)
		{
			return;
		}
		new Nos();
	}
}
