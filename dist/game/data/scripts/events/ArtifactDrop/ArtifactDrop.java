package events.ArtifactDrop;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class ArtifactDrop extends LongTimeEvent
{
	// @formatter:off
    // Monsters
    private static final int[] FORGOTTEN_OUT_MONSTERS =
    {
        21734, 21735, 21736, 21737,
        21742, 21743, 21744, 21745,
        21754, 21755, 21759, 21760
    };

    private static final int[] FORGOTTEN_INNER_MONSTERS =
    {
        21737, 21738, 21739, 21740, 21741,
        21746, 21747, 21748, 21749, 21750,
        21752, 21753, 21756, 21757
    };
    private static final int[] MONASTERY_SOLINAS =
    {
        22789, 22790, 22791, 22793
    };
    
    private static final int[] MONASTERY_ANGELS =
    {
        22794, 22795, 22796, 22797
    };
    
    private static final int[] MONASTERY_ARC_ANGELS =
    {
        22798, 22799, 22800
    };
    // @formatter:on
	
	// Items
	private static final int FORGOTTEN_ARTIFACT = 41073;
	private static final int MONASTERY_ARTIFACT = 41079;
	
	public ArtifactDrop()
	{
		addKillId(FORGOTTEN_OUT_MONSTERS);
		addKillId(FORGOTTEN_INNER_MONSTERS);
		addKillId(MONASTERY_SOLINAS);
		addKillId(MONASTERY_ANGELS);
		addKillId(MONASTERY_ARC_ANGELS);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		double rate = getDropRate(npc, killer);
		boolean isForgotten = (CommonUtil.contains(FORGOTTEN_OUT_MONSTERS, npc.getId()) || CommonUtil.contains(FORGOTTEN_INNER_MONSTERS, npc.getId())) ? true : false;
		dropArtifact(npc, killer, rate, isForgotten);
		
		return super.onKill(npc, killer, isSummon);
	}
	
	private double getDropRate(Npc npc, Player killer)
	{
		double rate = CommonUtil.contains(MONASTERY_ANGELS, npc.getId()) ? 6 : CommonUtil.contains(MONASTERY_ARC_ANGELS, npc.getId()) || CommonUtil.contains(FORGOTTEN_INNER_MONSTERS, npc.getId()) ? 7 : 4;
		
		if (killer.isInParty())
		{
			final Party party = killer.getParty();
			final List<Player> members = party.getMembers();
			
			if (members.size() > 2)
			{
				for (Player member : members)
				{
					if (member.isInsideRadius3D(killer, 500) && !member.isDead())
					{
						rate += 0.7;
					}
				}
			}
		}
		
		if (Config.PREMIUM_SYSTEM_ENABLED && (killer.isPremium()))
		{
			rate *= 1.05;
		}
		
		return rate;
	}
	
	private void dropArtifact(Npc npc, Player player, double rate, boolean isForgotten)
	{
		if (Rnd.chance(BorinetUtil.getInstance().isPlayerDropPenalty(player) ? (rate * 0.7) : rate))
		{
			int minAmount = Rnd.chance(isForgotten ? 20 : 50) && (rate > 6) ? Rnd.get(2, 4) : 1;
			player.doAutoLoot((Attackable) npc, isForgotten ? FORGOTTEN_ARTIFACT : MONASTERY_ARTIFACT, minAmount);
		}
	}
	
	public static void main(String[] args)
	{
		new ArtifactDrop();
	}
}
