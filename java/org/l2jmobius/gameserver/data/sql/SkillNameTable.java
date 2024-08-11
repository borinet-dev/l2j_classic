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

public class SkillNameTable
{
	private static final Logger _log = Logger.getLogger(SkillNameTable.class.getName());
	private static SkillNameTable _instance = null;
	private Map<Integer, String> _skillnames = null;
	
	private SkillNameTable()
	{
	}
	
	public static SkillNameTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillNameTable();
			_instance.load();
		}
		return _instance;
	}
	
	private void load()
	{
		_skillnames = new HashMap<>();
		String query = "SELECT * FROM skillname_classic";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int skillId = rs.getInt("skill_id");
				String skillname = rs.getString("skill_name").intern();
				_skillnames.put(skillId, skillname);
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "스킬 이름 호출 중 오류가 발생했습니다.", e);
		}
	}
	
	public String getskillnameKor(int skillId)
	{
		if ((_skillnames != null) && !_skillnames.isEmpty())
		{
			return _skillnames.get(skillId);
		}
		return null;
	}
}
