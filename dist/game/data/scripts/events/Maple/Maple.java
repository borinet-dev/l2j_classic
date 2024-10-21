package events.Maple;

import java.time.LocalDateTime;
import java.time.Month;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

public class Maple extends LongTimeEvent
{
	private static final int NPC = 40033;
	
	public Maple()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (!isMapleActive())
		{
			return null;
		}
		
		int check_Maple = player.getAccountVariables().getInt("MAPLE_ITEM", 0);
		if ("give_item".equalsIgnoreCase(event))
		{
			if (check_Maple == 1)
			{
				player.sendMessage("오늘은 이미 아이템을 받았습니다. 내일 다시 시도해 주세요.");
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "오늘은 이미 아이템을 받았습니다. 내일 다시 시도해 주세요."));
			}
			else
			{
				player.getAccountVariables().set("MAPLE_ITEM", 1);
				player.addItem("붉게 물든 가을 단풍 이벤트", 41391, 3, player, true);
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (!isMapleActive())
		{
			return null;
		}
		
		return npc.getId() + ".htm";
	}
	
	private boolean isMapleActive()
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = LocalDateTime.of(now.getYear(), Month.OCTOBER, 21, 6, 0); // 10월 21일 오전 7시
		LocalDateTime end = LocalDateTime.of(now.getYear(), Month.NOVEMBER, 4, 6, 0); // 11월 11일 오전 6시
		
		return now.isAfter(start) && now.isBefore(end);
	}
	
	public static void main(String[] args)
	{
		new Maple();
	}
}
