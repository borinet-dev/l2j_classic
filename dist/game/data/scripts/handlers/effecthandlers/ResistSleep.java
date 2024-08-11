package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistSleep extends AbstractStatEffect
{
	public ResistSleep(StatSet params)
	{
		super(params, Stat.SLEEP_RESIST);
	}
}
