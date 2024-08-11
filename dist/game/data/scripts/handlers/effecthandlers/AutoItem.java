package handlers.effecthandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.taskmanager.auto.AutoItemTaskManager;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class AutoItem extends AbstractEffect
{
	public AutoItem(StatSet params)
	{
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (BorinetUtil.usingAutoItem(effector.getActingPlayer()))
		{
			AutoItemTaskManager.getInstance().removeAutoItem(effector.getActingPlayer());
			effector.getActingPlayer().getVariables().remove("자동아이템사용");
			sendMessage(effector, "자동 아이템 사용이 중지되었습니다.");
		}
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (AutoItemTaskManager.getInstance().checkItemRegister(effector.getActingPlayer()))
		{
			AutoItemTaskManager.getInstance().addAutoItem(effector.getActingPlayer());
			effector.getActingPlayer().getVariables().set("자동아이템사용", true);
			sendMessage(effector, "지금부터 자동 아이템 사용을 시작합니다.");
		}
		else
		{
			effector.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CommonSkill.AUTO_ITEM.getId());
			sendMessage(effector, "자동으로 사용할 아이템을 먼저 등록하여 주십시오.");
			AutoItemTaskManager.getInstance().showHtml(effector.getActingPlayer(), effector.getActingPlayer().getVariables().getInt("자동아이템_페이지", 0));
		}
	}
	
	private void sendMessage(Creature effector, String message)
	{
		effector.getActingPlayer().sendMessage(message);
		effector.getActingPlayer().sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, message));
	}
}
