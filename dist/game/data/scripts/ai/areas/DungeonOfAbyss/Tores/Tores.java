package ai.areas.DungeonOfAbyss.Tores;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

public class Tores extends AbstractNpcAI
{
	// NPC
	private static final int TORES = 31778;
	// Locations
	private static final Map<String, Location> LOCATIONS = new HashMap<>();
	static
	{
		// move from Tores
		LOCATIONS.put("1", new Location(-120325, -182444, -6752)); // Move to Magrit
		LOCATIONS.put("2", new Location(-109202, -180546, -6751)); // Move to Iris
	}
	
	private Tores()
	{
		addStartNpc(TORES);
		addTalkId(TORES);
		addFirstTalkId(TORES);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "31778.htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final Location loc = LOCATIONS.get(event);
		
		if (loc != null)
		{
			int minLevel = 0;
			int maxLevel = 0;
			
			switch (event)
			{
				case "1":
					minLevel = 40;
					maxLevel = 44;
					break;
				case "2":
					minLevel = 45;
					maxLevel = 49;
					break;
			}
			
			if ((player.getLevel() >= minLevel) && (player.getLevel() <= maxLevel))
			{
				player.teleToLocation(loc, true);
			}
			else
			{
				return "31778-" + event + ".htm";
			}
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new Tores();
	}
}