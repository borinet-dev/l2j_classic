package ai.areas.Giran;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

public class GateKeeper extends AbstractNpcAI
{
	// NPC
	private static final int[] GATEKEEPERS =
	{
		30006,
		30059,
		30080,
		30134,
		30146,
		30177,
		30233,
		30256,
		30320,
		30540,
		30576,
		30836,
		30848,
		31275,
		31320
	};
	
	private GateKeeper()
	{
		addStartNpc(GATEKEEPERS);
		addFirstTalkId(GATEKEEPERS);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		final long reuse = player.getVariables().getLong("Keeper", 0);
		if (reuse <= System.currentTimeMillis())
		{
			player.sendPacket(new PlaySound(2, "borinet/gatekeeper", 0, 0, 0, 0, 0));
			player.getVariables().set("Keeper", System.currentTimeMillis() + 10000);
		}
		htmltext = getHtm(player, "data/html/teleporter/" + npc.getId() + ".htm");
		htmltext = htmltext.replace("%npcname%", "게이트키퍼 " + npc.getName());
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new GateKeeper();
	}
}
