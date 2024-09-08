package ai.bosses;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

public final class BossItemDrop extends AbstractNpcAI
{
	private static final int 여왕개미 = 29001;
	private static final int 코어 = 29006;
	private static final int 오르펜 = 29014;
	private static final int 바이움 = 29020;
	private static final int 자켄 = 29022;
	private static final int 안타라스 = 29068;
	private static final int 스칼렛_반_할라샤 = 29047;
	
	private static final int 오르펜80 = 18029;
	private static final int 여왕개미80 = 18020;
	private static final int 이그니스 = 29105;
	private static final int 네불라 = 29106;
	private static final int 프로첼라 = 29107;
	private static final int 페트람 = 29108;
	
	private static final int 아나킴 = 25286;
	private static final int 릴리스 = 25283;
	
	private BossItemDrop()
	{
		addKillId(여왕개미, 코어, 오르펜, 바이움, 자켄, 안타라스, 스칼렛_반_할라샤, 오르펜80, 여왕개미80, 이그니스, 네불라, 프로첼라, 페트람, 아나킴, 릴리스);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		switch (npc.getId())
		{
			case 여왕개미:
			{
				if (Rnd.chance(45))
				{
					npc.dropItem(killer, 6660, 1); // 여왕개미 반지
				}
				npc.dropItem(killer, 41373, 1);
				break;
			}
			case 코어:
			{
				if (Rnd.chance(45))
				{
					npc.dropItem(killer, 6662, 1); // 코어 반지
				}
				npc.dropItem(killer, 41373, 1);
				break;
			}
			case 오르펜:
			{
				if (Rnd.chance(45))
				{
					npc.dropItem(killer, 6661, 1); // 오르펜 반지
				}
				npc.dropItem(killer, 41373, 1);
				break;
			}
			case 바이움:
			{
				int randomValue = Rnd.get(100);
				int itemId = 0;
				
				if (randomValue < 20)
				{ // 20%
					itemId = 49683; // 바이움 탈리스만
				}
				else if (randomValue < 50)
				{ // 20% + 30% = 50%
					itemId = 91256; // 바이움 인형
				}
				else
				{ // 50% + 50% = 100%
					itemId = 49580; // 바이움 반지
				}
				
				// 아이템을 드랍
				npc.dropItem(killer, itemId, 1);
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 자켄:
			{
				if (Rnd.chance(45))
				{
					npc.dropItem(killer, 90763, 1); // 자켄 귀걸이
				}
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 안타라스:
			{
				if (Rnd.chance(85))
				{
					npc.dropItem(killer, 90992, 1); // 안타라스 귀걸이
				}
				if (Rnd.chance(85))
				{
					npc.dropItem(killer, 41365, 1); // 블랙 쿠폰
				}
				npc.dropItem(killer, 41373, Rnd.get(3, 6));
				break;
			}
			case 스칼렛_반_할라샤:
			{
				int randomValue = Rnd.get(100);
				int itemId = 0;
				
				if (randomValue < 10)
				{ // 10%
					itemId = 39361; // 아포칼립스 무기 상자
				}
				else if (randomValue < 25)
				{ // 15%
					itemId = 39331; // 카데이라 방어구 상자
				}
				else if (randomValue < 45)
				{ // 20%
					itemId = 91604; // 프린테사 Doll Lv. 1
				}
				else if (randomValue < 70)
				{ // 25%
					itemId = 21893; // 할리샤 투구
				}
				else
				{ // 30%
					itemId = 21718; // 프린테사 망토
				}
				// 아이템을 드랍
				npc.dropItem(killer, itemId, 1);
				
				if (Rnd.chance(20))
				{
					npc.dropItem(killer, 41365, 1); // 블랙 쿠폰
				}
				npc.dropItem(killer, 41373, Rnd.get(2, 4));
				break;
			}
			case 여왕개미80:
			{
				if (Rnd.chance(30))
				{
					npc.dropItem(killer, 91257, 1); // 여왕개미 인형
				}
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 오르펜80:
			{
				if (Rnd.chance(30))
				{
					npc.dropItem(killer, 91258, 1); // 오르펜 인형
				}
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 이그니스:
			{
				if (Rnd.chance(30))
				{
					npc.dropItem(killer, 91119, 1); // 이그니스 목걸이
				}
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 네불라:
			{
				if (Rnd.chance(30))
				{
					npc.dropItem(killer, 91117, 1); // 네불라 목걸이
				}
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 프로첼라:
			{
				if (Rnd.chance(30))
				{
					npc.dropItem(killer, 91121, 1); // 프로첼라 목걸이
				}
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 페트람:
			{
				if (Rnd.chance(30))
				{
					npc.dropItem(killer, 91123, 1); // 페트람 목걸이
				}
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
			case 아나킴:
			case 릴리스:
			{
				npc.dropItem(killer, 41373, Rnd.get(1, 2));
				break;
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new BossItemDrop();
	}
}
