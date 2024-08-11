package ai.areas.DungeonOfAbyss.SoulTracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;
import instances.DungeonOfAbyss.CoreOfAbyss;

public class SoulTrackers extends AbstractNpcAI
{
	private static final Map<Integer, Map<String, Location>> NPC_TELEPORT_LOCATIONS = new HashMap<>();
	private static final Map<Integer, Integer> NPC_KEYS = new HashMap<>();
	
	// @formatter:off
	static
	{
		NPC_TELEPORT_LOCATIONS.put(31774, Map.of(
			"1", new Location(-119440, -182464, -6752), // Join Room from Magrit
			"2", new Location(-120394, -179651, -6751), // Move to West Wing 2nd
			"3", new Location(-116963, -181492, -6575), // Go to the Condemned of Abyss Prison
			"4", new Location(146945, 26764, -2200) // Return to Aden
		));
		
		NPC_TELEPORT_LOCATIONS.put(31775, Map.of(
			"1", new Location(-119533, -179641, -6751), // Join Room from Ingrit
			"2", new Location(-120325, -182444, -6752), // Move to West Wing 1nd
			"3", new Location(-116975, -178699, -6751), // Go to the Condemned of Abyss Prison
			"4", new Location(146945, 26764, -2200) // Return to Aden
		));
		
		NPC_TELEPORT_LOCATIONS.put(31776, Map.of(
			"1", new Location(-110038, -180560, -6754), // Join Room from Iris
			"2", new Location(-109234, -177737, -6751), // Move to East Wing 2nd
			"3", new Location(-112648, -181517, -6751), // Go to the Condemned of Abyss Prison
			"4", new Location(146945, 26764, -2200) // Return to Aden
		));
		
		NPC_TELEPORT_LOCATIONS.put(31777, Map.of(
			"1", new Location(-110067, -177733, -6751), // Join Room from Rosammy
			"2", new Location(-109202, -180546, -6751), // Move to East Wing 1nd
			"3", new Location(-112632, -178671, -6751), // Go to the Condemned of Abyss Prison
			"4", new Location(146945, 26764, -2200) // Return to Aden
		));
		
		NPC_KEYS.put(31774, 90010);
		NPC_KEYS.put(31775, 90010);
		NPC_KEYS.put(31776, 90011);
		NPC_KEYS.put(31777, 90011);
	}
	// @formatter:on
	
	private SoulTrackers()
	{
		addFirstTalkId(31774, 31775, 31776, 31777);
		addTalkId(31774, 31775, 31776, 31777);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return getHtm(player, npc.getId() + ".htm").replace("%npcName%", npc.getName());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String result;
		switch (event)
		{
			case "1":
			case "2":
			case "4":
				result = teleportToEventLocation(player, npc, event);
				break;
			case "3":
				result = teleportWithKey(player, npc, event);
				break;
			default:
				result = null;
				break;
		}
		
		return result == null ? super.onAdvEvent(event, npc, player) : result;
	}
	
	private String teleportWithKey(Player player, Npc npc, String event)
	{
		if (!hasQuestItems(player, NPC_KEYS.getOrDefault(npc.getId(), 0)))
		{
			return getHtm(player, "no_key.htm").replace("%npcName%", npc.getName());
		}
		return teleportToEventLocation(player, npc, event);
	}
	
	private String teleportToEventLocation(Player player, Npc npc, String event)
	{
		Map<String, Location> locations = NPC_TELEPORT_LOCATIONS.getOrDefault(npc.getId(), Collections.emptyMap());
		Location location = locations.get(event);
		
		if (location != null)
		{
			if ("3".equals(event))
			{
				int keyItemId = NPC_KEYS.getOrDefault(npc.getId(), 0);
				takeItems(player, keyItemId, 1);
				CoreOfAbyss.enterInstance(player, npc.getId());
			}
			else
			{
				player.teleToLocation(location, false);
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new SoulTrackers();
	}
}