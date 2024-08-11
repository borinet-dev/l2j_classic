package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistPole extends AbstractStatEffect
{
	public ResistPole(StatSet params)
	{
		super(params, Stat.POLE_RESIST);
	}
}
