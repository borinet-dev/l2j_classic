package handlers.communityboard.borinet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.events.EnterEventTimes;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.BuyList;
import org.l2jmobius.gameserver.network.serverpackets.ExAdenaInvenCount;
import org.l2jmobius.gameserver.network.serverpackets.ExBuySellList;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.BorinetTask;

public class Homepage implements IParseBoardHandler
{
	public static final String HEADER_PATH = "data/html/CommunityBoard/Custom/header.htm";
	private static final String[] COMMANDS =
	{
		"_bbshome",
		"_bbstop",
	};
	
	private static final String[] CUSTOM_COMMANDS =
	{
		"_bbsexcmultisell",
		"_bbsmultisell",
		"_bbssell",
		"_bbsbuff",
		"_bbsheal",
		"_bbsdelevel",
		"howtodonation",
		"serverinfo",
		"enterEvent",
		"getItems",
		"test"
	};
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		final List<String> commands = new ArrayList<>();
		commands.addAll(Arrays.asList(COMMANDS));
		commands.addAll(Arrays.asList(CUSTOM_COMMANDS));
		return commands.stream().filter(Objects::nonNull).toArray(String[]::new);
	}
	
	public static void giveItems(Player player, int itemId, long count)
	{
		giveItems(player, itemId, count, 0, false);
	}
	
	public static void giveItems(Player player, int itemId, long count, int enchantlevel, boolean playSound)
	{
		if (player.isSimulatingTalking())
		{
			return;
		}
		
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		final Item item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((enchantlevel > 0) && (itemId != Inventory.ADENA_ID))
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		if (playSound)
		{
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		sendItemGetMessage(player, item, count);
	}
	
	private static void sendItemGetMessage(Player player, Item item, long count)
	{
		// If item for reward is gold, send message of gold reward to client
		if (item.getId() == Inventory.ADENA_ID)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_ADENA);
			smsg.addLong(count);
			player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else if (count > 1)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
			smsg.addItemName(item);
			smsg.addLong(count);
			player.sendPacket(smsg);
		}
		else
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
			smsg.addItemName(item);
			player.sendPacket(smsg);
		}
		// send packets
		player.sendPacket(new ExUserInfoInvenWeight(player));
		player.sendPacket(new ExAdenaInvenCount(player));
	}
	
	public static void playSound(Player player, QuestSound sound)
	{
		if (player.isSimulatingTalking())
		{
			return;
		}
		player.sendPacket(sound.getPacket());
	}
	
	public static Item createItem(int itemId)
	{
		Item item = new Item(IdManager.getInstance().getNextId(), itemId);
		item.setItemLocation(ItemLocation.VOID);
		item.setCount(1L);
		
		return item;
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		boolean isCaptchaActive = player.getQuickVarB("IsCaptchaActive", false);
		if (isCaptchaActive)
		{
			player.sendMessage("보안문자 입력 중 커뮤니티 보드는 사용이 불가능합니다.");
			return false;
		}
		
		if (command.equals("_bbshome") || command.equals("_bbstop"))
		{
			BorinetHtml.getInstance().showMainHtml(player, "home");
		}
		if (command.startsWith("test"))
		{
			player.restoreEffects();
		}
		else if (command.startsWith("_bbstop;"))
		{
			final String path = command.replace("_bbstop;", "");
			if ((path.length() > 0) && path.endsWith(".htm"))
			{
				BorinetHtml.showHtml(player, path, 0, "");
			}
		}
		else if (command.startsWith("getItems:"))
		{
			final String num = command.replace("getItems:", "");
			
			int times = Integer.parseInt(num);
			int[] count = EnterEventTimes.check(player);
			int hour = count[times];
			int itemCount = 0;
			
			switch (times)
			{
				case 1:
					itemCount = 3;
					break;
				case 2:
					itemCount = 7;
					break;
				case 3:
					itemCount = 15;
					break;
				case 4:
					itemCount = 30;
					break;
			}
			
			if (hour == 0)
			{
				player.getInventory().addItem("접속보상", 41001, itemCount, player, true);
				player.sendMessage("접속보상으로 접속 코인 " + itemCount + "개를 받았습니다.");
				EnterEventTimes.update(player, times);
			}
			EnterEventTimes.index(player);
		}
		else if (command.equals("enterEvent"))
		{
			BorinetHtml.getInstance().showMainHtml(player, "home");
			EnterEventTimes.index(player);
		}
		else if (command.startsWith("_bbsmultisell"))
		{
			final String fullBypass = command.replace("_bbsmultisell;", "");
			final int multisellId = Integer.parseInt(fullBypass);
			// player.sendPacket(new ShowBoard()); <== 커뮤니티보드 닫기
			BorinetHtml.getInstance().showMainHtml(player, "home");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, false);
		}
		else if (command.startsWith("_bbsexcmultisell"))
		{
			final String fullBypass = command.replace("_bbsexcmultisell;", "");
			final int multisellId = Integer.parseInt(fullBypass);
			BorinetHtml.getInstance().showMainHtml(player, "home");
			MultisellData.getInstance().separateAndSend(multisellId, player, null, true);
		}
		else if (command.startsWith("_bbssell"))
		{
			BorinetHtml.getInstance().showMainHtml(player, "home");
			player.sendPacket(new BuyList(BuyListData.getInstance().getBuyList(423), player, 0));
			player.sendPacket(new ExBuySellList(player, false));
		}
		else if (command.startsWith("_bbsbuff"))
		{
			final String fullBypass = command.replace("_bbsbuff;", "");
			final String[] buypassOptions = fullBypass.split(";");
			final int buffCount = buypassOptions.length - 1;
			final String page = buypassOptions[buffCount];
			if (player.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < (Config.COMMUNITYBOARD_BUFF_PRICE * buffCount))
			{
				player.sendMessage("아데나가 부족합니다.");
			}
			else
			{
				player.destroyItemByItemId("CB_Buff", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_BUFF_PRICE * buffCount, player, true);
				final Pet pet = player.getPet();
				final List<Creature> targets = new ArrayList<>(4);
				targets.add(player);
				if (pet != null)
				{
					targets.add(pet);
				}
				
				player.getServitors().values().stream().forEach(targets::add);
				
				for (int i = 0; i < buffCount; i++)
				{
					final Skill skill = SkillData.getInstance().getSkill(Integer.parseInt(buypassOptions[i].split(",")[0]), Integer.parseInt(buypassOptions[i].split(",")[1]));
					if (!Config.COMMUNITY_AVAILABLE_BUFFS.contains(skill.getId()))
					{
						continue;
					}
					for (Creature target : targets)
					{
						if (skill.isSharedWithSummon() || target.isPlayer())
						{
							skill.applyEffects(player, target);
							if (Config.COMMUNITYBOARD_CAST_ANIMATIONS)
							{
								player.sendPacket(new MagicSkillUse(player, target, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
							}
						}
					}
				}
			}
			
			BorinetHtml.showHtml(player, page + ".htm", 0, "");
		}
		else if (command.startsWith("_bbsheal"))
		{
			final String page = command.replace("_bbsheal;", "");
			if (player.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < (Config.COMMUNITYBOARD_HEAL_PRICE))
			{
				player.sendMessage("아데나가 부족합니다.");
			}
			else
			{
				player.destroyItemByItemId("CB_Heal", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_HEAL_PRICE, player, true);
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				if (player.hasPet())
				{
					player.getPet().setCurrentHp(player.getPet().getMaxHp());
					player.getPet().setCurrentMp(player.getPet().getMaxMp());
					player.getPet().setCurrentCp(player.getPet().getMaxCp());
				}
				for (Summon summon : player.getServitors().values())
				{
					summon.setCurrentHp(summon.getMaxHp());
					summon.setCurrentMp(summon.getMaxMp());
					summon.setCurrentCp(summon.getMaxCp());
				}
			}
			
			BorinetHtml.showHtml(player, page + ".htm", 0, "");
		}
		else if (command.equals("_bbsdelevel"))
		{
			if (player.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < Config.COMMUNITYBOARD_DELEVEL_PRICE)
			{
				player.sendMessage("아데나가 부족합니다.");
			}
			else if (player.getLevel() == 1)
			{
				player.sendMessage("더이상 레벨다운을 할 수 없습니다!");
			}
			else
			{
				player.destroyItemByItemId("CB_Delevel", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_DELEVEL_PRICE, player, true);
				final int newLevel = player.getLevel() - 1;
				player.setExp(ExperienceData.getInstance().getExpForLevel(newLevel));
				player.getStat().setLevel((byte) newLevel);
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				player.broadcastUserInfo();
				player.checkPlayerSkills(); // Adjust skills according to new level.
				BorinetHtml.showHtml(player, "delevel/complete.htm", 0, "");
			}
		}
		else if (command.equals("howtodonation"))
		{
			BorinetHtml.getInstance().showMainHtml(player, "HowToDonate");
		}
		else if (command.equals("serverinfo"))
		{
			BorinetHtml.getInstance().showMainHtml(player, (BorinetTask.SpecialEvent() || BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck()) ? "Serverinfo_Event" : "Serverinfo");
		}
		
		return false;
	}
}
