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

import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExAdenaInvenCount;
import org.l2jmobius.gameserver.network.serverpackets.GMViewItemList;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items with respective id, if num is not specified, assumes 1.
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_itemcreate",
		"admin_create_item",
		"admin_create_coin",
		"admin_give_item_target",
		"admin_give_item_to_all",
		"admin_delete_item",
		"admin_use_item"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_itemcreate"))
		{
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_item"))
		{
			try
			{
				final String val = command.substring(17);
				final StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					final String num = st.nextToken();
					final long numval = Long.parseLong(num);
					createItem(activeChar, activeChar, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					createItem(activeChar, activeChar, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //create_item <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				BuilderUtil.sendSysMessage(activeChar, "유효한 숫자를 지정하십시오.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_coin"))
		{
			try
			{
				final String val = command.substring(17);
				final StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					final String name = st.nextToken();
					final int idval = getCoinId(name);
					if (idval > 0)
					{
						final String num = st.nextToken();
						final long numval = Long.parseLong(num);
						createItem(activeChar, activeChar, idval, numval);
					}
				}
				else if (st.countTokens() == 1)
				{
					final String name = st.nextToken();
					final int idval = getCoinId(name);
					createItem(activeChar, activeChar, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //create_coin <name> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				BuilderUtil.sendSysMessage(activeChar, "유효한 숫자를 지정하십시오.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_give_item_target"))
		{
			try
			{
				final WorldObject target = activeChar.getTarget();
				if ((target == null) || !target.isPlayer())
				{
					BuilderUtil.sendSysMessage(activeChar, "잘못된 대상입니다.");
					return false;
				}
				
				final String val = command.substring(22);
				final StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					final String num = st.nextToken();
					final long numval = Long.parseLong(num);
					createItem(activeChar, (Player) target, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					createItem(activeChar, (Player) target, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //give_item_target <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				BuilderUtil.sendSysMessage(activeChar, "유효한 숫자를 지정하십시오.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_give_item_to_all"))
		{
			final String val = command.substring(22);
			final StringTokenizer st = new StringTokenizer(val);
			int idval = 0;
			long numval = 0;
			if (st.countTokens() == 2)
			{
				final String id = st.nextToken();
				idval = Integer.parseInt(id);
				final String num = st.nextToken();
				numval = Long.parseLong(num);
			}
			else if (st.countTokens() == 1)
			{
				final String id = st.nextToken();
				idval = Integer.parseInt(id);
				numval = 1;
			}
			int counter = 0;
			final ItemTemplate template = ItemTable.getInstance().getTemplate(idval);
			if (template == null)
			{
				BuilderUtil.sendSysMessage(activeChar, "아이템이 존재하지 않습니다.");
				return false;
			}
			if ((numval > 10) && !template.isStackable())
			{
				BuilderUtil.sendSysMessage(activeChar, "겹쳐지는 아이템이 아닙니다. 다시 시도하세요.");
				return false;
			}
			for (Player onlinePlayer : World.getInstance().getPlayers())
			{
				if ((activeChar != onlinePlayer) && onlinePlayer.isOnline() && ((onlinePlayer.getClient() != null) && !onlinePlayer.getClient().isDetached()))
				{
					onlinePlayer.getInventory().addItem("운영자가 지급", idval, numval, onlinePlayer, activeChar);
					if (numval > 1)
					{
						onlinePlayer.sendMessage("운영자가 아이템 [" + template.getName() + "] " + numval + "개를 지급하였습니다.");
						onlinePlayer.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "운영자가 [" + template.getName() + "] " + numval + "개를 지급하였습니다."));
					}
					else
					{
						onlinePlayer.sendMessage("운영자가 아이템 [" + template.getName() + "] 를 지급하였습니다.");
						onlinePlayer.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "운영자가 [" + template.getName() + "] 를 지급하였습니다."));
					}
					counter++;
				}
			}
			if (numval > 1)
			{
				activeChar.sendMessage(counter + "명의 유저에게 아이템 [" + template.getName() + "] " + numval + "개를 지급.");
			}
			else
			{
				activeChar.sendMessage(counter + "명의 유저에게 아이템 [" + template.getName() + "] 를 지급.");
			}
		}
		else if (command.startsWith("admin_delete_item"))
		{
			final String val = command.substring(18);
			final StringTokenizer st = new StringTokenizer(val);
			int idval = 0;
			long numval = 0;
			if (st.countTokens() == 2)
			{
				final String id = st.nextToken();
				idval = Integer.parseInt(id);
				final String num = st.nextToken();
				numval = Long.parseLong(num);
			}
			else if (st.countTokens() == 1)
			{
				final String id = st.nextToken();
				idval = Integer.parseInt(id);
				numval = 1;
			}
			final Item item = (Item) World.getInstance().findObject(idval);
			final int ownerId = item.getOwnerId();
			if (ownerId > 0)
			{
				final Player player = World.getInstance().getPlayer(ownerId);
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "대상이 접속상태가 아닙니다.");
					return false;
				}
				
				if (numval == 0)
				{
					numval = item.getCount();
				}
				
				player.getInventory().destroyItem("AdminDelete", idval, numval, activeChar, null);
				activeChar.sendPacket(new GMViewItemList(1, player));
				BuilderUtil.sendSysMessage(activeChar, "Item deleted.");
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Item doesn't have owner.");
				return false;
			}
		}
		else if (command.startsWith("admin_use_item"))
		{
			final String val = command.substring(15);
			final int idval = Integer.parseInt(val);
			final Item item = (Item) World.getInstance().findObject(idval);
			final int ownerId = item.getOwnerId();
			if (ownerId > 0)
			{
				final Player player = World.getInstance().getPlayer(ownerId);
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "대상이 접속상태가 아닙니다.");
					return false;
				}
				
				// equip
				if (item.isEquipable())
				{
					player.useEquippableItem(item, false);
				}
				else
				{
					final IItemHandler ih = ItemHandler.getInstance().getHandler(item.getEtcItem());
					if (ih != null)
					{
						ih.useItem(player, item, false);
					}
				}
				activeChar.sendPacket(new GMViewItemList(1, player));
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Item doesn't have owner.");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void createItem(Player activeChar, Player target, int id, long num)
	{
		final ItemTemplate template = ItemTable.getInstance().getTemplate(id);
		if (template == null)
		{
			BuilderUtil.sendSysMessage(activeChar, "아이템이 존재하지 않습니다.");
			return;
		}
		if ((num > 10) && !template.isStackable())
		{
			BuilderUtil.sendSysMessage(activeChar, "겹쳐지는 아이템이 아닙니다. 다시 시도하세요.");
			return;
		}
		
		target.getInventory().addItem("운영자가 지급", id, num, activeChar, null);
		BuilderUtil.sendSysMessage(activeChar, "아이템 생성: " + num + " " + template.getName() + "(" + id + ") -> " + target.getName());
		target.sendPacket(new ExAdenaInvenCount(target));
		if (!target.isGM())
		{
			target.sendMessage("운영자가 아이템 [" + template.getName() + "] " + num + "개를 지급했습니다.");
			target.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "운영자가 [" + template.getName() + "] " + num + "개를 지급하였습니다."));
		}
		target.sendItemList();
		target.sendPacket(new ExAdenaInvenCount(target));
	}
	
	private int getCoinId(String name)
	{
		int id;
		if (name.equalsIgnoreCase("아데나"))
		{
			id = 57;
		}
		else if (name.equalsIgnoreCase("루나"))
		{
			id = 41000;
		}
		else if (name.equalsIgnoreCase("혈맹이전코인"))
		{
			id = 41011;
		}
		else if (name.equalsIgnoreCase("홍보코인"))
		{
			id = 41003;
		}
		else if (name.equalsIgnoreCase("접속코인"))
		{
			id = 41001;
		}
		else
		{
			id = 0;
		}
		return id;
	}
}
