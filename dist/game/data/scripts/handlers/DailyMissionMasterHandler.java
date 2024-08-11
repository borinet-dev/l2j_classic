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
package handlers;

import org.l2jmobius.gameserver.handler.DailyMissionHandler;

import handlers.dailymissionhandlers.BoatManiaDailyMissionHandler;
import handlers.dailymissionhandlers.BossDailyMissionHandler;
import handlers.dailymissionhandlers.ClanDailyMissionHandler;
import handlers.dailymissionhandlers.EnchantItemDailyMissionHandler;
import handlers.dailymissionhandlers.FishingDailyMissionHandler;
import handlers.dailymissionhandlers.GambleDailyMissionHandler;
import handlers.dailymissionhandlers.ItemDestroyDailyMissionHandler;
import handlers.dailymissionhandlers.KamalokaManiaDailyMissionHandler;
import handlers.dailymissionhandlers.LevelDailyMissionHandler;
import handlers.dailymissionhandlers.LoginMonthDailyMissionHandler;
import handlers.dailymissionhandlers.LoginWeekendDailyMissionHandler;
import handlers.dailymissionhandlers.MineManiaDailyMissionHandler;
import handlers.dailymissionhandlers.MissionManiaDailyMissionHandler;
import handlers.dailymissionhandlers.MonsterDailyMissionHandler;
import handlers.dailymissionhandlers.MonsterLevelOverDailyMissionHandler;
import handlers.dailymissionhandlers.MonsterPartyDailyMissionHandler;
import handlers.dailymissionhandlers.OlympiadDailyMissionHandler;
import handlers.dailymissionhandlers.QuestDailyMissionHandler;
import handlers.dailymissionhandlers.RiftManiaDailyMissionHandler;
import handlers.dailymissionhandlers.SiegeDailyMissionHandler;
import handlers.dailymissionhandlers.SpecialDailyMissionHandler;
import handlers.dailymissionhandlers.SpiritDailyMissionHandler;
import handlers.dailymissionhandlers.UseItemDailyMissionHandler;

/**
 * @author UnAfraid
 */
public class DailyMissionMasterHandler
{
	// private static final Logger LOGGER = Logger.getLogger(DailyMissionMasterHandler.class.getName());
	
	public static void main(String[] args)
	{
		DailyMissionHandler.getInstance().registerHandler("level", LevelDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("loginweekend", LoginWeekendDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("loginmonth", LoginMonthDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("quest", QuestDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("olympiad", OlympiadDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("siege", SiegeDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("boss", BossDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("monster", MonsterDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("monsterlvlover", MonsterLevelOverDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("monsterparty", MonsterPartyDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("fishing", FishingDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("enchantitem", EnchantItemDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("gamble", GambleDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("spirit", SpiritDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("clan", ClanDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("special", SpecialDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("useitem", UseItemDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("itemdestroy", ItemDestroyDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("missionMania", MissionManiaDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("riftMania", RiftManiaDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("MineMania", MineManiaDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("KamalokaMania", KamalokaManiaDailyMissionHandler::new);
		DailyMissionHandler.getInstance().registerHandler("boatMania", BoatManiaDailyMissionHandler::new);
		//
		// LOGGER.info(DailyMissionMasterHandler.class.getSimpleName() + ": Loaded " + DailyMissionHandler.getInstance().size() + " handlers.");
	}
}
