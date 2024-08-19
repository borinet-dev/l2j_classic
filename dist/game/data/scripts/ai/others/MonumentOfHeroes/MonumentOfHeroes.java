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
package ai.others.MonumentOfHeroes;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExHeroList;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

/**
 * Monument of Heroes AI.
 * @author St3eT
 */
public class MonumentOfHeroes extends AbstractNpcAI
{
	// NPC
	private static final int MONUMENT = 31690;
	// Items
	private static final int HERO_CLOAK = 30372;
	// private static final int GLORIOUS_CLOAK = 30373;
	private static final int WINGS_OF_DESTINY_CIRCLET = 6842;
	private static final int[] WEAPONS =
	{
		6611, // Infinity Blade
		6612, // Infinity Cleaver
		6613, // Infinity Axe
		6614, // Infinity Rod
		6615, // Infinity Rod
		6616, // Infinity Scepter
		6617, // Infinity Stinger
		6618, // Infinity Fang
		6619, // Infinity Bow
		6620, // Infinity Wing
		6621, // Infinity Spear
		30392,
		30393,
		30394,
		30395,
		30396,
		30397,
		30398,
		30399,
		30400,
		30401,
		30402,
		30403,
		30404,
		30405,
	};
	
	private MonumentOfHeroes()
	{
		addStartNpc(MONUMENT);
		addFirstTalkId(MONUMENT);
		addTalkId(MONUMENT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "reward":
			{
				if (!player.isInventoryUnder80(false))
				{
					player.sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				}
				else if (Hero.getInstance().isHero(player.getObjectId()) || player.isGM())
				{
					htmltext = getHtm(player, "MonumentOfHeroes-reward.html");
					htmltext = htmltext.replace("%reward%", hasAtLeastOneQuestItem(player, WEAPONS) ? "무기 교체" : "무기 받기");
					htmltext = htmltext.replace("%change%", hasAtLeastOneQuestItem(player, WEAPONS) ? "다른 무기로 교체하여 사용할 수 있습니다." : "무기를 선택하여 사용할 수 있습니다.");
				}
				else
				{
					htmltext = "MonumentOfHeroes-noHero.html";
				}
				break;
			}
			case "index":
			{
				htmltext = onFirstTalk(npc, player);
				break;
			}
			case "heroList":
			{
				player.sendPacket(new ExHeroList());
				break;
			}
			case "heroWeapon":
			{
				if (player.isInventoryUnder80(false))
				{
					htmltext = getHtm(player, "MonumentOfHeroes-weaponList.html");
					htmltext = htmltext.replace("%choice%", hasAtLeastOneQuestItem(player, WEAPONS) ? "교체하실" : "자신에게 맞는");
				}
				else
				{
					player.sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				}
				break;
			}
			case "heroCertification":
			{
				if (Hero.getInstance().isUnclaimedHero(player.getObjectId()))
				{
					htmltext = "MonumentOfHeroes-heroCertification.html";
				}
				else if (Hero.getInstance().isHero(player.getObjectId()))
				{
					htmltext = "MonumentOfHeroes-heroCertificationAlready.html";
				}
				else
				{
					htmltext = "MonumentOfHeroes-heroCertificationNo.html";
				}
				break;
			}
			case "heroConfirm":
			{
				if (player.isMainClassActive())
				{
					Hero.getInstance().claimHero(player);
					showOnScreenMsg(player, (NpcStringId.getNpcStringId(13357 + player.getClassId().getId())), ExShowScreenMessage.TOP_CENTER, 5000);
					player.broadcastPacket(new PlaySound(1, "ns01_f", 0, 0, 0, 0, 0));
					if (!hasQuestItems(player, WINGS_OF_DESTINY_CIRCLET))
					{
						giveItems(player, WINGS_OF_DESTINY_CIRCLET, 1);
					}
					if (!hasAtLeastOneQuestItem(player, HERO_CLOAK))
					{
						giveItems(player, HERO_CLOAK, 1);
					}
					htmltext = "MonumentOfHeroes-heroCertificationsDone.html";
				}
				else
				{
					htmltext = "MonumentOfHeroes-heroCertificationSub.html";
				}
				break;
			}
			case "give_6611": // Infinity Blade
			case "give_6612": // Infinity Cleaver
			case "give_6613": // Infinity Axe
			case "give_6614": // Infinity Rod
			case "give_6615": // Infinity Rod
			case "give_6616": // Infinity Scepter
			case "give_6617": // Infinity Stinger
			case "give_6618": // Infinity Fang
			case "give_6619": // Infinity Bow
			case "give_6620": // Infinity Wing
			case "give_6621": // Infinity Spear
			case "give_30392":
			case "give_30393":
			case "give_30394":
			case "give_30395":
			case "give_30396":
			case "give_30397":
			case "give_30398":
			case "give_30399":
			case "give_30400":
			case "give_30401":
			case "give_30402":
			case "give_30403":
			case "give_30404":
			case "give_30405":
			{
				if (hasAtLeastOneQuestItem(player, WEAPONS))
				{
					deleteHerotIems(player);
				}
				final int weaponId = Integer.parseInt(event.replace("give_", ""));
				giveItems(player, weaponId, 1);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.isNoble() && player.isMainClassActive())
		{
			return "MonumentOfHeroes-noblesse.html";
		}
		return "MonumentOfHeroes-noNoblesse.html";
	}
	
	private void deleteHerotIems(Player player)
	{
		for (int itemId : WEAPONS)
		{
			if (player != null)
			{
				final Item item = player.getInventory().getItemByItemId(itemId);
				final Item items = player.getWarehouse().getItemByItemId(itemId);
				if (item != null)
				{
					player.destroyItemByItemId("영웅아이템삭제", itemId, item.getCount(), player, true);
				}
				if (items != null)
				{
					player.destroyItemByItemIdInWareHouse("영웅아이템삭제", itemId, items.getCount(), player, true);
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new MonumentOfHeroes();
	}
}