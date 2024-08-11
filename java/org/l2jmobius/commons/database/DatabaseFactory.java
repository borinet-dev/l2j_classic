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
package org.l2jmobius.commons.database;

import java.sql.Connection;
import java.util.logging.Logger;

import org.mariadb.jdbc.MariaDbPoolDataSource;

import org.l2jmobius.Config;

/**
 * @author Mobius
 * @version November 10th 2018
 */
public class DatabaseFactory
{
	private static final Logger LOGGER = Logger.getLogger(DatabaseFactory.class.getName());
	
	private static final MariaDbPoolDataSource DATABASE_POOL = new MariaDbPoolDataSource(Config.DATABASE_URL + "&user=" + Config.DATABASE_LOGIN + "&password=" + Config.DATABASE_PASSWORD + "&maxPoolSize=" + Config.DATABASE_MAX_CONNECTIONS);
	
	public static void init()
	{
		// Test if connection is valid.
		try
		{
			DATABASE_POOL.getConnection().close();
			LOGGER.info("데이터베이스에 정상적으로 연결되었습니다.");
		}
		catch (Exception e)
		{
			LOGGER.info("데이터베이스에 접속할 수 없습니다!");
		}
	}
	
	public static Connection getConnection()
	{
		Connection con = null;
		while (con == null)
		{
			try
			{
				con = DATABASE_POOL.getConnection();
			}
			catch (Exception e)
			{
				LOGGER.severe("DatabaseFactory: 데이터베이스에 연결할 수 없습니다.");
			}
		}
		return con;
	}
	
	public static void close()
	{
		try
		{
			DATABASE_POOL.close();
		}
		catch (Exception e)
		{
			LOGGER.severe("DatabaseFactory: 데이터베이스 소스를 닫는 동안 문제가 발생했습니다.");
		}
	}
}
