package custom.events.WaterMelon;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Event;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * @author 보리넷 가츠
 */
public class WaterMelonTask extends Event
{
	// Event state
	public static boolean _isActive = false;
	private List<Npc> _npclist;
	
	public WaterMelonTask()
	{
		if (Config.WATERMELON_EVENT_ENABLED && !BorinetTask._isActive)
		{
			if (BorinetTask.WeekendCheck())
			{
				startEvent();
			}
			
			ThreadPool.scheduleAtFixedRate(this::startEvent, BorinetTask.WeekendStartDelay(), BorinetUtil.MILLIS_PER_DAY); // 1 day
			ThreadPool.scheduleAtFixedRate(this::stopEvent, BorinetTask.WeekendEndDelay(), BorinetUtil.MILLIS_PER_DAY); // 1 day
		}
	}
	
	private void startEvent()
	{
		if (!_isActive && BorinetTask.WeekendCheck())
		{
			_npclist = new ArrayList<>();
			
			// Set inactive
			_isActive = true;
			
			// Announce
			for (Player player : World.getInstance().getPlayers())
			{
				player.sendMessage("이벤트: 왕수박 이벤트가 주말동안 진행됩니다!");
			}
			
			Npc npc = addSpawn(WaterMelon.MANAGER, 83596, 148863, -3400, 31532, false, 0);
			if (npc != null)
			{
				_npclist.add(npc);
			}
			
			LOGGER.info("왕수박 이벤트: 왕수박 이벤트가 시작 되었습니다.");
		}
	}
	
	private void stopEvent()
	{
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
		{
			if (_isActive)
			{
				// Set inactive
				_isActive = false;
				
				// Despawn NPCs
				for (Npc npc : _npclist)
				{
					if (npc != null)
					{
						npc.deleteMe();
					}
				}
				_npclist.clear();
				
				// Announce event end
				for (Player player : World.getInstance().getPlayers())
				{
					player.sendMessage("이벤트: 왕수박 이벤트가 종료 되었습니다!");
					player.getVariables().remove("WaterMelonBuff_reuse");
				}
				LOGGER.info("왕수박 이벤트: 왕수박 이벤트가 종료 되었습니다.");
				
				try (Connection con = DatabaseFactory.getConnection();
					Statement statement = con.createStatement())
				{
					statement.executeUpdate("DELETE FROM character_variables WHERE var = 'WaterMelonBuff_reuse';");
				}
				catch (Exception e)
				{
					LOGGER.warning("왕수박 이벤트 정리 오류" + e);
				}
			}
		}
	}
	
	@Override
	public boolean eventStart(Player eventMaker)
	{
		return true;
	}
	
	@Override
	public boolean eventStop()
	{
		return true;
	}
	
	@Override
	public boolean eventBypass(Player player, String bypass)
	{
		return false;
	}
	
	public static void main(String[] args)
	{
		new WaterMelonTask();
	}
}