package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistDual extends AbstractStatEffect
{
	public ResistDual(StatSet params)
	{
		super(params, Stat.DUAL_RESIST);
	}
}
