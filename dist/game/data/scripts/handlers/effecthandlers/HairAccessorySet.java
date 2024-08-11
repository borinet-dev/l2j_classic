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
package handlers.effecthandlers;

import org.l2jmobius.gameserver.enums.UserInfoType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author UnAfraid
 */
public class HairAccessorySet extends AbstractEffect
{
	public HairAccessorySet(StatSet params)
	{
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final Player player = effected.getActingPlayer();
		for (Item equippedItem : player.getInventory().getPaperdollItems())
		{
			final ItemTemplate hair = equippedItem.getTemplate();
			final long bodypart = hair.getBodyPart();
			if ((bodypart == ItemTemplate.SLOT_HAIR) || (bodypart == ItemTemplate.SLOT_HAIR2) || (bodypart == ItemTemplate.SLOT_HAIRALL))
			{
				player.setHairAccessoryEnabled(!player.isHairAccessoryEnabled());
				player.broadcastUserInfo(UserInfoType.APPAREANCE);
				player.sendPacket(player.isHairAccessoryEnabled() ? SystemMessageId.HAIR_ACCESSORIES_WILL_BE_DISPLAYED_FROM_NOW_ON : SystemMessageId.HAIR_ACCESSORIES_WILL_NO_LONGER_BE_DISPLAYED);
				return;
			}
		}
		player.sendPacket(SystemMessageId.THERE_IS_NO_EQUIPPED_HAIR_ACCESSORY);
		player.sendPacket(SystemMessageId.PLEASE_EQUIP_THE_HAIR_ACCESSORY_AND_TRY_AGAIN);
	}
}
