package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistHold extends AbstractStatEffect
{
	public ResistHold(StatSet params)
	{
		super(params, Stat.HOLD_RESIST);
	}
}
