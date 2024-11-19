package org.l2jmobius.gameserver.util.CaptchaSystem;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * 보리넷 가츠
 */
public class CaptchaWindow
{
	public static void CaptchaWindows(Player target, int editBox)
	{
		CaptchaTimer.getInstance().addCaptchaTimer(target);
		
		if (target.getCaptchaNumberPad(1) == 10)
		{
			CaptchaEvent.getNumberPadPattern(target);
		}
		if (target.getCaptchaQuestNumber(1) == 10)
		{
			CaptchaEvent.getQuestNumbers(target);
		}
		
		String[] names = CaptchaEvent.getKorNames(target);
		String box = "";
		
		box = editBox == 0 ? "borinet.CharacterPassword_DF_EditBoxFocus" : "borinet.CharacterPassword_DF_EditBoxFocus_" + editBox;
		
		String html = HtmCache.getInstance().getHtm(target, "data/html/CaptchaSystem.htm");
		html = html.replace("%time%", String.valueOf(Config.CAPTCHA_ANSWER_SECONDS));
		html = html.replace("%img%", "<img src=\"" + box + "\" width=120 height=21");
		
		for (int i = 0; i < 4; i++)
		{
			String questPlaceholder = "%quest" + i + "%";
			String cnumberPlaceholder = "%cnumber" + i + "%";
			
			html = html.replace(questPlaceholder, "<td width=\"32\" height=\"28\" valign=\"bottom\" align=\"center\">" + names[i] + "</td>");
			html = html.replace(cnumberPlaceholder, "<td align=center><br><button width=32 height=28 back=\"borinet.Botsystem_DF_Key" + target.getCaptchaNumberPad(i) + "\" fore=\"borinet.Botsystem_DF_Key" + target.getCaptchaNumberPad(i) + "\" action=\"" + (target.getCaptchaAnswerCount() < 4 ? "bypass -h Captcha " + target.getCaptchaNumberPad(i) : "") + "\" value=\" \" /></td>");
		}
		
		for (int i = 4; i < 10; i++)
		{
			String cnumberPlaceholder = "%cnumber" + i + "%";
			html = html.replace(cnumberPlaceholder, "<td align=center><button width=32 height=28 back=\"borinet.Botsystem_DF_Key" + target.getCaptchaNumberPad(i) + "\" fore=\"borinet.Botsystem_DF_Key" + target.getCaptchaNumberPad(i) + "\" action=\"" + (target.getCaptchaAnswerCount() < 4 ? "bypass -h Captcha " + target.getCaptchaNumberPad(i) : "") + "\" value=\" \" /></td>");
		}
		target.sendPacket(new NpcHtmlMessage(html));
	}
}