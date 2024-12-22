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
package org.l2jmobius.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.data.xml.InitialEquipmentData;
import org.l2jmobius.gameserver.data.xml.InitialShortcutData;
import org.l2jmobius.gameserver.data.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.appearance.PlayerAppearance;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerCreate;
import org.l2jmobius.gameserver.model.item.PlayerItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.serverpackets.CharCreateFail;
import org.l2jmobius.gameserver.network.serverpackets.CharCreateOk;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectionInfo;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ServerClose;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Util;

@SuppressWarnings("unused")
public class CharacterCreate implements IClientIncomingPacket
{
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_name = packet.readS();
		_race = packet.readD();
		_sex = (byte) packet.readD();
		_classId = packet.readD();
		_int = packet.readD();
		_str = packet.readD();
		_con = packet.readD();
		_men = packet.readD();
		_dex = packet.readD();
		_wit = packet.readD();
		_hairStyle = (byte) packet.readD();
		_hairColor = (byte) packet.readD();
		_face = (byte) packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (Config.ONLY_CREATE_CHARACTER)
		{
			if (!BorinetUtil.getInstance().checkAH(client))
			{
				client.close(ServerClose.STATIC_PACKET);
				return;
			}
			if ((CharInfoTable.getInstance().getAccountCharacterCount(client.getAccountName()) >= 1))
			{
				final NpcHtmlMessage msg = new NpcHtmlMessage();
				msg.setFile(null, "data/html/mods/OnlyCreateChar/OnlyCreateChar-2.htm");
				client.sendPacket(msg);
				
				client.close(ServerClose.STATIC_PACKET);
				return;
			}
		}
		if (Config.ALT_ONLY_ONE_ACCOUNT_FOR_HWID)
		{
			if (!BorinetUtil.getInstance().createCharJoinHwid(client))
			{
				return;
			}
		}
		
		// Last Verified: May 30, 2009 - Gracia Final - Players are able to create characters with names consisting of as little as 1,2,3 letter/number combinations.
		if ((_name.length() < 1) || (_name.length() > 16))
		{
			client.sendPacket(new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS));
			return;
		}
		
		if (Config.FORBIDDEN_NAMES.length > 0)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (_name.toLowerCase().contains(st.toLowerCase()))
				{
					client.sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
					return;
				}
			}
		}
		
		if (FakePlayerData.getInstance().getProperName(_name) != null)
		{
			client.sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
			return;
		}
		
		// Last Verified: May 30, 2009 - Gracia Final
		if (!Util.isMatchingRegexp(_name, Config.NAME_TEMPLATE) || !Util.isAlphaNumeric(_name) || !Util.isValidName(_name))
		{
			client.sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
			return;
		}
		
		if ((_face > 2) || (_face < 0))
		{
			PacketLogger.warning("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + client);
			client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		if ((_hairStyle < 0) || ((_sex == 0) && (_hairStyle > 4)) || ((_sex != 0) && (_hairStyle > 6)))
		{
			PacketLogger.warning("Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + client);
			client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		if ((_hairColor > 3) || (_hairColor < 0))
		{
			PacketLogger.warning("Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + client);
			client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		Player newChar = null;
		PlayerTemplate template = null;
		
		/*
		 * DrHouse: Since checks for duplicate names are done using SQL, lock must be held until data is written to DB as well.
		 */
		synchronized (CharInfoTable.getInstance())
		{
			if ((CharInfoTable.getInstance().getAccountCharacterCount(client.getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT) && (Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0))
			{
				client.sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			else if (CharInfoTable.getInstance().doesCharNameExist(_name))
			{
				client.sendPacket(new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS));
				return;
			}
			
			template = PlayerTemplateData.getInstance().getTemplate(_classId);
			if ((template == null) || (ClassId.getClassId(_classId).level() > 0))
			{
				client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}
			
			// Custom Feature: Disallow a race to be created.
			// Example: Humans can not be created if AllowHuman = False in Custom.properties
			switch (template.getRace())
			{
				case HUMAN:
				{
					if (!Config.ALLOW_HUMAN)
					{
						client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
						return;
					}
					break;
				}
				case ELF:
				{
					if (!Config.ALLOW_ELF)
					{
						client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
						return;
					}
					break;
				}
				case DARK_ELF:
				{
					if (!Config.ALLOW_DARKELF)
					{
						client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
						return;
					}
					break;
				}
				case ORC:
				{
					if (!Config.ALLOW_ORC)
					{
						client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
						return;
					}
					break;
				}
				case DWARF:
				{
					if (!Config.ALLOW_DWARF)
					{
						client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
						return;
					}
					break;
				}
				case KAMAEL:
				{
					if (!Config.ALLOW_KAMAEL)
					{
						client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
						return;
					}
					break;
				}
				case ERTHEIA:
				{
					if (!Config.ALLOW_ERTHEIA)
					{
						client.sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
						return;
					}
					break;
				}
			}
			newChar = Player.create(template, client.getAccountName(), _name, new PlayerAppearance(_face, _hairColor, _hairStyle, _sex != 0));
		}
		
		// HP and MP are at maximum and CP is zero by default.
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentMp(newChar.getMaxMp());
		// newChar.setMaxLoad(template.getBaseLoad());
		client.sendPacket(CharCreateOk.STATIC_PACKET);
		
		initNewChar(client, newChar);
	}
	
	private void initNewChar(GameClient client, Player newChar)
	{
		World.getInstance().addObject(newChar);
		
		if (Config.STARTING_ADENA > 0)
		{
			newChar.addAdena("신규캐릭터", Config.STARTING_ADENA, null, false);
		}
		
		final PlayerTemplate template = newChar.getTemplate();
		if (Config.CUSTOM_STARTING_LOC)
		{
			newChar.setXYZInvisible(Config.CUSTOM_STARTING_LOC_X, Config.CUSTOM_STARTING_LOC_Y, Config.CUSTOM_STARTING_LOC_Z);
		}
		else
		{
			final Location createLoc = template.getCreationPoint();
			newChar.setXYZInvisible(createLoc.getX(), createLoc.getY(), createLoc.getZ());
		}
		
		if (Config.CHAR_TITLE)
		{
			String title = getRandomTitleFromDatabase();
			if (title != null)
			{
				newChar.setTitle(title);
				newChar.getAppearance().setTitleColor(Integer.decode("0x00FFFF"));
			}
			else
			{
				switch (newChar.getRace())
				{
					case HUMAN:
						if (newChar.getClassId().isMage())
						{
							newChar.setTitle(_sex == 0 ? Config.HUMAN_MAGE_M_CHAR_TITLE : Config.HUMAN_MAGE_W_CHAR_TITLE);
						}
						else
						{
							newChar.setTitle(_sex == 0 ? Config.HUMAN_FIGHTER_M_CHAR_TITLE : Config.HUMAN_FIGHTER_W_CHAR_TITLE);
						}
						break;
					case ELF:
						if (newChar.getClassId().isMage())
						{
							newChar.setTitle(_sex == 0 ? Config.ELF_MAGE_M_CHAR_TITLE : Config.ELF_MAGE_W_CHAR_TITLE);
						}
						else
						{
							newChar.setTitle(_sex == 0 ? Config.ELF_FIGHTER_M_CHAR_TITLE : Config.ELF_FIGHTER_W_CHAR_TITLE);
						}
						break;
					case DARK_ELF:
						if (newChar.getClassId().isMage())
						{
							newChar.setTitle(_sex == 0 ? Config.DELF_MAGE_M_CHAR_TITLE : Config.DELF_MAGE_W_CHAR_TITLE);
						}
						else
						{
							newChar.setTitle(_sex == 0 ? Config.DELF_FIGHTER_M_CHAR_TITLE : Config.DELF_FIGHTER_W_CHAR_TITLE);
						}
						break;
					case ORC:
						if (newChar.getClassId().isMage())
						{
							newChar.setTitle(_sex == 0 ? Config.ORC_MAGE_M_CHAR_TITLE : Config.ORC_MAGE_W_CHAR_TITLE);
						}
						else
						{
							newChar.setTitle(_sex == 0 ? Config.ORC_FIGHTER_M_CHAR_TITLE : Config.ORC_FIGHTER_W_CHAR_TITLE);
						}
						break;
					case DWARF:
						newChar.setTitle(_sex == 0 ? Config.DWARF_FIGHTER_M_CHAR_TITLE : Config.DWARF_FIGHTER_W_CHAR_TITLE);
						break;
				}
			}
		}
		else
		{
			newChar.setTitle("");
		}
		
		if (Config.ENABLE_VITALITY)
		{
			newChar.setVitalityPoints(Math.min(Config.STARTING_VITALITY_POINTS, PlayerStat.MAX_VITALITY_POINTS), true);
		}
		if (Config.STARTING_LEVEL > 1)
		{
			newChar.getStat().addLevel((byte) (Config.STARTING_LEVEL - 1));
		}
		if (Config.STARTING_SP > 0)
		{
			newChar.getStat().addSp(Config.STARTING_SP);
		}
		
		final List<PlayerItemTemplate> initialItems = InitialEquipmentData.getInstance().getEquipmentList(newChar.getClassId());
		if (initialItems != null)
		{
			for (PlayerItemTemplate ie : initialItems)
			{
				final Item item = newChar.getInventory().addItem("Init", ie.getId(), ie.getCount(), newChar, null);
				if (item == null)
				{
					PacketLogger.warning("Could not create item during char creation: itemId " + ie.getId() + ", amount " + ie.getCount() + ".");
					continue;
				}
				
				if (item.isEquipable() && ie.isEquipped())
				{
					newChar.getInventory().equipItem(item);
				}
			}
		}
		
		for (SkillLearn skill : SkillTreeData.getInstance().getAvailableSkills(newChar, newChar.getClassId(), false, true))
		{
			newChar.addSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()), true);
		}
		
		// Register all shortcuts for actions, skills and items for this new character.
		InitialShortcutData.getInstance().registerAllShortcuts(newChar);
		
		EventDispatcher.getInstance().notifyEvent(new OnPlayerCreate(newChar, newChar.getObjectId(), newChar.getName(), client), Containers.Players());
		newChar.setOnlineStatus(true, false);
		Disconnection.of(client, newChar).storeMe().deleteMe();
		
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.setCharSelection(cl.getCharInfo());
	}
	
	private String getRandomTitleFromDatabase()
	{
		String randomTitle = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			int maxId = 0;
			try (PreparedStatement ps = con.prepareStatement("SELECT MAX(id) FROM newbie_title");
				ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					maxId = rs.getInt(1);
				}
			}
			
			if (maxId > 0)
			{
				int randomId = Rnd.get(1, maxId);
				
				try (PreparedStatement ps = con.prepareStatement("SELECT title FROM newbie_title WHERE id = ?"))
				{
					ps.setInt(1, randomId);
					try (ResultSet rs = ps.executeQuery())
					{
						if (rs.next())
						{
							randomTitle = rs.getString("title");
						}
					}
				}
			}
		}
		catch (SQLException e)
		{
			System.err.println("Failed to fetch random title: " + e.getMessage());
		}
		
		return randomTitle;
	}
	
}
