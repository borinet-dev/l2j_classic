package ai.areas.TowerOfInsolence.DimensionalVortex;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.HeavenlyRiftManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

public class DimensionalVortex extends AbstractNpcAI
{
	// NPC
	private static final int DIMENTIONAL_VORTEX = 30952;
	// Items
	private static final int CELESTIAL_SHARD = 49759;
	private static final int BROKEN_CELESTIAL_SHARD = 49767;
	
	private DimensionalVortex()
	{
		addStartNpc(DIMENTIONAL_VORTEX);
		addTalkId(DIMENTIONAL_VORTEX);
		addFirstTalkId(DIMENTIONAL_VORTEX);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("30952.htm") || event.equals("30952-1.htm") || event.equals("30952-2.htm") || event.equals("30952-3.htm"))
		{
			return event;
		}
		
		if (event.equals("tryenter"))
		{
			if (getQuestItemsCount(player, CELESTIAL_SHARD) >= 1)
			{
				if (checkParty(player))
				{
					final Party party = player.getParty();
					for (Player partyMember : party.getMembers())
					{
						partyMember.teleToLocation(112354, 13656, 10984);
						BorinetUtil.insertRift(partyMember);
					}
					player.destroyItemByItemId("Rift", CELESTIAL_SHARD, 1, npc, true);
					HeavenlyRiftManager.spawnMonster(30401, 112291, 13715, 10984, 1200000);
				}
			}
			else
			{
				return "30952-3.htm";
			}
		}
		else if (event.equals("exchange"))
		{
			long count = getQuestItemsCount(player, BROKEN_CELESTIAL_SHARD);
			if (count < 10)
			{
				player.sendMessage("천공의 부서진 파편이 부족합니다.");
				return "30952-2.htm";
			}
			if ((count % 10) != 0)
			{
				count -= count % 10;
			}
			final long reward = count / 10;
			player.destroyItemByItemId("Rift", BROKEN_CELESTIAL_SHARD, count, npc, true);
			player.addItem("Rift", CELESTIAL_SHARD, reward, npc, true);
		}
		return null;
	}
	
	public boolean checkParty(Player player)
	{
		if (GlobalVariablesManager.getInstance().getInt("heavenly_rift_complete", 0) > 0)
		{
			player.sendMessage("현재 다른 파티가 이용중이므로 잠시 후 이동가능합니다.");
			return false;
		}
		
		final Party party = player.getParty();
		if (party != null)
		{
			final List<Player> members = party.getMembers();
			if (!party.isLeader(player))
			{
				player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
				return false;
			}
			for (Player partyMember : members)
			{
				if (BorinetUtil.checkRift(partyMember) > 4)
				{
					Broadcast.toPlayerScreenMessageS(partyMember, partyMember.getName() + "님의 입장가능 횟수가 초과하였습니다.");
					return false;
				}
				if (!partyMember.isInsideRadius3D(player, Config.ALT_PARTY_RANGE))
				{
					Broadcast.toPlayerScreenMessageS(partyMember, partyMember.getName() + "님의 거리가 멀어서 입장할 수 없습니다.");
					return false;
				}
				if (partyMember.getLevel() < 76)
				{
					Broadcast.toPlayerScreenMessageS(partyMember, partyMember.getName() + "님의 레벨이 낮아서 입장할 수 없습니다.");
				}
				if ((members.size() < 3) || (members.size() > 5))
				{
					Broadcast.toPlayerScreenMessageS(partyMember, "입장가능 인원은 최소 3명 ~ 최대 5명 입니다.");
					return false;
				}
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return false;
		}
		
		return true;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new DimensionalVortex();
	}
}
