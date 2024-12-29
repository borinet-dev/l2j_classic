package ai.areas.Giran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

public class RaidManager extends AbstractNpcAI
{
	// NPC
	private static final int MANAGER = 40005;
	
	private static final String[] MANAGER_VOICE =
	{
		"borinet/40005_1",
		"borinet/40005_2",
		"borinet/40005_3",
	};
	
	private RaidManager()
	{
		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		
		ThreadPool.scheduleAtFixedRate(this::startAnnount, BorinetTask.specialRaidSatrtEvent(), BorinetUtil.MILLIS_PER_DAY); // 1 day
		ThreadPool.scheduleAtFixedRate(this::stopAnnount, BorinetTask.specialRaidSatrtEvent(), BorinetUtil.MILLIS_PER_DAY); // 1 day
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final long reuse = player.getVariables().getLong("RaidVoice", 0);
		if (!BorinetTask.getInstance().specialRaidTime())
		{
			if (reuse <= System.currentTimeMillis())
			{
				player.sendPacket(new PlaySound(2, "borinet/raidvoice", 0, 0, 0, 0, 0));
				player.getVariables().set("RaidVoice", System.currentTimeMillis() + 5000);
			}
			return "data/html/guide/RaidManager-no.htm";
		}
		if (reuse <= System.currentTimeMillis())
		{
			player.sendPacket(new PlaySound(2, MANAGER_VOICE[getRandom(3)], 0, 0, 0, 0, 0));
			player.getVariables().set("RaidVoice", System.currentTimeMillis() + 6000);
		}
		return "data/html/guide/RaidManager.htm";
	}
	
	protected void startAnnount()
	{
		Broadcast.toAllOnlinePlayersOnScreenS("레이드 매니저를 통해서 레이드가 가능합니다!");
	}
	
	protected void stopAnnount()
	{
		Broadcast.toAllOnlinePlayersOnScreenS("스페셜 레이드가 종료되었습니다.");
		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove("여왕개미");
			player.getVariables().remove("오르펜");
			player.getVariables().remove("베레스");
		}
		
		String deleteQueenAntQuery = "DELETE FROM character_variables WHERE var = '여왕개미'";
		String deleteOrfenQuery = "DELETE FROM character_variables WHERE var = '오르펜'";
		String deleteBelethQuery = "DELETE FROM character_variables WHERE var = '베레스'";
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(deleteQueenAntQuery))
			{
				ps.execute();
			}
			try (PreparedStatement ps = con.prepareStatement(deleteOrfenQuery))
			{
				ps.execute();
			}
			try (PreparedStatement ps = con.prepareStatement(deleteBelethQuery))
			{
				ps.execute();
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "변수를 삭제하는데 실패했습니다.");
		}
	}
	
	public static void main(String[] args)
	{
		new RaidManager();
	}
}
