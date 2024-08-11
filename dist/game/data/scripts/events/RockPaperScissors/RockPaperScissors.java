package events.RockPaperScissors;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class RockPaperScissors extends LongTimeEvent
{
	// NPC
	private static final int NPC_ID = 40026;
	// 확률을 나타내는 배열 정의
	private static final double[] PROBABILITIES =
	{
		0.6,
		0.15,
		0.25
	};
	private static final String[] OPTIONS =
	{
		"gawi",
		"bawi",
		"bo"
	};
	private static final String[] IMAGES =
	{
		"nIcon_big.xmas_gawi_i00",
		"nIcon_big.xmas_bawi_i00",
		"nIcon_big.xmas_bo_i00"
	};
	
	private static final String IMAGE_TAG = "<?img?>";
	private static final int DISPLAY_DURATION = 230; // 각 이미지를 보여주는 시간 (밀리초)
	private static final int TOTAL_DURATION = 3200; // 전체 시간 (밀리초)
	
	public RockPaperScissors()
	{
		addStartNpc(NPC_ID, 40029);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		
		switch (event)
		{
			case "start":
			{
				int 묵찌빠 = player.getAccountVariables().getInt("묵찌빠", 0);
				htmltext = getHtm(player, npc.getId() + "_start.htm");
				htmltext = htmltext.replace("<?game_count?>", Integer.toString(묵찌빠));
				break;
			}
			case "try":
			{
				switch (npc.getId())
				{
					case 40029:
					{
						if (player.getInventory().getInventoryItemCount(57, 0) >= 100000000)
						{
							player.getAccountVariables().remove("묵찌빠");
							player.destroyItemByItemId("묵찌빠", 57, 100000000, player, true);
							int 묵찌빠 = player.getAccountVariables().getInt("묵찌빠", 0);
							htmltext = getHtm(player, npc.getId() + "_start.htm");
							htmltext = htmltext.replace("<?game_count?>", Integer.toString(묵찌빠));
						}
						else
						{
							player.sendMessage("아데나가 부족합니다.");
						}
						break;
					}
					default:
					{
						if (player.getInventory().getInventoryItemCount(57, 0) >= 10000000)
						{
							player.getAccountVariables().remove("묵찌빠");
							player.destroyItemByItemId("묵찌빠", 57, 10000000, player, true);
							int 묵찌빠 = player.getAccountVariables().getInt("묵찌빠", 0);
							htmltext = getHtm(player, npc.getId() + "_start.htm");
							htmltext = htmltext.replace("<?game_count?>", Integer.toString(묵찌빠));
						}
						else
						{
							player.sendMessage("아데나가 부족합니다.");
						}
						break;
					}
				}
				break;
			}
			case "gawi":
			case "bawi":
			case "bo":
			{
				int 묵찌빠 = player.getAccountVariables().getInt("묵찌빠", 0);
				if (player.getInventory().getInventoryItemCount(57, 0) < 10000)
				{
					player.sendMessage("아데나가 부족합니다.");
				}
				else if (묵찌빠 > 9)
				{
					htmltext = npc.getId() + "_try.htm";
				}
				else
				{
					player.destroyItemByItemId("묵찌빠", 57, 10000, player, true);
					player.getAccountVariables().set("묵찌빠", 묵찌빠 + 1);
					playGame(event, npc, player);
				}
				break;
			}
			case "gawia":
			case "bawia":
			case "boa":
			{
				if (event.charAt(event.length() - 1) == 'a')
				{
					playGame(event.substring(0, event.length() - 1), npc, player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	private String playGame(String choice, Npc npc, Player player)
	{
		Random random = new Random();
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		String htmltext = getHtm(player, npc.getId() + "_progress.htm");
		boolean isNormal = npc.getId() == 40026 ? true : false;
		
		// 이미지 변경 작업
		Runnable displayTask = () ->
		{
			int index = random.nextInt(IMAGES.length);
			String imageUrl = IMAGES[index];
			String updatedHtml = htmltext.replace(IMAGE_TAG, "<img src=\"" + imageUrl + "\" width=64 height=64>")//
				.replace("<?result?>", "자~ 누가 이길까나~")//
				.replace("<?again?>", "")//
				.replace("<?myimg?>", "<img src=\"nIcon_big.xmas_" + choice + "_i00\" width=64 height=64>");
			player.sendPacket(new NpcHtmlMessage(updatedHtml));
		};
		
		// 최종 결과를 출력하는 작업
		// @formatter:off
		Runnable finalTask = () ->
		{
			executorService.shutdown(); // 실행을 중단
			// 결과를 결정하기 위한 랜덤 인덱스 생성
			int npcChoiceIndex = getRandomIndex(PROBABILITIES);
			// npc의 선택
			String npcChoice = OPTIONS[npcChoiceIndex];
			String result = determineResult(player, choice, npcChoice, isNormal);
			String finalImageUrl = IMAGES[npcChoiceIndex];
			String finalHtml = htmltext.replace(IMAGE_TAG, "<img src=\"" + finalImageUrl + "\" width=64 height=64>")//
				.replace("<?result?>", result)//
				.replace("<?myimg?>", "<img src=\"nIcon_big.xmas_" + choice + "_i00\" width=64 height=64>")//
				.replace("<?again?>", result.equals("비겼습니다. 다시 선택해 주세요!") ? 
				    "<table border=0 width=240>"
				    + "<tr>"
				    + "<td align=center>"
				    + "<table border=0 cellspacing=0 cellpadding=0 width=64 height=64 background=\"nIcon_big.xmas_gawi_i00\">"
				    + "<tr>"
				    + "<td width=32 height=32 align=center valign=top>"
				    + "<button value=\" \" action=\"bypass -h Quest RockPaperScissors gawia\" width=64 height=64 back=\"nIcon_big.ItemWindow_DF_Frame_Down\" fore=\"nIcon_big.ItemWindow_DF_Frame\"/>"
				    + "</td>"
				    + "</tr>"
				    + "</table>"
				    + "</td>"
				    + "<td align=center>"
				    + "<table border=0 cellspacing=0 cellpadding=0 width=64 height=64 background=\"nIcon_big.xmas_bawi_i00\">"
				    + "<tr>"
				    + "<td width=32 height=32 align=center valign=top>"
				    + "<button value=\" \" action=\"bypass -h Quest RockPaperScissors bawia\" width=64 height=64 back=\"nIcon_big.ItemWindow_DF_Frame_Down\" fore=\"nIcon_big.ItemWindow_DF_Frame\"/>"
				    + "</td>"
				    + "</tr>"
				    + "</table>"
				    + "</td>"
				    + "<td align=center>"
				    + "<table border=0 cellspacing=0 cellpadding=0 width=64 height=64 background=\"nIcon_big.xmas_bo_i00\">"
				    + "<tr>"
				    + "<td width=32 height=32 align=center valign=top>"
				    + "<button value=\" \" action=\"bypass -h Quest RockPaperScissors boa\" width=64 height=64 back=\"nIcon_big.ItemWindow_DF_Frame_Down\" fore=\"nIcon_big.ItemWindow_DF_Frame\"/>"
				    + "</td>"
				    + "</tr>"
				    + "</table>"
				    + "</td>"
				    + "</tr>"
				    + "</table>" 
				    : 
				    "<table border=0 width=240>"
				    + "<tr>"
				    + "<td align=center>"
				    + "<table border=0 cellspacing=0 cellpadding=0 width=64 height=64 background=\"nIcon_big.xmas_gawi_i00\">"
				    + "<tr>"
				    + "<td width=32 height=32 align=center valign=top>"
				    + "<button value=\" \" action=\"bypass -h Quest RockPaperScissors gawi\" width=64 height=64 back=\"nIcon_big.ItemWindow_DF_Frame_Down\" fore=\"nIcon_big.ItemWindow_DF_Frame\"/>"
				    + "</td>"
				    + "</tr>"
				    + "</table>"
				    + "</td>"
				    + "<td align=center>"
				    + "<table border=0 cellspacing=0 cellpadding=0 width=64 height=64 background=\"nIcon_big.xmas_bawi_i00\">"
				    + "<tr>"
				    + "<td width=32 height=32 align=center valign=top>"
				    + "<button value=\" \" action=\"bypass -h Quest RockPaperScissors bawi\" width=64 height=64 back=\"nIcon_big.ItemWindow_DF_Frame_Down\" fore=\"nIcon_big.ItemWindow_DF_Frame\"/>"
				    + "</td>"
				    + "</tr>"
				    + "</table>"
				    + "</td>"
				    + "<td align=center>"
				    + "<table border=0 cellspacing=0 cellpadding=0 width=64 height=64 background=\"nIcon_big.xmas_bo_i00\">"
				    + "<tr>"
				    + "<td width=32 height=32 align=center valign=top>"
				    + "<button value=\" \" action=\"bypass -h Quest RockPaperScissors bo\" width=64 height=64 back=\"nIcon_big.ItemWindow_DF_Frame_Down\" fore=\"nIcon_big.ItemWindow_DF_Frame\"/>"
				    + "</td>"
				    + "</tr>"
				    + "</table>"
				    + "</td>"
				    + "</tr>"
				    + "</table>");

			player.sendPacket(new NpcHtmlMessage(finalHtml));
		};
		// @formatter:on
		
		// 이미지 변경 작업을 일정 간격으로 실행
		executorService.scheduleAtFixedRate(displayTask, 0, DISPLAY_DURATION, TimeUnit.MILLISECONDS);
		// 최종 결과를 출력하는 작업을 지정된 시간 이후에 실행
		executorService.schedule(finalTask, TOTAL_DURATION, TimeUnit.MILLISECONDS);
		
		return null;
	}
	
	private String determineResult(Player player, String playerChoice, String npcChoice, boolean isNormal)
	{
		if (playerChoice.equalsIgnoreCase(npcChoice))
		{
			return "비겼습니다. 다시 선택해 주세요!";
		}
		else if ((playerChoice.equals("gawi") && npcChoice.equals("bo")) || (playerChoice.equals("bawi") && npcChoice.equals("gawi")) || (playerChoice.equals("bo") && npcChoice.equals("bawi")))
		{
			if (isNormal)
			{
				player.addItem("커스텀이벤트_선물스크롤", 29010, 3, player, true);
			}
			else
			{
				player.addItem("프레야_이벤트", 41364, 1, player, true);
			}
			return "이겼습니다! 다시 하시겠어요?";
		}
		else
		{
			return "졌습니다ㅠㅠ 다시 하시겠어요?";
		}
	}
	
	private int getRandomIndex(double[] probabilities)
	{
		Random random = new Random();
		double randomNumber = random.nextDouble();
		double cumulativeProbability = 0.0;
		int index = 0;
		// 누적 확률값이 랜덤 실수보다 크면 해당 인덱스를 결과로 선택
		while (index < probabilities.length)
		{
			cumulativeProbability += probabilities[index];
			if (randomNumber < cumulativeProbability)
			{
				break;
			}
			index++;
		}
		return index;
	}
	
	public static void main(String[] args)
	{
		new RockPaperScissors();
	}
}