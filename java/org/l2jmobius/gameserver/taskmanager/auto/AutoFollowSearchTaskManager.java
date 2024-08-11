package org.l2jmobius.gameserver.taskmanager.auto;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * @author 보리넷 가츠
 */
public class AutoFollowSearchTaskManager implements Runnable
{
	private static final Set<Player> PLAYERS = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	int count = 0;
	
	protected AutoFollowSearchTaskManager()
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
		
		SEARCH: for (Player player : PLAYERS)
		{
			if ((player == null) || player.isAlikeDead() || (player.isOnlineInt() != 1) || (player.isInOlympiadMode()))
			{
				stopSerch(player);
				continue SEARCH;
			}
			
			if (player.getVariables().getBoolean("자동따라가기", false))
			{
				String[] leaders = BorinetUtil.getInstance().getAutoFollow(player);
				final Player leaderName = World.getInstance().getPlayer(leaders[0]);
				
				if (leaderName == null)
				{
					if (!player.getVariables().getBoolean("자동비활성메세지", false))
					{
						player.setInvul(true);
						player.setBlockActions(true);
						player.setTarget(null);
						player.sendMessage("타겟의 접속이 종료되었습니다. 따라가기를 종료하기 전까지 캐릭터의 움직임이 제한됩니다.");
						player.sendMessage("타겟의 접속을 기다리고 있습니다. 원치 않으면 따라가기를 종료해주세요.");
						player.sendMessage("따라가기 모드 종료 명령어: .자동따라가기종료");
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "타겟의 접속이 종료되었습니다. 따라가기를 종료하기 전까지 캐릭터의 움직임이 제한됩니다."));
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "타겟의 접속을 기다리고 있습니다. 원치 않으면 따라가기를 종료해주세요."));
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "따라가기 모드 종료 명령어: .자동따라가기종료"));
						player.getVariables().set("자동비활성메세지", true);
						player.getVariables().set("자동타겟접속메세지", false);
					}
				}
				else
				{
					if (!player.getVariables().getBoolean("자동타겟접속메세지", false))
					{
						player.setInvul(false);
						player.setBlockActions(false);
						player.sendMessage("타겟이 접속을 하여 자동 따라가기가 시작됩니다.");
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "타겟이 접속을 하여 자동 따라가기가 시작됩니다."));
						player.getVariables().set("자동타겟접속메세지", true);
						player.getVariables().set("자동비활성메세지", false);
					}
					else
					{
						player.setInvul(false);
						player.setBlockActions(false);
						player.sendMessage("자동 따라가기가 시작됩니다.");
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동 따라가기가 시작됩니다."));
					}
					PLAYERS.remove(player);
					AutoFollowTaskManager.getInstance().doAutoFollow(leaderName, player);
				}
				continue SEARCH;
			}
		}
		_working = false;
	}
	
	public void doSerch(Player player)
	{
		if (!PLAYERS.contains(player))
		{
			player.onActionRequest();
			PLAYERS.add(player);
		}
	}
	
	public void stopSerch(Player player)
	{
		PLAYERS.remove(player);
	}
	
	public static AutoFollowSearchTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoFollowSearchTaskManager INSTANCE = new AutoFollowSearchTaskManager();
	}
}
