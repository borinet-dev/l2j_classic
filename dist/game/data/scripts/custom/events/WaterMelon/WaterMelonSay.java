package custom.events.WaterMelon;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Event;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

/**
 * @author 보리넷 가츠
 */
public class WaterMelonSay extends Event
{
	//@formatter:off
	public static final String[] 크로노무기없음 =
	{
		"크로노 없이는 날 죽일 수 없어!",
		"헤헤...계속해봐...",
		"좋은 시도야...",
		"피곤해?",
		"계속해! ㅋㅋㅋㅋ"
	};
	
	public static final String[] 어린수박대화 =
	{
		"어? 이게뭐야? 어떻게 된거지? 누가 나 불렀나?",
		"짜라잔~ 작은 박님 등장이시다!",
		"후훗... 절 부르셨나요...?",
		"작은 박 등장! 이제부터 무럭무럭 자랄거에요!",
		"어이쿠, 이게 얼마만에 보는 사람이여?",
		"대박기원!",
		"불렀수? 왜 불렀수? 뭐 나올까봐?",
		"불끈불끈. 나의 아름다운 모습이 보고싶나?",
		"에헴! 자 나오셨다! 어디 한번 모셔봐라!",
		"잘키우면 대박~ 못키우면 쪽박~"
	};

	public static final String[] 우량수박대화 =
	{
		"다 자랐다! 이제 도망가야지~",
		"잘자란 우량수박 열불량수박 안부럽지!"
	};
	
	public static final String[] 불량수박대화 =
	{
		"어이쿠... 불량수박? 내가 불량수박이라니..."
	};
	
	public static final String[] 왕우량수박대화 =
	{
		"다 자랐다! 이제 도망가야지~",
		"너는 수박의 마음에 대해서 좀 아는군."
	};
	
	public static final String[] 어린수박공격대화 =
	{
		"더 때려봐! 더 때려봐!",
		"키우지도 않고 잡아먹으려고? 그래 맘대로 해라~ 넥타 안주면 죽어버릴텨!",
		"지금 때리는거야? 응? 날 때리는거야?",
		"야, 나 이대로 죽음 아이템이고 뭐고 없다? 그렇게 넥타가 아깝냐?",
		"오, 소리 좋은데?",
		"아얏! 이젠 막 때리네? 넥타를 뿌리라니까?",
		"저는 넥타를 마셔야만 클 수 있어요~",
		"어쭈구리? 때렸냐? 때렸어?",
		"야 야 그만하지? 이러다 시든다?",
		"넥타좀 주세요~ 배가 고파요~",
		"넥타를 가져오면 쑥쑥 마시고 자라 주지!",
		"이런 작은 박을 먹으려고? 넥타를 줘봐, 좀 더 커져줄테니!",
		"우켈켈켈 잘 키우면 용나지~ 못키우면? 나도 몰라~",
		"자, 날 믿고 넥타를 부어봐!! 내가 대박이 되어줄께!!!",
		"좋은 공격이야. 날아가는 파리는 잡을 수 있겠군."
	};
	
	public static final String[] 우량수박공격대화 =
	{
		"크로노의 싸운드에 끌리는 이 마음!",
		"기분좋은걸~ 좀 더 쳐봐!",
		"으헤헤헤 잘 좀 쳐봐!",
		"어쭈구리? 좀 하는군 그래?",
		"멋진 음악이로군!",
		"악기는 좋은데, 노래가 없군. 내가 불러줄까?",
		"나는 너의 타격을 먹고 자라지!",
		"힘내봐~ 이러다 나 그냥 가겠다~",
		"화음 좀 제대로 맞춰 봐! 거기 틀렸잖아!",
		"생각하지 마! 그냥 쳐! 치는거야!",
		"그것도 치는거냐? 더 실력있는 애 데려와!",
		"겨우 그 정도로 내가 깨질 것 같냐?",
		"이거야 이거! 이 싸운드를 원했어! 너, 가수가 되어보지 않을래?",
		"오 이 화음! 기분 째지는군! 좀 더 쳐봐!",
		"아아~~ 몸이 열리려고 해!",
		"완전 놀자판이로군! 아주 좋아!",
		"거기 거기! 조금 오른쪽! 아~ 시원하다."
	};
	
	
	public static final String[] 넥타빨리먹임 =
	{
		"야야! 너무 빨리 먹이지마 체해!",
		"왜이리 급해!! 잠만 잠만 커억!!",
		"넥타로 나를 죽일 셈이냐!!!",
		"좀 천천히 줘. 나 민감한 수박이야!"
	};
	
	public static final String[] 넥타성공 =
	{
		"잘 해봐! 잘만 먹이면 큰 박이 된다고!",
		"어~ 시원하다! 좀 더 뿌려봐!",
		"나를 놔주면 천만 아데나를 주마! ...닮았냐?",
		"꼴깍 꼴깍~ 좋군! 그런데 더 없어?",
		"자! 내 안에 뭐가 들었을것같냐?",
		"오~ 좋아좋아. 조금만 더 해봐. 슬슬 좋아지려고 해",
		"어~ 시원하다! 좀 더 뿌려봐!"
	};
	
	public static final String[] 넥타실패 =
	{
		"이거 물 탄거 아냐? 뭔 맛이 이래?",
		"우엑 퉤퉤! 너 이거 뭐야! 넥타 맞아?",
		"조준 하나 제대로 못해? 그렇게 질질 흘리고는 말이야~",
		"넥타좀 주세요~ 배가 고파요~",
		"자, 얼른 키워봐! 잘되면 대박, 못되면 쪽박!",
		"자 어서 넥타를 가져오려무나.",
		"이봐! 똑바로 좀 해봐! 흘렸잖아!",
		"주인님 나좀 살려주소~ 넥타 한번 마셔보지 못하고 죽게 생겼수~",
		"이러다 죽으면 그냥 쪽박이요~",
		"이러다 시들지~ 난 몰라~"
	};
	//@formatter:on
	
	public static void noChronoText(Npc npc)
	{
		if (getRandom(100) < 20)
		{
			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), WaterMelonSay.크로노무기없음[getRandom(WaterMelonSay.크로노무기없음.length)]));
		}
	}
	
	public WaterMelonSay()
	{
	}
	
	@Override
	public boolean eventStart(Player eventMaker)
	{
		return false;
	}
	
	@Override
	public boolean eventStop()
	{
		return false;
	}
	
	@Override
	public boolean eventBypass(Player player, String bypass)
	{
		return false;
	}
	
	public static void main(String[] args)
	{
		new WaterMelonSay();
	}
}