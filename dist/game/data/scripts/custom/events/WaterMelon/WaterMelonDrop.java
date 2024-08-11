package custom.events.WaterMelon;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.type.CrystalType;
import org.l2jmobius.gameserver.model.quest.Event;

/**
 * @author 보리넷 가츠
 */
public class WaterMelonDrop extends Event
{
	// Main Material
	private static final int 동물뼈조각 = 1872;
	private static final int 흑탄 = 1870;
	private static final int 연마제 = 1865;
	private static final int 정화의_돌 = 1875;
	private static final int 강철 = 1880;
	private static final int 미스릴_원석 = 1876;
	private static final int 가죽 = 1882;
	private static final int 코크스 = 1879;
	private static final int 거친_뼈가루 = 1881;
	private static final int 아다만타이트 = 1877;
	private static final int 아소페 = 4043;
	private static final int 거푸집_접착제 = 4039;
	private static final int 오리하루콘_원석 = 1874;
	private static final int 쇠_거푸집 = 1883;
	private static final int 합성끈 = 1889;
	private static final int 합성_코크스 = 1888;
	private static final int 순백의_연마제 = 1887;
	private static final int 고급_스웨이드 = 1885;
	private static final int 엔리아 = 4042;
	private static final int 미스릴합금 = 1890;
	private static final int 거푸집_강화제 = 4041;
	private static final int 거푸집_윤활액 = 4040;
	private static final int 결정체_D그레이드 = 1458;
	private static final int 결정체_C그레이드 = 1459;
	private static final int 결정체_B그레이드 = 1460;
	private static final int 은제_거푸집 = 1886;
	private static final int 오리하루콘 = 1893;
	
	// 주문서
	private static final int 댄스_오브_워리어_주문서 = 70110;
	private static final int 댄스_오브_파이어_주문서 = 29014;
	private static final int 댄스_오브_미스틱_주문서 = 70111;
	private static final int 송_오브_헌터_주문서 = 29013;
	private static final int 최상급_생명의돌 = 90015;
	private static final int 최상급_액세서리용_생명의돌 = 41078;
	private static final int 아키서스의_주문서 = 41235;
	
	// 룬
	private static final int 시겔의_룬 = 29818;
	private static final int 티르의_룬 = 29838;
	private static final int 오셀의_룬 = 29858;
	private static final int 율의_룬 = 29878;
	private static final int 페오의_룬 = 29898;
	private static final int 이스의_룬 = 29918;
	private static final int 윈의_룬 = 29938;
	private static final int 에오로의_룬 = 29958;
	private static final int 아나킴_룬 = 91163;
	private static final int 릴리스_룬 = 91173;
	
	public WaterMelonDrop()
	{
	}
	
	public static final int[] DROPLIST_불량 =
	{
		1539, // 강력 체력 회복제
		결정체_D그레이드,
		엔리아,
		미스릴합금,
		거푸집_강화제,
		거푸집_윤활액,
		은제_거푸집,
		순백의_연마제,
		흑탄,
		동물뼈조각,
		연마제,
		고급_스웨이드
	};
	
	public static final int[] DROPLIST_우량수박 =
	{
		29584, // 앤젤 캣의 축복 상자
		1539, // 강력 체력 회복제
		49080, // 사기충천 인절미
		29009, // EXP/SP 부스트 주문서 - 일반
		29519, // EXP/SP 부스트 주문서 - 중급
		강철,
		아다만타이트,
		미스릴_원석,
		가죽,
		코크스,
		거친_뼈가루,
		정화의_돌,
		댄스_오브_미스틱_주문서,
	};
	
	public static final int[] DROPLIST_우량꿀수박 =
	{
		29584, // 앤젤 캣의 축복 상자
		1540, // 순간 체력 회복제
		49080, // 사기충천 인절미
		22228, // 파멸의 갑옷 강화 주문서-C그레이드
		29009, // EXP/SP 부스트 주문서 - 일반
		29519, // EXP/SP 부스트 주문서 - 중급
		아다만타이트,
		아소페,
		거친_뼈가루,
		코크스,
		고급_스웨이드,
		미스릴_원석,
		거푸집_접착제,
		오리하루콘_원석,
		강철,
		쇠_거푸집,
		정화의_돌,
		합성끈,
		합성_코크스,
		순백의_연마제,
		시겔의_룬,
		티르의_룬,
		오셀의_룬,
		율의_룬,
		페오의_룬,
		이스의_룬,
		윈의_룬,
		에오로의_룬,
		댄스_오브_미스틱_주문서,
		송_오브_헌터_주문서
	};
	
	public static final int[] DROPLIST_왕우량수박 =
	{
		29584, // 앤젤 캣의 축복 상자
		3936, // 축복받은 부활 주문서
		1540, // 순간 체력 회복제
		49081, // 타오르는 사기충천 인절미
		29817, // 해적단의 특별한 열매
		29010, // EXP/SP 부스트 주문서 - 상급
		29519, // EXP/SP 부스트 주문서 - 중급
		22228, // 파멸의 갑옷 강화 주문서-C그레이드
		22227, // 파멸의 무기 강화 주문서-C그레이드
		결정체_B그레이드,
		결정체_C그레이드,
		미스릴합금,
		거푸집_강화제,
		오리하루콘,
		은제_거푸집,
		송_오브_헌터_주문서,
		41061, // +16 무기 교환권 - B
		41062, // +16 방어구 교환권 - B
		41237, // 딸기 바나나 쉐이크
		41238, // 망고 바나나 쉐이크
		41239, // 체리 바나나 쉐이크
		41000 // 루나
	};
	
	public static final int[] DROPLIST_왕우량꿀수박 =
	{
		29584, // 앤젤 캣의 축복 상자
		3936, // 축복받은 부활 주문서
		22222, // 파멸의 갑옷 강화 주문서-S그레이드
		22221, // 파멸의 무기 강화 주문서-S그레이드
		22223, // 파멸의 갑옷 강화 주문서-A그레이드
		22224, // 파멸의 무기 강화 주문서-A그레이드
		1539, // 강력 체력 회복제
		1540, // 순간 체력 회복제
		49081, // 타오르는 사기충천 인절미
		33479, // 파멸의 갑옷 강화 주문서-R그레이드
		33478, // 파멸의 무기 강화 주문서-R그레이드
		29817, // 해적단의 특별한 열매
		29010, // EXP/SP 부스트 주문서 - 상급
		29519, // EXP/SP 부스트 주문서 - 중급
		41061, // +16 무기 교환권 - B
		41062, // +16 방어구 교환권 - B
		시겔의_룬,
		티르의_룬,
		오셀의_룬,
		율의_룬,
		페오의_룬,
		이스의_룬,
		윈의_룬,
		에오로의_룬,
		아나킴_룬,
		릴리스_룬,
		댄스_오브_워리어_주문서,
		댄스_오브_파이어_주문서,
		댄스_오브_미스틱_주문서,
		송_오브_헌터_주문서,
		최상급_생명의돌,
		최상급_액세서리용_생명의돌,
		아키서스의_주문서,
		41233, // 무기 강화석
		41234, // 방어구 강화석
		41237, // 딸기 바나나 쉐이크
		41238, // 망고 바나나 쉐이크
		41239, // 체리 바나나 쉐이크
		41000 // 루나
	};
	
	public static void dropItem(Npc npc, Player player, int npcId, int count, int dropCount)
	{
		int tryCount = count;
		tryCount++;
		
		if (tryCount <= dropCount)
		{
			switch (npcId)
			{
				case 13272:
				case 13276:
				{
					if (ItemTable.getInstance().getTemplate(DROPLIST_불량[getRandom(DROPLIST_불량.length)]).getCrystalType() != CrystalType.NONE)
					{
						npc.dropItem(player, DROPLIST_불량[getRandom(DROPLIST_불량.length)], 1);
					}
					npc.dropItem(player, DROPLIST_불량[getRandom(DROPLIST_불량.length)], (getRandom(1, 3)));
					dropItem(npc, player, npcId, tryCount, dropCount);
					break;
				}
				case 13273:
				{
					if (ItemTable.getInstance().getTemplate(DROPLIST_우량수박[getRandom(DROPLIST_우량수박.length)]).getCrystalType() != CrystalType.NONE)
					{
						npc.dropItem(player, DROPLIST_우량수박[getRandom(DROPLIST_우량수박.length)], 1);
					}
					npc.dropItem(player, DROPLIST_우량수박[getRandom(DROPLIST_우량수박.length)], (getRandom(1, 3)));
					dropItem(npc, player, npcId, tryCount, dropCount);
					break;
				}
				case 13274:
				{
					if (ItemTable.getInstance().getTemplate(DROPLIST_왕우량수박[getRandom(DROPLIST_왕우량수박.length)]).getCrystalType() != CrystalType.NONE)
					{
						npc.dropItem(player, DROPLIST_왕우량수박[getRandom(DROPLIST_왕우량수박.length)], 1);
					}
					npc.dropItem(player, DROPLIST_왕우량수박[getRandom(DROPLIST_왕우량수박.length)], (getRandom(1, 3)));
					dropItem(npc, player, npcId, tryCount, dropCount);
					break;
				}
				case 13277:
				{
					if (ItemTable.getInstance().getTemplate(DROPLIST_우량꿀수박[getRandom(DROPLIST_우량꿀수박.length)]).getCrystalType() != CrystalType.NONE)
					{
						npc.dropItem(player, DROPLIST_우량꿀수박[getRandom(DROPLIST_우량꿀수박.length)], 1);
					}
					npc.dropItem(player, DROPLIST_우량꿀수박[getRandom(DROPLIST_우량꿀수박.length)], (getRandom(1, 3)));
					dropItem(npc, player, npcId, tryCount, dropCount);
					break;
				}
				case 13278:
				{
					if (ItemTable.getInstance().getTemplate(DROPLIST_왕우량꿀수박[getRandom(DROPLIST_왕우량꿀수박.length)]).getCrystalType() != CrystalType.NONE)
					{
						npc.dropItem(player, DROPLIST_왕우량꿀수박[getRandom(DROPLIST_왕우량꿀수박.length)], 1);
					}
					npc.dropItem(player, DROPLIST_왕우량꿀수박[getRandom(DROPLIST_왕우량꿀수박.length)], (getRandom(1, 3)));
					dropItem(npc, player, npcId, tryCount, dropCount);
					break;
				}
			}
		}
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
		new WaterMelonDrop();
	}
}