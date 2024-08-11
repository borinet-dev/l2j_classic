package org.l2jmobius.gameserver.util;

/**
 * String name1=KorNameUtil.getName(name,"을","를");<br>
 * String name2=KorNameUtil.getName(name,"이","가");<br>
 * String name3=KorNameUtil.getName(name,"은","는");
 */
public class KorNameUtil
{
	public static final String getName(String name, String firstValue, String secondValue)
	{
		char lastName = name.charAt(name.length() - 1);
		
		if ((lastName < 0xAC00) || (lastName > 0xD7A3))
		{
			return name;
		}
		String seletedValue = ((lastName - 0xAC00) % 28) > 0 ? firstValue : secondValue;
		
		return name + seletedValue;
	}
}
