package org.l2jmobius.gameserver.util;

/**
 * String name1=KorNameUtil.getName(name,"을","를");<br>
 * String name2=KorNameUtil.getName(name,"이","가");<br>
 * String name3=KorNameUtil.getName(name,"은","는");
 */
public class KorNameUtil
{
	public static final String getName(String name, String firstValue, String secondValue)
	{
		if ((name == null) || name.isEmpty())
		{
			return name; // 이름이 null이거나 빈 문자열인 경우 그대로 반환
		}
		
		// 마지막 글자가 특수문자인 경우, 특수문자를 제외하고 마지막 한글 또는 숫자/영문자를 찾음
		char lastName = '\0'; // 마지막 글자를 저장할 변수 초기화
		for (int i = name.length() - 1; i >= 0; i--)
		{
			lastName = name.charAt(i);
			if (((lastName >= 0xAC00) && (lastName <= 0xD7A3)) || // 한글 체크
				((lastName >= '0') && (lastName <= '9')) || // 숫자 체크
				((lastName >= 'A') && (lastName <= 'Z')) || // 영문 체크 (대문자)
				((lastName >= 'a') && (lastName <= 'z')))
			{ // 영문 체크 (소문자)
				break; // 한글, 숫자, 영문을 찾으면 반복문 종료
			}
			lastName = '\0'; // 특수문자일 경우 '\0'으로 설정
		}
		
		if (lastName == '\0')
		{
			return name; // 특수문자를 제외하고도 문자가 없으면 그대로 반환
		}
		String selectedValue = secondValue; // 기본값을 secondValue로 설정
		
		// 한글 범위 체크
		if ((lastName >= 0xAC00) && (lastName <= 0xD7A3))
		{
			selectedValue = ((lastName - 0xAC00) % 28) > 0 ? firstValue : secondValue;
		}
		else if ((lastName >= '0') && (lastName <= '9'))
		{
			// 숫자 범위 체크: 특정 숫자는 '를', 나머지는 '을'
			if ((lastName == '2') || (lastName == '4') || (lastName == '5') || (lastName == '9'))
			{
				selectedValue = secondValue; // '를'
			}
			else
			{
				selectedValue = firstValue; // '을'
			}
		}
		else if (((lastName >= 'A') && (lastName <= 'Z')) || ((lastName >= 'a') && (lastName <= 'z')))
		{
			if (((lastName >= 'L') && (lastName <= 'N')) || (lastName == 'R') || ((lastName >= 'l') && (lastName <= 'n')) || (lastName == 'r'))
			{
				selectedValue = firstValue; // '을'
			}
			else
			{
				selectedValue = secondValue; // '를'
			}
		}
		
		return name + selectedValue; // 한글이 아니고 숫자나 영문도 아닌 경우 기본적으로 '를' 사용
	}
	
}
