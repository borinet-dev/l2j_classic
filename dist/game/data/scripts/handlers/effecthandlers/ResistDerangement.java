package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistDerangement extends AbstractStatEffect
{
	public ResistDerangement(StatSet params)
	{
		super(params, Stat.DERANGEMENT_RESIST);
	}
}
