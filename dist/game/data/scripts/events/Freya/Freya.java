package events.Freya;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.Skill;

public class Freya extends LongTimeEvent
{
	private static final int NPC = 40029;
	
	public Freya()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ("buff".equalsIgnoreCase(event))
		{
			player.getActingPlayer().getVariables().set("FREYA_BUFF", 1);
			final Skill snowFlower = SkillData.getInstance().getSkill(30279, 1);
			snowFlower.applyEffects(player, player, false, 3599);
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		htmltext = getHtm(player, npc.getId() + ".htm");
		htmltext = htmltext.replace("%charName%", player.getName());
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Freya();
	}
}
