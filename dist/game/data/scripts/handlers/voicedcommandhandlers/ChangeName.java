package handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author 보리넷
 */
public class ChangeName implements IVoicedCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(ChangeName.class.getName());
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.CHANGE_NAME_PRICE));
	
	private static final String[] VOICED_COMMANDS =
	{
		"이름변경"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (target != null)
		{
			String old_name = activeChar.getName();
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
					String new_name = name;
					activeChar.setName(name);
					LunaManager.getInstance().useLunaPoint(activeChar, itemCount, "캐릭터이름 변경");
					if (Config.CACHE_CHAR_NAMES)
					{
						CharInfoTable.getInstance().addName(activeChar);
					}
					activeChar.storeMe();
					activeChar.sendMessage("캐릭터 이름이 성공적으로 변경되었습니다.");
					activeChar.broadcastUserInfo();
					activeChar.decayMe();
					activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
					BorinetHtml.getInstance().showLunaMainHtml(activeChar);
					Npc.playTutorialVoice(activeChar, "borinet/CharNameChanged");
					insertDB(activeChar, old_name, new_name);
					
					if (activeChar.isInParty())
					{
						activeChar.getParty().broadcastToPartyMembers(activeChar, PartySmallWindowDeleteAll.STATIC_PACKET);
						for (Player member : activeChar.getParty().getMembers())
						{
							if (member != activeChar)
							{
								member.sendMessage(old_name + "님께서 " + new_name + "의 이름으로 변경하였습니다.");
								member.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, old_name + "님께서 " + new_name + "의 이름으로 변경하였습니다."));
								member.sendPacket(new PartySmallWindowAll(member, activeChar.getParty()));
							}
						}
					}
					if (activeChar.getClan() != null)
					{
						for (ClanMember member : activeChar.getClan().getMembers())
						{
							if (member.isOnline())
							{
								member.getPlayer().sendMessage(old_name + "님께서 " + new_name + "의 이름으로 변경하였습니다.");
								member.getPlayer().sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, old_name + "님께서 " + new_name + "의 이름으로 변경하였습니다."));
							}
						}
						activeChar.getClan().broadcastClanStatus();
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("이름을 변경 하지 못했습니다.");
				LOGGER.log(Level.WARNING, "", e);
			}
		}
		else
		{
			BorinetHtml.showHtml(activeChar, "LunaShop/ChangeCharName.htm", 0, "");
		}
		return true;
	}
	
	private void insertDB(Player player, String old_name, String new_name)
	{
		String query = "REPLACE INTO name_history(charId, old_name, new_name) VALUES (?, ?, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, player.getObjectId());
			statement.setString(2, old_name);
			statement.setString(3, new_name);
			statement.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "이름 변경 기록을 하는 도중 오류가 발생했습니다. 플레이어 ID: " + player.getObjectId() + ", 이전 이름: " + old_name + ", 새로운 이름: " + new_name, e);
			player.sendMessage("이름 변경 기록을 하는 도중 오류가 발생했습니다. 운영자에게 문의 바랍니다.");
		}
	}
	
	public boolean checkCondition(Player player, String name)
	{
		if (name.matches(".*[\\uAC00-\\uD7A3]+.*"))
		{
			if ((name.length() < 1) || (name.length() > 8))
			{
				BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
				player.sendPacket(SystemMessageId.YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGTH_PLEASE_TRY_AGAIN);
				return true;
			}
		}
		else
		{
			if ((name.length() < 1) || (name.length() > 16))
			{
				BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
				player.sendPacket(SystemMessageId.YOUR_TITLE_CANNOT_EXCEED_16_CHARACTERS_IN_LENGTH_PLEASE_TRY_AGAIN);
				return true;
			}
		}
		if (Config.FORBIDDEN_NAMES.length > 0)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (name.toLowerCase().contains(st.toLowerCase()))
				{
					BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
					player.sendPacket(SystemMessageId.INCORRECT_NAME_PLEASE_TRY_AGAIN);
					return true;
				}
			}
		}
		if (FakePlayerData.getInstance().getProperName(name) != null)
		{
			BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
			player.sendPacket(SystemMessageId.INCORRECT_NAME_PLEASE_TRY_AGAIN);
			return true;
		}
		if (!Util.isMatchingRegexp(name, Config.NAME_TEMPLATE) || !Util.isAlphaNumeric(name) || !Util.isValidName(name) || CharInfoTable.getInstance().doesCharNameExist(name))
		{
			BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
			player.sendPacket(SystemMessageId.INCORRECT_NAME_PLEASE_TRY_AGAIN);
			return true;
		}
		if ((name.matches("자리체")) || (name.matches("아카마나프")))
		{
			BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
			player.sendMessage("저주받은 무기의 이름으로 변경할 수 없습니다.");
			return true;
		}
		if (CharInfoTable.getInstance().ObsceneCharName(name))
		{
			BorinetHtml.showHtml(player, "LunaShop/ChangeCharName.htm", 0, "");
			player.sendMessage("욕설이 포함된 이름으로 변경할 수 없습니다.");
			return true;
		}
		
		if (player.isHero())
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("영웅 캐릭터는 사용할 수 없습니다.");
			return true;
		}
		if (player.isInSiege() || player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("공성중 에는 이름을 변경할수 없습니다.");
			return true;
		}
		if (player.isTransformed())
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("변신상태에서는 이름을 변경할수 없습니다.");
			return true;
		}
		if (player.isCursedWeaponEquipped() || (player.getReputation() < 0))
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("카오틱 캐릭터는 이름을 변경할수 없습니다.");
			return true;
		}
		if (player.isInDuel())
		{
			player.sendPacket(new ShowBoard());
			player.sendMessage("전투중에는 이름을 변경할수 없습니다.");
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
