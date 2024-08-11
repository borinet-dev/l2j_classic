package custom.GoldenPig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
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

public class GoldenPig extends AbstractNpcAI
{
	private final List<Npc> _npcs;
	private static final Logger LOG = Logger.getLogger(GoldenPig.class.getName());
	private static final int GOLDEN_PIG = 25735;
	
	static String _location;
	private static final int EVENT_DURATION_HOURS = 1;
	private static boolean EVENT_ACTIVE = false;
	private static final int[] HP_PERCENT_THRESHOLDS =
	{
		90,
		80,
		70,
		60,
		50,
		40,
		30,
		20,
		10
	};
	private final boolean[] rewardDropped = new boolean[HP_PERCENT_THRESHOLDS.length];
	private ScheduledFuture<?> eventTask = null;
	private ScheduledFuture<?> spawnTask = null;
	private final List<Npc> npcs = new ArrayList<>();
	private boolean _setLevel = false;
	
	public GoldenPig()
	{
		eventStop();
		if (spawnTask != null)
		{
			spawnTask.cancel(true);
			spawnTask = null;
		}
		
		_npcs = new ArrayList<>();
		addSpawnId(GOLDEN_PIG);
		addKillId(GOLDEN_PIG);
		addAttackId(GOLDEN_PIG);
		
		if (!Config.GOLDEN_PIG_ENABLE)
		{
			return;
		}
		
		if (BorinetTask.getInstance().GoldenPigStart().getTimeInMillis() > System.currentTimeMillis())
		{
			spawnTask = ThreadPool.schedule(this::initializeEvent, BorinetTask.getInstance().GoldenPigStart().getTimeInMillis() - System.currentTimeMillis());
		}
		else if (BorinetTask.getInstance().GoldenPig())
		{
			initializeEvent();
		}
	}
	
	private void initializeEvent()
	{
		long randomTime = (Rnd.get(1, 2)) * 1800000;
		long randomMins = (Rnd.get(10, 30)) * 1000;
		long nextTime = randomTime + randomMins;
		
		if (GlobalVariablesManager.getInstance().hasVariable("Golden_Pig_Spawn"))
		{
			long spawnTimes = GlobalVariablesManager.getInstance().getLong("Golden_Pig_Spawn");
			if (spawnTimes > System.currentTimeMillis())
			{
				final long setTime = spawnTimes - System.currentTimeMillis();
				spawnTask = ThreadPool.schedule(this::eventStart, setTime);
			}
			else
			{
				spawnTask = ThreadPool.schedule(this::eventStart, BorinetTask.GoldenPigEventDelay() + nextTime);
			}
		}
		else
		{
			spawnTask = ThreadPool.schedule(this::eventStart, BorinetTask.GoldenPigEventDelay() + nextTime);
		}
		LOG.info("커스텀 이벤트: 초거대 황금 돼지를 로드하였습니다.");
	}
	
	private void eventStart()
	{
		if (EVENT_ACTIVE || !Config.GOLDEN_PIG_ENABLE || !BorinetTask.getInstance().GoldenPig())
		{
			return;
		}
		EVENT_ACTIVE = true;
		
		final EventLocation randomLoc = getRandomEntry(EventLocation.values());
		final long despawnDelay = EVENT_DURATION_HOURS * 5 * 60 * 1000;
		recordSpawn(GOLDEN_PIG, randomLoc.getX(), randomLoc.getY(), randomLoc.getZ(), 0, true, 0);
		
		_location = randomLoc.getName();
		
		Broadcast.toAllOnlinePlayersOnScreen("황금 돼지 이벤트: 초거대 황금 돼지가 " + _location + " 주변에 나타났어요!");
		Broadcast.toAllOnlinePlayers("황금 돼지 이벤트: 초거대 황금 돼지가 " + _location + " 주변에 나타났어요!");
		Broadcast.toAllOnlinePlayers("황금 돼지 이벤트: 지금부터 5분 안에 초거대 황금 돼지를 잡으세요!");
		
		eventTask = ThreadPool.schedule(() ->
		{
			Broadcast.toAllOnlinePlayersOnScreen("황금 돼지 이벤트: 시간이 종료되어  초거대 황금 돼지가 사라졌습니다!");
			Broadcast.toAllOnlinePlayers("황금 돼지 이벤트: 시간이 종료되어  초거대 황금 돼지가 사라졌습니다!");
			eventStop();
		}, despawnDelay);
		
		long randomTime = (Rnd.get(1, 2)) * 1800000;
		long randomMins = (Rnd.get(10, 30)) * 1000;
		long nextTime = randomTime + randomMins;
		
		long spawnTimes = nextTime + System.currentTimeMillis();
		GlobalVariablesManager.getInstance().set("Golden_Pig_Spawn", spawnTimes);
		spawnTask = ThreadPool.schedule(this::eventStart, nextTime);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (EVENT_ACTIVE)
		{
			player.sendMessage("초거대 황금 돼지가 " + _location + " 주변에서 배회중입니다!");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "초거대 황금 돼지가 " + _location + " 주변에서 배회중입니다!"));
		}
	}
	
	private void eventStop()
	{
		if (!EVENT_ACTIVE)
		{
			return;
		}
		EVENT_ACTIVE = false;
		if (eventTask != null)
		{
			eventTask.cancel(true);
			eventTask = null;
		}
		
		for (Npc npc : npcs)
		{
			if (npc != null)
			{
				npc.deleteMe();
			}
		}
		npcs.clear();
		
		for (int i = 0; i < rewardDropped.length; i++)
		{
			rewardDropped[i] = false;
		}
		_setLevel = false;
	}
	
	private void dropRewardByHpPercent(Npc npc, Player attacker)
	{
		final double currentHpPercent = (npc.getCurrentHp() / npc.getMaxHp()) * 100;
		for (int i = 0; i < HP_PERCENT_THRESHOLDS.length; i++)
		{
			if (!rewardDropped[i] && (currentHpPercent <= HP_PERCENT_THRESHOLDS[i]))
			{
				rewardDropped[i] = true;
				dropitems(npc, attacker);
				break;
			}
		}
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		if ((npc == null) || npc.isDead() || (attacker == null))
		{
			return null;
		}
		
		if (!npcs.contains(npc))
		{
			npcs.add(npc);
		}
		
		if (!_setLevel)
		{
			npc.setLevel((byte) (attacker.getLevel() + Rnd.get(2, 5)));
			_setLevel = true;
		}
		
		double reducehp = Rnd.get(400, 1300);
		double currenthp = npc.getCurrentHp();
		double reduce = currenthp - reducehp;
		npc.setCurrentHp(reduce);
		
		dropRewardByHpPercent(npc, attacker);
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (EVENT_ACTIVE)
		{
			dropitems(npc, killer);
			if (Rnd.chance(25))
			{
				npc.dropItem(killer, 41277, 1);
			}
			
			Broadcast.toAllOnlinePlayersOnScreen("황금 돼지 이벤트: 초거대 황금 돼지가 사망하였습니다!");
			Broadcast.toAllOnlinePlayers("황금 돼지 이벤트: 초거대 황금 돼지가 사망하였습니다!");
			eventStop();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void dropitems(Npc mob, Player player)
	{
		int adenaAmount = Rnd.get(100000, 1000000);
		mob.dropItem(player, 57, adenaAmount);
		
		int itemCount = Rnd.get(10, 100);
		mob.dropItem(player, 3031, itemCount);
		
		if (Rnd.chance(20))
		{
			if (Rnd.chance(70))
			{
				int itemId = Rnd.get(41237, 41239); // 바나나 쉐이크 3종
				mob.dropItem(player, itemId, 1);
			}
			else
			{
				if (Rnd.chance(70))
				{
					mob.dropItem(player, 41276, 1); // 탈리스만 교환권
				}
				else
				{
					int itemId = Rnd.chance(50) ? 41234 : 41233; // 무기 강화석, 방어구 강화석
					mob.dropItem(player, itemId, 1);
				}
			}
		}
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setRandomWalking(true);
		return super.onSpawn(npc);
	}
	
	private enum EventLocation
	{
		GOLDEN_PIG_1("투렉 오크 야영지", -89972, 109033, -3425),
		GOLDEN_PIG_2("투렉 오크 야영지", -91423, 112146, -3528),
		GOLDEN_PIG_3("투렉 오크 야영지", -94928, 111100, -3617),
		GOLDEN_PIG_4("투렉 오크 야영지", -96846, 113323, -3617),
		GOLDEN_PIG_5("투렉 오크 야영지", -98165, 117116, -3281),
		GOLDEN_PIG_6("투렉 오크 야영지", -98173, 110667, -3505),
		GOLDEN_PIG_7("투렉 오크 야영지", -90017, 101074, -3441),
		GOLDEN_PIG_8("버려진 야영지", -53921, 146138, -2865),
		GOLDEN_PIG_9("버려진 야영지", -51216, 142222, -2881),
		GOLDEN_PIG_10("버려진 야영지", -50196, 138586, -2897),
		GOLDEN_PIG_11("버려진 야영지", -55943, 136670, -2769),
		GOLDEN_PIG_12("버려진 야영지", -57953, 140542, -2641),
		GOLDEN_PIG_13("버려진 야영지", -59693, 137541, -2321),
		GOLDEN_PIG_14("악어의 섬", 118547, 191131, -3697),
		GOLDEN_PIG_15("악어의 섬", 121411, 188786, -3505),
		GOLDEN_PIG_16("악어의 섬", 121781, 182981, -3265),
		GOLDEN_PIG_17("악어의 섬", 116509, 177144, -3712),
		GOLDEN_PIG_18("악어의 섬", 107142, 176042, -3681),
		GOLDEN_PIG_19("악어의 섬", 105109, 175419, -3713),
		GOLDEN_PIG_20("악어의 섬", 110137, 169139, -3265),
		GOLDEN_PIG_21("악어의 섬", 112543, 172272, -3745),
		GOLDEN_PIG_22("악어의 섬", 119010, 172909, -3713),
		GOLDEN_PIG_23("악어의 섬", 121630, 170660, -3649),
		GOLDEN_PIG_24("바람의 대지", 175723, 171693, -2320),
		GOLDEN_PIG_25("바람의 대지", 175141, 175996, -3248),
		GOLDEN_PIG_26("바람의 대지", 172782, 178777, -2704),
		GOLDEN_PIG_27("바람의 대지", 174531, 183960, -2192),
		GOLDEN_PIG_28("바람의 대지", 178313, 179928, -3040),
		GOLDEN_PIG_29("바람의 대지", 180774, 178094, -3376),
		GOLDEN_PIG_30("바람의 대지", 185294, 179977, -2816),
		GOLDEN_PIG_31("바람의 대지", 184752, 184742, -2464),
		GOLDEN_PIG_32("아르고스의 벽", 172844, -59861, -2856),
		GOLDEN_PIG_33("아르고스의 벽", 177620, -55349, -2895),
		GOLDEN_PIG_34("아르고스의 벽", 180996, -59064, -2596),
		GOLDEN_PIG_35("아르고스의 벽", 172497, -51027, -2964),
		GOLDEN_PIG_36("아르고스의 벽", 173745, -35149, -3159),
		GOLDEN_PIG_37("아르고스의 벽", 176661, -43196, -2942),
		GOLDEN_PIG_38("아르고스의 벽", 179990, -38099, -2669),
		GOLDEN_PIG_39("아르고스의 벽", 190259, -35372, -2497),
		GOLDEN_PIG_40("아르고스의 벽", 193352, -42108, -2519),
		GOLDEN_PIG_41("온천지대", 145247, -110705, -3408),
		GOLDEN_PIG_42("온천지대", 154830, -116488, -1406),
		GOLDEN_PIG_43("온천지대", 154536, -108675, -2606),
		GOLDEN_PIG_44("온천지대", 142670, -107503, -3400),
		GOLDEN_PIG_45("온천지대", 142516, -108793, -3501),
		GOLDEN_PIG_46("온천지대", 157778, -116539, -1809),
		GOLDEN_PIG_47("온천지대", 155563, -108705, -2605),
		GOLDEN_PIG_48("온천지대", 154567, -105599, -2609),
		GOLDEN_PIG_49("케트라 오크 전진기지", 136816, -91872, -3360),
		GOLDEN_PIG_50("케트라 오크 전진기지", 139784, -88088, -4240),
		GOLDEN_PIG_51("케트라 오크 전진기지", 137048, -82296, -3760),
		GOLDEN_PIG_52("케트라 오크 전진기지", 147840, -85036, -4576),
		GOLDEN_PIG_53("케트라 오크 전진기지", 150200, -89944, -4204),
		GOLDEN_PIG_54("케트라 오크 전진기지", 154024, -78452, -4016),
		GOLDEN_PIG_55("케트라 오크 전진기지", 141864, -73616, -3812),
		GOLDEN_PIG_56("케트라 오크 전진기지", 136656, -72704, -3628),
		GOLDEN_PIG_57("케트라 오크 전진기지", 139892, -79008, -3752),
		GOLDEN_PIG_58("바르카 실레노스 주둔지", 124646, -42005, -3505),
		GOLDEN_PIG_59("바르카 실레노스 주둔지", 120352, -46104, -2856),
		GOLDEN_PIG_60("바르카 실레노스 주둔지", 119112, -49484, -3656),
		GOLDEN_PIG_61("바르카 실레노스 주둔지", 121788, -55241, -2314),
		GOLDEN_PIG_62("바르카 실레노스 주둔지", 111057, -43956, -2579),
		GOLDEN_PIG_63("바르카 실레노스 주둔지", 109219, -45983, -2221),
		GOLDEN_PIG_64("바르카 실레노스 주둔지", 118722, -43492, -3216),
		GOLDEN_PIG_65("바르카 실레노스 주둔지", 113965, 43389, -2715),
		GOLDEN_PIG_66("바르카 실레노스 주둔지", 114232, -40874, 2557),
		GOLDEN_PIG_67("잊혀진 섬", 22530, -20008, -2612),
		GOLDEN_PIG_68("잊혀진 섬", 27916, -17405, -2560),
		GOLDEN_PIG_69("잊혀진 섬", 20696, -19670, -3280),
		GOLDEN_PIG_70("잊혀진 섬", 25092, -9738, -2440),
		GOLDEN_PIG_71("잊혀진 섬", 14658, -18974, -3160),
		GOLDEN_PIG_72("잊혀진 섬", 22396, -9470, -2736),
		GOLDEN_PIG_73("잊혀진 섬", 19864, -9248, -2784),
		GOLDEN_PIG_74("잊혀진 섬", 7384, -18219, -3600),
		GOLDEN_PIG_75("잊혀진 섬", 3948, -18289, -3576),
		GOLDEN_PIG_76("잊혀진 섬", 5280, -6285, -3410),
		GOLDEN_PIG_77("오만의 탑 2층", 114627, 16017, -3616),
		GOLDEN_PIG_78("오만의 탑 2층", 113440, 17312, -3616),
		GOLDEN_PIG_79("오만의 탑 2층", 113586, 14792, -3616),
		GOLDEN_PIG_80("오만의 탑 2층", 115850, 15062, -3616),
		GOLDEN_PIG_81("오만의 탑 2층", 115853, 17287, -3616),
		GOLDEN_PIG_82("오만의 탑 3층", 114758, 16039, -2128),
		GOLDEN_PIG_83("오만의 탑 3층", 115940, 17345, -2128),
		GOLDEN_PIG_84("오만의 탑 3층", 113377, 14762, -2128),
		GOLDEN_PIG_85("오만의 탑 3층", 116183, 14669, -2128),
		GOLDEN_PIG_86("오만의 탑 4층", 113283, 17557, -640),
		GOLDEN_PIG_87("오만의 탑 4층", 113017, 14653, -640),
		GOLDEN_PIG_88("오만의 탑 4층", 115975, 14580, -640),
		GOLDEN_PIG_89("오만의 탑 5층", 114559, 16668, 928),
		GOLDEN_PIG_90("오만의 탑 6층", 115776, 17286, 1936),
		GOLDEN_PIG_91("오만의 탑 7층", 114278, 16116, 2992),
		GOLDEN_PIG_92("미스릴 광산", 178947, -180820, 362),
		GOLDEN_PIG_93("미스릴 광산", 173759, -174553, 3644),
		GOLDEN_PIG_94("미스릴 광산", 180044, -172336, -88),
		GOLDEN_PIG_95("미스릴 광산", 182273, -179491, -824),
		GOLDEN_PIG_96("미스릴 광산", 176873, -177296, 554),
		GOLDEN_PIG_97("미스릴 광산", 180610, -175836, -3420),
		GOLDEN_PIG_98("미스릴 광산", 185804, -182114, -3042),
		GOLDEN_PIG_99("미스릴 광산", 186180, -187090, -3020),
		GOLDEN_PIG_100("엘프의 지하요새", 13600, 80013, -4080),
		GOLDEN_PIG_101("엘프의 지하요새", 13031, 76597, -4272),
		GOLDEN_PIG_102("엘프의 지하요새", 17489, 81010, -4352),
		GOLDEN_PIG_103("엘프의 지하요새", 18627, 76973, -4384),
		GOLDEN_PIG_104("엘프의 지하요새", 24813, 75067, -4096),
		GOLDEN_PIG_105("엘프의 지하요새", 10747, 82249, -3840),
		GOLDEN_PIG_106("엘프의 지하요새", 14994, 81142, -4096),
		GOLDEN_PIG_107("엘프의 지하요새", 22150, 79274, -4096),
		GOLDEN_PIG_108("엘프의 지하요새", 22968, 76083, -4096),
		GOLDEN_PIG_109("엘프의 지하요새", 9635, 78619, -3808);
		
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
	
	private Npc recordSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay)
	{
		final Npc npc = addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay);
		if (npc != null)
		{
			_npcs.add(npc);
		}
		return npc;
	}
	
	public static void main(String[] args)
	{
		new GoldenPig();
	}
}
