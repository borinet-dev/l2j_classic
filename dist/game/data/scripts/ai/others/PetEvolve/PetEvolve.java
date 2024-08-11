package ai.others.PetEvolve;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.util.Evolve;

import ai.AbstractNpcAI;

public class PetEvolve extends AbstractNpcAI
{
	private static final int[] MANAGERS =
	{
		30731,
		30827,
		30828,
		30829,
		30830,
		30831,
		30869,
		31067
	};
	
	private PetEvolve()
	{
		addStartNpc(MANAGERS);
		addTalkId(MANAGERS);
		addFirstTalkId(MANAGERS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		boolean ok = false;
		switch (event)
		{
			case "PetDelete":
			{
				final Summon pet = player.getPet();
				if (player.hasPet() && pet.isDead())
				{
					pet.unSummon(player);
					pet.deleteMe(player);
					pet.isInvisible();
				}
				else
				{
					player.sendMessage("사망상태의 펫이 없습니다.");
				}
				ok = true;
				break;
			}
			case "wind":
			{
				if (getQuestItemsCount(player, 57) < 1000000)
				{
					player.sendMessage("진화비용은 100만 아데나입니다. 비용이 부족합니다.");
				}
				else
				{
					ok = Evolve.doEvolve(player, npc, 4422, 10308, 55);
					if (ok)
					{
						takeItems(player, 57, 1000000);
					}
				}
				break;
			}
			case "star":
			{
				if (getQuestItemsCount(player, 57) < 1000000)
				{
					player.sendMessage("진화비용은 100만 아데나입니다. 비용이 부족합니다.");
				}
				else
				{
					ok = Evolve.doEvolve(player, npc, 4423, 10309, 55);
					if (ok)
					{
						takeItems(player, 57, 1000000);
					}
				}
				break;
			}
			case "twil":
			{
				if (getQuestItemsCount(player, 57) < 1000000)
				{
					player.sendMessage("진화비용은 100만 아데나입니다. 비용이 부족합니다.");
				}
				else
				{
					ok = Evolve.doEvolve(player, npc, 4424, 10310, 55);
					if (ok)
					{
						takeItems(player, 57, 1000000);
					}
				}
				break;
			}
			case "evol":
			{
				if (getQuestItemsCount(player, 57) < 5000000)
				{
					player.sendMessage("진화비용은 500만 아데나입니다. 비용이 부족합니다.");
				}
				else
				{
					if (player.getInventory().getItemByItemId(10308) != null)
					{
						ok = Evolve.doEvolve(player, npc, 10308, 14819, 75);
						if (ok)
						{
							takeItems(player, 57, 5000000);
						}
					}
					else if (player.getInventory().getItemByItemId(10309) != null)
					{
						ok = Evolve.doEvolve(player, npc, 10309, 14819, 75);
						if (ok)
						{
							takeItems(player, 57, 5000000);
						}
					}
					else if (player.getInventory().getItemByItemId(10310) != null)
					{
						ok = Evolve.doEvolve(player, npc, 10310, 14819, 75);
						if (ok)
						{
							takeItems(player, 57, 5000000);
						}
					}
				}
				break;
			}
		}
		if (!ok)
		{
			htmltext = "data/html/petmanager/evolve_no.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		htmltext = "data/html/petmanager/" + npc.getId() + ".htm";
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new PetEvolve();
	}
}
