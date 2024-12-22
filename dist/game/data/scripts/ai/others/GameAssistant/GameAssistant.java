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
package ai.others.GameAssistant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.TaxType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.buylist.ProductList;
import org.l2jmobius.gameserver.model.clan.ClanPrivilege;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerFreight;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.zone.type.TaxZone;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.BuyList;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExBuySellList;
import org.l2jmobius.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import org.l2jmobius.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import org.l2jmobius.gameserver.network.serverpackets.HennaEquipList;
import org.l2jmobius.gameserver.network.serverpackets.HennaRemoveList;
import org.l2jmobius.gameserver.network.serverpackets.PackageToList;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseDepositList;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseWithdrawalList;
import org.l2jmobius.gameserver.network.serverpackets.ensoul.ExShowEnsoulExtractionWindow;
import org.l2jmobius.gameserver.network.serverpackets.ensoul.ExShowEnsoulWindow;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * Dimensional Merchant AI.
 * @author Mobius, quangnguyen
 */
public class GameAssistant extends AbstractNpcAI
{
	// NPC
	private static final int MERCHANT = 32478; // Dimensional Merchant
	// Items
	private static final int BLACK_SAYHA_CLOAK = 91210;
	private static final int SAYHA_CLOAK_COUPON = 91227;
	// Multisells
	private static final int ATTENDANCE_REWARD_MULTISELL = 3247801;
	//
	private static final int SIGEL_SOUL_CRYSTAL = 3247802;
	private static final int TYRR_SOUL_CRYSTAL = 3247803;
	private static final int OTHELL_SOUL_CRYSTAL = 3247804;
	private static final int YUL_SOUL_CRYSTAL = 3247805;
	private static final int FEOH_SOUL_CRYSTAL = 3247806;
	private static final int ISS_SOUL_CRYSTAL = 3247807;
	private static final int WYNN_SOUL_CRYSTAL = 3247808;
	private static final int AEORE_SOUL_CRYSTAL = 3247809;
	//
	private static final int EX_SAYHA_BLESSING_SHOP = 3247810;
	private static final int EX_GIRAN_SEALS_SHOP = 3247811;
	private static final int EX_DOLL_7DAYS_SHOP = 3247812;
	private static final int EX_BOSS_WEAPON_SHOP = 3247813;
	//
	private static final int EX_MYSTERIUS_LEVEL2 = 3247814;
	private static final int EX_MYSTERIUS_LEVEL3 = 3247815;
	private static final int EX_MYSTERIUS_LEVEL4 = 3247816;
	private static final int EX_MYSTERIUS_LEVEL5 = 3247817;
	private static final int EX_MYSTERIUS_LEVEL6 = 3247818;
	private static final int EX_MYSTERIUS_LEVEL7 = 3247819;
	private static final int EX_MYSTERIUS_LEVEL8 = 3247820;
	//
	private static final int EX_HEAVY_A_GRADE = 3247821;
	private static final int EX_LIGHT_A_GRADE = 3247822;
	private static final int EX_ROBE_A_GRADE = 3247823;
	private static final int EX_WEAPON_A_GRADE = 3247824;
	private static final int EX_SPECIAL_A_GRADE = 3247825;
	private static final int EX_HEAVY_B_GRADE = 3247826;
	private static final int EX_LIGHT_B_GRADE = 3247827;
	private static final int EX_ROBE_B_GRADE = 3247828;
	private static final int EX_WEAPON_B_GRADE = 3247829;
	private static final int EX_WEAPON_C_GRADE = 3247830;
	private static final int EX_SAYHA_CLOAK = 3247831;
	private static final int EX_SAYAHA_CLOAK_PROTECTION = 3247832;
	private static final int EX_TALISMAN = 3247833;
	private static final int EX_AGATHION_BRACELET = 3247834;
	private static final int EX_AGATHION_SPIRIT = 3247835;
	private static final int EX_PENDANT = 3247836;
	private static final int EX_BUFF_SCROLL = 3247837;
	private static final int EX_SOULSHOT = 3247838;
	
	private final TaxZone _taxZone = null;
	
	private GameAssistant()
	{
		addStartNpc(MERCHANT);
		addFirstTalkId(MERCHANT);
		addTalkId(MERCHANT);
		addSpawnId(MERCHANT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (player == null)
		{
			return null;
		}
		
		String htmltext = null;
		if (event.startsWith("multisell"))
		{
			final String fullBypass = event.replace("multisell ", "");
			final int multisellId = Integer.parseInt(fullBypass);
			MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		}
		if (event.startsWith("Buy"))
		{
			final String fullBypass = event.replace("Buy ", "");
			final int value = Integer.parseInt(fullBypass);
			final ProductList buyList = BuyListData.getInstance().getBuyList(value);
			player.setInventoryBlockingStatus(true);
			player.sendPacket(new BuyList(buyList, player, getCastleTaxRate(TaxType.BUY)));
			player.sendPacket(new ExBuySellList(player, false));
		}
		switch (event)
		{
			case "get_luna":
			{
				if (checkDB(player))
				{
					if (player.getClassId().level() < 2)
					{
						player.sendMessage("2차 전직을 완료한 캐릭터만 받을 수 있습니다.");
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "2차 전직을 완료한 캐릭터만 받을 수 있습니다."));
						break;
					}
					else if (player.getVariables().getInt("DailyLuna", 0) < 1)
					{
						player.sendMessage("접속시간이 부족합니다. 5시간 이상 접속 후 받을 수 있습니다.");
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "접속시간이 부족합니다. 5시간 이상 접속 후 받을 수 있습니다."));
						break;
					}
					player.addItem("데일리루나", 41000, Rnd.getR(100, 150), player, true);
					insertDB(player);
				}
				else
				{
					player.sendMessage("루나지급은 하루에 한번만 지급됩니다. (ip 및 PC)");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "루나지급은 하루에 한번만 지급됩니다. (ip 및 PC)"));
					insertDB(player);
					break;
				}
				break;
			}
			case "package_deposit":
			{
				if (player.getAccountChars().size() < 1)
				{
					player.sendPacket(SystemMessageId.THAT_CHARACTER_DOES_NOT_EXIST);
				}
				else
				{
					player.sendPacket(new PackageToList(player.getAccountChars()));
				}
				break;
			}
			case "package_withdraw":
			{
				final PlayerFreight freight = player.getFreight();
				if ((freight != null) && (freight.getSize() > 0))
				{
					player.setActiveWarehouse(freight);
					for (Item i : player.getActiveWarehouse().getItems())
					{
						if (i.isTimeLimitedItem() && (i.getRemainingTime() <= 0))
						{
							player.getActiveWarehouse().destroyItem("ItemInstance", i, player, null);
						}
					}
					player.sendPacket(new WareHouseWithdrawalList(1, player, WareHouseWithdrawalList.FREIGHT));
					player.sendPacket(new WareHouseWithdrawalList(2, player, WareHouseWithdrawalList.FREIGHT));
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
				}
				break;
			}
			case "sellAll":
			{
				boolean sell = false;
				long total = 0;
				
				for (Item item : player.getInventory().getItems())
				{
					long Price = item.getReferencePrice();
					long count = item.getCount();
					
					if (item.isEquipped())
					{
						continue;
					}
					if (item.getName() == null)
					{
						continue;
					}
					
					if (Config.NO_SELL_ALL_ITEM_IDS.contains(item.getId()) || //
						((item.getName() != null) && Config.NO_SELL_ALL_ITEM_NAMES.stream().anyMatch(name -> item.getName().matches(".*\\b" + Pattern.quote(name) + "\\b.*"))))
					{
						continue;
					}
					
					if ((item.getItemType() != EtcItemType.MATERIAL) && (item.getItemType() != EtcItemType.RECIPE))
					{
						continue;
					}
					
					player.destroyItem("한방판매", item, count, player, true);
					player.addItem("한방판매", 57, (long) ((Price * count) / 2.5), null, false);
					
					total += (Price * count) / 2.5;
					
					if (count > 0)
					{
						sell = true;
					}
				}
				String adena = Util.formatAdena(total);
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/index.htm");
				player.sendMessage(sell ? "모든 잡템을 판매하여 " + adena + " 아데나를 획득했습니다." : "판매하실 잡템이 없습니다!");
				break;
			}
			case "back":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/32478.html");
				break;
			}
			case "index":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/index.htm");
				break;
			}
			case "buyItems":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/buyItems.htm");
				break;
			}
			case "buyEtc":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/buyEtc.htm");
				break;
			}
			case "blacksmith":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/blacksmith.htm");
				break;
			}
			case "itemCreate":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/ItemCreate.htm");
				break;
			}
			case "weaponCreate":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/weaponCreate.htm");
				break;
			}
			case "ArmorCreate":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/ArmorCreate.htm");
				break;
			}
			case "warehouse":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/warehouse.htm");
				break;
			}
			case "symbolmaker":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/symbolmaker.htm");
				break;
			}
			case "sellAllitems":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/sellAllitems.htm");
				break;
			}
			case "Draw":
			{
				player.sendPacket(new HennaEquipList(player));
				break;
			}
			case "RemoveDraw":
			{
				player.sendPacket(new HennaRemoveList(player));
				break;
			}
			case "buyDraw":
			{
				MultisellData.getInstance().separateAndSend(YUL_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "DepositP":
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.setActiveWarehouse(player.getWarehouse());
				player.setInventoryBlockingStatus(true);
				player.sendPacket(new WareHouseDepositList(1, player, WareHouseDepositList.PRIVATE));
				player.sendPacket(new WareHouseDepositList(2, player, WareHouseDepositList.PRIVATE));
				break;
			}
			case "SellItem":
			{
				player.sendPacket(new BuyList(BuyListData.getInstance().getBuyList(423), player, 0));
				player.sendPacket(new ExBuySellList(player, false));
				break;
			}
			case "WithdrawP":
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.setActiveWarehouse(player.getWarehouse());
				
				if (player.getActiveWarehouse().getSize() == 0)
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
					break;
				}
				
				player.sendPacket(new WareHouseWithdrawalList(1, player, WareHouseWithdrawalList.PRIVATE));
				player.sendPacket(new WareHouseWithdrawalList(2, player, WareHouseWithdrawalList.PRIVATE));
				break;
			}
			case "DepositC":
			{
				if (player.getClan() != null)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.setActiveWarehouse(player.getClan().getWarehouse());
					player.setInventoryBlockingStatus(true);
					player.sendPacket(new WareHouseDepositList(1, player, WareHouseDepositList.CLAN));
					player.sendPacket(new WareHouseDepositList(2, player, WareHouseDepositList.CLAN));
				}
				else
				{
					player.sendMessage("혈맹이 없습니다.");
				}
				break;
			}
			case "WithdrawC":
			{
				if (player.getClan() != null)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					
					if (!player.hasClanPrivilege(ClanPrivilege.CL_VIEW_WAREHOUSE))
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE);
						break;
					}
					
					player.setActiveWarehouse(player.getClan().getWarehouse());
					
					if (player.getActiveWarehouse().getSize() == 0)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
						break;
					}
					
					for (Item i : player.getActiveWarehouse().getItems())
					{
						if (i.isTimeLimitedItem() && (i.getRemainingTime() <= 0))
						{
							player.getActiveWarehouse().destroyItem("ItemInstance", i, player, null);
						}
					}
					
					player.sendPacket(new WareHouseWithdrawalList(1, player, WareHouseWithdrawalList.CLAN));
					player.sendPacket(new WareHouseWithdrawalList(2, player, WareHouseWithdrawalList.CLAN));
				}
				else
				{
					player.sendMessage("혈맹이 없습니다.");
				}
				break;
			}
			case "attendance_rewards":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/enhancement.html");
				break;
			}
			case "shop":
			{
				MultisellData.getInstance().separateAndSend(ATTENDANCE_REWARD_MULTISELL, player, null, false);
				break;
			}
			// Bypass
			case "Chat_Enhancement":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/enhancement.html");
				break;
			}
			case "Chat_Events":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/events.html");
				break;
			}
			case "Chat_Items":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/items.html");
				break;
			}
			case "Chat_RemoveAug":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/removeaug.html");
				break;
			}
			case "Chat_SoulCrystals":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/soulcrystals.html");
				break;
			}
			case "Chat_ItemConversion":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/itemconversion.html");
				break;
			}
			case "Chat_TransferItem":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/transferitem.html");
				break;
			}
			case "Chat_Redeem":
			{
				player.sendMessage("There are no more dimensional items to be found.");
				break;
			}
			case "Chat_Weapons":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/weapons.html");
				break;
			}
			case "Chat_Cloaks":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/cloaks.html");
				break;
			}
			case "Chat_ProtectionCloaks":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/protectioncloaks.html");
				break;
			}
			case "Chat_ProtectionCloaks_Black":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/blackprotectioncloaks.html");
				break;
			}
			case "Chat_ProtectionCloaks_White":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/whiteprotectioncloaks.html");
				break;
			}
			case "Chat_ProtectionCloaks_Red":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/redprotectcloaks.html");
				break;
			}
			case "Chat_Talismans":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/talismans.html");
				break;
			}
			case "Chat_Agathions":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/agathions.html");
				break;
			}
			case "Chat_Pendants":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/pendants.html");
				break;
			}
			case "Chat_BuffScrolls":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/buffscrolls.html");
				break;
			}
			case "Chat_Soulshots":
			{
				htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/soulshots.html");
				break;
			}
			// Actions
			case "Augment":
			{
				player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
				break;
			}
			case "removeAug":
			{
				player.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
				break;
			}
			case "insertSoulCrystals":
			{
				player.sendPacket(ExShowEnsoulWindow.STATIC_PACKET);
				break;
			}
			case "extractSoulCrystals":
			{
				player.sendPacket(ExShowEnsoulExtractionWindow.STATIC_PACKET);
				break;
			}
			// Multisell
			case "SigelSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(SIGEL_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "TyrrSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(TYRR_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "OthellSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(OTHELL_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "YulSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(YUL_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "FeohSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(FEOH_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "IssSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(ISS_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "WynnSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(WYNN_SOUL_CRYSTAL, player, null, false);
				break;
			}
			case "AeoreSC_Shop":
			{
				MultisellData.getInstance().separateAndSend(AEORE_SOUL_CRYSTAL, player, null, false);
				break;
			}
			
			case "Ex_Sayha_BlessingShop":
			{
				MultisellData.getInstance().separateAndSend(EX_SAYHA_BLESSING_SHOP, player, null, false);
				break;
			}
			case "EX_GiranSealsShop":
			{
				MultisellData.getInstance().separateAndSend(EX_GIRAN_SEALS_SHOP, player, null, false);
				break;
			}
			case "Ex_Doll7DayShop":
			{
				MultisellData.getInstance().separateAndSend(EX_DOLL_7DAYS_SHOP, player, null, false);
				break;
			}
			case "Ex_BossWeapFragShop":
			{
				MultisellData.getInstance().separateAndSend(EX_BOSS_WEAPON_SHOP, player, null, false);
				break;
			}
			case "Ex_MysteriousLv2Shop":
			{
				MultisellData.getInstance().separateAndSend(EX_MYSTERIUS_LEVEL2, player, null, false);
				break;
			}
			case "Ex_MysteriousLv3Shop":
			{
				MultisellData.getInstance().separateAndSend(EX_MYSTERIUS_LEVEL3, player, null, false);
				break;
			}
			case "Ex_MysteriousLv4Shop":
			{
				MultisellData.getInstance().separateAndSend(EX_MYSTERIUS_LEVEL4, player, null, false);
				break;
			}
			case "Ex_MysteriousLv5Shop":
			{
				MultisellData.getInstance().separateAndSend(EX_MYSTERIUS_LEVEL5, player, null, false);
				break;
			}
			case "Ex_MysteriousLv6Shop":
			{
				MultisellData.getInstance().separateAndSend(EX_MYSTERIUS_LEVEL6, player, null, false);
				break;
			}
			case "Ex_MysteriousLv7Shop":
			{
				MultisellData.getInstance().separateAndSend(EX_MYSTERIUS_LEVEL7, player, null, false);
				break;
			}
			case "Ex_MysteriousLv8Shop":
			{
				MultisellData.getInstance().separateAndSend(EX_MYSTERIUS_LEVEL8, player, null, false);
				break;
			}
			case "Ex_HeavyAGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_HEAVY_A_GRADE, player, null, false);
				break;
			}
			case "Ex_LightAGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_LIGHT_A_GRADE, player, null, false);
				break;
			}
			case "Ex_RobeAgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_ROBE_A_GRADE, player, null, false);
				break;
			}
			case "Ex_WeaponAgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_WEAPON_A_GRADE, player, null, false);
				break;
			}
			case "Ex_SpecialAgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_SPECIAL_A_GRADE, player, null, false);
				break;
			}
			case "Ex_HeavyBGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_HEAVY_B_GRADE, player, null, false);
				break;
			}
			case "Ex_LightBGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_LIGHT_B_GRADE, player, null, false);
				break;
			}
			case "Ex_RobeBgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_ROBE_B_GRADE, player, null, false);
				break;
			}
			case "Ex_WeaponBgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_WEAPON_B_GRADE, player, null, false);
				break;
			}
			case "Ex_WeaponCgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_WEAPON_C_GRADE, player, null, false);
				break;
			}
			case "Ex_SayhaCloak":
			{
				MultisellData.getInstance().separateAndSend(EX_SAYHA_CLOAK, player, null, false);
				break;
			}
			case "Ex_SayhaProtection":
			{
				MultisellData.getInstance().separateAndSend(EX_SAYAHA_CLOAK_PROTECTION, player, null, false);
				break;
			}
			case "Ex_Talisman":
			{
				MultisellData.getInstance().separateAndSend(EX_TALISMAN, player, null, false);
				break;
			}
			case "Ex_AgathionBracelet":
			{
				MultisellData.getInstance().separateAndSend(EX_AGATHION_BRACELET, player, null, false);
				break;
			}
			case "Ex_AgathionSpirit":
			{
				MultisellData.getInstance().separateAndSend(EX_AGATHION_SPIRIT, player, null, false);
				break;
			}
			case "Ex_Pendant":
			{
				MultisellData.getInstance().separateAndSend(EX_PENDANT, player, null, false);
				break;
			}
			case "Ex_BuffScroll":
			{
				MultisellData.getInstance().separateAndSend(EX_BUFF_SCROLL, player, null, false);
				break;
			}
			case "Ex_Soulshot":
			{
				MultisellData.getInstance().separateAndSend(EX_SOULSHOT, player, null, false);
				break;
			}
			case "exc_black_sayha_cloak":
			{
				final long itemCount = getQuestItemsCount(player, SAYHA_CLOAK_COUPON);
				if (itemCount < 1)
				{
					htmltext = HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html");
					return null;
				}
				takeItems(player, SAYHA_CLOAK_COUPON, 1);
				giveItems(player, BLACK_SAYHA_CLOAK, 1);
				break;
			}
			case "SPAM_TEXT":
			{
				int say = Rnd.get(1, 8);
				int min = Rnd.get(1, 5);
				int sec = Rnd.get(5, 30) * 1000;
				switch (say)
				{
					case 1:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저1);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 2:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저2);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 3:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저3);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 4:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저4);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 5:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저5);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 6:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저6);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 7:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저7);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
					case 8:
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.비타민매니저8);
						startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
						break;
				}
				break;
			}
		}
		return htmltext;
	}
	
	public Castle getTaxCastle()
	{
		return (_taxZone != null) ? _taxZone.getCastle() : null;
	}
	
	public double getCastleTaxRate(TaxType type)
	{
		final Castle castle = getTaxCastle();
		return (castle != null) ? (castle.getTaxPercent(type) / 100.0) : 0;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		int min = Rnd.get(3, 8);
		int sec = Rnd.get(5, 30) * 1000;
		startQuestTimer("SPAM_TEXT", (min * 60 * 1000) + sec, npc, null);
		return super.onSpawn(npc);
	}
	
	private boolean checkDB(Player player)
	{
		String query = "SELECT * FROM daily_luna WHERE account = ? OR ip = ? OR hwid = ? OR e_mail = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, player.getAccountName());
			ps.setString(2, player.getIPAddress());
			ps.setString(3, player.getHWID());
			ps.setString(4, player.getEmail());
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return false;
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "DB를 검사할 수 없습니다.", e);
		}
		return true;
	}
	
	private boolean checkAccount(Player player)
	{
		String query = "SELECT * FROM daily_luna WHERE account = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, player.getAccountName());
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return false;
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "계정을 검사할 수 없습니다.", e);
		}
		return true;
	}
	
	private boolean checkEmail(Player player)
	{
		String query = "SELECT * FROM daily_luna WHERE e_mail = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, player.getEmail());
			
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					return false;
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "이메일을 검사할 수 없습니다.", e);
		}
		return true;
	}
	
	private void insertDB(Player player)
	{
		if (checkAccount(player) && checkEmail(player))
		{
			String query = "INSERT INTO daily_luna (account, ip, hwid, e_mail) VALUES (?, ?, ?, ?)";
			
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement(query))
			{
				ps.setString(1, player.getAccountName());
				ps.setString(2, player.getIPAddress());
				ps.setString(3, player.getHWID());
				ps.setString(4, player.getEmail());
				ps.execute();
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "DB에 데이터를 삽입할 수 없습니다.", e);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new GameAssistant();
	}
}