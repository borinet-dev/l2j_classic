package handlers.voicedcommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.Util;

public class ExpandInventory implements IVoicedCommandHandler
{
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.EXPAND_INVENTORY_PRICE));
	
	private static final String[] VOICED_COMMANDS =
	{
		"인벤토리",
		"확장하기"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("확장하기"))
		{
			if (!check(activeChar))
			{
				LunaManager.getInstance().useLunaPoint(activeChar, itemCount, "인벤토리 확장");
				
				activeChar.setExpandInventory(activeChar.getExpandInventory() + Config.EXPAND_INVENTORY_SLOT);
				activeChar.getVariables().set("인벤토리확장", String.valueOf(activeChar.getExpandInventory()));
				activeChar.sendMessage("인벤토리가 " + activeChar.getInventoryLimit() + " 슬롯으로 확장되었습니다.");
				activeChar.sendPacket(new ExStorageMaxCount(activeChar));
			}
			BorinetHtml.showHtml(activeChar, "LunaShop/ExpandInventory.htm", 0, "");
		}
		else if (command.equals("인벤토리"))
		{
			BorinetHtml.showHtml(activeChar, "LunaShop/ExpandInventory.htm", 0, "");
		}
		
		return false;
		
	}
	
	public boolean check(Player player)
	{
		if (player.getLuna() < itemCount)
		{
			player.sendMessage(itemName + "가 부족합니다.");
			return true;
		}
		if ((player.getOriginRace() == Race.DWARF) && (player.getInventoryLimit() >= Config.EXPAND_INVENTORY_DWARF_MAX))
		{
			player.sendMessage("더이상 인벤토리 슬롯을 확장 할 수 없습니다.");
			return true;
		}
		if ((player.getOriginRace() != Race.DWARF) && (player.getInventoryLimit() >= Config.EXPAND_INVENTORY_MAX))
		{
			player.sendMessage("더이상 인벤토리 슬롯을 확장 할 수 없습니다.");
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