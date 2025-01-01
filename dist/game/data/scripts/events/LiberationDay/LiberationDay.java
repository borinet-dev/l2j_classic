package events.LiberationDay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class LiberationDay extends LongTimeEvent
{
	private static final int NPC = 40031;
	
	public LiberationDay()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if ("taekuk".equalsIgnoreCase(event))
		{
			int checkGift = player.getAccountVariables().getInt("광복절이벤트", 0);
			if (!BorinetUtil.getInstance().checkDB(player, "광복절이벤트") || (checkGift == 1))
			{
				player.sendMessage("이벤트 참여는 계정 및 PC에서 한번만 할 수 있습니다.");
				return null;
			}
			return generateTaekukPage();
		}
		if ("success".equalsIgnoreCase(event))
		{
			player.sendMessage("태극기를 정확하게 찾으셨어요!");
			player.getAccountVariables().set("광복절이벤트", 1);
			BorinetUtil.getInstance().insertDB(player, "광복절이벤트", 0);
			
			player.addItem("광복절이벤트", 41365, 1, player, true);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		if ("failed".equalsIgnoreCase(event))
		{
			player.getAccountVariables().set("광복절이벤트", 1);
			BorinetUtil.getInstance().insertDB(player, "광복절이벤트", 0);
			return npc.getId() + "-1.htm";
		}
		if ("stone".equalsIgnoreCase(event))
		{
			if (getQuestItemsCount(player, 41381) < 100)
			{
				return npc.getId() + "-2.htm";
			}
			else if (getQuestItemsCount(player, 41381) >= 100)
			{
				takeItems(player, 41381, 100);
				giveItems(player, 41365, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				return npc.getId() + "-3.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	private static final int[] IMAGES =
	{
		1,
		2,
		3,
		4
	};
	
	public String generateTaekukPage()
	{
		List<Integer> imageList = new ArrayList<>();
		for (int image : IMAGES)
		{
			imageList.add(image);
		}
		// 이미지 리스트를 랜덤하게 섞기
		Collections.shuffle(imageList);
		
		StringBuilder html = new StringBuilder();
		int nCount = 0;
		
		html.append("<html><body>이벤트 진행자 초롱이:<br>").append("<center>")//
			.append("<font color=LEVEL>기회는 단 한번입니다!</font><br>");
		
		for (int randomImage : imageList)
		{
			nCount++;
			String action = (randomImage == 4) ? "bypass -h Quest LiberationDay success" : "bypass -h Quest LiberationDay failed";
			
			html.append("<table border=0 cellspacing=0 cellpadding=0 width=256 height=171 background=\"nIcon.taekuk_").append(randomImage).append("\">") //
				.append("<tr><td>").append("<button value=\" \" action=\"").append(action).append("\" width=256 height=171 back=\"nIcon.taekuk_").append(randomImage).append("\" fore=\"nIcon.taekuk_").append(randomImage).append("\"/>") //
				.append("</td></tr>") //
				.append("</table><br>") //
				.append(nCount).append("번<br>");
		}
		
		html.append("</center>") //
			.append("</body></html>");
		
		return html.toString();
	}
	
	public static void main(String[] args)
	{
		new LiberationDay();
	}
}
