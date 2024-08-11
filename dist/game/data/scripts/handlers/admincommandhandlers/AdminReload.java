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
package handlers.admincommandhandlers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.enums.ServerMode;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.CrestTable;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.data.xml.AppearanceItemData;
import org.l2jmobius.gameserver.data.xml.ArmorSetData;
import org.l2jmobius.gameserver.data.xml.AttendanceRewardData;
import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.CombinationItemsData;
import org.l2jmobius.gameserver.data.xml.DailyMissionData;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.data.xml.EnchantItemData;
import org.l2jmobius.gameserver.data.xml.EnchantItemGroupsData;
import org.l2jmobius.gameserver.data.xml.EnchantItemOptionsData;
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.data.xml.FishingData;
import org.l2jmobius.gameserver.data.xml.ItemCrystallizationData;
import org.l2jmobius.gameserver.data.xml.LuckyGameData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.data.xml.NpcNameLocalisationData;
import org.l2jmobius.gameserver.data.xml.OptionData;
import org.l2jmobius.gameserver.data.xml.PrimeShopData;
import org.l2jmobius.gameserver.data.xml.RecipeData;
import org.l2jmobius.gameserver.data.xml.SayuneData;
import org.l2jmobius.gameserver.data.xml.SendMessageLocalisationData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.data.xml.TeleporterData;
import org.l2jmobius.gameserver.data.xml.TransformData;
import org.l2jmobius.gameserver.data.xml.VariationData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.instancemanager.AutoBuffManager;
import org.l2jmobius.gameserver.instancemanager.AutoSkillManager;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.instancemanager.FakePlayerChatManager;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.instancemanager.WalkingManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.events.EventResetCheck;
import org.l2jmobius.gameserver.model.holders.SubClassHolder;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.ExVitalityEffectInfo;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import org.l2jmobius.gameserver.scripting.ScriptEngineManager;
import org.l2jmobius.gameserver.util.BuilderUtil;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author NosBit
 */
public class AdminReload implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminReload.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_reload"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		if (actualCommand.equalsIgnoreCase("admin_reload"))
		{
			if (!st.hasMoreTokens())
			{
				AdminHtml.showAdminHtml(activeChar, "reload.htm");
				return true;
			}
			
			ThreadPool.execute(() ->
			{
				final String type = st.nextToken();
				switch (type.toLowerCase())
				{
					case "minigame":
					{
						try (Connection con = DatabaseFactory.getConnection())
						{
							try (
								PreparedStatement rs = con.prepareStatement("SELECT characters.char_name AS name, character_minigame_score.object_id AS object_id, character_minigame_score.score AS score FROM characters, character_minigame_score WHERE characters.charId=character_minigame_score.object_id ORDER BY score DESC LIMIT 1"))
							{
								ResultSet check = rs.executeQuery();
								while (check.next())
								{
									String name = check.getString("char_name");
									int charid = check.getInt("object_id");
									
									if (name != null)
									{
										PreparedStatement rsb = con.prepareStatement("INSERT INTO items_reward_mail (char_name, charId, delivered) VALUES ('" + name + "', '" + charid + "', 0)");
										rsb.execute();
										for (Player player : World.getInstance().getPlayers())
										{
											player.sendMessage("새로운 미니게임 경기가 시작되었으며, 지난 경기 1위에게 상품이 지급되었습니다!");
											player.sendPacket(new ExShowScreenMessage("새로운 미니게임 경기가 시작되었으며, 지난 경기 1위에게 상품이 지급되었습니다!", ExShowScreenMessage.TOP_CENTER, 7000, 0, true, true));
											player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "새로운 미니게임 경기가 시작되었으며, 지난 경기 1위에게 상품이 지급되었습니다!"));
										}
									}
									else
									{
										for (Player player : World.getInstance().getPlayers())
										{
											player.sendMessage("새로운 미니게임 경기가 시작되었으며, 지난 경기의 1위는 없습니다!");
											player.sendPacket(new ExShowScreenMessage("새로운 미니게임 경기가 시작되었으며, 지난 경기의 1위는 없습니다!", ExShowScreenMessage.TOP_CENTER, 7000, 0, true, true));
											player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "새로운 미니게임 경기가 시작되었으며, 지난 경기의 1위는 없습니다!"));
										}
									}
								}
							}
							try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_minigame_score"))
							{
								ps.execute();
							}
						}
						catch (SQLException e)
						{
							LOGGER.log(Level.WARNING, "미니게임 리셋중 오류가 발생했습니다.", e);
						}
						LOGGER.info("미니게임: 스코어가 리셋되었으며, 새로운 미니게임 경기가 시작되었습니다.");
						break;
					}
					case "popup":
					{
						// 확인/취소 버튼이 있는 팝업창 추가
						String popupMessage = "정기선 정박이 완료될때까지 기다려 주십시오.";
						activeChar.sendPacket(new ConfirmDlg(popupMessage));
						break;
					}
					case "autoskill":
					{
						AutoSkillManager.getInstance().load();
						break;
					}
					case "autobuff":
					{
						AutoBuffManager.getInstance().load();
						break;
					}
					case "vp":
					{
						for (Player player : World.getInstance().getPlayers())
						{
							int vp1 = player.getVitalityPoints() + PlayerStat.RESET_VITALITY_POINTS;
							if (vp1 >= PlayerStat.MAX_VITALITY_POINTS)
							{
								player.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS, false);
							}
							else
							{
								player.setVitalityPoints(vp1, false);
							}
							for (SubClassHolder subclass : player.getSubClasses().values())
							{
								if (vp1 >= PlayerStat.MAX_VITALITY_POINTS)
								{
									subclass.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS);
								}
								else
								{
									subclass.setVitalityPoints(vp1);
								}
							}
							activeChar.sendPacket(new ExVitalityEffectInfo(activeChar));
						}
						int charid = 0;
						int classid = 0;
						int vp2 = 0;
						try (Connection con1 = DatabaseFactory.getConnection();
							PreparedStatement ps1 = con1.prepareStatement("SELECT charId, vitality_points FROM characters WHERE vitality_points < " + PlayerStat.MAX_VITALITY_POINTS))
						{
							try (ResultSet rs1 = ps1.executeQuery())
							{
								while (rs1.next())
								{
									charid = rs1.getInt("charId");
									vp2 = rs1.getInt("vitality_points");
									
									try (PreparedStatement mt1 = con1.prepareStatement("UPDATE characters SET vitality_points = ? WHERE charId = ?"))
									{
										if ((vp2 + PlayerStat.RESET_VITALITY_POINTS) >= PlayerStat.MAX_VITALITY_POINTS)
										{
											mt1.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
											mt1.setInt(2, charid);
											mt1.execute();
										}
										else
										{
											mt1.setInt(1, vp2 + PlayerStat.RESET_VITALITY_POINTS);
											mt1.setInt(2, charid);
											mt1.execute();
										}
									}
								}
							}
						}
						catch (Exception e1)
						{
							LOGGER.log(Level.WARNING, "Error while updating vitality", e1);
						}
						try (Connection con2 = DatabaseFactory.getConnection();
							PreparedStatement ps2 = con2.prepareStatement("SELECT charId, class_id, vitality_points FROM character_subclasses WHERE vitality_points < " + PlayerStat.MAX_VITALITY_POINTS))
						{
							try (ResultSet rs2 = ps2.executeQuery())
							{
								while (rs2.next())
								{
									charid = rs2.getInt("charId");
									classid = rs2.getInt("class_id");
									vp2 = rs2.getInt("vitality_points");
									
									try (PreparedStatement mt2 = con2.prepareStatement("UPDATE character_subclasses SET vitality_points = ? WHERE charId = ? AND class_id = ?"))
									{
										if ((vp2 + PlayerStat.RESET_VITALITY_POINTS) >= PlayerStat.MAX_VITALITY_POINTS)
										{
											mt2.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
											mt2.setInt(2, charid);
											mt2.setInt(3, classid);
											mt2.execute();
										}
										else
										{
											mt2.setInt(1, vp2 + PlayerStat.RESET_VITALITY_POINTS);
											mt2.setInt(2, charid);
											mt2.setInt(3, classid);
											mt2.execute();
										}
									}
								}
							}
						}
						catch (Exception e2)
						{
							LOGGER.log(Level.WARNING, "Error while updating vitality", e2);
						}
						LOGGER.info("사이하의 은총 25% 지급");
						break;
					}
					case "config":
					{
						Config.load(ServerMode.GAME);
						AdminData.getInstance().broadcastMessageToGMs("Configs 리로딩 완료.");
						break;
					}
					case "access":
					{
						AdminData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Access 리로딩 완료.");
						break;
					}
					case "npc":
					{
						NpcData.getInstance().reload();
						AdminData.getInstance().broadcastMessageToGMs("Npc 리로딩 완료.");
						break;
					}
					case "quest":
					{
						if (st.hasMoreElements())
						{
							final String value = st.nextToken();
							if (!Util.isDigit(value))
							{
								QuestManager.getInstance().reload(value);
								AdminData.getInstance().broadcastMessageToGMs("Quest Name:" + value + ".");
							}
							else
							{
								final int questId = Integer.parseInt(value);
								QuestManager.getInstance().reload(questId);
								AdminData.getInstance().broadcastMessageToGMs("Quest ID:" + questId + ".");
							}
						}
						else
						{
							QuestManager.getInstance().reloadAllScripts();
							AdminData.getInstance().broadcastMessageToGMs("스크립트 리로딩 완료.");
						}
						break;
					}
					case "walker":
					{
						WalkingManager.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Walkers 리로딩 완료.");
						break;
					}
					case "recipe":
					{
						RecipeData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("레시피 리로딩 완료.");
						break;
					}
					case "htm":
					case "html":
					{
						if (st.hasMoreElements())
						{
							final String path = st.nextToken();
							final File file = new File(Config.DATAPACK_ROOT, "data/html/" + path);
							if (file.exists())
							{
								HtmCache.getInstance().reload(file);
								AdminData.getInstance().broadcastMessageToGMs("Htm File:" + file.getName() + ".");
							}
							else
							{
								BuilderUtil.sendSysMessage(activeChar, "File or Directory does not exist.");
							}
						}
						else
						{
							HtmCache.getInstance().reload();
							BuilderUtil.sendSysMessage(activeChar, "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
							AdminData.getInstance().broadcastMessageToGMs("htm 리로딩 완료.");
						}
						break;
					}
					case "multisell":
					{
						MultisellData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("멀티셀 리로딩 완료.");
						break;
					}
					case "buylist":
					{
						BuyListData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Buylists 리로딩 완료.");
						break;
					}
					case "teleport":
					{
						TeleporterData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Teleports 리로딩 완료.");
						break;
					}
					case "skill":
					{
						SkillData.getInstance().reload();
						AdminData.getInstance().broadcastMessageToGMs("Skills 리로딩 완료.");
						break;
					}
					case "item":
					{
						ItemTable.getInstance().reload();
						AdminData.getInstance().broadcastMessageToGMs("Items 리로딩 완료.");
						break;
					}
					case "door":
					{
						DoorData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Doors 리로딩 완료.");
						break;
					}
					case "zone":
					{
						ZoneManager.getInstance().reload();
						AdminData.getInstance().broadcastMessageToGMs("Zones 리로딩 완료.");
						break;
					}
					case "cw":
					{
						CursedWeaponsManager.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Cursed Weapons 리로딩 완료.");
						break;
					}
					case "crest":
					{
						CrestTable.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Crests 리로딩 완료.");
						break;
					}
					case "effect":
					{
						try
						{
							ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.EFFECT_MASTER_HANDLER_FILE);
							AdminData.getInstance().broadcastMessageToGMs("effect master handler.");
						}
						catch (Exception e3)
						{
							LOGGER.log(Level.WARNING, "Failed executing effect master handler!", e3);
							BuilderUtil.sendSysMessage(activeChar, "Error reloading effect master handler!");
						}
						break;
					}
					case "handler":
					{
						try
						{
							ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.MASTER_HANDLER_FILE);
							ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.EFFECT_MASTER_HANDLER_FILE);
							ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.SKILL_CONDITION_HANDLER_FILE);
							ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.CONDITION_HANDLER_FILE);
							ScriptEngineManager.getInstance().executeScript(ScriptEngineManager.ONE_DAY_REWARD_MASTER_HANDLER);
							AdminData.getInstance().broadcastMessageToGMs("handler 파일 리로딩 완료.");
						}
						catch (Exception e4)
						{
							LOGGER.log(Level.WARNING, "Failed executing master handler!", e4);
							BuilderUtil.sendSysMessage(activeChar, "Error reloading master handler!");
						}
						break;
					}
					case "enchant":
					{
						EnchantItemOptionsData.getInstance().load();
						EnchantItemGroupsData.getInstance().load();
						EnchantItemData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("강화시스템 리로딩 완료.");
						break;
					}
					case "lucky":
					{
						LuckyGameData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("타로카드 리로딩 완료.");
						break;
					}
					case "transform":
					{
						TransformData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("transform data 리로딩 완료.");
						break;
					}
					case "crystalizable":
					{
						ItemCrystallizationData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("item crystalization data 리로딩 완료.");
						break;
					}
					case "primeshop":
					{
						PrimeShopData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Prime Shop data 리로딩 완료.");
						break;
					}
					case "appearance":
					{
						AppearanceItemData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("appearance item data 리로딩 완료.");
						break;
					}
					case "sayune":
					{
						SayuneData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Sayune data 리로딩 완료.");
						break;
					}
					case "sets":
					{
						ArmorSetData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Armor sets data 리로딩 완료.");
						break;
					}
					case "options":
					{
						OptionData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Options data 리로딩 완료.");
						break;
					}
					case "fishing":
					{
						FishingData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Fishing data 리로딩 완료.");
						break;
					}
					case "attendance":
					{
						AttendanceRewardData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("출석보상 리로딩 완료.");
						break;
					}
					case "fakeplayers":
					{
						FakePlayerData.getInstance().load();
						for (WorldObject obj : World.getInstance().getVisibleObjects())
						{
							if (obj.isFakePlayer())
							{
								obj.broadcastInfo();
							}
						}
						AdminData.getInstance().broadcastMessageToGMs("Reloaded Fake Player data 리로딩 완료.");
						break;
					}
					case "fakeplayerchat":
					{
						FakePlayerChatManager.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Fake Player Chat data 리로딩 완료.");
						break;
					}
					case "localisations":
					{
						SystemMessageId.loadLocalisations();
						NpcStringId.loadLocalisations();
						SendMessageLocalisationData.getInstance().load();
						NpcNameLocalisationData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Localisation data 리로딩 완료.");
						break;
					}
					case "instance":
					{
						InstanceManager.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Instances data 리로딩 완료.");
						break;
					}
					case "combination":
					{
						CombinationItemsData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("Combination data 리로딩 완료.");
						break;
					}
					case "daily_reset":
					{
						activeChar.onPlayerEnter();
						EventResetCheck.resetDailyCheck();
						AdminData.getInstance().broadcastMessageToGMs("데일리 리셋을 실행했습니다.");
						break;
					}
					case "recom_reset":
					{
						EventResetCheck.resetRecomCheck();
						AdminData.getInstance().broadcastMessageToGMs("추천 시스템 리셋을 실행했습니다.");
						break;
					}
					case "reset":
					{
						activeChar.onPlayerEnter();
						DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::reset);
						DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::resetWeekly);
						DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::resetMonth);
						DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::resetMonthWeekly);
						AdminData.getInstance().broadcastMessageToGMs("미션 리셋을 실행했습니다.");
						break;
					}
					case "mission":
					{
						DailyMissionData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs("미션데이터를 리로드했습니다.");
						break;
					}
					case "variation":
					{
						VariationData.getInstance().load();
						AdminData.getInstance().broadcastMessageToGMs(activeChar.getName() + ": Reloaded Variation data.");
						break;
					}
					case "chardel":
					{
						int deleteDays = Config.AUTO_DELETE_CHAR_DAYS * 3600 * 24;
						if (Config.AUTO_DELETE_CHAR)
						{
							try (Connection con = DatabaseFactory.getConnection();
								Statement statement = con.createStatement())
							{
								int charCount = 0;
								int accountCount = 0;
								
								ResultSet resultSet = statement.executeQuery("SELECT charId FROM characters WHERE online = 0 AND ((UNIX_TIMESTAMP() - ( lastAccess/1000)) >= " + deleteDays + ");");
								List<Integer> charIds = new ArrayList<>();
								while (resultSet.next())
								{
									int charId = resultSet.getInt("charId");
									charIds.add(charId);
								}
								for (int charId : charIds)
								{
									final String player = CharInfoTable.getInstance().getNameById(charId);
									final int clanId = CharInfoTable.getInstance().getClanIdById(charId);
									final Clan clan = ClanTable.getInstance().getClan(clanId);
									
									if ((clan != null) && (player != null))
									{
										if (clan.getLeaderId() == charId)
										{
											ClanTable.getInstance().destroyClan(clanId);
										}
										else
										{
											clan.broadcastToOnlineMembers("혈맹원 " + player + " 캐릭터가 삭제되었습니다.");
											if (Config.ALT_CLAN_LEVEL_DOWN_FOR_MEMBERS)
											{
												clan.levelDownClan();
											}
											
											// Remove the Player From the Member list
											clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(player));
											clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
										}
									}
									// 이제 charId를 사용하여 원하는 작업을 수행할 수 있습니다.
									GameClient.deleteCharByObjId(charId);
									charCount++;
								}
								// charCount += statement.executeUpdate("DELETE FROM characters WHERE online = 0 AND characters.account_name IN (select login from accounts WHERE donate < 100) AND ((UNIX_TIMESTAMP() - ( lastAccess/1000)) >= " + deleteDays + ");");
								
								LOGGER.info(Config.AUTO_DELETE_CHAR_DAYS + "일간 미접속 캐릭터 " + charCount + "개와 계정 " + accountCount + "개를 데이터 베이스에서 정리하였습니다.");
							}
							catch (SQLException e)
							{
								LOGGER.severe("데이터베이스에서 개체 ID를 읽을 수 없습니다. 구성을 확인하십시오.");
							}
						}
						for (Player players : World.getInstance().getPlayers())
						{
							players.getVariables().set("DailyLuna", 0);
						}
						break;
					}
					default:
					{
						return;
					}
				}
			});
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
