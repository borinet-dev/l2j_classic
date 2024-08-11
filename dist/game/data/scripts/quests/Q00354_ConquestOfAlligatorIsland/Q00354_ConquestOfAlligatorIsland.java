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
package quests.Q00354_ConquestOfAlligatorIsland;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;

/**
 * Conquest of Alligator Island (354)
 * @author Adry_85
 */
public class Q00354_ConquestOfAlligatorIsland extends Quest
{
	// NPC
	private static final int KLUCK = 30895;
	// Items
	private static final int ALLIGATOR_TOOTH = 5863;
	// Misc
	private static final int MIN_LEVEL = 35;
	
	public final int CHANCE = 65;
	
	public final int[] MOBLIST =
	{
		20804, // 크로키언 라드
		20805, // 다일라온 라드
		20806, // 크로키언 라드 전사
		20807, // 파르히트 라드
		20808, // 노스 라드
		20793, // 노스
		20991, // 늪지의 수장
		20992, // 늪지의 악어
		20993 // 늪지의 전사
	};
	
	public Q00354_ConquestOfAlligatorIsland()
	{
		super(354);
		addStartNpc(KLUCK);
		addTalkId(KLUCK);
		for (int i : MOBLIST)
		{
			addKillId(i);
		}
		registerQuestItems(ALLIGATOR_TOOTH);
		addCondMaxLevel(49, getNoQuestMsg(null));
	}
	
	// @formatter:off
	private final int[][] RANDOM_REWARDS =
	{
		{13733, 10}, // 기란 축복받은 귀환 주문서
		{1539, 20}, // 강력 체력 회복제
		{728, 10}, // 강력 마나 치유약
		{1893, 5}, // 오리하루콘
		{1878, 25}, // 질긴 끈
		{1875, 10}, // 정화의 돌
		{1879, 10}, // 코크스
		{1880, 10}, // 강철
		{22228, 1}, // 파멸의 갑옷 강화 주문서-C그레이드
		{22227, 1}, // 파멸의 무기 강화 주문서-C그레이드
		{49790, 1} // +16 C그레이드 무기 상자
	};
	
	private final int[][] RANDOM_REWARDS_OVER =
	{
		{13733, 10}, // 기란 축복받은 귀환 주문서
		{1539, 40}, // 강력 체력 회복제
		{728, 30}, // 강력 마나 치유약
		{22228, 2}, // 파멸의 갑옷 강화 주문서-C그레이드
		{22227, 2}, // 파멸의 무기 강화 주문서-C그레이드
		{49790, 1} // +16 C그레이드 무기 상자
	};
	// @formatter:on
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "30895-04.html":
			case "30895-05.html":
			case "30895-09.html":
			{
				htmltext = event;
				break;
			}
			case "30895-02.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "ADENA":
			{
				final long count = getQuestItemsCount(player, ALLIGATOR_TOOTH);
				if (count > 0)
				{
					if (count > 199)
					{
						giveAdena(player, (count * 8000) * 2, true);
						takeItems(player, ALLIGATOR_TOOTH, -1);
						int random = Rnd.get(RANDOM_REWARDS_OVER.length);
						giveItems(player, RANDOM_REWARDS_OVER[random][0], RANDOM_REWARDS_OVER[random][1]);
						giveItems(player, RANDOM_REWARDS_OVER[random][0], RANDOM_REWARDS_OVER[random][1]);
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						htmltext = "30895-06.html";
					}
					else if (count > 99)
					{
						giveAdena(player, (count * 4000) * 2, true);
						takeItems(player, ALLIGATOR_TOOTH, -1);
						int random = Rnd.get(RANDOM_REWARDS.length);
						giveItems(player, RANDOM_REWARDS[random][0], RANDOM_REWARDS[random][1]);
						giveItems(player, RANDOM_REWARDS[random][0], RANDOM_REWARDS[random][1]);
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						htmltext = "30895-06.html";
					}
					else
					{
						giveAdena(player, (count * 2000) * 2, true);
						takeItems(player, ALLIGATOR_TOOTH, -1);
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						htmltext = "30895-07.html";
					}
				}
				else
				{
					htmltext = "30895-08.html";
				}
				break;
			}
			case "30895-10.html":
			{
				qs.exitQuest(true, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted())
		{
			final Party party = killer.getParty();
			if (party == null)
			{
				// 플레이어가 파티에 속하지 않은 경우 개별 보상을 처리합니다.
				handleKill(qs, killer);
			}
			else
			{
				// 파티원이 처치한 경우 모든 파티원에게 보상을 분배합니다.
				for (Player member : party.getMembers())
				{
					final QuestState memberQs = getQuestState(member, false);
					if ((member != null) && (memberQs != null))
					{
						handleKill(memberQs, member);
					}
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void handleKill(QuestState qs, Player player)
	{
		if (Rnd.chance(CHANCE))
		{
			giveItems(player, ALLIGATOR_TOOTH, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs == null)
		{
			return null;
		}
		
		if (qs.isCreated())
		{
			htmltext = ((player.getLevel() >= MIN_LEVEL) ? "30895-01.html" : "30895-03.html");
		}
		else if (qs.isStarted())
		{
			htmltext = "30895-04.html";
		}
		return htmltext;
	}
}
