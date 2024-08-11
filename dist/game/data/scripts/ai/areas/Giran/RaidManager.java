package ai.areas.Giran;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
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
		scheduleSatrtEvent();
		scheduleStopEvent();
		
		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final long currentTime = System.currentTimeMillis();
		
		final Calendar starttime = Calendar.getInstance();
		starttime.set(Calendar.MINUTE, 30);
		starttime.set(Calendar.SECOND, 0);
		starttime.set(Calendar.HOUR_OF_DAY, 19);
		
		final Calendar endtime = Calendar.getInstance();
		endtime.set(Calendar.MINUTE, 0);
		endtime.set(Calendar.SECOND, 0);
		endtime.set(Calendar.HOUR_OF_DAY, 01);
		
		final long reuse = player.getVariables().getLong("RaidVoice", 0);
		if ((currentTime > endtime.getTimeInMillis()) && (currentTime < starttime.getTimeInMillis()))
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
	
	private void scheduleSatrtEvent()
	{
		final long currentTime = System.currentTimeMillis();
		final Calendar starttime = Calendar.getInstance();
		starttime.set(Calendar.MINUTE, 30);
		starttime.set(Calendar.SECOND, 0);
		starttime.set(Calendar.HOUR_OF_DAY, 19);
		
		if (starttime.getTimeInMillis() < currentTime)
		{
			starttime.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long startDelay = Math.max(0, starttime.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::startAnnount, startDelay, BorinetUtil.MILLIS_PER_DAY);
	}
	
	private void scheduleStopEvent()
	{
		final long currentTime = System.currentTimeMillis();
		final Calendar endtime = Calendar.getInstance();
		endtime.set(Calendar.MINUTE, 0);
		endtime.set(Calendar.SECOND, 0);
		endtime.set(Calendar.HOUR_OF_DAY, 01);
		
		if (endtime.getTimeInMillis() < currentTime)
		{
			endtime.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		final long endDelay = Math.max(0, endtime.getTimeInMillis() - currentTime);
		ThreadPool.scheduleAtFixedRate(this::stopAnnount, endDelay, BorinetUtil.MILLIS_PER_DAY);
	}
	
	protected void startAnnount()
	{
		Broadcast.toAllOnlinePlayersOnScreenS("레이드 매니저를 통해서 레이드가 가능합니다!");
	}
	
	protected void stopAnnount()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			Broadcast.toAllOnlinePlayersOnScreenS("스페셜 레이드가 종료되었습니다.");
			player.getVariables().remove("여왕개미");
			player.getVariables().remove("오르펜");
		}
		
		String deleteQueenAntQuery = "DELETE FROM character_variables WHERE var = '여왕개미'";
		String deleteOrfenQuery = "DELETE FROM character_variables WHERE var = '오르펜'";
		
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
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "캐릭터 변수를 삭제하는데 실패했습니다.");
		}
	}
	
	public static void main(String[] args)
	{
		new RaidManager();
	}
}
