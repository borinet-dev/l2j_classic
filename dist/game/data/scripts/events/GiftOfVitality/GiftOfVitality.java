package events.GiftOfVitality;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author 보리넷 가츠
 */
public class GiftOfVitality extends LongTimeEvent
{
	// NPC
	private static final int STEVE_SHYAGEL = 4306;
	
	// Misc
	private static final int HOURS = 3; // Reuse between buffs
	private static final String REUSE = GiftOfVitality.class.getSimpleName() + "_reuse";
	private static final int CUBIC_LOWEST = 1;
	private static final int CUBIC_HIGHEST = 86;
	
	private GiftOfVitality()
	{
		addStartNpc(STEVE_SHYAGEL);
		addFirstTalkId(STEVE_SHYAGEL);
		addTalkId(STEVE_SHYAGEL);
	}
	
	// Buffs
	private static final SkillHolder CUBIC = new SkillHolder(4338, 1);
	private static final SkillHolder[] CHARACTER_BUFFS =
	{
		new SkillHolder(30235, 1), // 아큐맨
		new SkillHolder(30233, 1), // 헤이스트
		new SkillHolder(30234, 1), // 가이던스
		new SkillHolder(30236, 1), // 그레이트 마이트
		new SkillHolder(30237, 1), // 그레이트 실드
		new SkillHolder(30239, 1), // 챈트 오브 빅토리
		new SkillHolder(30240, 1), // 와일드 매직
		new SkillHolder(30241, 1), // 버서커 스피릿
		new SkillHolder(30242, 1), // 임프로브 컴뱃
		new SkillHolder(30243, 1), // 임프로브 실드 디펜스
		new SkillHolder(30244, 1), // 임프로브 매직
		new SkillHolder(30245, 1), // 임프로브 컨디션
		new SkillHolder(30246, 1), // 임프로브 실드 크리티컬
		new SkillHolder(30247, 1), // 임프로브 무브먼트
		new SkillHolder(30248, 1), // 클레리티
		new SkillHolder(30249, 1), // 블레싱 오브 노블레스
		new SkillHolder(4338, 1), // CUBIC
	};
	private static final SkillHolder[] SUMMON_BUFFS =
	{
		new SkillHolder(30235, 1), // 아큐맨
		new SkillHolder(30237, 1), // 그레이트 실드
		new SkillHolder(30240, 1), // 와일드 매직
		new SkillHolder(4325, 1), // Vampiric Rage
		new SkillHolder(30233, 1), // 헤이스트
		new SkillHolder(30234, 1), // 가이던스
		new SkillHolder(30236, 1), // 그레이트 마이트
		new SkillHolder(30239, 1), // 챈트 오브 빅토리
		new SkillHolder(30241, 1), // 버서커 스피릿
		new SkillHolder(30242, 1), // 임프로브 컴뱃
		new SkillHolder(30243, 1), // 임프로브 실드 디펜스
		new SkillHolder(30244, 1), // 임프로브 매직
		new SkillHolder(30245, 1), // 임프로브 컨디션
		new SkillHolder(30246, 1), // 임프로브 실드 크리티컬
		new SkillHolder(30247, 1), // 임프로브 무브먼트
		new SkillHolder(30248, 1), // 클레리티
		new SkillHolder(30249, 1), // 블레싱 오브 노블레스
	};
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (player == null)
		{
			return null;
		}
		
		String htmltext = event;
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage("저주받은 무기를 소유한 상태에서는 사용할 수 없습니다.");
			return null;
		}
		switch (event)
		{
			case "vitalityFree":
			{
				final long reuse = player.getVariables().getLong(REUSE, 0);
				if (reuse > System.currentTimeMillis())
				{
					final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
					final int hours = (int) (remainingTime / 3600);
					final int minutes = (int) ((remainingTime % 3600) / 60);
					final SystemMessage sm = new SystemMessage(SystemMessageId.AKISUS_BUFF_WILL_WILL_BE_AVAILABLE_FOR_RE_USE_AFTER_S1_HOUR_S_S2_MINUTE_S);
					sm.addInt(hours);
					sm.addInt(minutes);
					player.sendPacket(sm);
					htmltext = getHtm(player, "4306-notime.htm");
					htmltext = htmltext.replace("%hours%", Integer.toString(hours));
					htmltext = htmltext.replace("%mins%", Integer.toString(minutes));
				}
				else
				{
					final Skill GiftOfVitality = SkillData.getInstance().getSkill(23179, 1);
					GiftOfVitality.applyEffects(player, player, false, 7200);
					player.getVariables().set(REUSE, System.currentTimeMillis() + (HOURS * 3600000));
					htmltext = "4306-okvitality.htm";
				}
				break;
			}
			case "vitality":
			{
				if (getQuestItemsCount(player, 57) < 10000000)
				{
					int fee = 10000000;
					player.sendMessage("버프를 받기 위해서는 " + Util.formatAdena(fee) + " 아데나가 필요합니다.");
					htmltext = getHtm(player, "4306-noAdena.htm");
					htmltext = htmltext.replace("%fee%", Util.formatAdena(fee));
				}
				else
				{
					takeItems(player, 57, 10000000);
					final Skill GiftOfVitality = SkillData.getInstance().getSkill(23179, 1);
					GiftOfVitality.applyEffects(player, player, false, 3600);
					htmltext = "4306-okfee.htm";
				}
				break;
			}
			case "give_cloak":
			{
				if (player.getVariables().getInt("NEWBIE_CLOAK", 0) > 0)
				{
					htmltext = "4306-noCloak.htm";
				}
				else
				{
					player.getVariables().set("NEWBIE_CLOAK", 1);
					final Item createditem = ItemTemplate.createItem(47157);
					player.addItem("NEWBIE_CLOAK", createditem, null, true);
					player.getInventory().equipItem(createditem);
					final InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(createditem);
					player.sendInventoryUpdate(playerIU);
					
					htmltext = "4306-Cloak.htm";
				}
				break;
			}
			case "give_buff":
			{
				final int level = player.getLevel();
				if (getQuestItemsCount(player, 57) < 2500000)
				{
					int fee = 2500000;
					player.sendMessage("버프를 받기 위해서는 " + Util.formatAdena(fee) + " 아데나가 필요합니다.");
					htmltext = getHtm(player, "4306-noAdena.htm");
					htmltext = htmltext.replace("%fee%", Util.formatAdena(fee));
				}
				else
				{
					npc.setTarget(player);
					takeItems(player, 57, 2300000);
					for (SkillHolder skill : CHARACTER_BUFFS)
					{
						SkillCaster.triggerCast(player, player, skill.getSkill());
					}
					
					if ((level >= CUBIC_LOWEST) && (level <= CUBIC_HIGHEST))
					{
						SkillCaster.triggerCast(player, player, CUBIC.getSkill());
					}
					htmltext = "4306-okCharBuff.htm";
				}
				break;
			}
			case "pet_buff":
			{
				if (player.hasPet())
				{
					if (getQuestItemsCount(player, 57) < 1200000)
					{
						int fee = 1200000;
						player.sendMessage("펫에게 버프를 받기 위해서는 " + Util.formatAdena(fee) + " 아데나가 필요합니다.");
						htmltext = getHtm(player, "4306-noAdena.htm");
						htmltext = htmltext.replace("%fee%", Util.formatAdena(fee));
					}
					else
					{
						takeItems(player, 57, 1200000);
						List<Creature> target = new ArrayList<>();
						target.add(player.getPet());
						npc.setTarget(player.getPet());
						for (SkillHolder skill : SUMMON_BUFFS)
						{
							SkillCaster.triggerCast(npc, player.getPet(), skill.getSkill());
						}
						htmltext = "4306-okCharBuff.htm";
					}
				}
				else
				{
					htmltext = "4306-nosummon.htm";
				}
				break;
			}
			case "noblesse":
			{
				if (player.isNoble())
				{
					htmltext = "nobless-3.htm";
					break;
				}
				if (player.isMainClassActive())
				{
					htmltext = "nobless-4.htm";
					break;
				}
				if ((player.getLevel() < 76) || (player.getClassId().level() < 3))
				{
					player.sendMessage("3차전직을 완료한 캐릭터만 가능합니다!");
					htmltext = "nobless-2.htm";
					break;
				}
				if (getQuestItemsCount(player, 57) < 3500000)
				{
					int fee = 3500000;
					player.sendMessage("노블레스가 되기 위해서는 " + Util.formatAdena(fee) + " 아데나가 필요합니다.");
					htmltext = getHtm(player, "nobless-5.htm");
					htmltext = htmltext.replace("%fee%", Util.formatAdena(fee));
					break;
				}
				takeItems(player, 57, 3500000);
				giveItems(player, 7694, 1);
				player.setNoble(true);
				player.sendPacket(QuestSound.ITEMSOUND_QUEST_FINISH.getPacket());
				htmltext = "nobless-1.htm";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "4306.htm";
	}
	
	public static void main(String[] args)
	{
		new GiftOfVitality();
	}
}
