package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author 보리넷 가츠
 */
public class CustomClanManager
{
	private static final Logger LOGGER = Logger.getLogger(CustomClanManager.class.getName());
	
	protected CustomClanManager()
	{
		ThreadPool.scheduleAtFixedRate(() ->
		{
			long times = System.currentTimeMillis() - 604800000;
			for (Player player : World.getInstance().getPlayers())
			{
				final Clan clan = player.getClan();
				if (player.getVariables().getLong("신규자혈맹가입", 0) > 1)
				{
					if (player.getVariables().getLong("신규자혈맹가입", 0) < times)
					{
						if ((player.getClan() != null) && (player.getClanId() == 269357273))
						{
							clan.removeClanMember(player.getObjectId(), 0);
							final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED);
							sm.addString(player.getName());
							clan.broadcastToOnlineMembers(sm);
							
							clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(player.getName()));
							clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
							
							clan.broadcastClanStatus();
							Broadcast.toPlayerScreenMessageS(player, "신규자 혈맹에 가입한지 7일이 경과하여 제명되었습니다.");
							player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, "신규자 혈맹에 가입한지 7일이 경과하여 제명되었습니다."));
						}
						player.getVariables().remove("신규자혈맹가입");
					}
				}
			}
			
			final Clan clan = ClanTable.getInstance().getClan(269357273);
			final List<String> names = new ArrayList<>();
			final List<Integer> ids = new ArrayList<>();
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement ps = con.prepareStatement("SELECT char_name, charId FROM characters WHERE online = 0 AND clanid = '269357273' AND charId IN (SELECT charId FROM character_variables WHERE var = '신규자혈맹가입' AND val < " + times + ");"))
				{
					ResultSet rset = ps.executeQuery();
					while (rset.next())
					{
						names.add(rset.getString("char_name"));
						ids.add(rset.getInt("charId"));
					}
				}
				for (int charId : ids)
				{
					clan.removeClanMember(charId, 0);
				}
				for (String name : names)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED);
					sm.addString(name);
					clan.broadcastToOnlineMembers(sm);
					
					clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(name));
					clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
					
					clan.broadcastClanStatus();
				}
				try (Connection conn = DatabaseFactory.getConnection();
					PreparedStatement st1 = conn.prepareStatement("UPDATE characters SET clanid = 0 WHERE clanid = '269357273' AND charId IN (SELECT charId FROM character_variables WHERE var = '신규자혈맹가입' AND val < " + times + ");");
					PreparedStatement st2 = conn.prepareStatement("DELETE FROM character_variables WHERE var = '신규자혈맹가입' AND val < " + times + ";"))
				{
					st1.executeUpdate();
					st2.executeUpdate();
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "신규자 혈맹 아카데미를 정리하지 못했습니다.", e);
			}
		}, Config.CUSTOM_CLAN_MANAGER_DELAY, Config.CUSTOM_CLAN_MANAGER_DELAY);
		
		LOGGER.log(Level.SEVERE, "신규자 혈맹 아카데미 매니저가 로드하였습니다.");
	}
	
	public static CustomClanManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CustomClanManager INSTANCE = new CustomClanManager();
	}
}
