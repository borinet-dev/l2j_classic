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
package org.l2jmobius.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SellBuffHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import org.l2jmobius.gameserver.util.HtmlUtil;
import org.l2jmobius.gameserver.util.Util;

/**
 * Sell Buffs Manager
 * @author St3eT
 */
public class SellBuffsManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SellBuffsManager.class.getName());
	private static final Set<Integer> ALLOWED_BUFFS = new HashSet<>();
	private static final String HTML_FOLDER = "data/html/mods/SellBuffs/";
	
	protected SellBuffsManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		ALLOWED_BUFFS.clear();
		parseDatapackFile("data/SellBuffData.xml");
		LOGGER.info("버프판매: " + ALLOWED_BUFFS.size() + "개의 버프를 로드하였습니다.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		final NodeList node = doc.getDocumentElement().getElementsByTagName("skill");
		for (int i = 0; i < node.getLength(); ++i)
		{
			final Element elem = (Element) node.item(i);
			final int skillId = Integer.parseInt(elem.getAttribute("id"));
			ALLOWED_BUFFS.add(skillId);
		}
	}
	
	public void sendSellMenu(Player player)
	{
		String html = HtmCache.getInstance().getHtm(player, HTML_FOLDER + (player.isSellingBuffs() ? "BuffMenu_already.html" : "BuffMenu.html"));
		html = html.replace("<?min_price?>", String.valueOf(Util.formatAdena(Config.SELLBUFF_MIN_PRICE)));
		html = html.replace("<?max_price?>", String.valueOf(Util.formatAdena(Config.SELLBUFF_MAX_PRICE)));
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	public void sendBuffChoiceMenu(Player player, int index)
	{
		String html = HtmCache.getInstance().getHtm(player, HTML_FOLDER + "BuffChoice.html");
		html = html.replace("%list%", buildSkillMenu(player, index));
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	public void sendBuffEditMenu(Player player)
	{
		String html = HtmCache.getInstance().getHtm(player, HTML_FOLDER + "BuffChoice.html");
		html = html.replace("%list%", buildEditMenu(player));
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	public void sendBuffMenu(Player player, Player seller, int index)
	{
		if (!seller.isSellingBuffs() || seller.getSellingBuffs().isEmpty())
		{
			return;
		}
		
		String html = HtmCache.getInstance().getHtm(player, HTML_FOLDER + "BuffBuyMenu.html");
		html = html.replace("%list%", buildBuffMenu(seller, index));
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	public void startSellBuffs(Player player, String title)
	{
		player.sitDown();
		player.setSellingBuffs(true);
		player.setPrivateStoreType(PrivateStoreType.PACKAGE_SELL);
		player.getSellList().setTitle(title);
		player.getSellList().setPackaged(true);
		player.broadcastUserInfo();
		player.broadcastPacket(new ExPrivateStoreSetWholeMsg(player));
		sendSellMenu(player);
	}
	
	public void stopSellBuffs(Player player)
	{
		player.setSellingBuffs(false);
		player.setPrivateStoreType(PrivateStoreType.NONE);
		player.standUp();
		player.broadcastUserInfo();
	}
	
	private String buildBuffMenu(Player seller, int index)
	{
		final int ceiling = 10;
		int nextIndex = -1;
		int previousIndex = -1;
		int emptyFields = 0;
		final StringBuilder sb = new StringBuilder();
		final List<SellBuffHolder> sellList = new ArrayList<>();
		
		int count = 0;
		for (SellBuffHolder holder : seller.getSellingBuffs())
		{
			count++;
			if ((count > index) && (count <= (ceiling + index)))
			{
				sellList.add(holder);
			}
		}
		
		if ((count > 10) && (count > (index + 10)))
		{
			nextIndex = index + 10;
		}
		
		if (index >= 10)
		{
			previousIndex = index - 10;
		}
		
		emptyFields = ceiling - sellList.size();
		
		sb.append("<br>");
		sb.append(HtmlUtil.getMpGauge(250, (long) seller.getCurrentMp(), seller.getMaxMp(), false));
		sb.append("<br>");
		
		sb.append("<table border=0 cellpadding=0 cellspacing=0 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr><td><br><br><br></td></tr>");
		sb.append("<tr>");
		sb.append("<td fixwidth=\"10\"></td>");
		sb.append("<td> <button action=\"\" value=\"Icon\" width=75 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Icon
		sb.append("<td> <button action=\"\" value=\"이름\" width=175 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Name
		sb.append("<td> <button action=\"\" value=\"레벨\" width=85 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Leve
		sb.append("<td> <button action=\"\" value=\"MP 소모\" width=100 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Price
		sb.append("<td> <button action=\"\" value=\"가격\" width=170 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Price
		sb.append("<td> <button action=\"\" value=\"선택\" width=130 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Action
		sb.append("<td fixwidth=\"20\"></td>");
		sb.append("</tr>");
		
		for (SellBuffHolder holder : sellList)
		{
			final Skill skill = seller.getKnownSkill(holder.getSkillId());
			if (skill == null)
			{
				emptyFields++;
				continue;
			}
			
			final ItemTemplate item = ItemTable.getInstance().getTemplate(Config.SELLBUFF_PAYMENT_ID);
			
			sb.append("<tr>");
			sb.append("<td fixwidth=\"20\"></td>");
			sb.append("<td align=center><img src=\"" + skill.getIcon() + "\" width=\"32\" height=\"32\"></td>");
			sb.append("<td align=left>" + skill.getName() + (skill.getLevel() > 100 ? "<font color=\"LEVEL\"> + " + (skill.getLevel() % 100) + "</font></td>" : "</td>"));
			sb.append("<td align=center>" + ((skill.getLevel() > 100) ? SkillData.getInstance().getMaxLevel(skill.getId()) : skill.getLevel()) + "</td>");
			sb.append("<td align=center> <font color=\"1E90FF\">" + (skill.getMpConsume() * Config.SELLBUFF_MP_MULTIPLER) + "</font></td>");
			sb.append("<td align=center> " + Util.formatAdena(holder.getPrice()) + " <font color=\"LEVEL\"> " + (item != null ? item.getName() : "") + "</font> </td>");
			sb.append("<td align=center>");
			sb.append("<table border=0 cellpadding=0 cellspacing=0");
			sb.append("<tr>");
			sb.append("<td align=center><button value=\"버프 구매\" action=\"bypass -h sellbuffbuyskill " + seller.getObjectId() + " " + skill.getId() + " " + index + "\" width=\"80\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td align=center><button value=\"펫\" action=\"bypass -h sellbuffbuyskillPet " + seller.getObjectId() + " " + skill.getId() + " " + index + "\" width=\"35\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("</table>");
			sb.append("</td>");
			
			sb.append("<tr><td><br><br></td></tr>");
		}
		
		for (int i = 0; i < emptyFields; i++)
		{
			sb.append("<tr>");
			sb.append("<td fixwidth=\"20\" height=\"32\"></td>");
			sb.append("<td align=center></td>");
			sb.append("<td align=left></td>");
			sb.append("<td align=center></td>");
			sb.append("<td align=center></font></td>");
			sb.append("<td align=center></td>");
			sb.append("<td align=center fixwidth=\"50\"></td>");
			sb.append("</tr>");
			sb.append("<tr><td><br><br></td></tr>");
		}
		
		sb.append("</table>");
		
		sb.append("<table width=\"250\" border=\"0\">");
		sb.append("<tr>");
		
		if (previousIndex > -1)
		{
			sb.append("<td align=left><button value=\"이전\" action=\"bypass -h sellbuffbuymenu " + seller.getObjectId() + " " + previousIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		if (nextIndex > -1)
		{
			sb.append("<td align=right><button value=\"다음\" action=\"bypass -h sellbuffbuymenu " + seller.getObjectId() + " " + nextIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	private String buildEditMenu(Player player)
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<table border=0 cellpadding=0 cellspacing=0 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr><td><br><br><br></td></tr>");
		sb.append("<tr>");
		sb.append("<td fixwidth=\"10\"></td>");
		sb.append("<td> <button action=\"\" value=\"Icon\" width=75 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Icon
		sb.append("<td> <button action=\"\" value=\"이름\" width=150 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Name
		sb.append("<td> <button action=\"\" value=\"레벨\" width=75 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Level
		sb.append("<td> <button action=\"\" value=\"기존 가격\" width=100 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Old price
		sb.append("<td> <button action=\"\" value=\"신규 가격\" width=125 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // New price
		sb.append("<td> <button action=\"\" value=\"상태\" width=125 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Change Price
		sb.append("<td> <button action=\"\" value=\"제거\" width=85 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Remove Buff
		sb.append("<td fixwidth=\"20\"></td>");
		sb.append("</tr>");
		
		if (player.getSellingBuffs().isEmpty())
		{
			sb.append("</table>");
			sb.append("<br><br><br>");
			sb.append("아직 추가한 버프가 없습니다!");
		}
		else
		{
			for (SellBuffHolder holder : player.getSellingBuffs())
			{
				final Skill skill = player.getKnownSkill(holder.getSkillId());
				if (skill == null)
				{
					continue;
				}
				
				sb.append("<tr>");
				sb.append("<td fixwidth=\"20\"></td>");
				sb.append("<td align=center><img src=\"" + skill.getIcon() + "\" width=\"32\" height=\"32\"></td>"); // Icon
				sb.append("<td align=left>" + skill.getName() + (skill.getLevel() > 100 ? "<font color=\"LEVEL\"> + " + (skill.getLevel() % 100) + "</font></td>" : "</td>")); // Name + enchant
				sb.append("<td align=center>" + ((skill.getLevel() > 100) ? SkillData.getInstance().getMaxLevel(skill.getId()) : skill.getLevel()) + "</td>"); // Level
				sb.append("<td align=center> " + Util.formatAdena(holder.getPrice()) + " </td>"); // Price show
				sb.append("<td align=center><edit var=\"price_" + skill.getId() + "\" width=120 type=\"number\"></td>"); // Price edit
				sb.append("<td align=center><button value=\"설정\" action=\"bypass -h sellbuffchangeprice " + skill.getId() + " $price_" + skill.getId() + "\" width=\"85\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				sb.append("<td align=center><button value=\" X \" action=\"bypass -h sellbuffremove " + skill.getId() + "\" width=\"26\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				sb.append("</tr>");
				sb.append("<tr><td><br><br></td></tr>");
			}
			sb.append("</table>");
		}
		
		return sb.toString();
	}
	
	private String buildSkillMenu(Player player, int index)
	{
		final int ceiling = index + 10;
		int nextIndex = -1;
		int previousIndex = -1;
		final StringBuilder sb = new StringBuilder();
		final List<Skill> skillList = new ArrayList<>();
		
		int count = 0;
		for (Skill skill : player.getAllSkills())
		{
			if (ALLOWED_BUFFS.contains(skill.getId()) && !isInSellList(player, skill))
			{
				count++;
				
				if ((count > index) && (count <= ceiling))
				{
					skillList.add(skill);
				}
			}
		}
		
		if ((count > 10) && (count > (index + 10)))
		{
			nextIndex = index + 10;
		}
		
		if (index >= 10)
		{
			previousIndex = index - 10;
		}
		
		sb.append("<table border=0 cellpadding=0 cellspacing=0 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr><td><br><br><br></td></tr>");
		sb.append("<tr>");
		sb.append("<td fixwidth=\"10\"></td>");
		sb.append("<td> <button action=\"\" value=\"Icon\" width=100 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Icon
		sb.append("<td> <button action=\"\" value=\"이름\" width=175 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Name
		sb.append("<td> <button action=\"\" value=\"레벨\" width=150 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Leve
		sb.append("<td> <button action=\"\" value=\"가격\" width=150 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Price
		sb.append("<td> <button action=\"\" value=\"상태\" width=125 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"> </td>"); // Action
		sb.append("<td fixwidth=\"20\"></td>");
		sb.append("</tr>");
		
		if (skillList.isEmpty())
		{
			sb.append("</table>");
			sb.append("<br><br><br>");
			sb.append("현재 판매 가능한 버프가 없습니다!");
		}
		else
		{
			for (Skill skill : skillList)
			{
				sb.append("<tr>");
				sb.append("<td fixwidth=\"20\"></td>");
				sb.append("<td align=center><img src=\"" + skill.getIcon() + "\" width=\"32\" height=\"32\"></td>");
				sb.append("<td align=left>" + skill.getName() + (skill.getLevel() > 100 ? "<font color=\"LEVEL\"> + " + (skill.getLevel() % 100) + "</font></td>" : "</td>"));
				sb.append("<td align=center>" + ((skill.getLevel() > 100) ? SkillData.getInstance().getMaxLevel(skill.getId()) : skill.getLevel()) + "</td>");
				sb.append("<td align=center><edit var=\"price_" + skill.getId() + "\" width=120 type=\"number\"></td>");
				sb.append("<td align=center fixwidth=\"50\"><button value=\"버프 추가\" action=\"bypass -h sellbuffaddskill " + skill.getId() + " $price_" + skill.getId() + "\" width=\"85\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				sb.append("</tr>");
				sb.append("<tr><td><br><br></td></tr>");
			}
			sb.append("</table>");
		}
		
		sb.append("<table width=\"250\" border=\"0\">");
		sb.append("<tr>");
		
		if (previousIndex > -1)
		{
			sb.append("<td align=left><button value=\"이전\" action=\"bypass -h sellbuffadd " + previousIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		if (nextIndex > -1)
		{
			sb.append("<td align=right><button value=\"다음\" action=\"bypass -h sellbuffadd " + nextIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	public boolean isInSellList(Player player, Skill skill)
	{
		for (SellBuffHolder holder : player.getSellingBuffs())
		{
			if (holder.getSkillId() == skill.getId())
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean canStartSellBuffs(Player player)
	{
		if (player.isAlikeDead())
		{
			player.sendMessage("페이크 데스 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("올림피아드 경기 등록 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isOnEvent())
		{
			player.sendMessage("이벤트 등록 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isCursedWeaponEquipped() || (player.getReputation() < 0))
		{
			player.sendMessage("카오 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isInDuel())
		{
			player.sendMessage("듀얼 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isFishing())
		{
			player.sendMessage("낚시 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isMounted() || player.isFlyingMounted() || player.isFlying())
		{
			player.sendMessage("탈것을 탑승 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isTransformed())
		{
			player.sendMessage("변신 상태에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		else if (player.isInsideZone(ZoneId.NO_STORE) || !player.isInsideZone(ZoneId.PEACE) || player.isJailed())
		{
			player.sendMessage("이곳에서는 버프판매를 할 수 없습니다.");
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the single instance of {@code SellBuffsManager}.
	 * @return single instance of {@code SellBuffsManager}
	 */
	public static SellBuffsManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SellBuffsManager INSTANCE = new SellBuffsManager();
	}
}