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
package ai.others.OlyManager;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.handler.BypassHandler;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.olympiad.CompetitionType;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.olympiad.OlympiadGameManager;
import org.l2jmobius.gameserver.model.olympiad.OlympiadGameTask;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExOlympiadMatchList;

import ai.AbstractNpcAI;

/**
 * Olympiad Manager AI.
 * @author St3eT
 */
public class OlyManager extends AbstractNpcAI implements IBypassHandler
{
	private static final Logger LOGGER = Logger.getLogger(OlyManager.class.getName());
	
	// NPC
	private static final int MANAGER = 31688;
	// Misc
	private static final int EQUIPMENT_MULTISELL = 3168801;
	
	private static final String[] BYPASSES =
	{
		"watchmatch",
		"arenachange"
	};
	
	private OlyManager()
	{
		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		addTalkId(MANAGER);
		BypassHandler.getInstance().registerHandler(this);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "OlyManager-info.html":
			case "OlyManager-infoHistory.html":
			case "OlyManager-infoPoints.html":
			case "OlyManager-rewards.html":
			{
				htmltext = event;
				break;
			}
			case "OlyManager-infoRules.html":
			{
				htmltext = getHtm(player, "OlyManager-infoRules.html");
				htmltext = htmltext.replace("%min_member%", String.valueOf(Config.ALT_OLY_NONCLASSED));
				htmltext = htmltext.replace("%fight_time%", String.valueOf(Config.ALT_OLY_BATTLE / 60000));
				break;
			}
			case "OlyManager-infoPointsCalc.html":
			{
				htmltext = getHtm(player, "OlyManager-infoPointsCalc.html");
				htmltext = htmltext.replace("%rank1_point%", String.valueOf(Config.ALT_OLY_RANK1_POINTS));
				htmltext = htmltext.replace("%rank2_point%", String.valueOf(Config.ALT_OLY_RANK2_POINTS));
				htmltext = htmltext.replace("%rank3_point%", String.valueOf(Config.ALT_OLY_RANK3_POINTS));
				htmltext = htmltext.replace("%rank4_point%", String.valueOf(Config.ALT_OLY_RANK4_POINTS));
				htmltext = htmltext.replace("%rank5_point%", String.valueOf(Config.ALT_OLY_RANK5_POINTS));
				break;
			}
			case "ranking":
			{
				htmltext = getHtm(player, "OlyManager-rank.html");
				break;
			}
			case "index":
			{
				htmltext = onFirstTalk(npc, player);
				break;
			}
			case "joinMatch":
			{
				if (OlympiadManager.getInstance().isRegistered(player))
				{
					htmltext = "OlyManager-registred.html";
				}
				else
				{
					htmltext = getHtm(player, "OlyManager-joinMatch.html");
					htmltext = htmltext.replace("%olympiad_week%", String.valueOf(Olympiad.getInstance().getCurrentCycle()));
					htmltext = htmltext.replace("%olympiad_participant%", String.valueOf(OlympiadManager.getInstance().getCountOpponents()));
				}
				break;
			}
			case "register1v1":
			{
				if (player.isSubClassActive())
				{
					htmltext = "OlyManager-subclass.html";
				}
				else if (!player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && !player.isNoble()) // avoid exploits
				{
					htmltext = "OlyManager-noNoble.html";
				}
				else if (Olympiad.getInstance().getNoblePoints(player) <= 0)
				{
					htmltext = "OlyManager-noPoints.html";
				}
				else if (!player.isInventoryUnder80(false))
				{
					player.sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				}
				else
				{
					OlympiadManager.getInstance().registerNoble(player, CompetitionType.NON_CLASSED);
				}
				break;
			}
			case "unregister":
			{
				OlympiadManager.getInstance().unRegisterNoble(player);
				break;
			}
			case "calculatePoints":
			{
				if (player.getVariables().getInt(Olympiad.UNCLAIMED_OLYMPIAD_POINTS_VAR, 0) > 0)
				{
					htmltext = "OlyManager-calculateEnough.html";
				}
				else
				{
					htmltext = "OlyManager-calculateNoEnough.html";
				}
				break;
			}
			case "calculatePointsDone":
			{
				if (player.isInventoryUnder80(false))
				{
					final int tradePoints = player.getVariables().getInt(Olympiad.UNCLAIMED_OLYMPIAD_POINTS_VAR, 0);
					if (tradePoints > 0)
					{
						player.getVariables().remove(Olympiad.UNCLAIMED_OLYMPIAD_POINTS_VAR);
						giveItems(player, Config.ALT_OLY_COMP_RITEM, tradePoints / 2);
					}
				}
				else
				{
					player.sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				}
				break;
			}
			case "showEquipmentReward":
			{
				MultisellData.getInstance().separateAndSend(EQUIPMENT_MULTISELL, player, npc, false);
				break;
			}
			case "rank_88": // Duelist
			case "rank_89": // Dreadnought
			case "rank_90": // Phoenix Knight
			case "rank_91": // Hell Knight
			case "rank_92": // Sagittarius
			case "rank_93": // Adventurer
			case "rank_94": // Archmage
			case "rank_95": // Soultaker
			case "rank_96": // Arcana Lord
			case "rank_97": // Cardinal
			case "rank_98": // Hierophant
			case "rank_99": // Eva's Templar
			case "rank_100": // Sword Muse
			case "rank_101": // Wind Rider
			case "rank_102": // Moonlight Sentinel
			case "rank_103": // Mystic Muse
			case "rank_104": // Elemental Master
			case "rank_105": // Eva's Saint
			case "rank_106": // Shillien Templar
			case "rank_107": // Spectral Dancer
			case "rank_108": // Ghost Hunter
			case "rank_109": // Ghost Sentinel
			case "rank_110": // Storm Screamer
			case "rank_111": // Spectral Master
			case "rank_112": // Shillien Saint
			case "rank_113": // Titan
			case "rank_114": // Grand Khavatari
			case "rank_115": // Dominator
			case "rank_116": // Doom Cryer
			case "rank_117": // Fortune Seeker
			case "rank_118": // Maestro
			{
				final int classId = Integer.parseInt(event.replace("rank_", ""));
				final List<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
				htmltext = getHtm(player, "OlyManager-rankDetail.html");
				int index = 1;
				for (String name : names)
				{
					htmltext = htmltext.replace("%Rank" + index + "%", String.valueOf(index + "위"));
					htmltext = htmltext.replace("%Name" + index + "%", name);
					index++;
					if (index > 15)
					{
						break;
					}
				}
				for (; index <= 15; index++)
				{
					htmltext = htmltext.replace("%Rank" + index + "%", "");
					htmltext = htmltext.replace("%Name" + index + "%", "");
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		if (!player.isCursedWeaponEquipped())
		{
			if (talking())
			{
				player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
				htmltext = getHtm(player, "OlyManager-noPeriod.html");
				htmltext = htmltext.replace("%olympiad_week%", String.valueOf(Olympiad.getInstance().getCurrentCycle()));
			}
			else if (!Olympiad.getInstance().inCompPeriod())
			{
				player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
				htmltext = player.isSubClassActive() ? "OlyManager-nobleFightSub.html" : ((player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && player.isNoble()) ? "OlyManager-nobleFight.html" : "OlyManager-noNobleFight.html");
			}
			else
			{
				htmltext = player.isSubClassActive() ? "OlyManager-nobleSub.html" : ((player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && player.isNoble()) ? "OlyManager-noble.html" : "OlyManager-noNoble.html");
			}
		}
		else
		{
			htmltext = "OlyManager-noCursed.html";
		}
		return htmltext;
	}
	
	private boolean talking()
	{
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		if ((day == Calendar.MONDAY) && (hour < 12))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public boolean useBypass(String command, Player player, Creature bypassOrigin)
	{
		try
		{
			final Npc olymanager = player.getLastFolkNPC();
			if (command.startsWith(BYPASSES[0])) // list
			{
				if (!Olympiad.getInstance().inCompPeriod())
				{
					player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
					return false;
				}
				
				player.sendPacket(new ExOlympiadMatchList());
			}
			else if ((olymanager == null) || (olymanager.getId() != MANAGER) || (!player.inObserverMode() && !player.isInsideRadius2D(olymanager, 300)))
			{
				return false;
			}
			else if (OlympiadManager.getInstance().isRegisteredInComp(player))
			{
				player.sendPacket(SystemMessageId.YOU_MAY_NOT_OBSERVE_A_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST);
				return false;
			}
			else if (!Olympiad.getInstance().inCompPeriod())
			{
				player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
				return false;
			}
			else if (player.isOnEvent())
			{
				player.sendMessage("이벤트 등록 중에는 경기를 관전할 수 없습니다.");
				return false;
			}
			else
			{
				final int arenaId = Integer.parseInt(command.substring(12).trim());
				final OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(arenaId);
				if (nextArena != null)
				{
					final List<Location> spectatorSpawns = nextArena.getStadium().getZone().getSpectatorSpawns();
					if (spectatorSpawns.isEmpty())
					{
						LOGGER.warning(getClass().getSimpleName() + ": Zone: " + nextArena.getStadium().getZone() + " doesn't have specatator spawns defined!");
						return false;
					}
					final Location loc = spectatorSpawns.get(Rnd.get(spectatorSpawns.size()));
					player.enterOlympiadObserverMode(loc, arenaId);
				}
			}
			return true;
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return BYPASSES;
	}
	
	public static void main(String[] args)
	{
		new OlyManager();
	}
}