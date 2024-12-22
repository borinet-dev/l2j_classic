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
package org.l2jmobius.gameserver;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.enums.ServerMode;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.DeadLockDetector;
import org.l2jmobius.commons.util.PropertiesParser;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.BotReportTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.SchemeBufferTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.CharSummonTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.CrestTable;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.data.sql.NpcNameTable;
import org.l2jmobius.gameserver.data.sql.OfflineTraderTable;
import org.l2jmobius.gameserver.data.sql.SkillNameTable;
import org.l2jmobius.gameserver.data.xml.ActionData;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.data.xml.AgathionData;
import org.l2jmobius.gameserver.data.xml.AppearanceItemData;
import org.l2jmobius.gameserver.data.xml.ArmorSetData;
import org.l2jmobius.gameserver.data.xml.AttendanceRewardData;
import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.ClanHallData;
import org.l2jmobius.gameserver.data.xml.ClanRewardData;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.data.xml.CombinationItemsData;
import org.l2jmobius.gameserver.data.xml.CubicData;
import org.l2jmobius.gameserver.data.xml.DailyMissionData;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.data.xml.ElementalAttributeData;
import org.l2jmobius.gameserver.data.xml.ElementalSpiritData;
import org.l2jmobius.gameserver.data.xml.EnchantItemData;
import org.l2jmobius.gameserver.data.xml.EnchantItemGroupsData;
import org.l2jmobius.gameserver.data.xml.EnchantItemHPBonusData;
import org.l2jmobius.gameserver.data.xml.EnchantItemOptionsData;
import org.l2jmobius.gameserver.data.xml.EnchantSkillGroupsData;
import org.l2jmobius.gameserver.data.xml.EnsoulData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.data.xml.FenceData;
import org.l2jmobius.gameserver.data.xml.FishingData;
import org.l2jmobius.gameserver.data.xml.HennaData;
import org.l2jmobius.gameserver.data.xml.HitConditionBonusData;
import org.l2jmobius.gameserver.data.xml.InitialEquipmentData;
import org.l2jmobius.gameserver.data.xml.InitialShortcutData;
import org.l2jmobius.gameserver.data.xml.ItemCrystallizationData;
import org.l2jmobius.gameserver.data.xml.KarmaData;
import org.l2jmobius.gameserver.data.xml.LuckyGameData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.data.xml.NpcNameLocalisationData;
import org.l2jmobius.gameserver.data.xml.OptionData;
import org.l2jmobius.gameserver.data.xml.PetDataTable;
import org.l2jmobius.gameserver.data.xml.PetSkillData;
import org.l2jmobius.gameserver.data.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.data.xml.PlayerXpPercentLostData;
import org.l2jmobius.gameserver.data.xml.PrimeShopData;
import org.l2jmobius.gameserver.data.xml.RecipeData;
import org.l2jmobius.gameserver.data.xml.ResidenceFunctionsData;
import org.l2jmobius.gameserver.data.xml.SayuneData;
import org.l2jmobius.gameserver.data.xml.SecondaryAuthData;
import org.l2jmobius.gameserver.data.xml.SendMessageLocalisationData;
import org.l2jmobius.gameserver.data.xml.SiegeScheduleData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.data.xml.SpawnData;
import org.l2jmobius.gameserver.data.xml.StaticObjectData;
import org.l2jmobius.gameserver.data.xml.TeleporterData;
import org.l2jmobius.gameserver.data.xml.TransformData;
import org.l2jmobius.gameserver.data.xml.VariationData;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.ConditionHandler;
import org.l2jmobius.gameserver.handler.DailyMissionHandler;
import org.l2jmobius.gameserver.handler.EffectHandler;
import org.l2jmobius.gameserver.handler.SkillConditionHandler;
import org.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import org.l2jmobius.gameserver.instancemanager.AutoBuffManager;
import org.l2jmobius.gameserver.instancemanager.AutoSkillManager;
import org.l2jmobius.gameserver.instancemanager.BoatManager;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import org.l2jmobius.gameserver.instancemanager.ClanHallAuctionManager;
import org.l2jmobius.gameserver.instancemanager.CouponManager;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.instancemanager.CustomClanManager;
import org.l2jmobius.gameserver.instancemanager.CustomMailManager;
import org.l2jmobius.gameserver.instancemanager.DBSpawnManager;
import org.l2jmobius.gameserver.instancemanager.DailyTaskManager;
import org.l2jmobius.gameserver.instancemanager.FactionManager;
import org.l2jmobius.gameserver.instancemanager.FakePlayerChatManager;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.ItemAuctionManager;
import org.l2jmobius.gameserver.instancemanager.ItemCommissionManager;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.instancemanager.MapRegionManager;
import org.l2jmobius.gameserver.instancemanager.MatchingRoomManager;
import org.l2jmobius.gameserver.instancemanager.OfflineModeManager;
import org.l2jmobius.gameserver.instancemanager.PcCafePointsManager;
import org.l2jmobius.gameserver.instancemanager.PetitionManager;
import org.l2jmobius.gameserver.instancemanager.PrecautionaryRestartManager;
import org.l2jmobius.gameserver.instancemanager.PremiumManager;
import org.l2jmobius.gameserver.instancemanager.PunishmentManager;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.instancemanager.SellBuffsManager;
import org.l2jmobius.gameserver.instancemanager.ServerRestartManager;
import org.l2jmobius.gameserver.instancemanager.SiegeGuardManager;
import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.instancemanager.WalkingManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.instancemanager.events.EventDropManager;
import org.l2jmobius.gameserver.instancemanager.games.MiniGameScoreManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.EventResetCheck;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.SpecialEvents;
import org.l2jmobius.gameserver.model.events.WeekendEvent;
import org.l2jmobius.gameserver.model.events.impl.OnServerStart;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.network.ClientNetworkManager;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.scripting.ScriptEngineManager;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.taskmanager.ItemLifeTimeTaskManager;
import org.l2jmobius.gameserver.taskmanager.ItemsAutoDestroyTaskManager;
import org.l2jmobius.gameserver.taskmanager.TaskManager;
import org.l2jmobius.gameserver.ui.Gui;
import org.l2jmobius.gameserver.util.BoatUtil;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.ItemLog;
import org.l2jmobius.gameserver.util.Util;

import smartguard.core.properties.GuardProperties;

public class GameServer
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	// public static volatile int REVISION = 10581;
	public static boolean checkLicense = false;
	public static boolean checkInternet = false;
	public static boolean masterServer = false;
	
	private DeadLockDetector _deadDetectThread;
	private static GameServer INSTANCE;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	public static Date server_started;
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		final long serverLoadStart = System.currentTimeMillis();
		
		// GUI
		final PropertiesParser interfaceConfig = new PropertiesParser(Config.INTERFACE_CONFIG_FILE);
		Config.ENABLE_GUI = interfaceConfig.getBoolean("EnableGUI", true);
		if (Config.ENABLE_GUI && !GraphicsEnvironment.isHeadless())
		{
			Config.DARK_THEME = interfaceConfig.getBoolean("DarkTheme", true);
			System.out.println("GameServer: GUI 모드를 사용 중 입니다..");
			new Gui();
		}
		
		// Create log folder
		final File logFolder = new File(".", "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("./log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		BorinetUtil.jarFile();
		LOGGER.info("=============================================");
		LOGGER.info("Copyright: ..................... L2j-borinet");
		LOGGER.info("Chronicle: .......... Classic 2.8 SevenSigns");
		LOGGER.info("Build date: ..................... " + BorinetUtil.build_date);
		// LOGGER.info("외부 IP: .................... " + BorinetUtil.CheckMyIp());
		LOGGER.info("=============================================");
		
		// Initialize config
		BorinetUtil.getInstance().printSection("컨피그 설정 로드");
		Config.load(ServerMode.GAME);
		
		// 라이센스 체크
		BorinetUtil.getInstance().printSection("라이센스 체크");
		String checkIp = BorinetUtil.CheckMyIp();
		if (checkIp != null)
		{
			checkInternet = true;
			if (checkIp.equals("110.45.203.91") || checkIp.equals("110.45.203.66") || checkIp.equals("110.10.95.215"))
			{
				checkLicense = true;
				masterServer = true;
				LOGGER.info("라이센스 검사를 할 필요 없습니다. 마스터 서버입니다.");
			}
			else
			{
				if (BorinetUtil.getInstance().CheckLicense() == "보유 중")
				{
					checkLicense = true;
				}
				else
				{
					Shutdown.getInstance().scheduleLicense(5);
					return;
				}
			}
			// LOGGER.info("IP: " + checkIp);
		}
		else
		{
			Shutdown.getInstance().scheduleLicense(5);
			return;
		}
		
		if (Config.ALLOW_BOAT)
		{
			BoatUtil.getInstance().setBoatStatus("바이칼 호", "정박 중");
			BoatUtil.getInstance().setBoatStatus("보리넷 호", "정박 중");
			BoatUtil.getInstance().setBoatDestination("바이칼 호", "기란 항구");
			BoatUtil.getInstance().setBoatDestination("보리넷 호", "말하는 섬 항구");
			long departureTime = BorinetTask.getInstance().setBoatSchedule();
			GlobalVariablesManager.getInstance().set("boat_departureTime", departureTime);
		}
		
		BorinetUtil.getInstance().printSection("Database");
		DatabaseFactory.init();
		
		BorinetUtil.getInstance().printSection("Thread Pool");
		ThreadPool.init();
		LOGGER.info("완료!");
		
		// Start game time task manager early
		GameTimeTaskManager.getInstance();
		
		BorinetUtil.getInstance().printSection("데이트베이스 정리");
		IdManager.getInstance();
		if (!IdManager.hasInitialized())
		{
			LOGGER.severe(getClass().getSimpleName() + ": Could not read object IDs from database. Please check your configuration.");
			throw new Exception("Could not initialize the ID factory!");
		}
		
		LongTimeEvent._isSpawned = true;
		EventDispatcher.getInstance();
		ScriptEngineManager.getInstance();
		
		BorinetUtil.getInstance().printSection("World - 로드..");
		World.getInstance();
		ItemLifeTimeTaskManager.getInstance();
		MapRegionManager.getInstance();
		ZoneManager.getInstance();
		DoorData.getInstance();
		FenceData.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();
		LOGGER.info("완료!");
		
		BorinetUtil.getInstance().printSection("Data - 로드..");
		ActionData.getInstance();
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		CombinationItemsData.getInstance();
		SayuneData.getInstance();
		ClanRewardData.getInstance();
		DailyMissionHandler.getInstance().executeScript();
		DailyMissionData.getInstance();
		ElementalSpiritData.getInstance();
		LOGGER.info("완료!");
		
		BorinetUtil.getInstance().printSection("스킬 - 로드..");
		SkillNameTable.getInstance();
		SkillConditionHandler.getInstance().executeScript();
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreeData.getInstance();
		SkillData.getInstance();
		PetSkillData.getInstance();
		LOGGER.info("완료!");
		
		BorinetUtil.getInstance().printSection("아이템 - 로드..");
		ItemNameTable.getInstance();
		ConditionHandler.getInstance().executeScript();
		ItemTable.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		ElementalAttributeData.getInstance();
		ItemCrystallizationData.getInstance();
		OptionData.getInstance();
		VariationData.getInstance();
		EnsoulData.getInstance();
		EnchantItemHPBonusData.getInstance();
		BuyListData.getInstance();
		MultisellData.getInstance();
		AgathionData.getInstance();
		RecipeData.getInstance();
		ArmorSetData.getInstance();
		FishingData.getInstance();
		HennaData.getInstance();
		if (Config.PRIMESHOP_ENABLED)
		{
			PrimeShopData.getInstance();
		}
		if (Config.PC_CAFE_ENABLED)
		{
			PcCafePointsManager.getInstance();
		}
		AppearanceItemData.getInstance();
		ItemCommissionManager.getInstance();
		LuckyGameData.getInstance();
		AttendanceRewardData.getInstance();
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroyTaskManager.getInstance();
		}
		LOGGER.info("완료!");
		
		BorinetUtil.getInstance().printSection("캐릭터 - 로드..");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceData.getInstance();
		PlayerXpPercentLostData.getInstance();
		KarmaData.getInstance();
		HitConditionBonusData.getInstance();
		PlayerTemplateData.getInstance();
		CharInfoTable.getInstance();
		AdminData.getInstance();
		PetDataTable.getInstance();
		CubicData.getInstance();
		CharSummonTable.getInstance().init();
		// BeautyShopData.getInstance();
		// MentorManager.getInstance();
		if (Config.FACTION_SYSTEM_ENABLED)
		{
			FactionManager.getInstance();
			LOGGER.info("진영 시스템이 활성화 되었습니다.");
		}
		if (Config.PREMIUM_SYSTEM_ENABLED)
		{
			LOGGER.info("프리미엄 시스템이 활성화 되었습니다.");
			PremiumManager.getInstance();
		}
		
		BorinetUtil.getInstance().printSection("혈맹 관련 - 로드..");
		ClanTable.getInstance();
		ResidenceFunctionsData.getInstance();
		ClanHallData.getInstance();
		ClanHallAuctionManager.getInstance();
		ClanEntryManager.getInstance();
		CustomClanManager.getInstance();
		
		BorinetUtil.getInstance().printSection("Geodata");
		GeoEngine.getInstance();
		if (GeoEngine.getInstance().loadedRegions < 1)
		{
			LOGGER.info("GeoData 파일이 없습니다! 서버를 종료합니다.");
			Shutdown.getInstance().scheduleLicense(5);
			return;
		}
		
		BorinetUtil.getInstance().printSection("NPC - 로드..");
		NpcNameTable.getInstance();
		NpcData.getInstance();
		FakePlayerData.getInstance();
		FakePlayerChatManager.getInstance();
		SpawnData.getInstance();
		WalkingManager.getInstance();
		StaticObjectData.getInstance();
		if (Config.ALT_ITEM_AUCTION_ENABLED)
		{
			ItemAuctionManager.getInstance();
		}
		CastleManager.getInstance().loadInstances();
		SchemeBufferTable.getInstance();
		EventDropManager.getInstance();
		
		BorinetUtil.getInstance().printSection("그랜드보스");
		GrandBossManager.getInstance();
		
		BorinetUtil.getInstance().printSection("Instance - 로드..");
		InstanceManager.getInstance();
		LOGGER.info("완료!");
		
		BorinetUtil.getInstance().printSection("올림피아드");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		BorinetUtil.getInstance().printSection("캐쉬");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleporterData.getInstance();
		MatchingRoomManager.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		
		BorinetUtil.getInstance().printSection("진정 매니저");
		PetitionManager.getInstance();
		
		BorinetUtil.getInstance().printSection("자동사냥 신고");
		BotReportTable.getInstance();
		BorinetUtil.getInstance().printSection("버프 매니저");
		SellBuffsManager.getInstance();
		AutoBuffManager.getInstance();
		AutoSkillManager.getInstance();
		OfflineModeManager.getInstance();
		if (Config.MULTILANG_ENABLE)
		{
			SystemMessageId.loadLocalisations();
			NpcStringId.loadLocalisations();
			SendMessageLocalisationData.getInstance();
			NpcNameLocalisationData.getInstance();
			LOGGER.info("Multi Lang: 멀티 언어가 활성화 되었습니다.");
		}
		
		BorinetUtil.getInstance().printSection("스크립트 - 로드..");
		SpecialEvents.getInstance();
		WeekendEvent.getInstance();
		QuestManager.getInstance();
		// AirShipManager.getInstance();
		// ShuttleData.getInstance();
		// GraciaSeedsManager.getInstance();
		GlobalVariablesManager.getInstance().set("heavenly_rift_complete", 0);
		GlobalVariablesManager.getInstance().set("heavenly_rift_level", 0);
		MiniGameScoreManager.getInstance();
		// MonsterRace.getInstance();
		try
		{
			ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.MASTER_HANDLER_FILE);
			ScriptEngineManager.getInstance().executeScriptList();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": 스크립트 목록을 실행하지 못했습니다!", e);
		}
		
		if (EventDispatcher.getInstance().hasListener(EventType.ON_SERVER_START))
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnServerStart());
		}
		BorinetUtil.getInstance().printSection("처벌 시스템");
		PunishmentManager.getInstance();
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		BorinetUtil.getInstance().printSection("공성 - 로드..");
		SiegeManager.getInstance().getSieges();
		CastleManager.getInstance().activateInstances();
		// No fortresses
		// FortManager.getInstance().loadInstances();
		// FortManager.getInstance().activateInstances();
		// FortSiegeManager.getInstance();
		SiegeScheduleData.getInstance();
		
		CastleManorManager.getInstance();
		SiegeGuardManager.getInstance();
		
		BorinetUtil.getInstance().printSection("아이템 로그");
		ItemLog.getInstance();
		DailyTaskManager.cleanUpExpiredData();
		
		BorinetUtil.getInstance().printSection("오프라인 상점");
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTraderTable.getInstance().restoreOfflineTraders();
		}
		
		BorinetUtil.getInstance().printSection("Task 매니저");
		TaskManager.getInstance();
		
		BorinetUtil.getInstance().printSection("데일리/위클리 리셋 - 로드..");
		EventResetCheck.resetDailyCheck();
		EventResetCheck.resetRecomCheck();
		DailyTaskManager.getInstance();
		if (Config.AUTO_DELETE_CHAR)
		{
			LOGGER.info("장기 미접속 캐릭터 정리 기능이 활성화 되었습니다.");
		}
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		BorinetUtil.getInstance().printSection("우편 시스템");
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		if (Config.CUSTOM_MAIL_MANAGER_ENABLED)
		{
			CustomMailManager.getInstance();
		}
		
		BorinetUtil.getInstance().printSection("쿠폰 시스템");
		CouponManager.getInstance().loadCoupon();
		
		if (Config.ALLOW_BOAT)
		{
			BorinetUtil.getInstance().printSection("정기선 시스템");
			if (BoatManager.npcSpawned)
			{
				LOGGER.info("정기선 시스템: 선착장 관리인을 로드하였습니다.");
			}
			LOGGER.info("정기선 시스템: " + BoatManager.getInstance()._boats.size() + "개의 정기선을 로드하였습니다.");
		}
		
		BorinetUtil.getInstance().printSection("라이센스");
		LOGGER.info("라이센스 만료까지 남은 시간은 " + (masterServer ? "무기한" : "[" + Util.formatTime(BorinetUtil.getInstance().shutDown) + "]") + " 입니다.");
		
		BorinetUtil.getInstance().printSection("스마트 가드");
		String MaxInstances = GuardProperties.MaxInstances > 0 ? GuardProperties.MaxInstances + "대 입니다." : "없습니다.";
		String Virtual = GuardProperties.AllowVirtualization ? "허용됩니다." : "허용되지 않습니다.";
		if (GuardProperties.ProtectionEnabled)
		{
			LOGGER.info("스마트 가드가 실행됩니다.");
			LOGGER.info("1대의 PC에서 동시 접속제한은 " + MaxInstances);
			LOGGER.info("가상환경에서의 접속이 " + Virtual);
		}
		else
		{
			LOGGER.info("스마트 가드가 실행되지 않습니다.");
			LOGGER.info("1대의 PC에서 동시 접속제한은 없습니다.");
		}
		BorinetUtil.getInstance().printSection("오토방지 문자 시스템");
		if (Config.ENABLE_CAPTCHA_SYSTEM)
		{
			LOGGER.info("오토방지 문자 자동팝업이 활성화 되었습니다.");
		}
		else
		{
			LOGGER.info("오토방지 문자 시스템을 사용하지 않습니다.");
		}
		BorinetUtil.getInstance().printSection("서버 연결 상태");
		
		if (Config.SERVER_RESTART_SCHEDULE_ENABLED)
		{
			ServerRestartManager.getInstance();
		}
		
		if (Config.PRECAUTIONARY_RESTART_ENABLED)
		{
			PrecautionaryRestartManager.getInstance();
		}
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector(Duration.ofSeconds(Config.DEADLOCK_CHECK_INTERVAL), () ->
			{
				if (Config.RESTART_ON_DEADLOCK)
				{
					Broadcast.toAllOnlinePlayers("Server has stability issues - restarting now.");
					Shutdown.getInstance().startShutdown(null, 60, true);
				}
			});
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		LOGGER.info("사용메모리: " + BorinetUtil.getInstance().getTotalMemoryGB() + " GB 중 " + BorinetUtil.getInstance().getUsedMemoryGB() + " GB 사용");
		LOGGER.info("서버 최대 접속허용 인원: " + Config.MAXIMUM_ONLINE_USERS + " 명.");
		LOGGER.info("서버로딩 시간: " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " 초");
		LongTimeEvent._isSpawned = false;
		
		ClientNetworkManager.getInstance().start();
		LoginServerThread.getInstance().start();
		server_started = new Date();
		
		SpawnData.getInstance().init();
		DBSpawnManager.getInstance();
		
		Toolkit.getDefaultToolkit().beep();
		BorinetTask._isActive = true;
		BorinetUtil.getInstance().printSection("게임서버 로딩이 완료되었습니다.");
		deleteLogFiles();
	}
	
	private static void deleteLogFiles()
	{
		File logFolder = new File("log");
		
		// 폴더가 존재하지 않거나 디렉토리가 아닐 경우 종료
		if (!logFolder.exists() || !logFolder.isDirectory())
		{
			return;
		}
		
		// java1.log 이상의 파일과 error1.log 이상의 파일을 찾는 정규식
		Pattern logPattern = Pattern.compile("^(java[1-9][0-9]*\\.log|error[1-9][0-9]*\\.log)$");
		
		// 폴더 내부의 파일 목록을 순회
		File[] files = logFolder.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isFile() && logPattern.matcher(file.getName()).matches())
				{
					file.delete();
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		INSTANCE = new GameServer();
	}
	
	public static GameServer getInstance()
	{
		return INSTANCE;
	}
}
