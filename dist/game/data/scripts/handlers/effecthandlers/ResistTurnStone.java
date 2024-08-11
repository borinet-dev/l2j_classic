package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistTurnStone extends AbstractStatEffect
{
	public ResistTurnStone(StatSet params)
	{
		super(params, Stat.TURN_STONE_RESIST);
	}
}
