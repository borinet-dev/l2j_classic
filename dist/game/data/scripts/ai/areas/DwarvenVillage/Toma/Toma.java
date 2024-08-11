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
package ai.areas.DwarvenVillage.Toma;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Toma extends AbstractNpcAI
{
	// NPC
	private static final int TOMA = 30556;
	// Locations
	private static final Location[] LOCATIONS =
	{
		new Location(172010, -173394, 3440, 25950),
		new Location(178834, -184336, -355, 41400)
	};
	// Misc
	private static final int TELEPORT_DELAY = 1800000; // 30 minutes
	private static Npc _toma;
	
	private Toma()
	{
		if (Config.MITHRIL_MINE_ENABLED && !BorinetTask._isActive)
		{
			addStartNpc(TOMA);
			addFirstTalkId(TOMA);
			addTalkId(TOMA);
			onAdvEvent("RESPAWN_TOMA", null, null);
			startQuestTimer("RESPAWN_TOMA", TELEPORT_DELAY, null, null, true);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		if (event.equals("RESPAWN_TOMA"))
		{
			if (_toma != null)
			{
				_toma.deleteMe();
			}
			_toma = addSpawn(TOMA, getRandomEntry(LOCATIONS), false, TELEPORT_DELAY);
		}
		switch (event)
		{
			case "buy":
			{
				if (hasQuestItems(player, 32773))
				{
					htmltext = "30556-2.htm";
				}
				else if (getQuestItemsCount(player, 57) < 1000000)
				{
					int fee = 1000000;
					player.sendMessage("곡괭이를 구매하기 위해서는 " + Util.formatAdena(fee) + " 아데나가 필요합니다.");
					htmltext = getHtm(player, "30556-noAdena.htm").replace("%fee%", Util.formatAdena(fee));
				}
				else
				{
					takeItems(player, 57, 1000000);
					giveItems(player, 32773, 1);
					htmltext = "30556-1.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "30556.htm";
	}
	
	public static void main(String[] args)
	{
		new Toma();
	}
}
