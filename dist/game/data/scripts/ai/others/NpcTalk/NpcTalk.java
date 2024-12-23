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
				    "재미진 미니게임! 야호~ 게임하자~",
				    "신나는 미니게임 도전하고 선물 받아가세요!",
				    "지금이 기회! 미니게임 참가하고 상품 타가세요~",
				    "오늘의 운을 시험해 볼 시간입니다!",
				    "한 번 참여하면 빠져나올 수 없을걸요?",
				    "도전하세요! 재미와 보상이 기다립니다!",
				    "당신이 주인공이 될 차례입니다!",
				    "미니게임의 승자가 되어보세요!",
				    "놀라운 상품이 당신을 기다립니다!",
				    "자신감을 가지고 도전하세요!",
				    "오늘의 스타는 바로 당신입니다!",
				    "끝없는 재미! 놓치지 마세요!"
					);
				break;
			}
			case "40012_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
				    "오빠~ 인던에 가려고? 그럼 암운의 저택에 한번 들러봐~",
				    "암운의 저택에 드리운 어두운 그림자를 무찔러 주세요!",
				    "두명! 더도 말고 덜도 말고 딱!! 두명파티로 입장이 가능!",
				    "놀러가자~ 암운의 저택~ 너도가자~ 암운의 저택~",
				    "이곳은 어둠과 위험이 도사리는 곳이에요.",
				    "암운의 저택에서 당신의 용기를 증명해 보세요!",
				    "두 사람의 조화가 성공의 열쇠입니다.",
				    "그림자를 물리치고 영웅이 되어주세요!",
				    "두명 파티로만 입장이 가능합니다!",
				    "당신의 실력을 시험해볼 시간입니다.",
				    "여기는 진정한 도전이 기다리는 곳입니다.",
				    "암운의 비밀을 밝혀주세요!",
				    "보물을 찾아 나서는 여정을 시작하세요!",
				    "두 사람의 협력이 필요한 곳입니다.",
				    "용맹한 영웅만이 성공할 수 있습니다."
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
				    "발터스 기사단에서 용병을 모집하고 있습니다!",
				    "기사단에 합류하고 새로운 모험을 시작하세요!",
				    "우리와 함께라면 더 강해질 수 있습니다.",
				    "발터스의 힘을 믿어보세요.",
				    "행운 주화는 당신의 노력을 보답할 것입니다.",
				    "용병이 되어 기사단을 도와주세요!",
				    "새로운 동료를 만나보세요.",
				    "기사단의 용맹한 전사가 되어보세요.",
				    "우리는 당신의 도움이 필요합니다.",
				    "새로운 기회가 당신을 기다립니다.",
				    "함께라면 어떤 도전도 이겨낼 수 있습니다."
					);
				break;
			}
			case "40018_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
				    "혈맹 투기장에서 혈맹의 한계에 도전하세요!",
				    "당신의 혈맹에 경의를...!",
				    "혈맹 투기장에는 다양한 상품이 있답니다~",
				    "혈맹원과 파티해서 투기장에 도전해보세요!",
				    "혈맹의 단결력을 시험할 시간입니다.",
				    "투기장에서 승리를 쟁취해보세요!",
				    "당신의 혈맹은 얼마나 강한가요?",
				    "우리는 강한 혈맹을 기다립니다!",
				    "투기장에서 명예를 얻으세요!",
				    "혈맹원과 협력하여 도전해보세요!",
				    "강한 자만이 살아남습니다.",
				    "혈맹의 명성을 높일 기회입니다.",
				    "당신의 혈맹은 준비되었나요?",
				    "여기서 승리를 차지하는 자가 진정한 영웅입니다.",
				    "혈맹원들과 함께 강해지는 시간입니다."
					);
				break;
			}
			case "40010_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
				    "아브라~ 카타브라!",
				    "타로카드로 당신의 행운을 확인하세요!",
				    "행운만 있다면 최고의 무기를 가질 수 있다!!",
				    "타로카드로 점을 보고 푸짐한 상품을 받아가세요.",
				    "당신의 미래가 타로카드에 있습니다.",
				    "카드에 숨겨진 비밀을 밝혀보세요!",
				    "오늘의 운세는 어떻게 될까요?",
				    "타로가 알려주는 행운을 놓치지 마세요.",
				    "행운을 잡으려면 먼저 점을 보세요!",
				    "카드는 거짓말을 하지 않습니다.",
				    "최고의 선택을 도와드릴게요.",
				    "운명은 당신 손에 달렸습니다.",
				    "한 번의 점괘로 인생이 바뀔 수도 있어요.",
				    "지금 당신의 행운을 시험하세요.",
				    "타로카드가 모든 답을 가지고 있습니다."
					);
				break;
			}
			case "40019_TEXT":
			{
				sendNpcSaying(npc, Rnd.get(40, 150),
				    "업데이트 내용을 확인할 수 있어여~",
				    "홈페이지? 그게뭐죠? 씹어먹는건가...",
				    "업데이트 내용을 놓치지 말자!",
				    "자주자주 업데이트 내용을 확인해서 불이익을 받지마세요!",
				    "최신 정보를 확인하고 앞서가세요!",
				    "업데이트를 놓치면 후회할지도 몰라요.",
				    "새로운 기능을 확인해보세요.",
				    "변경된 내용을 확인하는 건 기본이죠!",
				    "게임을 더 재미있게 즐기고 싶다면 업데이트를 확인하세요.",
				    "최신 업데이트 소식을 놓치지 마세요.",
				    "매일 새로운 정보가 당신을 기다립니다.",
				    "홈페이지를 방문하고 모든 것을 알아보세요.",
				    "업데이트는 당신의 성공을 위한 길잡이입니다.",
				    "변화를 받아들이는 것이 승리의 시작입니다.",
				    "항상 준비된 자가 승리합니다!"
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
				    "특별한 아이템에 상위입찰을 하세요~",
				    "경매장에서 특별한 아이템을 놓치지 마세요!",
				    "희귀한 아이템이 당신을 기다리고 있습니다.",
				    "입찰의 재미를 느껴보세요!",
				    "최고 입찰자가 되어보세요.",
				    "특별한 경매 기회를 잡아보세요!",
				    "모두가 원하는 것을 차지할 시간입니다.",
				    "매번 새로운 물품이 등장합니다.",
				    "가장 빠른 손이 승리합니다!",
				    "당신의 전략으로 경매를 지배하세요."
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
				    "날씨가 참 좋죠? 저랑 카오사냥이나 할까요?",
				    "나쁜 자들은 용서하지 않겠습니다!",
				    "자경단은 항상 정의 편입니다.",
				    "범죄자는 우리 손에서 심판받을 것입니다.",
				    "마을을 지키는 것이 우리의 임무입니다.",
				    "카오를 조심하세요, 당신을 지켜보고 있습니다.",
				    "평화를 위해 언제든 출동합니다!"
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
				startQuestTimer(npc.getName() + "_TEXT", sec, npc, null);
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