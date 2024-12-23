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
package handlers.communityboard;

import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.enums.TaxType;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IWriteBoardHandler;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.util.Util;

/**
 * Region board.
 * @author Zoey76
 */
public class RegionBoard implements IWriteBoardHandler
{
	// Region data
	// @formatter:off
	private static final int[] REGIONS = { 1049, 1052, 1053, 1057, 1060, 1059, 1248, 1247, 1056 };
	// @formatter:on
	private static final String[] COMMANDS =
	{
		// "_bbsloc"
	};
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		return COMMANDS;
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		if (command.equals("_bbsloc"))
		{
			CommunityBoardHandler.getInstance().addBypass(player, "Region", command);
			
			final String list = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/region_list.html");
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < REGIONS.length; i++)
			{
				final Castle castle = CastleManager.getInstance().getCastleById(i + 1);
				final Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
				String link = list.replace("%region_id%", String.valueOf(i));
				link = link.replace("%region_name%", String.valueOf(REGIONS[i]));
				link = link.replace("%region_owning_clan%", (clan != null ? clan.getName() : "NPC"));
				link = link.replace("%region_owning_clan_alliance%", ((clan != null) && (clan.getAllyName() != null) ? clan.getAllyName() : ""));
				link = link.replace("%region_tax_rate%", castle.getTaxPercent(TaxType.BUY) + "%");
				sb.append(link);
			}
			
			String html = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/region.html");
			html = html.replace("%region_list%", sb.toString());
			CommunityBoardHandler.separateAndSend(html, player);
		}
		else if (command.startsWith("_bbsloc;"))
		{
			CommunityBoardHandler.getInstance().addBypass(player, "Region>", command);
			
			final String id = command.replace("_bbsloc;", "");
			if (!Util.isDigit(id))
			{
				LOG.warning(RegionBoard.class.getSimpleName() + ": " + player + " sent and invalid region bypass " + command + "!");
				return false;
			}
			
			// TODO: Implement.
		}
		return true;
	}
	
	@Override
	public boolean writeCommunityBoardCommand(Player player, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		// TODO: Implement.
		return false;
	}
}
