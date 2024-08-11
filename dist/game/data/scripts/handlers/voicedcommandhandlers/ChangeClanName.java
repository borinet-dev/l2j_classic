package handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.enums.UserInfoType;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author 보리넷
 */
public class ChangeClanName implements IVoicedCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(ChangeClanName.class.getName());
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.CHANGE_CLAN_NAME_PRICE));
	
	private static final String[] VOICED_COMMANDS =
	{
		"혈맹이름변경"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (target != null)
		{
			final StringTokenizer st = new StringTokenizer(target);
			try
			{
				String name = null;
				if (st.hasMoreTokens())
				{
					name = st.nextToken();
				}
				
				if (!checkCondition(activeChar, name))
				{
					Clan clan = activeChar.getClan();
					activeChar.getClan().setName(name);
					
					LunaManager.getInstance().useLunaPoint(activeChar, itemCount, "혈맹이름 변경");
					if (Config.CACHE_CHAR_NAMES)
					{
						CharInfoTable.getInstance().addName(activeChar);
					}
					try (Connection con = DatabaseFactory.getConnection();
						PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET clan_name = '" + name + "' WHERE clan_id = " + activeChar.getClanId() + ""))
					{
						ps.execute();
					}
					catch (Exception e)
					{
					}
					activeChar.storeMe();
					activeChar.sendMessage("혈맹 이름이 성공적으로 변경되었습니다.");
					
					activeChar.broadcastCharInfo();
					activeChar.broadcastUserInfo();
					activeChar.sendPacket(new PledgeShowInfoUpdate(clan));
					PledgeShowMemberListAll.sendAllTo(activeChar);
					activeChar.sendPacket(new PledgeShowMemberListUpdate(activeChar));
					activeChar.broadcastUserInfo(UserInfoType.RELATION, UserInfoType.CLAN);
					
					clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
					clan.broadcastToOnlineMembers("혈맹의 이름이 변경되었습니다. -> [" + name + "]");
					BorinetHtml.getInstance().showLunaMainHtml(activeChar);
					Npc.playTutorialVoice(activeChar, "borinet/ClanNameChanged");
					activeChar.getClan().broadcastClanStatus();
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("혈맹 이름을 변경 하지 못했습니다.");
				LOGGER.log(Level.WARNING, "", e);
			}
		}
		else
		{
			BorinetHtml.showHtml(activeChar, "LunaShop/ChangeClanName.htm", 0, "");
		}
		return true;
	}
	
	public boolean checkCondition(Player player, String name)
	{
		if ((player.getClan() == null) || !player.isClanLeader())
		{
			player.sendPacket(new ShowBoard());
			player.sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addPcName(player));
			return true;
		}
		if (player.isInSiege() || player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("공성중 에는 이름을 변경할수 없습니다.");
			return true;
		}
		if (!Util.isMatchingRegexp(name, Config.NAME_TEMPLATE) || !Util.isAlphaNumeric(name) || !Util.isValidName(name))
		{
			BorinetHtml.showHtml(player, "LunaShop/ChangeClanName.htm", 0, "");
			player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
			return true;
		}
		if ((name.length() < 2) && (name.length() > 16))
		{
			BorinetHtml.showHtml(player, "LunaShop/ChangeClanName.htm", 0, "");
			player.sendPacket(SystemMessageId.CLAN_NAME_S_LENGTH_IS_INCORRECT);
			return true;
		}
		if (ClanTable.getInstance().getClanByName(name) != null)
		{
			BorinetHtml.showHtml(player, "LunaShop/ChangeClanName.htm", 0, "");
			player.sendPacket(new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(name));
			return true;
		}
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("올림피아드 게임중에는 이름을 변경할수 없습니다.");
			return true;
		}
		if (player.isInInstance())
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("인스턴트 던전 이용중에는 이름을 변경할수 없습니다.");
			return true;
		}
		if (player.getLuna() < itemCount)
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage(itemName + "가 부족합니다.");
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
