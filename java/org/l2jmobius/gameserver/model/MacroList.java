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
package org.l2jmobius.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.enums.MacroType;
import org.l2jmobius.gameserver.enums.MacroUpdateType;
import org.l2jmobius.gameserver.enums.ShortcutType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.interfaces.IRestorable;
import org.l2jmobius.gameserver.network.serverpackets.SendMacroList;

public class MacroList implements IRestorable
{
	private static final Logger LOGGER = Logger.getLogger(MacroList.class.getName());
	
	private final Player _owner;
	private int _macroId;
	private final Map<Integer, Macro> _macroses = Collections.synchronizedMap(new LinkedHashMap<>());
	
	public MacroList(Player owner)
	{
		_owner = owner;
		_macroId = 1000;
	}
	
	public Map<Integer, Macro> getAllMacroses()
	{
		return _macroses;
	}
	
	public void registerMacro(Macro macro)
	{
		// /지연 9999999 명령어가 이미 있는지 확인
		boolean hasDelayCommand = macro.getCommands().stream().anyMatch(cmd -> (cmd.getType() == MacroType.DELAY) && (cmd.getD1() == 9999999) && (cmd.getD2() == 0));
		
		// 명령어가 12개인 경우 마지막 명령어를 /지연 9999999로 변경
		if (macro.getCommands().size() == 12)
		{
			macro.getCommands().set(11, new MacroCmd(11, MacroType.DELAY, 9999999, 0, ""));
		}
		// 명령어가 없는 경우에만 추가
		if (!hasDelayCommand)
		{
			macro.getCommands().add(new MacroCmd(macro.getCommands().size(), MacroType.DELAY, 9999999, 0, ""));
		}
		
		MacroUpdateType updateType = MacroUpdateType.ADD;
		if (macro.getId() == 0)
		{
			macro.setId(_macroId++);
			while (_macroses.containsKey(macro.getId()))
			{
				macro.setId(_macroId++);
			}
			_macroses.put(macro.getId(), macro);
			registerMacroInDb(macro);
		}
		else
		{
			updateType = MacroUpdateType.MODIFY;
			final Macro old = _macroses.put(macro.getId(), macro);
			if (old != null)
			{
				deleteMacroFromDb(old);
			}
			registerMacroInDb(macro);
		}
		_owner.sendPacket(new SendMacroList(1, macro, updateType));
	}
	
	public void deleteMacro(int id)
	{
		final Macro removed = _macroses.remove(id);
		if (removed != null)
		{
			deleteMacroFromDb(removed);
		}
		
		for (Shortcut sc : _owner.getAllShortCuts())
		{
			if ((sc.getId() == id) && (sc.getType() == ShortcutType.MACRO))
			{
				_owner.deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		_owner.sendPacket(new SendMacroList(0, removed, MacroUpdateType.DELETE));
	}
	
	public void sendAllMacros()
	{
		final Collection<Macro> allMacros = _macroses.values();
		final int count = allMacros.size();
		
		synchronized (_macroses)
		{
			if (allMacros.isEmpty())
			{
				_owner.sendPacket(new SendMacroList(0, null, MacroUpdateType.LIST));
			}
			else
			{
				for (Macro m : allMacros)
				{
					_owner.sendPacket(new SendMacroList(count, m, MacroUpdateType.LIST));
				}
			}
		}
	}
	
	private void registerMacroInDb(Macro macro)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_macroses (charId,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.getId());
			ps.setInt(3, macro.getIcon());
			ps.setString(4, macro.getName());
			ps.setString(5, macro.getDescr());
			ps.setString(6, macro.getAcronym());
			final StringBuilder sb = new StringBuilder(300);
			
			for (MacroCmd cmd : macro.getCommands())
			{
				sb.append(cmd.getType().ordinal()).append(',').append(cmd.getD1()).append(',').append(cmd.getD2());
				
				if ((cmd.getCmd() != null) && !cmd.getCmd().isEmpty())
				{
					sb.append(',').append(cmd.getCmd());
				}
				sb.append(';');
			}
			
			// 매크로 커맨드 목록 끝에 "6,9999999,0" 추가
			// sb.append("6,9999999,0;");
			
			if (sb.length() > 255)
			{
				sb.setLength(255);
			}
			
			ps.setString(7, sb.toString());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "could not store macro:", e);
		}
	}
	
	private void deleteMacroFromDb(Macro macro)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE charId=? AND id=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.getId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "could not delete macro:", e);
		}
	}
	
	@Override
	public boolean restoreMe()
	{
		_macroses.clear();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId, id, icon, name, descr, acronym, commands FROM character_macroses WHERE charId=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final int id = rset.getInt("id");
					final int icon = rset.getInt("icon");
					final String name = rset.getString("name");
					final String descr = rset.getString("descr");
					final String acronym = rset.getString("acronym");
					final List<MacroCmd> commands = new ArrayList<>();
					final StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
					boolean hasDelayCommand = false;
					while (st1.hasMoreTokens())
					{
						final StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
						if (st.countTokens() < 3)
						{
							continue;
						}
						final MacroType type = MacroType.values()[Integer.parseInt(st.nextToken())];
						final int d1 = Integer.parseInt(st.nextToken());
						final int d2 = Integer.parseInt(st.nextToken());
						String cmd = "";
						if (st.hasMoreTokens())
						{
							cmd = st.nextToken();
						}
						commands.add(new MacroCmd(commands.size(), type, d1, d2, cmd));
						if ((type == MacroType.DELAY) && (d1 == 9999999) && (d2 == 0))
						{
							hasDelayCommand = true;
						}
					}
					// 명령어 끝에 6,9999999,0; 추가 (없는 경우에만)
					if (!hasDelayCommand)
					{
						commands.add(new MacroCmd(commands.size(), MacroType.DELAY, 9999999, 0, ""));
					}
					
					_macroses.put(id, new Macro(id, icon, name, descr, acronym, commands));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "could not store shortcuts:", e);
			return false;
		}
		return true;
	}
}
