package handlers.bypasshandlers;

import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class ChoiceItems implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"ClanArmor",
		"ClanWeapon"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (command.startsWith("ClanArmor"))
		{
			if ((player.getInventory().getInventoryItemCount(41011, -1) < 1))
			{
				player.sendMessage("혈맹이전 코인이 없어서 교환할 수 없습니다.");
				return true;
			}
			player.destroyItemByItemId("혈맹이전코인사용", 41011, 1, player, true);
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			tokenizer.nextToken();
			while (tokenizer.hasMoreTokens())
			{
				final int itemId = Integer.parseInt(tokenizer.nextToken());
				final Item createditem = ItemTemplate.createItem(itemId);
				
				player.addItem("혈맹이전코인사용", createditem, null, true);
				final String content = HtmCache.getInstance().getHtm(player, "data/html/borinet/clanWeapon.htm");
				final NpcHtmlMessage html = new NpcHtmlMessage();
				html.setHtml(content);
				player.sendPacket(html);
			}
		}
		if (command.startsWith("ClanWeapon"))
		{
			StringTokenizer tokenizer = new StringTokenizer(command, " ");
			tokenizer.nextToken();
			while (tokenizer.hasMoreTokens())
			{
				final int itemId = Integer.parseInt(tokenizer.nextToken());
				final Item createditem = ItemTemplate.createItem(itemId);
				
				player.addItem("혈맹이전코인사용", createditem, null, true);
			}
			String subject = "다시한번 환영합니다!";
			String bodyText = "환영 선물입니다.\n\n" + Config.SERVER_NAME_KOR + "과 함께 즐거운 시간 되시기 바랍니다.\n\n감사합니다.";
			String itemIds = Config.CLAN_MOVED_REWARD;
			BorinetUtil.getInstance().insertCustomMail(player, subject, bodyText, itemIds);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}