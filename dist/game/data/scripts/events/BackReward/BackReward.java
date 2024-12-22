package events.BackReward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.Util;

public class BackReward extends LongTimeEvent
{
	private static final int NPC = 40032;
	
	public BackReward()
	{
		if (isBackRewardActive())
		{
			addStartNpc(NPC);
			addFirstTalkId(NPC);
			addTalkId(NPC);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "reward":
			{
				int rewardItem = player.getVariables().getInt("백섭 보상", 0);
				
				if (rewardItem >= 1)
				{
					player.sendMessage("이미 보상을 수령하였습니다.");
					return null;
				}
				
				// Database connection with try-with-resources
				String query = "SELECT charId, createDate FROM characters WHERE STR_TO_DATE(createDate, '%Y-%m-%d') < '2024-10-01' AND charId = ?";
				try (Connection con = DatabaseFactory.getConnection();
					PreparedStatement ps = con.prepareStatement(query))
				{
					ps.setInt(1, player.getObjectId());
					try (ResultSet rs = ps.executeQuery())
					{
						if (rs.next())
						{
							rewardItems(player);
						}
						else
						{
							player.sendMessage("보상을 받을 조건에 맞지 않습니다.");
						}
					}
				}
				catch (SQLException e)
				{
					// 예외를 로그로 남기는 방식으로 변경
					System.err.println("Database error while processing reward event: " + e.getMessage());
					e.printStackTrace();
				}
				return null;
			}
			default:
				return event;
		}
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	private boolean isBackRewardActive()
	{
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = LocalDateTime.of(now.getYear(), Month.OCTOBER, 1, 7, 0); // 10월 1일 오전 7시
		LocalDateTime end = LocalDateTime.of(now.getYear(), Month.NOVEMBER, 1, 0, 0); // 10월 30일 오후 24시
		
		return now.isAfter(start) && now.isBefore(end);
	}
	
	private void rewardItems(Player player)
	{
		String topic = "백섭 보상";
		String body = "아이템 백업을 사과드리며 소정의 보상을 드립니다!\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
		String items = "41025,1;35669,10;41249,1;70106,1;91375,1;91376,1;91377,1;41000,100";
		
		sendEventMail(player, topic, body, items, "백섭 보상", false);
	}
	
	public void sendEventMail(Player player, String topic, String body, String items, String evenName, boolean DBsave)
	{
		if (player == null)
		{
			// player가 null일 때 로그를 남기거나 추가 처리를 할 수 있습니다.
			return;
		}
		int msgId = IdManager.getInstance().getNextId();
		String charMsg = "백섭 보상이 왔어요. 어서 확인해 주세요!";
		
		Message msg = new Message(msgId, player.getObjectId(), topic, body, 7, MailType.PRIME_SHOP_GIFT, false);
		final List<ItemHolder> itemHolders = new ArrayList<>();
		for (String str : items.split(";"))
		{
			if (str.contains(","))
			{
				final String itemId = str.split(",")[0];
				final String itemCount = str.split(",")[1];
				if (Util.isDigit(itemId) && Util.isDigit(itemCount))
				{
					itemHolders.add(new ItemHolder(Integer.parseInt(itemId), Long.parseLong(itemCount)));
				}
			}
			else if (Util.isDigit(str))
			{
				itemHolders.add(new ItemHolder(Integer.parseInt(str), 1));
			}
		}
		if (!itemHolders.isEmpty())
		{
			final Mail attachments = msg.createAttachments();
			for (ItemHolder itemHolder : itemHolders)
			{
				attachments.addItem(evenName + " 메일발송", itemHolder.getId(), itemHolder.getCount(), player, null);
			}
		}
		MailManager.getInstance().sendMessage(msg);
		player.sendMessage("" + charMsg);
		player.sendPacket(new CreatureSay(player, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, charMsg));
		player.getVariables().set(evenName, 1);
	}
	
	public static void main(String[] args)
	{
		new BackReward();
	}
}
