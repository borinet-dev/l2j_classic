package org.l2jmobius.gameserver.model.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jmobius.gameserver.util.BorinetHtml;

public class CustomStats
{
	// 스탯 포인트
	public void buyStatPoint(Player player, String arg)
	{
		if ((arg != null) && (!arg.isEmpty()))
		{
			final int count = Integer.parseInt(arg);
			int addStats = player.getStatPoint() + count;
			int addStatsA = player.getStatPointA() + count;
			int luna = count * 100;
			
			int canPoint = 30 - player.getStatPointA();
			
			if (count > canPoint)
			{
				player.sendMessage("최대 스탯 포인트를 초과하였습니다.");
				player.sendMessage("현재 충전가능 스탯 포인트는 " + canPoint + "포인트 입니다.");
				BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
				return;
			}
			if (count == 0)
			{
				player.sendMessage("입력값이 잘못되었습니다.");
				BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
				return;
			}
			if (player.getLuna() < luna)
			{
				player.sendMessage("루나가 부족합니다.");
				BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
				return;
			}
			player.destroyItemByItemId("스탯포인트", Config.LUNA, luna, player, true);
			player.statUpdate("point", addStats, false);
			player.statUpdateA(addStatsA);
			player.sendMessage(count + "스탯 포인트를 충전하였습니다.");
			BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
			return;
		}
		player.sendMessage("입력값이 잘못되었습니다.");
		BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
		return;
	}
	
	public void insertStat(Player player)
	{
		int[] count = StatCheck(player);
		int pointChar = count[0];
		int checkChar = count[7];
		
		if (checkChar == 1)
		{
			if (getStatsAcc(player) > getStatsChar(player))
			{
				int point = getStatsAcc(player) - getStatsChar(player);
				player.statUpdate("point", pointChar + point, false);
			}
			else
			{
				player.statUpdateA(getStatsChar(player));
			}
			return;
		}
		player.statUpdate("", 0, true);
		if (getStatsAcc(player) > getStatsChar(player))
		{
			int point = getStatsAcc(player) - getStatsChar(player);
			player.statUpdate("point", pointChar + point, false);
		}
		else
		{
			player.statUpdateA(getStatsChar(player));
		}
	}
	
	public static void Str(Player player, int arg)
	{
		final int count = arg;
		player.statUpdate("str", count, false);
		
		if (count == 0)
		{
			player.removeSkill(30211, true);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30211, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		
		BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
	}
	
	public static void Dex(Player player, int arg)
	{
		final int count = arg;
		player.statUpdate("dex", count, false);
		
		if (count == 0)
		{
			player.removeSkill(30212, true);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30212, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
	}
	
	public static void Con(Player player, int arg)
	{
		final int count = arg;
		player.statUpdate("con", count, false);
		
		if (count == 0)
		{
			player.removeSkill(30213, true);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30213, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
	}
	
	public static void Mpw(Player player, int arg)
	{
		final int count = arg;
		player.statUpdate("mpw", count, false);
		
		if (count == 0)
		{
			player.removeSkill(30214, true);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30214, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
	}
	
	public static void Wit(Player player, int arg)
	{
		final int count = arg;
		player.statUpdate("wit", count, false);
		
		if (count == 0)
		{
			player.removeSkill(30215, true);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30215, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
	}
	
	public static void Men(Player player, int arg)
	{
		final int count = arg;
		player.statUpdate("men", count, false);
		
		if (count == 0)
		{
			player.removeSkill(30216, true);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30216, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customStat.htm", 0, "");
	}
	
	// 스펙 포인트
	public void buySpecPoint(Player player, String arg)
	{
		if ((arg != null) && (!arg.isEmpty()))
		{
			final int count = Integer.parseInt(arg);
			int addStats = player.getSpecPoint() + count;
			int addStatsA = player.getSpecPointA() + count;
			int luna = count * 100;
			
			int canPoint = 80 - player.getSpecPointA();
			
			if (count > canPoint)
			{
				player.sendMessage("최대 특수 포인트를 초과하였습니다.");
				player.sendMessage("현재 충전가능 특수 포인트는 " + canPoint + "포인트 입니다.");
				BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
				return;
			}
			if (count == 0)
			{
				player.sendMessage("입력값이 잘못되었습니다.");
				BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
				return;
			}
			if (player.getLuna() < luna)
			{
				player.sendMessage("루나가 부족합니다.");
				BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
				return;
			}
			player.destroyItemByItemId("특수포인트", Config.LUNA, luna, player, true);
			player.specUpdate("point", addStats, false);
			player.specUpdateA(addStatsA);
			player.sendMessage(count + "특수 포인트를 충전하였습니다.");
			BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
			return;
		}
		player.sendMessage("입력값이 잘못되었습니다.");
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
		return;
	}
	
	public void insertSpec(Player player)
	{
		int checkChar = 0;
		int[] count = specCheck(player);
		int pointChar = count[0];
		checkChar = count[9];
		
		if (checkChar == 1)
		{
			if (getSpecAcc(player) > getSpecChar(player))
			{
				int point = getSpecAcc(player) - getSpecChar(player);
				player.specUpdate("point", pointChar + point, false);
			}
			else
			{
				player.specUpdateA(getSpecChar(player));
			}
			return;
		}
		player.specUpdate("", 0, true);
		if (getSpecAcc(player) > getSpecChar(player))
		{
			int point = getSpecAcc(player) - getSpecChar(player);
			player.specUpdate("point", pointChar + point, false);
		}
		else
		{
			player.specUpdateA(getSpecChar(player));
		}
	}
	
	public static void spec1(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec1", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30200, player.getSkillLevel(30200));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30200, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	public static void spec2(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec2", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30201, player.getSkillLevel(30201));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30201, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	public static void spec3(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec3", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30202, player.getSkillLevel(30202));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30202, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	public static void spec4(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec4", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30203, player.getSkillLevel(30203));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30203, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	public static void spec5(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec5", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30204, player.getSkillLevel(30204));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30204, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	public static void spec6(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec6", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30205, player.getSkillLevel(30205));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30205, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	public static void spec7(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec7", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30206, player.getSkillLevel(30206));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30206, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	public static void spec8(Player player, int arg)
	{
		final int count = arg;
		player.specUpdate("spec8", count, false);
		
		if (count == 0)
		{
			final Skill skill = SkillData.getInstance().getSkill(30207, player.getSkillLevel(30207));
			player.removeSkill(skill);
		}
		else
		{
			player.addSkill(SkillData.getInstance().getSkill(30207, count), false);
		}
		player.sendSkillList();
		player.broadcastUserInfo();
		BorinetHtml.showHtml(player, "LunaShop/customSpec.htm", 0, "");
	}
	
	// 스탯, 스펙 체크
	public int getStatsAcc(Player player)
	{
		int point = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT stats FROM accounts WHERE login = '" + player.getAccountName() + "'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				point = rset.getInt("stats");
			}
		}
		catch (Exception e)
		{
		}
		return point;
	}
	
	public int getStatsChar(Player player)
	{
		int total = 0;
		int point = 0;
		int Str = 0;
		int Dex = 0;
		int Con = 0;
		int Mpw = 0;
		int Wit = 0;
		int Men = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT point, str, dex, con, mpw, wit, men FROM character_stats WHERE charId = '" + player.getObjectId() + "'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				point = rset.getInt("point");
				Str = rset.getInt("str");
				Dex = rset.getInt("dex");
				Con = rset.getInt("con");
				Mpw = rset.getInt("mpw");
				Wit = rset.getInt("wit");
				Men = rset.getInt("men");
				
				total = point + Str + Dex + Con + Mpw + Wit + Men;
			}
		}
		catch (Exception e)
		{
		}
		return total;
	}
	
	public int getSpecAcc(Player player)
	{
		int point = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT spec FROM accounts WHERE login = '" + player.getAccountName() + "'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				point = rset.getInt("spec");
			}
		}
		catch (Exception e)
		{
		}
		return point;
	}
	
	public int getSpecChar(Player player)
	{
		int total = 0;
		int point = 0;
		int Spec1 = 0;
		int Spec2 = 0;
		int Spec3 = 0;
		int Spec4 = 0;
		int Spec5 = 0;
		int Spec6 = 0;
		int Spec7 = 0;
		int Spec8 = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT point, spec1, spec2, spec3, spec4, spec5, spec6, spec7, spec8 FROM character_spec WHERE charId = '" + player.getObjectId() + "'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				point = rset.getInt("point");
				Spec1 = rset.getInt("spec1");
				Spec2 = rset.getInt("spec2");
				Spec3 = rset.getInt("spec3");
				Spec4 = rset.getInt("spec4");
				Spec5 = rset.getInt("spec5");
				Spec6 = rset.getInt("spec6");
				Spec7 = rset.getInt("spec7");
				Spec8 = rset.getInt("spec8");
				
				total = point + Spec1 + Spec2 + Spec3 + Spec4 + Spec5 + Spec6 + Spec7 + Spec8;
			}
		}
		catch (Exception e)
		{
		}
		return total;
	}
	
	public int[] StatCheck(Player player)
	{
		String Char = null;
		int point = 0;
		int Str = 0;
		int Dex = 0;
		int Con = 0;
		int Mpw = 0;
		int Wit = 0;
		int Men = 0;
		int charname = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM character_stats WHERE charId = '" + player.getObjectId() + "'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				point = rset.getInt("point");
				Str = rset.getInt("str");
				Dex = rset.getInt("dex");
				Con = rset.getInt("con");
				Mpw = rset.getInt("mpw");
				Wit = rset.getInt("wit");
				Men = rset.getInt("men");
				Char = rset.getString("char_name");
			}
			if (Char != null)
			{
				charname = 1;
			}
		}
		catch (Exception e)
		{
		}
		return new int[]
		{
			point,
			Str,
			Dex,
			Con,
			Mpw,
			Wit,
			Men,
			charname
		};
	}
	
	public int[] specCheck(Player player)
	{
		String Char = null;
		int point = 0;
		int spec1 = 0;
		int spec2 = 0;
		int spec3 = 0;
		int spec4 = 0;
		int spec5 = 0;
		int spec6 = 0;
		int spec7 = 0;
		int spec8 = 0;
		int charname = 0;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM character_spec WHERE charId = '" + player.getObjectId() + "'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				point = rset.getInt("point");
				spec1 = rset.getInt("spec1");
				spec2 = rset.getInt("spec2");
				spec3 = rset.getInt("spec3");
				spec4 = rset.getInt("spec4");
				spec5 = rset.getInt("spec5");
				spec6 = rset.getInt("spec6");
				spec7 = rset.getInt("spec7");
				spec8 = rset.getInt("spec8");
				Char = rset.getString("char_name");
			}
			if (Char != null)
			{
				charname = 1;
			}
		}
		catch (Exception e)
		{
		}
		return new int[]
		{
			point,
			spec1,
			spec2,
			spec3,
			spec4,
			spec5,
			spec6,
			spec7,
			spec8,
			charname
		};
	}
	
	public void subclassChange(Player player)
	{
		addskills(player);
		if (player.isInParty())
		{
			player.getParty().broadcastToPartyMembers(player, PartySmallWindowDeleteAll.STATIC_PACKET);
			for (Player member : player.getParty().getMembers())
			{
				if (member != player)
				{
					member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
				}
			}
		}
		if (player.getClan() != null)
		{
			player.getClan().broadcastClanStatus();
		}
		
		ThreadPool.schedule(() ->
		{
			player.setCurrentHp(currentHp(player));
			player.setCurrentMp(currentMp(player));
			player.setCurrentCp(currentCp(player));
		}, 500); // (단위: 밀리초)
	}
	
	private int currentHp(Player player)
	{
		int HP = player.getVariables().getInt("CURRENT_HP_" + player.getClassIndex(), 0);
		if ((HP != 0) && (HP <= player.getMaxHp()))
		{
			return HP;
		}
		return player.getMaxHp();
	}
	
	private int currentMp(Player player)
	{
		int MP = player.getVariables().getInt("CURRENT_MP_" + player.getClassIndex(), 0);
		if ((MP != 0) && (MP <= player.getMaxMp()))
		{
			return MP;
		}
		return player.getMaxMp();
	}
	
	private int currentCp(Player player)
	{
		int CP = player.getVariables().getInt("CURRENT_CP_" + player.getClassIndex(), 0);
		if ((CP != 0) && (CP <= player.getMaxCp()))
		{
			return CP;
		}
		return player.getMaxCp();
	}
	
	public void addskills(Player player)
	{
		if (player == null)
		{
			return;
		}
		
		// 특성 스킬 지급
		if (player.getSpec1() > 0)
		{
			player.addSkill(SkillData.getInstance().getSkill(30200, player.getSpec1()), false);
		}
		if (player.getSpec2() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30201, player.getSpec2()), false);
		}
		if (player.getSpec3() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30202, player.getSpec3()), false);
		}
		if (player.getSpec4() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30203, player.getSpec4()), false);
		}
		if (player.getSpec5() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30204, player.getSpec5()), false);
		}
		if (player.getSpec6() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30205, player.getSpec6()), false);
		}
		if (player.getSpec7() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30206, player.getSpec7()), false);
		}
		if (player.getSpec8() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30207, player.getSpec8()), false);
		}
		// 특성 스킬 지급 종료
		
		// 스탯 스킬 지급
		if (player.getStr() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30211, player.getStr()), false);
		}
		if (player.getDex() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30212, player.getDex()), false);
		}
		if (player.getCon() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30213, player.getCon()), false);
		}
		if (player.getMpw() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30214, player.getMpw()), false);
		}
		if (player.getWit() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30215, player.getWit()), false);
		}
		if (player.getMen() >= 1)
		{
			player.addSkill(SkillData.getInstance().getSkill(30216, player.getMen()), false);
		}
		// 스탯 스킬 지급 종료
		
		player.sendSkillList();
		player.broadcastUserInfo();
	}
	
	public void checkName(Player player)
	{
		String CharName = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name FROM character_spec WHERE charId = '" + player.getObjectId() + "'"))
		{
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				CharName = rset.getString("char_name");
			}
			if (CharName != player.getName())
			{
				try (PreparedStatement ps = con.prepareStatement("UPDATE character_spec SET char_name=? WHERE charId =?"))
				{
					ps.setString(1, player.getName());
					ps.setInt(2, player.getObjectId());
					ps.executeUpdate();
				}
				try (PreparedStatement ps = con.prepareStatement("UPDATE character_stats SET char_name=? WHERE charId =?"))
				{
					ps.setString(1, player.getName());
					ps.setInt(2, player.getObjectId());
					ps.executeUpdate();
				}
			}
		}
		catch (Exception e)
		{
		}
	}
	
	private static final CustomStats _instance = new CustomStats();
	
	public static CustomStats getInstance()
	{
		return _instance;
	}
}