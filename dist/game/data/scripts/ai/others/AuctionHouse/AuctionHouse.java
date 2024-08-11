package ai.others.AuctionHouse;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * 판매 대행 관리자.
 * @author 보리넷
 */
public class AuctionHouse extends AbstractNpcAI
{
	// NPCs
	private static final int[] AUCTION_HOUSE_MANAGER =
	{
		33417,
		33418,
		33447,
		33528,
		33529,
		33530,
		33531,
		33532,
		33533,
		33534,
		33551,
		33552,
		33949
	};
	
	private AuctionHouse()
	{
		addSpawnId(AUCTION_HOUSE_MANAGER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "SPAM_TEXT":
			{
				int say = Rnd.get(1, 7);
				int min = Rnd.get(1, 5);
				int sec = Rnd.get(5, 30) * 1000;
				switch (say)
				{
					case 1:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.판매대행매니저1);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 2:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.판매대행매니저2);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 3:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.판매대행매니저3);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 4:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.판매대행매니저4);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 5:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.판매대행매니저5);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 6:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.판매대행매니저6);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 7:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.판매대행매니저7);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		int min = Rnd.get(3, 8);
		int sec = Rnd.get(5, 30) * 1000;
		startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new AuctionHouse();
	}
}