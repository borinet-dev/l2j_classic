package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistDagger extends AbstractStatEffect
{
	public ResistDagger(StatSet params)
	{
		super(params, Stat.DAGGER_RESIST);
	}
}
