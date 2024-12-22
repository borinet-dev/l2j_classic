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
package ai.bosses.Baium;

import java.util.List;
import java.util.stream.Collectors;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.MountType;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.events.EnterRaidCheck;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.model.variables.NpcVariables;
import org.l2jmobius.gameserver.model.zone.type.NoRestartZone;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.Earthquake;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

/**
 * Baium AI.
 * @author St3eT
 */
public class Baium extends AbstractNpcAI
{
	// NPCs
	private static final int BAIUM = 29020; // Baium
	private static final int BAIUM_STONE = 29025; // Baium
	private static final int ARCHANGEL = 29021; // Archangel
	private static final int TELE_CUBE = 31842; // Teleportation Cubic
	private static final int MIN_PEOPLE = Config.BAIUM_MIN_MEMBER;
	// Skills
	private static final SkillHolder BAIUM_ATTACK = new SkillHolder(4127, 1); // Baium: General Attack
	private static final SkillHolder ENERGY_WAVE = new SkillHolder(4128, 1); // Wind Of Force
	private static final SkillHolder EARTH_QUAKE = new SkillHolder(4129, 1); // Earthquake
	private static final SkillHolder THUNDERBOLT = new SkillHolder(4130, 1); // Striking of Thunderbolt
	private static final SkillHolder GROUP_HOLD = new SkillHolder(4131, 1); // Stun
	private static final SkillHolder SPEAR_ATTACK = new SkillHolder(4132, 1); // Spear: Pound the Ground
	private static final SkillHolder ANGEL_HEAL = new SkillHolder(4133, 1); // Angel Heal
	private static final SkillHolder HEAL_OF_BAIUM = new SkillHolder(4135, 1); // Baium Heal
	private static final SkillHolder BAIUM_PRESENT = new SkillHolder(4136, 1); // Baium's Gift
	private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1); // Hinder Strider
	// Zone
	private static final NoRestartZone zone = ZoneManager.getInstance().getZoneById(70051, NoRestartZone.class); // Baium zone
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int IN_FIGHT = 2;
	private static final int DEAD = 3;
	// Locations
	private static final Location BAIUM_GIFT_LOC = new Location(115910, 17337, 10105);
	private static final Location BAIUM_LOC = new Location(116020, 17440, 10112, 41740);
	private static final Location TELEPORT_CUBIC_LOC = new Location(115203, 16620, 10078);
	private static final Location TELEPORT_IN_LOC = new Location(114077, 15882, 10078);
	private static final Location[] ARCHANGEL_LOC =
	{
		new Location(115792, 16608, 10136, 0),
		new Location(115168, 17200, 10136, 0),
		new Location(115780, 15564, 10136, 13620),
		new Location(114880, 16236, 10136, 5400),
		new Location(114239, 17168, 10136, -1992)
	};
	// Misc
	private GrandBoss _baium = null;
	private static long _lastAttack = 0;
	private static Player _standbyPlayer = null;
	
	private Baium()
	{
		addTalkId(TELE_CUBE, BAIUM_STONE);
		addStartNpc(TELE_CUBE, BAIUM_STONE);
		addAttackId(BAIUM, ARCHANGEL);
		addKillId(BAIUM);
		addCreatureSeeId(BAIUM);
		addSpellFinishedId(BAIUM);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(BAIUM);
		
		bossDelete();
		switch (getStatus())
		{
			case WAITING:
			{
				setStatus(ALIVE);
				// fallthrough
			}
			case ALIVE:
			{
				addSpawn(BAIUM_STONE, BAIUM_LOC, false, 0);
				break;
			}
			case IN_FIGHT:
			{
				_baium = (GrandBoss) addSpawn(BAIUM, BAIUM_LOC, false, 0);
				_lastAttack = System.currentTimeMillis();
				addBoss(_baium);
				
				for (Location loc : ARCHANGEL_LOC)
				{
					final Npc archangel = addSpawn(ARCHANGEL, loc, false, 0, true);
					startQuestTimer("SELECT_TARGET", 5000, archangel, null);
				}
				startQuestTimer("CHECK_ATTACK", 60000, _baium, null);
				break;
			}
			case DEAD:
			{
				final long remain = info.getLong("respawn_time") - System.currentTimeMillis();
				if (remain > 0)
				{
					startQuestTimer("CLEAR_STATUS", remain, null, null);
				}
				else
				{
					notifyEvent("CLEAR_STATUS", null, null);
				}
				break;
			}
		}
	}
	
	public void bossDelete()
	{
		for (Creature creature : zone.getCharactersInside())
		{
			if (creature != null)
			{
				if (creature.isNpc())
				{
					creature.deleteMe();
				}
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "31862-04.html":
			{
				return event;
			}
			case "enter":
			{
				String htmltext = null;
				if (getStatus() == DEAD)
				{
					Broadcast.toPlayerScreenMessageS(player, "현재 바이움이 사망한 상태이므로 이동할 수 없습니다.");
				}
				else if (getStatus() == IN_FIGHT)
				{
					Broadcast.toPlayerScreenMessageS(player, "현재 레이드가 진행 중 입니다. 입장할 수 없습니다.");
				}
				else if (player.getParty() == null)
				{
					Broadcast.toPlayerScreenMessageS(player, "현재 파티상태가 아니므로 입장할 수 없습니다.");
				}
				else if (player.isInParty())
				{
					final Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<Player> members = isInCC ? party.getCommandChannel().getMembers() : party.getMembers();
					final boolean isPartyLeader = party.isLeader(player);
					
					if (!isInCC)
					{
						if (!isPartyLeader)
						{
							Broadcast.toPlayerScreenMessageS(player, "파티장만이 입장시도를 할 수 있습니다.");
							break;
						}
					}
					else
					{
						final boolean isCCLeader = party.getCommandChannel().isLeader(player);
						if (!isCCLeader)
						{
							Broadcast.toPlayerScreenMessageS(player, "연합 파티장만이 입장시도를 할 수 있습니다.");
							break;
						}
					}
					if (members.size() < MIN_PEOPLE)
					{
						Broadcast.toPlayerScreenMessageS(player, "입장가능 최소인원은 " + MIN_PEOPLE + "명 입니다.");
					}
					else
					{
						if (EnterRaidCheck.ConditionCheck(player, isInCC, members))
						{
							for (Player member : members)
							{
								member.teleToLocation(TELEPORT_IN_LOC);
								member.sendMessage("바이움 레이드 존으로 이동하였습니다.");
							}
							
							String leaderName = isInCC ? party.getCommandChannel().getLeader().getName() : party.getLeader().getName();
							String memberNames = members.stream().map(Player::getName).collect(Collectors.joining(";"));
							EnterRaidCheck.enterMessage(player, isInCC, "바이움", leaderName, memberNames, false);
						}
						if (getStatus() != IN_FIGHT)
						{
							if (getStatus() != WAITING)
							{
								setStatus(WAITING);
							}
						}
					}
				}
				return htmltext;
			}
			case "teleportOut":
			{
				player.teleToLocation(81929 + getRandom(Rnd.get(1, 300)), 149309 + getRandom(Rnd.get(-100, 100)), -3464);
				break;
			}
			case "wakeUp":
			{
				setStatus(IN_FIGHT);
				_baium = (GrandBoss) addSpawn(BAIUM, BAIUM_LOC, false, 0);
				_baium.disableCoreAI(true);
				_baium.setRandomWalking(false);
				addBoss(_baium);
				_lastAttack = System.currentTimeMillis();
				startQuestTimer("WAKEUP_ACTION", 50, _baium, null);
				startQuestTimer("MANAGE_EARTHQUAKE", 2000, _baium, player);
				startQuestTimer("CHECK_ATTACK", 60000, _baium, null);
				npc.deleteMe();
				break;
			}
			case "WAKEUP_ACTION":
			{
				if (npc != null)
				{
					zone.broadcastPacket(new SocialAction(_baium.getObjectId(), 2));
				}
				break;
			}
			case "MANAGE_EARTHQUAKE":
			{
				if (npc != null)
				{
					zone.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 40, 10));
					zone.broadcastPacket(new PlaySound("BS02_A"));
					startQuestTimer("SOCIAL_ACTION", 8000, npc, player);
				}
				break;
			}
			case "SOCIAL_ACTION":
			{
				if (npc != null)
				{
					zone.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
					startQuestTimer("PLAYER_PORT", 6000, npc, player);
				}
				break;
			}
			case "PLAYER_PORT":
			{
				if (npc != null)
				{
					if ((player != null) && player.isInsideRadius3D(npc, 16000))
					{
						player.teleToLocation(BAIUM_GIFT_LOC);
						startQuestTimer("PLAYER_KILL", 3000, npc, player);
					}
					else if ((_standbyPlayer != null) && _standbyPlayer.isInsideRadius3D(npc, 16000))
					{
						_standbyPlayer.teleToLocation(BAIUM_GIFT_LOC);
						startQuestTimer("PLAYER_KILL", 3000, npc, _standbyPlayer);
					}
				}
				break;
			}
			case "PLAYER_KILL":
			{
				if ((player != null) && player.isInsideRadius3D(npc, 16000))
				{
					zone.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
					npc.broadcastSay(ChatType.NPC_GENERAL, "나의 잠을 깨우다니! 죽어라!");
					npc.setTarget(player);
					npc.doCast(BAIUM_PRESENT.getSkill());
				}
				
				for (Player players : zone.getPlayersInside())
				{
					if (players.isHero())
					{
						zone.broadcastPacket(new ExShowScreenMessage(NpcStringId.NOT_EVEN_THE_GODS_THEMSELVES_COULD_TOUCH_ME_BUT_YOU_S1_YOU_DARE_CHALLENGE_ME_IGNORANT_MORTAL, 2, 4000, players.getName()));
						break;
					}
				}
				startQuestTimer("SPAWN_ARCHANGEL", 8000, npc, null);
				break;
			}
			case "SPAWN_ARCHANGEL":
			{
				_baium.disableCoreAI(false);
				_baium.setRandomWalking(true);
				
				for (Location loc : ARCHANGEL_LOC)
				{
					final Npc archangel = addSpawn(ARCHANGEL, loc, false, 0, true);
					startQuestTimer("SELECT_TARGET", 5000, archangel, null);
				}
				
				if ((player != null) && !player.isDead())
				{
					addAttackPlayerDesire(npc, player);
				}
				else if ((_standbyPlayer != null) && !_standbyPlayer.isDead())
				{
					addAttackPlayerDesire(npc, _standbyPlayer);
				}
				else
				{
					for (Player creature : World.getInstance().getVisibleObjectsInRange(npc, Player.class, 2000))
					{
						if (zone.isInsideZone(creature) && !creature.isDead())
						{
							addAttackPlayerDesire(npc, creature);
							break;
						}
					}
				}
				break;
			}
			case "SELECT_TARGET":
			{
				if (npc != null)
				{
					final Attackable mob = (Attackable) npc;
					final Creature mostHated = mob.getMostHated();
					if ((_baium == null) || _baium.isDead())
					{
						mob.deleteMe();
						break;
					}
					
					if ((mostHated != null) && mostHated.isPlayer() && zone.isInsideZone(mostHated))
					{
						if (mob.getTarget() != mostHated)
						{
							mob.clearAggroList();
						}
						addAttackPlayerDesire(mob, (Playable) mostHated);
					}
					else
					{
						boolean found = false;
						for (Playable creature : World.getInstance().getVisibleObjectsInRange(mob, Playable.class, 1000))
						{
							if (zone.isInsideZone(creature) && !creature.isDead())
							{
								if (mob.getTarget() != creature)
								{
									mob.clearAggroList();
								}
								addAttackPlayerDesire(mob, creature);
								found = true;
								break;
							}
						}
						
						if (!found)
						{
							if (mob.isInsideRadius3D(_baium, 40))
							{
								if (mob.getTarget() != _baium)
								{
									mob.clearAggroList();
								}
								mob.setRunning();
								mob.addDamageHate(_baium, 0, 999);
								mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _baium);
							}
							else
							{
								mob.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _baium);
							}
						}
					}
					startQuestTimer("SELECT_TARGET", 5000, npc, null);
				}
				break;
			}
			case "CHECK_ATTACK":
			{
				if ((npc != null) && ((_lastAttack + 1800000) < System.currentTimeMillis()))
				{
					notifyEvent("CLEAR_ZONE", null, null);
					addSpawn(BAIUM_STONE, BAIUM_LOC, false, 0);
					setStatus(ALIVE);
				}
				else if (npc != null)
				{
					if (((_lastAttack + 300000) < System.currentTimeMillis()) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.75)))
					{
						npc.setTarget(npc);
						npc.doCast(HEAL_OF_BAIUM.getSkill());
					}
					startQuestTimer("CHECK_ATTACK", 60000, npc, null);
				}
				break;
			}
			case "CLEAR_STATUS":
			{
				setStatus(ALIVE);
				addSpawn(BAIUM_STONE, BAIUM_LOC, false, 0);
				break;
			}
			case "CLEAR_ZONE":
			{
				for (Creature creature : zone.getCharactersInside())
				{
					if (creature != null)
					{
						if (creature.isNpc())
						{
							creature.deleteMe();
						}
						else if (creature.isPlayer())
						{
							notifyEvent("teleportOut", null, (Player) creature);
						}
					}
				}
				break;
			}
			case "RESPAWN_BAIUM":
			{
				if (getStatus() == DEAD)
				{
					setRespawn(0);
					cancelQuestTimer("CLEAR_STATUS", null, null);
					notifyEvent("CLEAR_STATUS", null, null);
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": 현재 바이움이 소환되어 있습니다!");
				}
				break;
			}
			case "ABORT_FIGHT":
			{
				if (getStatus() == IN_FIGHT)
				{
					_baium = null;
					notifyEvent("CLEAR_ZONE", null, null);
					notifyEvent("CLEAR_STATUS", null, null);
					player.sendMessage(getClass().getSimpleName() + ": 전투가 중단되었습니다!");
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": 지금 공격을 중단할 수 없습니다!");
				}
				cancelQuestTimers("CHECK_ATTACK");
				cancelQuestTimers("SELECT_TARGET");
				break;
			}
			case "DESPAWN_MINIONS":
			{
				if (getStatus() == IN_FIGHT)
				{
					for (Creature creature : zone.getCharactersInside())
					{
						if ((creature != null) && creature.isNpc() && (creature.getId() == ARCHANGEL))
						{
							creature.deleteMe();
						}
					}
					if (player != null)
					{
						player.sendMessage(getClass().getSimpleName() + ": 모든 대천사가 소멸했습니다!");
					}
				}
				else if (player != null)
				{
					player.sendMessage(getClass().getSimpleName() + ": 지금은 대천사를 소멸할 수 없습니다.!");
				}
				break;
			}
			case "MANAGE_SKILLS":
			{
				if (npc != null)
				{
					manageSkills(npc);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		_lastAttack = System.currentTimeMillis();
		if (npc.getId() == BAIUM)
		{
			if ((attacker.getMountType() == MountType.STRIDER) && !attacker.isAffectedBySkill(ANTI_STRIDER.getSkillId()) && !npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
			{
				npc.setTarget(attacker);
				npc.doCast(ANTI_STRIDER.getSkill());
			}
			
			if (skill == null)
			{
				refreshAiParams(attacker, npc, (damage * 1000));
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.25))
			{
				refreshAiParams(attacker, npc, ((damage / 3) * 100));
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.5))
			{
				refreshAiParams(attacker, npc, (damage * 20));
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.75))
			{
				refreshAiParams(attacker, npc, (damage * 10));
			}
			else
			{
				refreshAiParams(attacker, npc, ((damage / 3) * 20));
			}
			manageSkills(npc);
		}
		else
		{
			final Attackable mob = (Attackable) npc;
			final Creature mostHated = mob.getMostHated();
			if ((getRandom(100) < 10) && SkillCaster.checkUseConditions(mob, SPEAR_ATTACK.getSkill()))
			{
				if ((mostHated != null) && (npc.calculateDistance3D(mostHated) < 1000) && zone.isCharacterInZone(mostHated))
				{
					mob.setTarget(mostHated);
					mob.doCast(SPEAR_ATTACK.getSkill());
				}
				else if (zone.isCharacterInZone(attacker))
				{
					mob.setTarget(attacker);
					mob.doCast(SPEAR_ATTACK.getSkill());
				}
			}
			
			if ((getRandom(100) < 5) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.5)) && SkillCaster.checkUseConditions(mob, ANGEL_HEAL.getSkill()))
			{
				npc.setTarget(npc);
				npc.doCast(ANGEL_HEAL.getSkill());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (zone.isCharacterInZone(killer))
		{
			setStatus(DEAD);
			addSpawn(TELE_CUBE, TELEPORT_CUBIC_LOC, false, 900000);
			zone.broadcastPacket(new PlaySound("BS01_D"));
			final long respawnTime = Config.BAIUM_SPAWN_INTERVAL * 3600000;
			setRespawn(respawnTime);
			startQuestTimer("CLEAR_STATUS", respawnTime, null, null);
			startQuestTimer("CLEAR_ZONE", 900000, null, null);
			cancelQuestTimer("CHECK_ATTACK", npc, null);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (!zone.isInsideZone(creature) || (creature.isNpc() && (creature.getId() == BAIUM_STONE)))
		{
			return super.onCreatureSee(npc, creature);
		}
		
		if (creature.isPlayer() && !creature.isDead() && (_standbyPlayer == null))
		{
			_standbyPlayer = (Player) creature;
		}
		
		if (creature.isInCategory(CategoryType.CLERIC_GROUP))
		{
			if (npc.getCurrentHp() < (npc.getMaxHp() * 0.25))
			{
				refreshAiParams(creature, npc, 10000);
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.5))
			{
				refreshAiParams(creature, npc, 10000, 6000);
			}
			else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.75))
			{
				refreshAiParams(creature, npc, 10000, 3000);
			}
			else
			{
				refreshAiParams(creature, npc, 10000, 2000);
			}
		}
		else
		{
			refreshAiParams(creature, npc, 10000, 1000);
		}
		manageSkills(npc);
		
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		startQuestTimer("MANAGE_SKILLS", 1000, npc, null);
		if (!zone.isCharacterInZone(npc) && (_baium != null))
		{
			_baium.teleToLocation(BAIUM_LOC);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public boolean unload(boolean removeFromList)
	{
		if (_baium != null)
		{
			_baium.deleteMe();
		}
		return super.unload(removeFromList);
	}
	
	private final void refreshAiParams(Creature attacker, Npc npc, int damage)
	{
		refreshAiParams(attacker, npc, damage, damage);
	}
	
	private final void refreshAiParams(Creature attacker, Npc npc, int damage, int aggro)
	{
		final int newAggroVal = damage + getRandom(3000);
		final int aggroVal = aggro + 1000;
		final NpcVariables vars = npc.getVariables();
		for (int i = 0; i < 3; i++)
		{
			if (attacker == vars.getObject("c_quest" + i, Creature.class))
			{
				if (vars.getInt("i_quest" + i) < aggroVal)
				{
					vars.set("i_quest" + i, newAggroVal);
				}
				return;
			}
		}
		final int index = CommonUtil.getIndexOfMinValue(vars.getInt("i_quest0"), vars.getInt("i_quest1"), vars.getInt("i_quest2"));
		vars.set("i_quest" + index, newAggroVal);
		vars.set("c_quest" + index, attacker);
	}
	
	private int getStatus()
	{
		return GrandBossManager.getInstance().getStatus(BAIUM);
	}
	
	private void addBoss(GrandBoss grandboss)
	{
		GrandBossManager.getInstance().addBoss(grandboss);
	}
	
	private void setStatus(int status)
	{
		GrandBossManager.getInstance().setStatus(BAIUM, status);
	}
	
	private void setRespawn(long respawnTime)
	{
		GrandBossManager.getInstance().getStatSet(BAIUM).set("respawn_time", (System.currentTimeMillis() + respawnTime));
	}
	
	private void manageSkills(Npc npc)
	{
		if (npc.isCastingNow(SkillCaster::isAnyNormalType) || npc.isCoreAIDisabled() || !npc.isInCombat())
		{
			return;
		}
		
		final NpcVariables vars = npc.getVariables();
		for (int i = 0; i < 3; i++)
		{
			final Creature attacker = vars.getObject("c_quest" + i, Creature.class);
			if ((attacker == null) || ((npc.calculateDistance3D(attacker) > 9000) || attacker.isDead()))
			{
				vars.set("i_quest" + i, 0);
			}
		}
		final int index = CommonUtil.getIndexOfMaxValue(vars.getInt("i_quest0"), vars.getInt("i_quest1"), vars.getInt("i_quest2"));
		final Creature creature = vars.getObject("c_quest" + index, Creature.class);
		final int i2 = vars.getInt("i_quest" + index);
		if ((i2 > 0) && (getRandom(100) < 70))
		{
			vars.set("i_quest" + index, 500);
		}
		
		SkillHolder skillToCast = null;
		if ((creature != null) && !creature.isDead())
		{
			if (npc.getCurrentHp() > (npc.getMaxHp() * 0.75))
			{
				if (getRandom(100) < 10)
				{
					skillToCast = ENERGY_WAVE;
				}
				else if (getRandom(100) < 10)
				{
					skillToCast = EARTH_QUAKE;
				}
				else
				{
					skillToCast = BAIUM_ATTACK;
				}
			}
			else if (npc.getCurrentHp() > (npc.getMaxHp() * 0.5))
			{
				if (getRandom(100) < 10)
				{
					skillToCast = GROUP_HOLD;
				}
				else if (getRandom(100) < 10)
				{
					skillToCast = ENERGY_WAVE;
				}
				else if (getRandom(100) < 10)
				{
					skillToCast = EARTH_QUAKE;
				}
				else
				{
					skillToCast = BAIUM_ATTACK;
				}
			}
			else if (npc.getCurrentHp() > (npc.getMaxHp() * 0.25))
			{
				if (getRandom(100) < 10)
				{
					skillToCast = THUNDERBOLT;
				}
				else if (getRandom(100) < 10)
				{
					skillToCast = GROUP_HOLD;
				}
				else if (getRandom(100) < 10)
				{
					skillToCast = ENERGY_WAVE;
				}
				else if (getRandom(100) < 10)
				{
					skillToCast = EARTH_QUAKE;
				}
				else
				{
					skillToCast = BAIUM_ATTACK;
				}
			}
			else if (getRandom(100) < 10)
			{
				skillToCast = THUNDERBOLT;
			}
			else if (getRandom(100) < 10)
			{
				skillToCast = GROUP_HOLD;
			}
			else if (getRandom(100) < 10)
			{
				skillToCast = ENERGY_WAVE;
			}
			else if (getRandom(100) < 10)
			{
				skillToCast = EARTH_QUAKE;
			}
			else
			{
				skillToCast = BAIUM_ATTACK;
			}
		}
		
		if ((skillToCast != null) && SkillCaster.checkUseConditions(npc, skillToCast.getSkill()))
		{
			npc.setTarget(creature);
			npc.doCast(skillToCast.getSkill());
		}
	}
	
	public static void main(String[] args)
	{
		new Baium();
	}
}