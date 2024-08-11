package ai.areas.TowerOfInsolence.Ateld;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;

import ai.AbstractNpcAI;

public class Ateld extends AbstractNpcAI
{
	// NPC
	private static final int ATELD = 31714;
	// Location
	private static final Location TELEPORT_LOC = new Location(115322, 16756, 9012);
	// Misc
	private static final NpcStringId[] TEXT =
	{
		NpcStringId.LET_S_JOIN_OUR_FORCES_AND_FACE_THIS_TOGETHER,
		NpcStringId.BALTHUS_KNIGHTS_ARE_LOOKING_FOR_MERCENARIES
	};
	
	private Ateld()
	{
		addFirstTalkId(ATELD);
		addTalkId(ATELD);
		addSpawnId(ATELD);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "teleToBaium":
			{
				if ((player.getCommandChannel() == null) || (player.getCommandChannel().getLeader() != player) || (player.getCommandChannel().getMemberCount() < 27) || (player.getCommandChannel().getMemberCount() > 300))
				{
					return "31714-01.html";
				}
				for (Player member : player.getCommandChannel().getMembers())
				{
					if ((member != null) && (member.getLevel() > 70))
					{
						member.teleToLocation(TELEPORT_LOC);
					}
				}
				break;
			}
			case "CHAT_TIMER":
			{
				npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, TEXT[Rnd.get(TEXT.length)]));
				startQuestTimer("CHAT_TIMER", 30000, npc, null);
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "31714.html";
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("CHAT_TIMER", 5000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Ateld();
	}
}
