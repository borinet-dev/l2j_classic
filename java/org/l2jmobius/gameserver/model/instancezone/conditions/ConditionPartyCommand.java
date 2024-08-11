package org.l2jmobius.gameserver.model.instancezone.conditions;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.InstanceTemplate;

/**
 * 파티 및 연합채널
 * @author 보리넷 가츠
 */
public class ConditionPartyCommand extends Condition
{
	public ConditionPartyCommand(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, true, showMessageAndHtml);
		sendMessage("파티 또는 연합채널에 속해있지 않으므로 입장할 수 없습니다.");
	}
	
	@Override
	public boolean test(Player player, Npc npc)
	{
		return player.isInParty() || player.isInCommandChannel();
	}
}
