package ai.others.NpcTalk;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * NPC 대화 AI 클래스입니다. 다양한 NPC가 무작위로 대화를 시작하고 일정 시간마다 무작위 대화를 전파합니다. 대화는 무작위로 선택된 메시지를 무작위로 선택된 시간마다 브로드캐스트합니다. 대화 이벤트에 따라 NPC가 적절한 메시지를 브로드캐스트합니다. 대화가 시작될 때와 NPC가 스폰될 때 대화 타이머가 시작됩니다. 현재 등록된 NPC: 이브, 츠바키, 리아나, 제이, 알리미, 폴리네, 시비스, 라이아 각 NPC에 대한 대화 이벤트 및 메시지는 onAdvEvent 메서드에서 처리됩니다. 대화 타이머는 onSpawn
 * 메서드에서 NPC가 스폰될 때 시작됩니다.
 * @author 보리넷 가츠
 */
public class NpcTalk extends AbstractNpcAI
{
	private static final int 이브 = 40010;
	private static final int 츠바키 = 40012;
	private static final int 리아나 = 40016;
	private static final int 제이 = 40018;
	private static final int 알리미 = 40019;
	private static final int 시비스 = 34262;
	private static final int 라이아 = 34328;
	private static final int 라푼젤 = 30006;
	// 자경단
	private static final int 폴리네 = 40020;
	private static final int 노바 = 81009;
	private static final int 마스라스 = 40027;
	private static final int 베라 = 40028;
	
	private NpcTalk()
	{
		addSpawnId(제이, 츠바키, 시비스, 리아나, 이브, 알리미, 라이아, 라푼젤, 폴리네, 노바, 마스라스, 베라);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		//@formatter:off
		switch (event)
		{
			case "40016_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
					"미니게임 한판하실래요?",
					"1등에게 푸짐한 상품이...!",
					"누구나 도전 가능한 미니게임 한판 하세요~",
					"재미진 미니게임! 야호~ 게임하자~"
					);
				break;
			}
			case "40012_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
					"오빠~ 인던에 가려고? 그럼 암운의 저택에 한번 들러봐~",
					"암운의 저택에 드리운 어두운 그림자를 무찔러 주세요!",
					"두명! 더도 말고 덜도 말고 딱!! 두명파티로 입장이 가능!",
					"놀러가자~ 암운의 저택~ 너도가자~ 암운의 저택~"
					);
				break;
			}
			case "34262_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
					"발터스 기사단에 가입하세요~",
					"발터스 기사단으로 위장하시면 행운 주화도 얻을 수 있고!! 사냥시 습득 경험치가 10% 증가해요!",
					"행운 주화로 레어 액세서리 뽑기에 도전하세요~",
					"오빠~ 언니들~ 저좀 보고 가세요~",
					"발터스 기사단에서 용병을 모집하고 있습니다!"
					);
				break;
			}
			case "40018_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
					"혈맹 투기장에서 혈맹의 한계에 도전하세요!",
					"당신의 혈맹에 경의를...!",
					"혈맹 투기장에는 다양한 상품이 있답니다~",
					"혈맹원과 파티해서 투기장에 도전해보세요!"
					);
				break;
			}
			case "40010_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
					"아브라~ 카타브라!",
					"타로카드로 당신의 행운을 확인하세요!",
					"행운만 있다면 최고의 무기를 가질 수 있다!!",
					"타로카드로 점을 보고 푸짐한 상품을 받아가세요."
					);
				break;
			}
			case "40019_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
					"업데이트 내용을 확인할 수 있어여~",
					"홈페이지? 그게뭐죠? 씹어먹는건가...",
					"업데이트 내용을 놓치지 말자!",
					"자주자주 업데이트 내용을 확인해서 불이익을 받지마세요!"
					);
				break;
			}
			case "34328_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
					"경매에 참가해 보세요!",
					"매주 금요일 오후 5시부터 한시간동안 경매가 열립니다!!",
					"내가 입찰한거에 상위입찰 하기 있냐?",
					"손은 눈보다 빠르다...",
					"누구보다 빠르게 남들과는 다르게!!!",
					"특별한 아이템에 상위입찰을 하세요~"
					);
				break;
			}
			case "30006_TEXT":
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.SPEAK_WITH_ME_ABOUT_TRAVELING_AROUND_ADEN, 1000);
				startQuestTimer(event, Rnd.get(10, 60) * 1000, npc, null, false);
				break;
			}
			case "40020_TEXT":
			case "81009_TEXT":
			case "40027_TEXT":
			case "40028_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(30, 60),
					"신고받고 왔습니다! 카오플레이어는 어디있죠?",
					"히히~ 저희 자경단이 기란마을을 지켜드려요!!",
					"저기요~ 나쁜짓하면 저좀 만나야 해요!",
					"우리 자경단에게 마을의 평화를 맡겨주세요!!!",
					"누구보다 빠르게 남들과는 다르게 순삭!!",
					"정의의 이름으로 용서치 않겠다~!",
					"룰루~",
					"오늘은 카오 사냥하기 좋은 날이네여~",
					"날씨가 참 좋죠? 저랑 카오사냥이나 할까요?"
					);
				break;
			}
		}
		//@formatter:on
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		int sec = Rnd.get(60, 120) * 1000;
		switch (npc.getId())
		{
			case 제이:
			case 츠바키:
			case 리아나:
			case 시비스:
			case 이브:
			case 알리미:
			case 라이아:
				sec = Rnd.get(60, 120) * 1000;
				startQuestTimer(npc.getId() + "_TEXT", sec, npc, null);
				break;
			case 라푼젤:
				sec = Rnd.get(10, 60) * 1000;
				startQuestTimer(npc.getId() + "_TEXT", sec, npc, null, false);
				break;
			case 폴리네:
			case 노바:
			case 마스라스:
			case 베라:
				sec = Rnd.get(30, 60) * 1000;
				startQuestTimer(npc.getId() + "_TEXT", sec, npc, null);
				break;
		}
		return super.onSpawn(npc);
	}
	
	private void sendNpcSaying(Npc npc, int sec, String... sayings)
	{
		String selectedSaying = sayings[Rnd.get(0, sayings.length - 1)];
		npc.broadcastSay(ChatType.NPC_GENERAL, selectedSaying);
		startQuestTimer(npc.getId() + "_TEXT", sec * 1000, npc, null);
	}
	
	public static void main(String[] args)
	{
		new NpcTalk();
	}
}