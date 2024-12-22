package ai.others.SpecialTree;

import org.l2jmobius.commons.time.RndSelector;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

import ai.AbstractNpcAI;

public class SpecialTree extends AbstractNpcAI
{
	private static final RndSelector<Integer> SOUNDS;
	
	static
	{
		SOUNDS = new RndSelector<>(5);
		SOUNDS.add(2140, 20);
		SOUNDS.add(2142, 20);
		SOUNDS.add(2145, 20);
		SOUNDS.add(2147, 20);
		SOUNDS.add(2149, 20);
	}
	
	// NPCs
	private static final int[] CHRISTMAS_TREES =
	{
		13006,
		13007,
	};
	
	private boolean _buffsEnabled = false;
	
	// Skills
	private static final SkillHolder BUFF = new SkillHolder(2139, 1);
	
	private SpecialTree()
	{
		addSpawnId(CHRISTMAS_TREES);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "BUFF":
			{
				if (_buffsEnabled)
				{
					SkillCaster.triggerCast(npc, npc, BUFF.getSkill());
					startQuestTimer("BUFF", 15000, npc, null);
					
					if (Rnd.chance(33))
					{
						npc.broadcastPacket(new MagicSkillUse(npc, npc, SOUNDS.select(), 1, 500, 0));
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		_buffsEnabled = !npc.isInsideZone(ZoneId.PEACE);
		startQuestTimer("BUFF", 10, npc, null);
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new SpecialTree();
	}
}