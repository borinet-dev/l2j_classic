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
package quests.Q11002_HelpWithTempleRestoration;

import java.util.List;
import java.util.StringTokenizer;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

import quests.Q11001_TombsOfAncestors.Q11001_TombsOfAncestors;

/**
 * Help with Temple Restoration (11002)
 * @author Stayway
 */
public class Q11002_HelpWithTempleRestoration extends Quest
{
	// NPCs
	private static final int ALTRAN = 30283;
	private static final int HARRYS = 30035;
	// Items
	private static final int WOODEN_POLE = 90205;
	private static final int WOODEN_DOOR_PANEL = 90206;
	private static final int STONE_POWDER = 90207;
	private static final int INVENTORY_BOOK = 90204;
	// Monsters
	private static final int ORC_LIEUTENANT = 20096;
	private static final int ORC_CAPTAIN = 20098;
	private static final int WEREWOLF_CHIEFTAIN = 20342;
	private static final int WEREWOLF_HUMTER = 20343;
	private static final int STONE_GOLEM = 20016;
	private static final int CRASHER = 20101;
	// Misc
	private static final int MIN_LEVEL = 5;
	private static final int MAX_LEVEL = 20;
	
	public Q11002_HelpWithTempleRestoration()
	{
		super(11002);
		addStartNpc(ALTRAN);
		addTalkId(HARRYS, ALTRAN);
		addKillId(ORC_CAPTAIN, ORC_LIEUTENANT, WEREWOLF_HUMTER, WEREWOLF_CHIEFTAIN, STONE_GOLEM, CRASHER);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no-level.html"); // Custom
		addCondCompletedQuest(Q11001_TombsOfAncestors.class.getSimpleName(), "notyet.htm");
		registerQuestItems(INVENTORY_BOOK, WOODEN_POLE, WOODEN_DOOR_PANEL, STONE_POWDER);
		setQuestNameNpcStringId(NpcStringId.LV_11_20_HELP_WITH_TEMPLE_RESTORATION);
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
			case "30283-02.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
		}
		if (event.startsWith("reward"))
		{
			if (qs.isCond(5))
			{
				takeItems(player, INVENTORY_BOOK, 1);
				takeItems(player, WOODEN_POLE, 10);
				takeItems(player, WOODEN_DOOR_PANEL, 10);
				takeItems(player, STONE_POWDER, 10);
				
				StringTokenizer tokenizer = new StringTokenizer(event, " ");
				tokenizer.nextToken();
				
				while (tokenizer.hasMoreTokens())
				{
					final int itemId = Integer.parseInt(tokenizer.nextToken());
					final Item createditem = ItemTemplate.createItem(itemId);
					
					player.addItem("11002_퀘스트_보상", createditem, null, true);
					player.getInventory().equipItem(createditem);
					final InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(createditem);
					player.sendInventoryUpdate(playerIU);
				}
				addExpAndSp(player, 80000, 0);
				htmltext = "30035-03.html";
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
				if (npc.getId() == ALTRAN)
				{
					htmltext = "30283-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == ALTRAN)
				{
					if (qs.isCond(1))
					{
						htmltext = "30283-02a.html";
					}
					break;
				}
				else if (npc.getId() == HARRYS)
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "30035-01.htm";
							qs.setCond(2, true);
							showOnScreenMsg(talker, NpcStringId.YOU_HAVE_TALKED_TO_HARRYS_NGO_HUNTING_AND_KILL_ORC_LIEUTENANTS_AND_ORC_CAPTAINS, ExShowScreenMessage.TOP_CENTER, 10000);
							giveItems(talker, INVENTORY_BOOK, 1);
							playSound(talker, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
							break;
						}
						case 2:
						{
							htmltext = "30035-01a.html";
							break;
						}
						case 5:
						{
							htmltext = "30035-02.html";
							break;
						}
					}
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
		if (qs != null)
		{
			switch (npc.getId())
			{
				case ORC_CAPTAIN:
				case ORC_LIEUTENANT:
				{
					if (killer.isInParty())
					{
						final Party party = killer.getParty();
						final List<Player> members = party.getMembers();
						
						for (Player member : members)
						{
							final QuestState qsm = getQuestState(member, false);
							if ((member != null) && (qsm != null))
							{
								if (qsm.isCond(2) && (getRandom(100) < 84))
								{
									if (getQuestItemsCount(member, WOODEN_POLE) < 10)
									{
										giveItems(member, WOODEN_POLE, 1);
										if (getQuestItemsCount(member, WOODEN_POLE) >= 10)
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_MIDDLE);
											showOnScreenMsg(member, NpcStringId.YOU_HAVE_KILLED_ENOUGH_ORC_LIEUTENANTS_AND_ORC_CAPTAINS_N_GO_HUNTING_AND_KILL_WEREWOLF_HUNTERS_AND_WEREWOLF_CHIEFTAINS, ExShowScreenMessage.TOP_CENTER, 10000);
											qsm.setCond(3);
											member.sendPacket(new TutorialShowHtml(getHtm(member, "popup-1.htm")));
											playSound(member, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
										}
										else
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_ITEMGET);
										}
									}
								}
							}
						}
					}
					else
					{
						if (qs.isCond(2) && (getQuestItemsCount(killer, WOODEN_POLE) < 10) && (getRandom(100) < 84))
						{
							giveItems(killer, WOODEN_POLE, 1);
							if (getQuestItemsCount(killer, WOODEN_POLE) >= 10)
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
								showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_ORC_LIEUTENANTS_AND_ORC_CAPTAINS_N_GO_HUNTING_AND_KILL_WEREWOLF_HUNTERS_AND_WEREWOLF_CHIEFTAINS, ExShowScreenMessage.TOP_CENTER, 10000);
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
					break;
				}
				case WEREWOLF_HUMTER:
				case WEREWOLF_CHIEFTAIN:
				{
					if (killer.isInParty())
					{
						final Party party = killer.getParty();
						final List<Player> members = party.getMembers();
						
						for (Player member : members)
						{
							final QuestState qsm = getQuestState(member, false);
							if (qsm != null)
							{
								if (qsm.isCond(3) && (getRandom(100) < 87))
								{
									if (getQuestItemsCount(member, WOODEN_DOOR_PANEL) < 10)
									{
										giveItems(member, WOODEN_DOOR_PANEL, 1);
										if (getQuestItemsCount(member, WOODEN_DOOR_PANEL) >= 10)
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_MIDDLE);
											showOnScreenMsg(member, NpcStringId.YOU_HAVE_KILLED_ENOUGH_WEREWOLF_HUNTERS_AND_WEREWOLF_CHIEFTAINS_N_GO_HUNTING_AND_KILL_STONE_GOLEMS_AND_CRASHERS, ExShowScreenMessage.TOP_CENTER, 10000);
											qsm.setCond(4);
											member.sendPacket(new TutorialShowHtml(getHtm(member, "popup-2.htm")));
											playSound(member, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
										}
										else
										{
											playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
										}
									}
								}
							}
						}
					}
					else
					{
						if (qs.isCond(3) && (getQuestItemsCount(killer, WOODEN_DOOR_PANEL) < 10) && (getRandom(100) < 87))
						{
							giveItems(killer, WOODEN_DOOR_PANEL, 1);
							if (getQuestItemsCount(killer, WOODEN_DOOR_PANEL) >= 10)
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
								showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_WEREWOLF_HUNTERS_AND_WEREWOLF_CHIEFTAINS_N_GO_HUNTING_AND_KILL_STONE_GOLEMS_AND_CRASHERS, ExShowScreenMessage.TOP_CENTER, 10000);
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
					break;
				}
				case CRASHER:
				case STONE_GOLEM:
				{
					if (killer.isInParty())
					{
						final Party party = killer.getParty();
						final List<Player> members = party.getMembers();
						
						for (Player member : members)
						{
							final QuestState qsm = getQuestState(member, false);
							if (qsm != null)
							{
								if (qsm.isCond(4) && (getRandom(100) < 84))
								{
									if (getQuestItemsCount(member, STONE_POWDER) < 10)
									{
										giveItems(member, STONE_POWDER, 1);
										if (getQuestItemsCount(member, STONE_POWDER) >= 10)
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_MIDDLE);
											showOnScreenMsg(member, NpcStringId.YOU_HAVE_ALL_OF_THE_ITEMS_HARRYS_REQUESTED_RETURN_TO_HIM, ExShowScreenMessage.TOP_CENTER, 10000);
											qsm.setCond(5);
											member.sendPacket(new TutorialShowHtml(getHtm(member, "popup-3.htm")));
											playSound(member, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
										}
										else
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_ITEMGET);
										}
									}
								}
							}
						}
					}
					else
					{
						if (qs.isCond(4) && (getQuestItemsCount(killer, STONE_POWDER) < 10) && (getRandom(100) < 84))
						{
							giveItems(killer, STONE_POWDER, 1);
							if (getQuestItemsCount(killer, STONE_POWDER) >= 10)
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
								showOnScreenMsg(killer, NpcStringId.YOU_HAVE_ALL_OF_THE_ITEMS_HARRYS_REQUESTED_RETURN_TO_HIM, ExShowScreenMessage.TOP_CENTER, 10000);
								qs.setCond(5);
								killer.sendPacket(new TutorialShowHtml(getHtm(killer, "popup-3.htm")));
								playSound(killer, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
							}
							else
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
