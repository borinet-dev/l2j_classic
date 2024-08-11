package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistBleed extends AbstractStatEffect
{
	public ResistBleed(StatSet params)
	{
		super(params, Stat.BLEED_RESIST);
	}
}
