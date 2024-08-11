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
package handlers.itemhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExVitalityEffectInfo;

/**
 * @author Mode
 */
public class SayhaPotions implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = playable.getActingPlayer();
		int vitality = 0;
		switch (item.getId())
		{
			case 49845:
			{
				vitality = 35000;
				break;
			}
			case 49846:
			{
				vitality = 70000;
				break;
			}
			case 49847:
			{
				vitality = 105000;
				break;
			}
		}
		if (player.getVitalityItemsUsed() < Config.VITALITY_MAX_ITEMS_ALLOWED)
		{
			if ((player.getVitalityPoints() + vitality) <= PlayerStat.MAX_VITALITY_POINTS)
			{
				player.setVitalityPoints(player.getVitalityPoints() + vitality, false);
				player.setVitalityItemsUsed(player.getVitalityItemsUsed() + 1);
				player.sendPacket(new ExVitalityEffectInfo(player));
				player.destroyItem("Sayha potion", item, 1, player, true);
			}
			else
			{
				player.sendMessage("아이템 사용시 사이하의 은총 최대 포인트를 벗어납니다.");
				return false;
			}
		}
		else
		{
			player.sendMessage("이번주에 사용 가능한 샤이아의 은총 아이템을 모두 사용하였습니다.");
			return false;
		}
		return true;
	}
}