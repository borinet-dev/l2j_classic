package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistParalyze extends AbstractStatEffect
{
	public ResistParalyze(StatSet params)
	{
		super(params, Stat.PARALYZE_RESIST);
	}
}
