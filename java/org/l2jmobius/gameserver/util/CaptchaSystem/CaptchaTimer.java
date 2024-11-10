package org.l2jmobius.gameserver.util.CaptchaSystem;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * 보리넷 가츠
 */
public class CaptchaTimer
{
	private final List<CaptchaEvent> captchaEventList;
	private final List<FailedBotReporter> failedBotReporters;
	
	protected CaptchaTimer()
	{
		captchaEventList = new CopyOnWriteArrayList<>();
		failedBotReporters = new CopyOnWriteArrayList<>();
		ThreadPool.execute(new CaptchaTimerThread());
	}
	
	public boolean canReportBotAgain(Player player)
	{
		FailedBotReporter reporter = getBotReporter(player);
		return reporter == null;
	}
	
	private FailedBotReporter getBotReporter(Player player)
	{
		for (FailedBotReporter reporter : failedBotReporters)
		{
			if (reporter.isBotReporter(player))
			{
				return reporter;
			}
		}
		return null;
	}
	
	public void addCaptchaTimer(Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		player.setBlockActions(true);
		player.setInvul(true);
		captchaEventList.add(new CaptchaEvent(player, System.currentTimeMillis()));
		player.startPopupDelay();
	}
	
	public void removeCaptchaTimer(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		player.stopPopupDelay();
		captchaEventList.remove(event);
	}
	
	public CaptchaEvent getAutoMyEvent(Player target)
	{
		for (CaptchaEvent events : captchaEventList)
		{
			if (events.getActorName().equals(target.getName()))
			{
				return events;
			}
		}
		return null;
	}
	
	protected Iterable<CaptchaEvent> getCaptchaEventLists()
	{
		return captchaEventList;
	}
	
	protected class CaptchaTimerThread implements Runnable
	{
		@Override
		public void run()
		{
			long currentTime = System.currentTimeMillis();
			for (CaptchaEvent event : getCaptchaEventLists())
			{
				if ((event.getStartDate() + (Config.CAPTCHA_ANSWER_SECONDS * 1000)) <= currentTime)
				{
					Player player = event.getPlayer();
					CaptchaHandler.onMissingCaptcha(event, player);
				}
			}
			
			ThreadPool.schedule(this, 1000);
		}
	}
	
	private static class FailedBotReporter
	{
		private final String accountName;
		
		private FailedBotReporter(String accountName, long lastReportTime)
		{
			this.accountName = accountName;
		}
		
		private boolean isBotReporter(Player player)
		{
			if (player.getAccountName().equals(accountName))
			{
				return true;
			}
			return false;
		}
	}
	
	public static CaptchaTimer getInstance()
	{
		return CaptchaTimerHolder.instance;
	}
	
	private static class CaptchaTimerHolder
	{
		protected static final CaptchaTimer instance = new CaptchaTimer();
	}
}
