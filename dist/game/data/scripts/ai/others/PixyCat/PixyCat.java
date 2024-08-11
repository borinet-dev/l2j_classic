package ai.others.PixyCat;

import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class PixyCat extends AbstractNpcAI
{
	protected static final Logger LOGGER = Logger.getLogger(PixyCat.class.getName());
	// NPC
	private static final int 픽시캣 = 40030;
	
	// Locations
	static String _location;
	
	// Misc
	private static final int DESPAWN_INTERVAL = 300000; // 5 minutes
	private static final int RANDOM_RESPAWN_INTERVAL_MIN = 1800000; // 30 minutes
	private static final int RANDOM_RESPAWN_INTERVAL_MAX = 3600000; // 60 minutes
	private static Npc _CAT;
	
	private PixyCat()
	{
		if (!BorinetTask._isActive)
		{
			addStartNpc(픽시캣);
			addFirstTalkId(픽시캣);
			addTalkId(픽시캣);
			startSpawnCycle();
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		if (event.equals("SPAWN_PIXYCAT"))
		{
			spawnCat();
			startQuestTimer("DESPAWN_PIXYCAT", DESPAWN_INTERVAL, null, null, false);
		}
		else if (event.equals("DESPAWN_PIXYCAT"))
		{
			despawnCat();
			int randomRespawnInterval = Rnd.get(RANDOM_RESPAWN_INTERVAL_MIN, RANDOM_RESPAWN_INTERVAL_MAX);
			startQuestTimer("SPAWN_PIXYCAT", randomRespawnInterval, null, null, false);
		}
		else if (event.equals("RANDOM_WALK") && (npc != null))
		{
			final Location randomLoc = Util.getRandomPosition(npc.getSpawn().getLocation(), 0, 200);
			addMoveToDesire(npc, GeoEngine.getInstance().getValidLocation(npc.getX(), npc.getY(), npc.getZ(), randomLoc.getX(), randomLoc.getY(), randomLoc.getZ(), npc.getInstanceWorld()), 23);
			startQuestTimer("RANDOM_WALK", getRandom(5000, 10000), npc, null);
		}
		
		switch (event)
		{
			case "give_buff":
			{
				if (getQuestItemsCount(player, 57) < 10000000)
				{
					int fee = 10000000;
					player.sendMessage("버프를 받기 위해서는 " + Util.formatAdena(fee) + " 아데나가 필요합니다.");
					htmltext = getHtm(player, "40030-noAdena.htm");
					htmltext = htmltext.replace("%fee%", Util.formatAdena(fee));
				}
				else
				{
					int randomValue = Rnd.get(100);
					int skillId = 30287;
					takeItems(player, 57, 10000000);
					
					if (randomValue < 70)
					{
						skillId = 30288;
					}
					
					final Skill GiftOfVitality = SkillData.getInstance().getSkill(skillId, 1);
					GiftOfVitality.applyEffects(player, player, false, 3600);
					htmltext = "40030-" + skillId + ".htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "40030.htm";
	}
	
	private void startSpawnCycle()
	{
		final long currentTime = System.currentTimeMillis();
		
		final Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MILLISECOND, 0);
		
		int currentMinute = startTime.get(Calendar.MINUTE);
		int nextMinute = ((currentMinute / 5) * 5) + 5;
		
		if (nextMinute >= 60)
		{
			nextMinute = 0;
			startTime.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		startTime.set(Calendar.MINUTE, nextMinute);
		
		// 현재 시간부터 startTime까지의 차이 계산
		long delay = startTime.getTimeInMillis() - currentTime;
		
		// delay가 음수가 되지 않도록 보정
		if (delay < 0)
		{
			delay += DESPAWN_INTERVAL; // 5분 = 300000ms
		}
		
		// 지정된 시간에 실행하도록 타이머 시작
		startQuestTimer("SPAWN_PIXYCAT", delay, null, null, false);
	}
	
	private void spawnCat()
	{
		despawnCat();
		
		final SpawnLocation randomLoc = getRandomEntry(SpawnLocation.values());
		_CAT = addSpawn(픽시캣, randomLoc.getX(), randomLoc.getY(), randomLoc.getZ(), randomLoc.getH(), false, DESPAWN_INTERVAL);
		_location = randomLoc.getName();
		startQuestTimer("RANDOM_WALK", getRandom(5000, 10000), _CAT, null);
		Broadcast.toAllOnlinePlayersOnScreen("행운의 사절 픽시캣이 5분간 " + _location + " 주변에서 배회합니다!");
		// LOGGER.info("행운의 사절 픽시캣이 " + _location + " 주변에 나타났어요!");
	}
	
	private void despawnCat()
	{
		if (_CAT != null)
		{
			_CAT.deleteMe();
			_CAT = null;
			// Broadcast.toAllOnlinePlayersOnScreen("픽시캣이 사라졌습니다.");
		}
	}
	
	private enum SpawnLocation
	{
		LOCATION_1("랑크 리자드맨 서식지", -44827, 202577, 3553, 31175),
		LOCATION_2("페르멜 채집장", -67726, 119805, 3601, 31151),
		LOCATION_3("버려진 야영지", -49937, 146571, -2769, 19238),
		LOCATION_4("투렉오크 야영지", -90657, 106369, -3752, 39662),
		LOCATION_5("풍차의 언덕", -76691, 166703, -3633, 5959),
		LOCATION_6("황무지", -23834, 186151, -4305, 16749),
		LOCATION_7("윈다우드 장원", -28873, 153922, -3473, 8352),
		LOCATION_8("크루마 습지", 5093, 126260, -3656, 9825),
		LOCATION_9("디온 구릉지", 31511, 141963, -3176, 28271),
		LOCATION_10("저항군의 아지트", 48145, 110594, -2112, 23457),
		LOCATION_11("처형터", 51274, 141631, -2833, 5315),
		LOCATION_12("플로란 개간지", 9803, 161649, -3553, 21379),
		LOCATION_13("악어의 섬", 115543, 191544, -3360, 17288),
		LOCATION_14("악마섬", 43929, 206389, -3760, 26941),
		LOCATION_15("브래카의 소굴", 85266, 131612, -3665, 61854),
		LOCATION_16("하딘의 사숙", 105407, 109767, -3152, 60900),
		LOCATION_17("용의 계곡", 73371, 118354, -3704, 39962),
		LOCATION_18("바람의 대지", 178029, 173198, -2000, 38276),
		LOCATION_19("앙헬 폭포", 165858, 85176, -2216, 12106),
		LOCATION_20("격전의 평원", 157449, 11230, -4033, 29412),
		LOCATION_21("학살의 대지", 183881, -15515, -2688, 30758),
		LOCATION_22("거인들의 동굴 입구", 174471, 52961, -4360, 50151),
		LOCATION_23("영광의 평원", 137482, 22304, -3592, 36991),
		LOCATION_24("케트라 오크 전진기지", 149960, -82140, -5584, 23095),
		LOCATION_25("바르카 실레노스 주둔지", 107731, 53755, -2432, 65304),
		LOCATION_26("온천지대", 149409, -112645, 2064, 7579),
		LOCATION_27("도마뱀 초원", 87637, 84836, -3072, 18284),
		LOCATION_28("포자의 바다", 62268, 30017, -3744, 6894),
		LOCATION_29("무법자의 삼림", 91993, -11964, -2401, 31654),
		LOCATION_30("권능의 교장", 87518, 61677, -3649, 42086),
		LOCATION_31("엘프의 지하요새", 28774, 75196, -3776, 64183),
		LOCATION_32("페어리의 계곡 동쪽", 104177, 33796, -3841, 59956),
		LOCATION_33("거울의 숲", 145792, 91784, -3728, 0),
		LOCATION_34("사냥꾼의 계곡", 114138, 86547, -3120, 18219),
		LOCATION_35("실렌의 봉인", 186500, 21169, -3480, 60534),
		LOCATION_36("잊혀진 섬 선착장", 11461, -23408, -3648, 44109),
		LOCATION_37("비하이브", 20996, 188604, -3408, 21037),
		LOCATION_38("개미굴 입구", -9670, 176016, -4129, 20717);
		
		private final String _name;
		private final int _x;
		private final int _y;
		private final int _z;
		private final int _h;
		
		SpawnLocation(String name, int x, int y, int z, int h)
		{
			_name = name;
			_x = x;
			_y = y;
			_z = z;
			_h = h;
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
		
		public int getH()
		{
			return _h;
		}
	}
	
	public static void main(String[] args)
	{
		new PixyCat();
	}
}
