package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistDualFist extends AbstractStatEffect
{
	public ResistDualFist(StatSet params)
	{
		super(params, Stat.DUALFIST_RESIST);
	}
}
