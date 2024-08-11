package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistSword extends AbstractStatEffect
{
	public ResistSword(StatSet params)
	{
		super(params, Stat.SWORD_RESIST);
	}
}
