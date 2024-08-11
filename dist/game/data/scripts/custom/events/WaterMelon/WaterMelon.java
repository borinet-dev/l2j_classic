package custom.events.WaterMelon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Event;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author 보리넷 가츠
 */
public class WaterMelon extends Event
{
	private static final Map<Npc, Integer> TRY_COUNT = new ConcurrentHashMap<>();
	private static final Map<Npc, Integer> NECTAR_COUNT = new ConcurrentHashMap<>();
	public static final int MANAGER = 31860;
	private static final int NECTAR_SKILL = 2005;
	private long _lastNectarUse;
	
	public final static int 어린수박 = 13271;
	public final static int 우량수박 = 13273;
	public final static int 불량수박 = 13272;
	public final static int 왕우량수박 = 13274;
	
	public final static int 어린꿀수박 = 13275;
	public final static int 우량꿀수박 = 13277;
	public final static int 불량꿀수박 = 13276;
	public final static int 왕우량꿀수박 = 13278;
	
	public final static int Squash_Level_up = 4513;
	public final static int Squash_Poisoned = 4514;
	private static int NECTAR_REUSE = 3000;
	
	private static final List<Integer> CHRONO_LIST = Arrays.asList(4202, 5133, 5817, 7058, 8350, 46249, 49798);
	
	public WaterMelon()
	{
		if (Config.WATERMELON_EVENT_ENABLED)
		{
			addAttackId(어린수박, 우량수박, 불량수박, 어린꿀수박, 우량꿀수박, 불량꿀수박, 왕우량수박, 왕우량꿀수박);
			addKillId(어린수박, 우량수박, 불량수박, 어린꿀수박, 우량꿀수박, 불량꿀수박, 왕우량수박, 왕우량꿀수박);
			addSpawnId(어린수박, 우량수박, 불량수박, 어린꿀수박, 우량꿀수박, 불량꿀수박, 왕우량수박, 왕우량꿀수박);
			addSkillSeeId(어린수박, 우량수박, 불량수박, 어린꿀수박, 우량꿀수박, 불량꿀수박, 왕우량수박, 왕우량꿀수박);
			
			addStartNpc(MANAGER);
			addFirstTalkId(MANAGER);
			addTalkId(MANAGER);
		}
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setImmobilized(true);
		npc.disableCoreAI(true);
		npc.setInvul(true);
		
		if ((npc.getId() == 어린수박) || (npc.getId() == 어린꿀수박))
		{
			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.어린수박대화[getRandom(WaterMelonSay.어린수박대화.length)]));
		}
		else if ((npc.getId() == 우량수박) || (npc.getId() == 우량꿀수박))
		{
			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.우량수박대화[getRandom(WaterMelonSay.우량수박대화.length)]));
		}
		else if ((npc.getId() == 불량수박) || (npc.getId() == 불량꿀수박))
		{
			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.불량수박대화[getRandom(WaterMelonSay.불량수박대화.length)]));
		}
		else if ((npc.getId() == 왕우량수박) || (npc.getId() == 왕우량꿀수박))
		{
			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.왕우량수박대화[getRandom(WaterMelonSay.왕우량수박대화.length)]));
		}
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		if (attacker.getActiveWeaponItem() == null)
		{
			WaterMelonSay.noChronoText(npc);
			npc.setInvul(true);
		}
		else if (!CHRONO_LIST.contains(attacker.getActiveWeaponItem().getId()))
		{
			WaterMelonSay.noChronoText(npc);
			npc.setInvul(true);
		}
		else
		{
			if (Rnd.chance(15))
			{
				if ((npc.getId() == 어린수박) || (npc.getId() == 어린꿀수박))
				{
					npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.어린수박공격대화[getRandom(WaterMelonSay.어린수박공격대화.length)]));
				}
				else
				{
					npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.우량수박공격대화[getRandom(WaterMelonSay.우량수박공격대화.length)]));
				}
			}
			npc.setInvul(false);
			npc.getStatus().reduceHp(10, attacker);
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet)
	{
		if (((npc.getId() == 어린수박) || (npc.getId() == 어린꿀수박)) && (skill.getId() == NECTAR_SKILL))
		{
			final int count = TRY_COUNT.getOrDefault(npc, 0);
			final int _nectar = NECTAR_COUNT.getOrDefault(npc, 0);
			
			switch (count)
			{
				default:
				case 0:
				{
					TRY_COUNT.put(npc, 1);
					_lastNectarUse = System.currentTimeMillis();
					if (Rnd.chance(40))
					{
						NECTAR_COUNT.put(npc, 1);
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타성공[getRandom(WaterMelonSay.넥타성공.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Level_up, 1, NECTAR_REUSE, 0));
					}
					else
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타실패[getRandom(WaterMelonSay.넥타실패.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Poisoned, 1, NECTAR_REUSE, 0));
					}
					break;
				}
				case 1:
				{
					if ((System.currentTimeMillis() - _lastNectarUse) < NECTAR_REUSE)
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타빨리먹임[getRandom(WaterMelonSay.넥타빨리먹임.length)]));
						break;
					}
					TRY_COUNT.put(npc, count + 1);
					_lastNectarUse = System.currentTimeMillis();
					if (Rnd.chance(40))
					{
						NECTAR_COUNT.put(npc, _nectar + 1);
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타성공[getRandom(WaterMelonSay.넥타성공.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Level_up, 1, NECTAR_REUSE, 0));
					}
					else
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타실패[getRandom(WaterMelonSay.넥타실패.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Poisoned, 1, NECTAR_REUSE, 0));
					}
					break;
				}
				case 2:
				{
					if ((System.currentTimeMillis() - _lastNectarUse) < NECTAR_REUSE)
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타빨리먹임[getRandom(WaterMelonSay.넥타빨리먹임.length)]));
						break;
					}
					TRY_COUNT.put(npc, count + 1);
					_lastNectarUse = System.currentTimeMillis();
					if (Rnd.chance(40))
					{
						NECTAR_COUNT.put(npc, _nectar + 1);
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타성공[getRandom(WaterMelonSay.넥타성공.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Level_up, 1, NECTAR_REUSE, 0));
					}
					else
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타실패[getRandom(WaterMelonSay.넥타실패.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Poisoned, 1, NECTAR_REUSE, 0));
					}
					break;
				}
				case 3:
				{
					if ((System.currentTimeMillis() - _lastNectarUse) < NECTAR_REUSE)
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타빨리먹임[getRandom(WaterMelonSay.넥타빨리먹임.length)]));
						break;
					}
					TRY_COUNT.put(npc, count + 1);
					_lastNectarUse = System.currentTimeMillis();
					if (Rnd.chance(40))
					{
						NECTAR_COUNT.put(npc, _nectar + 1);
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타성공[getRandom(WaterMelonSay.넥타성공.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Level_up, 1, NECTAR_REUSE, 0));
					}
					else
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타실패[getRandom(WaterMelonSay.넥타실패.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Poisoned, 1, NECTAR_REUSE, 0));
					}
					break;
				}
				case 4:
				{
					if ((System.currentTimeMillis() - _lastNectarUse) < NECTAR_REUSE)
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타빨리먹임[getRandom(WaterMelonSay.넥타빨리먹임.length)]));
						break;
					}
					TRY_COUNT.put(npc, count + 1);
					_lastNectarUse = System.currentTimeMillis();
					if (Rnd.chance(40))
					{
						NECTAR_COUNT.put(npc, _nectar + 1);
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타성공[getRandom(WaterMelonSay.넥타성공.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Level_up, 1, NECTAR_REUSE, 0));
					}
					else
					{
						npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.넥타실패[getRandom(WaterMelonSay.넥타실패.length)]));
						npc.broadcastPacket(new MagicSkillUse(npc, npc, Squash_Poisoned, 1, NECTAR_REUSE, 0));
					}
					if (npc.getId() == 어린수박)
					{
						if (_nectar < 3)
						{
							spawnNext(불량수박, npc);
						}
						else if (_nectar == 5)
						{
							spawnNext(왕우량수박, npc);
						}
						else
						{
							spawnNext(우량수박, npc);
						}
					}
					else if (npc.getId() == 어린꿀수박)
					{
						if (_nectar < 3)
						{
							spawnNext(불량꿀수박, npc);
						}
						else if (_nectar == 5)
						{
							spawnNext(왕우량꿀수박, npc);
						}
						else
						{
							spawnNext(우량꿀수박, npc);
						}
					}
					TRY_COUNT.remove(npc);
					NECTAR_COUNT.remove(npc);
					break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		switch (npc.getId())
		{
			case 불량수박:
				npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), "어이쿠! 터졌다! 내용물이 줄줄줄~"));
				WaterMelonDrop.dropItem(npc, killer, 13272, 0, 2);
				break;
			case 우량수박:
				npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), "여러분 여기 박 깨져요!! 아이템 떨어져요! 우케케케"));
				WaterMelonDrop.dropItem(npc, killer, 13273, 0, 5);
				break;
			case 왕우량수박:
				npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), "이거나 먹고 떨어져라~"));
				WaterMelonDrop.dropItem(npc, killer, 13274, 0, 8);
				break;
			case 불량꿀수박:
				npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), "으악 창자가 흘러나와!"));
				WaterMelonDrop.dropItem(npc, killer, 13276, 0, 2);
				break;
			case 우량꿀수박:
				npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), "나의 죽음을 알리지 마라!"));
				WaterMelonDrop.dropItem(npc, killer, 13277, 0, 5);
				break;
			case 왕우량꿀수박:
				npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), "안에서 도깨비나 나와라!"));
				WaterMelonDrop.dropItem(npc, killer, 13278, 0, 8);
				break;
			default:
				npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), "응? 나를 때리네;; 난 아무것도 없쪄!"));
				break;
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "31860-1.htm":
			{
				htmltext = "31860-1.htm";
				break;
			}
			case "31860-0.htm":
			{
				htmltext = "31860.htm";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public boolean eventBypass(Player player, String bypass)
	{
		return true;
	}
	
	private void spawnNext(int npcId, Npc npc)
	{
		addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 90000);
		npc.deleteMe();
	}
	
	public static void main(String[] args)
	{
		new WaterMelon();
	}
	
	@Override
	public boolean eventStart(Player eventMaker)
	{
		return false;
	}
	
	@Override
	public boolean eventStop()
	{
		return false;
	}
}