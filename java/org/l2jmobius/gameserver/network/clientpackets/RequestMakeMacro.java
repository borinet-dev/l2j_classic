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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.MacroType;
import org.l2jmobius.gameserver.model.Macro;
import org.l2jmobius.gameserver.model.MacroCmd;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;

public class RequestMakeMacro implements IClientIncomingPacket
{
	private Macro _macro;
	private int _commandsLength = 0;
	
	private static final int MAX_MACRO_LENGTH = 12;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		final int _id = packet.readD();
		final String _name = packet.readS();
		final String _desc = packet.readS();
		final String _acronym = packet.readS();
		final int icon = packet.readD();
		int count = packet.readC();
		if (count > MAX_MACRO_LENGTH)
		{
			count = MAX_MACRO_LENGTH;
		}
		
		final List<MacroCmd> commands = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
		{
			final int entry = packet.readC();
			final int type = packet.readC(); // 1 = skill, 3 = action, 4 = shortcut
			final int d1 = packet.readD(); // skill or page number for shortcuts
			final int d2 = packet.readC();
			final String command = packet.readS();
			_commandsLength += command.length();
			commands.add(new MacroCmd(entry, MacroType.values()[(type < 1) || (type > 6) ? 0 : type], d1, d2, command));
		}
		_macro = new Macro(_id, icon, _name, _desc, _acronym, commands);
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		if (_commandsLength > 255)
		{
			// Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(SystemMessageId.INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS);
			return;
		}
		if (!Config.ENABLE_MACRO_NEXT_TARGET)
		{
			final StringBuilder sb = new StringBuilder(300);
			for (MacroCmd cmd : _macro.getCommands())
			{
				sb.append(cmd.getType().ordinal() + "," + cmd.getD1() + "," + cmd.getD2());
				if ((cmd.getCmd() != null) && (cmd.getCmd().length() > 0))
				{
					sb.append("," + cmd.getCmd());
				}
				sb.append(';');
				if (cmd.getCmd().contains("다음타겟") || cmd.getCmd().contains("공격"))
				{
					player.sendMessage("생성이 불가능한 매크로입니다.");
					return;
				}
			}
		}
		if (player.getMacros().getAllMacroses().size() > 48)
		{
			// You may create up to 48 macros.
			player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_48_MACROS);
			return;
		}
		if (_macro.getName().isEmpty())
		{
			// Enter the name of the macro.
			player.sendPacket(SystemMessageId.ENTER_THE_NAME_OF_THE_MACRO);
			return;
		}
		if (_macro.getDescr().length() > 32)
		{
			// Macro descriptions may contain up to 32 characters.
			player.sendPacket(SystemMessageId.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
			return;
		}
		player.registerMacro(_macro);
	}
}
