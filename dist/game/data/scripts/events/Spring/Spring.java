package events.Spring;

import java.util.Calendar;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Util;

public class Spring extends LongTimeEvent
{
	// NPC
	private static final int NPC_ID = 40024;
	// Item
	private static final int LOLLIPOP1_ID = 41258;
	private static final int LOLLIPOP2_ID = 41259;
	// Buff
	private static final int BUFF_SKILL_ID = 30273;
	private static final String BUFF_VARIABLE_NAME = "SPRING_BUFF";
	private static final int LOLLIPOP_FEE = 5000000;
	
	public Spring()
	{
		addStartNpc(NPC_ID);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ("buff".equalsIgnoreCase(event))
		{
			return handleBuffEvent(player);
		}
		else if ("item".equalsIgnoreCase(event))
		{
			return handleItemEvent(player);
		}
		else if ("main".equalsIgnoreCase(event))
		{
			return getHtm(player, npc.getId() + ".htm").replace("%fee%", Util.formatAdena(LOLLIPOP_FEE));
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return getHtm(player, npc.getId() + ".htm").replace("%fee%", Util.formatAdena(LOLLIPOP_FEE));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (isSpringEventActive())
		{
			Player player = event.getPlayer();
			player.sendMessage("봄이 오다 이벤트가 진행 중 입니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "봄이 오다 이벤트가 진행 중 입니다."));
		}
	}
	
	private String handleBuffEvent(Player player)
	{
		int checkBuff = player.getAccountVariables().getInt(BUFF_VARIABLE_NAME, 0);
		if (player.isAffectedBySkill(30273) || player.isAffectedBySkill(30274))
		{
			return "40024-noTimeBuff.htm";
		}
		if (checkBuff != 1)
		{
			applySpringBuff(player);
			return "40024-1.htm";
		}
		return "40024-noTimeBuff.htm";
	}
	
	private String handleItemEvent(Player player)
	{
		if (playerHasLollipop(player))
		{
			String itemName = getPlayerLollipopName(player);
			player.sendMessage("이미 " + itemName + " 캔디를 보유 중입니다.");
			return getHtm(player, "40024-noItem.htm").replace("%itemName%", itemName);
		}
		return processLollipopPurchase(player);
	}
	
	private void applySpringBuff(Player player)
	{
		final Skill springBuff = SkillData.getInstance().getSkill(BUFF_SKILL_ID, 1);
		springBuff.applyEffects(player, player, false, 3600);
		player.getAccountVariables().set(BUFF_VARIABLE_NAME, 1);
	}
	
	private boolean playerHasLollipop(Player player)
	{
		return (getQuestItemsCount(player, LOLLIPOP1_ID) >= 1) || (getQuestItemsCount(player, LOLLIPOP2_ID) >= 1);
	}
	
	private String getPlayerLollipopName(Player player)
	{
		return ItemNameTable.getInstance().getItemNameKor(player.getAppearance().isFemale() ? LOLLIPOP2_ID : LOLLIPOP1_ID);
	}
	
	private String processLollipopPurchase(Player player)
	{
		if (getQuestItemsCount(player, 57) < LOLLIPOP_FEE)
		{
			player.sendMessage("롤리팝 캔디를 얻기 위해서는 " + Util.formatAdena(LOLLIPOP_FEE) + " 아데나가 필요합니다.");
			return getHtm(player, "40024-noAdena.htm").replace("%fee%", Util.formatAdena(LOLLIPOP_FEE));
		}
		takeItems(player, 57, LOLLIPOP_FEE);
		int lollipopId = player.getAppearance().isFemale() ? LOLLIPOP2_ID : LOLLIPOP1_ID;
		giveItems(player, lollipopId, 1);
		String itemName = ItemNameTable.getInstance().getItemNameKor(lollipopId);
		String htmltext = getHtm(player, "40024-2.htm");
		return htmltext.replace("%itemName%", itemName);
	}
	
	private boolean isSpringEventActive()
	{
		int month = BorinetTask.Month();
		int day = BorinetTask.Days();
		
		if (((month == Calendar.MARCH) && ((day >= 1) && (day <= 31))) || // 3월 1일부터 3월 31일까지
			((month == Calendar.APRIL) && ((day >= 1) && (day <= 7))))
		{
			return true;
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new Spring();
	}
}
