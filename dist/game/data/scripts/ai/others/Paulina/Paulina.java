package ai.others.Paulina;

import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import ai.AbstractNpcAI;

/**
 * Paulina AI입니다. 이 NPC는 특정 레벨 범위와 서브 클래스가 활성화되지 않은 플레이어에게 장비 세트를 제공합니다. 이 AI는 일부 HTML 페이지를 사용하여 대화하고 장비 세트를 제공합니다. 페이지는 "index.htm", "sub.htm", "low.htm", "aleady.htm", "sucsess.htm" 중 하나를 사용합니다. 플레이어의 레벨과 서브 클래스 활성화 여부를 확인하여 장비를 제공하거나 거부합니다. 플레이어가 이미 해당 등급의 장비를 획득한 경우에는 거부 메시지를 표시합니다. 장비 아이템을 획득한
 * 경우 플레이어에게 메시지를 표시하고 아이템을 제공합니다. 페이지에서 "<?string?>" 및 "<?name?>" 키워드를 사용하여 문자열 및 플레이어 이름을 동적으로 설정합니다. 대화 이벤트에 따라 장비 세트를 제공하거나 거부합니다. 대화 이벤트에 따라 적절한 HTML 페이지를 플레이어에게 보냅니다.
 * @author 보리넷 가츠
 */
public class Paulina extends AbstractNpcAI
{
	private static final int PAULINA = 40020;
	
	private Paulina()
	{
		addStartNpc(PAULINA);
		// addTalkId(PAULINA);
		// addFirstTalkId(PAULINA);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "index.htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "BOX_D":
			{
				if (player.isSubClassActive())
				{
					showHtml(player, "sub", null);
					break;
				}
				if (player.getLevel() < 20)
				{
					showHtml(player, "low", "20");
					break;
				}
				if (player.getVariables().getBoolean("D그레이드 폴리네의 장비 세트", false))
				{
					showHtml(player, "aleady", "D그레이드 폴리네의 장비 세트");
					break;
				}
				player.getVariables().set("D그레이드 폴리네의 장비 세트", true);
				player.addItem("D그레이드 폴리네의 장비 세트", 46849, 1, player, true);
				showHtml(player, "sucsess", "D그레이드 폴리네의 장비 세트");
				break;
			}
			case "BOX_C":
			{
				if (player.isSubClassActive())
				{
					showHtml(player, "sub", null);
					break;
				}
				if (player.getLevel() < 40)
				{
					showHtml(player, "low", "40");
					break;
				}
				if (player.getVariables().getBoolean("C그레이드 폴리네의 장비 세트", false))
				{
					showHtml(player, "aleady", "C그레이드 폴리네의 장비 세트");
					break;
				}
				player.getVariables().set("C그레이드 폴리네의 장비 세트", true);
				player.addItem("C그레이드 폴리네의 장비 세트", 46850, 1, player, true);
				showHtml(player, "sucsess", "C그레이드 폴리네의 장비 세트");
				break;
			}
			case "BOX_A":
			{
				if (player.isSubClassActive())
				{
					showHtml(player, "sub", null);
					break;
				}
				if (player.getLevel() < 61)
				{
					showHtml(player, "low", "61");
					break;
				}
				if (player.getVariables().getBoolean("A그레이드 폴리네의 장비 세트", false))
				{
					showHtml(player, "aleady", "A그레이드 폴리네의 장비 세트");
					break;
				}
				player.getVariables().set("A그레이드 폴리네의 장비 세트", true);
				player.addItem("A그레이드 폴리네의 장비 세트", 46851, 1, player, true);
				showHtml(player, "sucsess", "A그레이드 폴리네의 장비 세트");
				break;
			}
			case "BOX_S":
			{
				if (player.isSubClassActive())
				{
					showHtml(player, "sub", null);
					break;
				}
				if (player.getLevel() < 76)
				{
					showHtml(player, "low", "76");
					break;
				}
				if (player.getVariables().getBoolean("S그레이드 폴리네의 장비 세트", false))
				{
					showHtml(player, "aleady", "S그레이드 폴리네의 장비 세트");
					break;
				}
				player.getVariables().set("S그레이드 폴리네의 장비 세트", true);
				player.addItem("S그레이드 폴리네의 장비 세트", 46852, 1, player, true);
				showHtml(player, "sucsess", "S그레이드 폴리네의 장비 세트");
				break;
			}
			case "BOX_R":
			{
				if (player.isSubClassActive())
				{
					showHtml(player, "sub", null);
					break;
				}
				if (player.getLevel() < 84)
				{
					showHtml(player, "low", "84");
					break;
				}
				if (player.getVariables().getBoolean("R그레이드 폴리네의 장비 세트", false))
				{
					showHtml(player, "aleady", "R그레이드 폴리네의 장비 세트");
					break;
				}
				player.getVariables().set("R그레이드 폴리네의 장비 세트", true);
				player.addItem("R그레이드 폴리네의 장비 세트", 46919, 1, player, true);
				showHtml(player, "sucsess", "R그레이드 폴리네의 장비 세트");
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	private void showHtml(Player player, String page, String string)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/scripts/ai/others/Paulina/index_" + page + ".htm");
		html = html.replace("<?string?>", String.valueOf(string));
		html = html.replace("<?name?>", String.valueOf(player.getName()));
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	public static void main(String[] args)
	{
		new Paulina();
	}
}