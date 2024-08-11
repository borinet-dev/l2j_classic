package ai.others.TeleportToFantasy;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

public class TeleportToFantasy extends AbstractNpcAI
{
	// NPC
	private static final int FANTASY_MANAGER = 32378;
	// Locations
	public static final Location[] POINTS =
	{
		new Location(-60695 + Rnd.get(-100, 100), -56896 + Rnd.get(-100, 100), -2032),
		new Location(-59716 + Rnd.get(-100, 100), -55920 + Rnd.get(-100, 100), -2032),
		new Location(-58752 + Rnd.get(-100, 100), -56896 + Rnd.get(-100, 100), -2032),
		new Location(-59716 + Rnd.get(-100, 100), -57864 + Rnd.get(-100, 100), -2032)
	};
	
	private TeleportToFantasy()
	{
		addStartNpc(FANTASY_MANAGER);
		addTalkId(FANTASY_MANAGER);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (npc.getId() == FANTASY_MANAGER)
		{
			String var = player.getVariables().getString("FANTASY_RETURN", "");
			if (var.isEmpty())
			{
				player.teleToLocation(83400, 147943, -3404); // 기란
			}
			else
			{
				final String[] loc = var.split(" ");
				int x = Integer.parseInt(loc[0]);
				int y = Integer.parseInt(loc[1]);
				int z = Integer.parseInt(loc[2]);
				
				player.teleToLocation(x, y, z);
				player.getVariables().remove("FANTASY_RETURN");
			}
		}
		else
		{
			player.getVariables().set("FANTASY_RETURN", player.getX() + " " + player.getY() + " " + player.getZ());
			player.teleToLocation(POINTS[Rnd.get(POINTS.length)]);
		}
		return super.onTalk(npc, player);
	}
	
	public static void main(String[] args)
	{
		new TeleportToFantasy();
	}
}
