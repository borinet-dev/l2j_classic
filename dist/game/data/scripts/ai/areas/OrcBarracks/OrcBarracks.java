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
package ai.areas.OrcBarracks;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

public class OrcBarracks extends AbstractNpcAI
{
	// NPC
	private static final int 체르투바의_환영 = 23421;
	private static final int 체르투바의_환상 = 23422;
	private static final int[] MOBS =
	{
		20495, // 투렉 오크 군장
		20496, // 투렉 오크 궁병
		20497, // 투렉 오크 돌격병
		20498, // 투렉 오크 보급병
		20500, // 투렉 오크 보초병
		20501, // 투렉 오크 제사장
		20546, // 투렉 오크 장로
	};
	
	public OrcBarracks()
	{
		addKillId(MOBS);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (Rnd.chance(8))
		{
			final int npcId = (killer.isMageClass()) ? 체르투바의_환상 : 체르투바의_환영;
			showOnScreenMsg(killer, NpcStringId.A_POWERFUL_MONSTER_HAS_COME_TO_FACE_YOU, ExShowScreenMessage.TOP_CENTER, 5000);
			addSpawn(npcId, npc, false, 180000);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new OrcBarracks();
	}
}
