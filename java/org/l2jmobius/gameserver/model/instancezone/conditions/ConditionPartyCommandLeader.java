package org.l2jmobius.gameserver.model.instancezone.conditions;

import org.l2jmobius.gameserver.model.AbstractPlayerGroup;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.InstanceTemplate;

/**
 * 인스턴트 존: 파티 또는 연합장
 * @author 보리넷 가츠
 */
public class ConditionPartyCommandLeader extends Condition
{
	public ConditionPartyCommandLeader(InstanceTemplate template, StatSet parameters, boolean onlyLeader, boolean showMessageAndHtml)
	{
		super(template, parameters, true, showMessageAndHtml);
		sendMessage("파티장 또는 연합파티장만 입장시도를 할 수 있습니다.");
	}
	
	@Override
	public boolean test(Player player, Npc npc)
	{
		final AbstractPlayerGroup group = player.getCommandChannel();
		return (player.isInParty() && player.getParty().isLeader(player)) || ((group != null) && group.isLeader(player));
	}
}