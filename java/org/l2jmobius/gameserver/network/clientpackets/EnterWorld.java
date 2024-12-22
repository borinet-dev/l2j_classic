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
package org.l2jmobius.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.OfflineTraderTable;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.data.xml.BeautyShopData;
import org.l2jmobius.gameserver.data.xml.ClanHallData;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.enums.SubclassInfoType;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.instancemanager.PetitionManager;
import org.l2jmobius.gameserver.instancemanager.ServerRestartManager;
import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.events.CustomStats;
import org.l2jmobius.gameserver.model.events.EnterEventTimes;
import org.l2jmobius.gameserver.model.holders.AttendanceInfoHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.residences.ClanHall;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.model.siege.FortSiege;
import org.l2jmobius.gameserver.model.siege.Siege;
import org.l2jmobius.gameserver.model.skill.AbnormalType;
import org.l2jmobius.gameserver.model.skill.AbnormalVisualEffect;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.ConnectionState;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.Die;
import org.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ExAdenaInvenCount;
import org.l2jmobius.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jmobius.gameserver.network.serverpackets.ExBasicActionList;
import org.l2jmobius.gameserver.network.serverpackets.ExBeautyItemList;
import org.l2jmobius.gameserver.network.serverpackets.ExBrPremiumState;
import org.l2jmobius.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import org.l2jmobius.gameserver.network.serverpackets.ExNoticePostArrived;
import org.l2jmobius.gameserver.network.serverpackets.ExNotifyPremiumItem;
import org.l2jmobius.gameserver.network.serverpackets.ExPCCafePointInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeWaitingListAlarm;
import org.l2jmobius.gameserver.network.serverpackets.ExQuestItemList;
import org.l2jmobius.gameserver.network.serverpackets.ExRotation;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import org.l2jmobius.gameserver.network.serverpackets.ExSubjobInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExUnReadMailCount;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import org.l2jmobius.gameserver.network.serverpackets.ExVitalityEffectInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExWorldChatCnt;
import org.l2jmobius.gameserver.network.serverpackets.HennaInfo;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeSkillList;
import org.l2jmobius.gameserver.network.serverpackets.QuestList;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutInit;
import org.l2jmobius.gameserver.network.serverpackets.SkillCoolTime;
import org.l2jmobius.gameserver.network.serverpackets.SkillList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.attendance.ExVipAttendanceItemList;
import org.l2jmobius.gameserver.network.serverpackets.dailymission.ExConnectedTimeAndGettableReward;
import org.l2jmobius.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;
import org.l2jmobius.gameserver.network.serverpackets.friend.L2FriendList;
import org.l2jmobius.gameserver.taskmanager.auto.AutoItemTaskManager;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.BuilderUtil;
import org.l2jmobius.gameserver.util.CaptchaSystem.CaptchaEvent;
import org.l2jmobius.gameserver.util.CaptchaSystem.CaptchaTimer;

public class EnterWorld implements IClientIncomingPacket
{
	public static final Logger LOGGER = Logger.getLogger(EnterWorld.class.getName());
	private final int[][] _tracert = new int[5][4];
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		for (int i = 0; i < 5; i++)
		{
			for (int o = 0; o < 4; o++)
			{
				_tracert[i][o] = packet.readC();
			}
		}
		packet.readD(); // Unknown Value
		packet.readD(); // Unknown Value
		packet.readD(); // Unknown Value
		packet.readD(); // Unknown Value
		packet.readB(64); // Unknown Byte Array
		packet.readD(); // Unknown Value
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			PacketLogger.warning("EnterWorld failed! player returned 'null'.");
			Disconnection.of(client).defaultSequence(LeaveWorld.STATIC_PACKET);
			return;
		}
		
		client.setConnectionState(ConnectionState.IN_GAME);
		
		final String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
		{
			adress[i] = _tracert[i][0] + "." + _tracert[i][1] + "." + _tracert[i][2] + "." + _tracert[i][3];
		}
		
		LoginServerThread.getInstance().sendClientTracert(player.getAccountName(), adress);
		client.setClientTracert(_tracert);
		
		player.updateExpertise();
		player.broadcastUserInfo();
		
		// Restore to instanced area if enabled
		if (Config.RESTORE_PLAYER_INSTANCE)
		{
			final PlayerVariables vars = player.getVariables();
			final Instance instance = InstanceManager.getInstance().getPlayerInstance(player, false);
			if ((instance != null) && (instance.getId() == vars.getInt("INSTANCE_RESTORE", 0)))
			{
				player.setInstance(instance);
			}
			vars.remove("INSTANCE_RESTORE");
		}
		
		EnterEventTimes.selectDB(player);
		// EnterEventTimes.EnterEventTimeStart(player);
		player.updatePvpTitleAndColor(false);
		CustomStats.getInstance().insertSpec(player);
		CustomStats.getInstance().insertStat(player);
		CustomStats.getInstance().checkName(player);
		
		player.getVariables().remove("자동따라가기");
		player.getVariables().remove("자동따라가기설정");
		
		/*
		 * if (player.getX() == 0) { player.teleToLocation(-73200, 256134, -3120); }
		 */
		
		// Apply special GM properties to the GM when entering
		if (player.isGM())
		{
			gmStartupProcess:
			{
				if (AdminData.getInstance().hasAccess("admin_hide", player.getAccessLevel()))
				{
					BuilderUtil.setHiding(player, true);
					// BuilderUtil.sendSysMessage(player, "hide is default for builder.");
					// BuilderUtil.sendSysMessage(player, "FriendAddOff is default for builder.");
					// BuilderUtil.sendSysMessage(player, "whisperoff is default for builder.");
					
					// It isn't recommend to use the below custom L2J GMStartup functions together with retail-like GMStartupBuilderHide, so breaking the process at that stage.
					break gmStartupProcess;
				}
				
				if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", player.getAccessLevel()))
				{
					player.setInvul(true);
				}
				
				if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", player.getAccessLevel()))
				{
					player.setInvisible(true);
					player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.STEALTH);
				}
				
				if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
				{
					player.setSilenceMode(true);
				}
				
				if (Config.GM_STARTUP_DIET_MODE && AdminData.getInstance().hasAccess("admin_diet", player.getAccessLevel()))
				{
					player.setDietMode(true);
					player.refreshOverloaded(true);
				}
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmliston", player.getAccessLevel()))
			{
				AdminData.getInstance().addGm(player, false);
			}
			else
			{
				AdminData.getInstance().addGm(player, true);
			}
			
			if (Config.GM_GIVE_SPECIAL_SKILLS)
			{
				SkillTreeData.getInstance().addSkills(player, false);
			}
			
			if (Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				SkillTreeData.getInstance().addSkills(player, true);
			}
		}
		
		int inventory = player.getVariables().getInt("인벤토리확장", 0);
		if (inventory != 0)
		{
			player._expandInventory = inventory;
		}
		
		// Chat banned icon.
		if (player.isChatBanned())
		{
			player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.NO_CHAT);
		}
		
		// Set dead status if applies
		if (player.getCurrentHp() < 0.5)
		{
			player.setDead(true);
		}
		
		boolean showClanNotice = false;
		
		// Clan related checks are here
		final Clan clan = player.getClan();
		if (clan != null)
		{
			notifyClanMembers(player);
			notifySponsorOrApprentice(player);
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(clan))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(siege.getCastle().getResidenceId());
				}
				
				else if (siege.checkIsDefender(clan))
				{
					player.setSiegeState((byte) 2);
					player.setSiegeSide(siege.getCastle().getResidenceId());
				}
			}
			
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(clan))
				{
					player.setSiegeState((byte) 1);
					player.setSiegeSide(siege.getFort().getResidenceId());
				}
				
				else if (siege.checkIsDefender(clan))
				{
					player.setSiegeState((byte) 2);
					player.setSiegeSide(siege.getFort().getResidenceId());
				}
			}
			
			// Residential skills support
			if (clan.getCastleId() > 0)
			{
				final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if (castle != null)
				{
					castle.giveResidentialSkills(player);
				}
			}
			
			if (clan.getFortId() > 0)
			{
				final Fort fort = FortManager.getInstance().getFortByOwner(clan);
				if (fort != null)
				{
					fort.giveResidentialSkills(player);
				}
			}
			
			showClanNotice = clan.isNoticeEnabled();
		}
		
		if (Config.ENABLE_VITALITY)
		{
			player.sendPacket(new ExVitalityEffectInfo(player));
		}
		
		// Send Macro List
		player.getMacros().sendAllMacros();
		
		// Send Teleport Bookmark List
		player.sendPacket(new ExGetBookMarkInfoPacket(player));
		
		// Send Item List
		player.sendPacket(new ItemList(1, player));
		player.sendPacket(new ItemList(2, player));
		
		// Send Quest Item List
		player.sendPacket(new ExQuestItemList(1, player));
		player.sendPacket(new ExQuestItemList(2, player));
		
		// Send Shortcuts
		player.sendPacket(new ShortCutInit(player));
		
		// Send Action list
		player.sendPacket(ExBasicActionList.STATIC_PACKET);
		
		// Send blank skill list
		player.sendPacket(new SkillList());
		CustomStats.getInstance().addskills(player);
		
		// Send Dye Information
		player.sendPacket(new HennaInfo(player));
		
		// Send Skill list
		player.sendSkillList();
		
		// Send EtcStatusUpdate
		player.sendPacket(new EtcStatusUpdate(player));
		
		// Clan packets
		if (clan != null)
		{
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(player));
			PledgeShowMemberListAll.sendAllTo(player);
			clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
			player.sendPacket(new PledgeSkillList(clan));
			final ClanHall ch = ClanHallData.getInstance().getClanHallByClan(clan);
			if ((ch != null) && (ch.getCostFailDay() > 0) && (ch.getResidenceId() < 186))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
				sm.addInt(ch.getLease());
				player.sendPacket(sm);
			}
		}
		else
		{
			player.sendPacket(ExPledgeWaitingListAlarm.STATIC_PACKET);
		}
		
		// Send SubClass Info
		player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.NO_CHANGES));
		
		// Send Inventory Info
		player.sendPacket(new ExUserInfoInvenWeight(player));
		
		// Send Adena / Inventory Count Info
		player.sendPacket(new ExAdenaInvenCount(player));
		
		// Send Equipped Items
		player.sendPacket(new ExUserInfoEquipSlot(player));
		
		// Send VIP/Premium Info
		player.sendPacket(new ExBrPremiumState(player));
		
		// Send Unread Mail Count
		if (MailManager.getInstance().hasUnreadPost(player))
		{
			player.sendPacket(new ExUnReadMailCount(player));
		}
		
		// Faction System
		if (Config.FACTION_SYSTEM_ENABLED)
		{
			if (player.isGood())
			{
				player.getAppearance().setNameColor(Config.FACTION_GOOD_NAME_COLOR);
				player.getAppearance().setTitleColor(Config.FACTION_GOOD_NAME_COLOR);
				player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_GOOD_TEAM_NAME + " faction.");
				player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_GOOD_TEAM_NAME + " faction.", 10000));
			}
			else if (player.isEvil())
			{
				player.getAppearance().setNameColor(Config.FACTION_EVIL_NAME_COLOR);
				player.getAppearance().setTitleColor(Config.FACTION_EVIL_NAME_COLOR);
				player.sendMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_EVIL_TEAM_NAME + " faction.");
				player.sendPacket(new ExShowScreenMessage("Welcome " + player.getName() + ", you are fighting for the " + Config.FACTION_EVIL_TEAM_NAME + " faction.", 10000));
			}
		}
		
		Quest.playerEnter(player);
		
		// Send Quest List
		player.sendPacket(new QuestList(player));
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			player.setSpawnProtection(true);
		}
		
		player.spawnMe(player.getX(), player.getY(), player.getZ());
		player.sendPacket(new ExRotation(player.getObjectId(), player.getHeading()));
		
		if (player.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(player.getCursedWeaponEquippedId()).cursedOnLogin();
		}
		
		if (Config.PC_CAFE_ENABLED)
		{
			if (player.getPcCafePoints() > 0)
			{
				player.sendPacket(new ExPCCafePointInfo(player.getPcCafePoints(), 0, 1));
			}
			else
			{
				player.sendPacket(new ExPCCafePointInfo());
			}
		}
		
		// Expand Skill
		player.sendPacket(new ExStorageMaxCount(player));
		
		// Friend list
		player.sendPacket(new L2FriendList(player));
		SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FRIEND_S1_JUST_LOGGED_IN);
		sm.addString(player.getName());
		for (int id : player.getFriendList())
		{
			final WorldObject obj = World.getInstance().findObject(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}
		
		player.sendPacket(SystemMessageId.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
		
		AnnouncementsTable.getInstance().showAnnouncements(player);
		
		if ((Config.SERVER_RESTART_SCHEDULE_ENABLED) && (Config.SERVER_RESTART_SCHEDULE_MESSAGE))
		{
			player.sendMessage("다음 서버 자동재시작 일시는 [" + ServerRestartManager.getInstance().getNextRestartTime() + "] 입니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "다음 자동재시작 일시는 [" + ServerRestartManager.getInstance().getNextRestartTime() + "] 입니다."));
		}
		
		if (showClanNotice)
		{
			final NpcHtmlMessage notice = new NpcHtmlMessage();
			notice.setFile(player, "data/html/clanNotice.htm");
			notice.replace("%clan_name%", player.getClan().getName());
			notice.replace("%notice_text%", player.getClan().getNotice().replaceAll("\r\n", "<br>"));
			notice.disableValidation();
			player.sendPacket(notice);
		}
		else if (Config.SERVER_NEWS)
		{
			final String serverNews = HtmCache.getInstance().getHtm(player, "data/html/servnews.htm");
			if (serverNews != null)
			{
				player.sendPacket(new NpcHtmlMessage(serverNews));
			}
		}
		
		if (Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(player);
		}
		
		if (player.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			player.sendPacket(new Die(player));
		}
		
		player.onPlayerEnter();
		
		player.sendPacket(new SkillCoolTime(player));
		player.sendPacket(new ExVoteSystemInfo(player));
		for (Item item : player.getInventory().getItems())
		{
			if (item.isTimeLimitedItem())
			{
				item.scheduleLifeTimeTask();
			}
			if (item.isShadowItem() && item.isEquipped())
			{
				item.decreaseMana(false);
			}
		}
		
		for (Item whItem : player.getWarehouse().getItems())
		{
			if (whItem.isTimeLimitedItem())
			{
				whItem.scheduleLifeTimeTask();
			}
		}
		
		if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			if (Config.ALT_CLAN_JOIN_DAYS > 1)
			{
				player.sendMessage("혈맹에서 제명되었습니다. " + Config.ALT_CLAN_JOIN_DAYS + "일 동안은 다른 혈맹에 가입할 수 없습니다.");
			}
			player.getVariables().remove("신규자혈맹가입");
		}
		
		// remove combat flag before teleporting
		if (player.getInventory().getItemByItemId(9819) != null)
		{
			final Fort fort = FortManager.getInstance().getFort(player);
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(player, fort.getResidenceId());
			}
			else
			{
				final long slot = player.getInventory().getSlotFromItem(player.getInventory().getItemByItemId(9819));
				player.getInventory().unEquipItemInBodySlot(slot);
				player.destroyItem("CombatFlag", player.getInventory().getItemByItemId(9819), null, true);
			}
		}
		
		// Attacker or spectator logging in to a siege zone.
		// Actually should be checked for inside castle only?
		if (!player.canOverrideCond(PlayerCondOverride.ZONE_CONDITIONS) && player.isInsideZone(ZoneId.SIEGE) && (!player.isInSiege() || (player.getSiegeState() < 2)))
		{
			player.teleToLocation(TeleportWhereType.TOWN);
		}
		
		// Remove demonic weapon if character is not cursed weapon equipped.
		if ((player.getInventory().getItemByItemId(8190) != null) && !player.isCursedWeaponEquipped())
		{
			player.destroyItem("자리체", player.getInventory().getItemByItemId(8190), null, true);
		}
		if ((player.getInventory().getItemByItemId(8689) != null) && !player.isCursedWeaponEquipped())
		{
			player.destroyItem("아카마나프", player.getInventory().getItemByItemId(8689), null, true);
		}
		
		if (Config.ALLOW_MAIL)
		{
			if (MailManager.getInstance().hasUnreadPost(player))
			{
				player.sendPacket(ExNoticePostArrived.valueOf(false));
			}
		}
		
		if (Config.WELCOME_MESSAGE_ENABLED)
		{
			player.sendPacket(new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME));
		}
		
		final int birthday = player.checkBirthDay();
		if ((birthday >= 1) && (birthday <= 3))
		{
			if (player.getVariables().getInt("축하메세지", 0) == 0)
			{
				sm = new SystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_REMAINING_UNTIL_YOUR_BIRTHDAY_ON_YOUR_BIRTHDAY_YOU_WILL_RECEIVE_A_GIFT_THAT_ALEGRIA_HAS_CAREFULLY_PREPARED);
				sm.addString(Integer.toString(birthday));
				player.sendPacket(sm);
				player.getVariables().set("축하메세지", 1);
			}
		}
		else if (birthday == 0)
		{
			if (player.getVariables().getInt("축하메세지", 0) == 0)
			{
				player.sendPacket(SystemMessageId.HAPPY_BIRTHDAY_ALEGRIA_HAS_SENT_YOU_A_BIRTHDAY_GIFT);
				player.getVariables().set("축하메세지", 1);
			}
		}
		
		if (!player.getPremiumItemList().isEmpty())
		{
			player.sendPacket(ExNotifyPremiumItem.STATIC_PACKET);
		}
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.STORE_OFFLINE_TRADE_IN_REALTIME)
		{
			OfflineTraderTable.getInstance().onTransaction(player, true, false);
		}
		
		// Check if expoff is enabled.
		if (player.getVariables().getBoolean("EXPOFF", false))
		{
			player.disableExpGain();
			player.sendMessage("Experience gain is disabled.");
		}
		
		player.broadcastUserInfo();
		
		if (BeautyShopData.getInstance().hasBeautyData(player.getRace(), player.getAppearance().getSexType()))
		{
			player.sendPacket(new ExBeautyItemList(player));
		}
		
		if (Config.ENABLE_WORLD_CHAT)
		{
			player.sendPacket(new ExWorldChatCnt(player));
		}
		player.sendPacket(new ExConnectedTimeAndGettableReward(player));
		player.sendPacket(new ExOneDayReceiveRewardList(player, true));
		
		// Handle soulshots, disable all on EnterWorld
		player.sendPacket(new ExAutoSoulShot(0, true, 0));
		player.sendPacket(new ExAutoSoulShot(0, true, 1));
		player.sendPacket(new ExAutoSoulShot(0, true, 2));
		player.sendPacket(new ExAutoSoulShot(0, true, 3));
		
		// Client settings restore.
		player.getClientSettings();
		
		// Fix for equipped item skills
		if (!player.getEffectList().getCurrentAbnormalVisualEffects().isEmpty())
		{
			player.updateAbnormalVisualEffects();
		}
		
		// Activate first agathion when available.
		final Item agathion = player.getInventory().unEquipItemInBodySlot(ItemTemplate.SLOT_AGATHION);
		if (agathion != null)
		{
			player.getInventory().equipItemAndRecord(agathion);
		}
		
		if (Config.ENABLE_ATTENDANCE_REWARDS)
		{
			if (Config.ATTENDANCE_REWARD_DELAY != 0)
			{
				ThreadPool.schedule(() ->
				{
					// Check if player can receive reward today.
					final AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
					if (attendanceInfo.isRewardAvailable())
					{
						final int lastRewardIndex = attendanceInfo.getRewardIndex() + 1;
						final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_DAY_S1_ATTENDANCE_REWARD_IS_READY_CLICK_ON_THE_REWARDS_ICON_YOU_CAN_REDEEM_YOUR_REWARD_30_MINUTES_AFTER_LOGGING_IN);
						msg.addInt(lastRewardIndex);
						player.sendPacket(msg);
						
						player.sendPacket(new ExShowScreenMessage(lastRewardIndex + "일차 출석 보상을 받을 수 있습니다. 보상 아이콘을 클릭해 주세요.", ExShowScreenMessage.TOP_CENTER, 7000, 0, true, true));
						if (Config.ATTENDANCE_POPUP_WINDOW)
						{
							player.sendPacket(new ExVipAttendanceItemList(player));
						}
					}
				}, Config.ATTENDANCE_REWARD_DELAY * 60 * 1000);
			}
			else if (Config.ATTENDANCE_REWARD_DELAY == 0)
			{
				// Check if player can receive reward today.
				final AttendanceInfoHolder attendanceInfo = player.getAttendanceInfo();
				if (attendanceInfo.isRewardAvailable())
				{
					final int lastRewardIndex = attendanceInfo.getRewardIndex() + 1;
					final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_DAY_S1_ATTENDANCE_REWARD_IS_READY_CLICK_ON_THE_REWARDS_ICON_YOU_CAN_REDEEM_YOUR_REWARD_30_MINUTES_AFTER_LOGGING_IN);
					msg.addInt(lastRewardIndex);
					player.sendPacket(msg);
					
					player.sendPacket(new ExShowScreenMessage(lastRewardIndex + "일차 출석 보상을 받을 수 있습니다. 보상 아이콘을 클릭해 주세요.", ExShowScreenMessage.TOP_CENTER, 7000, 0, true, true));
					if (Config.ATTENDANCE_POPUP_WINDOW)
					{
						player.sendPacket(new ExVipAttendanceItemList(player));
					}
				}
			}
			
			if (Config.ATTENDANCE_POPUP_START)
			{
				player.sendPacket(new ExVipAttendanceItemList(player));
			}
		}
		
		if (player.getClan() != null)
		{
			final PlayerVariables vars = player.getVariables();
			int claimedRewards = vars.getInt("CLAIMED_CLAN_REWARDS", -1);
			if (claimedRewards < 0)
			{
				vars.set("CLAIMED_CLAN_REWARDS", 0);
				vars.storeMe();
			}
		}
		
		if (Config.ENABLE_CAPTCHA_SYSTEM)
		{
			player.getStat().setStartingExp(player.getStat().getExp());
			player.clearCaptcha();
		}
		
		if (player.isGM())
		{
			BorinetUtil.getCounts();
			player.sendMessage("접속자 보고서");
			player.sendMessage("현재 접속자: " + BorinetUtil.online + "명 | 오프라인 상점: " + BorinetUtil.offline + "명 | 낚시: " + BorinetUtil.fishing + "명");
			player.sendMessage("접속자 통계: " + BorinetUtil.total + "명 | 최대 접속자: " + World.MAX_CONNECTED_COUNT + "명");
			if (PetitionManager.getInstance().getPendingPetitionCount() >= 1)
			{
				PetitionManager.getInstance().sendPendingPetitionList(player);
			}
		}
		
		if (player.getDeathPenaltyLevel() > 0)
		{
			player.castDeathPenaltyBuff(player.getDeathPenaltyLevel());
			player.startPenalty(false);
		}
		
		final int hp_perc = player.getVariables().getInt("AUTO_POTION_HP_PERCENT", 0);
		if (hp_perc > 0)
		{
			AutoItemTaskManager.getInstance().addAutoPotion(player);
			player.sendMessage("자동 포션 기능이 활성화 되어있습니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동 포션 기능이 활성화 되어있습니다."));
		}
		if (BorinetUtil.usingAutoItem(player))
		{
			player.sendMessage("자동 아이템 기능이 활성화 되어있습니다.");
			AutoItemTaskManager.getInstance().addAutoItem(player);
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동 아이템 기능이 활성화 되어있습니다."));
			CommonSkill.AUTO_ITEM.getSkill().applyEffects(player, player);
		}
		
		if (player.getVariables().getBoolean("자동아이템사용_일시정지", false))
		{
			AutoItemTaskManager.getInstance().addAutoItem(player);
			player.getVariables().remove("자동아이템사용_일시정지");
			player.getVariables().set("자동아이템사용", true);
			player.sendMessage("자동 아이템 사용이 시작 되었습니다.");
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "자동 아이템 사용이 시작 되었습니다."));
		}
		
		insertDB(player);
		
		if (BorinetTask.SpecialEvent() || BorinetTask.WeekendCheck() || BorinetTask.MemorialDayCheck())
		{
			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, BorinetUtil.getInstance().getEventName() + " 이벤트가 진행 중 입니다!"));
		}
		
		// Chat banned icon.
		ThreadPool.schedule(() ->
		{
			if (player.isChatBanned())
			{
				player.getEffectList().startAbnormalVisualEffect(AbnormalVisualEffect.NO_CHAT);
			}
		}, 5500);
		
		player.setCurrentCp(player.getMaxCp());
		// EnterWorld has finished.
		player.setEnteredWorld();
		
		CaptchaEvent event = CaptchaTimer.getInstance().getAutoMyEvent(player);
		if (event != null)
		{
			player.deleteQuickVar("IsCaptchaActive");
			player.clearCaptcha();
			player.deleteQuickVar("LastCaptcha");
		}
		
		// 컬렉터 옵션 추가
		player.applyCollectionRewards();
		player.deleteQuickVar("boatReward"); // 보상 체크 상태를 취소됨으로 설정
		
		final Item ticket = player.getInventory().getItemByItemId(41366);
		final InventoryUpdate iu = new InventoryUpdate();
		if (ticket != null)
		{
			// 아이템의 개수를 가져옴
			long ticketCount = ticket.getCount();
			// 총 지급할 금액 계산
			long amount = ticketCount * 3471720;
			player.sendMessage("[정기선 배표]는 더이상 사용하지 않아 " + ticketCount + "장을 회수합니다.");
			
			player.getInventory().destroyItemByItemId("정기선 배표", 41366, ticketCount, player, null);
			iu.addModifiedItem(ticket);
			player.sendInventoryUpdate(iu);
			player.addItem("정기선 배표 보상", 57, amount, null, true);
		}
		
		if (!player.isUsingChristmasRod(46286))
		{
			player.getEffectList().stopEffects(AbnormalType.CHRISTMAS_FESTIVAL);
		}
	}
	
	private void insertDB(Player player)
	{
		String query = "REPLACE INTO character_hwid (account, char_name, charId, hwid) VALUES (?,?,?,?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, player.getAccountName());
			ps.setString(2, player.getName());
			ps.setInt(3, player.getObjectId());
			ps.setString(4, player.getHWID());
			ps.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "HWID를 데이터베이스에 삽입하는 중 오류가 발생했습니다.", e);
		}
	}
	
	/**
	 * @param player
	 */
	private void notifyClanMembers(Player player)
	{
		final Clan clan = player.getClan();
		if (clan != null)
		{
			clan.getClanMember(player.getObjectId()).setPlayer(player);
			
			final SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME);
			msg.addString(player.getName());
			clan.broadcastToOtherOnlineMembers(msg, player);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(player), player);
		}
	}
	
	/**
	 * @param player
	 */
	private void notifySponsorOrApprentice(Player player)
	{
		if (player.getSponsor() != 0)
		{
			final Player sponsor = World.getInstance().getPlayer(player.getSponsor());
			if (sponsor != null)
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(player.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (player.getApprentice() != 0)
		{
			final Player apprentice = World.getInstance().getPlayer(player.getApprentice());
			if (apprentice != null)
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(player.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
}
