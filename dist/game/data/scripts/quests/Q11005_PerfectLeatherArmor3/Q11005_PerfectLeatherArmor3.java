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
package quests.Q11005_PerfectLeatherArmor3;

import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

import quests.Q11004_PerfectLeatherArmor2.Q11004_PerfectLeatherArmor2;

/**
 * Perfect Leather Armor (3/3) (11005)
 * @author Stayway
 */
public class Q11005_PerfectLeatherArmor3 extends Quest
{
	// NPCs
	private static final int LECTOR = 30001;
	// Items
	private static final int COBWEB = 90215;
	private static final int ESSENCE_OF_WATER = 90216;
	private static final int LECTORS_NOTES = 90214;
	// Monsters
	private static final int GIANT_SPIDER = 20103;
	private static final int GIANT_FANG_SPIDER = 20106;
	private static final int GIANT_BLADE_SPIDER = 20108;
	private static final int UNDINE = 20110;
	private static final int UNDINE_ELDER = 20113;
	private static final int UNDINE_NOBLE = 20115;
	// Misc
	private static final int MIN_LEVEL = 11;
	
	public Q11005_PerfectLeatherArmor3()
	{
		super(11005);
		addStartNpc(LECTOR);
		addTalkId(LECTOR);
		addKillId(GIANT_SPIDER, GIANT_FANG_SPIDER, GIANT_BLADE_SPIDER, UNDINE, UNDINE_ELDER, UNDINE_NOBLE);
		addCondMinLevel(MIN_LEVEL, "no-level.html"); // Custom
		addCondCompletedQuest(Q11004_PerfectLeatherArmor2.class.getSimpleName(), "notyet.htm");
		registerQuestItems(LECTORS_NOTES, COBWEB, ESSENCE_OF_WATER);
		setQuestNameNpcStringId(NpcStringId.LV_15_PERFECT_LEATHER_ARMOR);
	}
	
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
			case "30001-02.htm":
			{
				qs.startQuest();
				qs.setCond(2, true);
				showOnScreenMsg(player, NpcStringId.LECTOR_WANTS_YOU_TO_BRING_HIM_MATERIALS_FOR_NEW_ARMOR_N_GO_HUNTING_AND_KILL_GIANT_SPIDERS, ExShowScreenMessage.TOP_CENTER, 10000);
				giveItems(player, LECTORS_NOTES, 1);
				htmltext = event;
				break;
			}
		}
		if (event.equals("abort.html"))
		{
			htmltext = event;
		}
		else if (event.startsWith("reward"))
		{
			if (qs.isCond(4))
			{
				takeItems(player, LECTORS_NOTES, 1);
				takeItems(player, COBWEB, 7);
				takeItems(player, ESSENCE_OF_WATER, 5);
				
				StringTokenizer tokenizer = new StringTokenizer(event, " ");
				tokenizer.nextToken();
				
				final int itemId = Integer.parseInt(tokenizer.nextToken());
				final Item createditem = ItemTemplate.createItem(itemId);
				final Item soulShot = player.isMageClass() ? ItemTemplate.createItem(3948) : ItemTemplate.createItem(1463);
				soulShot.setCount(1000);
				
				while (tokenizer.hasMoreTokens())
				{
					player.addItem("11005_퀘스트_보상", createditem, null, true);
					final InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(createditem);
					player.sendInventoryUpdate(playerIU);
				}
				if (player.getLevel() < 20)
				{
					final int level = 20;
					final long pXp = player.getExp();
					final long tXp = ExperienceData.getInstance().getExpForLevel(level);
					if (pXp > tXp)
					{
						player.getStat().setLevel((byte) level);
						player.removeExpAndSp(pXp - tXp, 0);
					}
					else if (pXp < tXp)
					{
						player.addExpAndSp(tXp - pXp, 0);
					}
					player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
					player.setCurrentCp(player.getMaxCp());
					player.broadcastUserInfo();
				}
				else
				{
					player.addItem("11005_퀘스트_보상", soulShot, null, true);
					player.addItem("11005_퀘스트_보상", 955, 5, null, true);
					addExpAndSp(player, 70000, 3600);
				}
				htmltext = "30001-04.html";
				qs.exitQuest(false, true);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final QuestState qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = "30001-01.html";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(2))
				{
					htmltext = "30001-02a.html";
				}
				else if (qs.isCond(4))
				{
					htmltext = "30001-03.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(talker);
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
				handleKill(qs, killer, npc.getId());
			}
			else
			{
				// 파티원이 처치한 경우 모든 파티원에게 보상을 분배합니다.
				for (Player member : party.getMembers())
				{
					final QuestState memberQs = getQuestState(member, false);
					if ((member != null) && (memberQs != null))
					{
						handleKill(memberQs, member, npc.getId());
					}
				}
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	private void handleKill(QuestState qs, Player killer, int npcId)
	{
		switch (npcId)
		{
			case GIANT_SPIDER:
			case GIANT_BLADE_SPIDER:
			case GIANT_FANG_SPIDER:
				handleSpiderKill(qs, killer);
				break;
			
			case UNDINE:
			case UNDINE_ELDER:
			case UNDINE_NOBLE:
				handleUndineKill(qs, killer);
				break;
		}
	}
	
	private void handleSpiderKill(QuestState qs, Player killer)
	{
		if (qs.isCond(2) && (getRandom(100) < 87))
		{
			if (getQuestItemsCount(killer, COBWEB) < 7)
			{
				giveItems(killer, COBWEB, 1);
				if (getQuestItemsCount(killer, COBWEB) >= 7)
				{
					playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_GIANT_SPIDERS_N_GO_HUNTING_AND_KILL_UNDINES, ExShowScreenMessage.TOP_CENTER, 10000);
					qs.setCond(3);
					killer.sendPacket(new TutorialShowHtml(getHtm(killer, "popup-1.htm")));
					playSound(killer, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
				}
				else
				{
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
	}
	
	private void handleUndineKill(QuestState qs, Player killer)
	{
		if (qs.isCond(3) && (getRandom(100) < 100))
		{
			if (getQuestItemsCount(killer, ESSENCE_OF_WATER) < 5)
			{
				giveItems(killer, ESSENCE_OF_WATER, 1);
				if (getQuestItemsCount(killer, ESSENCE_OF_WATER) >= 5)
				{
					playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					showOnScreenMsg(killer, NpcStringId.YOU_HAVE_ALL_OF_THE_ITEMS_LECTOR_REQUESTED_RETURN_TO_HIM, ExShowScreenMessage.TOP_CENTER, 10000);
					qs.setCond(4);
					killer.sendPacket(new TutorialShowHtml(getHtm(killer, "popup-2.htm")));
					playSound(killer, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
				}
				else
				{
					playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final Player player = event.getPlayer();
		final QuestState qs = player.getQuestState(this.getClass().getSimpleName());
		final QuestState bqs = player.getQuestState("Q11004_PerfectLeatherArmor2");
		
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() < 20) && (bqs != null) && bqs.isCompleted() && (qs == null) && canStartQuest(player))
		{
			final String html = getHtm(player, "popup.htm");
			player.sendPacket(new TutorialShowHtml(html));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
		}
	}
}
