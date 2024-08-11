package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistBlunt extends AbstractStatEffect
{
	public ResistBlunt(StatSet params)
	{
		super(params, Stat.BLUNT_RESIST);
	}
}
