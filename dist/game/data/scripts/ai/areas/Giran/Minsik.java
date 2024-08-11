package ai.areas.Giran;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

public class Minsik extends AbstractNpcAI
{
	// NPC
	private static final int MINSIK = 40008;
	// Misc
	private static final String[] MINSIK_VOICE =
	{
		"borinet/Minsik_1",
		"borinet/Minsik_2",
		"borinet/Minsik_3",
		"borinet/Minsik_4",
		"borinet/Minsik_5",
		"borinet/Minsik_6",
		"borinet/Minsik_7",
		"borinet/Minsik_8",
		"borinet/Minsik_9",
		"borinet/Minsik_10",
	};
	
	private Minsik()
	{
		addStartNpc(MINSIK);
		addFirstTalkId(MINSIK);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final long reuse = player.getVariables().getLong("Minsik", 0);
		if (reuse <= System.currentTimeMillis())
		{
			player.sendPacket(new PlaySound(2, "borinet/Minsik", 0, 0, 0, 0, 0));
			player.getVariables().set("Minsik", System.currentTimeMillis() + 28000);
			ThreadPool.schedule(new talkNpc(player), 9500);
		}
		return "data/html/default/40008.htm";
	}
	
	private class talkNpc implements Runnable
	{
		private final Player _player;
		
		public talkNpc(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			_player.sendPacket(new PlaySound(2, MINSIK_VOICE[getRandom(10)], 0, 0, 0, 0, 0));
		}
	}
	
	public static void main(String[] args)
	{
		new Minsik();
	}
}
