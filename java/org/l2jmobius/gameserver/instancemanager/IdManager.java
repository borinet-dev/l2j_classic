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
package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.PrimeFinder;

/**
 * @author Mobius (reworked from L2J IdFactory)
 */
public class IdManager
{
	private static final Logger LOGGER = Logger.getLogger(IdManager.class.getName());
	
	//@formatter:off
	private static final String[][] ID_EXTRACTS =
	{
		{"characters","charId"},
		{"items","object_id"},
		{"clan_data","clan_id"},
		{"itemsonground","object_id"},
		{"messages","messageId"}
	};
	//@formatter:on
	
	private static final String[] TIMESTAMPS_CLEAN =
	{
		"DELETE FROM character_instance_time WHERE time <= ?",
		"DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?"
	};
	
	private static final int FIRST_OID = 0x10000000;
	private static final int LAST_OID = 0x7FFFFFFF;
	private static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;
	
	private static BitSet _freeIds;
	private static AtomicInteger _freeIdCount;
	private static AtomicInteger _nextFreeId;
	private static boolean _initialized;
	
	public IdManager()
	{
		// Update characters online status.
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("UPDATE characters SET online = 0");
		}
		catch (Exception e)
		{
			LOGGER.warning("데이터베이스 정리: Could not update characters online status: " + e);
		}
		
		// Cleanup database.
		if (Config.DATABASE_CLEAN_UP)
		{
			try (Connection con = DatabaseFactory.getConnection();
				Statement statement = con.createStatement())
			{
				final long cleanupStart = System.currentTimeMillis();
				int cleanCount = 0;
				
				cleanCount += statement.executeUpdate("DELETE FROM account_premium WHERE account_premium.account_name NOT IN (SELECT account_name FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM accounts WHERE accounts.login NOT IN (SELECT account_name FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_variables WHERE var = '따라가기' OR var = '따라가기시작';");
				
				// Characters
				cleanCount += statement.executeUpdate("DELETE FROM account_gsdata WHERE account_gsdata.account_name NOT IN (SELECT account_name FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_contacts WHERE character_contacts.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_contacts WHERE character_contacts.contactId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_friends WHERE character_friends.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_friends WHERE character_friends.friendId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_quests WHERE character_quests.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_recipeshoplist WHERE character_recipeshoplist.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_skills WHERE character_skills.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_instance_time WHERE character_instance_time.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_minigame_score WHERE character_minigame_score.object_id NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_spec WHERE character_spec.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_stats WHERE character_stats.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_daily_rewards WHERE character_daily_rewards.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_summon_skills_save WHERE character_summon_skills_save.ownerId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_summons WHERE character_summons.ownerId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_hwid WHERE character_hwid.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_item_reuse_save WHERE character_item_reuse_save.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_spirits WHERE character_spirits.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM custom_mail WHERE custom_mail.receiver NOT IN (SELECT charId FROM characters);");
				
				// Items
				cleanCount += statement.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT charId FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data) AND items.owner_id NOT IN (SELECT item_obj_id FROM pets) AND items.owner_id != -1;");
				cleanCount += statement.executeUpdate("DELETE FROM items WHERE items.owner_id = -1 AND loc LIKE 'MAIL' AND loc_data NOT IN (SELECT messageId FROM messages WHERE senderId = -1);");
				cleanCount += statement.executeUpdate("DELETE FROM item_auction_bid WHERE item_auction_bid.playerObjId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM item_variations WHERE item_variations.itemId NOT IN (SELECT object_id FROM items);");
				cleanCount += statement.executeUpdate("DELETE FROM item_elementals WHERE item_elementals.itemId NOT IN (SELECT object_id FROM items);");
				cleanCount += statement.executeUpdate("DELETE FROM item_special_abilities WHERE item_special_abilities.objectId NOT IN (SELECT object_id FROM items);");
				cleanCount += statement.executeUpdate("DELETE FROM item_variables WHERE item_variables.id NOT IN (SELECT object_id FROM items);");
				
				// Misc
				cleanCount += statement.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM heroes WHERE heroes.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM olympiad_nobles_eom WHERE olympiad_nobles_eom.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
				cleanCount += statement.executeUpdate("DELETE FROM merchant_lease WHERE merchant_lease.player_id NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_reco_bonus WHERE character_reco_bonus.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_data WHERE clan_data.clan_id NOT IN (SELECT clanid FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charOneId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charTwoId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM heroes_diary WHERE heroes_diary.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_offline_trade WHERE character_offline_trade.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_offline_trade_items WHERE character_offline_trade_items.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_tpbookmark WHERE character_tpbookmark.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_variables WHERE character_variables.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM messages WHERE messages.receiverId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM enter_event WHERE enter_event.account NOT IN (SELECT account_name FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM commission_items WHERE commission_items.item_object_id NOT IN (SELECT object_id FROM items);");
				cleanCount += statement.executeUpdate("DELETE FROM event_hwid WHERE event_hwid.HWID NOT IN (SELECT hwid FROM character_hwid);");
				// cleanCount += statement.executeUpdate("DELETE FROM pledge_applicant WHERE pledge_applicant.charId NOT IN (SELECT charId FROM characters);");
				cleanCount += statement.executeUpdate("DELETE FROM character_pet_skills_save WHERE character_pet_skills_save.petObjItemId NOT IN (SELECT object_id FROM items);");
				
				// Clan
				cleanCount += statement.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_notices WHERE clan_notices.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += statement.executeUpdate("DELETE FROM clan_variables WHERE clan_variables.clanId NOT IN (SELECT clan_id FROM clan_data);");
				// cleanCount += statement.executeUpdate("UPDATE clanhall SET ownerId=0, paidUntil=0 WHERE clanhall.ownerId NOT IN (SELECT clan_id FROM clan_data);");
				// cleanCount += statement.executeUpdate("DELETE FROM crests WHERE crests.crest_id NOT IN (SELECT crest_id FROM clan_data);");
				// cleanCount += statement.executeUpdate("DELETE FROM pledge_applicant WHERE pledge_applicant.clanId NOT IN (SELECT clan_id FROM clan_data);");
				// cleanCount += statement.executeUpdate("DELETE FROM pledge_recruit WHERE pledge_recruit.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				// cleanCount += statement.executeUpdate("DELETE FROM pledge_waiting_list WHERE pledge_waiting_list.char_id NOT IN (SELECT charId FROM characters);");
				
				// Forums
				cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
				cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT charId FROM characters) AND forums.forum_parent=3;");
				cleanCount += statement.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
				cleanCount += statement.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");
				cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_name NOT IN (SELECT clan_name FROM clan_data) AND forums.forum_parent=2;");
				
				// Update needed items after cleaning has taken place.
				statement.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
				statement.executeUpdate("UPDATE clan_data SET new_leader_id = 0 WHERE new_leader_id <> 0 AND new_leader_id NOT IN (SELECT charId FROM characters);");
				statement.executeUpdate("UPDATE clan_subpledges SET leader_id=0 WHERE clan_subpledges.leader_id NOT IN (SELECT charId FROM characters) AND leader_id > 0;");
				statement.executeUpdate("UPDATE castle SET side='NEUTRAL' WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data);");
				statement.executeUpdate("UPDATE characters SET clanid=0, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0, clan_join_expiry_time=0, clan_create_expiry_time=0 WHERE characters.clanid > 0 AND characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
				statement.executeUpdate("UPDATE fort SET owner=0 WHERE owner NOT IN (SELECT clan_id FROM clan_data);");
				
				// 운영일수 계산
				statement.executeUpdate("UPDATE web_connect SET date=" + BorinetUtil.serviceDays(true));
				statement.executeUpdate("UPDATE web_connect SET newdate=" + BorinetUtil.serviceDays(false));
				
				if (cleanCount > 0)
				{
					LOGGER.info("데이터베이스 정리: " + cleanCount + "개의 데이터 베이스를 정리하였습니다. 소요시간: " + ((System.currentTimeMillis() - cleanupStart) / 1000) + "초");
				}
				else
				{
					LOGGER.info("데이터베이스 정리: 정리할 데이터 베이스가 없습니다!");
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("데이터베이스 정리: 데이터 베이스를 정리할 수 없습니다: " + e);
			}
		}
		
		// Cleanup timestamps.
		try (Connection con = DatabaseFactory.getConnection())
		{
			int cleanCount = 0;
			for (String line : TIMESTAMPS_CLEAN)
			{
				try (PreparedStatement statement = con.prepareStatement(line))
				{
					statement.setLong(1, System.currentTimeMillis());
					cleanCount += statement.executeUpdate();
				}
			}
			if (cleanCount > 0)
			{
				LOGGER.info("데이터베이스 정리: " + cleanCount + "개의 만료된 타임스템프를 정리하였습니다.");
			}
			else
			{
				LOGGER.info("데이터베이스 정리: 만료된 타임스템프가 없습니다.");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("데이터베이스 정리: 데이터베이스의 타임스탬프를 정리할 수 없습니다. " + e);
		}
		
		// Initialize.
		try
		{
			_freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			_freeIds.clear();
			_freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
			
			// Collect already used ids.
			final List<Integer> usedIds = new ArrayList<>();
			try (Connection con = DatabaseFactory.getConnection();
				Statement statement = con.createStatement())
			{
				String extractUsedObjectIdsQuery = "";
				for (String[] tblClmn : ID_EXTRACTS)
				{
					extractUsedObjectIdsQuery += "SELECT " + tblClmn[1] + " FROM " + tblClmn[0] + " UNION ";
				}
				extractUsedObjectIdsQuery = extractUsedObjectIdsQuery.substring(0, extractUsedObjectIdsQuery.length() - 7); // Remove the last " UNION "
				try (ResultSet result = statement.executeQuery(extractUsedObjectIdsQuery))
				{
					while (result.next())
					{
						usedIds.add(result.getInt(1));
					}
				}
			}
			Collections.sort(usedIds);
			
			// Register used ids.
			for (int usedObjectId : usedIds)
			{
				final int objectId = usedObjectId - FIRST_OID;
				if (objectId < 0)
				{
					LOGGER.warning("데이터베이스 정리: Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					continue;
				}
				_freeIds.set(usedObjectId - FIRST_OID);
				_freeIdCount.decrementAndGet();
			}
			
			_nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
			_initialized = true;
		}
		catch (Exception e)
		{
			_initialized = false;
			LOGGER.severe("데이터베이스 정리: Could not be initialized properly: " + e.getMessage());
		}
		
		// Schedule increase capacity task.
		ThreadPool.scheduleAtFixedRate(() ->
		{
			synchronized (_nextFreeId)
			{
				if (PrimeFinder.nextPrime((usedIdCount() * 11) / 10) > _freeIds.size())
				{
					increaseBitSetCapacity();
				}
			}
		}, 30000, 30000);
		
		LOGGER.info("데이터베이스 정리: " + _freeIds.size() + "개의 데이터를 사용합니다.");
	}
	
	public void releaseId(int objectId)
	{
		synchronized (_nextFreeId)
		{
			if ((objectId - FIRST_OID) > -1)
			{
				_freeIds.clear(objectId - FIRST_OID);
				_freeIdCount.incrementAndGet();
			}
			else
			{
				LOGGER.warning("데이터베이스 정리: Release objectID " + objectId + " failed (< " + FIRST_OID + ")");
			}
		}
	}
	
	public int getNextId()
	{
		synchronized (_nextFreeId)
		{
			final int newId = _nextFreeId.get();
			_freeIds.set(newId);
			_freeIdCount.decrementAndGet();
			
			final int nextFree = _freeIds.nextClearBit(newId) < 0 ? _freeIds.nextClearBit(0) : _freeIds.nextClearBit(newId);
			if (nextFree < 0)
			{
				if (_freeIds.size() >= FREE_OBJECT_ID_SIZE)
				{
					throw new NullPointerException("데이터베이스 정리: Ran out of valid ids.");
				}
				increaseBitSetCapacity();
			}
			_nextFreeId.set(nextFree);
			
			return newId + FIRST_OID;
		}
	}
	
	private void increaseBitSetCapacity()
	{
		final BitSet newBitSet = new BitSet(PrimeFinder.nextPrime((usedIdCount() * 11) / 10));
		newBitSet.or(_freeIds);
		_freeIds = newBitSet;
	}
	
	private int usedIdCount()
	{
		return _freeIdCount.get() - FIRST_OID;
	}
	
	public static int size()
	{
		return _freeIdCount.get();
	}
	
	public static boolean hasInitialized()
	{
		return _initialized;
	}
	
	public static IdManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IdManager INSTANCE = new IdManager();
	}
}
