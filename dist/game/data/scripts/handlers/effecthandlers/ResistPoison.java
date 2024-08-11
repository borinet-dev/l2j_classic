package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistPoison extends AbstractStatEffect
{
	public ResistPoison(StatSet params)
	{
		super(params, Stat.POISON_RESIST);
	}
}
