package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistBow extends AbstractStatEffect
{
	public ResistBow(StatSet params)
	{
		super(params, Stat.BOW_RESIST);
	}
}
