package ai.others.NewbieGuide;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

public class NewbieGuide extends AbstractNpcAI
{
	// NPCs
	private static final int[] NEWBIE_GUIDES =
	{
		30598,
		30599,
		30600,
		30601,
		30602,
		40009,
	};
	// Items
	private static final ItemHolder SOULSHOT_REWARD = new ItemHolder(5789, 200);
	private static final ItemHolder SPIRITSHOT_REWARD = new ItemHolder(5790, 100);
	// Other
	private static final String TUTORIAL_QUEST = "Q00255_Tutorial";
	private static final String SUPPORT_MAGIC_STRING = "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Link default/SupportMagic.htm\">보조마법의 도움을 받는다</Button>";
	
	private NewbieGuide()
	{
		addStartNpc(NEWBIE_GUIDES);
		addTalkId(NEWBIE_GUIDES);
		addFirstTalkId(NEWBIE_GUIDES);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if (event.equals("0"))
		{
			if (Config.MAX_NEWBIE_BUFF_LEVEL > 0)
			{
				htmltext = npc.getId() + ".htm";
			}
			else
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, "data/scripts/ai/others/NewbieGuide/" + npc.getId() + ".htm");
				html.replace(SUPPORT_MAGIC_STRING, "");
				player.sendPacket(html);
				return htmltext;
			}
		}
		else
		{
			htmltext = npc.getId() + "-" + event + (player.isMageClass() ? "m" : "f") + ".htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final QuestState qs = player.getQuestState(TUTORIAL_QUEST);
		if ((qs != null) && !Config.DISABLE_TUTORIAL && qs.isMemoState(5))
		{
			qs.setMemoState(6);
			if (player.isMageClass() && (player.getOriginRace() != Race.ORC))
			{
				giveItems(player, SPIRITSHOT_REWARD);
				playTutorialVoice(player, "tutorial_voice_027");
			}
			else
			{
				giveItems(player, SOULSHOT_REWARD);
				playTutorialVoice(player, "tutorial_voice_026");
			}
		}
		if (Config.MAX_NEWBIE_BUFF_LEVEL > 0)
		{
			return npc.getId() + ".htm";
		}
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(player, "data/scripts/ai/others/NewbieGuide/" + npc.getId() + ".htm");
		html.replace(SUPPORT_MAGIC_STRING, "");
		player.sendPacket(html);
		return null;
	}
	
	public void playTutorialVoice(Player player, String voice)
	{
		player.sendPacket(new PlaySound(2, voice, 0, 0, player.getX(), player.getY(), player.getZ()));
	}
	
	public static void main(String[] args)
	{
		new NewbieGuide();
	}
}
