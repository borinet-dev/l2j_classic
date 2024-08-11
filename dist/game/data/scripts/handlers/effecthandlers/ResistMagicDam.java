package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistMagicDam extends AbstractStatEffect
{
	public ResistMagicDam(StatSet params)
	{
		super(params, Stat.MAGIC_DAM_RESIST);
	}
}
