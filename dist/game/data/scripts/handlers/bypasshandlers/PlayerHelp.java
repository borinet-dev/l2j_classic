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
package handlers.bypasshandlers;

import java.util.StringTokenizer;
import java.util.logging.Level;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.util.EnchantRecord;

public class PlayerHelp implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"player_help",
		"guide",
		"globalteleport",
		"newbie_help",
		"enlist_item",
		"recovery_item",
		"recovery_blessed_item"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		try
		{
			if (command.startsWith("player_help"))
			{
				if (command.length() < 13)
				{
					return false;
				}
				
				final String path = command.substring(12);
				if (path.contains(".."))
				{
					return false;
				}
				
				final StringTokenizer st = new StringTokenizer(path);
				final String[] cmd = st.nextToken().split("#");
				final NpcHtmlMessage html;
				if (cmd.length > 1)
				{
					final int itemId = Integer.parseInt(cmd[1]);
					html = new NpcHtmlMessage(0, itemId);
				}
				else
				{
					html = new NpcHtmlMessage();
				}
				
				html.setFile(player, "data/html/help/" + cmd[0]);
				player.sendPacket(html);
			}
			else if (command.equals("enlist_item"))
			{
				EnchantRecord.getInstance().showEnchantRecords(player);
			}
			else if (command.startsWith("recovery_blessed_item"))
			{
				if (player.getInventory().getInventoryItemCount(41365, 0) >= 1)
				{
					String[] tokens = command.split(" ");
					if (tokens.length >= 1)
					{
						int itemEnchantLvl = Integer.parseInt(tokens[2]);
						int itemObjId = Integer.parseInt(tokens[3]);
						final Item recoveryItem = player.getInventory().getItemByObjectId(itemObjId);
						final Item recoveryItem_ware = player.getWarehouse().getItemByObjectId(itemObjId);
						
						if (!EnchantRecord.getInstance().checkRecovery(player, itemObjId))
						{
							player.sendMessage("+" + recoveryItem.getEnchantLevel() + " " + recoveryItem.getName() + "의 기록을 찾을 수 없습니다.");
							return false;
						}
						if (recoveryItem != null)
						{
							recoveryItem.setEnchantLevel(itemEnchantLvl);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(recoveryItem);
							player.sendInventoryUpdate(iu);
							player.broadcastUserInfo();
							
							player.destroyItemByItemId("블랙_쿠폰", 41365, 1, player, true);
							EnchantRecord.getInstance().deleteItemRecovery(player, itemObjId);
							player.sendMessage(recoveryItem.getName() + "의 강화수치를 복원하였습니다.");
						}
						else if (recoveryItem_ware != null)
						{
							recoveryItem_ware.setEnchantLevel(itemEnchantLvl);
							
							player.destroyItemByItemId("블랙_쿠폰", 41365, 1, player, true);
							EnchantRecord.getInstance().deleteItemRecovery(player, itemObjId);
							player.sendMessage(recoveryItem_ware.getName() + "의 강화수치를 복원하였습니다.");
						}
						else
						{
							player.sendMessage("강화수치 복원을 하기위한 아이템이 인벤토리 및 창고에 존재하지 않습니다.");
							player.sendMessage("복원 리스트에서 제거합니다.");
							EnchantRecord.getInstance().deleteItemRecovery(player, itemObjId);
							return false;
						}
						
					}
				}
				else
				{
					player.sendMessage("복원에 필요한 아이템이 없습니다.");
				}
			}
			else if (command.startsWith("recovery_item"))
			{
				if (player.getInventory().getInventoryItemCount(41365, 0) >= 1)
				{
					String[] tokens = command.split(" ");
					if (tokens.length >= 1)
					{
						int itemId = Integer.parseInt(tokens[1]);
						int itemEnchantLvl = Integer.parseInt(tokens[2]);
						int itemObjId = Integer.parseInt(tokens[3]);
						final Item recoveryItem = ItemTemplate.createItem(itemId);
						
						if (!EnchantRecord.getInstance().checkRecovery(player, itemObjId))
						{
							player.sendMessage("+" + recoveryItem.getEnchantLevel() + " " + recoveryItem.getName() + "의 기록을 찾을 수 없습니다.");
							return false;
						}
						recoveryItem.setEnchantLevel(itemEnchantLvl);
						player.addItem("블랙_복구", recoveryItem, null, true);
						player.destroyItemByItemId("블랙_쿠폰", 41365, 1, player, true);
						EnchantRecord.getInstance().deleteItemRecovery(player, itemObjId);
						player.sendMessage("+" + recoveryItem.getEnchantLevel() + " " + recoveryItem.getName() + "의 복구를 완료하였습니다.");
					}
				}
				else
				{
					player.sendMessage("복구에 필요한 아이템이 없습니다.");
				}
			}
			else if (command.startsWith("guide"))
			{
				String[] param = command.split("guide ");
				String html = HtmCache.getInstance().getHtm(null, "data/html/guide/" + param[1] + ".htm");
				html = html.replace("<?isBuffOn?>", Config.SELLBUFF_ENABLED ? "<button action=\"bypass -h voice .버프판매\" value=\"버프 판매하기\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">" : "");
				player.sendPacket(new NpcHtmlMessage(html));
			}
			else if (command.startsWith("newbie_help"))
			{
				player.sendPacket(new TutorialShowHtml(0, "..\\L2text_classic\\help.htm", 2));
			}
			else if (command.startsWith("globalteleport"))
			{
				final NpcHtmlMessage html;
				String[] param = command.split("globalteleport ");
				html = new NpcHtmlMessage();
				html.setFile(player, "data/html/GlobalTeleport/" + param[1] + ".htm");
				player.sendPacket(html);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception in " + getClass().getSimpleName(), e);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
