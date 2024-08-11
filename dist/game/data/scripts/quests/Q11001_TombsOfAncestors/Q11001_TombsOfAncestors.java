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
package quests.Q11001_TombsOfAncestors;

import java.util.List;
import java.util.StringTokenizer;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
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

/**
 * Tombs of Ancestors (11001)
 * @author Stayway
 */
public class Q11001_TombsOfAncestors extends Quest
{
	// NPCs
	private static final int NEWBIE_GUIDE = 30598;
	private static final int ALTRAN = 30283;
	// Items
	private static final int WOLF_PELT = 90200;
	private static final int ORC_AMULET = 90201;
	private static final int WEREWOLFS_FANG = 90202;
	private static final int BROKEN_SWORD = 90203;
	private static final int HUNTERS_MEMO = 90199;
	// Monsters
	private static final int WOLF = 20120;
	private static final int ELDER_WOLF = 20442;
	private static final int ORC = 20130;
	private static final int ORC_SOLDIER = 20131;
	private static final int ORC_ARCHER = 20006;
	private static final int ORC_WARRIOR = 20093;
	private static final int WEREWOLVES = 20132;
	// Misc
	private static final int MIN_LEVEL = 2;
	private static final int MAX_LEVEL = 20;
	
	public Q11001_TombsOfAncestors()
	{
		super(11001);
		addStartNpc(NEWBIE_GUIDE);
		addTalkId(NEWBIE_GUIDE, ALTRAN);
		addKillId(WOLF, ELDER_WOLF, ORC, ORC_SOLDIER, ORC_ARCHER, ORC_WARRIOR, WEREWOLVES);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no-level.htm");
		registerQuestItems(HUNTERS_MEMO, WOLF_PELT, ORC_AMULET, WEREWOLFS_FANG, BROKEN_SWORD);
		setQuestNameNpcStringId(NpcStringId.LV_2_20_TOMBS_OF_ANCESTORS);
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
			case "30598-02.htm":
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
				takeItems(player, HUNTERS_MEMO, 1);
				takeItems(player, WOLF_PELT, 10);
				takeItems(player, ORC_AMULET, 10);
				takeItems(player, WEREWOLFS_FANG, 10);
				
				StringTokenizer tokenizer = new StringTokenizer(event, " ");
				tokenizer.nextToken();
				
				while (tokenizer.hasMoreTokens())
				{
					final int itemId = Integer.parseInt(tokenizer.nextToken());
					final Item createditem = ItemTemplate.createItem(itemId);
					player.addItem("11001_퀘스트_보상", createditem, null, true);
					player.getInventory().equipItem(createditem);
					final InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(createditem);
					player.sendInventoryUpdate(playerIU);
				}
				addExpAndSp(player, 70000, 0);
				htmltext = "30283-03.htm";
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
				if (npc.getId() == NEWBIE_GUIDE)
				{
					htmltext = "30598-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (npc.getId() == NEWBIE_GUIDE)
				{
					if (qs.isCond(1))
					{
						htmltext = "30598-02a.htm";
					}
					break;
				}
				else if (npc.getId() == ALTRAN)
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "30283-01.htm";
							qs.setCond(2, true);
							showOnScreenMsg(talker, NpcStringId.NOW_YOU_KNOW_WHAT_ALTRAN_WANTS_NGO_HUNTING_AND_KILL_WOLVES, ExShowScreenMessage.TOP_CENTER, 10000);
							giveItems(talker, HUNTERS_MEMO, 1);
							break;
						}
						case 2:
						{
							htmltext = "30283-01a.htm";
							break;
						}
						case 5:
						{
							if (talker.isMageClass() && (talker.getOriginRace() != Race.ORC))
							{
								htmltext = "30283-mage.htm";
							}
							else if (talker.getOriginRace() == Race.ORC)
							{
								htmltext = "30283-orc.htm";
							}
							else if (talker.getOriginRace() == Race.DWARF)
							{
								htmltext = "30283-dwarf.htm";
							}
							else
							{
								htmltext = "30283-normal.htm";
							}
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
				case WOLF:
				case ELDER_WOLF:
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
								if (qsm.isCond(2) && (getRandom(100) < 93))
								{
									if (getQuestItemsCount(member, WOLF_PELT) < 10)
									{
										giveItems(member, WOLF_PELT, 1);
										if (getQuestItemsCount(member, WOLF_PELT) >= 10)
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_MIDDLE);
											showOnScreenMsg(member, NpcStringId.YOU_HAVE_KILLED_ENOUGH_WOLVES_N_GO_HUNTING_AND_KILL_ORCS, ExShowScreenMessage.TOP_CENTER, 10000);
											qsm.setCond(3);
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
						if (qs.isCond(2) && (getQuestItemsCount(killer, WOLF_PELT) < 10) && (getRandom(100) < 93))
						{
							giveItems(killer, WOLF_PELT, 1);
							if (getQuestItemsCount(killer, WOLF_PELT) >= 10)
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
								showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_WOLVES_N_GO_HUNTING_AND_KILL_ORCS, ExShowScreenMessage.TOP_CENTER, 10000);
								qs.setCond(3);
							}
							else
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
							}
						}
					}
					break;
				}
				case ORC:
				case ORC_SOLDIER:
				case ORC_ARCHER:
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
								if (qsm.isCond(3) && (getRandom(100) < 93))
								{
									if (getQuestItemsCount(member, ORC_AMULET) < 10)
									{
										giveItems(member, ORC_AMULET, 1);
										if (getQuestItemsCount(member, ORC_AMULET) >= 10)
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_MIDDLE);
											showOnScreenMsg(member, NpcStringId.YOU_HAVE_KILLED_ENOUGH_ORCS_NGO_HUNTING_AND_KILL_ORC_WARRIORS_AND_WEREWOLVES, ExShowScreenMessage.TOP_CENTER, 10000);
											qsm.setCond(4);
											member.sendPacket(new TutorialShowHtml(getHtm(member, "popup-1.htm")));
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
						if (qs.isCond(3) && (getQuestItemsCount(killer, ORC_AMULET) < 10) && (getRandom(100) < 93))
						{
							giveItems(killer, ORC_AMULET, 1);
							if (getQuestItemsCount(killer, ORC_AMULET) >= 10)
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
								showOnScreenMsg(killer, NpcStringId.YOU_HAVE_KILLED_ENOUGH_ORCS_NGO_HUNTING_AND_KILL_ORC_WARRIORS_AND_WEREWOLVES, ExShowScreenMessage.TOP_CENTER, 10000);
								qs.setCond(4);
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
				case ORC_WARRIOR:
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
								if (qsm.isCond(4) && (getRandom(100) < 89))
								{
									if (getQuestItemsCount(member, BROKEN_SWORD) < 5)
									{
										giveItems(member, BROKEN_SWORD, 1);
										if ((getQuestItemsCount(member, BROKEN_SWORD) >= 5) && (getQuestItemsCount(member, WEREWOLFS_FANG) >= 5))
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_MIDDLE);
											showOnScreenMsg(member, NpcStringId.YOU_FULFILLED_ALL_ALTRAN_S_REQUESTS_N_RETURN_TO_ALTRAN, ExShowScreenMessage.TOP_CENTER, 10000);
											qsm.setCond(5);
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
						if (qs.isCond(4) && (getQuestItemsCount(killer, BROKEN_SWORD) < 5) && (getRandom(100) < 89))
						{
							giveItems(killer, BROKEN_SWORD, 1);
							if ((getQuestItemsCount(killer, BROKEN_SWORD) >= 5) && (getQuestItemsCount(killer, WEREWOLFS_FANG) >= 5))
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
								showOnScreenMsg(killer, NpcStringId.YOU_FULFILLED_ALL_ALTRAN_S_REQUESTS_N_RETURN_TO_ALTRAN, ExShowScreenMessage.TOP_CENTER, 10000);
								qs.setCond(5);
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
				case WEREWOLVES:
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
								if (qsm.isCond(4) && (getRandom(100) < 100))
								{
									if (getQuestItemsCount(member, WEREWOLFS_FANG) < 5)
									{
										giveItems(member, WEREWOLFS_FANG, 1);
										if ((getQuestItemsCount(member, WEREWOLFS_FANG) >= 5) && (getQuestItemsCount(member, BROKEN_SWORD) >= 5))
										{
											playSound(member, QuestSound.ITEMSOUND_QUEST_MIDDLE);
											showOnScreenMsg(member, NpcStringId.YOU_FULFILLED_ALL_ALTRAN_S_REQUESTS_N_RETURN_TO_ALTRAN, ExShowScreenMessage.TOP_CENTER, 10000);
											qsm.setCond(5);
											member.sendPacket(new TutorialShowHtml(getHtm(member, "popup-2.htm")));
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
						if (qs.isCond(4) && (getQuestItemsCount(killer, WEREWOLFS_FANG) < 5) && (getRandom(100) < 100))
						{
							giveItems(killer, WEREWOLFS_FANG, 1);
							if ((getQuestItemsCount(killer, WEREWOLFS_FANG) >= 5) && (getQuestItemsCount(killer, BROKEN_SWORD) >= 5))
							{
								playSound(killer, QuestSound.ITEMSOUND_QUEST_MIDDLE);
								showOnScreenMsg(killer, NpcStringId.YOU_FULFILLED_ALL_ALTRAN_S_REQUESTS_N_RETURN_TO_ALTRAN, ExShowScreenMessage.TOP_CENTER, 10000);
								qs.setCond(5);
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
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
