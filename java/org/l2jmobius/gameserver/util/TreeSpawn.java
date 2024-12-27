package org.l2jmobius.gameserver.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.events.AbstractScript;

public class TreeSpawn
{
	private static final List<Npc> _trees = new CopyOnWriteArrayList<>();
	private static final ScheduledExecutorService _scheduler = Executors.newSingleThreadScheduledExecutor();
	
	public static void spawnTrees()
	{
		if (BorinetUtil.getInstance().isWinterActive())
		{
			startWinterCheckTask();
			addTree(34279, 81027, 149487, -3464);
			addTree(34279, 81028, 147836, -3464);
			addTree(34279, 82904, 147776, -3464);
			addTree(34279, 82897, 149436, -3464);
			addTree(34280, 82832, 147550, -3464);
			addTree(34280, 83413, 149338, -3400);
			addTree(34279, 83644, 148383, -3400);
			addTree(34279, 83654, 148863, -3400);
			addTree(34280, 82289, 148677, -3440);
			addTree(34280, 82299, 148553, -3440);
			addTree(34279, -84276, 243618, -3728);
			addTree(34279, -84357, 243496, -3728);
			addTree(34279, -84870, 242801, -3728);
			addTree(34279, -84027, 243056, -3728);
			addTree(34279, -84231, 242283, -3728);
			addTree(34280, -84128, 243082, -3704);
			addTree(34280, -84373, 243144, -3704);
		}
	}
	
	private static void addTree(int npcId, int x, int y, int z)
	{
		Npc npc = AbstractScript.addSpawn(npcId, x, y, z, 0, false, 0);
		if (npc != null)
		{
			_trees.add(npc);
		}
	}
	
	public static void unspawnTrees()
	{
		if ((_trees == null) || _trees.isEmpty())
		{
			return;
		}
		
		for (Npc npc : _trees)
		{
			if (npc != null)
			{
				try
				{
					npc.deleteMe();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		_trees.clear();
		
		// 스케줄러 종료
		if (!_scheduler.isShutdown())
		{
			_scheduler.shutdown();
		}
	}
	
	private static void startWinterCheckTask()
	{
		// 1시간마다 겨울 상태를 확인
		_scheduler.scheduleAtFixedRate(() ->
		{
			try
			{
				if (!BorinetUtil.getInstance().isWinterActive())
				{
					unspawnTrees();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}, 0, 1, TimeUnit.HOURS); // 주기를 1시간으로 설정
	}
}
