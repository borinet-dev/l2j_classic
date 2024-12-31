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

import java.util.ArrayList;
import java.util.List;

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
		"대장님 저놈은 제가 확실하게 보내버릴께요!!",
		"제낍니까?",
		"너 따위가 우리 대장님께 도전하다니 웃기는구나!",
		"너는 이미 죽은 목숨이다!",
		"대장님이 널 용서하지 않으실 것이다!",
		"이 놈을 처리하는 건 나의 임무다!",
		"대장님을 위해 싸우다 죽는 것도 영광이다!",
		"넌 이제 끝났다, 이 건방진 놈아!",
		"대장님, 저놈은 제가 처리하겠습니다!",
		"이런 도발은 우리 대장님께 통하지 않는다!",
		"지금 대장님께 용서를 구해도 늦었다!",
		"너 따위가 감히 여길 넘보다니!",
		"대장님의 노여움이 두렵지 않단 말인가?",
		"우리 대장님은 너 같은 놈을 상대할 필요도 없다!",
		"내 칼끝에서 죽음을 맞이하라!"
	};
	
	private static final String[] BOSS_TEXT =
	{
		"이런 건방진 %s놈아! 나와라 나의 부하들아!",
		"네놈의 오만함이 끝을 보는구나!",
		"내가 직접 상대해 주마, %s!",
		"너 따위가 내 앞에 설 자격이 있는 줄 아느냐?",
		"어리석은 %s, 네 놈의 운명은 여기까지다!",
		"%s, 나의 분노를 감당해 보아라!",
		"내 부하들로도 충분할 것이다, %s!",
		"네놈의 목숨이 이제 내 손안에 있구나!",
		"%s, 내가 직접 끝을 보아주마!",
		"용감한 척 하는 게 가소롭구나, %s!",
		"이 자리에서 널 쓰러뜨리겠다!",
		"감히 내 앞을 막다니, 무모하구나!",
		"너는 내 발 아래 엎드릴 운명이다!",
		"내 힘을 직접 경험해 보아라!",
		"%s, 지금이라도 도망칠 생각은 없느냐?",
		"한심한 놈아, 내가 널 상대해 주겠다!",
		"네놈의 용기는 칭찬해 주마, 그러나 끝은 뻔하다!",
		"내가 상대해야 할 정도로 강하다고 생각하느냐?",
		"이 자리에서 네놈의 오만함을 끝내주겠다!",
		"%s, 이 전투는 나의 승리로 끝날 것이다!"
	};
	
	private Nos()
	{
		addAggroRangeEnterId(CROKIAN);
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		if (Rnd.chance(MONSTER_CHANCE_SPAWN))
		{
			if (Rnd.chance(20))
			{
				spawnMonstersAndBroadcast(npc, player, 3, new int[]
				{
					80,
					60,
					40
				});
			}
			else if (Rnd.chance(40))
			{
				spawnMonstersAndBroadcast(npc, player, 2, new int[]
				{
					60,
					40
				});
			}
			else if (Rnd.chance(60))
			{
				spawnMonstersAndBroadcast(npc, player, 1, new int[]
				{
					40
				});
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	private void spawnMonstersAndBroadcast(Npc npc, Player player, int monsterCount, int[] broadcastChances)
	{
		if (npc.getTarget() != null)
		{
			String bossText = String.format(BOSS_TEXT[Rnd.get(0, BOSS_TEXT.length - 1)], player.getName());
			npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, bossText));
		}
		
		List<Npc> spawnedMonsters = new ArrayList<>();
		for (int i = 0; i < monsterCount; i++)
		{
			spawnedMonsters.add(addSpawn(NOS, npc, false, MONSTER_DESPAWN_DELAY));
		}
		
		for (int i = 0; i < broadcastChances.length; i++)
		{
			if (Rnd.chance(broadcastChances[i]))
			{
				for (int j = 0; (j <= i) && (j < spawnedMonsters.size()); j++)
				{
					spawnedMonsters.get(j).broadcastPacket(new NpcSay(spawnedMonsters.get(j), ChatType.NPC_GENERAL, MINION_TEXT[Rnd.get(0, MINION_TEXT.length - 1)]));
				}
				break;
			}
		}
		
		for (Npc monster : spawnedMonsters)
		{
			addAttackPlayerDesire(monster, player);
		}
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
