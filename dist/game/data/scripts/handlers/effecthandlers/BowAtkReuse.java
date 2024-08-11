package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

public class BowAtkReuse extends AbstractStatEffect
{
	public BowAtkReuse(StatSet params)
	{
		super(params, Stat.ATK_REUSE);
	}
}
