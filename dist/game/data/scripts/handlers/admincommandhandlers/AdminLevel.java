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
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.BuilderUtil;

public class AdminLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_level",
		"admin_set_level"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final WorldObject targetChar = activeChar.getTarget();
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("admin_add_level"))
		{
			try
			{
				if ((targetChar != null) && targetChar.isPlayable())
				{
					((Playable) targetChar).getStat().addLevel(Byte.parseByte(val));
				}
			}
			catch (NumberFormatException e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong Number Format");
			}
		}
		else if (actualCommand.equalsIgnoreCase("admin_set_level"))
		{
			if ((targetChar == null) || !targetChar.isPlayer())
			{
				activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET); // incorrect target!
				return false;
			}
			
			final Player targetPlayer = targetChar.getActingPlayer();
			int maxLevel = ExperienceData.getInstance().getMaxLevel();
			if (targetPlayer.isSubClassActive() && !targetPlayer.isDualClassActive() && (Config.MAX_SUBCLASS_LEVEL < maxLevel))
			{
				maxLevel = Config.MAX_SUBCLASS_LEVEL;
			}
			
			try
			{
				final byte level = Byte.parseByte(val);
				if ((level >= 1) && (level <= maxLevel))
				{
					final long pXp = targetPlayer.getExp();
					final long tXp = ExperienceData.getInstance().getExpForLevel(level);
					if (pXp > tXp)
					{
						targetPlayer.getStat().setLevel(level);
						targetPlayer.removeExpAndSp(pXp - tXp, 0);
					}
					else if (pXp < tXp)
					{
						targetPlayer.addExpAndSp(tXp - pXp, 0);
					}
					targetPlayer.setCurrentHpMp(targetPlayer.getMaxHp(), targetPlayer.getMaxMp());
					targetPlayer.setCurrentCp(targetPlayer.getMaxCp());
					targetPlayer.broadcastUserInfo();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "1에서 " + maxLevel + " 사이의 레벨을 지정해야 합니다.");
					return false;
				}
			}
			catch (NumberFormatException e)
			{
				BuilderUtil.sendSysMessage(activeChar, "1에서 " + maxLevel + " 사이의 레벨을 지정해야 합니다.");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
