/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.bypasshandlers;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Teleporter;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class Link implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Link"
	};
	
	private static final Set<String> VALID_LINKS = new HashSet<>();
	static
	{
		VALID_LINKS.add("common/craft_01.htm");
		VALID_LINKS.add("common/craft_02.htm");
		VALID_LINKS.add("common/runes_01.htm");
		VALID_LINKS.add("common/runes_01.htm");
		VALID_LINKS.add("common/sealed_runes_02.htm");
		VALID_LINKS.add("common/sealed_runes_03.htm");
		VALID_LINKS.add("common/sealed_runes_04.htm");
		VALID_LINKS.add("common/sealed_runes_05.htm");
		VALID_LINKS.add("common/sealed_runes_06.htm");
		VALID_LINKS.add("common/sealed_runes_07.htm");
		VALID_LINKS.add("common/sealed_runes_08.htm");
		VALID_LINKS.add("common/sealed_runes_09.htm");
		VALID_LINKS.add("common/skill_enchant_help.htm");
		VALID_LINKS.add("common/skill_enchant_help_01.htm");
		VALID_LINKS.add("common/skill_enchant_help_02.htm");
		VALID_LINKS.add("common/skill_enchant_help_03.htm");
		VALID_LINKS.add("default/BlessingOfProtection.htm");
		VALID_LINKS.add("default/SupportMagic.htm");
		VALID_LINKS.add("fisherman/fishing_manual001.htm");
		VALID_LINKS.add("fisherman/fishing_manual002.htm");
		VALID_LINKS.add("fisherman/fishing_manual003.htm");
		VALID_LINKS.add("fisherman/fishing_manual004.htm");
		VALID_LINKS.add("fisherman/fishing_manual005.htm");
		VALID_LINKS.add("fisherman/fishing_manual008.htm");
		VALID_LINKS.add("fortress/foreman.htm");
		VALID_LINKS.add("petmanager/evolve.htm");
		VALID_LINKS.add("petmanager/exchange.htm");
		VALID_LINKS.add("petmanager/instructions.htm");
		VALID_LINKS.add("petmanager/dragon_evolution.htm");
		VALID_LINKS.add("petmanager/red_dragon_evolution.htm");
		VALID_LINKS.add("warehouse/clanwh.htm");
		VALID_LINKS.add("warehouse/privatewh.htm");
		VALID_LINKS.add("guide/NewbieGuide-1.htm");
		VALID_LINKS.add("guide/NewbieGuide-2.htm");
		VALID_LINKS.add("guide/NewbieGuide-3.htm");
		VALID_LINKS.add("guide/NewbieGuide-4.htm");
		VALID_LINKS.add("guide/NewbieGuide-5.htm");
		VALID_LINKS.add("guide/NewbieGuide-6.htm");
		VALID_LINKS.add("guide/RaidManager.htm");
		VALID_LINKS.add("guide/RaidManager-level.htm");
		VALID_LINKS.add("guide/NewbieShop.htm");
		VALID_LINKS.add("guide/NewbieSupport.htm");
	}
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		final String htmlPath = command.substring(4).trim();
		if (htmlPath.isEmpty())
		{
			LOGGER.warning(player + " sent empty link html!");
			return false;
		}
		
		if (htmlPath.contains(".."))
		{
			LOGGER.warning(player + " sent invalid link html: " + htmlPath);
			return false;
		}
		
		String content = VALID_LINKS.contains(htmlPath) ? HtmCache.getInstance().getHtm(player, "data/html/" + htmlPath) : null;
		// Precaution.
		if (htmlPath.startsWith("teleporter/") && !(player.getTarget() instanceof Teleporter))
		{
			content = null;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(target != null ? target.getObjectId() : 0);
		if (content != null)
		{
			html.setHtml(content.replace("%objectId%", String.valueOf(target != null ? target.getObjectId() : 0)));
		}
		player.sendPacket(html);
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
