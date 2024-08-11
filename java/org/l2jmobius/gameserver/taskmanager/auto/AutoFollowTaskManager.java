package org.l2jmobius.gameserver.taskmanager.auto;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.KorNameUtil;

/**
 * @author 보리넷 가츠
 */
public class AutoFollowTaskManager implements Runnable
{
	private static final Set<Player> PLAYERS = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	int count = 0;
	private static final Set<Stat> SPEED_STATS = EnumSet.of(Stat.RUN_SPEED, Stat.WALK_SPEED, Stat.SWIM_RUN_SPEED, Stat.SWIM_WALK_SPEED, Stat.FLY_RUN_SPEED, Stat.FLY_WALK_SPEED);
	
	protected AutoFollowTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 500, 500);
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
		
		FOLLOW: for (Player player : PLAYERS)
		{
			if ((player == null) || (player.isOnlineInt() != 1) || player.isAlikeDead())
			{
				continue FOLLOW;
			}
			
			if ((player.isInOlympiadMode()))
			{
				stopAutoFollow(player);
				continue FOLLOW;
			}
			
			String[] leaders = BorinetUtil.getInstance().getAutoFollow(player);
			Player leader = World.getInstance().getPlayer(leaders[0]);
			
			if (leader == null)
			{
				PLAYERS.remove(player);
				AutoFollowSearchTaskManager.getInstance().doSerch(player);
				continue FOLLOW;
			}
			else if (player.getTarget() != leader)
			{
				player.setTarget(leader);
			}
			else if (!leader.getVariables().getBoolean("자동따라가기허용", false))
			{
				player.setTarget(null);
				player.sendMessage("타겟은 자동따라가기 모드를 허용하지 않았습니다.");
				stopAutoFollow(player);
				continue FOLLOW;
			}
			else if (leader.isInInstance() || leader.isInOlympiadMode() || leader.isInSiege() || leader.isJailed())
			{
				player.sendMessage("현재 타겟은 따라가기 불가능한 상태입니다.");
				player.sendMessage("자동 따라가기 모드가 불가능 한 곳: 인스턴트 존 | 올림피아드 | 공성지역 | 감옥");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "현재 타겟은 따라가기 불가능한 상태입니다."));
				stopAutoFollow(player);
				continue FOLLOW;
			}
			else
			{
				if (player.isJailed())
				{
					player.sendMessage("수감 중일 경우 따라가기 모드를 사용할 수 없습니다.");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "수감 중일 경우 따라가기 모드를 사용할 수 없습니다."));
					stopAutoFollow(player);
				}
				if (!leader.isInsideRadius3D(player, 1000))
				{
					int x1 = leader.getX();
					int y1 = leader.getY();
					int z1 = leader.getZ();
					
					final int range = 50;
					final int deltaX = Rnd.get(range * 2); // x
					int deltaY = Rnd.get(deltaX, range * 2); // distance
					deltaY = (int) Math.sqrt((deltaY * deltaY) - (deltaX * deltaX)); // y
					x1 = (deltaX + x1) - range;
					y1 = (deltaY + y1) - range;
					z1 = z1 + 50;
					
					if (leader.isInsideRadius3D(player, 2000))
					{
						player.teleToLocation(x1, y1, z1);
						player.setTarget(leader);
						player.getAI().setIntention(CtrlIntention.AI_AUTO_FOLLOW, leader);
					}
					else
					{
						if (player.getInventory().getAdena() < 50000)
						{
							player.sendMessage("텔레포트 비용이 부족하여 따라가기 모드를 종료합니다.");
							leader.sendMessage(player.getName() + "의 텔레포트 비용이 부족하여 따라가기 모드를 종료하였습니다.");
							player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "텔레포트 비용이 부족하여 따라가기 모드를 종료합니다."));
							leader.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, player.getName() + "의 텔레포트 비용이 부족하여 따라가기 모드를 종료하였습니다."));
							stopAutoFollow(player);
						}
						else
						{
							player.teleToLocation(x1, y1, z1);
							player.destroyItemByItemId("자동따라가기", 57, 50000, player, true);
							player.setTarget(leader);
							player.getAI().setIntention(CtrlIntention.AI_AUTO_FOLLOW, leader);
						}
						continue FOLLOW;
					}
				}
				else
				{
					player.setTarget(leader);
					player.getVariables().set("자동활성메세지", false);
					player.getAI().setIntention(CtrlIntention.AI_AUTO_FOLLOW, leader);
				}
			}
		}
		_working = false;
	}
	
	public void doAutoFollow(Player leader, Player player)
	{
		final Creature Character;
		Character = player;
		
		if (!PLAYERS.contains(player))
		{
			player.onActionRequest();
			PLAYERS.add(player);
			SPEED_STATS.forEach(speedStat -> Character.getStat().removeFixedValue(speedStat));
			SPEED_STATS.forEach(speedStat -> Character.getStat().addFixedValue(speedStat, Character.getTemplate().getBaseValue(speedStat, 120) + 160));
			Character.getStat().recalculateStats(false);
			((Player) Character).broadcastUserInfo();
		}
	}
	
	public void stopAutoFollow(Player player)
	{
		final Creature Character;
		Character = player;
		Player leader = null;
		
		String[] leaders = BorinetUtil.getInstance().getAutoFollow(player);
		String leaderName = leaders[0];
		
		if (leaderName != null)
		{
			leader = World.getInstance().getPlayer(leaderName);
		}
		
		PLAYERS.remove(player);
		player.sendMessage("자동 따라가기 모드를 종료합니다.");
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동 따라가기 모드를 종료합니다."));
		if (leader != null)
		{
			leader.sendMessage(KorNameUtil.getName(player.getName(), "이", "가") + " 따라가기 모드를 종료하였습니다.");
			leader.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, KorNameUtil.getName(player.getName(), "이", "가") + " 따라가기 모드를 종료하였습니다."));
		}
		player.setInvul(false);
		player.setBlockActions(false);
		player.setTarget(null);
		player.getVariables().remove("자동따라가기");
		player.getVariables().remove("자동따라가기설정");
		BorinetHtml.getInstance().showAutoFollowHtml(player);
		SPEED_STATS.forEach(speedStat -> Character.getStat().removeFixedValue(speedStat));
		((Player) Character).broadcastUserInfo();
	}
	
	public static AutoFollowTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoFollowTaskManager INSTANCE = new AutoFollowTaskManager();
	}
}
