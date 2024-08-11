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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.util.Util;

public class ChangeVisualRace implements IBypassHandler
{
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.CHANGE_RACE_PRICE));
	private static final String[] COMMANDS =
	{
		"change_visual_classid",
		"repair_visual_classid"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (command.toLowerCase().startsWith("change_visual_classid"))
		{
			String value = command.substring(21).trim();
			int visual_classid = -2;
			switch (value)
			{
				case "휴먼전사":// 휴먼 파이터
					visual_classid = ClassId.FIGHTER.ordinal();
					break;
				case "휴먼법사":// 휴먼 메이지
					visual_classid = ClassId.MAGE.ordinal();
					break;
				case "엘프":// 엘프 파이터
					visual_classid = ClassId.ELVEN_FIGHTER.ordinal();
					break;
				case "다크엘프":// 다크엘프 파이터
					visual_classid = ClassId.DARK_FIGHTER.ordinal();
					break;
				case "오크전사":// 오크 파이터
					visual_classid = ClassId.ORC_FIGHTER.ordinal();
					break;
				case "오크법사":// 오크 메이지
					visual_classid = ClassId.ORC_MAGE.ordinal();
					break;
				case "드워프":// 드워프 파이터
					visual_classid = ClassId.DWARVEN_FIGHTER.ordinal();
					break;
				default:
					player.sendMessage("관리자에게 문의하세요");
					return false;
			}
			
			if (check(player, value))
			{
				LunaManager.getInstance().useLunaPoint(player, itemCount, "캐릭터 외형 변경");
				change(player, visual_classid, "CharVisualRace", "캐릭터 외형이 성공적으로 변경되었습니다.");
			}
		}
		else if (command.equalsIgnoreCase("repair_visual_classid"))
		{
			if (player.getVisualClassId() == -2)
			{
				player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[외형변경]", "현재 기존 종족의 외형입니다."));
				return false;
			}
			change(player, -2, "CharVisualRestore", "캐릭터 외형이 기존 종족으로 복구되었습니다.");
		}
		return true;
	}
	
	private void change(Player player, int classId, String voice, String text)
	{
		Npc.playTutorialVoice(player, "borinet/" + voice);
		player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[외형변경]", text));
		player.sendPacket(new ShowBoard());
		player.setVisualClassId(ClassId.ELVEN_FIGHTER.ordinal());
		player.setVisualClassId(classId);
		player.broadcastUserInfo();
		player.storeMe();
		player.broadcastPacket(new MagicSkillUse(player, player, 2122, 1, 1000, 0));
	}
	
	private boolean check(Player player, String val)
	{
		if (player.getLuna() < itemCount)
		{
			player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[외형변경]", itemName + "가 부족합니다."));
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[외형변경]", "저주받은 무기를 장착한 상태에서는 불가능합니다."));
			return false;
		}
		if (player.isTransformed())
		{
			player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[외형변경]", "변신 중에는 불가능합니다."));
			return false;
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
