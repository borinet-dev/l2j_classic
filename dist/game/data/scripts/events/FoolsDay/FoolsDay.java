package events.FoolsDay;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLevelChanged;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Util;

public class FoolsDay extends LongTimeEvent
{
	private static final int NPC = 32639;
	private static final int FLOUR = 41261;
	private static final int REWARD = 20271;
	// BUFFS
	private static final List<SkillHolder> BUFFS = Arrays.asList(//
		new SkillHolder(2897, 1), // 바게트 허브
		new SkillHolder(2898, 1), // 슈크림 허브
		new SkillHolder(2899, 1) // 치즈 케이크 허브
	);
	
	public FoolsDay()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ("give".equalsIgnoreCase(event))
		{
			if (getQuestItemsCount(player, FLOUR) < 50)
			{
				return getHtm(player, npc.getId() + "-noMore.htm").replace("%itemCounter%", Util.formatAdena(getQuestItemsCount(player, FLOUR)));
			}
			else if (getQuestItemsCount(player, FLOUR) >= 50)
			{
				takeItems(player, FLOUR, 50);
				giveItems(player, REWARD, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				return npc.getId() + "-3.htm";
			}
		}
		if ("all".equalsIgnoreCase(event))
		{
			takeItems(player, FLOUR, getQuestItemsCount(player, FLOUR));
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			return npc.getId() + "-4.htm";
		}
		return event;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		int count = (int) getQuestItemsCount(player, FLOUR);
		boolean buff = false;
		SkillHolder selectedBuff = BUFFS.get(ThreadLocalRandom.current().nextInt(BUFFS.size()));
		
		if (getQuestItemsCount(player, FLOUR) >= 50)
		{
			if (!player.isAffectedBySkill(BUFFS.get(0)) && !player.isAffectedBySkill(BUFFS.get(1)) && !player.isAffectedBySkill(BUFFS.get(2)))
			{
				SkillCaster.triggerCast(npc, player, selectedBuff.getSkill());
			}
			return npc.getId() + "-2.htm";
		}
		else if (!player.isAffectedBySkill(BUFFS.get(0)) && !player.isAffectedBySkill(BUFFS.get(1)) && !player.isAffectedBySkill(BUFFS.get(2)))
		{
			SkillCaster.triggerCast(npc, player, selectedBuff.getSkill());
			buff = true;
		}
		
		String htmltext = getHtm(player, buff ? npc.getId() + "-1.htm" : npc.getId() + ".htm");
		String talk = (count >= 20) ? "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest FoolsDay give\">밀가루 " + count + "개를 가져왔습니다!</Button>" : "";
		return htmltext.replace("%talk%", talk);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (isFoolsDay())
		{
			if (event.getPlayer().isInCategory(CategoryType.FIRST_CLASS_GROUP) && (event.getPlayer().getLevel() >= 20))
			{
				ThreadPool.schedule(() ->
				{
					event.getPlayer().sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "만우절 이벤트가 진행 중 입니다!"));
				}, 1000);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		if (isFoolsDay())
		{
			ThreadPool.schedule(() ->
			{
				event.getPlayer().sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "만우절 이벤트가 진행 중 입니다!"));
			}, 1000);
		}
	}
	
	private boolean isFoolsDay()
	{
		return (BorinetTask.Month() == Calendar.APRIL) && (BorinetTask.Days() == 1);
	}
	
	public static void main(String[] args)
	{
		new FoolsDay();
	}
}
