package org.l2jmobius.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.KorNameUtil;

public class ItemNameTable
{
	private static final Logger LOGGER = Logger.getLogger(ItemNameTable.class.getName());
	private static ItemNameTable _instance = null;
	private Map<Integer, String> _itemNames = null;
	
	private ItemNameTable()
	{
	}
	
	public static ItemNameTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemNameTable();
			_instance.load();
		}
		return _instance;
	}
	
	private void load()
	{
		_itemNames = new HashMap<>();
		String query = "SELECT * FROM itemname_classic";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int itemId = rs.getInt("item_id");
				String itemName = rs.getString("item_name").intern();
				_itemNames.put(itemId, itemName);
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "아이템 이름 호출 중 오류가 발생했습니다.", e);
		}
	}
	
	public String getItemNameKor(int itemId)
	{
		if ((_itemNames != null) && !_itemNames.isEmpty())
		{
			return _itemNames.get(itemId);
		}
		return null;
	}
	
	public synchronized boolean doesItemNameExist(String name)
	{
		boolean result = false;
		String query = "SELECT COUNT(*) as count FROM itemname_classic WHERE item_name LIKE ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, "%" + name + "%");
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					int count = rs.getInt("count");
					result = count > 0;
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "아이템 이름 존재 여부를 확인하는 중 오류가 발생했습니다.", e);
		}
		
		return result;
	}
	
	public static void autoItemName(Player player, String string, boolean potion)
	{
		player.sendMessage((potion ? "자동 포션: " : "자동 아이템: ") + KorNameUtil.getName(string, "을", "를") + (potion ? " 섭취하였습니다." : " 사용하였습니다."));
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, (potion ? "자동 포션" : "자동 아이템"), KorNameUtil.getName(string, "을", "를") + (potion ? " 섭취하였습니다." : " 사용하였습니다.")));
	}
}
