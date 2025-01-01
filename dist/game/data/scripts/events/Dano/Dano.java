package events.Dano;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class Dano extends LongTimeEvent
{
	// private static final int NPC = 40031;
	
	public Dano()
	{
		// addStartNpc(NPC);
		// addFirstTalkId(NPC);
		// addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if ("give".equalsIgnoreCase(event))
		{
			int account = player.getAccountVariables().getInt("여름이벤트", 0);
			if (!BorinetUtil.getInstance().checkDB(player, "여름이벤트") || (account == 1))
			{
				player.sendMessage("선물상자는 계정 및 PC에서 한번만 받을 수 있습니다.");
			}
			else
			{
				player.getAccountVariables().set("여름이벤트", 1);
				BorinetUtil.getInstance().insertDB(player, "광복절이벤트", 0);
				player.addItem("여름이벤트", 41374, 1, player, true);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		if ("hair".equalsIgnoreCase(event))
		{
			return handleItemEvent(player);
		}
		if ("create".equalsIgnoreCase(event))
		{
			return "40031-create.htm";
		}
		if ("create_first".equalsIgnoreCase(event))
		{
			handleCreateFirst(player);
		}
		if ("create_second".equalsIgnoreCase(event))
		{
			handleCreates(player, true);
		}
		if ("create_third".equalsIgnoreCase(event))
		{
			handleCreates(player, false);
		}
		if ("create_fourth".equalsIgnoreCase(event))
		{
			handleCreateFourth(player);
		}
		if ("create_gold".equalsIgnoreCase(event))
		{
			return "40031-create_gold.htm";
		}
		if ("cgold".equalsIgnoreCase(event))
		{
			player.sendMessage("이미 지급받은 계정입니다.");
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	private String handleItemEvent(Player player)
	{
		if (getQuestItemsCount(player, 41377) > 9)
		{
			return processBuff(player);
		}
		return "40031-noItem.htm";
	}
	
	private String processBuff(Player player)
	{
		takeItems(player, 41377, 10);
		final Skill springBuff = SkillData.getInstance().getSkill(30295, 1);
		springBuff.applyEffects(player, player, false, 86400);
		
		int chance = Rnd.get(1, 100);
		int quantity = Rnd.get(1, 5);
		if (chance < 50)
		{
			player.addItem("여름이벤트", 41376, quantity, player, true);
		}
		return null;
	}
	
	private void handleCreateFirst(Player player)
	{
		if (getQuestItemsCount(player, 41375) < 3)
		{
			player.sendMessage("제작에 필요한 창포가 부족합니다.");
		}
		else if (getQuestItemsCount(player, 57) < 1250000)
		{
			player.sendMessage("제작에 필요한 아데나가 부족합니다.");
		}
		else
		{
			takeItems(player, 41375, 3);
			takeItems(player, 57, 1250000);
			
			int itemId = (Math.random() < 0.3) ? 41376 : 41377;
			player.addItem("여름이벤트", itemId, 1, player, true);
		}
	}
	
	private void handleCreates(Player player, boolean isSecond)
	{
		if (getQuestItemsCount(player, 41375) < 2)
		{
			player.sendMessage("제작에 필요한 창포가 부족합니다.");
		}
		else if (getQuestItemsCount(player, 57) < 500000)
		{
			player.sendMessage("제작에 필요한 아데나가 부족합니다.");
		}
		else
		{
			takeItems(player, 41375, 2);
			takeItems(player, 57, 500000);
			
			int itemId = (Math.random() < 0.3) ? 41376 : isSecond ? 41378 : 41379;
			player.addItem("여름이벤트", itemId, 1, player, true);
		}
	}
	
	private void handleCreateFourth(Player player)
	{
		if (getQuestItemsCount(player, 41376) < 4)
		{
			player.sendMessage("제작에 필요한 창포가 부족합니다.");
		}
		else if (getQuestItemsCount(player, 57) < 2000000)
		{
			player.sendMessage("제작에 필요한 아데나가 부족합니다.");
		}
		else
		{
			takeItems(player, 41376, 4);
			takeItems(player, 57, 2000000);
			
			player.addItem("여름이벤트", 41380, 1, player, true);
		}
	}
	
	public static void main(String[] args)
	{
		new Dano();
	}
}
