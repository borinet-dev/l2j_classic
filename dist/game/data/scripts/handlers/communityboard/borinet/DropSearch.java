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
package handlers.communityboard.borinet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.data.xml.SpawnData;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.holders.DropGroupHolder;
import org.l2jmobius.gameserver.model.holders.DropHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.spawns.NpcSpawnTemplate;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.network.serverpackets.ShowMiniMap;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.CustomDropHolder;
import org.l2jmobius.gameserver.util.DropCalculate;

import handlers.voicedcommandhandlers.DropSearchCommand;

public class DropSearch implements IParseBoardHandler
{
	private static final String HEADER_PATH = "data/html/CommunityBoard/Custom/header.htm";
	private static final String DEFUALT_PATH = "data/html/CommunityBoard/Custom/";
	
	private static final String[] COMMAND =
	{
		"_bbsdropsearch",
		"_bbs_search_drop",
		"_bbs_npc_trace"
	};
	
	private static final String[] CUSTOM_COMMANDS =
	{
		"return"
	};
	private final static Map<Integer, List<CustomDropHolder>> DROP_INDEX_CACHE = new HashMap<>();
	
	// nonsupport items
	private static final Set<Integer> BLOCK_ID = new HashSet<>();
	static
	{
		BLOCK_ID.add(Inventory.ADENA_ID);
	}
	
	public DropSearch()
	{
		buildDropIndex();
	}
	
	private void buildDropIndex()
	{
		NpcData.getInstance().getTemplates(npc -> npc.getDropGroups() != null).forEach(npcTemplate ->
		{
			for (DropGroupHolder dropGroup : npcTemplate.getDropGroups())
			{
				final double chance = dropGroup.getChance() / 100;
				for (DropHolder dropHolder : dropGroup.getDropList())
				{
					addToDropList(npcTemplate, new DropHolder(dropHolder.getDropType(), dropHolder.getItemId(), dropHolder.getMin(), dropHolder.getMax(), dropHolder.getChance() * chance));
				}
			}
		});
		NpcData.getInstance().getTemplates(npc -> npc.getDropList() != null).forEach(npcTemplate ->
		{
			for (DropHolder dropHolder : npcTemplate.getDropList())
			{
				addToDropList(npcTemplate, dropHolder);
			}
		});
		NpcData.getInstance().getTemplates(npc -> npc.getSpoilList() != null).forEach(npcTemplate ->
		{
			for (DropHolder dropHolder : npcTemplate.getSpoilList())
			{
				addToDropList(npcTemplate, dropHolder);
			}
		});
		
		DROP_INDEX_CACHE.values().stream().forEach(l -> l.sort((d1, d2) -> Byte.valueOf(d1.npcLevel).compareTo(Byte.valueOf(d2.npcLevel))));
	}
	
	private void addToDropList(NpcTemplate npcTemplate, DropHolder dropHolder)
	{
		if (BLOCK_ID.contains(dropHolder.getItemId()))
		{
			return;
		}
		
		List<CustomDropHolder> dropList = DROP_INDEX_CACHE.get(dropHolder.getItemId());
		if (dropList == null)
		{
			dropList = new ArrayList<>();
			DROP_INDEX_CACHE.put(dropHolder.getItemId(), dropList);
		}
		
		dropList.add(new CustomDropHolder(npcTemplate, dropHolder));
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		final String header = HtmCache.getInstance().getHtm(player, HEADER_PATH);
		final String[] params = command.split(" ");
		String html = null;
		
		switch (params[0])
		{
			case "_bbsdropsearch":
			{
				DropSearchCommand.showHtml(player, "", "");
				break;
			}
			case "return":
			{
				html = HtmCache.getInstance().getHtm(player, DEFUALT_PATH + "dropsearch/main.htm");
				break;
			}
			case "_bbs_search_drop":
			{
				html = HtmCache.getInstance().getHtm(player, DEFUALT_PATH + "dropsearch/serchitem.htm");
				final DecimalFormat chanceFormat = new DecimalFormat("#.#");
				final int itemId = Integer.parseInt(params[1]);
				int page = Integer.parseInt(params[2]);
				final List<CustomDropHolder> list = DROP_INDEX_CACHE.get(itemId);
				int totalItems = list.size();
				int itemsPerPage = 11;
				int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
				
				final int start = (page - 1) * itemsPerPage;
				final int end = Math.min(totalItems - 1, (start + itemsPerPage) - 1);
				final StringBuilder builder = new StringBuilder();
				
				for (int index = start; index <= end; index++)
				{
					final CustomDropHolder cbDropHolder = list.get(index);
					final ItemTemplate item = ItemTable.getInstance().getTemplate(cbDropHolder.itemId);
					// 수식을 DropCalculate로 이동
					double[] dropRates = DropCalculate.calculateDropRates(player, cbDropHolder, item);
					double rateChance = dropRates[0];
					double rateAmount = dropRates[1];
					
					builder.append("<table border=0 width=772 bgcolor=0d1b25>");
					builder.append("<tr>");
					builder.append("<td width=212 align=CENTER>").append(cbDropHolder.npcName).append("</td>");
					builder.append("<td width=70 align=CENTER>").append(cbDropHolder.npcLevel).append("</td>");
					builder.append("<td width=100 align=CENTER>").append(cbDropHolder.isRaid ? "레이드" : "일반").append("</td>");
					builder.append("<td width=70 align=CENTER>").append(cbDropHolder.isSpoil ? "스포일" : "드랍").append("</td>");
					// builder.append("<td width=200 align=CENTER>").append("최소 ").append(Math.round(cbDropHolder.min * rateAmount)).append("개 / 최대 ").append(Math.round(cbDropHolder.max * rateAmount)).append("개").append("</td>");
					
					if ((cbDropHolder.min * rateAmount) == (cbDropHolder.max * rateAmount))
					{
						builder.append("<td width=100 align=CENTER>").append(Math.round(cbDropHolder.min * rateAmount)).append("개").append("</td>");
					}
					else
					{
						builder.append("<td width=100 align=CENTER>").append(Math.round(cbDropHolder.min * rateAmount)).append("개 ~ ").append(Math.round(cbDropHolder.max * rateAmount)).append("개").append("</td>");
					}
					builder.append("<td width=90 align=CENTER>").append(chanceFormat.format(Math.min(cbDropHolder.chance * rateChance, 100))).append("%").append("</td>");
					// builder.append("<td width=130 align=CENTER>").append("<a action=\"bypass _bbs_npc_trace " + cbDropHolder.npcId + "\">").append("위치보기").append("</a>").append("</td>");
					builder.append("<td width=128 align=CENTER>").append("<button value=\"위치보기\" action=\"bypass _bbs_npc_trace " + cbDropHolder.npcId + "\" width=100 height=15></button>").append("</td>");
					builder.append("</tr>");
					builder.append("</table><table>");
					builder.append("<tr>");
					builder.append("<td width=765 height=1>").append("<img src=\"L2UI.squaregray\" width=765 height=1/>").append("</td>");
					builder.append("</tr>");
					builder.append("</table>");
				}
				
				html = html.replace("%itemResult%", builder.toString());
				builder.setLength(0);
				
				builder.append("<tr><td align=\"left\" width=380>");
				if (page > 1)
				{
					builder.append("<button value=\"이전 페이지\" action=\"bypass -h _bbs_search_drop " + itemId + " " + (page - 1) + "\" width=100 height=25>");
				}
				builder.append("</td>");
				builder.append("<td align=\"right\" width=380>");
				if (page < totalPages)
				{
					builder.append("<button value=\"다음 페이지\" action=\"bypass -h _bbs_search_drop " + itemId + " " + (page + 1) + "\" width=100 height=25>");
				}
				builder.append("</td></tr>");
				
				html = html.replace("%itempages%", builder.toString());
				break;
			}
			case "_bbs_npc_trace":
			{
				final int npcId = Integer.parseInt(params[1]);
				final List<NpcSpawnTemplate> spawnList = SpawnData.getInstance().getNpcSpawns(npc -> npc.getId() == npcId);
				if (spawnList.isEmpty())
				{
					player.sendMessage("위치를 찾을 수 없습니다. 레이드보스 또는 인스턴트 던전에서 드랍될 수 있습니다.");
				}
				else
				{
					player.sendPacket(new ShowMiniMap(0));
					
					final NpcSpawnTemplate spawn = spawnList.get(Rnd.get(spawnList.size()));
					player.getRadar().addMarker(spawn.getSpawnLocation().getX(), spawn.getSpawnLocation().getY(), spawn.getSpawnLocation().getZ());
					player.sendPacket(new ShowBoard());
				}
				break;
			}
		}
		
		if (html != null)
		{
			html = html.replace("%header%", header);
			html = html.replace("%mainbanner%", BorinetHtml.getBannerForRace(player));
			CommunityBoardHandler.separateAndSend(html, player);
		}
		
		return false;
	}
	
	public static String buildItemSearchResult(String itemName, int pageNumber)
	{
		int itemsPerPage = 21;
		int limit = 0;
		int startIndex = (pageNumber - 1) * itemsPerPage;
		final Set<Integer> existInDropData = DROP_INDEX_CACHE.keySet();
		final List<ItemTemplate> items = new ArrayList<>();
		
		for (ItemTemplate item : ItemTable.getInstance().getAllItems())
		{
			if (item == null)
			{
				continue;
			}
			
			if (!existInDropData.contains(item.getId()))
			{
				continue;
			}
			
			if (item.getName().toLowerCase().contains(itemName.toLowerCase()))
			{
				if ((limit >= startIndex) && (limit < (startIndex + itemsPerPage)))
				{
					items.add(item);
				}
				limit++;
			}
		}
		
		if (items.isEmpty() && (pageNumber == 1))
		{
			return "<tr><td width=500 align=CENTER>검색결과가 없습니다.</td></tr>";
		}
		
		final StringBuilder builder = new StringBuilder(items.size() * 28);
		int i = 0;
		for (ItemTemplate item : items)
		{
			i++;
			if (i == 1)
			{
				builder.append("<tr>");
			}
			
			String icon = item.getIcon();
			if (icon == null)
			{
				icon = "icon.etc_question_mark_i00";
			}
			
			builder.append("<td width=35>");
			builder.append("<button value=\".\" action=\"bypass _bbs_search_drop " + item.getId() + " 1 $order $level\" width=32 height=32 back=\"" + icon + "\" fore=\"" + icon + "\">");
			builder.append("</td>");
			builder.append("<td width=210 align=left>");
			builder.append("&#").append(item.getId()).append(";");
			builder.append("</td>");
			
			if (i == 3)
			{
				builder.append("</tr>");
				i = 0;
			}
		}
		
		if ((i % 3) != 0)
		{
			builder.append("</tr>");
		}
		
		return builder.toString();
	}
	
	public static String buildItemPageResult(String itemName, int pageNumber)
	{
		int itemsPerPage = 21;
		int limit = 0;
		final Set<Integer> existInDropData = DROP_INDEX_CACHE.keySet();
		
		for (ItemTemplate item : ItemTable.getInstance().getAllItems())
		{
			if (item == null)
			{
				continue;
			}
			
			if (!existInDropData.contains(item.getId()))
			{
				continue;
			}
			
			if (item.getName().toLowerCase().contains(itemName.toLowerCase()))
			{
				limit++;
			}
		}
		
		int totalPages = (int) Math.ceil((double) limit / itemsPerPage);
		final StringBuilder builder = new StringBuilder();
		
		builder.append("<tr><td align=\"left\" width=380>");
		if (pageNumber > 1)
		{
			builder.append("<button value=\"이전 페이지\" action=\"bypass voice ._bbssearch_drop " + itemName + " " + (pageNumber - 1) + "\" width=100 height=25>");
		}
		builder.append("</td>");
		builder.append("<td align=\"right\" width=380>");
		if (pageNumber < totalPages)
		{
			builder.append("<button value=\"다음 페이지\" action=\"bypass voice ._bbssearch_drop " + itemName + " " + (pageNumber + 1) + "\" width=100 height=25>");
		}
		builder.append("</td></tr>");
		
		return builder.toString();
	}
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		final List<String> commands = new ArrayList<>();
		commands.addAll(Arrays.asList(COMMAND));
		commands.addAll(Arrays.asList(CUSTOM_COMMANDS));
		return commands.stream().filter(Objects::nonNull).toArray(String[]::new);
	}
}