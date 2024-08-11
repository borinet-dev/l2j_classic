package org.l2jmobius.gameserver.util.CaptchaSystem;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
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
		if (((lastCaptchaTime + (Config.LAST_CAPTCHA_TIME * 60000)) > System.currentTimeMillis()) || !BotReportTable.AutoReport(player))
		{
			return;
		}
		
		ThreadPool.schedule(() ->
		{
			player.updateCaptchaCount(0);
			CaptchaEvent.clearCaptcha(player);
			player.addQuickVar("IsCaptchaActive", true);
			player.addQuickVar("LastCaptcha", System.currentTimeMillis());
			CaptchaWindow.CaptchaWindows(player, 0);
			CaptchaTimer.getInstance().addCaptchaTimer(player);
		}, Config.TIME_WAIT_DELAY * 1000);
	}
	
	public static void Captcha(Player actor, Player target)
	{
		long lastCaptchaTime = target.getQuickVarL("LastCaptcha");
		if ((lastCaptchaTime + (Config.LAST_CAPTCHA_TIME * 60000)) > System.currentTimeMillis())
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
		if (player != null)
		{
			CaptchaTimer.getInstance().removeCaptchaTimer(event, player);
			CaptchaEvent.clearCaptcha(player);
			player.updateCaptchaCount(0);
			player.deleteQuickVar("IsCaptchaActive");
			player.sendMessage("정확하게 입력하셨습니다! 즐거운 시간 되세요!");
		}
	}
	
	public static void NoAnswerCaptcha(Player actor)
	{
		CaptchaEvent event = CaptchaTimer.getInstance().getAutoMyEvent(actor);
		onFailedCaptcha(event, actor);
	}
	
	public static void onFailedCaptcha(CaptchaEvent event, Player player)
	{
		if ((player != null) && player.isOnline())
		{
			CaptchaTimer.getInstance().removeCaptchaTimer(event, player);
			int count = player.getCaptchaCount();
			
			if (count >= Config.CAPTCHA_COUNT)
			{
				CaptchaEvent.clearCaptcha(player);
				player.updateCaptchaCount(0);
				player.sendMessage("보안문자 입력에 실패하였습니다!");
				player.deleteQuickVar("IsCaptchaActive");
				punishment(player);
			}
			else
			{
				int total = 3 - player.getCaptchaCount();
				String name = (total == 1) ? "한번" : "두번";
				player.sendMessage("틀렸습니다. 순서대로 정확히 입력바랍니다.");
				player.sendMessage("보안문자 입력 남은 기회: " + name);
				CaptchaEvent.clearCaptcha(player);
				CaptchaWindow.CaptchaWindows(player, 0);
				CaptchaTimer.getInstance().addCaptchaTimer(player);
			}
		}
	}
	
	private static void punishment(Player player)
	{
		if (!player.isOnline())
		{
			return;
		}
		int jailCount = player.getVariables().getInt("BotReported", 0);
		final PunishmentAffect affect = PunishmentAffect.getByName("CHARACTER");
		final PunishmentType type = PunishmentType.getByName("JAIL");
		if (jailCount < 4)
		{
			player.getVariables().set("BotReported", jailCount + 1);
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(player.getObjectId(), affect, type, System.currentTimeMillis() + (4320 * 60 * 1000), "매크로 자동사냥<br> 오토방지 시스템의 문자입력을 시간내 하지 않았거나 3번의 입력오류.<br1>현재 <font color=LEVEL>" + (jailCount + 1) + "번째 수감</font>입니다. 5번째 수감시 무기한 수감됩니다!!<br1> 72시간동안 수감됩니다.<br><br>문의는 디스코드에 남겨주시기 바랍니다.<br>", "시스템"));
		}
		else
		{
			player.getVariables().set("BotReported", jailCount + 1);
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(player.getObjectId(), affect, type, System.currentTimeMillis() * 2, "매크로 자동사냥<br> 오토방지 시스템의 문자입력을 시간내 하지 않았거나 3번의 입력오류.<br1>현재 <font color=LEVEL>" + (jailCount + 1) + "번째 수감</font>되어 무기한 수감됩니다!!<br1> 문의는 디스코드에 남겨주시기 바랍니다.", "시스템"));
		}
		player.setInvul(false);
		player.setBlockActions(false);
	}
}
