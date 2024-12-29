package ai.others;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

public class Elpy extends AbstractNpcAI
{
	private static final int ELPY_NPC_ID = 20432; // NPC ID
	
	public Elpy()
	{
		addAttackId(ELPY_NPC_ID);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if ((attacker != null) && Rnd.chance(20))
		{
			Location pos = new Location(npc.getX() + Rnd.get(-200, 200), npc.getY() + Rnd.get(-200, 200), npc.getZ());
			if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), pos.getX(), pos.getY(), pos.getZ(), null))
			{
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos); // Changed to MOVE_TO
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
}
