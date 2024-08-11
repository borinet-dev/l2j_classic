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

public class NpcNameTable
{
	private static final Logger _log = Logger.getLogger(NpcNameTable.class.getName());
	private static NpcNameTable _instance = null;
	private Map<Integer, String> _npcName = null;
	
	private NpcNameTable()
	{
	}
	
	public static NpcNameTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NpcNameTable();
			_instance.load();
		}
		return _instance;
	}
	
	private void load()
	{
		_npcName = new HashMap<>();
		String query = "SELECT * FROM npcname_classic";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int npcId = rs.getInt("npc_id");
				String npcName = rs.getString("npc_name").intern();
				_npcName.put(npcId, npcName);
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "NPC 이름 호출 중 오류가 발생했습니다.", e);
		}
	}
	
	public String getNpcNameKor(int npcId)
	{
		if ((_npcName != null) && !_npcName.isEmpty())
		{
			return _npcName.get(npcId);
		}
		return null;
	}
}
