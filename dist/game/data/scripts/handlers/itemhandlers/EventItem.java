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

import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.ItemSkillType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.instancemanager.HandysBlockCheckerManager;
import org.l2jmobius.gameserver.instancemanager.PremiumManager;
import org.l2jmobius.gameserver.model.ArenaParticipantsHolder;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Block;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.EnchantRecord;

public class EventItem implements IItemHandler
{
	Random _random = new Random();
	// @formatter:off
	private static final int[] R_MATERIAL =
	{
		19251, 19252, 19253, 19254, 19255, 19256, 19257, 19258,
        19259, 19260, 19261, 19262, 19263, 19264, 19265, 19266, 19267,
        19203, 19204, 19205, 19206, 19207, 19208, 19209, 19211, 19212, 19213
	};
    private static final int[] ETC_MATERIAL =
    {
        5549, 5550, 5551, 5552, 5553, 5554, 9628, 9629, 9630, 9631
    };
	private static final int[] ENCHANT_STONE =
	{
		41233, 41234
	};
	private static final int[] FISHING_ITEM =
	{
		45573, 45574
	};
	private static final int[] FISHING_ITEM_POTION =
	{
		728, 1539
	};
	private static final int[] FISHING_ITEM_LETTERS =
	{
		41002, 3884, 3885, 3881, 3883, 3877, 3887, 49783
	};
	private static final int[] KAMALOKA_REWORD =
	{
		70106, 29817, 90499, 29584, 90015, 29014, 70110, 70111
	};
	private static final int[] DYES =
	{
		49995, 49996, 49997, 49998, 49999, 90000, 90001, 90002, 90003, 90004, 90005, 90006
	};
	private static final int[] RUNES =
	{
		29822, 29842, 29862, 29882, 29902, 29922, 29943, 29962
	};
	private static final int[] JEWEL =
	{
		38890, 38895, 38885, 38880, 38875, 38870, 38865, 38860,
		38850, 26502, 26497, 26486, 26481, 26476
	};
	private static final int[] 특대사탕바구니 =
	{
		14772, 14773, 14776
	};
	private static final int[] 사탕바구니 =
	{
		14771, 14775
	};
	private static final int[] 드라이아이스 =
	{
		48497, 41235, 29014, 70110, 70111, 23768, 10649
	};
	// @formatter:on
	
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		boolean used = false;
		
		final Player player = playable.getActingPlayer();
		final int itemId = item.getId();
		switch (itemId)
		{
			case 13787: // Handy's Block Checker Bond
			{
				used = useBlockCheckerItem(player, item);
				break;
			}
			case 13788: // Handy's Block Checker Land Mine
			{
				used = useBlockCheckerItem(player, item);
				break;
			}
			case 41249:
			case 41250:
			case 41251:
			{
				int days = itemId == 41249 ? 7 : (itemId == 41250 ? 15 : 30);
				PremiumManager.getInstance().addPremiumTime(player.getAccountName(), days, TimeUnit.DAYS);
				player.sendMessage(new SimpleDateFormat("yyyy.MM.dd HH:mm").format(PremiumManager.getInstance().getPremiumExpiration(player.getAccountName())) + " 까지 프리미엄 계정의 혜택을 받을 수 있습니다.");
				player.destroyItemByItemId("프리미엄 계정 쿠폰", itemId, 1, player, true);
				break;
			}
			case 41253:
			{
				player.destroyItemByItemId("R급 재료상자", 41253, 1, player, true);
				if (Rnd.chance(5))
				{
					player.addItem("R급 재료상자", ENCHANT_STONE[Rnd.get(ENCHANT_STONE.length)], 1, null, true);
				}
				else if (Rnd.chance(10))
				{
					player.addItem("R급 재료상자", R_MATERIAL[Rnd.get(R_MATERIAL.length)], 1, null, true);
				}
				else if (Rnd.chance(40))
				{
					player.addItem("R급 재료상자", ETC_MATERIAL[Rnd.get(ETC_MATERIAL.length)], Rnd.get(1, 3), null, true);
				}
				else
				{
					player.sendMessage("꽝! 다음 기회에...");
				}
				break;
			}
			case 45488:
			{
				player.destroyItemByItemId("황금 보물 상자", 45488, 1, player, true);
				if (Rnd.chance(10))
				{
					player.addItem("황금 보물 상자", FISHING_ITEM[Rnd.get(FISHING_ITEM.length)], 1, null, true);
				}
				else if (Rnd.chance(40))
				{
					player.addItem("황금 보물 상자", FISHING_ITEM_LETTERS[Rnd.get(FISHING_ITEM_LETTERS.length)], 1, null, true);
				}
				else if (Rnd.chance(50))
				{
					player.addItem("황금 보물 상자", FISHING_ITEM_POTION[Rnd.get(FISHING_ITEM_POTION.length)], Rnd.get(1, 3), null, true);
				}
				else
				{
					player.sendMessage("꽝! 다음 기회에...");
				}
				break;
			}
			case 23956:
			{
				player.destroyItemByItemId("카마로카 보상 상자", 23956, 1, player, true);
				if (Rnd.chance(0.1))
				{
					// 0 또는 1 중에서 랜덤한 숫자 생성
					int chance = _random.nextInt(2);
					// 반반 확률로 아이템 지급
					// 무기 강화석 or 방어구 강화석
					player.addItem("카마로카 보상 상자", chance == 0 ? 41233 : 41234, 1, null, true);
				}
				else if (Rnd.chance(10))
				{
					int chance = _random.nextInt(2);
					// 추가 입장권 - 카마로카
					player.addItem("카마로카 보상 상자", chance == 0 ? 13010 : 13012, 1, null, true);
				}
				else if (Rnd.chance(15))
				{
					// 카마로카 주문서 (2장 ~ 8장)
					player.addItem("카마로카 보상 상자", 41254, Rnd.get(2, 8), null, true);
				}
				else if (Rnd.chance(30))
				{
					player.addItem("카마로카 보상 상자", KAMALOKA_REWORD[Rnd.get(KAMALOKA_REWORD.length)], item.getId() == 70106 ? 1 : Rnd.get(1, 3), null, true);
				}
				else
				{
					player.addItem("카마로카 보상 상자", ETC_MATERIAL[Rnd.get(ETC_MATERIAL.length)], Rnd.get(1, 5), null, true);
				}
				break;
			}
			case 41257:
			{
				player.destroyItemByItemId("봄의 기억", 41257, 1, player, true);
				if (Rnd.chance(0.1))
				{
					// 0 또는 1 중에서 랜덤한 숫자 생성
					int chance = _random.nextInt(2);
					// 반반 확률로 아이템 지급
					// 무기 강화석 or 방어구 강화석
					player.addItem("봄의 기억", chance == 0 ? 41233 : 41234, 1, null, true);
				}
				else if (Rnd.chance(7))
				{
					player.addItem("봄의 기억", 70009, 1, null, true);
				}
				else if (Rnd.chance(10))
				{
					player.addItem("봄의 기억", DYES[Rnd.get(DYES.length)], Rnd.get(1, 2), null, true);
				}
				else if (Rnd.chance(20))
				{
					player.addItem("봄의 기억", RUNES[Rnd.get(RUNES.length)], Rnd.get(1, 2), null, true);
				}
				else if (Rnd.chance(30))
				{
					player.addItem("봄의 기억", JEWEL[Rnd.get(JEWEL.length)], 1, null, true);
				}
				else
				{
					player.addItem("봄의 기억", ETC_MATERIAL[Rnd.get(ETC_MATERIAL.length)], Rnd.get(1, 5), null, true);
				}
				break;
			}
			case 14766:
			{
				player.destroyItemByItemId("특대사탕바구니", 14766, 1, player, true);
				player.addItem("특대사탕바구니", 14769, 1, null, true);
				if (Rnd.chance(30))
				{
					player.addItem("특대사탕바구니", 특대사탕바구니[Rnd.get(특대사탕바구니.length)], 1, null, true);
				}
				break;
			}
			case 14767:
			{
				player.destroyItemByItemId("사탕바구니", 14767, 1, player, true);
				player.addItem("사탕바구니", 14769, 1, null, true);
				if (Rnd.chance(30))
				{
					player.addItem("사탕바구니", 사탕바구니[Rnd.get(사탕바구니.length)], 1, null, true);
				}
				break;
			}
			case 14768:
			{
				player.destroyItemByItemId("솔로패키지", 14768, 1, player, true);
				player.addItem("솔로패키지", 14770, 1, null, true);
				if (Rnd.chance(30))
				{
					player.addItem("솔로패키지", 14774, 1, null, true);
				}
				break;
			}
			case 41272:
			{
				player.destroyItemByItemId("어린이날기념쿠폰", 41272, 1, player, true);
				ChildrenDay(player, true);
				break;
			}
			case 41273:
			{
				player.destroyItemByItemId("어린이날복구쿠폰", 41273, 1, player, true);
				ChildrenDay(player, false);
				break;
			}
			case 41363:
			{
				player.destroyItemByItemId("프레야의 얼음 인형 상자", 41363, 1, player, true);
				int take_itemId = (Rnd.chance(60)) ? 41353 : 41364;
				player.addItem("프레야의 얼음 인형 상자", take_itemId, 1, null, true);
				break;
			}
			case 41364:
			{
				player.destroyItemByItemId("[드라이아이스]", 41364, 1, player, true);
				if (Rnd.chance(75))
				{
					player.addItem("[드라이아이스]", 드라이아이스[Rnd.get(드라이아이스.length)], Rnd.get(1, 3), null, true);
				}
				else
				{
					if (Rnd.chance(40))
					{
						player.addItem("[드라이아이스]", (Rnd.chance(55)) ? 41353 : 41249, 1, null, true);
					}
					else
					{
						player.addItem("[드라이아이스]", 48830, Rnd.get(1, 3), null, true);
					}
				}
				break;
			}
			case 41365:
			{
				EnchantRecord.getInstance().showEnchantRecords(player);
				break;
			}
			case 41367:
			{
				if (checkReuse(player, item))
				{
					int randomValue = Rnd.get(100);
					int skillId = 0;
					if (randomValue < 40)
					{
						skillId = 30286; // 40%
					}
					else if (randomValue < 65)
					{ // 40% + 25% = 65%
						skillId = 30285; // 25%
					}
					else if (randomValue < 85)
					{ // 65% + 20% = 85%
						skillId = 30284; // 20%
					}
					else
					{ // 85% + 15% = 100%
						skillId = 30283; // 15%
					}
					final Skill TransForm = SkillData.getInstance().getSkill(skillId, 1);
					
					player.destroyItemByItemId("[변신 주문서]", 41367, 1, player, true);
					SkillCaster.triggerCast(player, null, TransForm, null, false);
					player.getVariables().set(item.getId() + "_재사용시간", (System.currentTimeMillis() + item.getReuseDelay()));
					break;
				}
			}
			case 41372:
			{
				player.destroyItemByItemId("장비 강화 주문서 상자", 41372, 1, player, true);
				
				int randomValue = Rnd.get(100);
				int rewardId = 0;
				
				if (randomValue < 17)
				{
					rewardId = 22223; // 파멸의 무기 강화 주문서 - A그레이드
				}
				else if (randomValue < 34)
				{
					rewardId = 22224; // 파멸의 갑옷 강화 주문서 - A그레이드
				}
				else if (randomValue < 47)
				{
					rewardId = 6577; // 축복받은 무기 강화 주문서 - S그레이드
				}
				else if (randomValue < 60)
				{
					rewardId = 6578; // 축복받은 갑옷 강화 주문서 - S그레이드
				}
				else if (randomValue < 71)
				{
					rewardId = 19447; // 축복받은 무기 강화 주문서 - R그레이드
				}
				else if (randomValue < 81)
				{
					rewardId = 19448; // 축복받은 갑옷 강화 주문서 - R그레이드
				}
				else if (randomValue < 88)
				{
					rewardId = 41031; // 레어 액세서리 강화 주문서
				}
				else if (randomValue < 93)
				{
					rewardId = 41032; // 축복받은 레어 액세서리 강화 주문서
				}
				else if (randomValue < 96)
				{
					rewardId = 41233; // 무기 강화석
				}
				else
				{
					rewardId = 41234; // 방어구 강화석
				}
				
				player.addItem("장비 강화 주문서 상자", rewardId, 1, null, true);
				break;
			}
			case 41373:
			{
				player.destroyItemByItemId("주문서 행운 상자", 41373, 1, player, true);
				
				int randomValue = Rnd.get(100);
				int rewardId = 0;
				int rewardQuantity = 0;
				
				// 첫 4개 아이템 중 하나는 무조건 나옴
				if (randomValue < 25)
				{
					rewardId = 41368; // 공격력 강화 주문서 2
					rewardQuantity = 2;
				}
				else if (randomValue < 50)
				{
					rewardId = 41369; // 방어력 강화 주문서 2
					rewardQuantity = 2;
				}
				else if (randomValue < 75)
				{
					rewardId = 41370; // 경험치 향상 주문서 2
					rewardQuantity = 2;
				}
				else
				{
					rewardId = 41367; // 변신 주문서 1
					rewardQuantity = 1;
				}
				
				// 첫 번째 보상 아이템을 플레이어에게 추가
				player.addItem("주문서 행운 상자", rewardId, rewardQuantity, null, true);
				
				// 추가 획득 아이템
				int additionalRandomValue = Rnd.get(100);
				if (additionalRandomValue < 30)
				{
					int additionalRewardId = 41371; // 버서커 주문서 2
					int additionalRewardQuantity = 2;
					player.addItem("주문서 행운 상자", additionalRewardId, additionalRewardQuantity, null, true);
				}
				else if (additionalRandomValue < 50)
				{ // 30% + 20% = 50%
					int additionalRewardId = 41372; // 장비 강화 주문서 상자 1
					int additionalRewardQuantity = 1;
					player.addItem("주문서 행운 상자", additionalRewardId, additionalRewardQuantity, null, true);
				}
				break;
			}
			default:
			{
				LOGGER.warning("EventItemHandler: Item with id: " + itemId + " is not handled");
			}
		}
		return used;
	}
	
	private final void ChildrenDay(Player player, boolean first)
	{
		if (!check(player))
		{
			return;
		}
		
		int classId = first ? (player.getVisualClassId() == -2 ? ClassId.DWARVEN_FIGHTER.ordinal() : -2) : -2;
		String text = first ? "캐릭터 외형이 성공적으로 변경되었습니다." : "캐릭터 외형이 기존 종족으로 복구되었습니다.";
		
		changeRace(player, classId, text, first);
	}
	
	private void changeRace(Player player, int classId, String text, boolean first)
	{
		if (first && !player.getAppearance().isFemale())
		{
			int style = Rnd.get(0, 6);
			player.getVariables().set("EVENT_HAIR_STYLE", player.getAppearance().getHairStyle());
			player.getVariables().set("EVENT_SEX_TYPE", "male");
			player.getAppearance().setFemale();
			player.getAppearance().setHairStyle(style);
		}
		
		if (!first && player.getVariables().getString("EVENT_SEX_TYPE", "").equals("male"))
		{
			player.getAppearance().setMale();
			player.getAppearance().setHairStyle(hairStyle(player));
			player.getVariables().remove("EVENT_HAIR_STYLE");
			player.getVariables().remove("EVENT_SEX_TYPE");
		}
		
		player.getAppearance();
		player.setVisualClassId(ClassId.ELVEN_FIGHTER.ordinal());
		player.setVisualClassId(classId);
		player.broadcastUserInfo();
		player.decayMe();
		player.storeMe();
		player.spawnMe(player.getX(), player.getY(), player.getZ());
		player.broadcastPacket(new MagicSkillUse(player, player, 2122, 1, 1000, 0));
		player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[어린이날]", text));
	}
	
	private boolean check(Player player)
	{
		if (player.isCursedWeaponEquipped())
		{
			player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[어린이날]", "저주받은 무기를 장착한 상태에서는 불가능합니다."));
			return false;
		}
		if (player.isTransformed())
		{
			player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[어린이날]", "변신 중에는 불가능합니다."));
			return false;
		}
		if ((player.getRace() == Race.DWARF) && player.getAppearance().isFemale())
		{
			player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, "[어린이날]", "드워프 여성캐릭터는 할 필요가 없습니다."));
			return false;
		}
		return true;
	}
	
	private int hairStyle(Player player)
	{
		int hairStyle = player.getVariables().getInt("EVENT_HAIR_STYLE", 0);
		
		return hairStyle;
	}
	
	private final boolean useBlockCheckerItem(Player castor, Item item)
	{
		final int blockCheckerArena = castor.getBlockCheckerArena();
		if (blockCheckerArena == -1)
		{
			final SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			msg.addItemName(item);
			castor.sendPacket(msg);
			return false;
		}
		
		final Skill sk = item.getEtcItem().getSkills(ItemSkillType.NORMAL).get(0).getSkill();
		if (sk == null)
		{
			return false;
		}
		
		if (!castor.destroyItem("Consume", item, 1, castor, true))
		{
			return false;
		}
		
		final Block block = (Block) castor.getTarget();
		final ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(blockCheckerArena);
		if (holder != null)
		{
			final int team = holder.getPlayerTeam(castor);
			World.getInstance().forEachVisibleObjectInRange(block, Player.class, sk.getEffectRange(), pc ->
			{
				final int enemyTeam = holder.getPlayerTeam(pc);
				if ((enemyTeam != -1) && (enemyTeam != team))
				{
					sk.applyEffects(castor, pc);
				}
			});
			return true;
		}
		LOGGER.warning("Char: " + castor.getName() + "[" + castor.getObjectId() + "] has unknown block checker arena");
		return false;
	}
	
	private boolean checkReuse(Player player, Item item)
	{
		final long remainingTime = player.getVariables().getLong(item.getId() + "_재사용시간", 0);
		
		if (remainingTime > System.currentTimeMillis())
		{
			final long remaining = ((remainingTime - System.currentTimeMillis()) / 1000);
			final int hours = (int) (remaining / 3600);
			final int minutes = (int) ((remaining % 3600) / 60);
			final int seconds = (int) (remaining % 60);
			if (hours > 0)
			{
				player.sendMessage(item.getName() + "의 재사용 시간이 " + hours + "시간 " + minutes + "분 " + seconds + "초 남았습니다.");
			}
			else if (minutes > 0)
			{
				player.sendMessage(item.getName() + "의 재사용 시간이 " + minutes + "분 " + seconds + "초 남았습니다.");
			}
			else
			{
				player.sendMessage(item.getName() + "의 재사용 시간이 " + seconds + "초 남았습니다.");
			}
			return false;
		}
		return true;
	}
}
