package handlers.voicedcommandhandlers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.html.PageBuilder;
import org.l2jmobius.gameserver.model.html.PageResult;
import org.l2jmobius.gameserver.model.item.LunaShopItemInfo;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ShowCalculator;
import org.l2jmobius.gameserver.network.serverpackets.ShowCouponUI;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.taskmanager.AttackStanceTaskManager;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.BuilderUtil;

import ai.AbstractNpcAI;

public class Command extends AbstractNpcAI implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"계산기",
		"텔레포트",
		"세븐사인70",
		"gototele",
		"missiontele",
		"harbor",
		"closeHtml",
		"WeaponC",
		"ArmorC",
		"WeaponB",
		"ArmorB",
		"NEWBIE_SUPPORT",
		"BackTo",
		"ToForgotten",
		"접속유저보기",
		"자동사냥아님",
		"테스트열기",
		"루나상점초기화",
		"gototores",
		"쿠폰사용"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		int account = 0;
		if (command.equals("ToForgotten"))
		{
			if (activeChar.getInventory().getInventoryItemCount(57, -1) < 10000)
			{
				activeChar.sendMessage("아데나가 부족합니다.");
			}
			else
			{
				activeChar.destroyItemByItemId("CB_Teleport", 57, 10000, activeChar, true);
				activeChar.setInstanceById(0);
				activeChar.getVariables().set("FORGOTTEN_RETURN", activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ());
				activeChar.teleToLocation(11320 + getRandom(200), -24064 + getRandom(200), -3648, 0); // 잊혀진 섬
				activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2036, 1, 3, 1));
			}
		}
		if (command.equals("BackTo"))
		{
			String var = activeChar.getVariables().getString("FORGOTTEN_RETURN", "");
			if (var.isEmpty())
			{
				activeChar.sendMessage("이전 위치의 좌표가 없어서 기란성 마을로 이동합니다.");
				activeChar.teleToLocation(83400 + getRandom(100), 147995 + getRandom(100), -3400, 0); // 기란
				activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2036, 1, 3, 1));
			}
			else
			{
				final String[] loc = var.split(" ");
				int x = Integer.parseInt(loc[0]);
				int y = Integer.parseInt(loc[1]);
				int z = Integer.parseInt(loc[2]);
				
				activeChar.teleToLocation(x, y, z);
				activeChar.getVariables().remove("FORGOTTEN_RETURN");
				activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2036, 1, 3, 1));
			}
		}
		if (command.equals("테스트열기"))
		{
			activeChar.sendPacket(new TutorialShowHtml(0, "..\\L2text_classic\\skill_enchant_guide.htm", 2));
		}
		if (command.equals("NEWBIE_SUPPORT"))
		{
			account = activeChar.getAccountVariables().getInt("NEWBIE_SUPPORT", 0);
			if (BorinetUtil.getInstance().checkDB(activeChar, "NEWBIE_SUPPORT") && (account != 1))
			{
				if ((activeChar.getInventory().getInventoryItemCount(41000, -1) < 100))
				{
					activeChar.sendMessage("루나가 부족합니다. 커뮤니티보드(Alt+B) 루나상점에서 루나구매가 가능합니다.");
					return true;
				}
				activeChar.destroyItemByItemId("NEWBIE_TICKETC", 41000, 100, activeChar, true);
				activeChar.addItem("NEWBIE_TICKETC", 41017, 1, activeChar, true);
				activeChar.addItem("NEWBIE_TICKETC", 41018, 1, activeChar, true);
				BorinetUtil.getInstance().insertDB(activeChar, "NEWBIE_SUPPORT");
			}
			else
			{
				activeChar.sendMessage("계정 및 해당 PC에서 이미 지급받았습니다. 한번만 구매가 가능합니다.");
			}
		}
		if (command.equals("자동사냥아님"))
		{
			activeChar.deleteQuickVar("MacroCheck");
			activeChar.setInvul(false);
			activeChar.setBlockActions(false);
		}
		if (command.equals("WeaponC"))
		{
			account = activeChar.getAccountVariables().getInt("WeaponC", 0);
			if (BorinetUtil.getInstance().checkDB(activeChar, "WeaponC") && (account != 1))
			{
				if ((activeChar.getInventory().getInventoryItemCount(41003, -1) < 15))
				{
					activeChar.sendMessage("교환에 필요한 홍보 코인이 부족합니다.");
					return true;
				}
				activeChar.destroyItemByItemId("무기교환권사용C", 41003, 15, activeChar, true);
				activeChar.addItem("무기교환권사용C", 41019, 1, activeChar, true);
				BorinetUtil.getInstance().insertDB(activeChar, "WeaponC");
			}
			else
			{
				activeChar.sendMessage("계정 및 해당 PC에서 이미 지급받았습니다.");
			}
		}
		if (command.equals("ArmorC"))
		{
			account = activeChar.getAccountVariables().getInt("ArmorC", 0);
			if (BorinetUtil.getInstance().checkDB(activeChar, "ArmorC") && (account != 1))
			{
				if ((activeChar.getInventory().getInventoryItemCount(41003, -1) < 15))
				{
					activeChar.sendMessage("교환에 필요한 홍보 코인이 부족합니다.");
					return true;
				}
				activeChar.destroyItemByItemId("방어구교환권사용C", 41003, 15, activeChar, true);
				activeChar.addItem("방어구교환권사용C", 41020, 1, activeChar, true);
				BorinetUtil.getInstance().insertDB(activeChar, "ArmorC");
			}
			else
			{
				activeChar.sendMessage("계정 및 해당 PC에서 이미 지급받았습니다.");
			}
		}
		if (command.equals("WeaponB"))
		{
			account = activeChar.getAccountVariables().getInt("ArmorB", 0);
			if (BorinetUtil.getInstance().checkDB(activeChar, "ArmorB") && (account != 1))
			{
				if ((activeChar.getInventory().getInventoryItemCount(41003, -1) < 20))
				{
					activeChar.sendMessage("교환에 필요한 홍보 코인이 부족합니다.");
					return true;
				}
				activeChar.destroyItemByItemId("무기교환권사용B", 41003, 20, activeChar, true);
				activeChar.addItem("무기교환권사용B", 41021, 1, activeChar, true);
				BorinetUtil.getInstance().insertDB(activeChar, "WeaponB");
			}
			else
			{
				activeChar.sendMessage("계정 및 해당 PC에서 이미 지급받았습니다.");
			}
		}
		if (command.equals("ArmorB"))
		{
			account = activeChar.getAccountVariables().getInt("ArmorB", 0);
			if (BorinetUtil.getInstance().checkDB(activeChar, "ArmorB") && (account != 1))
			{
				if ((activeChar.getInventory().getInventoryItemCount(41003, -1) < 20))
				{
					activeChar.sendMessage("교환에 필요한 홍보 코인이 부족합니다.");
					return true;
				}
				activeChar.destroyItemByItemId("방어구교환권사용B", 41003, 20, activeChar, true);
				activeChar.addItem("방어구교환권사용B", 41022, 1, activeChar, true);
				BorinetUtil.getInstance().insertDB(activeChar, "ArmorB");
			}
			else
			{
				activeChar.sendMessage("계정 및 해당 PC에서 이미 지급받았습니다.");
			}
		}
		else if (command.startsWith("gototele"))
		{
			if (!BorinetUtil.getInstance().canTeleport(activeChar))
			{
				return false;
			}
			final String teleBuypass = target.replace("gototele", " ");
			final String[] loc = teleBuypass.split(" ");
			int x = Integer.parseInt(loc[0]);
			int y = Integer.parseInt(loc[1]);
			int z = Integer.parseInt(loc[2]);
			if (!checkBook(activeChar))
			{
				if (activeChar.getLevel() > 52)
				{
					if (activeChar.getInventory().getInventoryItemCount(57, -1) < 10000)
					{
						activeChar.sendMessage("아데나가 부족합니다.");
					}
					else
					{
						activeChar.destroyItemByItemId("CB_Teleport", 57, 10000, activeChar, true);
						activeChar.setInstanceById(0);
						activeChar.teleToLocation(x, y, z, 0);
						activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2036, 1, 3, 1));
					}
				}
				else
				{
					activeChar.setInstanceById(0);
					activeChar.teleToLocation(x, y, z, 0);
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2036, 1, 3, 1));
				}
			}
		}
		else if (command.startsWith("gototores"))
		{
			activeChar.setInstanceById(0);
			activeChar.teleToLocation(146961, 26800, -2193, 0);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2036, 1, 3, 1));
		}
		else if (command.startsWith("missiontele"))
		{
			final String teleBuypass = target.replace("missiontele", " ");
			final String[] loc = teleBuypass.split(" ");
			int x = Integer.parseInt(loc[0]);
			int y = Integer.parseInt(loc[1]);
			int z = Integer.parseInt(loc[2]);
			
			activeChar.setInstanceById(0);
			activeChar.teleToLocation(x, y, z, 0);
			activeChar.sendPacket(TutorialCloseHtml.STATIC_PACKET);
		}
		else if (command.startsWith("harbor"))
		{
			final String teleBuypass = target.replace("harbor", " ");
			final String[] loc = teleBuypass.split(" ");
			int ticket = Integer.parseInt(loc[0]);
			int x = Integer.parseInt(loc[1]);
			int y = Integer.parseInt(loc[2]);
			int z = Integer.parseInt(loc[3]);
			
			activeChar.getVariables().set("선착장이동", 1);
			activeChar.addItem("정기선 배표", ticket, 1, activeChar, true);
			activeChar.setInstanceById(0);
			activeChar.teleToLocation(x, y, z, 0);
			activeChar.sendPacket(TutorialCloseHtml.STATIC_PACKET);
		}
		else if (command.startsWith("closeHtml"))
		{
			activeChar.sendPacket(TutorialCloseHtml.STATIC_PACKET);
		}
		else if (command.equals("계산기"))
		{
			activeChar.sendPacket(new ShowCalculator(4393));
		}
		else if (command.equals("세븐사인70"))
		{
			String html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/Seven70.htm");
			activeChar.sendPacket(new NpcHtmlMessage(html));
		}
		else if (command.equals("텔레포트"))
		{
			if (!checkBook(activeChar))
			{
				if (Config.CAN_TELEPORT_LEVEL <= activeChar.getLevel())
				{
					String html = "";
					if (activeChar.getLevel() < 10)
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/first.htm");
					}
					else if ((activeChar.getLevel() >= 10) && (activeChar.getLevel() < 18))
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/1st.htm");
					}
					else if ((activeChar.getLevel() >= 18) && (activeChar.getLevel() < 28))
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/2st.htm");
					}
					else if ((activeChar.getLevel() >= 28) && (activeChar.getLevel() < 38))
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/3st.htm");
					}
					else if ((activeChar.getLevel() >= 38) && (activeChar.getLevel() < 48))
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/4st.htm");
					}
					else if ((activeChar.getLevel() >= 48) && (activeChar.getLevel() < 58))
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/5st.htm");
					}
					else if ((activeChar.getLevel() >= 58) && (activeChar.getLevel() < 68))
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/6st.htm");
					}
					else if ((activeChar.getLevel() >= 68) && (activeChar.getLevel() < 76))
					{
						html = HtmCache.getInstance().getHtm(null, "data/html/guide/teleport/7st.htm");
					}
					else if ((activeChar.getLevel() >= 76) && (activeChar.getLevel() < 80))
					{
						html = HtmCache.getInstance().getHtm(activeChar, "data/html/guide/teleport/8st.htm");
					}
					else if (activeChar.getLevel() >= 80)
					{
						html = HtmCache.getInstance().getHtm(activeChar, "data/html/guide/teleport/last.htm");
					}
					html = html.replace("%playername%", activeChar.getName());
					html = html.replace("%level%", String.valueOf(activeChar.getLevel()));
					activeChar.sendPacket(new NpcHtmlMessage(html));
				}
			}
		}
		else if (command.startsWith("접속유저보기"))
		{
			try
			{
				final String showOnline = target.replace("접속유저보기", " ");
				final String[] players = showOnline.split(" ");
				final int page = Integer.parseInt(players[0]);
				listCharacters(activeChar, page);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty page number
				BuilderUtil.sendSysMessage(activeChar, "Usage: //show_characters <page_number>");
			}
		}
		else if (command.equals("루나상점초기화"))
		{
			LunaShopItemInfo.removeLunaVariables(activeChar);
			LunaShopItemInfo.removeItemVariables(activeChar);
			BorinetHtml.getInstance().showShopHtml(activeChar, "_Luna", "", "", "");
		}
		else if (command.equals("쿠폰사용"))
		{
			activeChar.sendPacket(ShowCouponUI.STATIC_PACKET);
		}
		else
		{
			return false;
		}
		return true;
	}
	
	private void listCharacters(Player activeChar, int page)
	{
		final List<Player> players = new ArrayList<>(World.getInstance().getPlayers());
		players.sort(Comparator.comparingLong(Player::getUptime));
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
		html.setFile(activeChar, "data/html/guide/charlist.htm");
		
		final PageResult result = PageBuilder.newBuilder(players, 15, "bypass -h voice .접속유저보기").currentPage(page).bodyHandler((pages, player, sb) ->
		{
			if (!player.isGM())
			{
				sb.append("<tr>");
				sb.append("<td width=100><font color=00A5FF>" + ((player.isInOfflineMode() ? ("<font color=\"808080\">" + player.getName() + "</font>") : player.getName()) + "</font></td>"));
				sb.append("<td width=100>" + ((player.isInOfflineMode() ? ("<font color=\"808080\">" + ClassListData.getInstance().getClass(player.getClassId()).getClientCode() + "</font>") : ClassListData.getInstance().getClass(player.getClassId()).getClientCode()) + "</td><td width=40>" + ((player.isInOfflineMode() ? ("<font color=\"808080\">" + player.getLevel() + "</font>") : player.getLevel()) + "</td>")));
				sb.append("</tr>");
			}
		}).build();
		
		int previous = page - 1;
		int next = page + 1;
		int online = 0;
		int offline = 0;
		for (Player onlines : World.getInstance().getPlayers())
		{
			if (onlines.isInOfflineMode())
			{
				offline++;
			}
			else if (onlines.isOnline())
			{
				if (!onlines.isGM())
				{
					online++;
				}
			}
		}
		
		html.replace("%previous%", page > 0 ? "<button value=\"이전\" action=\"bypass -h voice .접속유저보기 " + previous + " \" width=\"120\" height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">" : "");
		html.replace("%next%", ((result.getPages() > 0) && (next < result.getPages())) ? "<button value=\"다음\" action=\"bypass -h voice .접속유저보기 " + next + " \" width=\"120\" height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">" : "");
		html.replace("%players%", result.getBodyTemplate().toString());
		html.replace("%online%", online);
		html.replace("%offline%", offline);
		activeChar.sendPacket(html);
	}
	
	public static boolean checkBook(Player player)
	{
		if (player.isInSiege() || player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendMessage("공성 중에는 사용할 수 없습니다.");
			return true;
		}
		if (player.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			player.sendMessage("전투 중에는 사용할 수 없습니다.");
			return true;
		}
		if (player.isInOlympiadMode() || player.isInCombat())
		{
			player.sendMessage("올림피아드 게임 중에는 사용할 수 없습니다.");
			return true;
		}
		if (player.isInInstance())
		{
			player.sendMessage("인스턴트 던전 이용 중에는 사용할 수 없습니다.");
			return true;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage("저주받은 무기를 소유한 상태에서는 사용할 수 없습니다.");
			return true;
		}
		boolean isCaptchaActive = player.getQuickVarB("IsCaptchaActive", false);
		if (isCaptchaActive)
		{
			player.sendMessage("보안문자 입력 중 에는 사용할 수 없습니다.");
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}