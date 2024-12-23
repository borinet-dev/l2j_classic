/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.instancemanager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.PetitionState;
import org.l2jmobius.gameserver.model.Petition;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * Petition Manager
 * @author Tempy
 */
public class PetitionManager
{
	protected static final Logger LOGGER = Logger.getLogger(PetitionManager.class.getName());
	
	private final Map<Integer, Petition> _pendingPetitions;
	private final Map<Integer, Petition> _completedPetitions;
	
	protected PetitionManager()
	{
		LOGGER.info("진정 매니저를 로드하였습니다.");
		_pendingPetitions = new HashMap<>();
		_completedPetitions = new HashMap<>();
	}
	
	public void clearPendingPetitions()
	{
		final int numPetitions = _pendingPetitions.size();
		_pendingPetitions.clear();
		LOGGER.info(getClass().getSimpleName() + ": 대기 중인 진정 대기열이 지워졌습니다. " + numPetitions + "개의 청원이 삭제되었습니다.");
	}
	
	public boolean acceptPetition(Player respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
		{
			return false;
		}
		
		final Petition currPetition = _pendingPetitions.get(petitionId);
		if (currPetition.getResponder() != null)
		{
			return false;
		}
		
		currPetition.setResponder(respondingAdmin);
		currPetition.setState(PetitionState.IN_PROCESS);
		
		// Petition application accepted. (Send to Petitioner)
		// currPetition.sendPetitionerPacket(new SystemMessage(SystemMessageId.PETITION_APPLICATION_ACCEPTED));
		
		// Petition consultation with <Player> underway.
		SystemMessage gm = new SystemMessage(SystemMessageId.STARTING_PETITION_CONSULTATION_WITH_C1);
		gm.addString(currPetition.getPetitioner().getName());
		currPetition.sendResponderPacket(gm);
		
		SystemMessage user = new SystemMessage(SystemMessageId.STARTING_PETITION_CONSULTATION_WITH_C1);
		user.addString("운영자 " + currPetition.getResponder().getName());
		currPetition.sendPetitionerPacket(user);
		
		// Set responder name on petitioner instance
		currPetition.getPetitioner().setLastPetitionGmName(currPetition.getResponder().getName());
		return true;
	}
	
	public boolean cancelActivePetition(Player player)
	{
		for (Petition currPetition : _pendingPetitions.values())
		{
			if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId()))
			{
				return (currPetition.endPetitionConsultation(PetitionState.PETITIONER_CANCEL));
			}
			
			if ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId()))
			{
				return (currPetition.endPetitionConsultation(PetitionState.RESPONDER_CANCEL));
			}
		}
		
		return false;
	}
	
	public void checkPetitionMessages(Player petitioner)
	{
		if (petitioner != null)
		{
			for (Petition currPetition : _pendingPetitions.values())
			{
				if (currPetition == null)
				{
					continue;
				}
				
				if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()))
				{
					for (CreatureSay logMessage : currPetition.getLogMessages())
					{
						petitioner.sendPacket(logMessage);
					}
					
					return;
				}
			}
		}
	}
	
	public boolean endActivePetition(Player player)
	{
		if (!player.isGM())
		{
			return false;
		}
		
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition == null)
			{
				continue;
			}
			
			if ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId()))
			{
				return (currPetition.endPetitionConsultation(PetitionState.COMPLETED));
			}
		}
		
		return false;
	}
	
	public Map<Integer, Petition> getCompletedPetitions()
	{
		return _completedPetitions;
	}
	
	public Map<Integer, Petition> getPendingPetitions()
	{
		return _pendingPetitions;
	}
	
	public int getPendingPetitionCount()
	{
		return _pendingPetitions.size();
	}
	
	public int getPlayerTotalPetitionCount(Player player)
	{
		if (player == null)
		{
			return 0;
		}
		
		int petitionCount = 0;
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition == null)
			{
				continue;
			}
			
			if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId()))
			{
				petitionCount++;
			}
		}
		
		for (Petition currPetition : _completedPetitions.values())
		{
			if (currPetition == null)
			{
				continue;
			}
			
			if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId()))
			{
				petitionCount++;
			}
		}
		
		return petitionCount;
	}
	
	public boolean isPetitionInProcess()
	{
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition == null)
			{
				continue;
			}
			
			if (currPetition.getState() == PetitionState.IN_PROCESS)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isPetitionInProcess(int petitionId)
	{
		if (!isValidPetition(petitionId))
		{
			return false;
		}
		
		final Petition currPetition = _pendingPetitions.get(petitionId);
		return (currPetition.getState() == PetitionState.IN_PROCESS);
	}
	
	public boolean isPlayerInConsultation(Player player)
	{
		if (player != null)
		{
			for (Petition currPetition : _pendingPetitions.values())
			{
				if (currPetition == null)
				{
					continue;
				}
				
				if (currPetition.getState() != PetitionState.IN_PROCESS)
				{
					continue;
				}
				
				if (((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId())) || ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId())))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isPetitioningAllowed()
	{
		return Config.PETITIONING_ALLOWED;
	}
	
	public boolean isPlayerPetitionPending(Player petitioner)
	{
		if (petitioner != null)
		{
			for (Petition currPetition : _pendingPetitions.values())
			{
				if (currPetition == null)
				{
					continue;
				}
				
				if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean isValidPetition(int petitionId)
	{
		return _pendingPetitions.containsKey(petitionId);
	}
	
	public boolean rejectPetition(Player respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
		{
			return false;
		}
		
		final Petition currPetition = _pendingPetitions.get(petitionId);
		if (currPetition.getResponder() != null)
		{
			return false;
		}
		
		currPetition.setResponder(respondingAdmin);
		return (currPetition.endPetitionConsultation(PetitionState.RESPONDER_REJECT));
	}
	
	public boolean sendActivePetitionMessage(Player player, String messageText)
	{
		// if (!isPlayerInConsultation(player))
		// return false;
		CreatureSay cs;
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition == null)
			{
				continue;
			}
			
			if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId()))
			{
				cs = new CreatureSay(player, ChatType.PETITION_PLAYER, player.getName(), messageText);
				currPetition.addLogMessage(cs);
				
				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
			
			if ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId()))
			{
				cs = new CreatureSay(player, ChatType.PETITION_GM, player.getName(), messageText);
				currPetition.addLogMessage(cs);
				
				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
		}
		
		return false;
	}
	
	public void sendPendingPetitionList(Player player)
	{
		final StringBuilder htmlContent = new StringBuilder(600 + (_pendingPetitions.size() * 300));
		htmlContent.append("<html><body><center><table width=292><tr><td width=45><button value=\"메인\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center>진정 메뉴</center></td><td width=45><button value=\"뒤로\" action=\"bypass -h admin_admin7\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><table width=\"292\"><tr><td><table width=\"292\"><tr><td><button value=\"새로고침\" action=\"bypass -h admin_view_petitions\" width=\"80\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br></td></tr>");
		
		if (_pendingPetitions.isEmpty())
		{
			htmlContent.append("<tr><td>현재 접수된 진정이 없습니다.</td></tr>");
		}
		else
		{
			htmlContent.append("<tr><td><font color=\"LEVEL\">현재 진정:</font><br></td></tr>");
		}
		
		boolean color = true;
		int petcount = 0;
		for (Petition currPetition : _pendingPetitions.values())
		{
			if (currPetition == null)
			{
				continue;
			}
			
			htmlContent.append("<tr><td width=\"290\"><table width=\"292\" cellpadding=\"2\" bgcolor=" + (color ? "131210" : "444444") + "><tr><td width=\"130\">" + BorinetUtil.dataDateFormatKor.format(new Date(currPetition.getSubmitTime())));
			htmlContent.append("</td><td width=\"140\" align=right><font color=\"" + (currPetition.getPetitioner().isOnline() ? "00FF00" : "999999") + "\">" + currPetition.getPetitioner().getName() + "</font></td></tr>");
			htmlContent.append("<tr><td width=\"130\">");
			if (currPetition.getState() != PetitionState.IN_PROCESS)
			{
				htmlContent.append("<table width=\"130\" cellpadding=\"2\"><tr><td><button value=\"보기\" action=\"bypass -h admin_view_petition " + currPetition.getId() + "\" width=\"50\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
			}
			else
			{
				htmlContent.append("<font color=\"" + (currPetition.getResponder().isOnline() ? "00FF00" : "999999") + "\">" + currPetition.getResponder().getName() + "</font>");
			}
			htmlContent.append("</td>" + currPetition.getTypeAsString() + "<td width=\"140\" align=right>" + currPetition.getTypeAsString() + "</td></tr></table></td></tr>");
			color = !color;
			petcount++;
			if (petcount > 10)
			{
				htmlContent.append("<tr><td><font color=\"LEVEL\">대기 중인 진정이 더 있습니다...</font><br></td></tr>");
				break;
			}
		}
		
		htmlContent.append("</table></center></body></html>");
		
		final NpcHtmlMessage htmlMsg = new NpcHtmlMessage();
		htmlMsg.setHtml(htmlContent.toString());
		player.sendPacket(htmlMsg);
	}
	
	public int submitPetition(Player petitioner, String petitionText, int petitionType)
	{
		// Create a new petition instance and add it to the list of pending petitions.
		final Petition newPetition = new Petition(petitioner, petitionText, petitionType);
		final int newPetitionId = newPetition.getId();
		_pendingPetitions.put(newPetitionId, newPetition);
		
		return newPetitionId;
	}
	
	public void viewPetition(Player player, int petitionId)
	{
		if (!player.isGM())
		{
			return;
		}
		
		if (!isValidPetition(petitionId))
		{
			return;
		}
		
		final Petition currPetition = _pendingPetitions.get(petitionId);
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player, "data/html/admin/petition.htm");
		html.replace("%petition%", String.valueOf(currPetition.getId()));
		html.replace("%time%", BorinetUtil.dataDateFormatKor.format(new Date(currPetition.getSubmitTime())));
		html.replace("%type%", currPetition.getTypeAsString());
		html.replace("%petitioner%", currPetition.getPetitioner().getName());
		html.replace("%online%", (currPetition.getPetitioner().isOnline() ? "00FF00" : "999999"));
		html.replace("%text%", currPetition.getContent());
		player.sendPacket(html);
	}
	
	/**
	 * Gets the single instance of {@code PetitionManager}.
	 * @return single instance of {@code PetitionManager}
	 */
	public static PetitionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PetitionManager INSTANCE = new PetitionManager();
	}
}
