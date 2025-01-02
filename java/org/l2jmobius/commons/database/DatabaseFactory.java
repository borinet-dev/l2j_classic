package org.l2jmobius.commons.database;

import java.sql.Connection;
import java.util.logging.Logger;

import org.mariadb.jdbc.MariaDbPoolDataSource;

import org.l2jmobius.Config;

/**
 * @author 보리넷 가츠
 * @version December 26th 2024
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
			LOGGER.warning("데이터베이스에 연결할 수 없습니다!");
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
				LOGGER.warning("데이터베이스에 연결할 수 없습니다.");
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
			LOGGER.warning("데이터베이스를 닫는 동안 문제가 발생했습니다.");
		}
	}
}
