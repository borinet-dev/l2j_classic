package handlers.bypasshandlers;

import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.CaptchaSystem.CaptchaHandler;
import org.l2jmobius.gameserver.util.CaptchaSystem.CaptchaWindow;

public class Captcha implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Captcha"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (player == null)
		{
			return false;
		}
		
		if (command.startsWith("Captcha"))
		{
			int event_id = 0;
			if (!command.equalsIgnoreCase("Captcha"))
			{
				event_id = Integer.valueOf(command.substring(8));
			}
			
			if ((event_id >= 0) && (event_id <= 9) && (player.getCaptchaAnswerCount() < 4))
			{
				int answerCount = player.getCaptchaAnswerCount();
				player.updateCaptchaAnswerNumber(answerCount, event_id);
				player.updateCaptchaAnswerCount(answerCount + 1);
				CaptchaWindow.CaptchaWindows(player, player.getCaptchaAnswerCount());
			}
			else if (event_id == 10)
			{
				if (player.getCaptchaAnswerCount() > 0)
				{
					int newAnswerCount = player.getCaptchaAnswerCount() - 1;
					player.updateCaptchaAnswerNumber(newAnswerCount, newAnswerCount % 2); // Set to 0 if even, 1 if odd
					player.updateCaptchaAnswerCount(newAnswerCount);
				}
				else
				{
					player.updateCaptchaAnswerCount(0);
				}
				
				CaptchaWindow.CaptchaWindows(player, player.getCaptchaAnswerCount());
			}
			else if (event_id == 11)
			{
				player.updateCaptchaAnswerCount(0);
				for (int i = 0; i < 4; i++)
				{
					player.updateCaptchaAnswerNumber(i, 10);
				}
				CaptchaWindow.CaptchaWindows(player, player.getCaptchaAnswerCount());
			}
			else if (event_id == 12)
			{
				if (player.getCaptchaAnswerCount() < 4)
				{
					player.sendMessage("보안문자 입력이 올바르지 않습니다.");
					CaptchaWindow.CaptchaWindows(player, player.getCaptchaAnswerCount());
				}
				else
				{
					player.updateCaptchaCount(player.getCaptchaCount() + 1);
					player.updateCaptchaAnswerCount(0);
					int[] captchaAnswerNumbers = new int[4];
					for (int i = 0; i < 4; i++)
					{
						captchaAnswerNumbers[i] = player.getCaptchaAnswerNumber(i);
					}
					
					int[] captchaQuestNumbers = new int[4];
					for (int i = 0; i < 4; i++)
					{
						captchaQuestNumbers[i] = player.getCaptchaQuestNumber(i);
					}
					
					boolean isCaptchaCorrect = true;
					for (int i = 0; i < 4; i++)
					{
						if (captchaAnswerNumbers[i] != captchaQuestNumbers[i])
						{
							isCaptchaCorrect = false;
							break;
						}
					}
					
					if (!isCaptchaCorrect)
					{
						CaptchaHandler.NoAnswerCaptcha(player);
					}
					else
					{
						CaptchaHandler.AnswerCaptcha(player);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}