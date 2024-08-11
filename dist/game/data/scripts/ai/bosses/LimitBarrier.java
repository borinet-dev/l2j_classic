package ai.bosses;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import ai.AbstractNpcAI;

public final class LimitBarrier extends AbstractNpcAI
{
	// NPCs
	private static final int[] RAID_BOSSES =
	{
		29001, // Queen Ant
		29006, // Core
		// 29014, // Orfen
		25010, // Furious Thiles
		25013, // Ghose of Peasant Captain
		25016, // The 3rd Underwater Guardian
		25029, // Atraiban
		25032, // Eva's Guardian Millenu
		25035,
		25050, // Verfa
		25067, // Red Flag Captain Shaka
		25070, // Enchanted Valley Lookout Ruell
		25089, // Soulless Wild Boar
		25099, // Rooting Tree Repira
		25103, // Wizard Isirr
		25119, // Faire Queens Messenger Berun
		25159, // Paniel the Unicorn
		25162,
		25122, // Refugee Applicant Leo
		25131, // Slaughter Lord Gata
		25137, // Beleth Seer Sephira
		25140,
		25143,
		25176, // Black Lily
		25179,
		25182,
		25198,
		25199,
		25217, // Cursed Clara
		25230, // Timak Priest Ragothi
		25233,
		25241, // Harit Hero Tamashi
		25244,
		25245,
		25418, // Dread Avenger Kraven
		25420, // Orfens Handmaiden
		25434, // Bandit Leader Barda
		25444,
		25447,
		25450,
		25248,
		25249,
		25460, // Deaman Ereve
		25463, // Harit Guardian Garangky
		25467,
		25470,
		25473, // Grave Robber Kim
		25475, // Ghost Knight Kabed
		25744, // Zombie Lord Darkhon
		25745, // Orc Timak Darphen
		18049, // Shilens Messenger Cabrio
		25051, // Rahha
		25054,
		25067,
		25106, // Ghost of the Well Lidia
		25109,
		25125, // Fierce Tiger King Angel
		25126,
		25163, // Roaring Skylancer
		25226, // Roaring Lord Kastor
		25234, // Ancient Weird Drake
		25235,
		25238,
		25252, // Palibati Queen Themis
		25255, // Gargayle Lord Tiphon
		25256, // Taik High Prefect Arak
		25259,
		25263, // Kernons Faithul Servant Kelone
		25266,
		25269,
		25276,
		25277,
		25280,
		25281,
		25282,
		25290,
		25293,
		25296,
		25299,
		25273,
		25407, // Lord Ishka
		25423, // Fairy Queen Timiniel
		25453, // Meanas Anor
		25478, // Shilens Priest Hisilrome
		25484,
		25493,
		25496,
		25738, // Queen Ant Drone Priest
		25739, // Angel Priest of Baium
		25742, // Priest of Core Decar
		25743, // Priest of Lord Ipos
		25746, // Evil Magikus
		25747, // Rael Mahum Radium
		25748, // Rael Mahum Supercium
		25749, // Tayga Feron King
		25750, // Tayga Marga Shaman
		25751, // Tayga Septon Champion
		25754, // Flamestone Giant
		25755, // Gross Salamander
		25756, // Gross Dre Vanul
		25757, // Gross Ifrit
		25758, // Fiend Goblier
		25759, // Fiend Cherkia
		25760, // Fiend Harthemon
		25761, // Fiend Sarboth
		25762, // Demon Bedukel
		25763, // Bloody Witch Rumilla
		25766, // Monster Minotaur
		25767, // Monster Bulleroth
		25768, // Dorcaus
		25769, // Kerfaus
		25770, // Milinaus
		25772, // Evil Orc Zetahl
		25773, // Evil Orc Tabris
		25774, // Evil Orc Ravolas
		25775, // Evil Orc Dephracor
		25776, // Amden Orc Turahot
		25777, // Amden Orc Turation
		25779, // Gariott
		25780, // Varbasion
		25781, // Varmoni
		25782, // Overlord Muscel
		25783, // Bathsus Elbogen
		25784, // Daumen Kshana
		25787, // Death Knight 1
		25788, // Death Knight 2
		25789, // Death Knight 3
		25790, // Death Knight 4
		25791, // Death Knight 5
		25792, // Death Knight 6
		25792, // Giant Golden Pig
		25073, // 피의 사제 루델토
		25089,
		25092,
		25099,
		25202,
		25205,
		25220,
		25229,
		25302,
		25305,
		25306,
		25309,
		25312,
		25315,
		25316,
		25319,
		25322,
		25325,
		25328,
		25336,
		25337,
		25338,
		// 29047, // 스칼렛
		// 25286, // 아나킴
		// 25283, // 릴리스
		// 18029, // 오르펜
		// 18020, // 여왕개미
		// 29105, // 이그니스
		// 29106, // 네불라
		// 29107, // 프로첼라
		// 29108, // 페트람
		41018, // 랜덤 보스
		41019,
		41020,
		41021,
		41022,
		41023,
		41024,
		41025,
		41026,
		41027,
		41028,
		41029,
		41030,
		41031,
		41032,
		41033,
		41034,
		41035,
		41036,
		41037,
		36706 // 미스릴광산
	};
	
	// Skill
	private static final SkillHolder LIMIT_BARRIER = new SkillHolder(32203, 1);
	private static final SkillHolder NEW_PHASE = new SkillHolder(30229, 1);
	// Misc
	private static final Map<Npc, Integer> RAIDBOSS_HITS = new ConcurrentHashMap<>();
	private static final Map<Npc, Integer> RAIDBOSS_STAGE = new ConcurrentHashMap<>();
	private static final Map<Npc, Integer> MESSAGE = new ConcurrentHashMap<>();
	private static final Map<Npc, Integer> HIT_COUNT = new ConcurrentHashMap<>();
	
	private LimitBarrier()
	{
		addAttackId(RAID_BOSSES);
		addKillId(RAID_BOSSES);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "RESTORE_FULL_HP":
			{
				npc.setInvul(false);
				npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_TO_DESTROY_THE_LIMIT_BARRIER_NTHE_RAID_BOSS_FULLY_RECOVERS_ITS_ITS_STRENGTH_ITS_HEALTH, 2, 5000, true));
				npc.setCurrentHp(npc.getStat().getMaxHp(), true);
				npc.stopSkillEffects(SkillFinishType.REMOVED, LIMIT_BARRIER.getSkillId());
				RAIDBOSS_STAGE.remove(npc);
				RAIDBOSS_HITS.remove(npc);
				MESSAGE.remove(npc);
				HIT_COUNT.put(npc, 100);
				break;
			}
			case "MESSAGE":
			{
				MESSAGE.put(npc, 1);
				break;
			}
			case "USE_SKILL":
			{
				npc.setTarget(npc);
				npc.abortAttack();
				npc.abortCast();
				npc.setInvul(true);
				npc.doCast(LIMIT_BARRIER.getSkill());
				if (!npc.isAffectedBySkill(LIMIT_BARRIER.getSkillId()))
				{
					startQuestTimer("USE_SKILL", 10, npc, null);
				}
				else
				{
					npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_RAID_BOSS_USES_THE_LIMIT_BARRIER_NFOCUS_YOUR_ATTACKS_TO_DESTROY_THE_LIMIT_BARRIER_IN_15_SECONDS, 2, 3000, true));
					startQuestTimer("MESSAGE", 3000, npc, null);
					startQuestTimer("RESTORE_FULL_HP", 15000, npc, null);
				}
				break;
			}
			case "NEW_PHASE":
			{
				npc.setTarget(npc);
				npc.abortAttack();
				npc.abortCast();
				npc.doCast(NEW_PHASE.getSkill());
				if (!npc.isAffectedBySkill(NEW_PHASE.getSkillId()))
				{
					startQuestTimer("NEW_PHASE", 10, npc, null);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
					sm.addString("보스의 HP가 20% 미만으로 떨어져서 페이즈 2로 돌입하였습니다!");
					npc.broadcastPacket(sm);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	private void broadcastScreenMessage(Npc npc, String message, int duration)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_N);
		sm.addString(message);
		npc.broadcastPacket(sm);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final int stage = RAIDBOSS_STAGE.getOrDefault(npc, 0);
		
		if (npc.isAffectedBySkill(LIMIT_BARRIER.getSkillId()))
		{
			final int count = HIT_COUNT.getOrDefault(npc, 0);
			final int message = MESSAGE.getOrDefault(npc, 0);
			final int hits = RAIDBOSS_HITS.getOrDefault(npc, 0);
			
			RAIDBOSS_HITS.put(npc, hits + 1);
			
			if (attacker.isPlayer())
			{
				final int hit = hits + 1;
				if (message == 1)
				{
					broadcastScreenMessage(npc, hit + " / " + count + " 타격!", 1);
				}
				if (hit >= count)
				{
					npc.setInvul(false);
					cancelQuestTimer("RESTORE_FULL_HP", npc, null);
					npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_HAVE_DESTROYED_THE_LIMIT_BARRIER, 2, 5000, true));
					npc.stopSkillEffects(SkillFinishType.REMOVED, LIMIT_BARRIER.getSkillId());
					RAIDBOSS_HITS.remove(npc);
					MESSAGE.remove(npc);
					HIT_COUNT.put(npc, 100);
				}
			}
		}
		else
		{
			if (attacker.isPlayer())
			{
				switch (stage)
				{
					case 0:
					{
						if (npc.getCurrentHpPercent() <= 90)
						{
							startQuestTimer("USE_SKILL", 10, npc, null);
							HIT_COUNT.put(npc, 100);
							RAIDBOSS_STAGE.put(npc, 1);
						}
						break;
					}
					case 1:
					{
						if (npc.getCurrentHpPercent() <= 60)
						{
							startQuestTimer("USE_SKILL", 10, npc, null);
							RAIDBOSS_STAGE.put(npc, 2);
						}
						break;
					}
					case 2:
					{
						if (npc.getCurrentHpPercent() <= 30)
						{
							startQuestTimer("USE_SKILL", 10, npc, null);
							RAIDBOSS_STAGE.put(npc, 3);
						}
						break;
					}
					case 3:
					{
						if (npc.getCurrentHpPercent() < 20)
						{
							startQuestTimer("NEW_PHASE", 10, npc, null);
							RAIDBOSS_STAGE.put(npc, 4);
						}
						break;
					}
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		RAIDBOSS_STAGE.remove(npc);
		RAIDBOSS_HITS.remove(npc);
		MESSAGE.remove(npc);
		HIT_COUNT.put(npc, 100);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new LimitBarrier();
	}
}
