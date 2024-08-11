package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistSilence extends AbstractStatEffect
{
	public ResistSilence(StatSet params)
	{
		super(params, Stat.SILENCE_RESIST);
	}
}
