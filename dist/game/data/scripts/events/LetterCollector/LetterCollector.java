/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package events.LetterCollector;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

/**
 * Event: Letter Collector
 * @URL https://eu.4gameforum.com/threads/648400/
 * @author Mobius, Gigi, Adapted for Classic by QuangNguyen
 */
public class LetterCollector extends LongTimeEvent
{
	private int WordCount = 0;
	// NPC memories
	private static final int ROSALIA = 9000;
	// Items
	private static final int B = 41002;
	private static final int O = 3884;
	private static final int R = 3885;
	private static final int I = 3881;
	private static final int N = 3883;
	private static final int E = 3877;
	private static final int T = 3887;
	// Exchange Letters
	private static final int[] LETTERS =
	{
		B,
		O,
		R,
		I,
		N,
		E,
		T
	};
	
	// Icon paths
	private static final Map<Character, String[]> ICON_PATHS = new HashMap<>();
	static
	{
		ICON_PATHS.put('B', new String[]
		{
			"nIcon.etc_alphabet_b_n",
			"icon.etc_alphabet_b_i00"
		});
		ICON_PATHS.put('O', new String[]
		{
			"nIcon.etc_alphabet_o_n",
			"icon.etc_alphabet_o_i00"
		});
		ICON_PATHS.put('R', new String[]
		{
			"nIcon.etc_alphabet_r_n",
			"icon.etc_alphabet_r_i00"
		});
		ICON_PATHS.put('I', new String[]
		{
			"nIcon.etc_alphabet_i_n",
			"icon.etc_alphabet_i_i00"
		});
		ICON_PATHS.put('N', new String[]
		{
			"nIcon.etc_alphabet_n_n",
			"icon.etc_alphabet_n_i00"
		});
		ICON_PATHS.put('E', new String[]
		{
			"nIcon.etc_alphabet_e_n",
			"icon.etc_alphabet_e_i00"
		});
		ICON_PATHS.put('T', new String[]
		{
			"nIcon.etc_alphabet_t_n",
			"icon.etc_alphabet_t_i00"
		});
	}
	
	// Rewards
	private static final ItemHolder[] REWARDS_베스페르방어구 =
	{
		new ItemHolder(13432, 1),
		new ItemHolder(13433, 1),
		new ItemHolder(13434, 1),
		new ItemHolder(13435, 1),
		new ItemHolder(13436, 1),
		new ItemHolder(13437, 1),
		new ItemHolder(13438, 1),
		new ItemHolder(13439, 1),
		new ItemHolder(13440, 1),
		new ItemHolder(13441, 1),
		new ItemHolder(13442, 1),
		new ItemHolder(13443, 1),
		new ItemHolder(13444, 1),
		new ItemHolder(13445, 1),
		new ItemHolder(13446, 1),
	};
	private static final ItemHolder[] REWARDS_베스페르무기 =
	{
		new ItemHolder(13457, 1),
		new ItemHolder(13458, 1),
		new ItemHolder(13459, 1),
		new ItemHolder(13460, 1),
		new ItemHolder(13461, 1),
		new ItemHolder(13462, 1),
		new ItemHolder(13463, 1),
		new ItemHolder(13464, 1),
		new ItemHolder(13465, 1),
		new ItemHolder(13466, 1),
		new ItemHolder(13467, 1),
		new ItemHolder(52, 1),
		new ItemHolder(13884, 1),
	};
	private static final ItemHolder[] REWARDS_강화주문서 =
	{
		new ItemHolder(49470, 1),
		new ItemHolder(25793, 1),
		new ItemHolder(21582, 1),
		new ItemHolder(22221, 1),
		new ItemHolder(22222, 1),
		new ItemHolder(22223, 1),
		new ItemHolder(22224, 1),
		new ItemHolder(22225, 1),
		new ItemHolder(22226, 1),
		new ItemHolder(41032, 1),
		new ItemHolder(19447, 1),
		new ItemHolder(19448, 1),
	};
	private static final ItemHolder[] ROSE_AMULET =
	{
		// new ItemHolder(20915, 1), // 향상된 드셀로프 장미의 목걸이
		// new ItemHolder(20916, 1), // 향상된 흄 장미의 목걸이
		// new ItemHolder(20917, 1), // 향상된 레캉 장미의 목걸이
		// new ItemHolder(20918, 1), // 향상된 릴리아스 장미의 목걸이
		// new ItemHolder(20919, 1), // 향상된 라팜 장미의 목걸이
		// new ItemHolder(20920, 1), // 향상된 마퓸 장미의 목걸이
		// new ItemHolder(49845, 1), // 사이하의 축복
		// new ItemHolder(49846, 1), // 사이하의 은빛 축복
		new ItemHolder(9546, 20), // 불의 원석
		new ItemHolder(9547, 20), // 물의 원석
		new ItemHolder(9548, 20), // 땅의 원석
		new ItemHolder(9549, 20), // 바람의 원석
		new ItemHolder(9550, 20), // 암흑의 원석
		new ItemHolder(9551, 20), // 신성의 원석
	};
	private static final ItemHolder[] 최상급방어구 =
	{
		new ItemHolder(22340, 1), // S80 최상급 무기 상자
	};
	private static final ItemHolder[] 최상급무기 =
	{
		new ItemHolder(22339, 1), // 엘레기아 방어구 상자
	};
	private static final ItemHolder[] ELEMENTALS =
	{
		new ItemHolder(9552, 5), // 불의 수정
		new ItemHolder(9553, 5), // 물의 수정
		new ItemHolder(9554, 5), // 땅의 수정
		new ItemHolder(9555, 5), // 바람의 수정
		new ItemHolder(9556, 5), // 암흑의 수정
		new ItemHolder(9557, 5), // 신성의 수정
	};
	private static final ItemHolder[] REWARDS_OTHER =
	{
		new ItemHolder(23767, 1), // 일반 럭키 타로카드
		new ItemHolder(23768, 1), // 프리미엄 럭키 타로카드
		new ItemHolder(13015, 1), // 자텔 서
		new ItemHolder(13016, 1), // 자텔 주문서
		new ItemHolder(20033, 1), // 저탤 깃발
		new ItemHolder(10649, 1), // 축복 깃털
		new ItemHolder(70000, 2), // 봉인된 룬
		new ItemHolder(49110, 1), // Lv. 3 Giant Dye Pack
		new ItemHolder(37009, 3), // Blueberry Cake (MP)
		new ItemHolder(38101, 1), // Leona's Scroll: 1,000,000 SP
		new ItemHolder(38102, 1), // Leona's Scroll: 5,000,000 SP
		new ItemHolder(38103, 1), // Leona's Scroll: 10,000,000 SP
		new ItemHolder(29013, 3), // 송 오브 헌터 주문서
		new ItemHolder(29014, 3), // 댄스 오브 파이어 주문서
		new ItemHolder(70110, 3), // 댄스 오브 워리어 주문서
		new ItemHolder(70111, 3), // 댄스 오브 미스틱 주문서
		new ItemHolder(70112, 1), // 샤프롱 - 특수능력
	};
	
	private LetterCollector()
	{
		addStartNpc(ROSALIA);
		addFirstTalkId(ROSALIA);
		addTalkId(ROSALIA);
	}
	
	private boolean checkLetters(Player player)
	{
		if ((getQuestItemsCount(player, B) >= 1) && //
			(getQuestItemsCount(player, O) >= 1) && //
			(getQuestItemsCount(player, R) >= 1) && //
			(getQuestItemsCount(player, I) >= 1) && //
			(getQuestItemsCount(player, N) >= 1) && //
			(getQuestItemsCount(player, E) >= 1) && //
			(getQuestItemsCount(player, T) >= 1))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "return":
			{
				htmltext = generateHtml(player);
				break;
			}
			case "exchange":
			{
				htmltext = createDynamicHtml(player);
				break;
			}
			case "take_Gift":
			{
				player.getAccountVariables().set("문자수집가의선물", 1);
				player.addItem("문자 수집가의 선물", 49457, 5, player, true);
				break;
			}
			case "borinet":
			{
				if (checkLetters(player))
				{
					takeItems(player, B, 1);
					takeItems(player, O, 1);
					takeItems(player, R, 1);
					takeItems(player, I, 1);
					takeItems(player, N, 1);
					takeItems(player, E, 1);
					takeItems(player, T, 1);
					giveItems(player, getReward());
					htmltext = generateHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "borinetAll":
			{
				if (checkLetters(player))
				{
					WordCount++;
					takeItems(player, B, 1);
					takeItems(player, O, 1);
					takeItems(player, R, 1);
					takeItems(player, I, 1);
					takeItems(player, N, 1);
					takeItems(player, E, 1);
					takeItems(player, T, 1);
					giveItems(player, getReward());
					startQuestTimer("borinetChange", 0, null, player);
				}
				else if (WordCount > 0)
				{
					player.sendMessage("총 " + WordCount + "번 교환하였습니다!");
					WordCount = 0;
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "borinetChange":
			{
				startQuestTimer("borinetAll", 0, null, player);
				break;
			}
			case "exchangeB":
			{
				if (getQuestItemsCount(player, B) >= 2)
				{
					takeItems(player, B, 2);
					giveItems(player, getRandomEntry(LETTERS), 1);
					htmltext = createDynamicHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "exchangeO":
			{
				if (getQuestItemsCount(player, O) >= 2)
				{
					takeItems(player, O, 2);
					giveItems(player, getRandomEntry(LETTERS), 1);
					htmltext = createDynamicHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "exchangeR":
			{
				if (getQuestItemsCount(player, R) >= 2)
				{
					takeItems(player, R, 2);
					giveItems(player, getRandomEntry(LETTERS), 1);
					htmltext = createDynamicHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "exchangeI":
			{
				if (getQuestItemsCount(player, I) >= 2)
				{
					takeItems(player, I, 2);
					giveItems(player, getRandomEntry(LETTERS), 1);
					htmltext = createDynamicHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "exchangeN":
			{
				if (getQuestItemsCount(player, N) >= 2)
				{
					takeItems(player, N, 2);
					giveItems(player, getRandomEntry(LETTERS), 1);
					htmltext = createDynamicHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "exchangeE":
			{
				if (getQuestItemsCount(player, E) >= 2)
				{
					takeItems(player, E, 2);
					giveItems(player, getRandomEntry(LETTERS), 1);
					htmltext = createDynamicHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "exchangeT":
			{
				if (getQuestItemsCount(player, T) >= 2)
				{
					takeItems(player, T, 2);
					giveItems(player, getRandomEntry(LETTERS), 1);
					htmltext = createDynamicHtml(player);
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	public String generateHtml(Player player)
	{
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<html><body>").append("<table width=292 height=64 cellspacing=0 cellpadding=0><tr><td>").append("<img src=\"nIcon.EventletterCollector_Bg\" width=292 height=64>").append("</td></tr></table>").append("<center><br>아래는 한번씩 보상을 받을 수 있어요.<br1>").append("<table border=0 width=292 height=86 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\"><tr><td>").append("<table width=292 border=0><tr><td align=left>조합 문자</td>");
		if (checkLetters(player))
		{
			htmlBuilder.append("<td align=right><button action=\"bypass -h Quest LetterCollector borinet\" value=\"보상받기\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		}
		else
		{
			htmlBuilder.append("<td height=31 align=right>");
		}
		htmlBuilder.append("</td>").append("</tr></table><table width=100><tr><td>").append("<img src=\"L2UI.squaregray\" width=279 height=1/>").append("</td></tr></table>").append("<table width=292 height=42 border=0><tr>");
		
		// 플레이어 인벤토리 확인 후 HTML에 이미지 경로 반영
		for (char letter : "BORINET".toCharArray())
		{
			String[] paths = ICON_PATHS.get(letter);
			String iconPath = playerHasItem(player, letter) ? paths[1] : paths[0];
			if (playerHasItem(player, letter))
			{
				htmlBuilder.append("<td height=40><button width=32 height=32 itemtooltip=\"").append(getItemIdByLetter(letter)).append("\" back=\"").append(iconPath).append("\" fore=\"").append(iconPath).append("\"></button></td>");
			}
			else
			{
				htmlBuilder.append("<td height=40><button width=32 height=32 back=\"").append(iconPath).append("\" fore=\"").append(iconPath).append("\"></button></td>");
			}
		}
		
		htmlBuilder.append("</tr></table></td></tr></table><br><br1>한번에 받고싶어요? 그럼 아래 한방보상을 이용해요!<br1>").append("<table border=0 width=292 height=86 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\"><tr><td>").append("<table width=292 border=0><tr><td align=left>조합 문자</td>");
		if (checkLetters(player))
		{
			htmlBuilder.append("<td align=right><button action=\"bypass -h Quest LetterCollector borinetAll\" value=\"한방보상\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		}
		else
		{
			htmlBuilder.append("<td height=31 align=right>");
		}
		htmlBuilder.append("</td>").append("</tr></table><table width=100><tr><td>").append("<img src=\"L2UI.squaregray\" width=279 height=1/>").append("</td></tr></table>").append("<table width=292 height=42 border=0><tr>");
		
		// 플레이어 인벤토리 확인 후 두 번째 HTML에도 동일한 로직 적용
		for (char letter : "BORINET".toCharArray())
		{
			String[] paths = ICON_PATHS.get(letter);
			String iconPath = playerHasItem(player, letter) ? paths[1] : paths[0];
			if (playerHasItem(player, letter))
			{
				htmlBuilder.append("<td height=40><button width=32 height=32 itemtooltip=\"").append(getItemIdByLetter(letter)).append("\" back=\"").append(iconPath).append("\" fore=\"").append(iconPath).append("\"></button></td>");
			}
			else
			{
				htmlBuilder.append("<td height=40><button width=32 height=32 back=\"").append(iconPath).append("\" fore=\"").append(iconPath).append("\"></button></td>");
			}
		}
		
		htmlBuilder.append("</tr></table></td></tr></table></center>");
		int Letter_Gift = player.getAccountVariables().getInt("문자수집가의선물", 0);
		if (isWinterActive() && (Letter_Gift == 0))
		{
			htmlBuilder.append("<br><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest LetterCollector take_Gift\"><font color=LEVEL>\"겨울이에요. 선물을 주세요!\"</font></Button>");
		}
		else
		{
			htmlBuilder.append("<br><br>");
		}
		htmlBuilder.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest LetterCollector exchange\">\"문자를 다른 문자와 교환하고 싶습니다.\"</Button>");
		htmlBuilder.append("</body></html>");
		
		return htmlBuilder.toString();
	}
	
	private boolean isWinterActive()
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = LocalDateTime.of(now.getYear(), Month.NOVEMBER, 1, 1, 0); // 10월 8일 오전 7시
		LocalDateTime end = LocalDateTime.of(now.getYear(), Month.DECEMBER, 31, 23, 59); // 10월 10일 오후 8시
		
		return now.isAfter(start) && now.isBefore(end);
	}
	
	public String createDynamicHtml(Player player)
	{
		StringBuilder htmlBuilder = new StringBuilder();
		htmlBuilder.append("<html><body>").append("<table width=292 height=64 cellspacing=0 cellpadding=0><tr><td>").append("<img src=\"nIcon.EventletterCollector_Bg\" width=292 height=64>").append("</td></tr></table>").append("<center><br><br><font color=\"LEVEL\">2개의 동일한 문자를</font> 랜덤하게 <font color=\"LEVEL\">하나의 문자</font>로<br1> 교환이 가능합니다.<br>2장이상 보유한 문자만 활성화 됩니다.<br>").append("<table border=0 width=292 height=40 background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\"><tr>");
		
		// 플레이어 인벤토리 확인 후 HTML에 이미지 경로 반영 (2개 이상 보유 시 i00 이미지 표시)
		for (char letter : "BORINET".toCharArray())
		{
			String[] paths = ICON_PATHS.get(letter);
			String iconPath;
			int itemId = getItemIdByLetter(letter);
			long itemCount = getQuestItemsCount(player, itemId);
			
			// 아이템이 2개 이상 있는 경우 i00 이미지 사용하고 교환 버튼 활성화
			if (itemCount >= 2)
			{
				iconPath = paths[1]; // i00 이미지
				String action = "exchange" + letter; // 교환 이벤트 액션 문자열
				htmlBuilder.append("<td height=36><button width=32 height=32 itemtooltip=\"").append(itemId).append("\" back=\"").append(iconPath).append("\" fore=\"").append(iconPath).append("\" action=\"bypass -h Quest LetterCollector ").append(action).append("\">").append("</button></td>");
			}
			else
			{
				// 아이템이 2개 미만인 경우 기본 이미지 사용
				iconPath = paths[0]; // 기본 이미지
				htmlBuilder.append("<td height=36><button width=32 height=32 back=\"").append(iconPath).append("\" fore=\"").append(iconPath).append("\"></button></td>");
			}
		}
		
		htmlBuilder.append("</tr></table></td></tr></table><br><br><br>");
		htmlBuilder.append("<Button ALIGN=LEFT ICON=\"RETURN\" action=\"bypass -h Quest LetterCollector return\">뒤로가기</Button>");
		return htmlBuilder.toString();
	}
	
	private int getItemIdByLetter(char letter)
	{
		switch (letter)
		{
			case 'B':
				return B;
			case 'O':
				return O;
			case 'R':
				return R;
			case 'I':
				return I;
			case 'N':
				return N;
			case 'E':
				return E;
			case 'T':
				return T;
			default:
				return -1;
		}
	}
	
	private boolean playerHasItem(Player player, char letter)
	{
		switch (letter)
		{
			case 'B':
				return getQuestItemsCount(player, B) >= 1;
			case 'O':
				return getQuestItemsCount(player, O) >= 1;
			case 'R':
				return getQuestItemsCount(player, R) >= 1;
			case 'I':
				return getQuestItemsCount(player, I) >= 1;
			case 'N':
				return getQuestItemsCount(player, N) >= 1;
			case 'E':
				return getQuestItemsCount(player, E) >= 1;
			case 'T':
				return getQuestItemsCount(player, T) >= 1;
			default:
				return false;
		}
	}
	
	private ItemHolder getReward()
	{
		if (getRandom(300) < 2)
		{
			return getRandomEntry(최상급무기);
		}
		else if (getRandom(300) < 3)
		{
			return getRandomEntry(최상급방어구);
		}
		else if (getRandom(200) < 4)
		{
			return getRandomEntry(REWARDS_강화주문서);
		}
		else if (getRandom(200) < 5)
		{
			return getRandomEntry(REWARDS_베스페르무기);
		}
		else if (getRandom(200) < 6)
		{
			return getRandomEntry(REWARDS_베스페르방어구);
		}
		else if (getRandom(100) < 7)
		{
			return getRandomEntry(ELEMENTALS);
		}
		else if (getRandom(100) < 10)
		{
			return getRandomEntry(ROSE_AMULET);
		}
		else
		{
			return getRandomEntry(REWARDS_OTHER);
		}
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return generateHtml(player);
	}
	
	public static void main(String[] args)
	{
		new LetterCollector();
	}
}