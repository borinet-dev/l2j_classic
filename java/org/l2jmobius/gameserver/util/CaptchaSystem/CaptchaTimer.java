package org.l2jmobius.gameserver.util.CaptchaSystem;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * 보리넷 가츠
 */
public class CaptchaTimer
{
	private final ConcurrentHashMap<String, CaptchaEvent> captchaEventMap = new ConcurrentHashMap<>();
	private final List<FailedBotReporter> failedBotReporters;
	
	protected CaptchaTimer()
	{
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
		captchaEventMap.put(player.getName(), new CaptchaEvent(player, System.currentTimeMillis()));
	}
	
	public void removeCaptchaTimer(CaptchaEvent event, Player player)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		captchaEventMap.remove(player.getName());
	}
	
	public CaptchaEvent getAutoMyEvent(Player target)
	{
		return captchaEventMap.get(target.getName());
	}
	
	protected Iterable<CaptchaEvent> getCaptchaEventLists()
	{
		return captchaEventMap.values();
	}
	
	protected class CaptchaTimerThread implements Runnable
	{
		@Override
		public void run()
		{
			long currentTime = System.currentTimeMillis();
			captchaEventMap.values().removeIf(event ->
			{
				if ((event.getStartDate() + (Config.CAPTCHA_ANSWER_SECONDS * 1000)) <= currentTime)
				{
					CaptchaHandler.onMissingCaptcha(event, event.getPlayer());
					return true;
				}
				return false;
			});
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
