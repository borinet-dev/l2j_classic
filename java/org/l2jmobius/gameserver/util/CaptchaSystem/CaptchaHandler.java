package org.l2jmobius.gameserver.util.CaptchaSystem;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.BotReportTable;
import org.l2jmobius.gameserver.instancemanager.PunishmentManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.punishment.PunishmentAffect;
import org.l2jmobius.gameserver.model.punishment.PunishmentTask;
import org.l2jmobius.gameserver.model.punishment.PunishmentType;

public class CaptchaHandler
{
	public static void AutoCaptcha(Player player)
	{
		long lastCaptchaTime = player.getQuickVarL("LastCaptcha");
		long capchaTime = Rnd.get(Config.LAST_CAPTCHA_TIME_MIN, Config.LAST_CAPTCHA_TIME_MAX);
		if (((lastCaptchaTime + (capchaTime * 60000)) > System.currentTimeMillis()) || !BotReportTable.AutoReport(player))
		{
			return;
		}
		
		long delay = Rnd.get(Config.TIME_WAIT_DELAY_MIN, Config.TIME_WAIT_DELAY_MAX);
		
		ThreadPool.schedule(() ->
		{
			player.updateCaptchaCount(0);
			CaptchaEvent.clearCaptcha(player);
			player.addQuickVar("IsCaptchaActive", true);
			player.addQuickVar("LastCaptcha", System.currentTimeMillis());
			CaptchaWindow.CaptchaWindows(player, 0);
			CaptchaTimer.getInstance().addCaptchaTimer(player);
		}, delay * 1000);
	}
	
	public static void Captcha(Player actor, Player target)
	{
		long lastCaptchaTime = target.getQuickVarL("LastCaptcha");
		long capchaTime = Rnd.get(Config.LAST_CAPTCHA_TIME_MIN, Config.LAST_CAPTCHA_TIME_MAX);
		if ((lastCaptchaTime + (capchaTime * 60000)) > System.currentTimeMillis())
		{
			return;
		}
		
		target.updateCaptchaCount(0);
		CaptchaEvent.clearCaptcha(target);
		target.addQuickVar("IsCaptchaActive", true);
		target.addQuickVar("LastCaptcha", System.currentTimeMillis());
		CaptchaWindow.CaptchaWindows(target, 0);
		CaptchaTimer.getInstance().addCaptchaTimer(target);
	}
	
	public static void AnswerCaptcha(Player actor)
	{
		CaptchaEvent event = CaptchaTimer.getInstance().getAutoMyEvent(actor);
		onCorrectCaptcha(event, actor);
	}
	
	private static void onCorrectCaptcha(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		CaptchaTimer.getInstance().removeCaptchaTimer(event, player);
		player.setBlockActions(false);
		player.setInvul(false);
		CaptchaEvent.clearCaptcha(player);
		player.updateCaptchaCount(0);
		player.deleteQuickVar("IsCaptchaActive");
		player.sendMessage("정확하게 입력하셨습니다! 즐거운 시간 되세요!");
	}
	
	public static void NoAnswerCaptcha(Player actor)
	{
		CaptchaEvent event = CaptchaTimer.getInstance().getAutoMyEvent(actor);
		onFailedCaptcha(event, actor);
	}
	
	public static void onFailedCaptcha(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		// 먼저 count를 업데이트하고 가져오기
		CaptchaTimer.getInstance().removeCaptchaTimer(event, player);
		int count = player.getCaptchaCount();
		
		if (count >= Config.CAPTCHA_COUNT)
		{
			CaptchaEvent.clearCaptcha(player);
			player.updateCaptchaCount(0);
			player.sendMessage("보안문자 입력에 실패하였습니다!");
			player.deleteQuickVar("IsCaptchaActive");
			punishment(event, player);
		}
		else
		{
			player.setBlockActions(false);
			player.setInvul(false);
			
			String name = (count == 2) ? "한번" : "두번";
			player.sendMessage("틀렸습니다. 순서대로 정확히 입력바랍니다.");
			player.sendMessage("보안문자 입력 남은 기회: " + name);
			CaptchaEvent.clearCaptcha(player);
			CaptchaWindow.CaptchaWindows(player, 0);
			CaptchaTimer.getInstance().addCaptchaTimer(player);
		}
	}
	
	public static void onMissingCaptcha(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		CaptchaEvent.clearCaptcha(player);
		player.updateCaptchaCount(0);
		player.sendMessage("보안문자 입력에 실패하였습니다!");
		player.deleteQuickVar("IsCaptchaActive");
		punishment(event, player);
	}
	
	private static void punishment(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		player.getVariables().set("BotReported", player.getVariables().getInt("BotReported", 0) + 1);
		int jailCount = player.getVariables().getInt("BotReported", 0);
		final PunishmentAffect affect = PunishmentAffect.getByName("CHARACTER");
		final PunishmentType type = PunishmentType.getByName("JAIL");
		
		if (jailCount < 5)
		{
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(player.getObjectId(), affect, type, System.currentTimeMillis() + (4320 * 60 * 1000), "<br> 오토방지 시스템의 문자입력을 시간내 하지 않았거나 3번의 입력오류.<br1>현재 <font color=LEVEL>" + (jailCount) + "번째 수감</font>입니다. 5번째 수감시 무기한 수감됩니다!!<br1>지금부터 72시간(3일)동안 수감됩니다.<br><br>문의는 디스코드 또는 master@borinet.org 로 남겨주시기 바랍니다.<br>", "시스템"));
		}
		else
		{
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(player.getObjectId(), affect, type, System.currentTimeMillis() * 2, "<br> 오토방지 시스템의 문자입력을 시간내 하지 않았거나 3번의 입력오류.<br1>현재 <font color=LEVEL>" + (jailCount) + "번째 수감</font>되어 무기한 수감됩니다!!<br1> 문의는 디스코드 또는 master@borinet.org 로 남겨주시기 바랍니다.", "시스템"));
		}
		
		CaptchaTimer.getInstance().removeCaptchaTimer(event, player);
	}
}
