package quests.Q00404_BorinetNewQuestPart4;

import java.util.List;

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
public class Q00404_BorinetNewQuestPart4 extends Quest
{
	// NPCs
	private static final int 멕켄 = 30108;
	// Items
	private static final int 브래카오크의두개골 = 3196;
	// Monsters
	private static final int 오크 = 20267;
	private static final int 오크_궁수 = 20268;
	private static final int 오크_주술사 = 20269;
	private static final int 오크_군장 = 20270;
	private static final int 오크_전사 = 20271;
	// Misc
	private static final int MIN_LEVEL = 32;
	private static final int MAX_LEVEL = 34;
	
	public Q00404_BorinetNewQuestPart4()
	{
		super(404);
		addStartNpc(멕켄);
		addTalkId(멕켄);
		addKillId(오크, 오크_궁수, 오크_주술사, 오크_군장, 오크_전사);
		addCondMinLevel(MIN_LEVEL, "no-level.html");
		addCondMaxLevel(MAX_LEVEL, "no-level.html");
		registerQuestItems(브래카오크의두개골);
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
				showOnScreenMsg(player, "브래카의 소굴로 이동해서 오크 뚝빼기를 깨자!", ExShowScreenMessage.TOP_CENTER, 5000);
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
				takeItems(player, 브래카오크의두개골, -1);
				
				if (player.getLevel() < 36)
				{
					final int level = 36;
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
			if (killer.isInParty())
			{
				final Party party = killer.getParty();
				final List<Player> members = party.getMembers();
				
				for (Player member : members)
				{
					final QuestState qsm = getQuestState(member, false);
					if ((member != null) && (qsm != null))
					{
						handleKill(qsm, member);
					}
				}
			}
			else
			{
				handleKill(qs, killer);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void handleKill(QuestState qs, Player player)
	{
		if (qs.isCond(1) && (getRandom(100) < 75))
		{
			if (getQuestItemsCount(player, 브래카오크의두개골) < 50)
			{
				giveItems(player, 브래카오크의두개골, 1);
				if (getQuestItemsCount(player, 브래카오크의두개골) >= 50)
				{
					qs.setCond(2);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					showOnScreenMsg(player, "오크 두개골을 모두 구했다. 마스터 멕켄에게 이동하자.", ExShowScreenMessage.TOP_CENTER, 10000);
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
		if ((qs != null) && (getQuestItemsCount(event.getPlayer(), 브래카오크의두개골) >= 50))
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
		final QuestState bqs = player.getQuestState("Q00403_BorinetNewQuestPart3");
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() < MAX_LEVEL) && (qs == null) && (bqs != null) && bqs.isCompleted())
		{
			final String html = getHtm(player, "popup.html");
			player.sendPacket(new TutorialShowHtml(html));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
		}
	}
}