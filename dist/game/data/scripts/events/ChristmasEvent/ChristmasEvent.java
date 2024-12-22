package events.ChristmasEvent;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Broadcast;

public class ChristmasEvent extends LongTimeEvent
{
	private static final Logger LOG = Logger.getLogger(ChristmasEvent.class.getName());
	private static final int 산타클로스 = 13185;
	private static final int 눈사람 = 13160;
	private static final int 칠면조 = 13183;
	private static final int EVENT_DURATION_TIMES = 10;
	private static Npc _snowman;
	// private static List<Npc> _santa;
	private static final List<Npc> _santa = new CopyOnWriteArrayList<>();
	private static Creature _thomas;
	private static final List<Integer> CHRONO_LIST = Arrays.asList(4202, 5133, 5817, 7058, 8350);
	private static final int SANTA_BUFF_REUSE = 2 * 3600 * 1000; // 2 hours
	private static final int SANTA_LOTTERY_REUSE = 2 * 3600 * 1000; // 2 hours
	private static final SkillHolder BUFF = new SkillHolder(23017, 1);
	private static final SkillHolder SANTA_BUFF = new SkillHolder(6120, 1);
	private static final SkillHolder THOMAS_BUFF = new SkillHolder(23018, 1);
	public static String loc;
	public static SnowmanState _snowmanState;
	
	public static enum SnowmanState
	{
		CAPTURED,
		FAILED,
		SAVED;
	}
	
	private long _lastSay;
	private static boolean EVENT_START = false;
	private ScheduledFuture<?> _eventTask = null;
	
	public ChristmasEvent()
	{
		if ((Calendar.getInstance().get(Calendar.MONTH) != Calendar.DECEMBER) || BorinetTask._isActive)
		{
			return;
		}
		addFirstTalkId(산타클로스);
		addTalkId(산타클로스);
		addSpawnId(산타클로스, 눈사람);
		addKillId(칠면조);
		addAttackId(칠면조);
		
		if (BorinetTask.getInstance().ChristmasEventStart().getTimeInMillis() > System.currentTimeMillis())
		{
			ThreadPool.schedule(this::spawnNpc, BorinetTask.getInstance().ChristmasEventStart().getTimeInMillis() - System.currentTimeMillis());
		}
		else if ((BorinetTask.getInstance().ChristmasEventStart().getTimeInMillis() <= System.currentTimeMillis()) && (BorinetTask.getInstance().ChristmasEventEnd().getTimeInMillis() > System.currentTimeMillis()))
		{
			_snowmanState = SnowmanState.SAVED;
			final long randomTime = (getRandom(1, 2)) * 3600000;
			ThreadPool.schedule(this::eventStart, randomTime);
			ThreadPool.schedule(this::eventEnd, BorinetTask.getInstance().ChristmasEventEnd().getTimeInMillis() - System.currentTimeMillis());
			LOG.info("커스텀 이벤트: 크리스마스 이벤트를 로드하였습니다.");
		}
	}
	
	private static final String[] GOOD =
	{
		"호, 호, 호~ 모두 즐거운 성탄절 보내시게나~",
		"새해에도 " + Config.SERVER_NAME_KOR + "과 함께!!",
		"메리 크리스마스! 호, 호, 호.",
		"나에게 오게. 버프와 푸짐한 선물이 준비되어 있다네.",
		"Merry Christmas & Happy New year",
		"호, 호, 호! 메리 크리스마스!"
	};
	
	public static final String[] CAPTURED =
	{
		"눈사람이 납치되었네!",
		"이보게. 눈사람 좀 구해주지 않겠나?",
		"눈사람이 걱정되어 당분간 서비스를 할 수 없다네.",
		"나의 귀여운 눈사람을 칠면조놈이 납치해갔네!"
	};
	
	private static final String[] BAD =
	{
		"눈사람도 구해주지 않는 무능한놈들!!",
		"은혜를 원수로 갚다니...",
		"기분이 매우 안좋으니 나에게 말걸지 말게!",
		"자네들은 영웅이라 불릴 자격이 없어!"
	};
	
	private static final String[] attacked =
	{
		"크하하하! 눈사람을 녹여버릴것이다!!",
		"새해는 오지 않을것이다!",
		"모두 죽여주마!",
		"이거로 되겠어? 하 ... 하 ...",
		"영웅으로 불리고 싶은가?",
		"눈사람을 절대 구할 수 없을거야!"
	};
	
	private static final String[] noWeapon =
	{
		"크하하하! 맨손으로 나를 잡겠다고?",
		"무기를 들어라!"
	};
	
	private static final String[] noCrono =
	{
		"크하하하! 그런 무기로 나를 죽일 수 있을거라 생각하는가!",
		"크로노 무기로만 나에게 대미지를 줄 수 있지!"
	};
	
	// @formatter:off
	private static final int WEAPONSC[] = { 20123, 20124, 20125, 20126, 20127, 20128, 20129, 20130, 20131, 20132, 20133 }; // C
	private static final int WEAPONSB[] = { 20137, 20138, 20139, 20140, 20141, 20142, 20143, 20144, 20145, 20146, 20147 }; // B
	private static final int WEAPONSA[] = { 20151, 20152, 20153, 20154, 20155, 20156, 20157, 20158, 20159, 20160, 20161 }; // A
	private static final int WEAPONSS[] = { 41035, 41036, 41039, 41041, 41045, 41042, 41044, 41043, 41038, 41040, 41046 }; // S
	private static final int WEAPONSS80[] = { 41048, 41052, 41049, 41050, 41051, 41055, 41056, 41053, 41058, 41054, 41059 }; // S80

	// @formatter:on
	private void spawnNpc()
	{
		_snowmanState = SnowmanState.SAVED;
		final long randomTime = (getRandom(1, 2)) * 3600000;
		ThreadPool.schedule(this::eventStart, randomTime);
		ThreadPool.schedule(this::eventEnd, BorinetTask.getInstance().ChristmasEventEnd().getTimeInMillis() - System.currentTimeMillis());
		LOG.info("커스텀 이벤트: 크리스마스 이벤트를 로드하였습니다.");
	}
	
	private void eventEnd()
	{
		if ((_santa == null) || _santa.isEmpty())
		{
			return;
		}
		
		for (Npc npc : _santa)
		{
			if (npc != null)
			{
				try
				{
					npc.deleteMe();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		_santa.clear();
		
		EVENT_START = false;
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		
		if (_thomas != null)
		{
			_thomas.deleteMe();
		}
		if (_snowman != null)
		{
			_snowman.deleteMe();
		}
	}
	
	public boolean eventStart()
	{
		if (EVENT_START || !BorinetTask.ChristmasEvent())
		{
			return false;
		}
		EVENT_START = true;
		
		final EventLocation randomLoc = getRandomEntry(EventLocation.values());
		final long despawnDelay = EVENT_DURATION_TIMES * 60000;
		_thomas = addSpawn(칠면조, randomLoc.getX(), randomLoc.getY(), randomLoc.getZ(), 0, false, despawnDelay);
		_snowman = addSpawn(눈사람, randomLoc.getX() + getRandom(1, 100), randomLoc.getY() + getRandom(1, 100), randomLoc.getZ(), 0, false, despawnDelay);
		
		Broadcast.toAllOnlinePlayersOnScreen("크리스마스 이벤트: 비뚤어진 칠면조가 " + randomLoc.getName() + "지역에 나타났어요!");
		Broadcast.toAllOnlinePlayers("크리스마스 이벤트: 비뚤어진 칠면조가 " + randomLoc.getName() + "지역에 나타났어요!");
		Broadcast.toAllOnlinePlayers("크리스마스 이벤트: 지금부터 " + EVENT_DURATION_TIMES + "분안에 비뚤어진 칠면조를 처치하여 눈사람을 구하세요!");
		
		_snowmanState = SnowmanState.CAPTURED;
		GlobalVariablesManager.getInstance().set("CapturedSaveSnowman", System.currentTimeMillis() + despawnDelay);
		loc = randomLoc.getName();
		
		_eventTask = ThreadPool.schedule(() ->
		{
			_snowmanState = SnowmanState.FAILED;
			Broadcast.toAllOnlinePlayersOnScreen("크리스마스 이벤트: 눈사람 구출에 실패하였습니다!");
			Broadcast.toAllOnlinePlayers("크리스마스 이벤트: 눈사람 구출에 실패하였습니다! 산타클로스가 매우 화가났습니다!!");
			ThreadPool.schedule(this::endAngry, 1800000);
			GlobalVariablesManager.getInstance().set("FalsedSaveSnowman", System.currentTimeMillis() + 1800000);
			eventStop();
		}, despawnDelay);
		return true;
	}
	
	public boolean eventStop()
	{
		if (!EVENT_START)
		{
			return false;
		}
		
		EVENT_START = false;
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		
		if (_thomas != null)
		{
			_thomas.deleteMe();
		}
		if (_snowman != null)
		{
			_snowman.deleteMe();
		}
		
		final long randomTime = (getRandom(1, 2)) * 3600000;
		ThreadPool.schedule(this::eventStart, randomTime);
		return true;
	}
	
	public boolean endAngry()
	{
		_snowmanState = SnowmanState.SAVED;
		Broadcast.toAllOnlinePlayersOnScreen("산타클로스의 화가 풀렸습니다! 지금부터 서비스 이용이 가능합니다.");
		Broadcast.toAllOnlinePlayers("산타클로스의 화가 풀렸습니다! 지금부터 서비스 이용이 가능합니다.");
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (player == null)
		{
			return null;
		}
		
		if (event.startsWith("coupon"))
		{
			final String fullBypass = event.replace("coupon ", "");
			
			if (getQuestItemsCount(player, 20107) < 1)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				return null;
			}
			
			final int num = Integer.parseInt(fullBypass);
			if ((num < 0) || (num > 10))
			{
				return null;
			}
			
			int item_id = 0;
			if (player.getLevel() < 51)
			{
				item_id = WEAPONSC[num];
			}
			else if ((player.getLevel() >= 51) && (player.getLevel() < 60))
			{
				item_id = WEAPONSB[num];
			}
			else if ((player.getLevel() >= 60) && (player.getLevel() < 75))
			{
				item_id = WEAPONSA[num];
			}
			else if ((player.getLevel() >= 75) && (player.getLevel() < 80))
			{
				item_id = WEAPONSS[num];
			}
			else if (player.getLevel() >= 80)
			{
				item_id = WEAPONSS80[num];
			}
			
			int enchant = Rnd.get(15, 20);
			Item item = ItemTemplate.createItem(item_id);
			item.setEnchantLevel(enchant);
			player.addItem("크리스마스이벤트", item, null, true);
			takeItems(player, 20107, 1);
		}
		if (event.startsWith("multisell"))
		{
			final String fullBypass = event.replace("multisell ", "");
			final int multisellId = Integer.parseInt(fullBypass);
			MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		}
		
		String htmltext = null;
		switch (event)
		{
			case "0":
			{
				if ((getQuestItemsCount(player, 5556) >= 4) && (getQuestItemsCount(player, 5557) >= 4) && (getQuestItemsCount(player, 5558) >= 10) && (getQuestItemsCount(player, 5559) >= 1))
				{
					takeItems(player, 5556, 4);
					takeItems(player, 5557, 4);
					takeItems(player, 5558, 10);
					takeItems(player, 5559, 1);
					player.addItem("ChristmasChange", 5560, 1, null, true);
					break;
				}
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				break;
			}
			case "1":
			{
				if (getQuestItemsCount(player, 5560) >= 10)
				{
					takeItems(player, 5560, 10);
					player.addItem("ChristmasChange", 5561, 1, null, true);
					break;
				}
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				break;
			}
			case "2":
			{
				if (getQuestItemsCount(player, 5560) >= 1)
				{
					takeItems(player, 5560, 1);
					player.addItem("ChristmasChange", 7836, 1, null, true);
					break;
				}
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				break;
			}
			case "3":
			{
				if (getQuestItemsCount(player, 5560) >= 1)
				{
					takeItems(player, 5560, 1);
					player.addItem("ChristmasChange", 8936, 1, null, true);
					break;
				}
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				break;
			}
			case "4":
			{
				if (getQuestItemsCount(player, 5560) >= 3)
				{
					takeItems(player, 5560, 3);
					player.addItem("ChristmasChange", 10606, 1, null, true);
					break;
				}
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				break;
			}
			case "5":
			{
				htmltext = "13185-11.htm";
				break;
			}
			case "6":
			{
				if (getQuestItemsCount(player, 5560) >= 30)
				{
					takeItems(player, 5560, 30);
					player.addItem("ChristmasChange", 46286, 1, null, true);
					break;
				}
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				break;
			}
			case "change":
			{
				htmltext = "13185-1.htm";
				break;
			}
			case "buff":
			{
				final long var = player.getVariables().getLong("ChristmasEventTime", 0);
				if (var > System.currentTimeMillis())
				{
					htmltext = "13185-4.htm";
					break;
				}
				
				SkillCaster.triggerCast(npc, player, BUFF.getSkill());
				player.getVariables().set("ChristmasEventTime", System.currentTimeMillis() + SANTA_BUFF_REUSE);
				
				Summon pet = player.getPet();
				if (pet != null)
				{
					SkillCaster.triggerCast(npc, player, BUFF.getSkill());
				}
				break;
			}
			case "back":
			{
				htmltext = "13185.htm";
				break;
			}
			case "loc":
			{
				htmltext = getHtm(player, "13185-8.htm");
				htmltext = htmltext.replace("%loc%", String.valueOf(loc));
				break;
			}
			case "gamble":
			{
				if (getQuestItemsCount(player, 57) < 100000)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					break;
				}
				
				final long var = player.getVariables().getLong("ChristmasLotteryTime", 0);
				if (var > System.currentTimeMillis())
				{
					htmltext = "13185-5.htm";
					break;
				}
				
				int chance = Rnd.get(100); // 0~100 사이 난수 생성
				int itemId = 0; // 획득한 아이템 ID를 저장
				int obtainedItemCount = 1; // 기본 아이템 개수는 1개
				
				// 크리스마스 트리 30%
				if (chance < 30)
				{
					itemId = 5561; // 크리스마스 트리 30%
				}
				else if (chance < 39)
				{
					itemId = 20102; // 산타클로스의 선물 세트 9%
				}
				else if (chance < 47)
				{
					if (getQuestItemsCount(player, 20107) > 0)
					{
						startQuestTimer("gamble", 0, npc, null);
						break;
					}
					itemId = 20107; // 산타클로스의 무기 교환권 8%
				}
				else if (chance < 55)
				{
					itemId = 14616; // 산타클로스의 선물 8%
				}
				else if (chance < 62)
				{
					if (getQuestItemsCount(player, 14611) > 0)
					{
						startQuestTimer("gamble", 0, npc, null);
						break;
					}
					itemId = 14611; // 루돌프 코 7%
				}
				else if (chance < 69)
				{
					if (getQuestItemsCount(player, 7836) > 0)
					{
						startQuestTimer("gamble", 0, npc, null);
						break;
					}
					itemId = 7836; // 산타 모자 7%
				}
				else if (chance < 76)
				{
					if (getQuestItemsCount(player, 8936) > 0)
					{
						startQuestTimer("gamble", 0, npc, null);
						break;
					}
					itemId = 8936; // 산타 뿔 모자 7%
				}
				else if (chance < 82)
				{
					if (getQuestItemsCount(player, 10606) > 0)
					{
						startQuestTimer("gamble", 0, npc, null);
						break;
					}
					itemId = 10606; // 아가시온 봉인 팔찌 - 루돌프(시간제) 6%
				}
				else if (chance < 88)
				{
					if (getQuestItemsCount(player, 20094) > 0)
					{
						startQuestTimer("gamble", 0, npc, null);
						break;
					}
					itemId = 20094; // 아가시온 봉인 팔찌 - 루돌프 6%
				}
				else if (chance < 94)
				{
					itemId = 20575; // 경험치 달인 - 이벤트 6%
				}
				else if (chance < 99)
				{
					if ((getQuestItemsCount(player, 9177) > 0) || (getQuestItemsCount(player, 9204) > 0))
					{
						startQuestTimer("gamble", 0, npc, null);
						break;
					}
					itemId = 9204; // 투영병기 - 강철의 서클릿 5%
				}
				else if (chance <= 100)
				{
					itemId = 29010;
					obtainedItemCount = 5; // EXP/SP 부스트 주문서 - 상급은 2%
				}
				
				takeItems(player, 57, 100000);
				player.addItem("ChristmasLottery", itemId, obtainedItemCount, null, true);
				player.getVariables().set("ChristmasLotteryTime", System.currentTimeMillis() + SANTA_LOTTERY_REUSE);
				break;
			}
			case "SPAM_TEXT":
			{
				int min = Rnd.get(1, 2);
				if (_snowmanState == SnowmanState.SAVED)
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, GOOD[Rnd.get(GOOD.length)]));
					startQuestTimer("SPAM_TEXT", (min * 60 * 1000), npc, null);
				}
				else if (_snowmanState == SnowmanState.CAPTURED)
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, CAPTURED[Rnd.get(CAPTURED.length)]));
					startQuestTimer("SPAM_TEXT", (min * 60 * 1000), npc, null);
				}
				else if (_snowmanState == SnowmanState.FAILED)
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, BAD[Rnd.get(BAD.length)]));
					startQuestTimer("SPAM_TEXT", (min * 60 * 1000), npc, null);
				}
				break;
			}
			case "SPAM_TEXT_SNOWMAN":
			{
				int min = Rnd.get(1, 2);
				npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, "도와주세요! 살려주세요!!!"));
				startQuestTimer("SPAM_TEXT_SNOWMAN", (min * 60 * 1000), npc, null);
				break;
			}
			case "letmego":
			{
				htmltext = "13185-9.htm";
				break;
			}
			case "giftbox":
			{
				int itemCount = (int) getQuestItemsCount(player, 46290);
				
				// 아이템이 15개 이상 있어야 진행 가능
				if (itemCount < 15)
				{
					htmltext = "13185-10.htm";
					break;
				}
				
				// 몇 번 진행 가능한지 계산
				int iterations = itemCount / 15; // 진행 가능한 횟수
				int totalItemsToRemove = iterations * 15; // 제거할 총 아이템 수
				Map<Integer, Integer> itemTotals = new HashMap<>();
				int missCount = 0; // 꽝 횟수 추적
				
				// **아이템을 한 번에 제거**
				takeItems(player, 46290, totalItemsToRemove);
				
				synchronized (player) // 동시성 제어
				{
					for (int i = 0; i < iterations; i++)
					{
						int chance = Rnd.get(100); // 0~100 사이 난수 생성
						int itemId = 0; // 획득한 아이템 ID를 저장
						int obtainedItemCount = 1; // 기본 아이템 개수는 1개
						
						if (chance < 2)
						{
							// 꽝 5%
							player.sendMessage("꽝. 다음 기회에...");
							missCount++;
							continue;
						}
						else if (chance < 20)
						{
							itemId = 5561; // 크리스마스 트리
						}
						else if (chance < 28)
						{
							itemId = 20102; // 산타클로스의 선물 세트
						}
						else if (chance < 37)
						{
							itemId = 46289; // 기적의 크리스마스 선물 상자
						}
						else if (chance < 46)
						{
							if (getQuestItemsCount(player, 20107) > 0)
							{
								// 이미 아이템이 있으면 꽝 처리
								missCount++;
								continue;
							}
							itemId = 20107; // 산타클로스의 무기 교환권
						}
						else if (chance < 54)
						{
							itemId = 14616; // 산타클로스의 선물
						}
						else if (chance < 60)
						{
							if (getQuestItemsCount(player, 14611) > 0)
							{
								// 이미 아이템이 있으면 꽝 처리
								missCount++;
								continue;
							}
							itemId = 14611; // 루돌프 코
						}
						else if (chance < 68)
						{
							if (getQuestItemsCount(player, 7836) > 0)
							{
								// 이미 아이템이 있으면 꽝 처리
								missCount++;
								continue;
							}
							itemId = 7836; // 산타 모자
						}
						else if (chance < 75)
						{
							if (getQuestItemsCount(player, 8936) > 0)
							{
								// 이미 아이템이 있으면 꽝 처리
								missCount++;
								continue;
							}
							itemId = 8936; // 산타 뿔 모자
						}
						else if (chance < 80)
						{
							if (getQuestItemsCount(player, 10606) > 0)
							{
								// 이미 아이템이 있으면 꽝 처리
								missCount++;
								continue;
							}
							itemId = 10606; // 아가시온 봉인 팔찌 - 루돌프(시간제)
						}
						else if (chance < 86)
						{
							if (getQuestItemsCount(player, 20094) > 0)
							{
								// 이미 아이템이 있으면 꽝 처리
								missCount++;
								continue;
							}
							itemId = 20094; // 아가시온 봉인 팔찌 - 루돌프
						}
						else if (chance < 92)
						{
							itemId = 20575; // 경험치 달인 - 이벤트
						}
						else if (chance < 98)
						{
							if ((getQuestItemsCount(player, 9177) > 0) || (getQuestItemsCount(player, 9204) > 0))
							{
								// 이미 아이템이 있으면 꽝 처리
								missCount++;
								continue;
							}
							itemId = 9204; // 투영병기 - 강철의 서클릿
						}
						else if (chance <= 100)
						{
							itemId = 29010;
							obtainedItemCount = 5; // EXP/SP 부스트 주문서 - 상급
						}
						
						// 아이템 지급
						if (itemId > 0)
						{
							player.addItem("ChristmasLottery", itemId, obtainedItemCount, null, true);
							itemTotals.put(itemId, itemTotals.getOrDefault(itemId, 0) + obtainedItemCount);
						}
					}
				}
				
				// 결과 생성 및 출력
				StringBuilder itemList = new StringBuilder();
				for (Map.Entry<Integer, Integer> entry : itemTotals.entrySet())
				{
					String itemName = ItemNameTable.getInstance().getItemNameKor(entry.getKey());
					itemList.append(itemName).append(" x").append(entry.getValue()).append(" 개<br1>");
				}
				
				if (iterations > 1)
				{
					htmltext = getHtm(player, "13185-multiple.htm");
					htmltext = htmltext.replace("%iterations%", String.valueOf(iterations));
					htmltext = htmltext.replace("%items%", itemList.toString());
					htmltext = htmltext.replace("%missCount%", String.valueOf(missCount));
				}
				break;
			}
			
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		if ((attacker.isPlayer()))
		{
			if ((System.currentTimeMillis() - _lastSay) > 5000)
			{
				if (attacker.getActiveWeaponItem() == null)
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, noWeapon[Rnd.get(noWeapon.length)]));
					_lastSay = System.currentTimeMillis();
					npc.setInvul(true);
				}
				else if (CHRONO_LIST.contains(attacker.getActiveWeaponItem().getId()))
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, attacked[Rnd.get(attacked.length)]));
					_lastSay = System.currentTimeMillis();
					npc.setInvul(false);
					npc.getStatus().reduceHp(10, attacker);
				}
				else
				{
					npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, noCrono[Rnd.get(noCrono.length)]));
					_lastSay = System.currentTimeMillis();
					npc.setInvul(true);
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (EVENT_START)
		{
			npc.dropItem(killer, 20034, 3);
			
			Broadcast.toAllOnlinePlayersOnScreen("크리스마스 이벤트: 눈사람을 구출했습니다!");
			Broadcast.toAllOnlinePlayers("크리스마스 이벤트: 눈사람을 구출했습니다!");
			npc.broadcastPacket(new NpcSay(_snowman, ChatType.NPC_GENERAL, "고맙습니다! 저를 구해주셔서 감사합니다."));
			_snowmanState = SnowmanState.SAVED;
			
			for (Player b : World.getInstance().getPlayers())
			{
				SkillCaster.triggerCast(b, b, SANTA_BUFF.getSkill());
			}
			for (Player players : World.getInstance().getVisibleObjectsInRange(npc, Player.class, 1000))
			{
				SkillCaster.triggerCast(players, players, THOMAS_BUFF.getSkill());
			}
			eventStop();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (npc.getId() == 산타클로스)
		{
			startQuestTimer("SPAM_TEXT", (1 * 60 * 1000), npc, null);
		}
		if (npc.getId() == 눈사람)
		{
			if ((_snowman != null) || (_snowmanState == SnowmanState.CAPTURED))
			{
				startQuestTimer("SPAM_TEXT_SNOWMAN", (1 * 60 * 1000), npc, null);
			}
		}
		
		npc.setRandomWalking(false);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final String htmltext = null;
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		
		if (_snowmanState == SnowmanState.SAVED)
		{
			packet.setHtml(getHtm(player, "13185.htm"));
			player.sendPacket(packet);
		}
		else if (_snowmanState == SnowmanState.CAPTURED)
		{
			final long reuse = GlobalVariablesManager.getInstance().getLong("CapturedSaveSnowman", 0);
			final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
			final int minutes = (int) ((remainingTime % 3600) / 60);
			packet.setHtml(getHtm(player, "13185-3.htm"));
			packet.replace("%mins%", Integer.toString(minutes));
			player.sendPacket(packet);
		}
		else if (_snowmanState == SnowmanState.FAILED)
		{
			final long reuse = GlobalVariablesManager.getInstance().getLong("FalsedSaveSnowman", 0);
			final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
			final int minutes = (int) ((remainingTime % 3600) / 60);
			packet.setHtml(getHtm(player, "13185-7.htm"));
			packet.replace("%mins%", Integer.toString(minutes));
			player.sendPacket(packet);
		}
		return htmltext;
	}
	
	private enum EventLocation
	{
		HUMAN1("말하는 섬 마을", -81880, 245672, -3712),
		HUMAN2("말하는 섬 마을", -81752, 240904, -3704),
		HUMAN3("말하는 섬 마을", -86840, 240248, -3680),
		GIRAN1("기란성 마을", 91320, 147272, -3472),
		GIRAN2("기란성 마을", 81224, 142088, -3576),
		GIRAN3("기란성 마을", 76040, 148664, -3576),
		GIRAN4("기란성 마을", 81528, 153768, -3536),
		GLUDIO1("글루디오성 마을", -14824, 120232, -3136),
		GLUDIO2("글루디오성 마을", -14024, 127448, -3216),
		DION1("디온성 마을", 22664, 145992, -3320),
		DION2("디온성 마을", 18904, 141656, -3232),
		FLORAN("플로란 마을", 18280, 171544, -3528);
		
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
	
	public static void main(String[] args)
	{
		new ChristmasEvent();
	}
}
