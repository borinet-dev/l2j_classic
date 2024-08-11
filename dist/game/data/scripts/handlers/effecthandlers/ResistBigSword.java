package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistBigSword extends AbstractStatEffect
{
	public ResistBigSword(StatSet params)
	{
		super(params, Stat.BIGSWORD_RESIST);
	}
}
