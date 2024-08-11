package handlers.voicedcommandhandlers;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.AutoBuffManager;
import org.l2jmobius.gameserver.instancemanager.AutoSkillManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.taskmanager.auto.AutoPlayTaskManager;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author 보리넷 가츠
 */
public class AutoPlay implements IVoicedCommandHandler
{
	private static Location _startLocation = null;
	
	private static final String[] VOICED_COMMANDS =
	{
		"A자동사냥",
		"A거리설정",
		"A제자리가기",
		"A제자리말뚝딜",
		"S자동사냥",
		"버프선택",
		"버프등록",
		"스킬등록"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		final boolean isInPeaceZone = activeChar.isInsideZone(ZoneId.PEACE) || activeChar.isInsideZone(ZoneId.SAYUNE);
		if (isInPeaceZone)
		{
			activeChar.sendMessage("피스존에서는 자동사냥 실행이 불가능 합니다.");
			activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "피스존에서는 자동사냥 실행이 불가능 합니다."));
			return false;
		}
		
		if (command.equals("A자동사냥"))
		{
			showHtml(activeChar);
		}
		else if (command.equals("S자동사냥"))
		{
			if (activeChar.getAutoPlay())
			{
				activeChar.setAutoPlay(false);
				AutoPlayTaskManager.getInstance().stopAutoPlay(activeChar);
				activeChar.moveToLocation(0, 0, 0, 0);
			}
			else
			{
				activeChar.setTarget(null);
				checkBuffs(activeChar);
				checkSkills(activeChar);
				activeChar.setAutoPlay(true);
				if (activeChar.getVariables().getBoolean("자동사냥_제자리가기", false))
				{
					setStartLocation(activeChar.getLocation());
					activeChar.getVariables().set("자동사냥_현재위치", getStartLocation().getX() + " " + getStartLocation().getY() + " " + getStartLocation().getZ());
				}
				AutoPlayTaskManager.getInstance().doAutoPlay(activeChar);
			}
		}
		else if (command.equals("A거리설정"))
		{
			if (activeChar.getVariables().getBoolean("자동사냥_거리설정", false))
			{
				activeChar.getVariables().remove("자동사냥_거리설정");
			}
			else
			{
				activeChar.getVariables().set("자동사냥_거리설정", true);
				activeChar.getVariables().remove("자동사냥_제자리말뚝딜");
			}
			showHtml(activeChar);
		}
		else if (command.equals("A제자리가기"))
		{
			if (activeChar.getVariables().getBoolean("자동사냥_제자리가기", false))
			{
				activeChar.getVariables().remove("자동사냥_현재위치");
				activeChar.getVariables().remove("자동사냥_제자리가기");
			}
			else
			{
				setStartLocation(activeChar.getLocation());
				activeChar.getVariables().set("자동사냥_현재위치", getStartLocation().getX() + " " + getStartLocation().getY() + " " + getStartLocation().getZ());
				activeChar.getVariables().set("자동사냥_제자리가기", true);
				activeChar.getVariables().remove("자동사냥_제자리말뚝딜");
			}
			showHtml(activeChar);
		}
		else if (command.equals("A제자리말뚝딜"))
		{
			final ClassId classId = activeChar.getClassId();
			if ((CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId.getId())) || (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()) || (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_HEAL, classId.getId()))))
			{
				if (activeChar.getVariables().getBoolean("자동사냥_제자리말뚝딜", false))
				{
					activeChar.getVariables().remove("자동사냥_제자리말뚝딜");
				}
				else
				{
					activeChar.getVariables().set("자동사냥_제자리말뚝딜", true);
					activeChar.getVariables().remove("자동사냥_거리설정");
					activeChar.getVariables().remove("자동사냥_현재위치");
					activeChar.getVariables().remove("자동사냥_제자리가기");
				}
			}
			else
			{
				activeChar.sendMessage("궁수/위자드 계열 클래스만 설정이 가능합니다.");
			}
			showHtml(activeChar);
		}
		else if (command.startsWith("버프선택"))
		{
			final String autoBuff = target.replace("버프선택", " ");
			final String[] buffs = autoBuff.split(" ");
			final int index_buff = Integer.parseInt(buffs[0]);
			final int index_skill = Integer.parseInt(buffs[1]);
			
			autoSkillhtml(activeChar, index_buff, index_skill);
		}
		else if (command.startsWith("버프등록"))
		{
			final String autoBuff = target.replace("버프등록", " ");
			final String[] buffs = autoBuff.split(" ");
			final int skillId = Integer.parseInt(buffs[0]);
			final int index_buff = Integer.parseInt(buffs[1]);
			final int index_skill = Integer.parseInt(buffs[2]);
			
			if (activeChar.getVariables().getBoolean("자동버프_" + skillId, false))
			{
				activeChar.removeAutoBuff(skillId);
				activeChar.getVariables().remove("자동버프_" + skillId);
			}
			else
			{
				activeChar.addAutoBuff(skillId);
				activeChar.getVariables().set("자동버프_" + skillId, true);
			}
			autoSkillhtml(activeChar, index_buff, index_skill);
		}
		else if (command.startsWith("스킬등록"))
		{
			final String autoSkill = target.replace("스킬등록", " ");
			final String[] skills = autoSkill.split(" ");
			final int skillId = Integer.parseInt(skills[0]);
			final int index_buff = Integer.parseInt(skills[1]);
			final int index_skill = Integer.parseInt(skills[2]);
			
			if (activeChar.getVariables().getBoolean("자동스킬_" + skillId, false))
			{
				activeChar.getVariables().remove("자동스킬_" + skillId);
			}
			else
			{
				activeChar.getVariables().set("자동스킬_" + skillId, true);
			}
			resetAutoSkills(activeChar);
			autoSkillhtml(activeChar, index_buff, index_skill);
		}
		return true;
	}
	
	private String autoSkillhtml(Player player, int index_buff, int index_skill)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/html/borinet/autoplay/AutoBuffs/BuffChoice.htm");
		html = html.replace("%list%", buildAutoMenu(player, index_buff, index_skill));
		html = html.replace("%mainbanner%", BorinetHtml.getBannerForRace(player));
		Util.sendCBHtml(player, html);
		CommunityBoardHandler.separateAndSend(html, player);
		
		return html;
	}
	
	public String showHtml(Player player)
	{
		checkBuffs(player);
		checkSkills(player);
		
		String html = HtmCache.getInstance().getHtm(null, "data/html/borinet/autoplay/AutoPlay.htm");
		html = html.replace("%potion%", String.valueOf(player.getVariables().getInt("AUTO_POTION_HP_PERCENT", 0) > 0 ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미사용</font>"));
		html = html.replace("%autoItem%", String.valueOf(BorinetUtil.usingAutoItem(player) ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미사용</font>"));
		html = html.replace("%autoskill%", String.valueOf(checkSkills(player) ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미등록</font>"));
		html = html.replace("%autobuff%", String.valueOf(checkBuffs(player) ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미등록</font>"));
		html = html.replace("%status%", String.valueOf(player.getAutoPlay() ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미사용</font>"));
		html = html.replace("%isShort%", String.valueOf(player.getVariables().getBoolean("자동사냥_거리설정", false) ? "<font color=LEVEL>근거리</font>" : "<font color=LEVEL>원거리</font>"));
		html = html.replace("%location%", String.valueOf(player.getVariables().getBoolean("자동사냥_제자리가기", false) ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미사용</font>"));
		html = html.replace("%stay%", String.valueOf(player.getVariables().getBoolean("자동사냥_제자리말뚝딜", false) ? "<font color=LEVEL>사용 중</font>" : "<font color=FF0000>미사용</font>"));
		player.sendPacket(new NpcHtmlMessage(html));
		
		return html;
	}
	
	public String buildAutoMenu(Player player, int index_buff, int index_skill)
	{
		int count_buff = 0;
		int count_skill = 0;
		int nextIndex_buff = -1;
		int previousIndex_buff = -1;
		int nextIndex_skill = -1;
		int previousIndex_skill = -1;
		final int current_page_buff = index_buff;
		final int current_page_skill = index_skill;
		final int added_buff = index_buff + 10;
		final int added_skill = index_skill + 10;
		final StringBuilder sb = new StringBuilder();
		final List<Skill> buffList = new ArrayList<>();
		final List<Skill> skillList = new ArrayList<>();
		
		for (Skill skill : player.getAllSkills())
		{
			AutoBuffManager.getInstance();
			AutoSkillManager.getInstance();
			
			if (AutoBuffManager.ALLOWED_BUFFS.contains(skill.getId()))
			{
				count_buff++;
				
				if ((count_buff > index_buff) && (count_buff <= added_buff))
				{
					buffList.add(skill);
				}
			}
			
			if (AutoSkillManager.ALLOWED_SKILLS.contains(skill.getId()))
			{
				count_skill++;
				
				if ((count_skill > index_skill) && (count_skill <= added_skill))
				{
					skillList.add(skill);
				}
			}
		}
		
		if ((count_buff > 10) && (count_buff > (index_buff + 10)))
		{
			nextIndex_buff = index_buff + 10;
		}
		if (index_buff >= 10)
		{
			previousIndex_buff = index_buff - 10;
		}
		
		if ((count_skill > 10) && (count_skill > (index_skill + 10)))
		{
			nextIndex_skill = index_skill + 10;
		}
		if (index_skill >= 10)
		{
			previousIndex_skill = index_skill - 10;
		}
		
		sb.append("<table border=0 cellpadding=0 cellspacing=0 width=778 height=450 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr>");
		sb.append("<td height=28></td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td width=22></td>");
		sb.append("<td width=290 align=left>");
		sb.append("<table border=0 width=330 height=404 cellspacing=0 cellpadding=0 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\">");
		sb.append("<tr>");
		sb.append("<td width=280 height=260 align=left valign=top>");
		
		sb.append("<table width=370 border=0>");
		sb.append("<tr>");
		sb.append("<td align=center><br><font color=LEVEL name=hs12>사용할 버프(강화) 스킬</font></td>");
		sb.append("</tr>");
		sb.append("</table>");
		
		sb.append("<table width=330 height=20 border=0>");
		sb.append("<tr>");
		sb.append("<td align=left height=10></td>");
		sb.append("</tr>");
		if (buffList.isEmpty())
		{
			sb.append("<tr>");
			sb.append("<td fixwidth=\"26\"></td>");
			sb.append("<td align=center><br><br><br>사용 가능한 버프가 없습니다!</td>");
			sb.append("</tr>");
		}
		else
		{
			for (Skill skill : buffList)
			{
				sb.append("<tr>");
				sb.append("<td width=10></td>");
				sb.append("<td width=33 align=left><img src=" + skill.getIcon() + " width=32 height=32></td>");
				sb.append("<td width=220 align=left><Button width=220 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .버프등록 " + skill.getId() + " " + index_buff + " " + index_skill + "\">" + skill.getName() + "</Button></td>");
				sb.append("<td width=50 align=left>" + (player.getVariables().getBoolean("자동버프_" + skill.getId(), false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>") + "</td>");
				sb.append("</tr>");
			}
		}
		sb.append("</table>");
		sb.append("<table width=\"250\" border=\"0\">");
		sb.append("<tr>");
		
		if (previousIndex_buff > -1)
		{
			sb.append("<td align=right><button value=\"이전\" action=\"bypass -h voice .버프선택 " + previousIndex_buff + " " + current_page_skill + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		if (nextIndex_buff > -1)
		{
			sb.append("<td align=right><button value=\"다음\" action=\"bypass -h voice .버프선택 " + nextIndex_buff + " " + current_page_skill + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		
		sb.append("<td width=290 align=left>");
		sb.append("<table border=0 width=330 height=404 cellspacing=0 cellpadding=0 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\">");
		sb.append("<tr>");
		sb.append("<td width=280 align=left valign=top>");
		
		sb.append("<table width=370 border=0>");
		sb.append("<tr>");
		sb.append("<td align=center><br><font color=LEVEL name=hs12>사용할 공격 스킬</font></td>");
		sb.append("</tr>");
		sb.append("</table>");
		
		sb.append("<table width=330 height=20 border=0>");
		sb.append("<tr>");
		sb.append("<td align=left height=10></td>");
		sb.append("</tr>");
		if (skillList.isEmpty())
		{
			sb.append("<tr>");
			sb.append("<td fixwidth=\"45\"></td>");
			sb.append("<td align=center><br><br><br>사용 가능한 공격스킬이 없습니다!</td>");
			sb.append("</tr>");
		}
		else
		{
			for (Skill skill : skillList)
			{
				sb.append("<tr>");
				sb.append("<td width=10></td>");
				sb.append("<td width=33 height=32 align=left><img src=" + skill.getIcon() + " width=32 height=32></td>");
				sb.append("<td width=220 align=left><Button width=220 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .스킬등록 " + skill.getId() + " " + index_buff + " " + index_skill + "\">" + skill.getName() + "</Button></td>");
				sb.append("<td width=50 align=left fixwidth=\"50\">" + (player.getVariables().getBoolean("자동스킬_" + skill.getId(), false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>") + "</td>");
				sb.append("</tr>");
			}
		}
		sb.append("</table>");
		sb.append("<table width=\"250\" border=\"0\">");
		sb.append("<tr>");
		
		if (previousIndex_skill > -1)
		{
			sb.append("<td align=right><button value=\"이전\" action=\"bypass -h voice .버프선택 " + current_page_buff + " " + previousIndex_skill + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		if (nextIndex_skill > -1)
		{
			sb.append("<td align=right><button value=\"다음\" action=\"bypass -h voice .버프선택 " + current_page_buff + " " + nextIndex_skill + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	private boolean checkBuffs(Player player)
	{
		player.getAutoBuffs().clear();
		int list = 0;
		for (Skill skill : player.getAllSkills())
		{
			AutoBuffManager.getInstance();
			if (AutoBuffManager.ALLOWED_BUFFS.contains(skill.getId()))
			{
				if (player.getVariables().getBoolean("자동버프_" + skill.getId(), false))
				{
					list++;
					player.addAutoBuff(skill.getId());
				}
			}
		}
		return list > 0 ? true : false;
	}
	
	private boolean checkSkills(Player player)
	{
		int list = 0;
		for (Skill skill : player.getAllSkills())
		{
			AutoSkillManager.getInstance();
			if (AutoSkillManager.ALLOWED_SKILLS.contains(skill.getId()))
			{
				if (player.getVariables().getBoolean("자동스킬_" + skill.getId(), false))
				{
					list++;
					player.addAutoSkill(skill.getId());
				}
			}
		}
		return list > 0 ? true : false;
	}
	
	private void resetAutoSkills(Player player)
	{
		player.getAutoSkills().clear();
		for (Skill skill : player.getAllSkills())
		{
			AutoSkillManager.getInstance();
			if (AutoSkillManager.ALLOWED_SKILLS.contains(skill.getId()))
			{
				if (player.getVariables().getBoolean("자동스킬_" + skill.getId(), false))
				{
					player.addAutoSkill(skill.getId());
				}
			}
		}
		player.resetSkillOrder();
	}
	
	private void setStartLocation(Location loc)
	{
		_startLocation = loc;
	}
	
	private Location getStartLocation()
	{
		return _startLocation;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}