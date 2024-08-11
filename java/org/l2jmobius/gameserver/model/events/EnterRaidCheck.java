package org.l2jmobius.gameserver.model.events;

import java.util.List;

import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.Broadcast;

public class EnterRaidCheck
{
	public static boolean ConditionCheck(Player player, boolean CC)
	{
		final Party party = player.getParty();
		final List<Player> members = CC ? party.getCommandChannel().getMembers() : party.getMembers();
		
		for (Player member : members)
		{
			if (member.isDead() || member.isFlying() || member.isCursedWeaponEquipped())
			{
				Broadcast.toPlayerScreenMessageS(member, (CC ? "연합채널 맴버" : "파티 맴버 ") + member.getName() + "님의 조건이 충족되지 않아 입장할 수 없습니다.");
				return false;
			}
			else if (!member.isInsideRadius3D(player, 1000))
			{
				Broadcast.toPlayerScreenMessageS(member, (CC ? "연합채널 맴버" : "파티 맴버 ") + member.getName() + "님의 거리가 멀어서 입장할 수 없습니다.");
				return false;
			}
		}
		return true;
	}
}