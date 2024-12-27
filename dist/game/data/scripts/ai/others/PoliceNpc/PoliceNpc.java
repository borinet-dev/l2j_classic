package ai.others.PoliceNpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetTask;

import ai.AbstractNpcAI;
import smartguard.core.utils.Rnd;

public class PoliceNpc extends AbstractNpcAI
{
	ScheduledFuture<?> spawnTask;
	// 자경단
	private static final int 폴리네 = 40020;
	private static final int 노바 = 81009;
	private static final int 마스라스 = 40027;
	private static final int 베라 = 40028;
	
	//@formatter:off
	private static final int[][] SPAWN_POINTS =
	{
        {81827, 147881}, // 폴리네의 스폰 좌표
        {82915, 149088}, // 노바의 스폰 좌표
        {81928, 149329}, // 마스라스의 스폰 좌표
        {81111, 148627}  // 베라의 스폰 좌표
	};
	//@formatter:on
	
	private static final List<Npc> _npcList = new ArrayList<>();
	private static final Random random = new Random();
	
	private PoliceNpc()
	{
		if (BorinetTask._isActive)
		{
			return;
		}
		if (spawnTask != null)
		{
			spawnTask.cancel(true);
		}
		addFirstTalkId(폴리네, 노바, 마스라스, 베라);
		addTalkId(폴리네, 노바, 마스라스, 베라);
		spawnTask = ThreadPool.scheduleAtFixedRate(this::Spawn, 5000, 1800000);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getId() == 폴리네)
		{
			return getHtm(player, "index_p.htm").replace("%npcName%", npc.getName());
		}
		return getHtm(player, "index.htm").replace("%npcName%", npc.getName());
	}
	
	private void Spawn()
	{
		_npcList.forEach(Npc::deleteMe);
		_npcList.clear();
		
		List<Integer> spawnedNpcIds = new ArrayList<>();
		int[] npcIds =
		{
			폴리네,
			노바,
			마스라스,
			베라
		};
		
		for (int[] spawnPoint : SPAWN_POINTS)
		{
			int x = spawnPoint[0] + Rnd.get(-100, 100);
			int y = spawnPoint[1] + Rnd.get(-100, 100);
			
			// 이전에 스폰된 NPC ID들을 제외하고 선택
			int npcId = selectNonDuplicateNpcId(npcIds, spawnedNpcIds);
			Npc npc = addSpawn(npcId, x, y, -3357, 0, true, 0);
			if (npc != null)
			{
				npc.setWalking();
				_npcList.add(npc);
				spawnedNpcIds.add(npcId);
			}
		}
	}
	
	private static int selectNonDuplicateNpcId(int[] npcIds, List<Integer> spawnedNpcIds)
	{
		List<Integer> availableNpcIds = new ArrayList<>();
		for (int npcId : npcIds)
		{
			if (!spawnedNpcIds.contains(npcId))
			{
				availableNpcIds.add(npcId);
			}
		}
		return availableNpcIds.get(random.nextInt(availableNpcIds.size()));
	}
	
	public static void main(String[] args)
	{
		new PoliceNpc();
	}
}
