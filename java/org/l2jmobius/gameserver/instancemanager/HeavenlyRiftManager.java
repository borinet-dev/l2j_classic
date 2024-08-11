package org.l2jmobius.gameserver.instancemanager;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.AbstractScript;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerRiftMania;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

public class HeavenlyRiftManager extends AbstractScript
{
	public static final ZoneType ZONE = ZoneManager.getInstance().getZoneByName("heavenly_rift");
	public static Npc _tower = null;
	private static ScheduledFuture<?> RUNNING = null;
	private static ScheduledFuture<?> END_TIME = null;
	
	private static ScheduledFuture<?> TASK;
	private static int SECONDS = 0;
	public static boolean isStarted = false;
	public static boolean isSucsess = false;
	
	public static final int[] DIVINE_ANGELS =
	{
		36697,
		36698,
		36699,
		36700,
		36701,
		36702,
		36703,
		36704,
		36705
	};
	
	private static final int[] ITEM_REWARD =
	{
		39373,
		49765
	};
	
	public static ZoneType getZone()
	{
		return ZONE;
	}
	
	public static int getAliveNpcCount(int npcId)
	{
		int result = 0;
		for (Creature creature : ZONE.getCharactersInside())
		{
			if (creature.isMonster() && !creature.isDead() && (creature.getId() == npcId))
			{
				result++;
			}
		}
		return result;
	}
	
	public static void startEvent(Player player, int zoneId)
	{
		isStarted = true;
		RUNNING = ThreadPool.schedule(() ->
		{
			endRift(false, zoneId);
		}, 1200000);
		END_TIME = ThreadPool.schedule(() ->
		{
			ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.HEAVENLY_RIFT_WILL_BE_GONE_IN_5_MINUTES, 2, 5000));
		}, 900000);
		
		if (zoneId == 1)
		{
			ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.SET_OFF_BOMBS_AND_GET_TREASURES, 2, 5000));
			spawnMonster(18003, 113352, 12936, 10984, 1200000);
			spawnMonster(18003, 113592, 13272, 10984, 1200000);
			spawnMonster(18003, 113816, 13592, 10984, 1200000);
			spawnMonster(18003, 113080, 13192, 10984, 1200000);
			spawnMonster(18003, 113336, 13528, 10984, 1200000);
			spawnMonster(18003, 113560, 13832, 10984, 1200000);
			spawnMonster(18003, 112776, 13512, 10984, 1200000);
			spawnMonster(18003, 113064, 13784, 10984, 1200000);
			spawnMonster(18003, 112440, 13848, 10984, 1200000);
			spawnMonster(18003, 112728, 14104, 10984, 1200000);
			spawnMonster(18003, 112760, 14600, 10984, 1200000);
			spawnMonster(18003, 112392, 14456, 10984, 1200000);
			spawnMonster(18003, 112104, 14184, 10984, 1200000);
			spawnMonster(18003, 111816, 14488, 10984, 1200000);
			spawnMonster(18003, 112104, 14760, 10984, 1200000);
			spawnMonster(18003, 112392, 15032, 10984, 1200000);
			spawnMonster(18003, 112120, 15288, 10984, 1200000);
			spawnMonster(18003, 111784, 15064, 10984, 1200000);
			spawnMonster(18003, 111480, 14824, 10984, 1200000);
			spawnMonster(18003, 113144, 14216, 10984, 1200000);
		}
		else if (zoneId == 2)
		{
			isSucsess = false;
			int count = Rnd.get(25, 35);
			ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.PROTECT_THE_CENTRAL_TOWER_FROM_DIVINE_ANGELS, 2, 5000));
			_tower = addSpawn(18004, 112696, 14121, 10984, 0, false, 1200000);
			ThreadPool.schedule(() ->
			{
				for (int i = 0; i < count; ++i)
				{
					spawnAngels(DIVINE_ANGELS, 112696 + getRandom(-350, 350), 13960 + getRandom(-350, 350), 10984, 1200000);
				}
			}, 5000);
		}
		else if (zoneId == 3)
		{
			int count = Rnd.get(35, 45);
			ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.DESTROY_WEAKENED_DIVINE_ANGELS, 2, 5000));
			for (int i = 0; i < count; ++i)
			{
				spawnAngels(DIVINE_ANGELS, 112696 + getRandom(-350, 350), 13960 + getRandom(-350, 350), 10984, 1200000);
			}
		}
	}
	
	public static void spawnAngels(int[] npcIds, int x, int y, int z, long despawnTime)
	{
		try
		{
			Random random = new Random();
			int randomNpcId = npcIds[random.nextInt(npcIds.length)]; // 랜덤으로 선택
			
			Spawn spawn = new Spawn(randomNpcId);
			Location location = new Location(x, y, z);
			spawn.setLocation(location);
			Npc npc = spawn.doSpawn();
			npc.scheduleDespawn(despawnTime);
		}
		catch (Exception e)
		{
		}
	}
	
	public static void spawnMonster(int npcId, int x, int y, int z, long despawnTime)
	{
		try
		{
			Spawn spawn = new Spawn(npcId);
			Location location = new Location(x, y, z);
			spawn.setLocation(location);
			Npc npc = spawn.doSpawn();
			npc.scheduleDespawn(despawnTime);
		}
		catch (Exception e)
		{
		}
	}
	
	public static void stopRunning()
	{
		if (RUNNING != null)
		{
			RUNNING.cancel(true);
			RUNNING = null;
		}
		
		if (END_TIME != null)
		{
			END_TIME.cancel(true);
			END_TIME = null;
		}
		
		if (_tower != null)
		{
			_tower = null;
		}
	}
	
	@Override
	public String getScriptName()
	{
		return null;
	}
	
	public static void ClearZone()
	{
		for (Creature creature : ZONE.getCharactersInside())
		{
			if (creature.isPlayer())
			{
				creature.teleToLocation(114264, 13352, -5104);
			}
			GlobalVariablesManager.getInstance().set("heavenly_rift_complete", 0);
			GlobalVariablesManager.getInstance().set("heavenly_rift_level", 0);
			creature.decayMe();
		}
	}
	
	public static void endRift(boolean sucsess, int zone)
	{
		isStarted = false;
		if (!sucsess)
		{
			for (Creature creature : ZONE.getCharactersInside())
			{
				if (creature.isMonster() && !creature.isDead())
				{
					creature.deleteMe();
					ThreadPool.schedule(() ->
					{
						creature.deleteMe();
					}, 1000);
				}
			}
			if (zone == 2)
			{
				ZONE.broadcastPacket(new ExShowScreenMessage("디펜스에 실패하였습니다!", 2, 5000));
			}
			else
			{
				ZONE.broadcastPacket(new ExShowScreenMessage("실패하였습니다!", 2, 5000));
			}
		}
		else
		{
			for (Creature creature : ZONE.getCharactersInside())
			{
				if (creature.isPlayer())
				{
					int count = Rnd.get(1, 2);
					((Player) creature).addItem("완료보상", 41253, count, null, true);
					if (zone == 2)
					{
						((Player) creature).addItem("완료보상", ITEM_REWARD[getRandom(ITEM_REWARD.length)], 1, null, true);
					}
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerRiftMania(((Player) creature)), ((Player) creature));
				}
			}
		}
		stopRunning();
		stopTask();
		ClearMessage();
	}
	
	public static void ClearMessage()
	{
		TASK = ThreadPool.scheduleAtFixedRate(() ->
		{
			SECONDS++;
			sendMessage(SECONDS);
		}, 0, 1000);
	}
	
	public static void stopTask()
	{
		if (TASK != null)
		{
			TASK.cancel(false);
			TASK = null;
		}
	}
	
	private static void sendMessage(int sec)
	{
		int count = 0;
		switch (sec)
		{
			case 5:
				count = 10;
				break;
			case 6:
				count = 9;
				break;
			case 7:
				count = 8;
				break;
			case 8:
				count = 7;
				break;
			case 9:
				count = 6;
				break;
			case 10:
				count = 5;
				break;
			case 11:
				count = 4;
				break;
			case 12:
				count = 3;
				break;
			case 13:
				count = 2;
				break;
			case 14:
				count = 1;
				break;
		}
		if ((sec >= 5) && (sec < 15))
		{
			ZONE.broadcastPacket(new ExShowScreenMessage(count + "초 후 외부로 이동됩니다. 잠시만 기다려주세요!", ExShowScreenMessage.TOP_CENTER, 1000, 0, false, true));
		}
		else if (sec == 15)
		{
			ZONE.broadcastPacket(new ExShowScreenMessage("지금 외부로 이동됩니다!", ExShowScreenMessage.TOP_CENTER, 1000, 0, false, true));
			stopTask();
			ClearZone();
			SECONDS = 0;
		}
	}
}
