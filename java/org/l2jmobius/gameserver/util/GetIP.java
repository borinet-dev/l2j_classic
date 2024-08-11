package org.l2jmobius.gameserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GetIP
{
	private static String IpAddress;
	
	private static void setIpAdd()
	{
		try
		{
			final URL autoIp = new URL("http://ip1.dynupdate.no-ip.com:8245/");
			try (BufferedReader in = new BufferedReader(new InputStreamReader(autoIp.openStream())))
			{
				IpAddress = in.readLine();
			}
		}
		catch (IOException e)
		{
		}
	}
	
	public static String getIpAddress()
	{
		setIpAdd();
		return IpAddress;
	}
}
