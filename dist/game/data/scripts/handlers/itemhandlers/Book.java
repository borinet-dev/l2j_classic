package handlers.itemhandlers;

import java.util.Random;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

import handlers.voicedcommandhandlers.Command;

public class Book implements IItemHandler
{
	private static final ItemHolder[] 황금돼지_이벤트 =
	{
		new ItemHolder(41233, 1),
		new ItemHolder(41234, 2),
		new ItemHolder(41276, 1),
	};
	
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = (Player) playable;
		String filename = "";
		final int itemId = item.getId();
		switch (itemId)
		{
			case 8153:
				if (!Command.checkBook(player))
				{
					filename = "data/html/guide/index.htm";
					break;
				}
				break;
			case 41004:
				MultisellData.getInstance().separateAndSend(3247831, player, null, false);
				filename = null;
				break;
			case 41005:
				MultisellData.getInstance().separateAndSend(3247833, player, null, false);
				filename = null;
				break;
			case 41006:
				MultisellData.getInstance().separateAndSend(3247835, player, null, false);
				filename = null;
				break;
			case 41007:
				MultisellData.getInstance().separateAndSend(3247832, player, null, false);
				filename = null;
				break;
			case 41008:
				MultisellData.getInstance().separateAndSend(3247834, player, null, false);
				filename = null;
				break;
			case 41009:
				MultisellData.getInstance().separateAndSend(3247836, player, null, false);
				filename = null;
				break;
			case 41011:
				filename = "data/html/borinet/clanArmor.htm";
				break;
			case 41017:
				MultisellData.getInstance().separateAndSend(3247820, player, null, false);
				filename = null;
				break;
			case 41018:
				MultisellData.getInstance().separateAndSend(3247821, player, null, false);
				filename = null;
				break;
			case 41019:
				MultisellData.getInstance().separateAndSend(3247822, player, null, false);
				filename = null;
				break;
			case 41020:
				MultisellData.getInstance().separateAndSend(3247823, player, null, false);
				filename = null;
				break;
			case 41021:
				MultisellData.getInstance().separateAndSend(3247824, player, null, false);
				filename = null;
				break;
			case 41022:
				MultisellData.getInstance().separateAndSend(3247825, player, null, false);
				filename = null;
				break;
			case 41023:
				MultisellData.getInstance().separateAndSend(3247826, player, null, false);
				filename = null;
				break;
			case 41024:
				MultisellData.getInstance().separateAndSend(3247827, player, null, false);
				filename = null;
				break;
			case 41061:
				MultisellData.getInstance().separateAndSend(3247829, player, null, false);
				filename = null;
				break;
			case 41062:
				MultisellData.getInstance().separateAndSend(3247830, player, null, false);
				filename = null;
				break;
			case 41276:
				MultisellData.getInstance().separateAndSend(2573500, player, null, false);
				filename = null;
				break;
			case 41277:
				ItemHolder items = getRandomItem();
				player.destroyItemByItemId("황금돼지상자", 41277, 1, player, true);
				player.addItem("황금돼지상자", items.getId(), items.getCount(), player, true);
				
				String oldItemName = ItemNameTable.getInstance().getItemNameKor(41277);
				String message = BorinetUtil.getInstance().createMessage(player.getName(), oldItemName, items.getId(), (int) items.getCount(), false);
				BorinetUtil.getInstance().broadcastMessageToAllPlayers(message);
				
				filename = null;
				break;
			default:
				filename = "data/html/help/" + itemId + ".htm";
				break;
		}
		
		if (filename != null)
		{
			final String content = HtmCache.getInstance().getHtm(player, filename);
			if (content != null)
			{
				String html = HtmCache.getInstance().getHtm(null, filename);
				html = html.replace("<?isBuffOn?>", Config.SELLBUFF_ENABLED ? "<button action=\"bypass -h voice .버프판매\" value=\"버프 판매하기\" width=128 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">" : "");
				player.sendPacket(new NpcHtmlMessage(html));
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return true;
	}
	
	private static ItemHolder getRandomItem()
	{
		Random random = new Random();
		int randomIndex = random.nextInt(황금돼지_이벤트.length);
		return 황금돼지_이벤트[randomIndex];
	}
}
