package events.CustomEvent;

import java.util.Calendar;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * @author 보리넷 가츠
 */
public class CustomEvent extends LongTimeEvent
{
	// NPC
	private static final int NPC = Config.CUSTOM_EVENT_NAME == 3 ? 40023 : (Config.CUSTOM_EVENT_NAME == 4 ? Config.CUSTOM_EVENT_NPC_ID : 34330);
	// private static final SkillHolder 아키서서의축복 = new SkillHolder(23179, 1);
	private static final int HOURS = 3; // Reuse between buffs
	private static final String REUSE = "CUSTOM_EVENT_GIFT_SCROLL_TIMES";
	
	private CustomEvent()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		int account = player.getAccountVariables().getInt("CUSTOM_EVENT_GIFT", 0);
		int box = player.getAccountVariables().getInt("CUSTOM_EVENT_BOX", 0);
		int weaponD = player.getAccountVariables().getInt("CUSTOM_EVENT_WEAPON_D", 0);
		int weaponC = player.getAccountVariables().getInt("CUSTOM_EVENT_WEAPON_C", 0);
		int weaponB = player.getAccountVariables().getInt("CUSTOM_EVENT_WEAPON_B", 0);
		int armorD = player.getAccountVariables().getInt("CUSTOM_EVENT_ARMOR_D", 0);
		int armorC = player.getAccountVariables().getInt("CUSTOM_EVENT_ARMOR_C", 0);
		int armorB = player.getAccountVariables().getInt("CUSTOM_EVENT_ARMOR_B", 0);
		int check_Chuseok = player.getAccountVariables().getInt("CHUSEOK_ITEM", 0);
		
		switch (event)
		{
			case "get_scroll":
			{
				final long reuse = player.getAccountVariables().getLong(REUSE, 0);
				if (reuse > System.currentTimeMillis())
				{
					final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
					final int hours = (int) (remainingTime / 3600);
					final int minutes = (int) ((remainingTime % 3600) / 60);
					player.sendMessage(hours + "시간 " + minutes + "분 후 구매할 수 있습니다.");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, hours + "시간 " + minutes + "분 후 구매할 수 있습니다."));
					htmltext = getHtm(player, "notime.htm");
					htmltext = htmltext.replace("%hours%", Integer.toString(hours));
					htmltext = htmltext.replace("%mins%", Integer.toString(minutes));
					htmltext = htmltext.replace("%evenNpcName%", npc.getName());
					htmltext = htmltext.replace("%eventName%", BorinetUtil.getInstance().getEventName());
				}
				else
				{
					if ((player.getInventory().getInventoryItemCount(57, -1) < 100000))
					{
						player.sendMessage("구입에 필요한 아데나가 부족합니다.");
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "구입에 필요한 아데나가 부족합니다."));
						break;
					}
					player.destroyItemByItemId("커스텀이벤트_선물스크롤", 57, 100000, player, true);
					player.addItem("커스텀이벤트_선물스크롤", 29010, 3, player, true);
					player.getAccountVariables().set(REUSE, System.currentTimeMillis() + (HOURS * 3600000));
					htmltext = getHtm(player, "ok.htm");
					htmltext = htmltext.replace("%evenNpcName%", npc.getName());
					htmltext = htmltext.replace("%eventName%", BorinetUtil.getInstance().getEventName());
				}
				break;
			}
			case "get_gift":
			{
				if (account != 1)
				{
					if (player.getClassId().level() < 1)
					{
						player.sendMessage("1차 전직을 완료한 캐릭터만 받을 수 있습니다.");
						player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "1차 전직을 완료한 캐릭터만 받을 수 있습니다."));
						break;
					}
					if ((BorinetTask.getInstance().NewYearEventStart().getTimeInMillis() <= System.currentTimeMillis()) && (BorinetTask.getInstance().NewYearEventEnd().getTimeInMillis() > System.currentTimeMillis()))
					{
						player.addItem("커스텀이벤트_선물", 47416, 1, player, true); // 새해
						player.getAccountVariables().set("CUSTOM_EVENT_GIFT", 1);
					}
					else
					{
						switch (Config.CUSTOM_EVENT_NAME)
						{
							case 1:
							{
								player.addItem("커스텀이벤트_선물", 41074, 1, player, true); // 설날
								player.getAccountVariables().set("CUSTOM_EVENT_GIFT", 1);
								break;
							}
							case 2:
							{
								player.addItem("커스텀이벤트_선물", 47823, 1, player, true); // 추석
								player.getAccountVariables().set("CUSTOM_EVENT_GIFT", 1);
								break;
							}
						}
					}
				}
				else
				{
					player.sendMessage("선물은 계정당 한번만 받을 수 있습니다.");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "선물은 계정당 한번만 받을 수 있습니다."));
				}
				break;
			}
			case "make_box":
			{
				if (account == 1)
				{
					sendMessages(player, "선물상자 제작은 계정당 한번만 받을 수 있습니다.");
				}
				else
				{
					if (player.getClassId().level() < 1)
					{
						sendMessages(player, "1차 전직을 완료한 캐릭터만 제작할 수 있습니다.");
						break;
					}
					if (player.getInventory().getInventoryItemCount(57, -1) < 10000000)
					{
						sendMessages(player, "제작에 필요한 아데나가 부족합니다.");
						break;
					}
					int box_id = Config.CUSTOM_EVENT_NAME == 4 ? 41274 : 41240; // box_id 설정
					destroyAndAddItem(player, 57, 10000000, box_id, "커스텀이벤트_선물", "CUSTOM_EVENT_GIFT");
				}
				break;
			}
			case "may_box":
			{
				if (box == 1)
				{
					sendMessages(player, "선물상자 제작은 계정 및 해당 PC에서 하루에 한번만 제작됩니다.");
				}
				else
				{
					if (player.getClassId().level() < 1)
					{
						sendMessages(player, "1차 전직을 완료한 캐릭터만 제작할 수 있습니다.");
						break;
					}
					if (player.getInventory().getInventoryItemCount(57, -1) < 10000000)
					{
						sendMessages(player, "제작에 필요한 아데나가 부족합니다.");
						break;
					}
					int box_id = 41240;
					destroyAndAddItem(player, 57, 10000000, box_id, "가정의 달 기념 선물 상자", "CUSTOM_EVENT_BOX");
				}
				break;
			}
			case "get_weaponD":
			{
				if ((player.getInventory().getInventoryItemCount(41000, -1) < 100))
				{
					sendMessages(player, "구입에 필요한 루나가 부족합니다.");
					break;
				}
				if (weaponD == 1)
				{
					sendMessages(player, "무기 구매는 계정당 1회만 받을 수 있습니다.");
					break;
				}
				sendItem(player, 41000, 100, 41017, "커스텀이벤트_무기구매D", "CUSTOM_EVENT_WEAPON_D");
				break;
			}
			case "get_weaponC":
			{
				if ((player.getInventory().getInventoryItemCount(41000, -1) < 150))
				{
					sendMessages(player, "구입에 필요한 루나가 부족합니다.");
					break;
				}
				if (weaponC == 1)
				{
					sendMessages(player, "무기 구매는 계정당 1회만 받을 수 있습니다.");
					break;
				}
				sendItem(player, 41000, 150, 41023, "커스텀이벤트_무기구매C", "CUSTOM_EVENT_WEAPON_C");
				break;
			}
			case "get_weaponB":
			{
				if ((player.getInventory().getInventoryItemCount(41000, -1) < 200))
				{
					sendMessages(player, "구입에 필요한 루나가 부족합니다.");
					break;
				}
				if (weaponB == 1)
				{
					sendMessages(player, "무기 구매는 계정당 1회만 받을 수 있습니다.");
					break;
				}
				sendItem(player, 41000, 200, 41061, "커스텀이벤트_무기구매B", "CUSTOM_EVENT_WEAPON_B");
				break;
			}
			case "get_armorD":
			{
				if ((player.getInventory().getInventoryItemCount(41000, -1) < 100))
				{
					sendMessages(player, "구입에 필요한 루나가 부족합니다.");
					break;
				}
				if (armorD == 1)
				{
					sendMessages(player, "무기 구매는 계정당 1회만 받을 수 있습니다.");
					break;
				}
				sendItem(player, 41000, 100, 41018, "커스텀이벤트_방어구구매D", "CUSTOM_EVENT_ARMOR_D");
				break;
			}
			case "get_armorC":
			{
				if ((player.getInventory().getInventoryItemCount(41000, -1) < 150))
				{
					sendMessages(player, "구입에 필요한 루나가 부족합니다.");
					break;
				}
				if (armorC == 1)
				{
					sendMessages(player, "무기 구매는 계정당 1회만 받을 수 있습니다.");
					break;
				}
				sendItem(player, 41000, 150, 41024, "커스텀이벤트_방어구구매C", "CUSTOM_EVENT_ARMOR_C");
				break;
			}
			case "get_armorB":
			{
				if ((player.getInventory().getInventoryItemCount(41000, -1) < 200))
				{
					sendMessages(player, "구입에 필요한 루나가 부족합니다.");
					break;
				}
				if (armorB == 1)
				{
					sendMessages(player, "무기 구매는 계정당 1회만 받을 수 있습니다.");
					break;
				}
				sendItem(player, 41000, 200, 41062, "커스텀이벤트_방어구구매B", "CUSTOM_EVENT_ARMOR_B");
				break;
			}
			case "exchange_yut":
			{
				int requiredYutRocks = 4; // 필요한 윷가락 개수
				int exchangeRate = 4; // 교환 비율
				int yutRockItemId = 41263; // 윷가락 아이템 ID
				int exchangedItemId = 41262; // 교환된 아이템 ID
				
				long yutRockCount = player.getInventory().getInventoryItemCount(yutRockItemId, -1); // 윷가락 아이템 개수
				
				if (yutRockCount < requiredYutRocks)
				{
					player.sendMessage("윷가락이 부족합니다.");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "윷가락이 부족합니다."));
					break;
				}
				
				// 4:1 비율에 맞춰 교환 진행
				long exchangeCount = yutRockCount / exchangeRate; // 교환할 개수
				long remainingYutRocks = yutRockCount % exchangeRate; // 남은 윷가락 개수
				
				// 윷가락 아이템 제거 및 교환 아이템 추가
				player.destroyItemByItemId("윷놀이", yutRockItemId, exchangeCount * exchangeRate, player, true);
				player.addItem("윷놀이", exchangedItemId, exchangeCount, player, true);
				
				// 남은 윷가락 개수를 메시지로 알림
				if (remainingYutRocks > 0)
				{
					player.sendMessage("윷가락 " + remainingYutRocks + "개가 남았습니다.");
				}
				break;
			}
			case "give_buff":
			{
				player.getActingPlayer().getVariables().set("CHUSEOK_BUFF", 1);
				final Skill fullMoon = SkillData.getInstance().getSkill(30296, 1);
				fullMoon.applyEffects(player, player, false, 10);
				break;
			}
			case "give_item":
			{
				if (check_Chuseok == 1)
				{
					player.sendMessage("오늘은 이미 아이템을 받았습니다. 내일 다시 시도해 주세요.");
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "오늘은 이미 아이템을 받았습니다. 내일 다시 시도해 주세요."));
				}
				else
				{
					player.getAccountVariables().set("CHUSEOK_ITEM", 1);
					player.addItem("한가위 선물 주머니", 41382, 3, player, true);
				}
				break;
			}
			case "buy_items":
			{
				htmltext = getHtm(player, "buy_items.htm");
				htmltext = htmltext.replace("%evenNpcName%", npc.getName());
				htmltext = htmltext.replace("%eventName%", BorinetUtil.getInstance().getEventName());
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final Calendar calendar = Calendar.getInstance();
		String htmltext = null;
		// if (!player.isAffectedBySkill(아키서서의축복))
		// {
		// SkillCaster.triggerCast(player, player, 아키서서의축복.getSkill());
		// }
		
		int customEventName = Config.CUSTOM_EVENT_NAME;
		String eventName = BorinetUtil.getInstance().getEventName();
		
		if ((BorinetTask.getInstance().NewYearEventStart().getTimeInMillis() <= System.currentTimeMillis()) && (BorinetTask.getInstance().NewYearEventEnd().getTimeInMillis() > System.currentTimeMillis()))
		{
			htmltext = getHtm(player, "34330_new.htm");
			htmltext = htmltext.replace("%year%", Integer.toString(calendar.get(Calendar.YEAR)));
		}
		else
		{
			switch (customEventName)
			{
				case 1:
					htmltext = getHtm(player, "34330_old.htm");
					htmltext = htmltext.replace("%eventName%", eventName);
					htmltext = htmltext.replace("%year%", Integer.toString(calendar.get(Calendar.YEAR)));
					break;
				case 2:
					htmltext = getHtm(player, "34330_event.htm");
					htmltext = htmltext.replace("%eventName%", eventName);
					break;
				case 3:
					htmltext = getHtm(player, "40023.htm");
					htmltext = htmltext.replace("%eventName%", eventName);
					break;
			}
		}
		
		return htmltext;
	}
	
	private void sendItem(Player player, int itemId, long itemCount, int addItem, String log, String dbKey)
	{
		player.destroyItemByItemId(log, itemId, itemCount, player, true);
		player.addItem(log, addItem, 1, player, true);
		player.getAccountVariables().set(dbKey, 1);
	}
	
	private void sendMessages(Player player, String msg)
	{
		player.sendMessage(msg);
		player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, msg));
	}
	
	private void destroyAndAddItem(Player player, int itemId, long itemCount, int addItem, String log, String dbKey)
	{
		player.destroyItemByItemId(log, itemId, itemCount, player, true);
		player.addItem(log, addItem, 1, player, true);
		player.getAccountVariables().set(dbKey, 1);
	}
	
	public static void main(String[] args)
	{
		new CustomEvent();
	}
}
