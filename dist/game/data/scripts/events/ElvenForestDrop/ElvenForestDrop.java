package events.ElvenForestDrop;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class ElvenForestDrop extends LongTimeEvent
{
	// @formatter:off
	// Monsters
	private static final int[] ELVEN_FOREST_MONSTERS =
	{
		19306,19307,19327,19328,19329,19330,19331,
		19332,19336,19337,19338,19339,19340,19341,
		19342,19343,19344,19345,19346,19347,19348,
		19349,19350,19351,19352,19353,19354,19357,
		19358,19359,19360,19361,19362,19363,22545,
		22546,22547,22548,22549,22550,22551,22552,
		22593,22596,22597,22868,22869,22870,22875,
		22876,22877,22878,22884,22885,22886,22892,
		22893,22894,22900,22901,22902,22908,22909,
		22910,22928,22929,22930,22950,22952,22953,
		22954,22955,22957,22958,22959,22960,22961,
		22963,22964,22965,22966,22967,22968,22969,
		22970,22979,22980,22981,22983,22985
	};
	// @formatter:on
	
	// Items
	private static final int 무기강화석 = 41233;
	private static final int 방어구강화석 = 41234;
	
	public ElvenForestDrop()
	{
		addKillId(ELVEN_FOREST_MONSTERS);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		double rate = 10;
		
		if (killer.isInParty())
		{
			final Party party = killer.getParty();
			final List<Player> members = party.getMembers();
			
			if (members.size() > 2)
			{
				for (Player member : members)
				{
					if (member.isInsideRadius3D(killer, 500) && !member.isDead())
					{
						rate += 1;
					}
				}
			}
		}
		if (Config.PREMIUM_SYSTEM_ENABLED && (killer.isPremium()))
		{
			rate *= 1.05;
		}
		ElvenForestDropRate(npc, killer, rate);
		
		return super.onKill(npc, killer, isSummon);
	}
	
	private void ElvenForestDropRate(Npc npc, Player player, double rate)
	{
		if (Rnd.chance((BorinetUtil.getInstance().isPlayerDropPenalty(player) ? (rate * 0.8) : rate) * 0.04))
		{
			player.doAutoLoot((Attackable) npc, Rnd.chance(65) ? 방어구강화석 : 무기강화석, 1);
		}
	}
	
	public static void main(String[] args)
	{
		new ElvenForestDrop();
	}
}