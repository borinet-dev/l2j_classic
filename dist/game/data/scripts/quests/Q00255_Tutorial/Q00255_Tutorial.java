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
package quests.Q00255_Tutorial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.HtmlActionScope;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.Id;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureDeath;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureTeleported;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBypass;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerItemPickup;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerPressTutorialMark;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Util;

/**
 * Tutorial Quest
 * @author Mobius
 */
public class Q00255_Tutorial extends Quest
{
	private static final SkillHolder CUBIC = new SkillHolder(4338, 1);
	private static final SkillHolder[] FIGHTER_BUFFS =
	{
		new SkillHolder(30233, 1), // 헤이스트
		new SkillHolder(30234, 1), // 가이던스
		new SkillHolder(30236, 1), // 그레이트 마이트
		new SkillHolder(30238, 1), // 샤픈 엣지
		new SkillHolder(30239, 1), // 챈트 오브 빅토리
		new SkillHolder(30241, 1), // 버서커 스피릿
		new SkillHolder(30242, 1), // 임프로브 컴뱃
		new SkillHolder(30243, 1), // 임프로브 실드 디펜스
		new SkillHolder(30244, 1), // 임프로브 매직
		new SkillHolder(30245, 1), // 임프로브 컨디션
		new SkillHolder(30246, 1), // 임프로브 실드 크리티컬
		new SkillHolder(30247, 1), // 임프로브 무브먼트
		new SkillHolder(30248, 1), // 클레리티
		new SkillHolder(30249, 1), // 블레싱 오브 노블레스
	};
	private static final SkillHolder[] MAGE_BUFFS =
	{
		new SkillHolder(30235, 1), // 아큐맨
		new SkillHolder(30237, 1), // 그레이트 실드
		new SkillHolder(7063, 1), // 프로피시 오브 윈드
		new SkillHolder(30240, 1), // 와일드 매직
		new SkillHolder(30241, 1), // 버서커 스피릿
		new SkillHolder(30242, 1), // 임프로브 컴뱃
		new SkillHolder(30243, 1), // 임프로브 실드 디펜스
		new SkillHolder(30244, 1), // 임프로브 매직
		new SkillHolder(30245, 1), // 임프로브 컨디션
		new SkillHolder(30246, 1), // 임프로브 실드 크리티컬
		new SkillHolder(30247, 1), // 임프로브 무브먼트
		new SkillHolder(30248, 1), // 클레리티
		new SkillHolder(30249, 1), // 블레싱 오브 노블레스
	};
	// NPCs
	private static final List<Integer> NEWBIE_HELPERS = new ArrayList<>();
	static
	{
		NEWBIE_HELPERS.add(30009); // human fighter
		NEWBIE_HELPERS.add(30019); // human mystic
		NEWBIE_HELPERS.add(30400); // elf
		NEWBIE_HELPERS.add(30131); // dark elf
		NEWBIE_HELPERS.add(30575); // orc
		NEWBIE_HELPERS.add(30530); // dwarf
		NEWBIE_HELPERS.add(30598); // 말섬
	}
	// Monsters
	private static final int[] MONSTERS =
	{
		20544, // this is used for now
		20481,
		20432,
		20120,
		18342
	};
	// Items
	private static final int BLUE_GEM = 6353;
	private static final ItemHolder SOULSHOT_REWARD = new ItemHolder(5789, 1000);
	private static final ItemHolder SPIRITSHOT_REWARD = new ItemHolder(5790, 1000);
	private static final ItemHolder HEALING_POTION = new ItemHolder(1539, 20);
	private static final ItemHolder BUFF_SCROLLS1 = new ItemHolder(3926, 10);
	private static final ItemHolder BUFF_SCROLLS2 = new ItemHolder(3927, 10);
	private static final ItemHolder BUFF_SCROLLS3 = new ItemHolder(3928, 10);
	private static final ItemHolder BUFF_SCROLLS4 = new ItemHolder(3929, 10);
	private static final ItemHolder BUFF_SCROLLS5 = new ItemHolder(3930, 10);
	private static final ItemHolder BUFF_SCROLLS6 = new ItemHolder(3931, 10);
	private static final ItemHolder BUFF_SCROLLS7 = new ItemHolder(3932, 10);
	private static final ItemHolder BUFF_SCROLLS8 = new ItemHolder(3933, 10);
	private static final ItemHolder BUFF_SCROLLS9 = new ItemHolder(3934, 10);
	private static final ItemHolder BUFF_SCROLLS10 = new ItemHolder(3935, 10);
	
	// Others
	private static final String TUTORIAL_BYPASS = "Quest Q00255_Tutorial ";
	private static final int QUESTION_MARK_ID_1 = 1;
	private static final int QUESTION_MARK_ID_2 = 5;
	private static final int QUESTION_MARK_ID_3 = 3;
	
	public Q00255_Tutorial()
	{
		super(255);
		addTalkId(NEWBIE_HELPERS);
		addFirstTalkId(NEWBIE_HELPERS);
		addKillId(MONSTERS);
		registerQuestItems(BLUE_GEM);
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
			case "start_newbie_tutorial":
			{
				qs.setMemoState(2);
				playTutorialVoice(player, "borinet/newbie");
				showTutorialHtml(player, "borinet/tutorial_Newbie_01.htm");
				BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
				player.sendMessage("커스텀 능력치를 올리세요. 10포인트는 무료입니다!");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "커스텀 능력치를 올리세요. 10포인트는 무료입니다!"));
				break;
			}
			case "tutorial_Newbie_02.htm":
			case "tutorial_Newbie_03.htm":
			case "tutorial_Newbie_04.htm":
			case "tutorial_Newbie_05.htm":
			{
				showTutorialHtml(player, "borinet/" + event);
				break;
			}
			case "question_mark_1":
			{
				qs.setMemoState(2);
				playSound(player, "ItemSound.quest_tutorial");
				player.sendPacket(new TutorialShowQuestionMark(QUESTION_MARK_ID_1, 0));
				player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
				player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
				break;
			}
			case "tutorial_reward":
			{
				if (qs.getMemoState() == 5)
				{
					showTutorialHtml(player, "borinet/quest_start_02.htm");
					qs.setMemoState(6);
					SkillCaster.triggerCast(npc, player, CUBIC.getSkill());
					giveItems(player, HEALING_POTION);
					giveItems(player, BUFF_SCROLLS1);
					giveItems(player, BUFF_SCROLLS2);
					giveItems(player, BUFF_SCROLLS3);
					giveItems(player, BUFF_SCROLLS4);
					giveItems(player, BUFF_SCROLLS5);
					giveItems(player, BUFF_SCROLLS6);
					giveItems(player, BUFF_SCROLLS7);
					giveItems(player, BUFF_SCROLLS8);
					giveItems(player, BUFF_SCROLLS9);
					giveItems(player, BUFF_SCROLLS10);
					
					int itemId = 0;
					if (player.isMageClass() && (player.getOriginRace() != Race.ORC))
					{
						itemId = 1142;
						for (SkillHolder skill : MAGE_BUFFS)
						{
							SkillCaster.triggerCast(npc, player, skill.getSkill());
						}
					}
					else if (player.getOriginRace() == Race.ORC)
					{
						itemId = 255;
						for (SkillHolder skill : FIGHTER_BUFFS)
						{
							SkillCaster.triggerCast(npc, player, skill.getSkill());
						}
					}
					else if (player.getOriginRace() == Race.DWARF)
					{
						itemId = 2501;
						for (SkillHolder skill : FIGHTER_BUFFS)
						{
							SkillCaster.triggerCast(npc, player, skill.getSkill());
						}
					}
					else
					{
						itemId = 121;
						for (SkillHolder skill : FIGHTER_BUFFS)
						{
							SkillCaster.triggerCast(npc, player, skill.getSkill());
						}
					}
					
					final Item createditem = ItemTemplate.createItem(itemId);
					player.addItem("튜토리얼", createditem, null, true);
					player.getInventory().equipItem(createditem);
					final InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(createditem);
					player.sendInventoryUpdate(playerIU);
					
					startQuestTimer("buff", 1000, null, player);
				}
				break;
			}
			case "buff":
			{
				if (player.isMageClass() && (player.getOriginRace() != Race.ORC))
				{
					for (SkillHolder skill : MAGE_BUFFS)
					{
						SkillCaster.triggerCast(player, player, skill.getSkill());
					}
				}
				else
				{
					for (SkillHolder skill : FIGHTER_BUFFS)
					{
						SkillCaster.triggerCast(player, player, skill.getSkill());
					}
				}
				SkillCaster.triggerCast(player, player, CUBIC.getSkill());
				break;
			}
			case "goto_talkingIsland":
			{
				player.teleToLocation(-83929, 243354, -3720, 0);
				player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
				player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
				startQuestTimer("TutorialEnd", 1500, null, player);
				break;
			}
			case "TutorialEnd":
			{
				addRadar(player, -84149, 243141, -3704);
				playTutorialVoice(player, "borinet/TalkingIsland");
				showTutorialHtml(player, "borinet/tutorial_Newbie_09.htm");
				break;
			}
			case "close_tutorial":
			{
				player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
				player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
				break;
			}
			case "teleToDeathPoint":
			{
				BorinetUtil.getInstance().checkTeleCond(player, false);
				break;
			}
		}
		if (event.startsWith("setemail"))
		{
			final StringTokenizer st = new StringTokenizer(event);
			st.nextToken();
			try
			{
				final String email1 = st.nextToken();
				final String email2 = st.nextToken();
				
				setEmail(player, email1, email2);
			}
			catch (Exception e)
			{
				player.sendMessage("계속하기 전에 모든 필드를 채우십시오.");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "계속하기 전에 모든 필드를 채우십시오."));
			}
		}
		if (event.startsWith("changeName"))
		{
			final StringTokenizer st = new StringTokenizer(event);
			st.nextToken();
			try
			{
				final String name = st.nextToken();
				
				setName(player, name);
			}
			catch (Exception e)
			{
				player.sendMessage("계속하기 전에 모든 필드를 채우십시오.");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "와썹", "계속하기 전에 모든 필드를 채우십시오."));
			}
		}
		if (event.equals("NoChangeName"))
		{
			player.getVariables().set("캐릭터명 무료 변경", 1);
			checkEmail(player);
		}
		if (event.equals("JoinToClan"))
		{
			final Clan clan = ClanTable.getInstance().getClan(269357273);
			if (clan != null)
			{
				player.sendPacket(new JoinPledge(clan.getId()));
				player.setPledgeType(Clan.SUBUNIT_ACADEMY);
				
				player.setPowerGrade(9); // academy
				player.setLvlJoinedAcademy(player.getLevel());
				
				clan.addClanMember(player);
				player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
				
				player.sendSkillList();
				
				player.getVariables().set("신규자 혈맹 아카데미", 1);
				player.getVariables().set("신규자혈맹가입", System.currentTimeMillis());
				player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
				
				// this activates the clan tab on the new member
				PledgeShowMemberListAll.sendAllTo(player);
				player.setClanJoinExpiryTime(0);
				player.broadcastUserInfo();
				player.getRequest().onRequestResponse();
				
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(player), player);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
				
				startQuestTimer("WELCOM_MESSEGES", 100, null, player);
			}
		}
		if (event.equals("NoJoin"))
		{
			player.sendMessage("신규자 혈맹 아카데미에 가입하지 않았습니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "신규자 혈맹 아카데미에 가입하지 않았습니다."));
			player.getVariables().set("신규자 혈맹 아카데미", 1);
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
		}
		if (event.equals("WELCOM_MESSEGES"))
		{
			player.sendMessage("신규자 혈맹 아카데미에 가입하였습니다.");
			player.sendMessage("신규자 혈맹 가입 후 7일이 경과하면 자동으로 제명됩니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "신규자 혈맹 아카데미에 가입하였습니다."));
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "신규자 혈맹 가입 후 7일이 경과하면 자동으로 제명됩니다."));
			CommonSkill.NEWBIE_CLAN_ADVENT.getSkill().applyEffects(player, player);
			
			final Clan clan = ClanTable.getInstance().getClan(269357273);
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
			sm.addString(player.getName());
			clan.broadcastToOnlineMembers(sm);
			
			clan.broadcastClanStatus();
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		
		if ((qs != null) && (player.getLevel() < 20))
		{
			if ((npc.getId() == 30009) && (qs.getMemoState() >= 6))
			{
				return "borinet/quest_start_02.htm";
			}
			// start newbie helpers
			if (NEWBIE_HELPERS.contains(npc.getId()))
			{
				if (hasQuestItems(player, BLUE_GEM))
				{
					qs.setMemoState(4);
				}
				switch (qs.getMemoState())
				{
					case 0:
					case 1:
					case 2:
					{
						player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
						player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
						qs.setMemoState(3);
						return "borinet/newbie_helper_01.htm";
					}
					case 3:
					{
						return "borinet/newbie_helper_01_back.htm";
					}
					case 4:
					{
						player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
						player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
						qs.setMemoState(5);
						takeItems(player, BLUE_GEM, -1);
						if (player.getLevel() < 2)
						{
							final byte level = Byte.parseByte("2");
							player.getStat().setLevel(level);
							player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
							player.setCurrentCp(player.getMaxCp());
							player.broadcastUserInfo();
						}
						if (player.isMageClass() && (player.getOriginRace() != Race.ORC))
						{
							giveItems(player, SPIRITSHOT_REWARD);
							playTutorialVoice(player, "tutorial_voice_027");
							return "borinet/quest_start_01.htm";
						}
						giveItems(player, SOULSHOT_REWARD);
						playTutorialVoice(player, "tutorial_voice_026");
						return "borinet/quest_start_01.htm";
					}
					case 5:
					{
						return "borinet/quest_start_01.htm";
					}
					case 6:
					{
						qs.setMemoState(7);
						return "borinet/quest_start_03.htm";
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.getMemoState() <= 3) && !hasQuestItems(killer, BLUE_GEM))
		{
			if (getRandom(100) < 45)
			{
				// 정확한 카운트 로직
				long counter = World.getInstance().getVisibleObjectsInRange(killer, Item.class, 1500).stream().filter(item -> item.getId() == BLUE_GEM).count();
				if (counter < 5) // 5개 이하일 때만 드랍
				{
					npc.dropItem(killer, BLUE_GEM, 1);
				}
			}
			else
			{
				npc.dropItem(killer, 57, Rnd.get(10, 100));
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_ITEM_PICKUP)
	@RegisterType(ListenerRegisterType.ITEM)
	@Id(BLUE_GEM)
	public void OnPlayerItemPickup(OnPlayerItemPickup event)
	{
		final Player player = event.getPlayer();
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.getMemoState() < 4))
		{
			qs.setMemoState(4);
			playSound(player, "ItemSound.quest_tutorial");
			playTutorialVoice(player, "tutorial_voice_013");
			showTutorialHtml(event.getPlayer(), "borinet/tutorial_Newbie_07.htm");
			player.sendPacket(new TutorialShowQuestionMark(QUESTION_MARK_ID_2, 0));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerPressTutorialMark(OnPlayerPressTutorialMark event)
	{
		final QuestState qs = getQuestState(event.getPlayer(), false);
		if (qs != null)
		{
			switch (event.getMarkId())
			{
				case QUESTION_MARK_ID_1:
				{
					if (qs.isMemoState(2))
					{
						showOnScreenMsg(event.getPlayer(), NpcStringId.SPEAK_WITH_THE_NEWBIE_HELPER, ExShowScreenMessage.TOP_CENTER, 5000);
						addRadar(event.getPlayer(), -72911, 256511, -3120);
						showTutorialHtml(event.getPlayer(), "borinet/tutorial_Newbie_05.htm");
					}
					break;
				}
				case QUESTION_MARK_ID_2:
				{
					if (qs.isMemoState(4))
					{
						addRadar(event.getPlayer(), -72911, 256511, -3120);
						playTutorialVoice(event.getPlayer(), "tutorial_voice_last");
						showTutorialHtml(event.getPlayer(), "borinet/tutorial_Newbie_08.htm");
					}
					break;
				}
				case QUESTION_MARK_ID_3:
				{
					BorinetUtil.getInstance().checkTeleCond(event.getPlayer(), true);
					break;
				}
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerBypass(OnPlayerBypass event)
	{
		final Player player = event.getPlayer();
		if (event.getCommand().startsWith(TUTORIAL_BYPASS))
		{
			notifyEvent(event.getCommand().replace(TUTORIAL_BYPASS, ""), null, player);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		final Player player = event.getPlayer();
		if ((player.getLevel() > 1) && (player.getLevel() < 39))
		{
			if (Config.ENABLE_NEWBIE_GIFT)
			{
				BorinetUtil.getInstance().checkGift(player);
			}
			if ((player.getClan() == null) && (player.getVariables().getInt("신규자 혈맹 아카데미", 0) < 1))
			{
				clanJoin(player);
			}
			return;
		}
		if (player.getLevel() > 39)
		{
			player.getVariables().remove("신규자 혈맹 아카데미");
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
		if ((player.getLevel() > 1) && (player.getLevel() < 39))
		{
			if (Config.ENABLE_NEWBIE_GIFT)
			{
				BorinetUtil.getInstance().checkGift(player);
			}
			if ((player.getClan() == null) && (player.getVariables().getInt("신규자 혈맹 아카데미", 0) < 1))
			{
				clanJoin(player);
			}
			
			String deathLocation = player.getVariables().getString("DeathLocation", null);
			if (deathLocation != null)
			{
				BorinetUtil.getInstance().teleToDeathPoint(player);
			}
			
			return;
		}
		
		final QuestState qs = getQuestState(player, true);
		if ((qs.getMemoState() < 5) && (player.getLevel() < 20))
		{
			startQuestTimer("buff", 1000, null, player);
		}
		if ((player.getVariables().getInt("캐릭터명 무료 변경", 0) < 1) && (player.getLevel() < 20))
		{
			qs.startQuest();
			qs.setMemoState(1);
			if (!player.getName().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*"))
			{
				changeName(player);
			}
			else
			{
				checkEmail(player);
			}
		}
	}
	
	private void showTutorialHtml(Player player, String html)
	{
		player.sendPacket(new TutorialShowHtml(getHtm(player, html)));
	}
	
	private void playTutorialVoice(Player player, String voice)
	{
		player.sendPacket(new PlaySound(2, voice, 0, 0, player.getX(), player.getY(), player.getZ()));
	}
	
	private void checkEmail(Player player)
	{
		if (getEmail(player.getAccountName()) == null)
		{
			playTutorialVoice(player, "borinet/Email");
			showTutorialHtml(player, "borinet/AccountEmail.htm");
			player.setInvul(true);
			player.setBlockActions(true);
		}
		else
		{
			player.setInvul(false);
			player.setBlockActions(false);
			
			if (player.getSpecPoint() == 10)
			{
				startQuestTimer("start_newbie_tutorial", 500, null, player);
				startQuestTimer("buff", 500, null, player);
			}
		}
	}
	
	private String getEmail(String accountName)
	{
		if (accountName == null)
		{
			return null;
		}
		return getAccountValue(accountName);
	}
	
	private static boolean validateEmail(String email, String email2)
	{
		
		if ((email == null) || (email2 == null) || email.isEmpty() || email2.isEmpty())
		{
			return false;
		}
		
		if (email.contains("@") && email.contains(".") && (email.length() <= 50) && (email.length() >= 5))
		{
			if (email.equalsIgnoreCase(email2))
			{
				return true;
			}
		}
		return false;
	}
	
	private void setEmail(Player player, String email, String confirmEmail)
	{
		if (!validateEmail(email, confirmEmail))
		{
			player.sendMessage("이 이메일 주소는 유효하지 않습니다. 유효한 것으로 다시 시도하십시오.");
			player.sendMessage("이 계정의 비밀번호 분실시 이를 인식 할 수있는 유일한 방법이므로 유효한 전자 메일 주소를 사용하는 것이 중요합니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "이 이메일 주소는 유효하지 않습니다. 유효한 것으로 다시 시도하십시오."));
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "이 계정의 비밀번호 분실시 이를 인식 할 수있는 유일한 방법이므로 유효한 전자 메일 주소를 사용하는 것이 중요합니다."));
		}
		else
		{
			insertAccountData(player.getAccountName(), email);
			player.sendMessage("귀하의 이메일이 성공적으로 설정되었습니다: " + email);
			player.sendMessage("이 이메일 주소는 이 계정의 비밀번호 분실시 사용됩니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "귀하의 이메일이 성공적으로 설정되었습니다: " + email));
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "이 이메일 주소는 이 계정의 비밀번호 분실시 사용됩니다."));
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			final MagicSkillUse msu = new MagicSkillUse(player, player, 6463, 1, 0, 500);
			player.broadcastPacket(msu);
			player.broadcastCharInfo();
			player.setInvul(false);
			player.setBlockActions(false);
			
			if (player.getSpecPoint() == 10)
			{
				startQuestTimer("start_newbie_tutorial", 500, null, player);
				startQuestTimer("buff", 500, null, player);
			}
		}
	}
	
	private void insertAccountData(String accountName, String value)
	{
		String query = "UPDATE accounts SET e_mail = ? WHERE login = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setString(1, value);
			statement.setString(2, accountName);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "계정 변수 삽입 중 오류가 발생했습니다.", e);
		}
	}
	
	private String getAccountValue(String accountName)
	{
		String data = null;
		String query = "SELECT e_mail FROM accounts WHERE login=?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setString(1, accountName);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					data = rset.getString(1);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "계정 변수 값을 조회하는 중 오류가 발생했습니다.", e);
		}
		return data;
	}
	
	private void changeName(Player player)
	{
		playTutorialVoice(player, "borinet/CharName");
		showTutorialHtml(player, "borinet/ChangeName.htm");
		player.setInvul(true);
		player.setBlockActions(true);
	}
	
	private void clanJoin(Player player)
	{
		showTutorialHtml(player, "borinet/ClanJoin.htm");
	}
	
	private void setName(Player player, String name)
	{
		if (!checkCondition(player, name))
		{
			player.setName(name);
			if (Config.CACHE_CHAR_NAMES)
			{
				CharInfoTable.getInstance().addName(player);
			}
			player.storeMe();
			player.sendMessage("캐릭터 이름이 성공적으로 변경되었습니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "와썹", "캐릭터 이름이 성공적으로 변경되었습니다."));
			player.broadcastUserInfo();
			player.decayMe();
			player.spawnMe(player.getX(), player.getY(), player.getZ());
			player.getVariables().set("캐릭터명 무료 변경", 1);
			checkEmail(player);
			
			if (player.isInParty())
			{
				player.getParty().broadcastToPartyMembers(player, PartySmallWindowDeleteAll.STATIC_PACKET);
				for (Player member : player.getParty().getMembers())
				{
					if (member != player)
					{
						member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
					}
				}
			}
			if (player.getClan() != null)
			{
				player.getClan().broadcastClanStatus();
			}
		}
	}
	
	private boolean checkCondition(Player player, String name)
	{
		if (name.matches(".*[\\uAC00-\\uD7A3]+.*"))
		{
			if ((name.length() < 1) || (name.length() > 8))
			{
				BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
				player.sendPacket(SystemMessageId.YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGTH_PLEASE_TRY_AGAIN);
				return true;
			}
		}
		else
		{
			if ((name.length() < 1) || (name.length() > 16))
			{
				BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
				player.sendPacket(SystemMessageId.YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGTH_PLEASE_TRY_AGAIN);
				return true;
			}
		}
		if (Config.FORBIDDEN_NAMES.length > 0)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (name.toLowerCase().contains(st.toLowerCase()))
				{
					showTutorialHtml(player, "borinet/ChangeName.htm");
					player.sendPacket(SystemMessageId.INCORRECT_NAME_PLEASE_TRY_AGAIN);
					return true;
				}
			}
		}
		if (FakePlayerData.getInstance().getProperName(name) != null)
		{
			showTutorialHtml(player, "borinet/ChangeName.htm");
			player.sendPacket(SystemMessageId.INCORRECT_NAME_PLEASE_TRY_AGAIN);
			return true;
		}
		if (!Util.isMatchingRegexp(name, Config.NAME_TEMPLATE) || !Util.isAlphaNumeric(name) || !Util.isValidName(name) || CharInfoTable.getInstance().doesCharNameExist(name))
		{
			showTutorialHtml(player, "borinet/ChangeName.htm");
			player.sendPacket(SystemMessageId.INCORRECT_NAME_PLEASE_TRY_AGAIN);
			return true;
		}
		if ((name.matches("자리체")) || (name.matches("아카마나프")))
		{
			showTutorialHtml(player, "borinet/ChangeName.htm");
			player.sendMessage("저주받은 무기의 이름으로 변경할 수 없습니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "와썹", "저주받은 무기의 이름으로 변경할 수 없습니다."));
			return true;
		}
		if (CharInfoTable.getInstance().ObsceneCharName(name))
		{
			showTutorialHtml(player, "borinet/ChangeName.htm");
			player.sendMessage("욕설이 포함된 이름으로 변경할 수 없습니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "와썹", "욕설이 포함된 이름으로 변경할 수 없습니다."));
			return true;
		}
		return false;
	}
	
	// 튜토리얼 사망
	@RegisterEvent(EventType.ON_CREATURE_DEATH)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerDeath(OnCreatureDeath event)
	{
		Player player = (Player) event.getTarget();
		// 퀘스트가 진행 중일 때만 사망 좌표를 저장
		if (BorinetUtil.getInstance().isQuestActive(player) && (player.getLevel() < 37))
		{
			Location deathLocation = player.getLocation();
			String deathLocationString = deathLocation.getX() + "," + deathLocation.getY() + "," + deathLocation.getZ();
			player.getVariables().set("DeathLocation", deathLocationString);
		}
	}
	
	@RegisterEvent(EventType.ON_CREATURE_TELEPORTED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onCreatureTeleported(OnCreatureTeleported event)
	{
		Player player = (Player) event.getCreature();
		String deathLocation = player.getVariables().getString("DeathLocation", null);
		if (deathLocation != null)
		{
			BorinetUtil.getInstance().teleToDeathPoint(player);
		}
	}
}