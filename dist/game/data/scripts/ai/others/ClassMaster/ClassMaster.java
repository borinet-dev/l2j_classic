package ai.others.ClassMaster;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EnterEventTimes;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBypass;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerPressTutorialMark;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerProfessionChange;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.spawns.SpawnTemplate;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;
import org.l2jmobius.gameserver.util.BoatUtil;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class ClassMaster extends AbstractNpcAI
{
	// NPCs
	private static final List<Integer> CLASS_MASTERS = new ArrayList<>();
	static
	{
		CLASS_MASTERS.add(31756); // Mr. Cat
		CLASS_MASTERS.add(31757); // Queen of Hearts
	}
	
	public ClassMaster()
	{
		addStartNpc(CLASS_MASTERS);
		addTalkId(CLASS_MASTERS);
		addFirstTalkId(CLASS_MASTERS);
	}
	
	@Override
	public void onSpawnDeactivate(SpawnTemplate template)
	{
		template.despawnAll();
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		// 전직 가능 여부 확인
		if (!canChangeProfession(player))
		{
			// 전직 조건이 충족되지 않은 경우 기본 대화 반환
			return htmltext = "just_talk.html";
		}
		
		// 전직 조건 충족 시 전직 창 표시
		// 기본 HTML 파일 로드
		htmltext = getHtm(player, "class_master.html");
		StringBuilder options = new StringBuilder();
		
		// 현재 클래스에서 가능한 전직 리스트 생성
		for (ClassId nextClass : player.getClassId().getNextClassIds())
		{
			options.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest ClassMaster setclass ").append(nextClass.getId()).append("\">").append(Util.getFullClassName(nextClass.getId())) // 한글 클래스명 가져오기
				.append("</Button><br>");
		}
		
		// HTML에 옵션 삽입
		htmltext = htmltext.replace("%OPTIONS%", options.toString());
		return htmltext;
	}
	
	private boolean canChangeProfession(Player player)
	{
		// 전직 조건: 20/40/76 레벨일 때만 가능
		if ((player.getLevel() >= 20) && player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
		{
			return true;
		}
		else if ((player.getLevel() >= 40) && player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
		{
			return true;
		}
		else if ((player.getLevel() >= 76) && player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
		{
			return true;
		}
		
		// 조건 충족하지 않음
		return false;
	}
	
	@Override
	public String onAdvEvent(String eventValue, Npc npc, Player player)
	{
		String htmltext = null;
		String event = eventValue;
		final StringTokenizer st = new StringTokenizer(event);
		event = st.nextToken();
		
		switch (event)
		{
			case "setclass":
			{
				if (!st.hasMoreTokens())
				{
					return null;
				}
				
				final int classId = Integer.parseInt(st.nextToken());
				
				player.setClassId(classId);
				if (player.isSubClassActive())
				{
					player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
				}
				else
				{
					player.setBaseClass(player.getActiveClass());
				}
				
				player.store(false);
				player.broadcastUserInfo();
				player.sendSkillList();
				
				if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP) && (player.getLevel() >= 20))
				{
					final QuestState qs = player.getQuestState("Q00401_BorinetNewQuestPart1");
					if (qs == null)
					{
						htmltext = "popup.html";
						playSound(player, QuestSound.ITEMSOUND_QUEST_TUTORIAL);
					}
				}
				else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() >= 40))
				{
					htmltext = "PrisonAbyss.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	private void showPopupWindow(Player player)
	{
		int[] time = EnterEventTimes.check(player);
		int inTimes = time[0];
		int hour1 = time[1];
		int hour2 = time[2];
		int hour3 = time[3];
		int hour5 = time[4];
		
		boolean isBoatEnabled = false;
		if (BoatUtil.isStartedBoatBaikal || BoatUtil.isStartedBoatBorinet)
		{
			isBoatEnabled = true;
		}
		if (((inTimes >= 60) && (hour1 == 0)) || ((inTimes >= 120) && (hour2 == 0)) || ((inTimes >= 180) && (hour3 == 0)) || ((inTimes >= 300) && (hour5 == 0)))
		{
			player.sendPacket(new TutorialShowQuestionMark(201, 0));
		}
		
		String html = null;
		if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && (player.getLevel() >= 20))
		{
			player.sendPacket(new TutorialShowQuestionMark(6, 0)); // mark id was 1001 - used 2 for quest text
			changeClass(player);
		}
		else if (Config.ALLOW_BOAT && isBoatEnabled && !BoatUtil._stopRequested && (player.getLevel() >= 21) && (player.getVariables().getInt("선착장이동", 0) < 1))
		{
			showBoatMission(player);
		}
		else if (player.getLevel() == 35)
		{
			html = getHtm(player, "teleport_35.html");
		}
		else if (player.isMainClassActive() && (player.getLevel() == 45))
		{
			html = getHtm(player, "teleport_45.html");
		}
		else if (player.isMainClassActive() && (player.getLevel() == 55))
		{
			html = getHtm(player, "teleport_55.html");
		}
		else if (player.isMainClassActive() && (player.getLevel() == 65))
		{
			html = getHtm(player, "teleport_65.html");
		}
		else if ((player.isInCategory(CategoryType.SECOND_CLASS_GROUP)) && (player.getLevel() >= 40))
		{
			player.sendPacket(new TutorialShowQuestionMark(7, 0));
			changeClass(player);
		}
		else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() >= 76))
		{
			player.sendPacket(new TutorialShowQuestionMark(8, 0));
			changeClass(player);
		}
		if (html != null)
		{
			player.sendPacket(new TutorialShowHtml(html));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerPressTutorialMark(OnPlayerPressTutorialMark event)
	{
		final Player player = event.getPlayer();
		if (event.getMarkId() == 201)
		{
			EnterEventTimes.index(player);
		}
		
		if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && (player.getLevel() >= 20) && (event.getMarkId() == 6))
		{
			changeClass(player);
		}
		else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP) && (player.getLevel() >= 40) && (event.getMarkId() == 7)) // In retail you can skip first occupation
		{
			changeClass(player);
		}
		else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() >= 76) && (event.getMarkId() == 8))
		{
			changeClass(player);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerBypass(OnPlayerBypass event)
	{
		if (event.getCommand().startsWith("Quest ClassMaster "))
		{
			final String html = onAdvEvent(event.getCommand().substring(18), null, event.getPlayer());
			event.getPlayer().sendPacket(TutorialCloseHtml.STATIC_PACKET);
			showResult(event.getPlayer(), html);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PROFESSION_CHANGE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerProfessionChange(OnPlayerProfessionChange event)
	{
		showPopupWindow(event.getPlayer());
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		showPopupWindow(event.getPlayer());
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		showPopupWindow(event.getPlayer());
	}
	
	private void showBoatMission(Player player)
	{
		String html = generateBoatInfoHtml();
		TutorialShowHtml tutorialHtml = new TutorialShowHtml(html);
		player.sendPacket(tutorialHtml);
	}
	
	public String generateBoatInfoHtml()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>일일 미션</title><br>");
		sb.append("21레벨부터 <font color=LEVEL>정기선</font>을 탑승하는 미션이 있어요!<br1>");
		sb.append("화면 우측아래 [미션] 아이콘을 열어서 <font color=LEVEL>[저 푸른 바다로]</font> 미션 확인이 가능합니다.<br1>");
		sb.append("지금 선착장으로 이동하시면 <font color=LEVEL>정기선 배표</font>도 한장 받을 수 있답니다.<br1>");
		sb.append("<table width=280><tr><td><center><font color=\"LEVEL\">정기선 정보</font></center>");
		sb.append("</td></tr></table><tr><td>");
		
		if (!BoatUtil._stopRequested)
		{
			sb.append("<table border=0 width=280>");
			BoatUtil.getInstance().appendBoatInfo(sb, "보리넷 호");
			BoatUtil.getInstance().appendBoatInfo(sb, "바이칼 호");
			sb.append("</table>");
		}
		else
		{
			sb.append("<center>현재 정기선은 운행하지 않습니다.</center>");
		}
		
		// 보트 상태에 따라 다른 메시지를 추가
		sb.append("<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h voice .harbor 3945 -96801 261052 -3625\">말하는 섬 선착장으로 이동한다</Button>");
		sb.append("<Button ALIGN=LEFT ICON=\"TELEPORT\" action=\"bypass -h voice .harbor 3946 48579 190058 -3628\">기란 선착장으로 이동한다</Button>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	private void changeClass(Player player)
	{
		StringBuilder options = new StringBuilder();
		for (ClassId nextClass : player.getClassId().getNextClassIds())
		{
			options.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest ClassMaster setclass ").append(nextClass.getId()).append("\">").append(Util.getFullClassName(nextClass.getId())) // 한글 이름 추가
				.append("</Button><br>");
		}
		
		String html = getHtm(player, "class_master.html");
		html = html.replace("%OPTIONS%", options.toString());
		player.sendPacket(new TutorialShowHtml(html));
	}
	
	public static void main(String[] args)
	{
		new ClassMaster();
	}
}