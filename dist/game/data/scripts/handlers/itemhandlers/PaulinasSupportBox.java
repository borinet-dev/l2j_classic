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

import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;

/**
 * @author Mobius
 */
public class PaulinasSupportBox implements IItemHandler
{
	// Items
	private static final int BOX_D_GRADE = 46849;
	private static final int BOX_C_GRADE = 46850;
	private static final int BOX_A_GRADE = 46851;
	private static final int BOX_S_GRADE = 46852;
	private static final int BOX_R_GRADE = 46919;
	// Rewards
	private static final int BOX_D_HEAVY = 46837;
	private static final int BOX_D_LIGHT = 46838;
	private static final int BOX_D_ROBE = 46839;
	private static final int BOX_C_HEAVY = 46840;
	private static final int BOX_C_LIGHT = 46841;
	private static final int BOX_C_ROBE = 46842;
	private static final int BOX_A_HEAVY = 46843;
	private static final int BOX_A_LIGHT = 46844;
	private static final int BOX_A_ROBE = 46845;
	private static final int BOX_S_HEAVY = 46846;
	private static final int BOX_S_LIGHT = 46847;
	private static final int BOX_S_ROBE = 46848;
	private static final int BOX_R_HEAVY = 46924;
	private static final int BOX_R_LIGHT = 46925;
	private static final int BOX_R_ROBE = 46926;
	// D-Grade weapon rewards
	private static final int WEAPON_SWORD_D = 46791;
	private static final int WEAPON_GSWORD_D = 46792;
	private static final int WEAPON_BLUNT_D = 46793;
	private static final int WEAPON_FIST_D = 46794;
	private static final int WEAPON_BOW_D = 46795;
	private static final int WEAPON_DAGGER_D = 46796;
	private static final int WEAPON_STAFF_D = 46797;
	private static final int ARROWS_D = 1341;
	// C-Grade weapon rewards
	private static final int WEAPON_SWORD_C = 46801;
	private static final int WEAPON_GSWORD_C = 46802;
	private static final int WEAPON_BLUNT_C = 46803;
	private static final int WEAPON_FIST_C = 46804;
	private static final int WEAPON_SPEAR_C = 46805;
	private static final int WEAPON_BOW_C = 46806;
	private static final int WEAPON_DAGGER_C = 46807;
	private static final int WEAPON_STAFF_C = 46808;
	private static final int WEAPON_DUALSWORD_C = 46809;
	private static final int ARROWS_C = 1342;
	// A-Grade weapon rewards
	private static final int WEAPON_SWORD_A = 46813;
	private static final int WEAPON_GSWORD_A = 46814;
	private static final int WEAPON_BLUNT_A = 46815;
	private static final int WEAPON_FIST_A = 46816;
	private static final int WEAPON_SPEAR_A = 46817;
	private static final int WEAPON_BOW_A = 46818;
	private static final int WEAPON_DAGGER_A = 46819;
	private static final int WEAPON_STAFF_A = 46820;
	private static final int WEAPON_DUALSWORD_A = 46821;
	private static final int ARROWS_A = 1344;
	// S-Grade weapon rewards
	private static final int WEAPON_SWORD_S = 46825;
	private static final int WEAPON_GSWORD_S = 46826;
	private static final int WEAPON_BLUNT_S = 46827;
	private static final int WEAPON_FIST_S = 46828;
	private static final int WEAPON_SPEAR_S = 46829;
	private static final int WEAPON_BOW_S = 46830;
	private static final int ARROW_OF_LIGHT_S = 1345;
	private static final int WEAPON_DAGGER_S = 46831;
	private static final int WEAPON_STAFF_S = 46832;
	private static final int WEAPON_DUALSWORD_S = 46833;
	// R-Grade weapon rewards
	private static final int WEAPON_SWORD_R = 47008;
	private static final int WEAPON_SHIELD_R = 47026;
	private static final int WEAPON_GSWORD_R = 47009;
	private static final int WEAPON_BLUNT_R = 47010;
	private static final int WEAPON_FIST_R = 47011;
	private static final int WEAPON_SPEAR_R = 47012;
	private static final int WEAPON_BOW_R = 47013;
	private static final int ORICHALCUM_ARROW_R = 18550;
	private static final int WEAPON_DUALDAGGER_R = 47019;
	private static final int WEAPON_CASTER_R = 47016;
	private static final int WEAPON_SIGIL_R = 47037;
	private static final int WEAPON_DUALSWORD_R = 47018;
	
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = playable.getActingPlayer();
		final Race race = player.getOriginRace();
		final ClassId classId = player.getClassId();
		if (!player.isInventoryUnder80(false))
		{
			player.sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
			return false;
		}
		
		player.getInventory().destroyItem(getClass().getSimpleName(), item, 1, player, null);
		player.sendPacket(new InventoryUpdate(item));
		
		switch (item.getId())
		{
			case BOX_D_GRADE:
			{
				switch (race)
				{
					case HUMAN:
					case ELF:
					case DARK_ELF:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_MAGIC_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_D_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_D);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_ROGUE_GROUP, classId.getId()))
						{
							Item itema = ItemTemplate.createItem(WEAPON_DAGGER_D);
							Item itemb = ItemTemplate.createItem(WEAPON_BOW_D);
							player.addItem(getClass().getSimpleName(), BOX_D_LIGHT, 1, player, true);
							itema.setEnchantLevel(5);
							itemb.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itema, player, true);
							player.addItem(getClass().getSimpleName(), itemb, player, true);
							player.addItem(getClass().getSimpleName(), ARROWS_D, 3000, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_WARRIOR_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_D_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SWORD_D);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_KNIGHT_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_D_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_D);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
					case DWARF:
					{
						player.addItem(getClass().getSimpleName(), BOX_D_HEAVY, 1, player, true);
						Item itemw = ItemTemplate.createItem(WEAPON_BLUNT_D);
						itemw.setEnchantLevel(5);
						player.addItem(getClass().getSimpleName(), itemw, player, true);
						break;
					}
					case ORC:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_ORCM_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_D_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_D);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.LIGHT_ARMOR_CLASS, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_D_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_FIST_D);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_D_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_D);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
				}
				break;
			}
			case BOX_C_GRADE:
			{
				switch (race)
				{
					case HUMAN:
					case ELF:
					case DARK_ELF:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_C_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_BOW_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
							player.addItem(getClass().getSimpleName(), ARROWS_C, 3000, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DAGGER, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_C_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DAGGER_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DANCE, classId.getId()) || (player.getClassId() == ClassId.GLADIATOR))
						{
							player.addItem(getClass().getSimpleName(), BOX_C_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DUALSWORD_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (player.getClassId() == ClassId.WARLORD)
						{
							player.addItem(getClass().getSimpleName(), BOX_C_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SPEAR_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_WARRIOR_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_C_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SWORD_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_ENCHANTER, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_SUMMON, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_HEAL, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_C_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_C_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
					case DWARF:
					{
						player.addItem(getClass().getSimpleName(), BOX_C_HEAVY, 1, player, true);
						Item itemw = ItemTemplate.createItem(WEAPON_BLUNT_C);
						itemw.setEnchantLevel(5);
						player.addItem(getClass().getSimpleName(), itemw, player, true);
						break;
					}
					case ORC:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_ORCM_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_C_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.LIGHT_ARMOR_CLASS, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_C_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_FIST_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_C_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_C);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
				}
				break;
			}
			case BOX_A_GRADE:
			{
				switch (race)
				{
					case HUMAN:
					case ELF:
					case DARK_ELF:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_A_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_BOW_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
							player.addItem(getClass().getSimpleName(), ARROWS_A, 3000, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DAGGER, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_A_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DAGGER_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DANCE, classId.getId()) || (player.getClassId() == ClassId.GLADIATOR))
						{
							player.addItem(getClass().getSimpleName(), BOX_A_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DUALSWORD_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (player.getClassId() == ClassId.WARLORD)
						{
							player.addItem(getClass().getSimpleName(), BOX_A_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SPEAR_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_WARRIOR_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_A_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SWORD_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_ENCHANTER, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_SUMMON, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_HEAL, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_A_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_A_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
					case DWARF:
					{
						player.addItem(getClass().getSimpleName(), BOX_A_HEAVY, 1, player, true);
						Item itemw = ItemTemplate.createItem(WEAPON_BLUNT_A);
						itemw.setEnchantLevel(5);
						player.addItem(getClass().getSimpleName(), itemw, player, true);
						break;
					}
					case ORC:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_ORCM_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_A_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.LIGHT_ARMOR_CLASS, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_A_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_FIST_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_A_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_A);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
				}
				break;
			}
			case BOX_S_GRADE:
			{
				switch (race)
				{
					case HUMAN:
					case ELF:
					case DARK_ELF:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_S_LIGHT, 1, player, true);
							player.addItem(getClass().getSimpleName(), ARROW_OF_LIGHT_S, 5000, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_BOW_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DAGGER, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_S_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DAGGER_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DANCE, classId.getId()) || (player.getClassId() == ClassId.DUELIST))
						{
							player.addItem(getClass().getSimpleName(), BOX_S_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DUALSWORD_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (player.getClassId() == ClassId.DREADNOUGHT)
						{
							player.addItem(getClass().getSimpleName(), BOX_S_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SPEAR_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_WARRIOR_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_S_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SWORD_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_ENCHANTER, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_SUMMON, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_HEAL, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_S_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_S_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
					case DWARF:
					{
						player.addItem(getClass().getSimpleName(), BOX_S_HEAVY, 1, player, true);
						Item itemw = ItemTemplate.createItem(WEAPON_BLUNT_S);
						itemw.setEnchantLevel(5);
						player.addItem(getClass().getSimpleName(), itemw, player, true);
						break;
					}
					case ORC:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_ORCM_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_S_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_STAFF_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.LIGHT_ARMOR_CLASS, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_S_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_FIST_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_S_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_S);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
				}
				break;
			}
			case BOX_R_GRADE:
			{
				switch (race)
				{
					case HUMAN:
					case ELF:
					case DARK_ELF:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_R_LIGHT, 1, player, true);
							player.addItem(getClass().getSimpleName(), ORICHALCUM_ARROW_R, 10000, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_BOW_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DAGGER, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_R_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DUALDAGGER_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DANCE, classId.getId()) || (player.getClassId() == ClassId.DUELIST))
						{
							player.addItem(getClass().getSimpleName(), BOX_R_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_DUALSWORD_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (player.getClassId() == ClassId.DREADNOUGHT)
						{
							player.addItem(getClass().getSimpleName(), BOX_R_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SPEAR_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_WARRIOR_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_R_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_SWORD_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
							Item items = ItemTemplate.createItem(WEAPON_SHIELD_R);
							items.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), items, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_ENCHANTER, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_SUMMON, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId.getId()) || CategoryData.getInstance().isInCategory(CategoryType.DIVISION_HEAL, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_R_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_CASTER_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
							Item items = ItemTemplate.createItem(WEAPON_SIGIL_R);
							items.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), items, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_R_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
							Item items = ItemTemplate.createItem(WEAPON_SHIELD_R);
							items.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), items, player, true);
						}
						break;
					}
					case DWARF:
					{
						player.addItem(getClass().getSimpleName(), BOX_R_HEAVY, 1, player, true);
						Item itemw = ItemTemplate.createItem(WEAPON_BLUNT_R);
						itemw.setEnchantLevel(5);
						player.addItem(getClass().getSimpleName(), itemw, player, true);
						Item items = ItemTemplate.createItem(WEAPON_SHIELD_R);
						items.setEnchantLevel(5);
						player.addItem(getClass().getSimpleName(), items, player, true);
						break;
					}
					case ORC:
					{
						if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_ORCM_GROUP, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_R_ROBE, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_CASTER_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
							Item items = ItemTemplate.createItem(WEAPON_SIGIL_R);
							items.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), items, player, true);
						}
						else if (CategoryData.getInstance().isInCategory(CategoryType.LIGHT_ARMOR_CLASS, classId.getId()))
						{
							player.addItem(getClass().getSimpleName(), BOX_R_LIGHT, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_FIST_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						else
						{
							player.addItem(getClass().getSimpleName(), BOX_R_HEAVY, 1, player, true);
							Item itemw = ItemTemplate.createItem(WEAPON_GSWORD_R);
							itemw.setEnchantLevel(5);
							player.addItem(getClass().getSimpleName(), itemw, player, true);
						}
						break;
					}
				}
				break;
			}
		}
		return true;
	}
}
