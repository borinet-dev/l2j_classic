package ai.others;

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
		20500 // 투렉 오크 보초병
	};
	
	private TurekOrcs()
	{
		addAttackId(MOBS);
		addEventReceivedId(MOBS);
		addMoveFinishedId(MOBS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("checkState") && !npc.isDead() && (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK))
		{
			if ((npc.getCurrentHp() > (npc.getMaxHp() * 0.7)) && (npc.getVariables().getInt("state") == 2))
			{
				npc.getVariables().set("state", 3);
				((Attackable) npc).returnHome();
			}
			else
			{
				npc.getVariables().remove("state");
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if (!npc.getVariables().hasVariable("isHit"))
		{
			npc.getVariables().set("isHit", 1);
		}
		else if ((npc.getCurrentHp() > (npc.getMaxHp() * 0.3)) && (attacker.getCurrentHp() > (attacker.getMaxHp() * 0.25)) && (npc.getVariables().getInt("state") == 0) && (getRandom(100) < 25))
		{
			int fleeX = npc.getX() + 1000;
			int fleeY = npc.getY() + 1000;
			int fleeZ = npc.getZ() + 100;
			
			npc.getVariables().set("fleeX", fleeX);
			npc.getVariables().set("fleeY", fleeY);
			npc.getVariables().set("fleeZ", fleeZ);
			
			// Say and flee
			npc.broadcastSay(ChatType.GENERAL, NpcStringId.getNpcStringId(getRandom(1000007, 1000027)));
			npc.disableCoreAI(true); // to avoid attacking behaviour, while flee
			npc.setRunning();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(fleeX, fleeY, fleeZ));
			npc.getVariables().set("state", 1);
			npc.getVariables().set("attacker", attacker.getObjectId());
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onEventReceived(String eventName, Npc sender, Npc receiver, WorldObject reference)
	{
		if (eventName.equals("WARNING") && !receiver.isDead() && (receiver.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK) && (reference != null) && (reference.getActingPlayer() != null) && !reference.getActingPlayer().isDead())
		{
			receiver.getVariables().set("state", 3);
			receiver.setRunning();
			((Attackable) receiver).addDamageHate(reference.getActingPlayer(), 0, 99999);
			receiver.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, reference.getActingPlayer());
		}
		return super.onEventReceived(eventName, sender, receiver, reference);
	}
	
	@Override
	public void onMoveFinished(Npc npc)
	{
		// NPC reaches flee point
		if (npc.getVariables().getInt("state") == 1)
		{
			if ((npc.getX() == npc.getVariables().getInt("fleeX")) && (npc.getY() == npc.getVariables().getInt("fleeY")))
			{
				npc.disableCoreAI(false);
				startQuestTimer("checkState", 15000, npc, null);
				npc.getVariables().set("state", 2);
				npc.broadcastEvent("WARNING", 400, World.getInstance().getPlayer(npc.getVariables().getInt("attacker")));
			}
			else
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getVariables().getInt("fleeX"), npc.getVariables().getInt("fleeY"), npc.getVariables().getInt("fleeZ")));
			}
		}
		else if ((npc.getVariables().getInt("state") == 3) && npc.staysInSpawnLoc())
		{
			npc.disableCoreAI(false);
			npc.getVariables().remove("state");
		}
	}
	
	public static void main(String[] args)
	{
		new TurekOrcs();
	}
}
