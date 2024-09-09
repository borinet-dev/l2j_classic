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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.ExtractableProductItem;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.holders.RestorationItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * Restoration Random effect implementation.<br>
 * This effect is present in item skills that "extract" new items upon usage.<br>
 * This effect has been unhardcoded in order to work on targets as well.
 * @author Zoey76, Mobius
 */
public class RestorationRandom extends AbstractEffect
{
	private final List<ExtractableProductItem> _products = new ArrayList<>();
	
	public RestorationRandom(StatSet params)
	{
		for (StatSet group : params.getList("items", StatSet.class))
		{
			final List<RestorationItemHolder> items = new ArrayList<>();
			for (StatSet item : group.getList(".", StatSet.class))
			{
				items.add(new RestorationItemHolder(item.getInt(".id"), item.getInt(".count"), item.getInt(".minEnchant", 0), item.getInt(".maxEnchant", 0)));
			}
			_products.add(new ExtractableProductItem(items, group.getFloat(".chance")));
		}
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final double rndNum = 100 * Rnd.nextDouble();
		double chance = 0;
		double chanceFrom = 0;
		final List<RestorationItemHolder> creationList = new ArrayList<>();
		String oldName = item.getName();
		
		// Explanation for future changes:
		// You get one chance for the current skill, then you can fall into
		// one of the "areas" like in a roulette.
		// Example: for an item like Id1,A1,30;Id2,A2,50;Id3,A3,20;
		// #---#-----#--#
		// 0--30----80-100
		// If you get chance equal 45% you fall into the second zone 30-80.
		// Meaning you get the second production list.
		// Calculate extraction
		for (ExtractableProductItem expi : _products)
		{
			chance = expi.getChance();
			if ((rndNum >= chanceFrom) && (rndNum <= (chance + chanceFrom)))
			{
				creationList.addAll(expi.getItems());
				break;
			}
			chanceFrom += chance;
		}
		
		final Player player = effected.getActingPlayer();
		if (creationList.isEmpty())
		{
			player.sendPacket(SystemMessageId.THERE_WAS_NOTHING_FOUND_INSIDE);
			return;
		}
		
		final Map<Item, Long> extractedItems = new HashMap<>();
		for (RestorationItemHolder createdItem : creationList)
		{
			if ((createdItem.getId() <= 0) || (createdItem.getCount() <= 0))
			{
				continue;
			}
			
			final long itemCount = (long) (createdItem.getCount() * Config.RATE_EXTRACTABLE);
			final Item newItem = player.addItem("추출획득", createdItem.getId(), itemCount, effector, false);
			
			if (createdItem.getMaxEnchant() > 0)
			{
				newItem.setEnchantLevel(Rnd.get(createdItem.getMinEnchant(), createdItem.getMaxEnchant()));
			}
			
			if (extractedItems.containsKey(newItem))
			{
				extractedItems.put(newItem, extractedItems.get(newItem) + itemCount);
			}
			else
			{
				extractedItems.put(newItem, itemCount);
			}
		}
		
		if (!extractedItems.isEmpty())
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			for (Entry<Item, Long> entry : extractedItems.entrySet())
			{
				if (entry.getKey().getTemplate().isStackable())
				{
					playerIU.addModifiedItem(entry.getKey());
				}
				else
				{
					for (Item itemInstance : player.getInventory().getAllItemsByItemId(entry.getKey().getId()))
					{
						playerIU.addModifiedItem(itemInstance);
					}
				}
				boolean sendScreen = true;
				switch (item.getId())
				{
					case 49782:
					case 70000:
					case 70001:
					case 70002:
					case 70003:
					case 70004:
					case 70005:
					case 70006:
					case 70007:
					case 70008:
					case 70009:
					case 70010:
					case 70011:
					case 70012:
					case 70013:
					case 70014:
					case 70015:
					case 90793:
					case 34695:
					case 23396:
					case 23397:
					case 23398:
					case 23399:
					case 13990:
					case 34813:
					case 70025:
					case 70026:
					case 70027:
					case 70028:
					case 90911:
					case 90912:
					case 90913:
					case 90914:
					case 90915:
					{
						sendScreen = false;
						break;
					}
				}
				sendMessage(player, oldName, entry.getKey(), entry.getValue().intValue(), sendScreen);
			}
			player.sendPacket(playerIU);
		}
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.EXTRACT_ITEM;
	}
	
	private void sendMessage(Player player, String oldName, Item item, int count, boolean sendScreen)
	{
		final SystemMessage sm;
		if (count > 1)
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
			sm.addItemName(item);
			sm.addLong(count);
		}
		else if (item.getEnchantLevel() > 0)
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_A_S1_S2);
			sm.addInt(item.getEnchantLevel());
			sm.addItemName(item);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
			sm.addItemName(item);
		}
		player.sendPacket(sm);
		
		String message = null;
		if (sendScreen)
		{
			message = BorinetUtil.getInstance().createMessage(player.getName(), oldName, item.getId(), count, false);
			BorinetUtil.getInstance().broadcastMessageToAllPlayers(message);
		}
		
		// if (item.getName().contains("아포칼립스") || item.getName().contains("카데이라"))
		// {
		// if (item.getEnchantLevel() > 0)
		// {
		// Broadcast.toAllOnlinePlayersOnScreen(player.getName() + "님이 [" + oldName + "]에서 [+" + item.getEnchantLevel() + " " + KorNameUtil.getName(item.getName(), "]을", "]를") + " 획득했습니다!");
		// }
		// else
		// {
		// Broadcast.toAllOnlinePlayersOnScreen(player.getName() + "님이 [" + oldName + "]에서 [" + KorNameUtil.getName(item.getName(), "]을", "]를") + " 획득했습니다!");
		// final Skill skill = CommonSkill.FIREWORK.getSkill();
		// if ((skill != null) && item.getName().contains("축복받은"))
		// {
		// player.broadcastPacket(new MagicSkillUse(player, player, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
		// }
		// }
		// }
	}
}
