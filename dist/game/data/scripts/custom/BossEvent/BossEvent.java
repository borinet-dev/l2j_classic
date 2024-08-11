package custom.BossEvent;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.NpcNameTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.RaidBoss;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

public class BossEvent extends AbstractNpcAI
{
	private final Collection<Npc> _npcs = ConcurrentHashMap.newKeySet();
	private static final Logger LOG = Logger.getLogger(BossEvent.class.getName());
	
	// @formatter:off
	private static final int[] BOSS =
	{
		41018,	41019,	41020,	41021,
		41022,	41023,	41024,	41025,
		41026,	41027,	41028,	41029,
		41030,	41031,	41032,	41033,
		41034,	41035,	41036, 	41037
	};

	// @formatter:on
	
	static String _bossName;
	static String _location;
	static int _bossId;
	static int _counts;
	
	private static final int EVENT_DURATION_HOURS = 1;
	
	private static boolean EVENT_ACTIVE = false;
	private ScheduledFuture<?> _eventTask = null;
	private static long _lastAttack = 0;
	
	private BossEvent()
	{
		addSpawnId(BOSS);
		addKillId(BOSS);
		addAttackId(BOSS);
		
		if (Config.RANDOM_BOSS_EVENT && !BorinetTask._isActive)
		{
			long randomTime = (Rnd.get(3, 24)) * 3600000;
			long randomMins = (Rnd.get(10, 50)) * 1000;
			long nextTime = randomTime + randomMins;
			
			if (GlobalVariablesManager.getInstance().hasVariable("Random_Boss_Spawn"))
			{
				long spawnTimes = GlobalVariablesManager.getInstance().getLong("Random_Boss_Spawn");
				if (spawnTimes > System.currentTimeMillis())
				{
					final long setTime = spawnTimes - System.currentTimeMillis();
					ThreadPool.schedule(this::eventStart, setTime);
				}
				else
				{
					ThreadPool.schedule(this::eventStart, BorinetTask.BossEventDelay() + nextTime);
				}
			}
			else
			{
				ThreadPool.schedule(this::eventStart, BorinetTask.BossEventDelay() + nextTime);
			}
			LOG.info("커스텀 이벤트: 랜덤보스 이벤트를 로드하였습니다.");
		}
		else
		{
			LOG.info("커스텀 이벤트: 랜덤보스 이벤트를 사용하지 않습니다.");
		}
	}
	
	private void eventStart()
	{
		if (EVENT_ACTIVE)
		{
			return;
		}
		
		EVENT_ACTIVE = true;
		
		_bossId = getRandom(0, BOSS.length - 1);
		final EventLocation randomLoc = getRandomEntry(EventLocation.values());
		final long despawnDelay = EVENT_DURATION_HOURS * 3600000;
		recordSpawn(_npcs, BOSS[_bossId], randomLoc.getX(), randomLoc.getY(), randomLoc.getZ(), 0, true, 0);
		
		_bossName = NpcNameTable.getInstance().getNpcNameKor(BOSS[_bossId]);
		_location = randomLoc.getName();
		
		Broadcast.toAllOnlinePlayersOnScreen("랜덤보스 이벤트: 랜덤 레이드보스 [" + _bossName + "]가 " + _location + " 주변에 나타났어요!");
		Broadcast.toAllOnlinePlayers("랜덤보스 이벤트: 랜덤 레이드보스 [" + _bossName + "]가 " + _location + " 주변에 나타났어요!");
		Broadcast.toAllOnlinePlayers("랜덤보스 이벤트: 지금부터 " + EVENT_DURATION_HOURS + "시간 안에 랜덤 레이드보스 [" + _bossName + "]를 잡으세요!");
		
		_eventTask = ThreadPool.schedule(() ->
		{
			Broadcast.toAllOnlinePlayersOnScreen("랜덤보스 이벤트: 시간이 종료되어 랜덤 레이드보스 [" + _bossName + "]가 사라졌습니다!");
			Broadcast.toAllOnlinePlayers("랜덤보스 이벤트: 시간이 종료되어 랜덤 레이드보스 [" + _bossName + "]가 사라졌습니다!");
			eventStop();
		}, despawnDelay);
		
		long randomTime = (Rnd.get(3, 24)) * 3600000;
		long randomMins = (Rnd.get(10, 50)) * 1000;
		long nextTime = randomTime + randomMins;
		
		long spawnTimes = nextTime + System.currentTimeMillis();
		GlobalVariablesManager.getInstance().set("Random_Boss_Spawn", spawnTimes);
		ThreadPool.schedule(this::eventStart, nextTime);
		// String date = BorinetUtil.dataDateFormatKor.format(new Date(spawnTimes));
		
		// LOG.info("랜덤보스 이벤트가 시작되었습니다.");
		// LOG.info("보스: [" + _bossName + "] 위치: [" + _location + "] 좌표: x=" + randomLoc.getX() + " y=" + randomLoc.getY() + " z=" + randomLoc.getZ());
		// LOG.info("다음 이벤트 시간: " + date);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (EVENT_ACTIVE)
		{
			player.sendMessage("랜덤 레이드보스 [" + _bossName + "]가 " + _location + " 주변에서 배회중입니다!");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "랜덤 레이드보스 [" + _bossName + "]가 " + _location + " 주변에서 배회중입니다!"));
		}
	}
	
	private void eventStop()
	{
		if (!EVENT_ACTIVE)
		{
			return;
		}
		
		EVENT_ACTIVE = false;
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		
		for (Npc npc : _npcs)
		{
			if (npc != null)
			{
				npc.deleteMe();
			}
		}
		_npcs.clear();
		_counts = 0;
		
		cancelQuestTimer("CHECK_ATTACK", null, null);
		// LOG.info("랜덤보스 이벤트가 종료되었습니다.");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final long lastattack = EVENT_DURATION_HOURS * 3600000;
		switch (event)
		{
			case "CHECK_ATTACK":
			{
				if ((npc != null) && ((_lastAttack + lastattack) < System.currentTimeMillis()))
				{
					Broadcast.toAllOnlinePlayersOnScreen("랜덤보스 이벤트: 시간이 종료되어 랜덤 레이드보스 [" + _bossName + "]가 사라졌습니다!");
					Broadcast.toAllOnlinePlayers("랜덤보스 이벤트: 시간이 종료되어 랜덤 레이드보스 [" + _bossName + "]가 사라졌습니다!");
					eventStop();
				}
				else if (npc != null)
				{
					startQuestTimer("CHECK_ATTACK", 60000, npc, null);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		_lastAttack = System.currentTimeMillis();
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		
		startQuestTimer("CHECK_ATTACK", 60000, npc, null);
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (EVENT_ACTIVE)
		{
			String[] npcSayings =
			{
				"이 치욕은 반드시 갚아주마!",
				"다음에는 반드시 죽여주마!",
				"더렵혀진 내 명예, 반드시 되찾으리라!",
				"원망과 증오의 힘으로, 너를 저주하노라!",
				"내, 내가 패배하다니!!!",
				"누명을 벗을 때까지는 결코 잠들 수 없다!",
				"비록...이렇게 허무하게 사라지더라도...너희들은 내가 내린 저주에서 평생을 고통받으리라..."
			};
			
			String selectedSaying = npcSayings[Rnd.get(0, npcSayings.length - 1)];
			npc.broadcastSay(ChatType.NPC_GENERAL, selectedSaying);
			
			dropitems(npc, killer);
			if (_counts < 3)
			{
				dropitems(npc, killer);
			}
			
			Broadcast.toAllOnlinePlayersOnScreen("랜덤보스 이벤트: 랜덤 레이드보스 [" + _bossName + "]가 사망하였습니다!");
			Broadcast.toAllOnlinePlayers("랜덤보스 이벤트: 랜덤 레이드보스 [" + _bossName + "]가 사망하였습니다!");
			eventStop();
			// LOG.info(killer.getName() + "님의 파티가 랜덤 레이드보스 [" + _bossName + "]를 사냥하였습니다.");
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public void dropitems(Npc mob, Player player)
	{
		int drop = Rnd.get(3, 4);
		if (Rnd.chance(1) && (_counts < drop))
		{
			mob.dropItem(player, 39331, 1); // 카데이라 방어구 상자
			_counts += 1;
		}
		if (Rnd.chance(2) && (_counts < drop))
		{
			mob.dropItem(player, 39361, 1); // 아포칼립스 무기 상자
			_counts += 1;
		}
		if (Rnd.chance(4) && (_counts < drop))
		{
			mob.dropItem(player, 22339, 1); // S80 최상급 무기상자
			_counts += 1;
		}
		if (Rnd.chance(5) && (_counts < drop))
		{
			mob.dropItem(player, 22340, 1); // 엘레기아 방어구 상자
			_counts += 1;
		}
		if (Rnd.chance(6) && (_counts < drop))
		{
			mob.dropItem(player, 33478, 1); // 파멸 R데이
			_counts += 1;
		}
		if (Rnd.chance(7) && (_counts < drop))
		{
			mob.dropItem(player, 22202, 1); // S80 상급 무기 상자
			_counts += 1;
		}
		if (Rnd.chance(8) && (_counts < drop))
		{
			mob.dropItem(player, 22203, 1); // 버페스 방어구 상자
			_counts += 1;
		}
		if (Rnd.chance(9) && (_counts < drop))
		{
			mob.dropItem(player, 17069, 1); // 베스페르 무기 상자
			_counts += 1;
		}
		if (Rnd.chance(10) && (_counts < drop))
		{
			mob.dropItem(player, 17070, 1); // 이카루스 무기 상자
			_counts += 1;
		}
		if (Rnd.chance(11) && (_counts < drop))
		{
			mob.dropItem(player, 33478, Rnd.get(1, 2)); // 파멸 R 데이
			_counts += 1;
		}
		if (Rnd.chance(12) && (_counts < drop))
		{
			mob.dropItem(player, 22221, Rnd.get(1, 2)); // 파멸 S 데이
			_counts += 1;
		}
		if (Rnd.chance(15) && (_counts < drop))
		{
			mob.dropItem(player, 22222, Rnd.get(1, 3)); // 파멸 S 젤
			_counts += 1;
		}
		if (Rnd.chance(20) && (_counts < drop))
		{
			mob.dropItem(player, 90138, Rnd.get(1, 2)); // 찬란한 펜던트 연마제
			_counts += 1;
		}
		if (Rnd.chance(23) && (_counts < drop))
		{
			mob.dropItem(player, 41033, Rnd.get(1, 3)); // 파멸 레어 액세
			_counts += 1;
		}
		if (Rnd.chance(30) && (_counts < drop))
		{
			mob.dropItem(player, 13082, Rnd.get(1, 3)); // 축복의 깃털 3장 펙
			_counts += 1;
		}
		if (Rnd.chance(50) && (_counts < drop))
		{
			mob.dropItem(player, 41078, Rnd.get(5, 10)); // 최상급 생명의 돌
			_counts += 1;
		}
		if (Rnd.chance(60) && (_counts < drop))
		{
			mob.dropItem(player, 90015, Rnd.get(5, 15)); // 최상급 생명의 돌
			_counts += 1;
		}
		if (Rnd.chance(90) && (_counts < drop))
		{
			mob.dropItem(player, 70005, Rnd.get(1, 2)); // 봉인된 룬 - 6
			_counts += 1;
		}
		if (Rnd.chance(96) && (_counts < drop))
		{
			mob.dropItem(player, 20033, Rnd.get(1, 2)); // 자유 텔레포트 주문서
			_counts += 1;
		}
		if (Rnd.chance(97) && (_counts < drop))
		{
			mob.dropItem(player, 13021, Rnd.get(1, 2)); // 컬러 호칭
			_counts += 1;
		}
		if (Rnd.chance(98) && (_counts < drop))
		{
			mob.dropItem(player, 13016, Rnd.get(1, 2)); // 자유 텔레포트 주문서
			_counts += 1;
		}
		if (Rnd.chance(99) && (_counts < drop))
		{
			mob.dropItem(player, 13015, Rnd.get(1, 5)); // 자유 텔레포트의 서
			_counts += 1;
		}
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setRandomWalking(false);
		return super.onSpawn(npc);
	}
	
	private enum EventLocation
	{
		PERMEL1("페르멜 채집장", -70028, 117685, -3489),
		PERMEL2("페르멜 채집장", -62260, 112212, -3489),
		PERMEL3("페르멜 채집장", -67074, 104399, -3745),
		PERMEL4("페르멜 채집장", -59815, 101559, -3681),
		WINDMIL1("풍차의 언덕", -72628, 171461, -3489),
		WINDMIL2("풍차의 언덕", -77653, 170417, -3553),
		WINDMIL3("풍차의 언덕", -75418, 163810, -3489),
		WINDMIL4("풍차의 언덕", -70315, 166646, -3617),
		TUREKORK1("투렉 오크 야영지", -96413, 107607, -3473),
		TUREKORK2("투렉 오크 야영지", -98115, 113415, -3585),
		TUREKORK3("투렉 오크 야영지", -89089, 114783, -3456),
		TUREKORK4("투렉 오크 야영지", -92030, 111516, -3665),
		BADLANDS1("황무지", -24801, 191635, -4129),
		BADLANDS2("황무지", -17495, 189209, -4161),
		BADLANDS3("황무지", -14317, 181153, -4145),
		BADLANDS4("황무지", -21398, 177863, -4177),
		CRUMA1("크루마 습지", 12819, 126348, -3624),
		CRUMA2("크루마 습지", 18337, 121954, -3592),
		CRUMA3("크루마 습지", 20481, 114023, -3696),
		CRUMA4("크루마 습지", 18779, 104089, -3688),
		CRUMA5("크루마 습지", 11978, 102070, -3579),
		EXECUTION1("처형터", 43816, 147637, -3640),
		EXECUTION2("처형터", 48715, 147332, -3393),
		EXECUTION3("처형터", 45888, 140560, -3297),
		EXECUTION4("처형터", 53359, 144138, -3841),
		EXECUTION5("처형터", 52970, 148496, -2417),
		EXECUTION6("처형터", 54487, 152576, -2401),
		EXECUTION7("처형터", 47418, 152720, -2800),
		FLORAN1("플로란 개간지", 12678, 159065, -3056),
		FLORAN2("플로란 개간지", 13788, 165907, -3656),
		FLORAN3("플로란 개간지", 8582, 164311, -3608),
		CROCODILE1("악어의 섬", 110067, 174718, -3377),
		CROCODILE2("악어의 섬", 124855, 169408, -3552),
		CROCODILE3("악어의 섬", 119811, 172830, -3720),
		CROCODILE4("악어의 섬", 118115, 184194, -3736),
		CROCODILE5("악어의 섬", 112892, 185317, -3345),
		DEATHGIRAN1("죽음의 회랑", 72320, 125419, -3633),
		DEATHGIRAN2("죽음의 회랑", 64979, 122253, -3633),
		DEATHGIRAN3("죽음의 회랑", 70845, 114589, -3681),
		HADINGIRAN("하딘의 사숙", 104156, 107096, -3248),
		GORGONGIRAN1("고르곤의 화원", 114766, 133517, -3089),
		GORGONGIRAN2("고르곤의 화원", 109669, 133024, -3400),
		GORGONGIRAN3("고르곤의 화원", 106104, 136597, -3473),
		PLAINADEN1("격전의 평원", 154838, 9218, -4116),
		PLAINADEN2("격전의 평원", 157948, 4762, -4401),
		PLAINADEN3("격전의 평원", 161081, 10837, -3745),
		PLAINADEN4("격전의 평원", 161045, 16225, -3793),
		PLAINADEN5("격전의 평원", 155280, 16932, -3873),
		MASSACREADEN1("학살의 대지", 179795, -11156, -3424),
		MASSACREADEN2("학살의 대지", 183250, -5262, -2944),
		MASSACREADEN3("학살의 대지", 170243, -7993, -2641),
		MASSACREADEN4("학살의 대지", 165851, -5523, -2928),
		VALLEYADEN1("고요한 분지", 170553, 54159, -4576),
		VALLEYADEN2("고요한 분지", 176058, 57904, -5840),
		VALLEYADEN3("고요한 분지", 181883, 55851, -5836),
		VALLEYADEN4("고요한 분지", 186824, 47452, -5944),
		VALLEYADEN5("고요한 분지", 189837, 46459, -4200),
		GLORYADEN1("영광의 평원", 138068, 24190, -2817),
		GLORYADEN2("영광의 평원", 135852, 16368, -3665),
		GLORYADEN3("영광의 평원", 135258, 11936, -4113),
		GLORYADEN4("영광의 평원", 133150, 6620, -4337),
		GLORYADEN5("영광의 평원", 139004, 478, -4401),
		ARGOSGODDARD1("아르고스의 벽", 172112, -50303, -3504),
		ARGOSGODDARD2("아르고스의 벽", 173358, -47643, -3360),
		ARGOSGODDARD3("아르고스의 벽", 168951, -39372, -3536),
		ARGOSGODDARD4("아르고스의 벽", 170229, -35900, -3329),
		ARGOSGODDARD5("아르고스의 벽", 166925, -49372, -3569),
		ARGOSGODDARD6("아르고스의 벽", 178542, -57686, -3136),
		ARGOSGODDARD7("아르고스의 벽", 170461, -53916, -3456),
		LIZARDMENOREN1("도마뱀 초원", 84812, 86519, -3016),
		LIZARDMENOREN2("도마뱀 초원", 81126, 81959, -3537),
		LIZARDMENOREN3("도마뱀 초원", 83422, 76140, -3697),
		LIZARDMENOREN4("도마뱀 초원", 85834, 73308, -3368),
		LIZARDMENOREN5("도마뱀 초원", 91979, 73243, -3697),
		LIZARDMENOREN6("도마뱀 초원", 91184, 77784, -3665),
		LIZARDMENOREN7("도마뱀 초원", 87438, 83385, -3073),
		SPORESOREN1("포자의 바다", 60324, 25682, -4208),
		SPORESOREN2("포자의 바다", 58578, 21719, -5200),
		SPORESOREN3("포자의 바다", 59036, 17598, -4706),
		SPORESOREN4("포자의 바다", 56733, 12999, -5248),
		SPORESOREN5("포자의 바다", 46931, 24018, -5088),
		SPORESOREN6("포자의 바다", 51326, 26160, -5008),
		FORESTOREN1("무법사의 삼림", 88449, -9641, -2193),
		FORESTOREN2("무법사의 삼림", 88811, -7694, -3073),
		FORESTOREN3("무법사의 삼림", 81040, -5940, -2977),
		FORESTOREN4("무법사의 삼림", 85353, -127, -3713),
		FORESTOREN5("무법사의 삼림", 92556, 1277, -3713),
		FORESTOREN6("무법사의 삼림", 97880, -3519, -3665),
		ELFOREN1("엘프의 숲", 13079, 54532, -3489),
		ELFOREN2("엘프의 숲", 8958, 48365, -3617),
		ELFOREN3("엘프의 숲", 11547, 44304, -3665),
		ELFOREN4("엘프의 숲", 20207, 39886, -3521),
		ELFOREN5("엘프의 숲", 25977, 40999, -3649),
		ELFOREN6("엘프의 숲", 30493, 49670, -3685),
		VALLEYHV1("사냥꾼의 계곡", 110656, 87631, -2897),
		VALLEYHV2("사냥꾼의 계곡", 108603, 90829, -2576),
		VALLEYHV3("사냥꾼의 계곡", 114717, 90427, -2464),
		VALLEYHV4("사냥꾼의 계곡", 122671, 89107, -2961),
		VALLEYHV5("사냥꾼의 계곡", 117292, 89205, -3809);
		
		private final String _name;
		private final int _x;
		private final int _y;
		private final int _z;
		
		EventLocation(String name, int x, int y, int z)
		{
			_name = name;
			_x = x;
			_y = y;
			_z = z;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public int getX()
		{
			return _x;
		}
		
		public int getY()
		{
			return _y;
		}
		
		public int getZ()
		{
			return _z;
		}
	}
	
	private void recordSpawn(Collection<Npc> npcs, int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay)
	{
		final RaidBoss npc = (RaidBoss) addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay);
		npcs.add(npc);
	}
	
	public static void main(String[] args)
	{
		new BossEvent();
	}
}
