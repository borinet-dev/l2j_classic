package ai.areas.Master;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.AbstractScript;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class Master extends AbstractNpcAI
{
	private static final Location[] FIREN_POS =
	{
		new Location(188398, -115303, -3285),
		new Location(187694, -115479, -3285),
		new Location(186341, -114321, -3285),
		new Location(185097, -113375, -3285),
		new Location(186667, -113071, -3285),
		new Location(187896, -113207, -3285)
	};
	private static final Location[] TORTAY_POS =
	{
		new Location(176937, -56495, -3472),
		new Location(176312, -51078, -3472),
		new Location(178801, -46005, -3472),
		new Location(170189, -54326, -3472)
	};
	private static final Location[] ISHTIR_POS =
	{
		new Location(77864, 257448, -10381),
		new Location(76376, 257432, -10381),
		new Location(87560, 247048, -10381),
		new Location(89984, 247080, -10381),
		new Location(89144, 259016, -10381),
		new Location(87649, 259010, -10381),
		new Location(87656, 257400, -10381),
		new Location(75448, 247064, -10381),
		new Location(77880, 247064, -10381)
	};
	private static final Location[] RODIN_POS =
	{
		new Location(180826, 175627, -2808),
		new Location(183964, 174281, -2808),
		new Location(181930, 178467, -2808),
		new Location(185441, 179781, -2808),
		new Location(184652, 187385, -2808),
		new Location(182157, 177830, -2808),
		new Location(182000, 170518, -2808)
	};
	
	private static final int FIREN = 21763;
	private static final int TORTAY = 21766;
	private static final int ISHTIR = 21769;
	private static final int RODIN = 21772;
	
	private Master()
	{
		addKillId(FIREN);
		addKillId(TORTAY);
		addKillId(ISHTIR);
		addKillId(RODIN);
		addSpawnId(FIREN);
		addSpawnId(TORTAY);
		addSpawnId(ISHTIR);
		addSpawnId(RODIN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "spawn_Firen":
				final int a = getRandom(5);
				AbstractScript.addSpawn(FIREN, FIREN_POS[a], false, 0);
				break;
			case "spawn_Tortay":
				final int b = getRandom(3);
				AbstractScript.addSpawn(TORTAY, TORTAY_POS[b], false, 0);
				break;
			case "spawn_Ishtir":
				final int c = getRandom(8);
				AbstractScript.addSpawn(ISHTIR, ISHTIR_POS[c], false, 0);
				break;
			case "spawn_Rodin":
				final int d = getRandom(6);
				AbstractScript.addSpawn(RODIN, RODIN_POS[d], false, 0);
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final int i = Rnd.get(1500, 1800);
		switch (npc.getId())
		{
			case FIREN:
				startQuestTimer("spawn_Firen", i * 1000, null, null);
				break;
			case TORTAY:
				startQuestTimer("spawn_Tortay", i * 1000, null, null);
				break;
			case ISHTIR:
				startQuestTimer("spawn_Ishtir", i * 1000, null, null);
				break;
			case RODIN:
				startQuestTimer("spawn_Rodin", i * 1000, null, null);
				break;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Master();
	}
}
