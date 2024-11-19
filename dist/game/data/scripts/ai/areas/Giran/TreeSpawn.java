package ai.areas.Giran;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2jmobius.gameserver.model.actor.Npc;

import ai.AbstractNpcAI;

public class TreeSpawn extends AbstractNpcAI
{
	private static final List<Npc> _trees = new CopyOnWriteArrayList<>();
	
	private TreeSpawn()
	{
		spawnTrees();
	}
	
	public static void spawnTrees()
	{
		addTree(30627, 80830, 149027, -3457);
		addTree(30627, 80827, 148199, -3457);
		addTree(34279, 82158, 149559, -3464);
		addTree(34279, 81027, 149487, -3464);
		addTree(34279, 81028, 147836, -3464);
		addTree(34279, 81673, 147555, -3464);
		addTree(34279, 82904, 147776, -3464);
		addTree(34279, 82897, 149436, -3464);
		addTree(34280, 82832, 147550, -3464);
		addTree(34280, 83413, 149338, -3400);
	}
	
	private static void addTree(int npcId, int x, int y, int z)
	{
		Npc npc = addSpawn(npcId, x, y, z, 0, false, 0);
		if (npc != null)
		{
			_trees.add(npc);
		}
	}
	
	public static void eventEnd()
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
	}
	
	public static void main(String[] args)
	{
		new TreeSpawn();
	}
}
