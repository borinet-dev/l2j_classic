package ai.others.Sineater;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import ai.AbstractNpcAI;

/**
 * @author 보리넷 가츠
 */
public class Sineater extends AbstractNpcAI
{
	private static final int SINEATER = 40022;
	
	private Sineater()
	{
		addStartNpc(SINEATER);
		addTalkId(SINEATER);
		addFirstTalkId(SINEATER);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String page = ((player.getReputation() < 0) || (player.getPkKills() > 0)) ? "index" : "no";
		String html = HtmCache.getInstance().getHtm(null, "data/scripts/ai/others/Sineater/" + page + ".htm");
		html = html.replace("<?name?>", String.valueOf(player.getName()));
		html = html.replace("<?pk?>", String.valueOf(player.getPkKills()));
		html = html.replace("<?reputation?>", String.valueOf(player.getReputation()));
		html = html.replace("<?pktext?>", player.getPkKills() > 0 ? "사람을 <font color=\"FF0000\">" + player.getPkKills() + "</font>번이나 죽였군요." : "");
		html = html.replace("<?karmatext?>", player.getReputation() < 0 ? player.getName() + "님의 카르마 수치는 무려 <font color=\"FF0000\">" + player.getReputation() + "</font> 입니다!" : player.getName() + "님께서는 그나마 카르마 수치는 없군요!");
		html = html.replace("<?eat?>", ((player.getReputation() < 0) || (player.getPkKills() > 0)) ? "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest Sineater ONE_EAT\">1,000만 아데나로 300의 수치를 감면 받는다.</Button><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest Sineater ALL_EAT\">10억 아데나로 모든 수치를 감면 받는다.</Button>" : "");
		html = html.replace("<?pkcount?>", player.getPkKills() > 0 ? "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest Sineater PK_KILL_COUNT\">3억 아데나로 PK 카운터를 1~3 랜덤으로 감면 받는다.</Button>" : "");
		
		return html;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "ONE_EAT":
			{
				if (player.getInventory().getInventoryItemCount(57, 0) < 10000000)
				{
					player.sendMessage("아데나 부족합니다.");
					break;
				}
				if (player.getReputation() >= 0)
				{
					player.sendMessage("감면 받을 카르마 수치가 없습니다.");
					break;
				}
				
				takeItems(player, 57, 10000000);
				player.setReputation(player.getReputation() + 300);
				showHtml(player);
				break;
			}
			case "ALL_EAT":
			{
				if (player.getInventory().getInventoryItemCount(57, 0) < 1000000000)
				{
					player.sendMessage("아데나 부족합니다.");
					break;
				}
				if (player.getReputation() >= 0)
				{
					player.sendMessage("감면 받을 카르마 수치가 없습니다.");
					break;
				}
				
				takeItems(player, 57, 1000000000);
				player.setReputation(0);
				showHtml(player);
				break;
			}
			case "PK_KILL_COUNT":
			{
				if (player.getInventory().getInventoryItemCount(57, 0) < 300000000)
				{
					player.sendMessage("아데나 부족합니다.");
					break;
				}
				if (player.getPkKills() == 0)
				{
					player.sendMessage("감면 받을 PK 카운트가 없습니다.");
					break;
				}
				
				takeItems(player, 57, 300000000);
				if (Rnd.chance(65))
				{
					player.setPkKills(Math.max(0, player.getPkKills() - 1));
					player.broadcastUserInfo();
				}
				else
				{
					if (Rnd.chance(65))
					{
						player.setPkKills(Math.max(0, player.getPkKills() - 2));
						player.broadcastUserInfo();
					}
					else
					{
						player.setPkKills(Math.max(0, player.getPkKills() - 3));
						player.broadcastUserInfo();
					}
				}
				showHtml(player);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	private void showHtml(Player player)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/scripts/ai/others/SinEater/index_sucsess.htm");
		html = html.replace("<?name?>", String.valueOf(player.getName()));
		html = html.replace("<?reputation?>", String.valueOf(player.getReputation()));
		html = html.replace("<?pk?>", String.valueOf(player.getPkKills()));
		html = html.replace("<?text?>", ((player.getReputation() < 0) || (player.getPkKills() > 0)) ? "사람을 죽일때마다 카르마 수치는 더욱 많이 늘어난다는걸 항상 명심해야해요!<br>조금 더 아키서스님의 힘으로 그 죄를 감면 받고 싶으신가요?" : "<br>아키서스님의 은총을 듬뿍 받으셨군요!! 부럽..<br>에헴... 앞으로 더이상 죄인으로 살지마세요!");
		html = html.replace("<?eat?>", player.getReputation() < 0 ? "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest Sineater ONE_EAT\">1,000만 아데나로 300의 수치를 감면 받는다.</Button><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest Sineater ALL_EAT\">10억 아데나로 모든 수치를 감면 받는다.</Button>" : "");
		html = html.replace("<?pkcount?>", player.getPkKills() > 0 ? "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest Sineater PK_KILL_COUNT\">3억 아데나로 PK 카운터를 1~3 랜덤으로 감면 받는다.</Button>" : "");
		player.sendPacket(new NpcHtmlMessage(html));
	}
	
	public static void main(String[] args)
	{
		new Sineater();
	}
}