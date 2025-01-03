package quests.Q00401_BorinetNewQuestPart1;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

/**
 * @author 보리넷 가츠
 */
public class Q00401_BorinetNewQuestPart1 extends Quest
{
	// NPCs
	private static final int 카렐_베스퍼_경 = 30417;
	// Items
	private static final int 오크_부적 = 90201;
	// Monsters
	private static final int 오크_군장 = 20495;
	private static final int 오크_궁수 = 20496;
	private static final int 오크_돌격병 = 20497;
	private static final int 오크_보급병 = 20498;
	private static final int 오크_보병 = 20499;
	private static final int 오크_보초병 = 20500;
	private static final int 오크_제사장 = 20501;
	// Misc
	private static final int MIN_LEVEL = 20;
	private static final int MAX_LEVEL = 24;
	
	public Q00401_BorinetNewQuestPart1()
	{
		super(401);
		addStartNpc(카렐_베스퍼_경);
		addTalkId(카렐_베스퍼_경);
		addKillId(오크_군장, 오크_궁수, 오크_돌격병, 오크_보급병, 오크_보병, 오크_보초병, 오크_제사장);
		addCondMinLevel(MIN_LEVEL, "no-level.html");
		addCondMaxLevel(MAX_LEVEL, "no-level.html");
		registerQuestItems(오크_부적);
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
				qs.setCond(1, true);
				showOnScreenMsg(player, "투렉 오크 야영지로 이동해서 오크를 잡자!", ExShowScreenMessage.TOP_CENTER, 5000);
				htmltext = event;
				break;
			}
		}
		if (event.equals("abort.html"))
		{
			htmltext = event;
		}
		else if (event.equals("reward"))
		{
			if (qs.isCond(2))
			{
				takeItems(player, 오크_부적, -1);
				
				if (player.getLevel() < 25)
				{
					final int level = 25;
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
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30001-02a.html";
						break;
					}
					case 2:
					{
						htmltext = "30001-03.html";
						break;
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
		if (qs.isCond(1) && (getRandom(100) < 75))
		{
			if (getQuestItemsCount(player, 오크_부적) < 50)
			{
				giveItems(player, 오크_부적, 1);
				if (getQuestItemsCount(player, 오크_부적) >= 50)
				{
					qs.setCond(2);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					showOnScreenMsg(player, "오크 부적을 모두 구했다. 카렐 베스퍼 경에게 이동하자.", ExShowScreenMessage.TOP_CENTER, 10000);
					theEnd(qs, player);
				}
				else
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
	}
	
	private void theEnd(QuestState qs, Player player)
	{
		player.sendPacket(new TutorialShowHtml(getHtm(player, "popup-1.htm")));
		playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		handleTutorialQuest(event.getPlayer());
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		handleTutorialQuest(event.getPlayer());
		
		final QuestState qs = getQuestState(event.getPlayer(), false);
		if ((qs != null) && (getQuestItemsCount(event.getPlayer(), 오크_부적) >= 50))
		{
			theEnd(qs, event.getPlayer());
		}
	}
	
	private void handleTutorialQuest(Player player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		final QuestState bqs = player.getQuestState("Q11005_PerfectLeatherArmor3");
		
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() < MAX_LEVEL) && (qs == null) && player.isInCategory(CategoryType.SECOND_CLASS_GROUP) && (bqs != null) && bqs.isCompleted())
		{
			final String html = getHtm(player, "popup.html");
			player.sendPacket(new TutorialShowHtml(html));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
		}
	}
}