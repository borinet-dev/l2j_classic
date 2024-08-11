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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands: - kill = kills target Creature - kill_monster = kills target non-player - kill <radius> = If radius is specified, then ALL players only in that radius will be killed. - kill_monster <radius> = If radius is specified, then ALL non-players only in that
 * radius will be killed.
 * @version $Revision: 1.2.4.5 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kill",
		"admin_kill_monster"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final WorldObject obj = activeChar.getTarget();
		if (obj != null)
		{
			kill(activeChar, (Creature) obj);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
		}
		return true;
	}
	
	private void kill(Player activeChar, Creature target)
	{
		if (target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		if (target.isPlayer())
		{
			if (!target.isGM())
			{
				target.stopAllEffects(); // e.g. invincibility effect
			}
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar, null);
		}
		else if (Config.CHAMPION_ENABLE && target.isChampion())
		{
			target.reduceCurrentHp((target.getMaxHp() * Config.CHAMPION_HP) + 1, activeChar, null);
		}
		else
		{
			boolean targetIsInvul = false;
			if (target.isInvul())
			{
				targetIsInvul = true;
				target.setInvul(false);
			}
			
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null);
			if (targetIsInvul)
			{
				target.setInvul(true);
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
