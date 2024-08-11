package handlers.voicedcommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.NpcNameTable;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EnterRaidCheck;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

/**
 * @author 보리넷
 */
public class EpicBossStatus extends AbstractNpcAI implements IVoicedCommandHandler
{
	private static final byte DEAD = 3;
	private static final int MIN_PEOPLE_CORE = Config.CORE_MIN_MEMBER;
	private static final int MIN_PEOPLE_ORFEN = Config.ORFEN_MIN_MEMBER;
	private static final int MIN_PEOPLE_QUEEN_ANT = Config.QUEEN_ANT_MIN_MEMBER;
	
	private static final String[] VOICED_COMMANDS =
	{
		"showBoss",
		"gotoCore",
		"gotoOrfen",
		"gotoAnt",
		"bossLvl",
		"gotoGiran",
		"gotoMine"
	};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		switch (command)
		{
			case "showBoss":
				showEpicIndexPage(player);
				break;
			case "bossLvl":
				showBossLvl(player);
				break;
			case "gotoCore":
				gotoBoss(player, 29006);
				break;
			case "gotoOrfen":
				gotoBoss(player, 29014);
				break;
			case "gotoAnt":
				gotoBoss(player, 29001);
				break;
			case "gotoGiran":
				player.teleToLocation(81929 + getRandom(300), 149309 + getRandom(300), -3464);
				break;
			case "gotoMine":
				player.teleToLocation(BorinetUtil.MITHRIL_RAID_ZONE_POINT[Rnd.get(BorinetUtil.MITHRIL_RAID_ZONE_POINT.length)]);
				break;
		}
		return true;
	}
	
	public String gotoBoss(Player player, int bossId)
	{
		String korName = "";
		String deadName = "";
		switch (bossId)
		{
			case 29006:
				korName = "코어";
				deadName = "코어가";
				break;
			case 29014:
				korName = "오르펜";
				deadName = "오르펜이";
				break;
			case 29001:
				korName = "여왕개미";
				deadName = "여왕개미가";
				break;
		}
		String htmltext = null;
		if (getStatus(bossId) == DEAD)
		{
			Broadcast.toPlayerScreenMessageS(player, "현재 " + deadName + " 사망한 상태이므로 이동할 수 없습니다.");
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
					return null;
				}
			}
			else
			{
				final boolean isCCLeader = party.getCommandChannel().isLeader(player);
				if (!isCCLeader)
				{
					Broadcast.toPlayerScreenMessageS(player, "연합 파티장만이 입장시도를 할 수 있습니다.");
					return null;
				}
			}
			
			switch (bossId)
			{
				case 29001:
					if (members.size() < MIN_PEOPLE_QUEEN_ANT)
					{
						Broadcast.toPlayerScreenMessageS(player, "입장가능 최소인원은 " + MIN_PEOPLE_QUEEN_ANT + "명 입니다.");
					}
					break;
				case 29006:
					if (members.size() < MIN_PEOPLE_CORE)
					{
						Broadcast.toPlayerScreenMessageS(player, "입장가능 최소인원은 " + MIN_PEOPLE_CORE + "명 입니다.");
					}
					break;
				case 29014:
					if (members.size() < MIN_PEOPLE_ORFEN)
					{
						Broadcast.toPlayerScreenMessageS(player, "입장가능 최소인원은 " + MIN_PEOPLE_ORFEN + "명 입니다.");
					}
					break;
			}
			teleport(player, bossId, korName, isInCC);
		}
		return htmltext;
	}
	
	public void teleport(Player player, int bossId, String korName, boolean CC)
	{
		Location TELEPORT_IN_LOC = bossId == 29001 ? new Location(-21583, 180554, -5816) : bossId == 29014 ? new Location(54050, 18435, -5376) : new Location(16698, 111837, -6576);
		final Party party = player.getParty();
		final List<Player> members = CC ? party.getCommandChannel().getMembers() : party.getMembers();
		if (EnterRaidCheck.ConditionCheck(player, CC))
		{
			for (Player member : members)
			{
				if ((bossId == 29001) || (bossId == 29014))
				{
					member.getVariables().set(korName, 1);
				}
				member.teleToLocation(TELEPORT_IN_LOC);
				member.sendMessage(korName + " 레이드 존으로 이동하였습니다.");
				Broadcast.toAllOnlinePlayersOnScreen(party.getLeader().getName() + "님의 파티가 " + korName + " 레이드 존으로 이동하였습니다.");
			}
		}
	}
	
	public int getStatus(int bossId)
	{
		return GrandBossManager.getInstance().getStatus(bossId);
	}
	
	public void showBossLvl(Player player)
	{
		String html = null;
		html = HtmCache.getInstance().getHtm(player, "data/html/guide/RaidManager-level.htm");
		html = html.replace("%Antharas%", Integer.toString(Config.ANTHARAS_MIN_MEMBER));
		html = html.replace("%Baium%", Integer.toString(Config.BAIUM_MIN_MEMBER));
		html = html.replace("%Core%", Integer.toString(Config.CORE_MIN_MEMBER));
		html = html.replace("%Orfen%", Integer.toString(Config.ORFEN_MIN_MEMBER));
		html = html.replace("%Ant%", Integer.toString(Config.QUEEN_ANT_MIN_MEMBER));
		html = html.replace("%Zaken%", Integer.toString(Config.ZAKEN_MIN_MEMBER));
		html = html.replace("%Lilith%", Integer.toString(Config.LILITH_MIN_MEMBER));
		html = html.replace("%Anakim%", Integer.toString(Config.ANAKIM_MIN_MEMBER));
		
		player.sendPacket(new TutorialShowHtml(html));
	}
	
	public void showEpicIndexPage(Player player)
	{
		int[] BOSSES =
		{
			29001,
			29006,
			29014,
			29020,
			29022,
			29068,
			25283,
			25286,
		};
		
		String html = HtmCache.getInstance().getHtm(null, "data/html/guide/epicBoss.htm");
		
		int i = 1;
		for (int boss : BOSSES)
		{
			StatSet stats = GrandBossManager.getInstance().getStatSet(boss);
			String npcNameKor = NpcNameTable.getInstance().getNpcNameKor(boss);
			int isLive = GrandBossManager.getInstance().getStatus(boss);
			long delay = stats.getLong("respawn_time");
			
			html = html.replace("<?name_" + i + "?>", npcNameKor);
			html = html.replace("<?state_" + i + "?>", isLive == 0 ? "<font color=\"99CC33\">가능</font>" : (isLive == 1) ? "<font color=\"99CC33\">진행 전</font>" : (isLive == 2) ? "<font color=\"CC3333\">진행 중</font>" : "<font color=\"FF3333\">" + new SimpleDateFormat("yyyy.MM.dd HH:mm").format(new Date(delay)) + "</font>");
			
			i++;
		}
		player.sendPacket(new TutorialShowHtml(html));
	}
}
