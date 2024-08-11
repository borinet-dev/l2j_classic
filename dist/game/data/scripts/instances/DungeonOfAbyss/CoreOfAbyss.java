package instances.DungeonOfAbyss;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;

import instances.AbstractInstance;

public class CoreOfAbyss extends AbstractInstance
{
	// @formatter:off
	private static final int TEMPLATE_ID = 203;
	private static final int DURATION = 30;
	
	// Teleport points into instances x, y, z
	private static final Location TELEPORTS = new Location(-116963, -181492, -6575);
	
	private static final int[][] SPAWNS =
	{
		{-115828, -181639, -6752},
		{-115816, -181377, -6752},
		{-116091, -181381, -6752},
		{-116084, -181645, -6752},
		{-116328, -181649, -6752},
		{-116347, -181381, -6752},
		{-116596, -181381, -6752},
		{-116586, -181630, -6752},
		{-116821, -181651, -6752},
		{-116830, -181373, -6752}
	};
	
	// Respawn delay for the mobs in the first room, seconds Default: 25
	private static final int RESPAWN_DELAY = 25;
	
	private static final int 영원의_사형수 = 21643;
	private static final int 심연의_사형수 = 21649;
	// @formatter:on
	
	public CoreOfAbyss()
	{
		super(TEMPLATE_ID);
	}
	
	public final synchronized static void enterInstance(Player player, int npcId)
	{
		int templateId = TEMPLATE_ID;
		
		// check for existing instances for this player
		Instance world = player.getInstanceWorld();
		// player already in the instance
		if (world != null)
		{
			if (world.getTemplateId() != templateId)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
				return;
			}
			// check what instance still exist
			final Instance inst = InstanceManager.getInstance().getInstance(world.getId());
			if (inst != null)
			{
				player.teleToLocation(TELEPORTS, world);
			}
			return;
		}
		
		world = InstanceManager.getInstance().createInstance(templateId, player);
		if (world != null)
		{
			world.setDuration(DURATION * 60000);
			world.setStatus(0);
			spawn(world, npcId);
			
			player.teleToLocation(TELEPORTS, world);
			player.sendPacket(new ExSendUIEvent(player, false, false, DURATION * 60, 0, NpcStringId.REMAINING_TIME));
			ThreadPool.schedule(() -> player.sendPacket(new ExSendUIEvent(player, true, false, 0, 0, NpcStringId.REMAINING_TIME)), 11000);
		}
	}
	
	private final static void spawn(Instance world, int npcIds)
	{
		int[][] spawns = SPAWNS;
		int npcId = ((npcIds == 31774) || (npcIds == 31775)) ? 영원의_사형수 : 심연의_사형수;
		
		for (int i = 0; i < spawns.length; i++)
		{
			int[] spawn = spawns[i];
			Npc npc = Spawn(npcId, spawn[0], spawn[1], spawn[2], 0, 300000, world.getId());
			final Spawn spawnObj = npc.getSpawn();
			spawnObj.setRespawnDelay(RESPAWN_DELAY);
			spawnObj.setAmount(1);
			spawnObj.startRespawn();
			npc.setRandomWalking(true);
		}
	}
	
	private static Npc Spawn(int npcId, int xValue, int yValue, int zValue, int heading, long despawnDelay, int instance)
	{
		try
		{
			final Spawn spawn = new Spawn(npcId);
			if ((xValue != 0) || (yValue != 0))
			{
				spawn.setInstanceId(instance);
				spawn.setHeading(heading);
				spawn.setXYZ(xValue, yValue, zValue);
				spawn.stopRespawn();
				
				Npc npc = spawn.doSpawn(false);
				if (despawnDelay > 0)
				{
					ThreadPool.schedule(npc::decayMe, despawnDelay);
				}
				return npc;
			}
		}
		catch (Exception e)
		{
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new CoreOfAbyss();
	}
}
