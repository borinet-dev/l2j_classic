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
import org.l2jmobius.gameserver.model.zone.ZoneId;

public class CaptchaHandler
{
	public static void Captcha(Player player, boolean isAuto)
	{
		if ((player == null) || !player.isOnline())
		{
			return; // 대상이 유효하지 않으면 종료
		}
		
		// 캡챠 설정 로직
		Runnable setupCaptcha = () ->
		{
			if (popupCheck(player, isAuto))
			{
				player.updateCaptchaCount(0);
				player.clearCaptcha();
				player.addQuickVar("IsCaptchaActive", true);
				player.addQuickVar("LastCaptcha", System.currentTimeMillis());
				player.setEscDisabled(true);
				player.startPopupDelay();
				CaptchaWindow.CaptchaWindows(player, 0);
			}
		};
		
		// AutoCaptcha는 랜덤 딜레이 추가
		if (isAuto)
		{
			long delay = Rnd.get(Config.TIME_WAIT_DELAY_MIN, Config.TIME_WAIT_DELAY_MAX);
			ThreadPool.schedule(setupCaptcha, delay * 1000);
		}
		else
		{
			setupCaptcha.run(); // 일반 Captcha는 즉시 실행
		}
	}
	
	private static boolean popupCheck(Player player, boolean isAuto)
	{
		if ((player == null) || !player.isOnline())
		{
			return false; // 대상이 유효하지 않으면 종료
		}
		
		long lastCaptchaTime = player.getQuickVarL("LastCaptcha");
		long captchaInterval = Rnd.get(Config.LAST_CAPTCHA_TIME_MIN, Config.LAST_CAPTCHA_TIME_MAX) * 60000;
		
		// Captcha 발동 조건 확인
		if ((lastCaptchaTime + captchaInterval) > System.currentTimeMillis())
		{
			return false;
		}
		
		if (!player.getActingPlayer().isInBattle() || !player.isInCombat() || player.getActingPlayer().isSitting() || player.getActingPlayer().isFishing() || player.getActingPlayer().isInStoreMode() || player.isDead() || player.getActingPlayer().isInSiege())
		{
			return false;
		}
		
		if (player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.PVP) || player.isInsideZone(ZoneId.SIEGE))
		{
			return false;
		}
		
		if (player.getActingPlayer().getVariables().getBoolean("자동따라가기", false))
		{
			return false;
		}
		
		if (player.isInInstance())
		{
			return false;
		}
		
		// AutoCaptcha 추가 조건 확인
		if (isAuto && !BotReportTable.AutoReport(player))
		{
			return false;
		}
		
		return true;
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
		
		reset(event, player);
		player.setEscDisabled(false);
		player.sendMessage("정확하게 입력하셨습니다! 즐거운 시간 되세요!");
	}
	
	public static void NoAnswerCaptcha(Player actor)
	{
		CaptchaEvent event = CaptchaTimer.getInstance().getAutoMyEvent(actor);
		onFailedCaptcha(event, actor);
	}
	
	private static void onFailedCaptcha(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		boolean isCaptchaActive = player.getQuickVarB("IsCaptchaActive", false);
		if (!isCaptchaActive)
		{
			reset(event, player);
			return;
		}
		
		// 먼저 count를 업데이트하고 가져오기
		CaptchaTimer.getInstance().removeCaptchaTimer(event, player);
		int count = player.getCaptchaCount();
		
		if (count >= Config.CAPTCHA_COUNT)
		{
			punishment(event, player);
		}
		else
		{
			String name = (count == 2) ? "한번" : "두번";
			player.sendMessage("틀렸습니다. 순서대로 정확히 입력바랍니다.");
			player.sendMessage("보안문자 입력 남은 기회: " + name);
			player.clearCaptcha();
			player.startPopupDelay();
			CaptchaWindow.CaptchaWindows(player, 0);
		}
	}
	
	public static void onMissingCaptcha(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		boolean isCaptchaActive = player.getQuickVarB("IsCaptchaActive", false);
		if (!isCaptchaActive)
		{
			reset(event, player);
			return;
		}
		
		punishment(event, player);
	}
	
	private static void punishment(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		player.sendMessage("보안문자 입력에 실패하였습니다!");
		reset(event, player);
		player.setEscDisabled(false);
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
	}
	
	private static void reset(CaptchaEvent event, Player player)
	{
		CaptchaTimer.getInstance().removeCaptchaTimer(event, player);
		player.deleteQuickVar("IsCaptchaActive");
		player.stopPopupDelay();
		player.setBlockActions(false);
		player.setInvul(false);
		player.clearCaptcha();
		player.updateCaptchaCount(0);
	}
}
