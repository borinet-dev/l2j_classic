package quests.Q00402_BorinetNewQuestPart2;

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
public class Q00402_BorinetNewQuestPart2 extends Quest
{
	// NPCs
	private static final int 마틸드 = 30738;
	// Items
	private static final int 진딧물 = 3689;
	// Monsters
	private static final int 일개미 = 20079;
	private static final int 일개미_대장 = 20080;
	private static final int 일개미_감독 = 20081;
	private static final int 병정개미_신병 = 20082;
	private static final int 병정개미_척후병 = 20084;
	private static final int 병정개미_경비병 = 20086;
	private static final int 병정개미 = 20087;
	private static final int 병정개미_대장 = 20088;
	// Misc
	private static final int MIN_LEVEL = 25;
	private static final int MAX_LEVEL = 27;
	
	public Q00402_BorinetNewQuestPart2()
	{
		super(402);
		addStartNpc(마틸드);
		addTalkId(마틸드);
		addKillId(일개미, 일개미_대장, 일개미_감독, 일개미_감독, 병정개미_신병, 병정개미_척후병, 병정개미_경비병, 병정개미, 병정개미_대장);
		addCondMinLevel(MIN_LEVEL, "no-level.html");
		addCondMaxLevel(MAX_LEVEL, "no-level.html");
		registerQuestItems(진딧물);
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
				showOnScreenMsg(player, "개미굴로 이동해서 개미들을 때려잡자!", ExShowScreenMessage.TOP_CENTER, 5000);
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
				takeItems(player, 진딧물, -1);
				
				if (player.getLevel() < 28)
				{
					final int level = 28;
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
			if (getQuestItemsCount(player, 진딧물) < 50)
			{
				giveItems(player, 진딧물, 1);
				if (getQuestItemsCount(player, 진딧물) >= 50)
				{
					qs.setCond(2);
					playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					showOnScreenMsg(player, "병정개미의 진딧물을 모두 구했다. 마틸드에게 이동하자.", ExShowScreenMessage.TOP_CENTER, 10000);
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
		if ((qs != null) && (getQuestItemsCount(event.getPlayer(), 진딧물) >= 50))
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
		
		final QuestState qs = player.getQuestState(this.getClass().getSimpleName());
		final QuestState qs2 = player.getQuestState("Q11000_MoonKnight");
		
		if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() < MAX_LEVEL) && (qs == null) && (qs2 != null) && qs2.isCompleted() && canStartQuest(player))
		{
			final String html = getHtm(player, "popup.html");
			player.sendPacket(new TutorialShowHtml(html));
			playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
		}
	}
}