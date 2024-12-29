package ai.others;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

public class TurekOrcs extends AbstractNpcAI
{
	// NPCs
	private static final int[] MOBS =
	{
		20436, // 올 마훔 보급병
		20437, // 올 마훔 신병
		20438, // 올 마훔 장성
		20439, // 올 마훔 하사관
		20494, // 투렉 군견
		20495, // 투렉 오크 군장
		20496, // 투렉 오크 궁병
		20497, // 투렉 오크 돌격병
		20498, // 투렉 오크 보급병
		20499, // 투렉 오크 보병
		20500, // 투렉 오크 보초병
		20501 // 투렉 오크 보초병
	};
	
	private TurekOrcs()
	{
		addAttackId(MOBS);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if ((attacker != null) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.4)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.1)) && Rnd.chance(20))
		{
			Location pos = new Location(npc.getX() + Rnd.get(-800, -500), npc.getY() + Rnd.get(700, 1200), npc.getZ() + Rnd.get(100, 150));
			if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), pos.getX(), pos.getY(), pos.getZ(), null))
			{
				npc.broadcastSay(ChatType.GENERAL, NpcStringId.getNpcStringId(getRandom(1000007, 1000027)));
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos); // Changed to MOVE_TO
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new TurekOrcs();
	}
}
