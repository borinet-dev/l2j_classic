package handlers.voicedcommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureTeleport;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureTeleported;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.taskmanager.auto.AutoItemTaskManager;
import org.l2jmobius.gameserver.util.BorinetUtil;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class AutoUseItem extends AbstractNpcAI implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"A자동아이템설정",
		"A자동아이템포션설정",
		"A자동아이템포션취소",
		"A자동아이템등록",
		"A자동아이템시작",
		"A자동아이템취소",
		"A자동아이템다음"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		if (activeChar.isOnEvent())
		{
			activeChar.sendMessage("이벤트 참가 중에는 사용할 수 없습니다.");
			activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "이벤트 참가 중에는 사용할 수 없습니다."));
			
			return false;
		}
		
		if (command.equals("A자동아이템설정"))
		{
			activeChar.getVariables().set("자동아이템_페이지", 1);
			AutoItemTaskManager.getInstance().showHtml(activeChar, 1);
		}
		else if (command.equals("A자동아이템다음"))
		{
			activeChar.getVariables().set("자동아이템_페이지", 0);
			AutoItemTaskManager.getInstance().showHtml(activeChar, 0);
		}
		else if (command.startsWith("A자동아이템등록"))
		{
			final String autoItem = target.replace("A자동아이템등록", " ");
			final String[] items = autoItem.split(" ");
			final int itemNum = Integer.parseInt(items[0]);
			
			if (activeChar.getVariables().getBoolean("자동아이템_" + itemNum, false))
			{
				activeChar.getVariables().remove("자동아이템_" + itemNum);
			}
			else
			{
				switch (itemNum)
				{
					case 19:
					{
						activeChar.getVariables().remove("자동아이템_20");
						break;
					}
					case 20:
					{
						activeChar.getVariables().remove("자동아이템_19");
						break;
					}
					case 21:
					{
						activeChar.getVariables().remove("자동아이템_22");
						break;
					}
					case 22:
					{
						activeChar.getVariables().remove("자동아이템_21");
						break;
					}
					case 28:
					{
						activeChar.getVariables().remove("자동아이템_29");
						break;
					}
					case 29:
					{
						activeChar.getVariables().remove("자동아이템_28");
						break;
					}
				}
				activeChar.getVariables().set("자동아이템_" + itemNum, true);
			}
			AutoItemTaskManager.getInstance().showHtml(activeChar, activeChar.getVariables().getInt("자동아이템_페이지", 0));
		}
		else if (command.equals("A자동아이템시작"))
		{
			if (BorinetUtil.usingAutoItem(activeChar))
			{
				activeChar.sendMessage("이미 자동 아이템 사용 중 입니다.");
				activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "이미 자동 아이템 사용 중 입니다."));
			}
			else
			{
				CommonSkill.AUTO_ITEM.getSkill().applyEffects(activeChar, activeChar);
			}
			AutoItemTaskManager.getInstance().showHtml(activeChar, activeChar.getVariables().getInt("자동아이템_페이지", 0));
		}
		else if (command.equals("A자동아이템취소"))
		{
			activeChar.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CommonSkill.AUTO_ITEM.getId());
			AutoItemTaskManager.getInstance().showHtml(activeChar, activeChar.getVariables().getInt("자동아이템_페이지", 0));
		}
		else if (command.startsWith("A자동아이템포션설정"))
		{
			final String autoPotion = target.replace("A자동아이템포션설정", " ");
			final String[] loc = autoPotion.split(" ");
			int item_hp = 0;
			
			try
			{
				final int perc_hp = Integer.parseInt(loc[0]);
				final String itemhp = loc[1];
				final int perc_mp = Integer.parseInt(loc[2]);
				
				switch (itemhp)
				{
					case "강력체력회복제":
						item_hp = 1539;
						break;
					case "체력회복제":
						item_hp = 1060;
						break;
					case "고급체력회복제":
						item_hp = 1061;
						break;
					case "체력회복보조물약":
						item_hp = 725;
						break;
				}
				AutoItemTaskManager.getInstance().addAutoPotion(activeChar);
				activeChar.getVariables().set("AUTO_POTION_HP_PERCENT", perc_hp < 1 ? 1 : perc_hp);
				activeChar.getVariables().set("AUTO_POTION_HP_ITEM", item_hp);
				activeChar.getVariables().set("AUTO_POTION_MP_PERCENT", perc_mp < 1 ? 1 : perc_mp);
				activeChar.sendMessage("자동 포션이 설정이 등록 되었습니다.");
				activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동포션이 설정이 등록 되었습니다."));
				AutoItemTaskManager.getInstance().showHtml(activeChar, activeChar.getVariables().getInt("자동아이템_페이지", 0));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("설정값이 없습니다. 다시 입력해 주세요.");
				activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "설정값이 없습니다. 다시 입력해 주세요."));
				AutoItemTaskManager.getInstance().showHtml(activeChar, activeChar.getVariables().getInt("자동아이템_페이지", 0));
			}
		}
		else if (command.equals("A자동아이템포션취소"))
		{
			AutoItemTaskManager.getInstance().removeAutoPotion(activeChar);
			activeChar.sendMessage("자동포선 설정이 취소되었습니다.");
			activeChar.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동포션 설정이 취소되었습니다."));
			AutoItemTaskManager.getInstance().showHtml(activeChar, activeChar.getVariables().getInt("자동아이템_페이지", 0));
		}
		return true;
	}
	
	// 자동 아이템 변수
	@RegisterEvent(EventType.ON_CREATURE_TELEPORT)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onCreatureTeleported(OnCreatureTeleport event)
	{
		Player player = (Player) event.getCreature();
		player.addQuickVar("isTeleporting", true);
	}
	
	@RegisterEvent(EventType.ON_CREATURE_TELEPORTED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onCreatureTeleported(OnCreatureTeleported event)
	{
		Player player = (Player) event.getCreature();
		player.deleteQuickVar("isTeleporting");
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}