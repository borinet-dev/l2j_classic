package org.l2jmobius.gameserver.util.CaptchaSystem;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * 보리넷 가츠
 */
public class CaptchaEvent
{
	private final String actorName;
	private final long startDate;
	private final Player _player;
	
	CaptchaEvent(Player player, long startDate)
	{
		actorName = player.getName();
		_player = player;
		this.startDate = startDate;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public String getActorName()
	{
		return actorName;
	}
	
	public long getStartDate()
	{
		return startDate;
	}
	
	public static void clearCaptcha(Player player)
	{
		for (int i = 0; i < 4; i++)
		{
			player.updateCaptchaAnswerNumber(i, 10);
			player.updateCaptchaQuestNumber(i, 10);
		}
		
		for (int i = 0; i < 10; i++)
		{
			player.updateCaptchaNumberPad(i, 10);
		}
	}
	
	public static void getNumberPadPattern(Player player)
	{
		for (int i = 0; i < 10; i++)
		{
			int currentNumber = player.getCaptchaNumberPad(i);
			if (currentNumber == 10)
			{
				player.updateCaptchaNumberPad(i, Rnd.get(0, 9));
			}
		}
		
		// 중복된 숫자가 체크
		for (int i = 0; i < 10; i++)
		{
			int currentNumber = player.getCaptchaNumberPad(i);
			for (int j = i + 1; j < 10; j++)
			{
				if (currentNumber == player.getCaptchaNumberPad(j))
				{
					player.updateCaptchaNumberPad(i, Rnd.get(0, 9));
					getNumberPadPattern(player);
					return;
				}
			}
		}
	}
	
	public static void getQuestNumbers(Player player)
	{
		List<Integer> oddNumbers = new ArrayList<>();
		List<Integer> evenNumbers = new ArrayList<>();
		
		// 홀수와 짝수를 나눠서 리스트에 저장
		for (int i = 0; i < 10; i++)
		{
			if ((i % 2) == 0)
			{
				evenNumbers.add(i);
			}
			else
			{
				oddNumbers.add(i);
			}
		}
		
		boolean isOddTurn = true; // 홀수를 뽑을 차례인지 여부
		
		for (int i = 0; i < 4; i++)
		{
			List<Integer> numbersToChooseFrom = isOddTurn ? oddNumbers : evenNumbers;
			int randomIndex = Rnd.get(numbersToChooseFrom.size());
			int selectedNumber = numbersToChooseFrom.get(randomIndex);
			player.updateCaptchaQuestNumber(i, selectedNumber);
			numbersToChooseFrom.remove(randomIndex);
			isOddTurn = !isOddTurn; // 홀수/짝수 차례를 번갈아가면서 바꿉니다.
		}
	}
	
	public static String[] getKorNames(Player player)
	{
		String[] names = new String[4];
		// @formatter:off
		String[] KorNames =
		{
			"영", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"
		};
		// @formatter:on
		
		for (int i = 0; i < 4; i++)
		{
			int questNumber = player.getCaptchaQuestNumber(i);
			if ((questNumber >= 0) && (questNumber <= KorNames.length))
			{
				String color = ((questNumber == 1) || (questNumber == 3) || (questNumber == 5) || (questNumber == 7) || (questNumber == 9)) ? "color=DED59C" : "";
				names[i] = String.format("<font name=\"hs12\" %s>%s</font>", color, KorNames[questNumber]);
			}
		}
		
		return names;
	}
}