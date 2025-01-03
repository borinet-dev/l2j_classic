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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.Config;

/**
 * @author Mobius
 */
public class DatabaseBackup
{
	public static void performBackup()
	{
		// Delete old files.
		final long cut = LocalDateTime.now().minusDays(Config.BACKUP_DAYS).toEpochSecond(ZoneOffset.UTC);
		final Path path = Paths.get("backup/");
		try
		{
			Files.list(path).filter(n ->
			{
				try
				{
					return Files.getLastModifiedTime(n).to(TimeUnit.SECONDS) < cut;
				}
				catch (Exception ex)
				{
					return false;
				}
			}).forEach(n ->
			{
				try
				{
					Files.delete(n);
				}
				catch (Exception ex)
				{
					// Ignore.
				}
			});
		}
		catch (Exception e)
		{
			// Ignore.
		}
		
		String backupDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm"));
		String backupFile = "backup/l2jserver_" + backupDate + ".sql";
		
		String mariadbldumpPath = Config.MARIADB_DUMP_PATH;
		String username = "root";
		String password = Config.DATABASE_PASSWORD;
		String database = "l2jserver";
		String charset = "--default-character-set=euckr";
		String dumpCommand = String.format( //
			"%s -u%s -p%s %s --result-file=\"%s\" %s", //
			mariadbldumpPath, username, password, charset, backupFile, database //
		);
		
		try
		{
			final Process process = Runtime.getRuntime().exec(dumpCommand);
			process.waitFor();
		}
		catch (Exception e)
		{
			// Ignore.
		}
	}
}
