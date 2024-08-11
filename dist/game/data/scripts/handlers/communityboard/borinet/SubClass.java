package handlers.communityboard.borinet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.AbstractScript;
import org.l2jmobius.gameserver.model.events.CustomStats;
import org.l2jmobius.gameserver.model.holders.SubClassHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.RequestAcquireSkill;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BorinetHtml;

public class SubClass implements IParseBoardHandler
{
	public static final int SUB_CERTIFICATE = 10280;
	public static final int[] SUB_SKILL_LEVELS =
	{
		65,
		70,
		75,
		80
	};
	public static final String SUB_CERTIFICATE_COUNT_VAR = "SUB_CERTIFICATE_COUNT";
	
	private static final EnumMap<ClassId, Set<ClassId>> subclassSetMap = new EnumMap<>(ClassId.class);
	private static final Set<ClassId> mainSubclassSet;
	private static final Set<ClassId> neverSubclassed = EnumSet.of(ClassId.OVERLORD, ClassId.WARSMITH);
	private static final Set<ClassId> subclasseSet1 = EnumSet.of(ClassId.DARK_AVENGER, ClassId.PALADIN, ClassId.TEMPLE_KNIGHT, ClassId.SHILLIEN_KNIGHT);
	private static final Set<ClassId> subclasseSet2 = EnumSet.of(ClassId.TREASURE_HUNTER, ClassId.ABYSS_WALKER, ClassId.PLAINS_WALKER);
	private static final Set<ClassId> subclasseSet3 = EnumSet.of(ClassId.HAWKEYE, ClassId.SILVER_RANGER, ClassId.PHANTOM_RANGER);
	private static final Set<ClassId> subclasseSet4 = EnumSet.of(ClassId.WARLOCK, ClassId.ELEMENTAL_SUMMONER, ClassId.PHANTOM_SUMMONER);
	private static final Set<ClassId> subclasseSet5 = EnumSet.of(ClassId.SORCERER, ClassId.SPELLSINGER, ClassId.SPELLHOWLER);
	
	static
	{
		final Set<ClassId> subclasses = CategoryData.getInstance().getCategoryByType(CategoryType.THIRD_CLASS_GROUP).stream().map(ClassId::getClassId).collect(Collectors.toSet());
		subclasses.removeAll(neverSubclassed);
		mainSubclassSet = subclasses;
		subclassSetMap.put(ClassId.DARK_AVENGER, subclasseSet1);
		subclassSetMap.put(ClassId.PALADIN, subclasseSet1);
		subclassSetMap.put(ClassId.TEMPLE_KNIGHT, subclasseSet1);
		subclassSetMap.put(ClassId.SHILLIEN_KNIGHT, subclasseSet1);
		subclassSetMap.put(ClassId.TREASURE_HUNTER, subclasseSet2);
		subclassSetMap.put(ClassId.ABYSS_WALKER, subclasseSet2);
		subclassSetMap.put(ClassId.PLAINS_WALKER, subclasseSet2);
		subclassSetMap.put(ClassId.HAWKEYE, subclasseSet3);
		subclassSetMap.put(ClassId.SILVER_RANGER, subclasseSet3);
		subclassSetMap.put(ClassId.PHANTOM_RANGER, subclasseSet3);
		subclassSetMap.put(ClassId.WARLOCK, subclasseSet4);
		subclassSetMap.put(ClassId.ELEMENTAL_SUMMONER, subclasseSet4);
		subclassSetMap.put(ClassId.PHANTOM_SUMMONER, subclasseSet4);
		subclassSetMap.put(ClassId.SORCERER, subclasseSet5);
		subclassSetMap.put(ClassId.SPELLSINGER, subclasseSet5);
		subclassSetMap.put(ClassId.SPELLHOWLER, subclasseSet5);
	}
	
	private static final String[] COMMANDS =
	{
		"_bbssubclass"
	};
	
	private static final String[] CUSTOM_COMMANDS =
	{
		"subClasspage",
		"_bbssub",
		"_changeSubPage",
		"Subclass",
		"Certifys",
		"Certificate",
		"learnSubSkill",
		"deleteSubSkill",
		"deleteSubSkill_ok"
	};
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		final List<String> commands = new ArrayList<>();
		commands.addAll(Arrays.asList(COMMANDS));
		commands.addAll(Arrays.asList(CUSTOM_COMMANDS));
		return commands.stream().filter(Objects::nonNull).toArray(String[]::new);
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, Player player)
	{
		if (player.isOnEvent())
		{
			player.sendMessage("이벤트 참가 중에는 서브클래스 메뉴를 이용할 수 없습니다.");
			return false;
		}
		else if (command.equals("_bbssubclass") || command.equals("subClasspage"))
		{
			BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
		}
		else if (command.startsWith("_bbssub:"))
		{
			final String path = command.replace("_bbssub:", "");
			if ((path.length() > 0) && path.endsWith(".htm"))
			{
				BorinetHtml.showHtml(player, "subclass/" + path, 0, "");
			}
		}
		else if (command.startsWith("Subclass"))
		{
			int jobLevel = 0;
			if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
			{
				jobLevel = 0;
			}
			else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
			{
				jobLevel = 1;
			}
			else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
			{
				jobLevel = 2;
			}
			else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
			{
				jobLevel = 3;
			}
			
			if (!canChangeClass(player))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
				return true;
			}
			
			int cmdChoice = 0;
			int paramOne = 0;
			int paramTwo = 0;
			try
			{
				cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				int endIndex = command.indexOf(' ', 11);//
				if (endIndex == -1)
				{
					endIndex = command.length();
				}
				
				if (command.length() > 11)
				{
					paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
					if (command.length() > endIndex)
					{
						paramTwo = Integer.parseInt(command.substring(endIndex).trim());
					}
				}
			}
			catch (Exception nfe)
			{
			}
			
			Set<ClassId> subsAvailable = null;
			switch (cmdChoice)
			{
				case 1: // Add Subclass - Initial
					if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
					{
						player.sendMessage("더이상 서브클래스를 추가할 수 없습니다.");
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						break;
					}
					if (player.getLevel() < 75)
					{
						player.sendMessage("레벨이 75이상이 되어야 새로운 서브클래스를 추가할 수 있습니다.");
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						break;
					}
					
					subsAvailable = getAvailableSubClasses(player);
					if ((subsAvailable != null) && !subsAvailable.isEmpty())
					{
						final StringBuilder content1 = new StringBuilder(200);
						int count = 0;
						for (ClassId subClass : subsAvailable)
						{
							count++;
							if (count == 1)
							{
								content1.append("<tr>");
							}
							content1.append("<td><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass Subclass 4 " + subClass.getId() + "\" msg=\"1268;" + ClassListData.getInstance().getClass(subClass.getId()).getClassName() + "\">" + ClassListData.getInstance().getClass(subClass.getId()).getClientCode() + "</Button></td>");
							if (count == 2)
							{
								content1.append("</tr>");
								count = 0;
							}
						}
						if ((count % 2) == 1)
						{
							content1.append("</tr>");
						}
						BorinetHtml.showHtml(player, "subclass/SubClass_Add.htm", 0, content1.toString());
					}
					else
					{
						if (jobLevel < 2)
						{
							player.sendMessage("2차전직 후 이용가능합니다.");
							BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
							return true;
						}
						if ((player.getOriginRace() == Race.ELF) || (player.getOriginRace() == Race.DARK_ELF))
						{
							player.sendMessage("엘프와 다크엘프간의 서브클래스 추가는 불가능합니다.");
							BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						}
						else if (player.getOriginRace() == Race.KAMAEL)
						{
							player.sendMessage("카마엘 종족이 추가할 수 없는 서브클래스입니다.");
							BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						}
						else
						{
							player.sendMessage("현재 서브클래스 변경을 할 수 없습니다.");
							BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						}
						return true;
					}
					break;
				case 2: // Change Class - Initial
					if (player.getSubClasses().isEmpty())
					{
						player.sendMessage("현재 서브클래스가 존재하지 않습니다.");
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
					}
					else
					{
						final StringBuilder content2 = new StringBuilder(200);
						if (checkVillageMaster(player.getBaseClass()))
						{
							content2.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass Subclass 5 0\">" + ClassListData.getInstance().getClass(player.getBaseClass()).getClientCode() + " - 메인 클래스</Button><br>");
						}
						
						for (Iterator<SubClassHolder> subList = iterSubClasses(player); subList.hasNext();)
						{
							final SubClassHolder subClass = subList.next();
							if (checkVillageMaster(subClass.getClassDefinition()))
							{
								content2.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass Subclass 5 " + subClass.getClassIndex() + "\">" + ClassListData.getInstance().getClass(subClass.getClassId()).getClientCode() + "</Button><br>");
							}
						}
						
						if (content2.length() > 0)
						{
							BorinetHtml.showHtml(player, "subclass/SubClass_Change.htm", 0, content2.toString());
						}
						else
						{
							player.sendMessage("서브클래스를 찾을 수 없습니다. 다시 시도하시거나 운영자에게 문의하시기 바랍니다.");
							BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						}
					}
					break;
				case 3: // Change/Cancel Subclass - Initial
					if ((player.getSubClasses() == null) || player.getSubClasses().isEmpty())
					{
						player.sendMessage("현재 서브클래스가 존재하지 않습니다.");
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						break;
					}
					
					/*
					 * if (player.isSubClassActive()) { player.sendMessage("서브클래스 취소를 하기위해서는 메인클래스 상태로 전환하세요."); BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, ""); break; }
					 */
					// custom value
					final StringBuilder content3 = new StringBuilder(200);
					for (Iterator<SubClassHolder> subList = iterSubClasses(player); subList.hasNext();)
					{
						final SubClassHolder subClass = subList.next();
						content3.append("<br><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass Subclass 6 " + subClass.getClassIndex() + "\">" + ClassListData.getInstance().getClass(subClass.getClassId()).getClientCode() + "</Button><br>");
					}
					BorinetHtml.showHtml(player, "subclass/SubClass_3.htm", 0, content3.toString());
					break;
				case 4: // Add Subclass - Action (Subclass 4 x[x])
					/**
					 * If the character is less than level 75 on any of their previously chosen classes then disallow them to change to their most recently added sub-class choice.
					 */
					if (!player.getClient().getFloodProtectors().canChangeSubclass())
					{
						return true;
					}
					
					boolean allowAddition = true;
					if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
					{
						allowAddition = false;
					}
					
					if (player.getLevel() < 75)
					{
						player.sendMessage("레벨이 75이상이 되어야 새로운 서브클래스를 추가할 수 있습니다.");
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						allowAddition = false;
						break;
					}
					
					if (allowAddition && !player.getSubClasses().isEmpty())
					{
						for (Iterator<SubClassHolder> subList = iterSubClasses(player); subList.hasNext();)
						{
							final SubClassHolder subClass = subList.next();
							if (subClass.getLevel() < 75)
							{
								player.sendMessage("서브클래스 레벨이 75이상이 되어야 새로운 서브클래스를 추가할 수 있습니다.");
								BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
								allowAddition = false;
								break;
							}
						}
					}
					
					/**
					 * If quest checking is enabled, verify if the character has completed the Mimir's Elixir (Path to Subclass) and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items. If they both exist, remove both unique items and continue with adding
					 * the sub-class.
					 */
					if (allowAddition && !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
					{
						allowAddition = checkQuests(player);
					}
					
					if (allowAddition && isValidNewSubClass(player, paramOne))
					{
						if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1, false))
						{
							return true;
						}
						
						player.setActiveClass(player.getTotalSubClasses());
						
						CustomStats.getInstance().subclassChange(player);
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ACHIEVED_THE_SECOND_CLASS_S1_CONGRATS).addClassId(player.getClassId().getId()));
						
						// final SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_ACHIEVED_THE_SECOND_CLASS_S1_CONGRATS);
						// msg.addClassId(player.getClassId().getId());
						// player.sendPacket(msg);
					}
					else
					{
						player.sendMessage("현재 더이상 서브클래스를 추가할 수 없습니다.");
						player.sendPacket(new ShowBoard());
					}
					break;
				case 5: // Change Class - Action
					/**
					 * If the character is less than level 75 on any of their previously chosen classes then disallow them to change to their most recently added sub-class choice. Note: paramOne = classIndex
					 */
					if (!player.getClient().getFloodProtectors().canChangeSubclass())
					{
						return true;
					}
					
					if (player.getClassIndex() == paramOne)
					{
						player.sendMessage("현재 서브클래스를 선택했습니다. 다른 클래스를 선택하세요.");
						BorinetHtml.showHtml(player, "subclass/SubClass_Change.htm", 0, "");
						break;
					}
					
					if (paramOne == 0)
					{
						if (!checkVillageMaster(player.getBaseClass()))
						{
							return true;
						}
					}
					else
					{
						try
						{
							if (!checkVillageMaster(player.getSubClasses().get(paramOne).getClassDefinition()))
							{
								return true;
							}
						}
						catch (NullPointerException e)
						{
							return true;
						}
					}
					
					player.setActiveClass(paramOne);
					CustomStats.getInstance().subclassChange(player);
					
					// TODO: Retail message YOU_HAVE_SUCCESSFULLY_SWITCHED_S1_TO_S2
					player.sendMessage("서브클래스 변경을 완료하였습니다.");
					// BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
					player.sendPacket(new ShowBoard());
					return true;
				case 6: // Change/Cancel Subclass - Choice
					if ((paramOne < 1) || (paramOne > Config.MAX_SUBCLASS))
					{
						return true;
					}
					/*
					 * if (player.isSubClassActive()) { player.sendMessage("메인클래스 상태에서 시도하시기 바랍니다."); BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, ""); break; }
					 */
					
					subsAvailable = getAvailableSubClasses(player);
					if ((subsAvailable == null) || subsAvailable.isEmpty())
					{
						// TODO: Retail message
						player.sendMessage("지금은 불가능한 행동입니다.");
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						return true;
					}
					final StringBuilder content6 = new StringBuilder(200);
					int count = 0;
					for (ClassId subClass : subsAvailable)
					{
						count++;
						if (count == 1)
						{
							content6.append("<tr>");
						}
						content6.append("<td><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass Subclass 7 " + paramOne + " " + subClass.getId() + "\" msg=\"1445;\">" + ClassListData.getInstance().getClass(subClass.getId()).getClientCode() + "</Button></td>");
						if (count == 2)
						{
							content6.append("</tr>");
							count = 0;
						}
					}
					if ((count % 2) == 1)
					{
						content6.append("</tr>");
					}
					
					switch (paramOne)
					{
						case 1:
						case 2:
						case 3:
						default:
							BorinetHtml.showHtml(player, "subclass/SubClass_ModifyChoice.htm", 0, "");
					}
					BorinetHtml.showHtml(player, "subclass/SubClass_ModifyChoice.htm", 0, content6.toString());
					break;
				case 7: // Change Subclass - Action
					/**
					 * Warning: the information about this subclass will be removed from the subclass list even if false!
					 */
					if (!player.isInsideZone(ZoneId.PEACE))
					{
						if (player.getAdena() < 1000000)
						{
							BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
							player.sendMessage("아데나가 부족하여 서브 간 전환을 할 수 없습니다.");
							return true;
						}
						AbstractScript.takeItems(player, Inventory.ADENA_ID, 1000000);
					}
					if (!player.getClient().getFloodProtectors().canChangeSubclass())
					{
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						return true;
					}
					
					if (!isValidNewSubClass(player, paramTwo))
					{
						BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
						return true;
					}
					
					if (player.modifySubClass(paramOne, paramTwo, false))
					{
						player.abortCast();
						player.stopAllEffectsExceptThoseThatLastThroughDeath(); // all effects from old subclass stopped!
						player.stopAllEffects();
						player.stopCubics();
						player.setActiveClass(paramOne);
						CustomStats.getInstance().subclassChange(player);
						
						player.sendPacket(new ShowBoard());
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ACHIEVED_THE_SECOND_CLASS_S1_CONGRATS).addClassId(player.getClassId().getId()));
					}
					else
					{
						player.setActiveClass(0);
						player.sendMessage("서브클래스를 추가할 수 없습니다. 메인 클래스로 전환됩니다.");
						player.sendPacket(new ShowBoard());
						return true;
					}
					break;
			}
		}
		else if (command.equals("Certificate"))
		{
			if (player.isMainClassActive())
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_Certificate_main.htm", 0, "");
			}
			else
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_Certificate.htm", 0, "");
			}
		}
		else if (command.startsWith("Certifys"))
		{
			if (player.isMainClassActive())
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_Certificate_main.htm", 0, "");
			}
			
			if (!canChangeClass(player))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
			}
			
			int cmdChoice = 0;
			try
			{
				cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				int endIndex = command.indexOf(' ', 11);//
				if (endIndex == -1)
				{
					endIndex = command.length();
				}
			}
			catch (Exception nfe)
			{
			}
			
			final int level = SUB_SKILL_LEVELS[cmdChoice];
			if (player.getLevel() < level)
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_Certificate_low.htm", level, "");
			}
			else if (player.getVariables().hasVariable(getSubSkillVariableName(player, level)))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_Certificate_already.htm", level, "");
			}
			else
			{
				final PlayerVariables vars = player.getVariables();
				if ((player.getLevel() < level) || vars.hasVariable(getSubSkillVariableName(player, level)))
				{
					BorinetHtml.showHtml(player, "subclass/SubClass_Certificate_low.htm", level, "");
				}
				else
				{
					final int subId = player.getClassId().getId();
					final int currentCount = player.getVariables().getInt(SUB_CERTIFICATE_COUNT_VAR + subId, 0);
					player.getVariables().set(SUB_CERTIFICATE_COUNT_VAR + subId, currentCount + 1);
					vars.set(getSubSkillVariableName(player, level), true);
					AbstractScript.giveItems(player, SUB_CERTIFICATE, 1);
					BorinetHtml.showHtml(player, "subclass/SubClass_Certificate_ok.htm", level, "");
				}
			}
		}
		else if (command.equals("learnSubSkill"))
		{
			if (!canChangeClass(player))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
			}
			
			if (player.isSubClassActive())
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_sub.htm", 0, "");
			}
			else
			{
				final Npc npc = player.getLastFolkNPC();
				if (npc == null)
				{
					BorinetHtml.showHtml(player, "subclass/SubClass_skills_time.htm", 0, "");
				}
				else
				{
					BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
					RequestAcquireSkill.showSubSkillList(player);
				}
			}
		}
		else if (command.equals("deleteSubSkill"))
		{
			if (!canChangeClass(player))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
			}
			
			if (player.isSubClassActive())
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_sub.htm", 0, "");
			}
			else if (!hasSubCertificate(player))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_del_no_skill.htm", 0, "");
			}
			else
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_del.htm", 0, "");
			}
		}
		else if (command.equals("deleteSubSkill_ok"))
		{
			if (!canChangeClass(player))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
			}
			
			if (player.isSubClassActive())
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_sub.htm", 0, "");
			}
			else if (player.getAdena() < Config.FEE_DELETE_SUBCLASS_SKILLS)
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_del_no_adena.htm", 0, "");
			}
			else if (!hasSubCertificate(player))
			{
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_del_no_skill.htm", 0, "");
			}
			else
			{
				AbstractScript.takeItems(player, SUB_CERTIFICATE, -1);
				player.getWarehouse().destroyItemByItemId("Quest", SUB_CERTIFICATE, -1, player, null);
				AbstractScript.takeItems(player, Inventory.ADENA_ID, Config.FEE_DELETE_SUBCLASS_SKILLS);
				for (SubClassHolder subclass : player.getSubClasses().values())
				{
					player.getVariables().remove(SUB_CERTIFICATE_COUNT_VAR + subclass.getClassId());
				}
				
				final PlayerVariables vars = player.getVariables();
				for (int i = 1; i <= Config.MAX_SUBCLASS; i++)
				{
					for (int lv : SUB_SKILL_LEVELS)
					{
						vars.remove("SubSkill-" + i + "-" + lv);
					}
				}
				takeSkills(player, "SubSkillList");
				BorinetHtml.showHtml(player, "subclass/SubClass_skills_del_ok.htm", 0, "");
			}
		}
		else
		{
			BorinetHtml.showHtml(player, "subclass/SubClass.htm", 0, "");
		}
		return true;
	}
	
	public final static void takeSkills(Player player, String type)
	{
		final PlayerVariables vars = player.getVariables();
		final String list = vars.getString(type, "");
		if (!list.isEmpty())
		{
			final String[] skills = list.split(";");
			for (String skill : skills)
			{
				final String[] str = skill.split("-");
				final Skill sk = SkillData.getInstance().getSkill(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
				player.removeSkill(sk);
			}
			vars.remove(type);
			player.sendSkillList();
		}
	}
	
	private final boolean hasSubCertificate(Player player)
	{
		final PlayerVariables vars = player.getVariables();
		final Set<Integer> subs = player.getSubClasses().keySet();
		for (int index : subs)
		{
			for (int lv : SUB_SKILL_LEVELS)
			{
				if (vars.hasVariable("SubSkill-" + index + "-" + lv))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private final String getSubSkillVariableName(Player player, int level)
	{
		return "SubSkill-" + player.getClassIndex() + "-" + level;
	}
	
	private static boolean canChangeClass(Player player)
	{
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("올림피아드에 참가 중에는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		if ((player.getPet() != null) || player.hasSummon())
		{
			player.sendPacket(SystemMessageId.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_IS_SUMMONED);
			return false;
		}
		if (player.isCastingNow() || player.isAllSkillsDisabled())
		{
			player.sendPacket(SystemMessageId.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
			return false;
		}
		if (player.isTransformed())
		{
			player.sendMessage("변신 상태에서는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		if (!player.isInventoryUnder90(true))
		{
			player.sendPacket(SystemMessageId.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
			return false;
		}
		if (player.getWeightPenalty() >= 2)
		{
			player.sendPacket(SystemMessageId.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
			return false;
		}
		if (OlympiadManager.getInstance().isRegisteredInComp(player))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASSES_AND_DUEL_CLASSES_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addString(player.getName());
			player.sendPacket(sm);
			return false;
		}
		if (player.isSitting())
		{
			player.sendMessage("앉은 상태에서는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		if (player.isDead())
		{
			player.sendMessage("사망 상태에서는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		boolean isCaptchaActive = player.getQuickVarB("IsCaptchaActive", false);
		if (isCaptchaActive)
		{
			player.sendMessage("보안문자 입력 중 에는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		if (player.isInCombat() || player.isInDuel())
		{
			player.sendMessage("전투 상태에서는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		if (player.isInInstance())
		{
			player.sendMessage("인스턴트 던전에 입장한 상태에서는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		if (player.isInStoreMode())
		{
			player.sendMessage("상점모드 상태에서는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		if (player.getAutoPlay())
		{
			player.sendMessage("자동사냥 상태에서는 서브 클래스를 만들거나 변경할 수 없습니다.");
			return false;
		}
		
		return true;
	}
	
	private final Set<ClassId> getAvailableSubClasses(Player player)
	{
		// get player base class
		final int currentBaseId = player.getBaseClass();
		final ClassId baseCID = ClassId.getClassId(currentBaseId);
		
		// we need 2nd occupation ID
		final int baseClassId;
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().getId();
		}
		else
		{
			baseClassId = currentBaseId;
		}
		
		final Set<ClassId> availSubs = getSubclasses(player, baseClassId);
		if ((availSubs != null) && !availSubs.isEmpty())
		{
			for (Iterator<ClassId> availSub = availSubs.iterator(); availSub.hasNext();)
			{
				final ClassId pclass = availSub.next();
				
				// check for the village master
				if (!checkVillageMaster(pclass))
				{
					availSub.remove();
					continue;
				}
				
				// scan for already used subclasses
				final int availClassId = pclass.getId();
				final ClassId cid = ClassId.getClassId(availClassId);
				SubClassHolder prevSubClass;
				ClassId subClassId;
				for (Iterator<SubClassHolder> subList = iterSubClasses(player); subList.hasNext();)
				{
					prevSubClass = subList.next();
					subClassId = ClassId.getClassId(prevSubClass.getClassId());
					if (subClassId.equalsOrChildOf(cid))
					{
						availSub.remove();
						break;
					}
				}
			}
		}
		
		return availSubs;
	}
	
	protected boolean checkQuests(Player player)
	{
		// Noble players can add Sub-Classes without quests
		if (player.isNoble())
		{
			return true;
		}
		
		QuestState qs = player.getQuestState("Q00234_FatesWhisper"); // TODO: Not added with Saviors.
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		qs = player.getQuestState("Q00235_MimirsElixir"); // TODO: Not added with Saviors.
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		return true;
	}
	
	public final Set<ClassId> getSubclasses(Player player, int classId)
	{
		Set<ClassId> subclasses = null;
		final ClassId pClass = ClassId.getClassId(classId);
		if (CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, classId) || (CategoryData.getInstance().isInCategory(CategoryType.FOURTH_CLASS_GROUP, classId)))
		{
			subclasses = EnumSet.copyOf(mainSubclassSet);
			subclasses.remove(pClass);
			
			if (player.getOriginRace() == Race.KAMAEL)
			{
				for (ClassId cid : ClassId.values())
				{
					if (cid.getRace() != Race.KAMAEL)
					{
						subclasses.remove(cid);
					}
				}
				
				if (player.getAppearance().isFemale())
				{
					subclasses.remove(ClassId.MALE_SOULBREAKER);
				}
				else
				{
					subclasses.remove(ClassId.FEMALE_SOULBREAKER);
				}
				
				if (!player.getSubClasses().containsKey(2) || (player.getSubClasses().get(2).getLevel() < 75))
				{
					subclasses.remove(ClassId.INSPECTOR);
				}
			}
			else
			{
				if (player.getOriginRace() == Race.ELF)
				{
					for (ClassId cid : ClassId.values())
					{
						if (cid.getRace() == Race.DARK_ELF)
						{
							subclasses.remove(cid);
						}
					}
				}
				else if (player.getOriginRace() == Race.DARK_ELF)
				{
					for (ClassId cid : ClassId.values())
					{
						if (cid.getRace() == Race.ELF)
						{
							subclasses.remove(cid);
						}
					}
				}
				
				for (ClassId cid : ClassId.values())
				{
					if (cid.getRace() == Race.KAMAEL)
					{
						subclasses.remove(cid);
					}
				}
				
				for (ClassId cid : ClassId.values())
				{
					if (cid.getRace() == Race.ERTHEIA)
					{
						subclasses.remove(cid);
					}
				}
			}
			
			final Set<ClassId> unavailableClasses = subclassSetMap.get(pClass);
			if (unavailableClasses != null)
			{
				subclasses.removeAll(unavailableClasses);
			}
		}
		
		if (subclasses != null)
		{
			final ClassId currClassId = player.getClassId();
			for (ClassId tempClass : subclasses)
			{
				if (currClassId.equalsOrChildOf(tempClass))
				{
					subclasses.remove(tempClass);
				}
			}
		}
		return subclasses;
	}
	
	private final boolean isValidNewSubClass(Player player, int classId)
	{
		if (!checkVillageMaster(classId))
		{
			return false;
		}
		
		final ClassId cid = ClassId.getClassId(classId);
		SubClassHolder sub;
		ClassId subClassId;
		for (Iterator<SubClassHolder> subList = iterSubClasses(player); subList.hasNext();)
		{
			sub = subList.next();
			subClassId = ClassId.getClassId(sub.getClassId());
			if (subClassId.equalsOrChildOf(cid))
			{
				return false;
			}
		}
		
		// get player base class
		final int currentBaseId = player.getBaseClass();
		final ClassId baseCID = ClassId.getClassId(currentBaseId);
		
		// we need 2nd occupation ID
		final int baseClassId;
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().getId();
		}
		else
		{
			baseClassId = currentBaseId;
		}
		
		final Set<ClassId> availSubs = getSubclasses(player, baseClassId);
		if ((availSubs == null) || availSubs.isEmpty())
		{
			return false;
		}
		
		boolean found = false;
		for (ClassId pclass : availSubs)
		{
			if (pclass.getId() == classId)
			{
				found = true;
				break;
			}
		}
		return found;
	}
	
	protected boolean checkVillageMasterRace(ClassId pClass)
	{
		return true;
	}
	
	protected boolean checkVillageMasterTeachType(ClassId pClass)
	{
		return true;
	}
	
	public boolean checkVillageMaster(int classId)
	{
		return checkVillageMaster(ClassId.getClassId(classId));
	}
	
	public boolean checkVillageMaster(ClassId pclass)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE)
		{
			return true;
		}
		return checkVillageMasterRace(pclass) && checkVillageMasterTeachType(pclass);
	}
	
	private static Iterator<SubClassHolder> iterSubClasses(Player player)
	{
		return player.getSubClasses().values().iterator();
	}
	
}