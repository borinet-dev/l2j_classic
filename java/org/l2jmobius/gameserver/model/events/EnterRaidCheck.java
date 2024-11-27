package org.l2jmobius.gameserver.model.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.Broadcast;

public class EnterRaidCheck
{
	public static boolean ConditionCheck(Player player, boolean CC, List<Player> members)
	{
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
	
	public static void enterMessage(Player player, boolean CC, String bossName, String leaderName, String members, boolean isAnakim)
	{
		Broadcast.toAllOnlinePlayersOnScreenS(leaderName + "님의 " + (CC ? "연합파티가 " : "파티가 ") + bossName + (isAnakim ? "의 성소로" : " 레이드 존으로") + " 이동하였습니다.");
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			String query = "INSERT INTO special_raid_history (enter_time, raid_name, leader_name, members) VALUES (?,?,?,?)";
			try (PreparedStatement ps = con.prepareStatement(query))
			{
				ps.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // 현재 시간
				ps.setString(2, bossName); // 레이드 이름
				ps.setString(3, leaderName); // 리더 이름
				ps.setString(4, members); // 멤버 이름들
				ps.executeUpdate();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}