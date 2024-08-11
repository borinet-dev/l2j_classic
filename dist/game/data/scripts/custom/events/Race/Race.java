/*
 * 이 파일은 L2J Mobius 프로젝트의 일부입니다.
 *
 * 이 프로그램은 무료 소프트웨어이며 다음과 같이 재배포하거나 수정할 수 있습니다.
 * GNU General Public License에 따라 제 3 버전 또는 이후 버전의 라이센스에 따라
 * 또는 (귀하의 선택에 따라) 발표됩니다.
 *
 * 이 프로그램은 유용할 것으로 기대하지만
 * 어떠한 보증도 없이 "있는 그대로" 제공됩니다. 암묵적인 보증도 아니며
 * 상품성 또는 특정 목적에 대한 무보증 보증도 없습니다. 더 자세한 내용은
 * GNU General Public License를 참조하십시오.
 *
 * 귀하가 이 프로그램과 함께 GNU General Public License를 받지 않은 경우
 * http://www.gnu.org/licenses/에서 확인하십시오.
 */

package custom.events.Race;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Event;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.skill.AbnormalType;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * 레이스 이벤트 클래스.
 */
public class Race extends Event
{
	// 등록 시간 5분
	private static final int REGISTER_TIME = Config.RACE_REGISTER_TIME;
	// 레이스 시간 10분
	private static final int RACE_TIME = Config.RACE_RUN_TIME;
	// NPC ID
	private static final int START_NPC = 900103;
	private static final int STOP_NPC = 900104;
	// 위치
	private static final String[] LOCATIONS =
	{
		"처형터 주변",
		"디온 성 다리",
		"플로란 마을 입구",
		"플로란 요새 게이트"
	};
	// 이벤트 NPC 목록
	private List<Npc> _npclist;
	// NPC
	private Npc _npc;
	// 플레이어 목록
	private Collection<Player> _players;
	// 이벤트 태스크
	private ScheduledFuture<?> _eventTask = null;
	// 이벤트 상태
	private static boolean _isactive = false;
	// 레이스 상태
	private static boolean _isRaceStarted = false;
	// 스킬 (기본값은 Frog)
	private static int _skill = 6201;
	// 레이더를 위해 두 번째 NPC 스폰 유지 필요
	private static int[] _randspawn = null;
	
	// @formatter:off
    private static final int[][] COORDS =
    {
        // x, y, z, heading
        { 39177, 144345, -3650, 0 },
        { 22294, 155892, -2950, 0 },
        { 16537, 169937, -3500, 0 },
        {  7644, 150898, -2890, 0 }
    };
    private static final int[][] REWARDS =
    {
        { 6622, 2 }, // Giant's Codex
        { 9625, 2 }, // Giant's Codex -
        { 9626, 2 }, // Giant's Codex -
        { 9627, 2 }, // Giant's Codex -
        { 9546, 5 }, // Attr stones
        { 9547, 5 },
        { 9548, 5 },
        { 9549, 5 },
        { 9550, 5 },
        { 9551, 5 },
        { 9574, 3 }, // Mid-Grade Life Stone: level 80
        { 9575, 2 }, // High-Grade Life Stone: level 80
        { 9576, 1 }, // Top-Grade Life Stone: level 80
        { 20034,1 }  // Revita pop
    };
    // @formatter:on
	
	private Race()
	{
		addStartNpc(START_NPC);
		addFirstTalkId(START_NPC);
		addTalkId(START_NPC);
		addStartNpc(STOP_NPC);
		addFirstTalkId(STOP_NPC);
		addTalkId(STOP_NPC);
		
		if (Config.RACE_EVENT_ENABLE && !BorinetTask._isActive)
		{
			ThreadPool.scheduleAtFixedRate(() -> startEvent(), BorinetTask.RaceEventStart(), BorinetUtil.MILLIS_PER_DAY); // 1 day
		}
	}
	
	private void startEvent()
	{
		eventStart(null);
	}
	
	@Override
	public boolean eventStart(Player eventMaker)
	{
		// 이미 활성화된 이벤트를 시작하지 않습니다.
		if (_isactive)
		{
			return false;
		}
		// 사용자 지정 NPC 데이터를 사용하도록 설정되어 있지 않으면 이벤트를 시작하지 않습니다.
		if (!Config.CUSTOM_NPC_DATA)
		{
			LOGGER.info(getName() + ": 사용자 지정 NPC 테이블이 비활성화되어 있으므로 이벤트를 시작할 수 없습니다!");
			return false;
		}
		// 목록 초기화
		_npclist = new ArrayList<>();
		_players = ConcurrentHashMap.newKeySet();
		// 이벤트 활성화 상태 설정
		_isactive = true;
		// 스폰 관리자
		_npc = recordSpawn(START_NPC, 18345, 145607, -3105, 58824, false, 0);
		// 이벤트 시작을 알립니다.
		Broadcast.toAllOnlinePlayers("이벤트: * 레이스 이벤트가 시작되었습니다! *");
		Broadcast.toAllOnlinePlayers("이벤트: 디온성 마을에 있는 이벤트 매니저를 통해 참가 신청을 할 수 있습니다.");
		Broadcast.toAllOnlinePlayers("이벤트: " + REGISTER_TIME + "분 후 레이스가 시작됩니다...");
		// 이벤트 종료 예약
		_eventTask = ThreadPool.schedule(this::StartRace, REGISTER_TIME * 60 * 1000);
		return true;
	}
	
	protected void StartRace()
	{
		// 플레이어가 가입하지 않았다면 레이스를 중지합니다.
		if (_players.isEmpty())
		{
			Broadcast.toAllOnlinePlayers("이벤트: 레이스가 취소되었습니다.");
			eventStop();
			return;
		}
		// 레이스 상태 설정
		_isRaceStarted = true;
		// 알림
		Broadcast.toAllOnlinePlayers("이벤트: 레이스가 시작되었습니다!");
		// 무작위로 종료 지점 설정
		final int location = getRandom(0, LOCATIONS.length - 1);
		_randspawn = COORDS[location];
		// NPC 스폰
		recordSpawn(STOP_NPC, _randspawn[0], _randspawn[1], _randspawn[2], _randspawn[3], false, 0);
		// 플레이어 변신 및 메시지 전송
		for (Player player : _players)
		{
			if ((player != null) && player.isOnline())
			{
				if (player.isInsideRadius2D(_npc, 500))
				{
					sendMessage(player, "레이스가 시작되었습니다! 최대한 빨리 레이스 종료 NPC를 찾으러 가십세요. " + LOCATIONS[location] + " 근처에 있습니다.");
					transformPlayer(player);
					player.getRadar().addMarker(_randspawn[0], _randspawn[1], _randspawn[2]);
				}
				else
				{
					sendMessage(player, "내 근처에 있으라고 했죠? 거리가 너무 멀어 레이스에서 제외되었습니다.");
					player.setOnEvent(false);
					_players.remove(player);
				}
			}
		}
		// 레이스 시간 만료 예약
		_eventTask = ThreadPool.schedule(this::timeUp, RACE_TIME * 60 * 1000);
	}
	
	@Override
	public boolean eventStop()
	{
		// 비활성화된 이벤트 중지하지 않습니다.
		if (!_isactive)
		{
			return false;
		}
		// 비활성화 상태로 설정
		_isactive = false;
		_isRaceStarted = false;
		// 작업이 있는 경우 취소
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		// 모든 플레이어의 변신 해제 및 이벤트 시작 지점으로 텔레포트
		for (Player player : _players)
		{
			if ((player != null) && player.isOnline())
			{
				player.setOnEvent(false);
				player.untransform();
				player.teleToLocation(_npc, true);
			}
		}
		// NPC 삭제
		for (Npc npc : _npclist)
		{
			if (npc != null)
			{
				npc.deleteMe();
			}
		}
		_npclist.clear();
		_players.clear();
		// 이벤트 종료 알림
		Broadcast.toAllOnlinePlayers("* 레이스 이벤트가 종료되었습니다! *");
		return true;
	}
	
	@Override
	public boolean eventBypass(Player player, String bypass)
	{
		if (bypass.startsWith("skill"))
		{
			if (_isRaceStarted)
			{
				player.sendMessage("레이스가 이미 시작되었습니다. 이제 변신 스킬을 변경할 수 없습니다.");
			}
			else
			{
				final int number = Integer.parseInt(bypass.substring(5));
				final Skill skill = SkillData.getInstance().getSkill(number, 1);
				if (skill != null)
				{
					_skill = number;
					player.sendMessage("변신 스킬이 설정되었습니다:");
					player.sendMessage(skill.getName());
				}
				else
				{
					player.sendMessage("변신 스킬 변경 중 오류 발생");
				}
			}
		}
		else if (bypass.startsWith("tele"))
		{
			if ((Integer.parseInt(bypass.substring(4)) > 0) && (_randspawn != null))
			{
				player.teleToLocation(_randspawn[0], _randspawn[1], _randspawn[2]);
			}
			else
			{
				player.teleToLocation(18345, 145607, -3105);
			}
		}
		showMenu(player);
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		if (event.equalsIgnoreCase("transform"))
		{
			transformPlayer(player);
			return null;
		}
		else if (event.equalsIgnoreCase("untransform"))
		{
			player.untransform();
			return null;
		}
		else if (event.equalsIgnoreCase("showfinish"))
		{
			player.getRadar().addMarker(_randspawn[0], _randspawn[1], _randspawn[2]);
			return null;
		}
		else if (event.equalsIgnoreCase("signup"))
		{
			if (_players.contains(player))
			{
				return "900103-onlist.htm";
			}
			_players.add(player);
			player.setOnEvent(true);
			return "900103-signup.htm";
		}
		else if (event.equalsIgnoreCase("quit"))
		{
			player.untransform();
			if (_players.contains(player))
			{
				player.setOnEvent(false);
				_players.remove(player);
			}
			return "900103-quit.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		getQuestState(player, true);
		if (npc.getId() == START_NPC)
		{
			return START_NPC + (_isRaceStarted ? "-started-" : "-") + isRacing(player) + ".htm";
		}
		else if ((npc.getId() == STOP_NPC))
		{
			if (_isRaceStarted)
			{
				if (!_players.contains(player))
				{
					return STOP_NPC + "-0.htm";
				}
				if (player.isAffectedBySkill(_skill))
				{
					winRace(player);
					return STOP_NPC + "-winner.htm";
				}
				return STOP_NPC + "-notrans.htm";
			}
		}
		return npc.getId() + ".htm";
	}
	
	private int isRacing(Player player)
	{
		return _players.contains(player) ? 1 : 0;
	}
	
	private Npc recordSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay)
	{
		final Npc npc = addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay);
		if (npc != null)
		{
			_npclist.add(npc);
		}
		return npc;
	}
	
	private void transformPlayer(Player player)
	{
		if (player.isTransformed())
		{
			player.untransform();
		}
		if (player.isSitting())
		{
			player.standUp();
		}
		
		player.getEffectList().stopEffects(AbnormalType.SPEED_UP);
		player.stopSkillEffects(SkillFinishType.REMOVED, 268);
		player.stopSkillEffects(SkillFinishType.REMOVED, 298); // Rabbit Spirit Totem
		SkillData.getInstance().getSkill(_skill, 1).applyEffects(player, player);
	}
	
	private void sendMessage(Player player, String text)
	{
		player.sendPacket(new CreatureSay(_npc, ChatType.MPCC_ROOM, _npc.getName(), text));
	}
	
	private void showMenu(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		final String content = getHtm(player, "admin_menu.htm");
		html.setHtml(content);
		player.sendPacket(html);
	}
	
	protected void timeUp()
	{
		Broadcast.toAllOnlinePlayers("이벤트: 타임 오버! 우승자가 없습니다!");
		eventStop();
	}
	
	private void winRace(Player player)
	{
		final int[] reward = REWARDS[getRandom(REWARDS.length - 1)];
		player.addItem("레이스", reward[0], reward[1], _npc, true);
		Broadcast.toAllOnlinePlayers("이벤트: 레이스에서 " + player.getName() + "님이 우승했습니다!");
		eventStop();
	}
	
	public static void main(String[] args)
	{
		new Race();
	}
}
