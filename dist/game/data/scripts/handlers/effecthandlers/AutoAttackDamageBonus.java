package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.stats.Stat;

public class AutoAttackDamageBonus extends AbstractStatPercentEffect
{
	public AutoAttackDamageBonus(StatSet params)
	{
		super(params, Stat.AUTO_ATTACK_DAMAGE_BONUS);
	}
}