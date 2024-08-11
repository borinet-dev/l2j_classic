package org.l2jmobius.gameserver.util;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class EnchantRecord
{
	// 최근 10개의 강화 기록 조회
	public static List<EnchantEntry> getRecentEnchantRecords(int charId)
	{
		List<EnchantEntry> records = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM enchant_records WHERE char_id = ? ORDER BY enchant_date DESC LIMIT 10"))
		{
			statement.setInt(1, charId);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				int itemObjId = resultSet.getInt("itemObjId");
				int itemId = resultSet.getInt("itemId");
				int char_Id = resultSet.getInt("char_Id");
				String itemName = resultSet.getString("item_name");
				int enchantLevel = resultSet.getInt("enchant_level");
				Date enchantDate = resultSet.getDate("enchant_date");
				boolean blessed = resultSet.getBoolean("blessed");
				EnchantEntry entry = new EnchantEntry(itemObjId, itemId, char_Id, itemName, enchantLevel, enchantDate, blessed);
				records.add(entry);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return records;
	}
	
	public static void addEnchantRecord(int itemObjId, int itemId, int charId, String itemName, int enchantLevel, boolean success)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO enchant_records (itemObjId, itemId, char_Id, item_name, enchant_level, blessed) VALUES (?, ?, ?, ?, ?, ?)"))
		{
			ps.setInt(1, itemObjId);
			ps.setInt(2, itemId);
			ps.setInt(3, charId);
			ps.setString(4, itemName);
			ps.setInt(5, enchantLevel);
			ps.setBoolean(6, success);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void showEnchantRecords(Player activeChar)
	{
		List<EnchantEntry> records = EnchantRecord.getRecentEnchantRecords(activeChar.getObjectId());
		
		if (records.isEmpty())
		{
			activeChar.sendMessage("아이템 강화 실패 기록이 없습니다.");
			return;
		}
		
		String htmlContent = generateEnchantRecordsHTML(records);
		
		final NpcHtmlMessage htmlMsg = new NpcHtmlMessage();
		htmlMsg.setHtml(htmlContent.toString());
		activeChar.sendPacket(htmlMsg);
	}
	
	private String generateEnchantRecordsHTML(List<EnchantEntry> records)
	{
		StringBuilder html = new StringBuilder();
		html.append("<html><head><title>아이템 강화 실패 기록</title></head><body>");
		html.append("<br><center><font name=\"hs16\" color=\"LEVEL\">복구(복원) 가능한 항목</font><br>");
		html.append("강화 실패한 아이템 최근 10개만 표시됩니다.<br1>");
		html.append("<font color=\"FF0000\">제련/룬</font> 등의 부가 옵션은 복구되지 않습니다.</center>");
		
		for (EnchantEntry entry : records)
		{
			final ItemTemplate item = ItemTable.getInstance().getTemplate(entry.getItemId());
			html.append("<table border=0>");
			html.append("<tr>");
			html.append("<td height=40><button width=32 height=32 itemtooltip=\"").append(entry.getItemId()).append("\" back=\"").append(item.getIcon()).append("\" fore=\"").append(item.getIcon()).append("\"></button></td>");
			html.append("<td width=180>").append(entry.getItemName()).append(" <font color=\"00FF00\">+").append(entry.getEnchantLevel()).append("</font></td>");
			String itemName = "+" + entry.getEnchantLevel() + " " + entry.getItemName();
			if (!entry.getBlessed())
			{
				html.append("<td width=30><br><button tooltip=\"[" + itemName + "] 아이템을 복구한다\" action=\"bypass -h recovery_item ").append(entry.getItemId()).append(" ").append(entry.getEnchantLevel()).append(" ").append(entry.getItemObjId()).append("\" value=\"복구\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></button></td>");
			}
			else
			{
				html.append("<td width=30><br><button tooltip=\"[" + itemName + "] 아이템의 강화수치를 복원한다\n아이템이 인벤토리나 창고에 없다면 불가능한다\" action=\"bypass -h recovery_blessed_item ").append(entry.getItemId()).append(" ").append(entry.getEnchantLevel()).append(" ").append(entry.getItemObjId()).append("\" value=\"복원\" width=50 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></button></td>");
			}
			html.append("</tr>");
			html.append("</table>");
			html.append("<table border=0><tr><td><center><img src=\"L2UI.SquareWhite\" width=270 height=1></center></td></tr></table><br>");
		}
		
		html.append("</body></html>");
		
		return html.toString();
	}
	
	public void deleteItemRecovery(Player player, int itemObjId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM enchant_records WHERE itemObjId=? AND char_id=?"))
		{
			ps.setInt(1, itemObjId);
			ps.setInt(2, player.getObjectId());
			ps.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkRecovery(Player player, int itemObjId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM enchant_records WHERE char_id = ? AND itemObjId=?"))
		{
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, itemObjId);
			ResultSet rset = statement.executeQuery();
			return rset.next();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static EnchantRecord getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantRecord INSTANCE = new EnchantRecord();
	}
}
