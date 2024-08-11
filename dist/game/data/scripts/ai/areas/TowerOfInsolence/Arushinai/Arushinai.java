package ai.areas.TowerOfInsolence.Arushinai;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.HeavenlyRiftManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;

import ai.AbstractNpcAI;

public class Arushinai extends AbstractNpcAI
{
	// NPC
	private static final int ARUSHINAI = 30401;
	
	private Arushinai()
	{
		addStartNpc(ARUSHINAI);
		addTalkId(ARUSHINAI);
		addFirstTalkId(ARUSHINAI);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("30401-1.htm"))
		{
			return event;
		}
		
		if (event.equals("proceed"))
		{
			if (!player.isGM())
			{
				Party party = player.getParty();
				if (party == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
					player.teleToLocation(114264, 13352, -5104);
					return null;
				}
				if (!party.isLeader(player))
				{
					player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
					return null;
				}
			}
			
			HeavenlyRiftManager.stopRunning();
			final int riftLevel = Rnd.get(1, 3);
			GlobalVariablesManager.getInstance().set("heavenly_rift_level", riftLevel);
			GlobalVariablesManager.getInstance().set("heavenly_rift_complete", 4);
			HeavenlyRiftManager.startEvent(player, riftLevel);
			npc.decayMe();
			
			return null;
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Arushinai();
	}
}
