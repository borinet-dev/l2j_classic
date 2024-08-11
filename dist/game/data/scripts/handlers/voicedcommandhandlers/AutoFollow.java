package handlers.voicedcommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.taskmanager.auto.AutoFollowSearchTaskManager;
import org.l2jmobius.gameserver.taskmanager.auto.AutoFollowTaskManager;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.KorNameUtil;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class AutoFollow extends AbstractNpcAI implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"자동따라가기시작",
		"자동따라가기종료",
		"자동따라가기허용",
		"자동따라가기"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("자동따라가기"))
		{
			BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
		}
		else if (command.equals("자동따라가기종료"))
		{
			if (activeChar.getVariables().getBoolean("자동따라가기", false))
			{
				AutoFollowTaskManager.getInstance().stopAutoFollow(activeChar);
				BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
			}
			else
			{
				activeChar.sendMessage("현재 자동 따라가기 모드의 실행상태가 아닙니다.");
				BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
			}
		}
		else if (command.equals("자동따라가기허용"))
		{
			if (activeChar.getVariables().getBoolean("자동따라가기허용", false))
			{
				activeChar.getVariables().set("자동따라가기허용", false);
				BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
			}
			else
			{
				activeChar.getVariables().set("자동따라가기허용", true);
				BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
			}
		}
		else if (command.startsWith("자동따라가기시작"))
		{
			final String bypass = target.replace("자동따라가기시작", " ");
			final String[] value = bypass.split(" ");
			final String range = value[0];
			
			int actualRange = 50; // 기본값
			String rangeName = "가까운 거리";
			if ("적당한".startsWith(range))
			{
				actualRange = 150;
				rangeName = "적당한 거리";
			}
			
			if (activeChar.isInStoreMode())
			{
				activeChar.setTarget(null);
				activeChar.sendMessage("현재 따라가기 모드를 사용할 수 없습니다.");
			}
			if (activeChar.isJailed())
			{
				activeChar.setTarget(null);
				activeChar.sendMessage("수감 중일 경우 따라가기 모드를 사용할 수 없습니다.");
			}
			if (activeChar.getTarget() != null)
			{
				String leaderName = activeChar.getTarget().getName();
				Player leader = World.getInstance().getPlayer(leaderName);
				
				if ((leader == null) || !leader.isPlayer() || leader.isInOfflineMode() || (leader == activeChar))
				{
					activeChar.setTarget(null);
					activeChar.sendMessage("타겟이 잘못되었습니다.");
					BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
				}
				else if (!leader.getVariables().getBoolean("자동따라가기허용", false))
				{
					activeChar.setTarget(null);
					activeChar.sendMessage("타겟은 자동따라가기 모드를 허용하지 않았습니다.");
					BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
				}
				else if (leader.getVariables().getBoolean("자동따라가기", false))
				{
					activeChar.setTarget(null);
					activeChar.sendMessage("타겟은 현재 자동따라가기 모드상태이므로 타겟에게 사용할 수 없습니다.");
					BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
				}
				else if (!leader.isInsideRadius3D(activeChar, 1000))
				{
					activeChar.setTarget(null);
					activeChar.sendMessage("타겟이 너무 멀리 있습니다.");
					BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
				}
				else
				{
					activeChar.sendMessage(KorNameUtil.getName(leaderName, "을", "를") + " " + rangeName + "를 두고 따라다닙니다.");
					activeChar.sendMessage("로그아웃 및 따라가기 모드를 종료하기 전까지 적용됩니다.");
					activeChar.sendMessage(KorNameUtil.getName(leaderName, "이", "가") + " 튕기거나 로그아웃 등을 할 경우에는 그자리에 멈춰서서 " + KorNameUtil.getName(leaderName, "을", "를") + " 기다립니다.");
					activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, KorNameUtil.getName(leaderName, "을", "를") + " " + rangeName + "를 두고 따라다닙니다."));
					activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "로그아웃 및 따라가기 모드를 종료하기 전까지 적용됩니다."));
					activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, KorNameUtil.getName(leaderName, "이", "가") + " 튕기거나 로그아웃 등을 할 경우에는 그자리에 멈춰서서 " + KorNameUtil.getName(leaderName, "을", "를") + " 기다립니다."));
					leader.sendMessage(KorNameUtil.getName(activeChar.getName(), "이", "가") + " 따라가기 모드를 설정하였습니다.");
					leader.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, KorNameUtil.getName(activeChar.getName(), "이", "가") + " 따라가기 모드를 설정하였습니다."));
					activeChar.getVariables().set("자동따라가기", true);
					activeChar.getVariables().set("자동따라가기설정", leaderName + ", " + actualRange);
					AutoFollowSearchTaskManager.getInstance().doSerch(activeChar);
					BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
				}
			}
			else
			{
				activeChar.sendMessage("타겟이 없습니다.");
				BorinetHtml.getInstance().showAutoFollowHtml(activeChar);
			}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}