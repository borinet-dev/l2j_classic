package ai.others.Taylor;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.data.xml.SendMessageLocalisationData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.SubclassInfoType;
import org.l2jmobius.gameserver.instancemanager.LunaManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.AbstractScript;
import org.l2jmobius.gameserver.model.events.CustomStats;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.Id;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.npc.OnNpcMenuSelect;
import org.l2jmobius.gameserver.model.holders.SubClassHolder;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.serverpackets.ExSubjobInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import org.l2jmobius.gameserver.network.serverpackets.HennaInfo;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;
import handlers.communityboard.borinet.SubClass;

public class Taylor extends AbstractNpcAI
{
	// NPC
	private static final int TAYLOR = 40021;
	
	private Taylor()
	{
		addStartNpc(TAYLOR);
		addFirstTalkId(TAYLOR);
		addTalkId(TAYLOR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case ("back"):
			{
				htmltext = getHtm(player, "40021.html");
				break;
			}
			case ("CHANGE_HUMAN"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.DUELIST + "\"><font color=LEVEL>듀얼리스트</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.DREADNOUGHT + "\"><font color=LEVEL>드레드노트</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.PHOENIX_KNIGHT + "\"><font color=LEVEL>피닉스 나이트</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.SAGITTARIUS + "\"><font color=LEVEL>사지타리우스</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.ADVENTURER + "\"><font color=LEVEL>어드벤쳐러</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.HELL_KNIGHT + "\"><font color=LEVEL>헬 나이트</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.ARCHMAGE + "\"><font color=LEVEL>아크메이지</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.SOULTAKER + "\"><font color=LEVEL>소울테이커</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.ARCANA_LORD + "\"><font color=LEVEL>아르카나 로드</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.CARDINAL + "\"><font color=LEVEL>카디날</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.HIEROPHANT + "\"><font color=LEVEL>하이로펀트</font></button>");
				htmltext = getHtm(player, "40021-1.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("CHANGE_ELF"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.EVA_TEMPLAR + "\"><font color=LEVEL>에바스 템플러</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.SWORD_MUSE + "\"><font color=LEVEL>소드뮤즈</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.WIND_RIDER + "\"><font color=LEVEL>윈드 라이더</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.MOONLIGHT_SENTINEL + "\"><font color=LEVEL>문라이트 센티넬</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.MYSTIC_MUSE + "\"><font color=LEVEL>미스틱 뮤즈</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.ELEMENTAL_MASTER + "\"><font color=LEVEL>엘레멘탈 마스터</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.EVA_SAINT + "\"><font color=LEVEL>에바스 세인트</font></button>");
				htmltext = getHtm(player, "40021-1.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("CHANGE_DELF"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.SHILLIEN_TEMPLAR + "\"><font color=LEVEL>실리엔 템플러</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.SPECTRAL_DANCER + "\"><font color=LEVEL>스펙트럴 댄서</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.GHOST_HUNTER + "\"><font color=LEVEL>고스트 헌터</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.GHOST_SENTINEL + "\"><font color=LEVEL>고스트 센티넬</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.STORM_SCREAMER + "\"><font color=LEVEL>스톰 스크리머</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.SPECTRAL_MASTER + "\"><font color=LEVEL>스펙트럴 마스터</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.SHILLIEN_SAINT + "\"><font color=LEVEL>실리엔 세인트</font></button>");
				htmltext = getHtm(player, "40021-1.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("CHANGE_ORC"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.TITAN + "\"><font color=LEVEL>타이탄</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.GRAND_KHAVATARI + "\"><font color=LEVEL>그랜드 카바타리</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.DOMINATOR + "\"><font color=LEVEL>도미네이터</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.DOOMCRYER + "\"><font color=LEVEL>둠크라이어</font></button>");
				htmltext = getHtm(player, "40021-1.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("CHANGE_DWARF"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.FORTUNE_SEEKER + "\"><font color=LEVEL>포츈 시커</font></button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Taylor CHANGE_" + ClassId.MAESTRO + "\"><font color=LEVEL>마에스트로</font></button>");
				htmltext = getHtm(player, "40021-1.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			default:
			{
				final ClassId classId = ClassId.valueOf(event.replace("CHANGE_", ""));
				if (classId != null)
				{
					final StringBuilder sb = new StringBuilder();
					sb.append("<Button ALIGN=LEFT ICON=NORMAL action=\"bypass -h menu_select?ask=1&reply=" + classId.getId() + "\">" + ClassListData.getInstance().getClass(classId.getId()).getClassName() + " 직업으로 변경한다" + "</Button>");
					htmltext = getHtm(player, "40021-2.html").replace("%CONFIRM_BUTTON%", sb.toString());
					htmltext = htmltext.replace("%CLASS_NAME%", ClassListData.getInstance().getClass(classId.getId()).getClassName());
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext;
		htmltext = getHtm(player, "40021.html");
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_NPC_MENU_SELECT)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(TAYLOR)
	public void onNpcMenuSelect(OnNpcMenuSelect event)
	{
		final Player player = event.getTalker();
		final int ask = event.getAsk();
		switch (ask)
		{
			case 1:
			{
				final int classId = event.getReply();
				if ((player.getLevel() < 76) || !player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
				{
					String msg = "76레벨이상 3차전직 캐릭터만 직업변경이 가능합니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.isSubClassActive())
				{
					String msg = "메인클래스 상태에서만 직업변경이 가능합니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.getClassId().getId() == classId)
				{
					String msg = "선택하신 직업은 현재 직업과 동일합니다. 다른 직업을 선택하세요.";
					showHtml(player, msg);
					return;
				}
				else if (player.isHero())
				{
					String msg = "영웅 캐릭터는 사용할 수 없습니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.isInSiege() || player.isInsideZone(ZoneId.SIEGE))
				{
					String msg = "공성중에는 직업을 변경할수 없습니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.isCursedWeaponEquipped() || (player.getReputation() < 0))
				{
					String msg = "카오틱 캐릭터는 직업을 변경할수 없습니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.isTransformed())
				{
					String msg = "변신상태에서는 변경할수 없습니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.isInDuel())
				{
					String msg = "전투중에는 직업을 변경할수 없습니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
				{
					String msg = "올림피아드 게임중에는 직업을 변경할수 없습니다.";
					showHtml(player, msg);
					return;
				}
				else if (Olympiad.getInstance().getRemainingWeeklyMatches(player.getObjectId()) < 30)
				{
					String msg = "올림피아드 게임에 참가한 캐릭터는 직업을 변경이 불가능하며, 다음 주에 가능합니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.isInInstance())
				{
					String msg = "인스턴트 던전 이용중에는 직업을 변경할수 없습니다.";
					showHtml(player, msg);
					return;
				}
				else if (player.getLuna() < 100)
				{
					String msg = "루나가 부족합니다.";
					showHtml(player, msg);
					return;
				}
				
				player.abortCast();
				player.stopAllEffects();
				player.stopCubics();
				player.setClassId(classId);
				player.setBaseClass(player.getActiveClass());
				
				LunaManager.getInstance().useLunaPoint(player, 100, "캐릭터 직업 변경");
				delsubskills(player);
				player.giveAvailableSkills(true, true, true);
				CustomStats.getInstance().subclassChange(player);
				player.sendPacket(new HennaInfo(player));
				player.setNoble(false);
				
				player.getServitorsAndPets().forEach(s -> s.unSummon(player));
				player.getEffectList().stopAllEffects(true);
				player.getInventory().getItems().forEach(item ->
				{
					if (item.isEquipped())
					{
						player.getInventory().unEquipItemInSlot(item.getLocationSlot());
					}
				});
				
				player.store(false);
				player.broadcastUserInfo();
				player.sendSkillList();
				player.decayMe();
				player.spawnMe(player.getX(), player.getY(), player.getZ());
				player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.CLASS_CHANGED));
				player.sendPacket(new ExUserInfoInvenWeight(player));
				player.sendPacket(new SocialAction(player.getObjectId(), 20));
				player.setBlockActions(true);
				Broadcast.toPlayerScreenMessage(player, "메인 클래스 변경을 완료하였습니다. 잠시 후 서버와 접속이 종료됩니다. 다시 접속해 주시기 바랍니다.");
				ThreadPool.schedule(new logOut(player), 10000);
				break;
			}
		}
	}
	
	private class logOut implements Runnable
	{
		private final Player _player;
		
		public logOut(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			Disconnection.of(_player).deleteMe().defaultSequence(new SystemMessage(SendMessageLocalisationData.getLocalisation(_player, "메인 클래스 변경 후 캐릭터는 다시 접속을 해야합니다.")));
		}
	}
	
	private void showHtml(Player player, String msg)
	{
		String html = getHtm(player, "40021-3.html");
		html = html.replace("%msg%", msg);
		player.sendPacket(new NpcHtmlMessage(html));
		player.sendMessage("" + msg);
	}
	
	private void delsubskills(Player player)
	{
		AbstractScript.takeItems(player, SubClass.SUB_CERTIFICATE, -1);
		player.getWarehouse().destroyItemByItemId("Quest", SubClass.SUB_CERTIFICATE, -1, player, null);
		for (SubClassHolder subclass : player.getSubClasses().values())
		{
			player.getVariables().remove(SubClass.SUB_CERTIFICATE_COUNT_VAR + subclass.getClassId());
		}
		
		final PlayerVariables vars = player.getVariables();
		for (int i = 1; i <= Config.MAX_SUBCLASS; i++)
		{
			for (int lv : SubClass.SUB_SKILL_LEVELS)
			{
				vars.remove("SubSkill-" + i + "-" + lv);
			}
		}
		SubClass.takeSkills(player, "SubSkillList");
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement deleteHennas = con.prepareStatement("DELETE FROM character_hennas WHERE charId=? AND class_index <> 0");
			PreparedStatement deleteShortcuts = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND class_index <> 0");
			PreparedStatement deleteSkillReuse = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=? AND class_index <> 0");
			PreparedStatement deleteSkills = con.prepareStatement("DELETE FROM character_skills WHERE charId=? AND class_index <> 0");
			PreparedStatement deleteSubclass = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=? AND class_index <> 0"))
		{
			// Remove all henna info stored for this sub-class.
			deleteHennas.setInt(1, player.getObjectId());
			deleteHennas.execute();
			
			// Remove all shortcuts info stored for this sub-class.
			deleteShortcuts.setInt(1, player.getObjectId());
			deleteShortcuts.execute();
			
			// Remove all effects info stored for this sub-class.
			deleteSkillReuse.setInt(1, player.getObjectId());
			deleteSkillReuse.execute();
			
			// Remove all skill info stored for this sub-class.
			deleteSkills.setInt(1, player.getObjectId());
			deleteSkills.execute();
			
			// Remove all basic info stored about this sub-class.
			deleteSubclass.setInt(1, player.getObjectId());
			deleteSubclass.execute();
			
			// 올림피아드 기록 삭제
			Olympiad.removeNobleStats(player);
			
			player.getSubClasses().remove(1);
			player.getSubClasses().remove(2);
			player.getSubClasses().remove(3);
			player.getSubClasses().remove(4);
		}
		catch (Exception e)
		{
		}
	}
	
	public static void main(String[] args)
	{
		new Taylor();
	}
}