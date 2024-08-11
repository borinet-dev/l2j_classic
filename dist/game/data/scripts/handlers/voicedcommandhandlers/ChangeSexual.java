package handlers.voicedcommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author 보리넷
 */
public class ChangeSexual implements IVoicedCommandHandler
{
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.CHANGE_SEXUAL_PRICE));
	
	private static final String[] VOICED_COMMANDS =
	{
		"성전환",
		"성전환하기"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equals("성전환"))
		{
			SexsualMain(activeChar);
		}
		else if (command.equals("성전환하기"))
		{
			if (activeChar.getLuna() >= itemCount)
			{
				int maxHairstyle = activeChar.getAppearance().isFemale() ? 6 : 4;
				
				activeChar.getVariables().set("EVENT_HAIR_STYLE", activeChar.getAppearance().getHairStyle());
				activeChar.getAppearance().toggleGender();
				activeChar.getAppearance().setHairStyle(Rnd.get(0, maxHairstyle));
				LunaManager.getInstance().useLunaPoint(activeChar, itemCount, "성전환");
				activeChar.getAppearance();
				activeChar.sendPacket(new CreatureSay(activeChar, ChatType.BATTLEFIELD, "[성전환]", "캐릭터 성별이 성공적으로 변경되었습니다."));
				activeChar.broadcastUserInfo();
				activeChar.decayMe();
				activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 2122, 1, 1000, 0));
				activeChar.sendPacket(new ShowBoard());
				Npc.playTutorialVoice(activeChar, "borinet/CharSexual");
				return true;
			}
			activeChar.sendMessage("루나가 부족합니다.");
			return true;
		}
		return true;
	}
	
	public void SexsualMain(Player player)
	{
		final String header = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/header.htm");
		final String menu = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/menu.htm");
		String html = null;
		html = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/LunaShop/ChangeSexual.htm");
		html = html.replace("%header%", header);
		html = html.replace("%menu%", menu);
		html = html.replace("%item%", itemName);
		html = html.replace("%itemCount%", Integer.toString(itemCount));
		html = html.replace("%sex%", player.getAppearance().isFemale() ? "여성" : "남성");
		html = html.replace("%mainbanner%", BorinetHtml.getBannerForRace(player));
		CommunityBoardHandler.separateAndSend(html, player);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
