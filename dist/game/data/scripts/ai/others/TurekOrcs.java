package ai.others;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

public class TurekOrcs extends AbstractNpcAI
{
	private static boolean enabled = false;
	
	// 타이머를 관리하는 스레드 풀
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	// NPC별 타이머 관리
	private final Map<Npc, ScheduledFuture<?>> npcTimers = new ConcurrentHashMap<>();
	
	// NPCs
	private static final int[] MOBS =
	{
		20436, // 올 마훔 보급병
		20437, // 올 마훔 신병
		20438, // 올 마훔 장성
		20439, // 올 마훔 하사관
		20494, // 투렉 군견
		20495, // 투렉 오크 군장
		20496, // 투렉 오크 궁병
		20497, // 투렉 오크 돌격병
		20498, // 투렉 오크 보급병
		20499, // 투렉 오크 보병
		20500, // 투렉 오크 보초병
		20501 // 투렉 오크 보초병
	};
	
	private TurekOrcs()
	{
		clearAllTimers();
		addAttackId(MOBS);
		addMoveFinishedId(MOBS);
	}
	
	// 타이머 시작
	private void startNpcTimer(Npc npc, Runnable task, long delay)
	{
		cancelNpcTimer(npc); // 기존 타이머 취소
		ScheduledFuture<?> timer = scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
		npcTimers.put(npc, timer);
	}
	
	// 타이머 취소
	private void cancelNpcTimer(Npc npc)
	{
		ScheduledFuture<?> timer = npcTimers.remove(npc);
		if (timer != null)
		{
			timer.cancel(false);
		}
	}
	
	// 모든 타이머 취소
	private void clearAllTimers()
	{
		npcTimers.values().forEach(timer -> timer.cancel(false));
		npcTimers.clear();
		enabled = false;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.4)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.1)) && (npc.getVariables().getInt("state") < 1) && (getRandom(100) < 2))
		{
			if (!enabled)
			{
				// 도망 좌표 계산, 변수 저장
				npc.getVariables().set("fleeX", npc.getX() + Rnd.get(-800, -500));
				npc.getVariables().set("fleeY", npc.getY() + Rnd.get(700, 1200));
				npc.getVariables().set("fleeZ", npc.getZ() + Rnd.get(100, 150));
				
				// 상태 정보 저장
				npc.getVariables().set("state", 1);
				npc.getVariables().set("attacker", attacker.getObjectId());
				
				// 도망 동작 수행
				enabled = true;
				npc.broadcastSay(ChatType.GENERAL, NpcStringId.getNpcStringId(getRandom(1000007, 1000027)));
				// npc.disableCoreAI(true); // 공격 행동 방지
				// npc.setTarget(null); // NPC 타겟 초기화
				((Attackable) npc).clearAggroList(); // 증오 목록 초기화
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getVariables().getInt("fleeX"), npc.getVariables().getInt("fleeY"), npc.getVariables().getInt("fleeZ")));
			}
		}
		else if (npc.getVariables().getInt("state") >= 1)
		{
			npc.disableCoreAI(false);
			cancelNpcTimer(npc); // 기존 타이머 취소
			enabled = false;
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public void onMoveFinished(Npc npc)
	{
		npc.disableCoreAI(false);
		if (npc.getVariables().getInt("state") == 1)
		{
			startNpcTimer(npc, () ->
			{
				npc.getVariables().set("state", 2);
				npc.setCurrentHp(npc.getStat().getMaxHp(), true);
				((Attackable) npc).returnHome();
			}, 3000);
		}
		else if (npc.getVariables().getInt("state") == 2)
		{
			enabled = false;
			cancelNpcTimer(npc); // 기존 타이머 취소
			npc.getVariables().remove("state");
			
			String[] npcSayings =
			{
				"복수의 시간이다!",
				"내가 도망간 줄 알았냐?",
				"나비처럼 날아서 벌처럼 쏴주마~",
				"거기 딱 대기! 대기!",
				"으하하하!",
				"도망간건 추진력을 얻기위함 이었지!!",
				"이번엔 내가 이긴다!!"
			};
			String selectedSaying = npcSayings[Rnd.get(0, npcSayings.length - 1)];
			npc.broadcastSay(ChatType.GENERAL, selectedSaying);
			
			// 주변 플레이어 중 최근에 자신을 공격한 대상 찾기
			WorldObject attacker = World.getInstance().getPlayer(npc.getVariables().getInt("attacker"));
			if ((attacker instanceof Player) && !((Player) attacker).isDead())
			{
				Attackable attackable = (Attackable) npc;
				attackable.addDamageHate((Player) attacker, 0, 99999); // 강한 증오 설정
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, (Player) attacker);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new TurekOrcs();
	}
}
