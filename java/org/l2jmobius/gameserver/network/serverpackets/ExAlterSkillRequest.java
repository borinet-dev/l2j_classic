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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author UnAfraid
 */
public class ExAlterSkillRequest implements IClientOutgoingPacket
{
	private final int _currentSkillId;
	private final int _nextSkillId;
	private final int _alterTime;
	private final Player _player;
	
	public ExAlterSkillRequest(Player player, int currentSkill, int nextSkill, int alterTime)
	{
		_player = player;
		_currentSkillId = currentSkill;
		_nextSkillId = nextSkill;
		_alterTime = alterTime;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (!Config.ENABLE_ALTER_SKILLS)
		{
			return true;
		}
		OutgoingPackets.EX_ALTER_SKILL_REQUEST.writeId(packet);
		packet.writeD(_nextSkillId);
		packet.writeD(_currentSkillId);
		packet.writeD(_alterTime);
		if (_alterTime > 0)
		{
			_player.setAlterSkillActive(true);
			ThreadPool.schedule(() ->
			{
				_player.sendPacket(new ExAlterSkillRequest(null, -1, -1, -1));
				_player.setAlterSkillActive(false);
			}, _alterTime * 1000);
		}
		return true;
	}
}
