package org.l2jmobius.gameserver.util;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Player;

public class BoatReward
{
	public static class RewardItem
	{
		public final int itemId;
		public final int minCount;
		public final int maxCount;
		
		public RewardItem(String configValue)
		{
			String[] values = configValue.split(",");
			this.itemId = Integer.parseInt(values[0]);
			this.minCount = Integer.parseInt(values[1]);
			this.maxCount = Integer.parseInt(values[2]);
		}
	}
	
	public static void scheduleRewardCheck(Player player)
	{
		boolean rewardCheck = player.getQuickVarB("boatReward", false);
		if (!rewardCheck || !Config.ALLOW_REWARD)
		{
			return;
		}
		
		int delayInSeconds = Rnd.get(Config.BOAT_REWARD_SCHEDULE_MIN_SEC, Config.BOAT_REWARD_SCHEDULE_MAX_SEC);
		ThreadPool.schedule(() ->
		{
			distributeReward(player);
			scheduleRewardCheck(player); // 다음 실행을 다시 예약
		}, delayInSeconds * 1000); // 밀리초로 변환
	}
	
	public static void distributeReward(Player player)
	{
		boolean rewardCheck = player.getQuickVarB("boatReward", false);
		if (!rewardCheck || !Config.ALLOW_REWARD)
		{
			return;
		}
		
		int randomValue = Rnd.get(100);
		RewardItem rewardItem = null;
		
		if (randomValue < 50)
		{
			rewardItem = new RewardItem(Config.BOAT_REWARD_CHANCE_50);
		}
		else if (randomValue < 75)
		{
			rewardItem = new RewardItem(Config.BOAT_REWARD_CHANCE_25);
		}
		else if (randomValue < 90)
		{
			rewardItem = new RewardItem(Config.BOAT_REWARD_CHANCE_15);
		}
		else if (randomValue < 97)
		{
			rewardItem = new RewardItem(Config.BOAT_REWARD_CHANCE_7);
		}
		else
		{
			rewardItem = new RewardItem(Config.BOAT_REWARD_CHANCE_3);
		}
		
		int itemId = rewardItem.itemId;
		int itemCount = Rnd.get(rewardItem.minCount, rewardItem.maxCount);
		player.addItem("정기선 보상", itemId, itemCount, null, true);
	}
	
	public static BoatReward getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BoatReward INSTANCE = new BoatReward();
	}
}