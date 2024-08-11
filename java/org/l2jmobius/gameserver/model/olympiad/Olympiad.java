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
package org.l2jmobius.gameserver.model.olympiad;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.ListenersContainer;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author godson
 */
public class Olympiad extends ListenersContainer
{
	protected static final Logger LOGGER = Logger.getLogger(Olympiad.class.getName());
	protected static final Logger LOGGER_OLYMPIAD = Logger.getLogger("olympiad");
	
	private static final Map<Integer, StatSet> NOBLES = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer> NOBLES_RANK = new HashMap<>();
	
	public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
	private static final String OLYMPIAD_LOAD_DATA = "SELECT current_cycle FROM olympiad_data WHERE id = 0";
	private static final String OLYMPIAD_SAVE_DATA = "INSERT INTO olympiad_data (id, current_cycle) VALUES (0,?) ON DUPLICATE KEY UPDATE current_cycle=?";
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn, olympiad_nobles.competitions_done_week FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles (`charId`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`,`competitions_drawn`, `competitions_done_week`) VALUES (?,?,?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ?, competitions_done_week = ? WHERE charId = ?";
	private static final String OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.charId, characters.char_name FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND olympiad_nobles.class_id in (?, ?) AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " AND olympiad_nobles.competitions_won > 0 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC";
	private static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT charId from olympiad_nobles_eom WHERE competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.charId = olympiad_nobles_eom.charId AND olympiad_nobles_eom.class_id = ? AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT = "SELECT characters.char_name from olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_SOULHOUND = "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.charId = olympiad_nobles_eom.charId AND (olympiad_nobles_eom.class_id = ? OR olympiad_nobles_eom.class_id = 133) AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND = "SELECT characters.char_name from olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND (olympiad_nobles.class_id = ? OR olympiad_nobles.class_id = 133) AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
	
	private static final String REMOVE_UNCLAIMED_POINTS = "DELETE FROM character_variables WHERE charId=? AND var=?";
	private static final String INSERT_UNCLAIMED_POINTS = "INSERT INTO character_variables (charId, var, val) VALUES (?, ?, ?)";
	public static final String UNCLAIMED_OLYMPIAD_POINTS_VAR = "UNCLAIMED_OLYMPIAD_POINTS";
	private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN (30392, 30393, 30394, 30395, 30396, 30397, 30398, 30399, 30400, 30401, 30402, 30403, 30404, 30405, 30372, 30373, 6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390) AND owner_id NOT IN (SELECT charId FROM characters WHERE accesslevel > 0)";
	
	private static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
	private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
	private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT charId, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles";
	
	private static final Set<Integer> HERO_IDS = CategoryData.getInstance().getCategoryByType(CategoryType.FOURTH_CLASS_GROUP);
	
	private static final int COMP_START = Config.ALT_OLY_START_TIME; // 6PM
	private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
	private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 6 hours
	
	public static final int DEFAULT_POINTS = Config.ALT_OLY_START_POINTS;
	protected static final int WEEKLY_POINTS = Config.ALT_OLY_WEEKLY_POINTS;
	
	public static final String CHAR_ID = "charId";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	public static final String COMP_DRAWN = "competitions_drawn";
	public static final String COMP_DONE_WEEK = "competitions_done_week";
	
	/**
	 * The current period of the olympiad.<br>
	 * <b>0 -</b> Competition period<br>
	 * <b>1 -</b> Validation Period
	 */
	// protected int _period;
	protected int _currentCycle;
	private long _compEnd;
	private Calendar _compStart;
	protected static boolean _inCompPeriod;
	protected static boolean _compStarted = false;
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _gameManager = null;
	protected ScheduledFuture<?> _gameAnnouncer = null;
	
	// @formatter:off
	private static final int[] HERO_ITEMS =
	{
		30392, 30393, 30394, 30395, 30396,
		30397, 30398, 30399, 30400, 30401,
		30402, 30403, 30404, 30405, 30372,
		30373, 6842, 6611, 6612, 6613, 6614,
		6615, 6616, 6617, 6618, 6619, 6620,
		6621, 9388, 9389, 9390
	};
	// @formatter:on
	
	protected Olympiad()
	{
		load();
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.OLYMPIAD_ID);
		olyinit(false);
	}
	
	private void load()
	{
		NOBLES.clear();
		// if (GlobalVariablesManager.getInstance().getInt("Olympiad_Period", 0) < 1)
		// {
		// GlobalVariablesManager.getInstance().set("Olympiad_Period", 0);
		// }
		
		// _period = GlobalVariablesManager.getInstance().getInt("Olympiad_Period");
		
		boolean loaded = false;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_DATA);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				_currentCycle = rset.getInt("current_cycle");
				loaded = true;
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error loading olympiad data from database: ", e);
		}
		
		if (!loaded)
		{
			final Properties olympiadProperties = new Properties();
			try (InputStream is = new FileInputStream(Config.OLYMPIAD_CONFIG_FILE))
			{
				olympiadProperties.load(is);
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Olympiad System: Error loading olympiad properties: ", e);
				return;
			}
			
			_currentCycle = Integer.parseInt(olympiadProperties.getProperty("CurrentCycle", "1"));
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			ResultSet rset = statement.executeQuery())
		{
			StatSet statData;
			while (rset.next())
			{
				statData = new StatSet();
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set(COMP_DONE_WEEK, rset.getInt(COMP_DONE_WEEK));
				statData.set("to_save", false);
				
				addNobleStats(rset.getInt(CHAR_ID), statData);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error loading noblesse data from database: ", e);
		}
		
		synchronized (this)
		{
			LOGGER.info("올림피아드: 로딩 중....");
		}
		
		LOGGER.info("올림피아드: " + NOBLES.size() + "명의 노블레스를 로드하였습니다.");
	}
	
	public int getOlympiadRank(Player player)
	{
		return NOBLES_RANK.getOrDefault(player.getObjectId(), 0);
	}
	
	public void loadNoblesRank()
	{
		NOBLES_RANK.clear();
		final Map<Integer, Integer> tmpPlace = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS);
			ResultSet rset = statement.executeQuery())
		{
			int place = 1;
			while (rset.next())
			{
				tmpPlace.put(rset.getInt(CHAR_ID), place++);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error loading noblesse data from database for Ranking: ", e);
		}
		
		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.10);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.50);
		if (rank1 == 0)
		{
			rank1 = 1;
			rank2++;
			rank3++;
			rank4++;
		}
		for (Entry<Integer, Integer> chr : tmpPlace.entrySet())
		{
			if (chr.getValue() <= rank1)
			{
				NOBLES_RANK.put(chr.getKey(), 1);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank2)
			{
				NOBLES_RANK.put(chr.getKey(), 2);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank3)
			{
				NOBLES_RANK.put(chr.getKey(), 3);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank4)
			{
				NOBLES_RANK.put(chr.getKey(), 4);
			}
			else
			{
				NOBLES_RANK.put(chr.getKey(), 5);
			}
		}
		
		// Store remaining hero reward points to player variables.
		for (int noblesId : NOBLES.keySet())
		{
			final int points = getOlympiadTradePoint(noblesId);
			if (points > 0)
			{
				final Player player = World.getInstance().getPlayer(noblesId);
				if (player != null)
				{
					player.getVariables().set(UNCLAIMED_OLYMPIAD_POINTS_VAR, points);
				}
				else
				{
					// Remove previous record.
					try (Connection con = DatabaseFactory.getConnection();
						PreparedStatement statement = con.prepareStatement(REMOVE_UNCLAIMED_POINTS))
					{
						statement.setInt(1, noblesId);
						statement.setString(2, UNCLAIMED_OLYMPIAD_POINTS_VAR);
						statement.execute();
					}
					catch (SQLException e)
					{
						LOGGER.warning("Olympiad System: Couldn't remove unclaimed olympiad points from DB!");
					}
					// Add new value.
					try (Connection con = DatabaseFactory.getConnection();
						PreparedStatement statement = con.prepareStatement(INSERT_UNCLAIMED_POINTS))
					{
						statement.setInt(1, noblesId);
						statement.setString(2, UNCLAIMED_OLYMPIAD_POINTS_VAR);
						statement.setString(3, String.valueOf(points));
						statement.execute();
					}
					catch (SQLException e)
					{
						LOGGER.warning("Olympiad System: Couldn't store unclaimed olympiad points to DB!");
					}
				}
			}
		}
	}
	
	public void olyinit(boolean newstart)
	{
		_compStart = Calendar.getInstance();
		final int currentDay = _compStart.get(Calendar.DAY_OF_WEEK);
		boolean dayFound = false;
		int dayCounter = 0;
		for (int i = currentDay; i < 8; i++)
		{
			if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
			{
				dayFound = true;
				break;
			}
			dayCounter++;
		}
		if (!dayFound)
		{
			for (int i = 1; i < 8; i++)
			{
				if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
				{
					break;
				}
				dayCounter++;
			}
		}
		if (dayCounter > 0)
		{
			_compStart.add(Calendar.DAY_OF_MONTH, dayCounter);
		}
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compStart.set(Calendar.SECOND, 0);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		
		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		
		if (!newstart)
		{
			updateCompStatus();
		}
	}
	
	protected void ValidationEndTask()
	{
		// _period = 0;
		_currentCycle++;
		deleteNobles();
		olyinit(true);
	}
	
	protected static int getNobleCount()
	{
		return NOBLES.size();
	}
	
	public static StatSet getNobleStats(int playerId)
	{
		return NOBLES.get(playerId);
	}
	
	public static void removeNobleStats(Player player)
	{
		NOBLES.remove(player.getObjectId());
		NOBLES_RANK.remove(player.getObjectId());
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement deleteHeros = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			PreparedStatement deleteHerosDiary = con.prepareStatement("DELETE FROM heroes_diary WHERE charId=?");
			PreparedStatement deleteOlyFight = con.prepareStatement("DELETE FROM olympiad_fights WHERE charOneId=? OR charTwoId=?");
			PreparedStatement deleteOlyNoble = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			PreparedStatement deleteOlyNobleOem = con.prepareStatement("DELETE FROM olympiad_nobles_eom WHERE charId=?"))
		{
			deleteHeros.setInt(1, player.getObjectId());
			deleteHerosDiary.setInt(1, player.getObjectId());
			deleteOlyFight.setInt(1, player.getObjectId());
			deleteOlyFight.setInt(2, player.getObjectId());
			deleteOlyNoble.setInt(1, player.getObjectId());
			deleteOlyNobleOem.setInt(1, player.getObjectId());
			deleteHeros.execute();
			deleteHerosDiary.execute();
			deleteOlyFight.execute();
			deleteOlyNoble.execute();
			deleteOlyNobleOem.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Error removing noblesse data from database: ", e);
		}
	}
	
	public void updateCompStatus()
	{
		// _compStarted = false;
		
		synchronized (this)
		{
			final long milliToStart = getMillisToCompBegin();
			
			final double numSecs = (milliToStart / 1000) % 60;
			double countDown = ((milliToStart / 1000.) - numSecs) / 60;
			final int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			final int numHours = (int) Math.floor(countDown % 24);
			final int numDays = (int) Math.floor((countDown - numHours) / 24);
			
			LOGGER.info("올림피아드: " + numDays + "일 " + numHours + "시간 " + numMins + "분 후 경기가 시작됩니다.");
			String date = BorinetUtil.dataDateFormatKor.format(new Date(_compStart.getTimeInMillis()));
			LOGGER.info("올림피아드: 경기 시작시간: " + date);
		}
		
		_scheduledCompStart = ThreadPool.schedule(() ->
		{
			// if (isOlympiadEnd())
			// {
			// return;
			// }
			
			_inCompPeriod = true;
			
			Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.SHARPEN_YOUR_SWORDS_TIGHTEN_THE_STITCHING_IN_YOUR_ARMOR_AND_MAKE_HASTE_TO_A_OLYMPIAD_MANAGER_BATTLES_IN_THE_OLYMPIAD_GAMES_ARE_NOW_TAKING_PLACE));
			LOGGER.info("올림피아드: 올림피아드 게임이 시작되었습니다.");
			LOGGER_OLYMPIAD.info("Result,Player1,Player2,Player1 HP,Player2 HP,Player1 Damage,Player2 Damage,Points,Classed");
			
			_gameManager = ThreadPool.scheduleAtFixedRate(OlympiadGameManager.getInstance(), 30000, 30000);
			if (Config.ALT_OLY_ANNOUNCE_GAMES)
			{
				_gameAnnouncer = ThreadPool.scheduleAtFixedRate(new OlympiadAnnouncer(), 30000, 500);
			}
			
			final long regEnd = getMillisToCompEnd() - 600000;
			if (regEnd > 0)
			{
				ThreadPool.schedule(() -> Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.THE_OLYMPIAD_REGISTRATION_PERIOD_HAS_ENDED)), regEnd);
			}
			
			_scheduledCompEnd = ThreadPool.schedule(() ->
			{
				// if (isOlympiadEnd())
				// {
				// return;
				// }
				_inCompPeriod = false;
				Broadcast.toAllOnlinePlayers(new SystemMessage(SystemMessageId.MUCH_CARNAGE_HAS_BEEN_LEFT_FOR_THE_CLEANUP_CREW_OF_THE_OLYMPIAD_STADIUM_BATTLES_IN_THE_OLYMPIAD_GAMES_ARE_NOW_OVER));
				LOGGER.info("올림피아드: 올림피아드 경기가 종료되었습니다.");
				
				while (OlympiadGameManager.getInstance().isBattleStarted()) // cleared in game manager
				{
					try
					{
						// wait 1 minutes for end of pending games
						Thread.sleep(60000);
					}
					catch (Exception e)
					{
						// Ignore.
					}
				}
				
				if (_gameManager != null)
				{
					_gameManager.cancel(false);
					_gameManager = null;
				}
				
				if (_gameAnnouncer != null)
				{
					_gameAnnouncer.cancel(false);
					_gameAnnouncer = null;
				}
				
				saveOlympiadStatus();
				
				olyinit(false);
				
				Calendar calendar = Calendar.getInstance();
				int day = calendar.get(Calendar.DAY_OF_WEEK);
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int min = calendar.get(Calendar.MINUTE);
				if ((day == Calendar.MONDAY) && (hour == 0) && (min <= 1))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.ROUND_S1_OF_THE_OLYMPIAD_GAMES_HAS_NOW_ENDED);
					sm.addInt(_currentCycle);
					
					Broadcast.toAllOnlinePlayers(sm);
					
					if (_scheduledWeeklyTask != null)
					{
						_scheduledWeeklyTask.cancel(true);
					}
					
					Hero.getInstance().updateHeroes(true);
					
					for (Integer objectId : Hero.HEROES.keySet())
					{
						final Player players = World.getInstance().getPlayer(objectId);
						if (players == null)
						{
							continue;
						}
						
						players.setHero(false);
						
						for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
						{
							final Item equippedItem = players.getInventory().getPaperdollItem(i);
							if ((equippedItem != null) && equippedItem.isHeroItem())
							{
								players.getInventory().unEquipItemInSlot(i);
							}
						}
						
						final InventoryUpdate iu = new InventoryUpdate();
						for (Item item : players.getInventory().getAvailableItems(false, false, false))
						{
							if ((item != null) && item.isHeroItem())
							{
								players.destroyItem("Hero", item, null, true);
								iu.addRemovedItem(item);
							}
						}
						
						if (!iu.getItems().isEmpty())
						{
							players.sendInventoryUpdate(iu);
						}
						players.broadcastUserInfo();
					}
					
					deleteHerotIems();
					Hero.HEROES.clear();
				}
			}, getMillisToCompEnd());
		}, getMillisToCompBegin());
	}
	
	private void deleteHerotIems()
	{
		for (int itemId : HERO_ITEMS)
		{
			for (Player player : World.getInstance().getPlayers())
			{
				if (player != null)
				{
					final Item item = player.getInventory().getItemByItemId(itemId);
					final Item items = player.getWarehouse().getItemByItemId(itemId);
					if (item != null)
					{
						player.destroyItemByItemId("영웅아이템삭제", itemId, item.getCount(), player, true);
					}
					if (items != null)
					{
						player.destroyItemByItemIdInWareHouse("영웅아이템삭제", itemId, items.getCount(), player, true);
					}
				}
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement())
		{
			s.executeUpdate(DELETE_ITEMS);
		}
		catch (SQLException e)
		{
			LOGGER.warning("Heroes: " + e.getMessage());
		}
	}
	
	// public boolean isOlympiadEnd()
	// {
	// return _period != 0;
	// }
	
	public void setNewOlympiad()
	{
		saveNobleData();
		
		// _period = 1;
		final List<StatSet> heroesToBe = sortHerosToBe();
		Hero.getInstance().resetData();
		Hero.getInstance().computeNewHeroes(heroesToBe);
		
		saveOlympiadStatus();
		updateMonthlyData();
		loadNoblesRank();
		ValidationEndTask();
		resetWeeklyMatches();
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.ROUND_S1_OF_THE_OLYMPIAD_GAMES_HAS_STARTED);
		sm.addInt(_currentCycle);
		Broadcast.toAllOnlinePlayers(sm);
		if (Hero.HEROES.size() >= 1)
		{
			Broadcast.toAllOnlinePlayers("이 시대의 영웅이 탄생하였습니다!", false);
		}
	}
	
	public boolean inCompPeriod()
	{
		return _inCompPeriod;
	}
	
	private long getMillisToCompBegin()
	{
		if ((_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_compEnd > Calendar.getInstance().getTimeInMillis()))
		{
			return 10;
		}
		
		if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
		{
			return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		}
		
		return setNewCompBegin();
	}
	
	private long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		
		int currentDay = _compStart.get(Calendar.DAY_OF_WEEK);
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compStart.set(Calendar.SECOND, 0);
		
		// Today's competitions ended, start checking from next day.
		if (currentDay == _compStart.get(Calendar.DAY_OF_WEEK))
		{
			if (currentDay == Calendar.SATURDAY)
			{
				currentDay = Calendar.SUNDAY;
			}
			else
			{
				currentDay++;
			}
		}
		
		boolean dayFound = false;
		int dayCounter = 0;
		for (int i = currentDay; i < 8; i++)
		{
			if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
			{
				dayFound = true;
				break;
			}
			dayCounter++;
		}
		if (!dayFound)
		{
			for (int i = 1; i < 8; i++)
			{
				if (Config.ALT_OLY_COMPETITION_DAYS.contains(i))
				{
					break;
				}
				dayCounter++;
			}
		}
		if (dayCounter > 0)
		{
			_compStart.add(Calendar.DAY_OF_MONTH, dayCounter);
		}
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		
		LOGGER.info("Olympiad System: New Schedule @ " + _compStart.getTime());
		
		return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}
	
	protected long getMillisToCompEnd()
	{
		// if (_compEnd > Calendar.getInstance().getTimeInMillis())
		return _compEnd - Calendar.getInstance().getTimeInMillis();
		// return 10;
	}
	
	/**
	 * Resets number of matches, classed matches, non classed matches, team matches done by noble characters in the week.
	 */
	protected synchronized void resetWeeklyMatches()
	{
		// if (_period == 1)
		// {
		// return;
		// }
		
		for (StatSet nobleInfo : NOBLES.values())
		{
			nobleInfo.set(COMP_DONE_WEEK, 0);
		}
	}
	
	public int getCurrentCycle()
	{
		return _currentCycle;
	}
	
	public boolean playerInStadia(Player player)
	{
		return ZoneManager.getInstance().getOlympiadStadium(player) != null;
	}
	
	/**
	 * Save noblesse data to database
	 */
	protected synchronized void saveNobleData()
	{
		if (NOBLES.isEmpty())
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (Entry<Integer, StatSet> entry : NOBLES.entrySet())
			{
				final StatSet nobleInfo = entry.getValue();
				
				if (nobleInfo == null)
				{
					continue;
				}
				
				final int charId = entry.getKey();
				final int classId = nobleInfo.getInt(CLASS_ID);
				final int points = nobleInfo.getInt(POINTS);
				final int compDone = nobleInfo.getInt(COMP_DONE);
				final int compWon = nobleInfo.getInt(COMP_WON);
				final int compLost = nobleInfo.getInt(COMP_LOST);
				final int compDrawn = nobleInfo.getInt(COMP_DRAWN);
				final int compDoneWeek = nobleInfo.getInt(COMP_DONE_WEEK);
				final boolean toSave = nobleInfo.getBoolean("to_save");
				
				try (PreparedStatement statement = con.prepareStatement(toSave ? OLYMPIAD_SAVE_NOBLES : OLYMPIAD_UPDATE_NOBLES))
				{
					if (toSave)
					{
						statement.setInt(1, charId);
						statement.setInt(2, classId);
						statement.setInt(3, points);
						statement.setInt(4, compDone);
						statement.setInt(5, compWon);
						statement.setInt(6, compLost);
						statement.setInt(7, compDrawn);
						statement.setInt(8, compDoneWeek);
						
						nobleInfo.set("to_save", false);
					}
					else
					{
						statement.setInt(1, points);
						statement.setInt(2, compDone);
						statement.setInt(3, compWon);
						statement.setInt(4, compLost);
						statement.setInt(5, compDrawn);
						statement.setInt(6, compDoneWeek);
						statement.setInt(7, charId);
					}
					statement.execute();
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Olympiad System: Failed to save noblesse data to database: ", e);
		}
	}
	
	/**
	 * Save olympiad.properties file with current olympiad status and update noblesse table in database
	 */
	public void saveOlympiadStatus()
	{
		saveNobleData();
		// GlobalVariablesManager.getInstance().set("Olympiad_Period", _period);
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_SAVE_DATA))
		{
			statement.setInt(1, _currentCycle);
			statement.setInt(2, _currentCycle);
			statement.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Olympiad System: Failed to save olympiad data to database: ", e);
		}
	}
	
	protected void updateMonthlyData()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement(OLYMPIAD_MONTH_CLEAR);
			PreparedStatement ps2 = con.prepareStatement(OLYMPIAD_MONTH_CREATE))
		{
			ps1.execute();
			ps2.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Olympiad System: Failed to update monthly noblese data: ", e);
		}
	}
	
	protected List<StatSet> sortHerosToBe()
	{
		// if (_period != 1)
		// {
		// return Collections.emptyList();
		// }
		
		LOGGER_OLYMPIAD.info("Noble,charid,classid,compDone,points");
		StatSet nobleInfo;
		for (Entry<Integer, StatSet> entry : NOBLES.entrySet())
		{
			nobleInfo = entry.getValue();
			if (nobleInfo == null)
			{
				continue;
			}
			
			final int charId = entry.getKey();
			final int classId = nobleInfo.getInt(CLASS_ID);
			final String charName = nobleInfo.getString(CHAR_NAME);
			final int points = nobleInfo.getInt(POINTS);
			final int compDone = nobleInfo.getInt(COMP_DONE);
			
			LOGGER_OLYMPIAD.info(charName + "," + charId + "," + classId + "," + compDone + "," + points);
		}
		
		final List<StatSet> heroesToBe = new LinkedList<>();
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_GET_HEROS))
		{
			StatSet hero;
			for (int element : HERO_IDS)
			{
				// Classic can have 2nd and 3rd class competitors, but only 1 hero
				final ClassId parent = ClassListData.getInstance().getClass(element).getParentClassId();
				statement.setInt(1, element);
				statement.setInt(2, parent.getId());
				
				try (ResultSet rset = statement.executeQuery())
				{
					if (rset.next())
					{
						hero = new StatSet();
						hero.set(CLASS_ID, element); // save the 3rd class title
						hero.set(CHAR_ID, rset.getInt(CHAR_ID));
						hero.set(CHAR_NAME, rset.getString(CHAR_NAME));
						
						LOGGER_OLYMPIAD.info("Hero " + hero.getString(CHAR_NAME) + "," + hero.getInt(CHAR_ID) + "," + hero.getInt(CLASS_ID));
						heroesToBe.add(hero);
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Couldnt load heros from DB");
		}
		
		return heroesToBe;
	}
	
	public List<String> getClassLeaderBoard(int classId)
	{
		final List<String> names = new ArrayList<>();
		final String query = Config.ALT_OLY_SHOW_MONTHLY_WINNERS ? ((classId == 132) ? GET_EACH_CLASS_LEADER_SOULHOUND : GET_EACH_CLASS_LEADER) : ((classId == 132) ? GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND : GET_EACH_CLASS_LEADER_CURRENT);
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, classId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					names.add(rset.getString(CHAR_NAME));
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Couldn't load olympiad leaders from DB!");
		}
		return names;
	}
	
	private int getOlympiadTradePoint(int objectId)
	{
		if (/* (_period != 1) || */NOBLES_RANK.isEmpty())
		{
			return 0;
		}
		
		if (!NOBLES_RANK.containsKey(objectId))
		{
			return 0;
		}
		
		final StatSet noble = NOBLES.get(objectId);
		if ((noble == null) || (noble.getInt(POINTS) == 0))
		{
			return 0;
		}
		
		// Hero point bonus
		int points = Hero.getInstance().isHero(objectId) || Hero.getInstance().isUnclaimedHero(objectId) ? Config.ALT_OLY_HERO_POINTS : 0;
		// Rank point bonus
		switch (NOBLES_RANK.get(objectId))
		{
			case 1:
			{
				points += Config.ALT_OLY_RANK1_POINTS;
				break;
			}
			case 2:
			{
				points += Config.ALT_OLY_RANK2_POINTS;
				break;
			}
			case 3:
			{
				points += Config.ALT_OLY_RANK3_POINTS;
				break;
			}
			case 4:
			{
				points += Config.ALT_OLY_RANK4_POINTS;
				break;
			}
			default:
			{
				points += Config.ALT_OLY_RANK5_POINTS;
			}
		}
		
		// Win/no win matches point bonus
		points += getCompetitionWon(objectId) > 0 ? 10 : 0;
		
		// This is a one time calculation.
		noble.set(POINTS, 0);
		
		return points;
	}
	
	public int getNoblePoints(Player player)
	{
		if (!NOBLES.containsKey(player.getObjectId()))
		{
			final StatSet statDat = new StatSet();
			statDat.set(CLASS_ID, player.getBaseClass());
			statDat.set(CHAR_NAME, player.getName());
			statDat.set(POINTS, DEFAULT_POINTS);
			statDat.set(COMP_DONE, 0);
			statDat.set(COMP_WON, 0);
			statDat.set(COMP_LOST, 0);
			statDat.set(COMP_DRAWN, 0);
			statDat.set(COMP_DONE_WEEK, 0);
			statDat.set("to_save", true);
			addNobleStats(player.getObjectId(), statDat);
		}
		return NOBLES.get(player.getObjectId()).getInt(POINTS);
	}
	
	public int getLastNobleOlympiadPoints(int objId)
	{
		int result = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT olympiad_points FROM olympiad_nobles_eom WHERE charId = ?"))
		{
			ps.setInt(1, objId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getInt(1);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Olympiad System: Could not load last olympiad points:", e);
		}
		return result;
	}
	
	public int getCompetitionDone(int objId)
	{
		if (!NOBLES.containsKey(objId))
		{
			return 0;
		}
		return NOBLES.get(objId).getInt(COMP_DONE);
	}
	
	public int getCompetitionWon(int objId)
	{
		if (!NOBLES.containsKey(objId))
		{
			return 0;
		}
		return NOBLES.get(objId).getInt(COMP_WON);
	}
	
	public int getCompetitionLost(int objId)
	{
		if (!NOBLES.containsKey(objId))
		{
			return 0;
		}
		return NOBLES.get(objId).getInt(COMP_LOST);
	}
	
	/**
	 * Gets how many matches a noble character did in the week
	 * @param objId id of a noble character
	 * @return number of weekly competitions done
	 */
	public int getCompetitionDoneWeek(int objId)
	{
		if (!NOBLES.containsKey(objId))
		{
			return 0;
		}
		return NOBLES.get(objId).getInt(COMP_DONE_WEEK);
	}
	
	/**
	 * Number of remaining matches a noble character can join in the week
	 * @param objId id of a noble character
	 * @return difference between maximum allowed weekly matches and currently done weekly matches.
	 */
	public int getRemainingWeeklyMatches(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES - getCompetitionDoneWeek(objId), 0);
	}
	
	protected void deleteNobles()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_DELETE_ALL))
		{
			statement.execute();
		}
		catch (SQLException e)
		{
			LOGGER.warning("Olympiad System: Couldn't delete nobles from DB!");
		}
		NOBLES.clear();
	}
	
	/**
	 * @param charId the noble object Id.
	 * @param data the stats set data to add.
	 * @return the old stats set if the noble is already present, null otherwise.
	 */
	public static StatSet addNobleStats(int charId, StatSet data)
	{
		return NOBLES.put(charId, data);
	}
	
	public static Olympiad getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Olympiad INSTANCE = new Olympiad();
	}
}
