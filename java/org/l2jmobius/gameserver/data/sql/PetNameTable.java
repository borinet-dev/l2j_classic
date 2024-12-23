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
package org.l2jmobius.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.xml.PetDataTable;
import org.l2jmobius.gameserver.util.Util;

public class PetNameTable
{
	private static final Logger LOGGER = Logger.getLogger(PetNameTable.class.getName());
	
	public static PetNameTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public boolean doesPetNameExist(String name, int petNpcId)
	{
		boolean result = true;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)"))
		{
			ps.setString(1, name);
			final StringBuilder cond = new StringBuilder();
			if (!cond.toString().isEmpty())
			{
				cond.append(", ");
			}
			
			cond.append(PetDataTable.getInstance().getPetItemsByNpc(petNpcId));
			ps.setString(2, cond.toString());
			try (ResultSet rs = ps.executeQuery())
			{
				result = rs.next();
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "기존 애완동물 이름을 확인할 수 없습니다.", e);
		}
		return result;
	}
	
	public boolean isValidPetName(String name)
	{
		boolean result = true;
		if (!Util.isMatchingRegexp(name, Config.NAME_TEMPLATE) || !Util.isAlphaNumeric(name) || !Util.isValidName(name))
		{
			return result;
		}
		return result;
	}
	
	private static class SingletonHolder
	{
		protected static final PetNameTable INSTANCE = new PetNameTable();
	}
}
