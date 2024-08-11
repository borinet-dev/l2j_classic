package ai.bosses;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import ai.AbstractNpcAI;

public final class NewPhase extends AbstractNpcAI
{
	// NPCs
	private static final int[] GRAND_BOSSES =
	{
		29020, // 바이움
		29022, // 자켄
		29047, // 스칼렛
		29068, // 안타라스
		25286, // 아나킴
		25283, // 릴리스
		18029, // 오르펜
		18020, // 여왕개미
		29105, // 이그니스
		29106, // 네불라
		29107, // 프로첼라
		29108, // 페트람
	};
	
	// Skill
	private static final SkillHolder NEW_PHASE = new SkillHolder(30229, 1);
	// Misc
	private static final Map<Npc, Integer> BOSS_PHASE = new ConcurrentHashMap<>();
	
	private NewPhase()
	{
		addAttackId(GRAND_BOSSES);
		addKillId(GRAND_BOSSES);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "NEW_PHASE":
			{
				npc.setTarget(npc);
				npc.abortAttack();
				npc.abortCast();
				npc.doCast(NEW_PHASE.getSkill());
				if (!npc.isAffectedBySkill(NEW_PHASE.getSkillId()))
				{
					startQuestTimer("NEW_PHASE", 10, npc, null);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
					sm.addString("보스의 HP가 20% 미만으로 떨어져서 페이즈 2로 돌입하였습니다!");
					npc.broadcastPacket(sm);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final int phase = BOSS_PHASE.getOrDefault(npc, 0);
		
		if (!npc.isAffectedBySkill(NEW_PHASE.getSkillId()) && (npc.getCurrentHpPercent() < 20))
		{
			if (attacker.isPlayer() && (phase < 1))
			{
				startQuestTimer("NEW_PHASE", 10, npc, null);
				BOSS_PHASE.put(npc, 1);
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		BOSS_PHASE.remove(npc);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new NewPhase();
	}
}
