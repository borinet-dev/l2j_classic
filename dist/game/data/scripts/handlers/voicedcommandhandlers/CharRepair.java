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
package handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author 보리넷
 */
public class CharRepair implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"교정",
		"교정하기"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (target != null)
		{
			final StringTokenizer st = new StringTokenizer(target);
			try
			{
				String charName = null;
				if (st.hasMoreTokens())
				{
					charName = st.nextToken();
				}
				if (activeChar.getName().equalsIgnoreCase(charName))
				{
					activeChar.sendMessage("자신은 선택할 수 없습니다.");
					return false;
				}
				if (activeChar.getInventory().getInventoryItemCount(57, -1) < 20000)
				{
					activeChar.sendMessage("캐릭터 교정에 필요한 아데나가 부족합니다.");
					return false;
				}
				int objId = 0;
				for (Map.Entry<Integer, String> e : activeChar.getAccountChars().entrySet())
				{
					if (e.getValue().equalsIgnoreCase(charName))
					{
						objId = e.getKey();
						break;
					}
				}
				if (objId == 0)
				{
					activeChar.sendMessage("캐릭터를 찾을 수 없습니다.");
					return false;
				}
				else if (World.getInstance().getPlayer(objId) != null)
				{
					activeChar.sendMessage("캐릭터가 온라인 상태입니다.");
					return false;
				}
				try (Connection con = DatabaseFactory.getConnection();
					PreparedStatement statement = con.prepareStatement("SELECT reputation FROM characters WHERE charId=?"))
				{
					statement.setInt(1, objId);
					statement.execute();
					ResultSet rs = statement.getResultSet();
					
					int karma = 0;
					
					rs.next();
					karma = rs.getInt("reputation");
					
					if (karma > 100)
					{
						try (PreparedStatement statement1 = con.prepareStatement("UPDATE characters SET x=17144, y=170156, z=-3502 WHERE charId=?"))
						{
							statement1.setInt(1, objId);
							statement1.execute();
						}
					}
					else
					{
						try (PreparedStatement statement2 = con.prepareStatement("UPDATE characters SET x='83642', y='148633', z='-3400' WHERE charId=?"))
						{
							statement2.setInt(1, objId);
							statement2.execute();
						}
					}
					
					activeChar.destroyItemByItemId("캐릭터교정", 57, 20000, activeChar, true);
					activeChar.sendMessage("캐릭터가 성공적으로 교정되었습니다.");
					return true;
				}
				catch (SQLException e)
				{
					return false;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("캐릭터 교정을 하지 못했습니다.");
			}
		}
		else
		{
			String html = HtmCache.getInstance().getHtm(null, "data/html/mods/CharRepair.htm");
			if (html == null)
			{
				html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
			}
			html = html.replaceFirst("%charsOnAccount%", getCharsOnAccount(activeChar.getName(), activeChar.getAccountName()));
			activeChar.sendPacket(new NpcHtmlMessage(html));
			return true;
		}
		return true;
	}
	
	public static String getCharsOnAccount(String myCharName, String accountName)
	{
		List<String> chars = new ArrayList<>();
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?"))
		{
			statement.setString(1, accountName);
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					chars.add(rset.getString("char_name"));
				}
			}
		}
		catch (SQLException e)
		{
		}
		
		StringBuilder resultBuilder = new StringBuilder();
		for (String charName : chars)
		{
			if (!charName.equalsIgnoreCase(myCharName))
			{
				resultBuilder.append(charName).append(';');
			}
		}
		
		if (resultBuilder.length() == 0)
		{
			return "";
		}
		
		return resultBuilder.substring(0, resultBuilder.length() - 1);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
