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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * @author Psychokiller1888
 */
public class AdminVitality implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_set_vitality",
		"admin_full_vitality",
		"admin_empty_vitality",
		"admin_get_vitality"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!Config.ENABLE_VITALITY)
		{
			BuilderUtil.sendSysMessage(activeChar, "활력시스템은 사용하지 않습니다.");
			return false;
		}
		
		int vitality = 0;
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String cmd = st.nextToken();
		if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
		{
			final Player target = (Player) activeChar.getTarget();
			if (cmd.equals("admin_set_vitality"))
			{
				try
				{
					vitality = Integer.parseInt(st.nextToken());
				}
				catch (Exception e)
				{
					BuilderUtil.sendSysMessage(activeChar, "Incorrect vitality");
				}
				
				if (vitality < 1)
				{
					target.setVitalityPoints(vitality, true);
					target.sendMessage("운영자에 의해 은총이 모두 소진 되었습니다.");
				}
				else
				{
					int totalVitality = target.getVitalityPoints() + vitality;
					if ((target.getVitalityPoints() + vitality) >= PlayerStat.MAX_VITALITY_POINTS)
					{
						target.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS, true);
					}
					else
					{
						target.setVitalityPoints(totalVitality, true);
						target.sendMessage("운영자에 의해 은총이 " + vitality + " 포인트가 충전 되었습니다.");
					}
				}
			}
			else if (cmd.equals("admin_full_vitality"))
			{
				target.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS, true);
				target.sendMessage("운영자에 의해 은총이 모두 충전되었습니다.");
			}
			else if (cmd.equals("admin_empty_vitality"))
			{
				target.setVitalityPoints(PlayerStat.MIN_VITALITY_POINTS, true);
				target.sendMessage("운영자에 의해 은총이 모두 소진 되었습니다.");
			}
			else if (cmd.equals("admin_get_vitality"))
			{
				vitality = target.getVitalityPoints();
				BuilderUtil.sendSysMessage(activeChar, "대상의 은총 포인트: " + vitality);
			}
			return true;
		}
		BuilderUtil.sendSysMessage(activeChar, "대상을 찾을 수 없거나 플레이어가 아닙니다");
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
