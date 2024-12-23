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
package handlers.admincommandhandlers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExWorldChatCnt;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BuilderUtil;
import org.l2jmobius.gameserver.util.Util;
import org.l2jmobius.gameserver.util.CaptchaSystem.CaptchaWindow;

/**
 * This class handles following admin commands: - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus - gmliston/gmlistoff = includes/excludes active character from /gmlist results - silence = toggles private messages acceptance mode - diet = toggles weight penalty mode -
 * tradeoff = toggles trade acceptance mode - reload = reloads specified component from multisell|skill|npc|htm|item - set/set_menu/set_mod = alters specified server setting - saveolymp = saves olympiad state manually - manualhero = cycles olympiad and calculate new heroes.
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2007/07/28 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_admin5",
		"admin_admin6",
		"admin_admin7",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		"admin_diet",
		"admin_tradeoff",
		"admin_set",
		"admin_set_mod",
		"admin_saveolymp",
		"admin_startolymp",
		"admin_sethero",
		"admin_unsethero",
		"admin_settruehero",
		"admin_givehero",
		"admin_endolympiad",
		"admin_setconfig",
		"admin_config_server",
		"admin_gmon",
		"admin_worldchat",
		"admin_song",
		"admin_captcha",
		"admin_gettime"
	};
	
	// @formatter:off
	private static final int[] HERO_ITEMS =
	{
		30392, 30393, 30394, 30395, 30396,
		30397, 30398, 30399, 30400, 30401,
		30402, 30403, 30404, 30405, 30372,
		30373, 6842, 6611, 6612, 6613, 6614,
		6615, 6616, 6617, 6618, 6619, 6620,
		6621, 9388, 9389, 9390
	};
	// @formatter:on
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_admin"))
		{
			showMainPage(activeChar, command);
		}
		else if (command.equals("admin_gettime"))
		{
			// 현재 시간 구하기
			Instant currentInstant = Instant.now();
			// 미래의 날짜와 시간 설정
			LocalDateTime futureDateTime = LocalDateTime.of(2023, 9, 19, 7, 0);
			// 현재 시간과 미래 시간 간의 차이 구하기
			long secondsUntilFuture = ChronoUnit.SECONDS.between(currentInstant, futureDateTime.toInstant(ZoneOffset.UTC));
			
			System.out.println("현재로부터 " + secondsUntilFuture + "초 후입니다.");
		}
		else if (command.equals("admin_captcha"))
		{
			Player target = activeChar;
			if (activeChar.getTarget() == null)
			{
				target = activeChar.getActingPlayer();
			}
			else
			{
				target = activeChar.getTarget().getActingPlayer();
			}
			boolean isCaptchaActive = target.getQuickVarB("IsCaptchaActive", false);
			if (!isCaptchaActive)
			{
				target.addQuickVar("IsCaptchaActive", true);
				target.clearCaptcha();
				target.addQuickVar("LastCaptcha", System.currentTimeMillis());
				target.setEscDisabled(true);
				target.startPopupDelay();
				CaptchaWindow.CaptchaWindows(target, 0);
			}
			else
			{
				activeChar.sendMessage("대상은 보안문자 입력 중 입니다.");
			}
		}
		else if (command.equals("admin_config_server"))
		{
			showConfigPage(activeChar);
		}
		else if (command.equals("admin_song"))
		{
			AdminHtml.showAdminHtml(activeChar, "songs/songs.htm");
		}
		else if (command.startsWith("admin_gmliston"))
		{
			AdminData.getInstance().addGm(activeChar, false);
			BuilderUtil.sendSysMessage(activeChar, "Registered into GM list.");
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_gmlistoff"))
		{
			AdminData.getInstance().addGm(activeChar, true);
			BuilderUtil.sendSysMessage(activeChar, "Removed from GM list.");
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_silence"))
		{
			if (activeChar.isSilenceMode()) // already in message refusal mode
			{
				activeChar.setSilenceMode(false);
				activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
			}
			else
			{
				activeChar.setSilenceMode(true);
				activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
			}
		}
		else if (command.startsWith("admin_saveolymp"))
		{
			GlobalVariablesManager.getInstance().set("Olympiad_Period", 1);
			int period = GlobalVariablesManager.getInstance().getInt("Olympiad_Period", 0);
			BuilderUtil.sendSysMessage(activeChar, "olympiad system start." + period);
		}
		else if (command.startsWith("admin_sethero"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			
			final Player target = activeChar.getTarget().isPlayer() ? activeChar.getTarget().getActingPlayer() : activeChar;
			if (!target.isHero())
			{
				target.setHero(true);
				target.broadcastUserInfo();
			}
			else
			{
				activeChar.sendMessage("대상은 이미 영웅캐릭터 입니다.");
			}
		}
		else if (command.equals("admin_unsethero"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			
			final Player target = activeChar.getTarget().isPlayer() ? activeChar.getTarget().getActingPlayer() : activeChar;
			if (target.isHero())
			{
				target.setHero(false);
				
				for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
				{
					final Item equippedItem = target.getInventory().getPaperdollItem(i);
					if ((equippedItem != null) && equippedItem.isHeroItem())
					{
						target.getInventory().unEquipItemInSlot(i);
					}
				}
				
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item item : target.getInventory().getAvailableItems(false, false, false))
				{
					if ((item != null) && item.isHeroItem())
					{
						target.destroyItem("Hero", item, null, true);
						iu.addRemovedItem(item);
					}
				}
				
				if (!iu.getItems().isEmpty())
				{
					target.sendInventoryUpdate(iu);
				}
				target.broadcastUserInfo();
				deleteHerotIems(target);
				Hero.HEROES.remove(target, iu);
			}
			else
			{
				activeChar.sendMessage("대상은 영웅캐릭터가 아닙니다.");
			}
		}
		else if (command.startsWith("admin_settruehero"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			
			final Player target = activeChar.getTarget().isPlayer() ? activeChar.getTarget().getActingPlayer() : activeChar;
			target.setTrueHero(!target.isTrueHero());
			target.broadcastUserInfo();
		}
		else if (command.startsWith("admin_givehero"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
				return false;
			}
			
			final Player target = activeChar.getTarget().isPlayer() ? activeChar.getTarget().getActingPlayer() : activeChar;
			if (Hero.getInstance().isHero(target.getObjectId()))
			{
				BuilderUtil.sendSysMessage(activeChar, "This player has already claimed the hero status.");
				return false;
			}
			
			if (!Hero.getInstance().isUnclaimedHero(target.getObjectId()))
			{
				BuilderUtil.sendSysMessage(activeChar, "This player cannot claim the hero status.");
				return false;
			}
			Hero.getInstance().claimHero(target);
		}
		else if (command.startsWith("admin_diet"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				if (st.nextToken().equalsIgnoreCase("on"))
				{
					activeChar.setDietMode(true);
					BuilderUtil.sendSysMessage(activeChar, "Diet mode on.");
				}
				else if (st.nextToken().equalsIgnoreCase("off"))
				{
					activeChar.setDietMode(false);
					BuilderUtil.sendSysMessage(activeChar, "Diet mode off.");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getDietMode())
				{
					activeChar.setDietMode(false);
					BuilderUtil.sendSysMessage(activeChar, "Diet mode off.");
				}
				else
				{
					activeChar.setDietMode(true);
					BuilderUtil.sendSysMessage(activeChar, "Diet mode on.");
				}
			}
			finally
			{
				activeChar.refreshOverloaded(true);
			}
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_tradeoff"))
		{
			try
			{
				final String mode = command.substring(15);
				if (mode.equalsIgnoreCase("on"))
				{
					activeChar.setTradeRefusal(true);
					BuilderUtil.sendSysMessage(activeChar, "Trade refusal enabled.");
				}
				else if (mode.equalsIgnoreCase("off"))
				{
					activeChar.setTradeRefusal(false);
					BuilderUtil.sendSysMessage(activeChar, "Trade refusal disabled.");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getTradeRefusal())
				{
					activeChar.setTradeRefusal(false);
					BuilderUtil.sendSysMessage(activeChar, "Trade refusal disabled.");
				}
				else
				{
					activeChar.setTradeRefusal(true);
					BuilderUtil.sendSysMessage(activeChar, "Trade refusal enabled.");
				}
			}
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_setconfig"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				final String pName = st.nextToken();
				final String pValue = st.nextToken();
				if (Float.valueOf(pValue) == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Invalid parameter!");
					return false;
				}
				switch (pName)
				{
					case "RateXp":
					{
						Config.RATE_XP = Float.parseFloat(pValue);
						break;
					}
					case "RateSp":
					{
						Config.RATE_SP = Float.parseFloat(pValue);
						break;
					}
					case "RateDropSpoil":
					{
						Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER = Float.parseFloat(pValue);
						break;
					}
				}
				BuilderUtil.sendSysMessage(activeChar, "Config parameter " + pName + " set to " + pValue);
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //setconfig <parameter> <value>");
			}
			finally
			{
				showConfigPage(activeChar);
			}
		}
		else if (command.startsWith("admin_worldchat"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // admin_worldchat
			switch (st.hasMoreTokens() ? st.nextToken() : "")
			{
				case "shout":
				{
					final StringBuilder sb = new StringBuilder();
					while (st.hasMoreTokens())
					{
						sb.append(st.nextToken());
						sb.append(" ");
					}
					
					final CreatureSay cs = new CreatureSay(activeChar, ChatType.WORLD, activeChar.getName(), sb.toString());
					for (Player player : World.getInstance().getPlayers())
					{
						if (player.isNotBlocked(activeChar))
						{
							player.sendPacket(cs);
						}
					}
					break;
				}
				case "see":
				{
					final WorldObject target = activeChar.getTarget();
					if ((target == null) || !target.isPlayer())
					{
						activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
						break;
					}
					final Player targetPlayer = target.getActingPlayer();
					if (targetPlayer.getLevel() < Config.WORLD_CHAT_MIN_LEVEL)
					{
						BuilderUtil.sendSysMessage(activeChar, "Your target's level is below the minimum: " + Config.WORLD_CHAT_MIN_LEVEL);
						break;
					}
					BuilderUtil.sendSysMessage(activeChar, targetPlayer.getName() + ": has used world chat " + targetPlayer.getWorldChatUsed() + " times out of maximum " + targetPlayer.getWorldChatPoints() + " times.");
					break;
				}
				case "set":
				{
					final WorldObject target = activeChar.getTarget();
					if ((target == null) || !target.isPlayer())
					{
						activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
						break;
					}
					
					final Player targetPlayer = target.getActingPlayer();
					if (targetPlayer.getLevel() < Config.WORLD_CHAT_MIN_LEVEL)
					{
						BuilderUtil.sendSysMessage(activeChar, "Your target's level is below the minimum: " + Config.WORLD_CHAT_MIN_LEVEL);
						break;
					}
					
					if (!st.hasMoreTokens())
					{
						BuilderUtil.sendSysMessage(activeChar, "Incorrect syntax, use: //worldchat set <times used>");
						break;
					}
					
					final String valueToken = st.nextToken();
					if (!Util.isDigit(valueToken))
					{
						BuilderUtil.sendSysMessage(activeChar, "Incorrect syntax, use: //worldchat set <times used>");
						break;
					}
					
					BuilderUtil.sendSysMessage(activeChar, targetPlayer.getName() + ": times used changed from " + targetPlayer.getWorldChatPoints() + " to " + valueToken);
					targetPlayer.setWorldChatUsed(Integer.parseInt(valueToken));
					if (Config.ENABLE_WORLD_CHAT)
					{
						targetPlayer.sendPacket(new ExWorldChatCnt(targetPlayer));
					}
					break;
				}
				default:
				{
					BuilderUtil.sendSysMessage(activeChar, "Possible commands:");
					BuilderUtil.sendSysMessage(activeChar, " - Send message: //worldchat shout <text>");
					BuilderUtil.sendSysMessage(activeChar, " - See your target's points: //worldchat see");
					BuilderUtil.sendSysMessage(activeChar, " - Change your target's points: //worldchat set <points>");
					break;
				}
			}
		}
		else if (command.startsWith("admin_gmon"))
		{
			// TODO why is this empty?
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(Player activeChar, String command)
	{
		int mode = 0;
		String filename = null;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e)
		{
			// Not important.
		}
		switch (mode)
		{
			case 1:
			{
				filename = "main";
				break;
			}
			case 2:
			{
				filename = "game";
				break;
			}
			case 3:
			{
				filename = "effects";
				break;
			}
			case 4:
			{
				filename = "server";
				break;
			}
			case 5:
			{
				filename = "mods";
				break;
			}
			case 6:
			{
				filename = "char";
				break;
			}
			case 7:
			{
				filename = "gm";
				break;
			}
			default:
			{
				filename = "main";
				break;
			}
		}
		AdminHtml.showAdminHtml(activeChar, filename + "_menu.htm");
	}
	
	private void deleteHerotIems(Player player)
	{
		for (int itemId : HERO_ITEMS)
		{
			if (player != null)
			{
				final Item item = player.getInventory().getItemByItemId(itemId);
				if (item != null)
				{
					player.destroyItemByItemId("영웅아이템삭제", itemId, item.getCount(), player, true);
				}
			}
		}
	}
	
	private void showConfigPage(Player activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		final StringBuilder replyMSG = new StringBuilder("<html><title>L2J :: Config</title><body>");
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Main\" action=\"bypass -h admin_admin\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=150>Config Server Panel</td><td width=60><button value=\"Back\" action=\"bypass -h admin_admin4\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260><tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("<tr><td><font color=\"00AA00\">Drop:</font></td><td></td><td></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate EXP</font> = " + Config.RATE_XP + "</td><td><edit var=\"param1\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateXp $param1\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate SP</font> = " + Config.RATE_SP + "</td><td><edit var=\"param2\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateSp $param2\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Spoil</font> = " + Config.RATE_SPOIL_DROP_CHANCE_MULTIPLIER + "</td><td><edit var=\"param4\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateDropSpoil $param4\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("</table></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}
