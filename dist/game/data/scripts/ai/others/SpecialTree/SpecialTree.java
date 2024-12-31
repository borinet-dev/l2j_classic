package ai.others.SpecialTree;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.zone.ZoneId;

import ai.AbstractNpcAI;

public class SpecialTree extends AbstractNpcAI
{
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