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
package handlers.bypasshandlers;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.SkillCaster;

public class SupportMagic implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"supportmagicservitor",
		"supportmagic"
	};
	
	// Buffs
	private static final SkillHolder CUBIC = new SkillHolder(4338, 1);
	private static final SkillHolder[] CHARACTER_BUFFS =
	{
		new SkillHolder(30235, 1), // 아큐맨
		new SkillHolder(30233, 1), // 헤이스트
		new SkillHolder(30234, 1), // 가이던스
		new SkillHolder(30236, 1), // 그레이트 마이트
		new SkillHolder(30237, 1), // 그레이트 실드
		// new SkillHolder(30238, 1), // 샤픈 엣지
		new SkillHolder(30239, 1), // 챈트 오브 빅토리
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
	private static final SkillHolder[] SUMMON_BUFFS =
	{
		new SkillHolder(30235, 1), // 아큐맨
		new SkillHolder(30237, 1), // 그레이트 실드
		new SkillHolder(30240, 1), // 와일드 매직
		new SkillHolder(4325, 1), // Vampiric Rage
		new SkillHolder(30233, 1), // 헤이스트
		new SkillHolder(30234, 1), // 가이던스
		new SkillHolder(30236, 1), // 그레이트 마이트
		// new SkillHolder(30238, 1), // 샤픈 엣지
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
	
	// Levels
	private static final int LOWEST_LEVEL = 1;
	private static final int CUBIC_LOWEST = 1;
	private static final int CUBIC_HIGHEST = 80;
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage("저주받은 무기를 소유한 상태에서는 사용할 수 없습니다.");
			return false;
		}
		
		if (command.equalsIgnoreCase(COMMANDS[0]))
		{
			giveSupportMagicPet(player, (Npc) target);
		}
		else if (command.equalsIgnoreCase(COMMANDS[1]))
		{
			makeSupportMagic(player, (Npc) target, true);
		}
		return true;
	}
	
	public static void giveSupportMagicPet(Player player, Npc npc)
	{
		if (player.hasPet())
		{
			final int petLevel = player.getPet().getLevel();
			if (petLevel > 80)
			{
				npc.showChatWindow(player, "data/html/default/SupportMagicHighLevelPet.htm");
				return;
			}
			
			List<Creature> target = new ArrayList<>();
			target.add(player.getPet());
			npc.setTarget(player.getPet());
			for (SkillHolder skill : SUMMON_BUFFS)
			{
				SkillCaster.triggerCast(npc, player.getPet(), skill.getSkill());
			}
		}
		else
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicNoSummon.htm");
			return;
		}
	}
	
	public static void makeSupportMagic(Player player, Npc npc, boolean isMage)
	{
		final int level = player.getLevel();
		if (level < LOWEST_LEVEL)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicLowLevel.htm");
			return;
		}
		else if (level > 80)
		{
			npc.showChatWindow(player, "data/html/default/SupportMagicHighLevel.htm");
			return;
		}
		
		npc.setTarget(player);
		for (SkillHolder skill : CHARACTER_BUFFS)
		{
			SkillCaster.triggerCast(npc, player, skill.getSkill());
		}
		
		if ((level >= CUBIC_LOWEST) && (level <= CUBIC_HIGHEST))
		{
			SkillCaster.triggerCast(npc, player, CUBIC.getSkill());
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}