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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.LunaShopItemInfo;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.util.Util;

public class LunaManager
{
	private final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	
	public void playVoice(Player player, String voice)
	{
		player.sendPacket(new PlaySound(2, voice, 0, 0, player.getX(), player.getY(), player.getZ()));
	}
	
	public void giveLunaItem(Player player, int point, String itemIds)
	{
		if (point > 1)
		{
			playVoice(player, "borinet/LunaDelivery");
			String luna = Util.formatAdena(point);
			player.getInventory().addItem(luna + " " + itemName + " 구매", Config.LUNA, point, player, null);
			final InventoryUpdate playerIU = new InventoryUpdate();
			player.sendInventoryUpdate(playerIU);
			player.sendMessage("구매하신 " + luna + " " + itemName + "가 적립되었습니다!");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "구매하신 " + luna + " " + itemName + "가 적립되었습니다!"));
			LunaShopItemInfo.removeLunaVariables(player);
		}
		else
		{
			sendGiftMail(player, itemIds);
		}
	}
	
	public void useLunaPoint(Player player, int point, String string)
	{
		String luna = Util.formatAdena(point);
		player.destroyItemByItemId(luna + " " + itemName + " 사용: " + string, Config.LUNA, point, player, false);
		player.sendMessage(luna + " " + itemName + "를 사용하였습니다.");
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, luna + " " + itemName + "를 사용하였습니다."));
	}
	
	public static LunaManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LunaManager INSTANCE = new LunaManager();
	}
	
	private void sendGiftMail(Player player, String itemIds)
	{
		int msgId = IdManager.getInstance().getNextId();
		Message msg = new Message(msgId, player.getObjectId(), "구매하신 아이템이 도착했습니다!", "\n" + Config.SERVER_NAME_KOR + "과 함께 즐거운시간 되시기 바랍니다.", 7, MailType.PRIME_SHOP_GIFT, false);
		final List<ItemHolder> itemHolders = parseItemIds(itemIds);
		if (!itemHolders.isEmpty())
		{
			Mail attachments = msg.createAttachments();
			for (ItemHolder itemHolder : itemHolders)
			{
				attachments.addItem("후원 구매", itemHolder.getId(), itemHolder.getCount(), null, null);
			}
		}
		MailManager.getInstance().sendMessage(msg);
	}
	
	private List<ItemHolder> parseItemIds(String itemIds)
	{
		List<ItemHolder> itemHolders = new ArrayList<>();
		for (String str : itemIds.split(";"))
		{
			if (str.contains(","))
			{
				String[] parts = str.split(",");
				if ((parts.length == 2) && Util.isDigit(parts[0]) && Util.isDigit(parts[1]))
				{
					itemHolders.add(new ItemHolder(Integer.parseInt(parts[0]), Long.parseLong(parts[1])));
				}
			}
			else if (Util.isDigit(str))
			{
				itemHolders.add(new ItemHolder(Integer.parseInt(str), 1));
			}
		}
		return itemHolders;
	}
}
