package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author 보리넷 가츠
 */
public class CraftingCritical extends AbstractStatAddEffect
{
	public CraftingCritical(StatSet params)
	{
		super(params, Stat.CRAFTING_CRITICAL);
	}
}
