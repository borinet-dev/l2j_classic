package ai.others.SevenSigns;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;

import ai.AbstractNpcAI;

public class SevenSigns extends AbstractNpcAI
{
	private static final SkillHolder SKILL = new SkillHolder(4124, 12);
	private static final Map<Npc, Integer> CAST = new ConcurrentHashMap<>();
	private static final Map<Npc, Long> LAST_SAY = new ConcurrentHashMap<>();
	
	// NPCs
	// @formatter:off
	private static final int[] MONSTERS =
	{
		41001, 41002, 41003, 41004, 41005, 41006, 
		41007, 41008, 41009, 41010, 41011, 41012,
		41013, 41014, 41015, 41016, 41017,
	};
	// @formatter:on
	
	private SevenSigns()
	{
		addKillId(MONSTERS);
		addAttackId(MONSTERS);
	}
	
	private static final String[] HASPARTY =
	{
		"나에게 도전하는가!",
		"푸하하하. 고작 그정도 실력으로 나는 상대하려하는가?",
		"가소롭다!!",
		"나를 이렇게까지 기쁘게 만들어 주다니!!",
		"이거이거~ 아프지도 않은데?",
		"이렇게까지 나를 화나게 하다니.. 가만두지 않겠다!",
		"네 놈. 네 놈이 바로 악마구나! 나를 이렇게 만든 악마!",
		"미약한 그대의 힘으로 나를 꺾을 수 있다고 생각하는가!"
	};
	
	private static final String[] DOCAST =
	{
		"아햏햏~ 나의 숨겨돈 비장의 수를... 써야겠군...!",
		"아브라카 타브라!!! 죽어라! 크하하하",
		"제법이군... 하지만 이렇게 하면 어떨까!!"
	};
	
	private static final String[] LOWHP =
	{
		"놀랍군! 나를 이렇게까지 몰아붙이다니!",
		"나를 살려주면 천만 아데나를 주마!",
		"나를 죽여도 아무 것도 얻을 수 없다!",
		"자.. 잠깐! 그만하자구! 날 살려주면 천만 아데나를 줄께!!",
		"가소로운 녀석, 생각보다 버티는군. 나를 즐겁게 해주었으니 이번에는 용서해주마.",
		"나를 쉽게 죽이지는 못할것이다~! 크아아아아~~!!!!",
		"도와주세요! 살려주세요!!!"
	};
	
	private static final String[] NOPARTY =
	{
		"혼자니?",
		"혼자서 나를 상대하겠다고?",
		"파티가 아닌이상 나를 상대할 순 없다!",
		"싱글이니? 가서 파티하고 와라!"
	};
	
	private static final String[] NOPARTYRADAR =
	{
		"파티원이 다 모이지 않았군?",
		"모두 모인 후 나를 상대하라!",
		"너무 가소롭구나! 모든 파티원을 모아라!"
	};
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final int docast = CAST.getOrDefault(npc, 0);
		final Long lastSay = LAST_SAY.getOrDefault(npc, (long) 0);
		
		if ((attacker.isPlayer()))
		{
			if ((System.currentTimeMillis() - lastSay) > 5000)
			{
				if (!checkParty(npc, attacker))
				{
					npc.setInvul(true);
				}
				else
				{
					npc.setInvul(false);
					if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.5)) && (docast < 1))
					{
						npc.broadcastPacket(new NpcSay(npc, ChatType.GENERAL, DOCAST[Rnd.get(DOCAST.length)]));
						npc.doCast(SKILL.getSkill());
						CAST.put(npc, 1);
						LAST_SAY.put(npc, System.currentTimeMillis());
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.07))
					{
						npc.broadcastPacket(new NpcSay(npc, ChatType.GENERAL, LOWHP[Rnd.get(LOWHP.length)]));
						LAST_SAY.put(npc, System.currentTimeMillis());
					}
					else if ((System.currentTimeMillis() - lastSay) > 40000)
					{
						npc.broadcastPacket(new NpcSay(npc, ChatType.GENERAL, HASPARTY[Rnd.get(HASPARTY.length)]));
						LAST_SAY.put(npc, System.currentTimeMillis());
					}
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	public boolean checkParty(Npc npc, Player player)
	{
		if (player.getParty() == null)
		{
			npc.broadcastPacket(new NpcSay(npc, ChatType.GENERAL, NOPARTY[Rnd.get(NOPARTY.length)]));
			LAST_SAY.put(npc, System.currentTimeMillis());
			return false;
		}
		
		final Party party = player.getParty();
		final boolean isInCC = party.isInCommandChannel();
		final List<Player> members = isInCC ? party.getCommandChannel().getMembers() : party.getMembers();
		for (Player member : members)
		{
			if (!member.isInsideRadius3D(player, 1200))
			{
				npc.broadcastPacket(new NpcSay(npc, ChatType.GENERAL, NOPARTYRADAR[Rnd.get(NOPARTYRADAR.length)]));
				LAST_SAY.put(npc, System.currentTimeMillis());
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args)
	{
		new SevenSigns();
	}
}