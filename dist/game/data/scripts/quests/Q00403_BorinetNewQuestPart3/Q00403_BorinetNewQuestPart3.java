package quests.Q00403_BorinetNewQuestPart3;

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
public class Q00403_BorinetNewQuestPart3 extends Quest
{
	// NPCs
	private static final int 소피아 = 30735;
	// Items
	private static final int 언데드의_재 = 3848;
	// Monsters
	private static final int 만드라고라_유체 = 20223;
	private static final int 만드라고라 = 20154;
	private static final int 만드라고라_성체 = 20155;
	private static final int 만드라고라_완전체 = 20156;
	private static final int 구울 = 20201;
	private static final int 화강암골렘 = 20083;
	private static final int 시체_추적자 = 20202;
	private static final int 니르_크롤러 = 20160;
	private static final int 니르_크롤러_프랙 = 20198;
	private static final int 앰버_바실리스크 = 20199;
	private static final int 스트레인 = 20200;
	private static final int 행드맨_리퍼 = 20144;
	private static final int 스펙터 = 20171;
	private static final int 레무레스 = 20197;
	// Misc
	private static final int MIN_LEVEL = 28;
	private static final int MAX_LEVEL = 31;
	
	public Q00403_BorinetNewQuestPart3()
	{
		super(403);
		addStartNpc(소피아);
		addTalkId(소피아);
		addKillId(만드라고라_유체, 만드라고라, 만드라고라_성체, 만드라고라_완전체, 구울, 화강암골렘, 시체_추적자, 니르_크롤러, 니르_크롤러_프랙, 앰버_바실리스크, 스트레인, 행드맨_리퍼, 스펙터, 레무레스);
		addCondMinLevel(MIN_LEVEL, "no-level.html");
		addCondMaxLevel(MAX_LEVEL, "no-level.html");
		registerQuestItems(언데드의_재);
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
				showOnScreenMsg(player, "처형터로 이동해서 언데드들을 후드려패자!", ExShowScreenMessage.TOP_CENTER, 5000);
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
				takeItems(player, 언데드의_재, -1);
				
				if (player.getLevel() < 32)
				{
					final int level = 32;
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
			if (getQuestItemsCount(player, 언데드의_재) < 50)
			{
				giveItems(player, 언데드의_재, 1);
				if (getQuestItemsCount(player, 언데드의_재) >= 50)
				{
					qs.setCond(2);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					showOnScreenMsg(player, "언데드의 재를 모두 구했다. 소피아에게 이동하자.", ExShowScreenMessage.TOP_CENTER, 10000);
					player.sendPacket(new TutorialShowHtml(getHtm(player, "popup-1.htm")));
					playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
				}
				else
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
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
	}
	
	private void handleTutorialQuest(Player player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() < MAX_LEVEL) && (qs == null) && canStartQuest(player))
		{
			final String html = getHtm(player, "popup.html");
			player.sendPacket(new TutorialShowHtml(html));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
		}
	}
}