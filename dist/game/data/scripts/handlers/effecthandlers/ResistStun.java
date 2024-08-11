package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 가츠
 */
public class ResistStun extends AbstractStatEffect
{
	public ResistStun(StatSet params)
	{
		super(params, Stat.STUN_RESIST);
	}
}
