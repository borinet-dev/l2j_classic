package handlers.admincommandhandlers;

import java.util.List;
import java.util.StringTokenizer;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.HtmlActionScope;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.instancemanager.BoatManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Vehicle;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.GetOffVehicle;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.util.BoatUtil;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * 보리넷 가츠
 */
public class AdminBoat implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_boat"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		
		if (actualCommand.equalsIgnoreCase("admin_boat"))
		{
			if (!st.hasMoreTokens())
			{
				return true;
			}
			
			ThreadPool.execute(() ->
			{
				final String type = st.nextToken();
				switch (type.toLowerCase())
				{
					case "stop":
					{
						stopBoatSystem(activeChar);
						break;
					}
					case "spawn":
					{
						BoatManager.boatNpcSpawn();
						break;
					}
					case "unspawn":
					{
						BoatManager.boatNpcUnSpawn();
						break;
					}
					case "restart":
					{
						BoatUtil._stopRequested = false;
						break;
					}
					case "ms":
					{
						final Party party = activeChar.getParty();
						if (party != null)
						{
							final List<Player> members = party.getMembers();
							for (Player partyMember : members)
							{
								Broadcast.toPlayerScreenMessageS(partyMember, "입장가능 인원은 최소 3명 ~ 최대 5명 입니다.");
							}
						}
						break;
					}
				}
			});
		}
		return true;
	}
	
	public void showPopup(Player player)
	{
		String popupMessage = "가 정박을 완료할 때까지 하선을 잠시 기다려 주세요";
		ConfirmDlg confirmDlg = new ConfirmDlg(popupMessage);
		confirmDlg.addTime(5000); // 5초 동안 팝업을 표시
		
		player.sendPacket(confirmDlg);
	}
	
	public void showTimedPopup(Player player)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append("가 정박을 완료할 때까지 하선을 잠시 기다려 주세요");
		sb.append("</body></html>");
		
		String htmlContent = sb.toString();
		player.sendPacket(new TutorialShowHtml(htmlContent));
		
		// 일정 시간 후 팝업 닫기
		ThreadPool.schedule(() ->
		{
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
		}, 5000); // 5초 후 닫기
	}
	
	private void stopBoatSystem(Player activeChar)
	{
		BoatManager.boatNpcUnSpawn();
		BoatUtil.isComplatedBaikal = false;
		BoatUtil.isComplatedBorinet = false;
		
		if (BoatManager.getInstance()._boats.size() >= 1)
		{
			activeChar.sendMessage("정기선 시스템: " + BoatManager.getInstance()._boats.size() + "개의 정기선의 운항을 중지하였습니다.");
		}
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isInBoat() && player.isOnBoat())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.broadcastPacket(new GetOffVehicle(player.getObjectId(), player.getBoat().getObjectId(), player.getBoat().getX(), player.getBoat().getY(), player.getBoat().getZ()));
				player.setInsideZone(ZoneId.PEACE, false);
				player.setVehicle(null);
				player.setInVehiclePosition(null);
				player.setOnBoat(false);
				player.teleToLocation(TeleportWhereType.TOWN, null);
			}
			if (BoatManager.getInstance()._boats.size() >= 1)
			{
				player.sendPacket(new ExShowScreenMessage("모든 정기선의 운항이 중지되었습니다.", 4000));
			}
			Vehicle vehicle = player.getVehicle();
			if (vehicle != null)
			{
				vehicle.removePassenger(player);
			}
		}
		
		// 모든 보트를 제거하기 전에 정지 요청을 설정
		BoatUtil._stopRequested = true;
		
		// 모든 보트의 상태를 초기화
		BoatUtil.getInstance().setBoatStatus("바이칼 호", "정박 중");
		BoatUtil.getInstance().setBoatStatus("보리넷 호", "정박 중");
		BoatUtil.getInstance().setBoatDestination("바이칼 호", "기란 항구");
		BoatUtil.getInstance().setBoatDestination("보리넷 호", "말하는 섬 항구");
		
		// 보트 제거
		List<Integer> boatIds = BoatManager.getInstance().getBoatIds();
		BoatManager.getInstance().removeBoats(boatIds);
		// 모든 보트의 운항 상태를 초기화
		activeChar.sendMessage("정기선 시스템: 모든 정기선의 운항을 중지하였습니다.");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}