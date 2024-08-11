package org.l2jmobius.gameserver.taskmanager.auto;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author 보리넷 가츠
 */
public class AutoLuckyCoinTaskManager implements Runnable
{
	// Items
	private static final int LUCKY_COIN = 49783;
	private static final int LUCKY_COIN_MIN_REAWRD = 3;
	private static final int LUCKY_COIN_MAX_REAWRD = 6;
	// Skill
	private static final int TRANSFORMATION_SKILL = 39171;
	// Other
	public static final int REWARD_INTERVAL = 60 * 1000; // 1 min
	private static final Set<Player> PLAYER = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	int count = 0;
	
	protected AutoLuckyCoinTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public void run()
	{
		if (_working && (count < 20))
		{
			count++;
			return;
		}
		count = 0;
		_working = true;
		
		REWARD: for (Player player : PLAYER)
		{
			if ((player == null) || (player.isOnlineInt() != 1) || !player.isOnline())
			{
				PLAYER.remove(player);
				continue REWARD;
			}
			
			final long delay = player.getVariables().getLong("LuckyCoin", 0);
			
			if (player.isAffectedBySkill(TRANSFORMATION_SKILL))
			{
				if (delay < System.currentTimeMillis())
				{
					int chance = Rnd.get(30, 65);
					final int count = Rnd.get(LUCKY_COIN_MIN_REAWRD, LUCKY_COIN_MAX_REAWRD);
					
					if (Rnd.chance(chance))
					{
						player.addItem("LuckyCoin", LUCKY_COIN, count, player, false);
						Broadcast.toPlayerScreenMessageS(player, "행운 주화 " + count + "개를 획득하셨습니다.");
					}
					final long times = REWARD_INTERVAL * (Rnd.get(5, 30));
					player.getVariables().set("LuckyCoin", System.currentTimeMillis() + times);
					continue REWARD;
				}
			}
			else if (delay < System.currentTimeMillis())
			{
				PLAYER.remove(player);
				player.getVariables().remove("LuckyCoin");
			}
		}
		_working = false;
	}
	
	public void addAutoLuckyCoin(Player player)
	{
		if (!PLAYER.contains(player))
		{
			final long times = 60000 * (Rnd.get(5, 30));
			player.getVariables().set("LuckyCoin", System.currentTimeMillis() + times);
			PLAYER.add(player);
		}
	}
	
	public static AutoLuckyCoinTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoLuckyCoinTaskManager INSTANCE = new AutoLuckyCoinTaskManager();
	}
}
