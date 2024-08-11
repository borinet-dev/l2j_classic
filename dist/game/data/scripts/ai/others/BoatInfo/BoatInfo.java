package ai.others.BoatInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BoatUtil;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class BoatInfo extends AbstractNpcAI
{
	protected static final Logger LOGGER = Logger.getLogger(BoatInfo.class.getName());
	
	// NPCs
	private static final int[] WharfManager =
	{
		Config.BOAT_WHARF_MANAGER_GIRAN,
		Config.BOAT_WHARF_MANAGER_TALKING
	};
	
	public BoatInfo()
	{
		if (!Config.ALLOW_BOAT)
		{
			return;
		}
		addStartNpc(WharfManager);
		addFirstTalkId(WharfManager);
		addTalkId(WharfManager);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		// HTML 대화창 생성
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>선착장 관리인 " + npc.getName() + ":<br>");
		sb.append(Config.SERVER_NAME_KOR + "에서는 두대의 정기선이 말섬과 기란의 항구를<br1>서로 교차하여 30분마다 운행하고 있다네.<br>");
		sb.append("<table border=0 width=280><tr><td><center><font color=\"LEVEL\">정기선 정보</font></center>");
		sb.append("</td></tr></table><tr><td>");
		
		if (!BoatUtil._stopRequested)
		{
			sb.append("<table border=0 width=280>");
			BoatUtil.getInstance().appendBoatInfo(sb, "보리넷 호");
			BoatUtil.getInstance().appendBoatInfo(sb, "바이칼 호");
			sb.append("</table><br>");
		}
		else
		{
			sb.append("<center>현재 정기선은 운행하지 않습니다.</center>");
		}
		
		// 보트 상태에 따라 다른 메시지를 추가
		String bottomMessage = getBoardingMessage(npc.getId());
		sb.append(!BoatUtil._stopRequested ? bottomMessage : "");
		sb.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc_%objectId%_Buy " + npc.getId() + "00\">정기선 배표를 구매한다.</Button>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	private String getBoardingMessage(int npcId)
	{
		StringBuilder message = new StringBuilder();
		String query = "";
		
		if (npcId == Config.BOAT_WHARF_MANAGER_TALKING) // 말섬 선착장 관리인
		{
			query = "SELECT name, status FROM boat_schedule WHERE (destination='기란 항구' AND status='정박 중') OR (destination='말하는 섬 항구' AND status='운항 중')";
		}
		else if (npcId == Config.BOAT_WHARF_MANAGER_GIRAN) // 기란 선착장 관리인
		{
			query = "SELECT name, status FROM boat_schedule WHERE (destination='말하는 섬 항구' AND status='정박 중') OR (destination='기란 항구' AND status='운항 중')";
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					String boatName = rs.getString("name");
					String status = rs.getString("status");
					
					if (boatName.equals("바이칼 호") && (!BoatUtil.isStartedBoatBaikal))
					{
						message.append("");
						break;
					}
					else if (boatName.equals("보리넷 호") && (!BoatUtil.isStartedBoatBorinet))
					{
						message.append("");
						break;
					}
					else if ("정박 중".equals(status) || ("운항 중".equals(status)))
					{
						String 현재상태 = "도착";
						if (status.equals("정박 중"))
						{
							현재상태 = "출항";
						}
						message.append("&nbsp;&nbsp;곧 " + 현재상태 + " 예정인 <font color=LEVEL>").append(boatName).append("</font>를 탑승해 보겠는가?");
						break;
					}
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			message.append("정기선 정보를 가져오는 중 오류가 발생했습니다.");
		}
		
		return message.toString();
	}
	
	public static BoatInfo getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final BoatInfo INSTANCE = new BoatInfo();
	}
	
	public static void main(String[] args)
	{
		new BoatInfo();
	}
}