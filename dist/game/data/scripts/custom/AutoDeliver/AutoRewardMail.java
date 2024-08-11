package custom.AutoDeliver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.util.Util;

public class AutoRewardMail
{
	private static final Logger LOGGER = Logger.getLogger(AutoRewardMail.class.getName());
	
	public static void main(String[] args)
	{
		ThreadPool.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				autoDeliver();
			}
			
			private void autoDeliver()
			{
				for (Player player : World.getInstance().getPlayers())
				{
					try (Connection con = DatabaseFactory.getConnection();
						PreparedStatement ps = con.prepareStatement("SELECT id, charId FROM items_reward_mail WHERE delivered = 0 AND charId = ?"))
					{
						ps.setInt(1, player.getObjectId());
						
						try (ResultSet rs = ps.executeQuery())
						{
							while (rs.next())
							{
								String topic = "축하합니다!";
								String body = "미니게임에서 우승하신것을 축하합니다!";
								String bodyEnd = "\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
								String charmsg = "미니게임 우승 상품이 왔습니다. 어서 확인해 주세요!";
								String items = Config.MINIGAME_REWARD_ITEM;
								
								int msgId = IdManager.getInstance().getNextId();
								
								try (PreparedStatement psUpdate = con.prepareStatement("UPDATE items_reward_mail SET messageId = ?, delivered = 1 WHERE id = ?"))
								{
									psUpdate.setInt(1, msgId);
									psUpdate.setInt(2, rs.getInt("id"));
									psUpdate.executeUpdate();
								}
								
								Message msg = new Message(msgId, player.getObjectId(), topic, body + bodyEnd, 7, MailType.PRIME_SHOP_GIFT, false);
								List<ItemHolder> itemHolders = parseItems(items);
								
								if (!itemHolders.isEmpty())
								{
									Mail attachments = msg.createAttachments();
									for (ItemHolder itemHolder : itemHolders)
									{
										attachments.addItem("미니게임 우승자 메일발송", itemHolder.getId(), itemHolder.getCount(), null, null);
									}
								}
								
								MailManager.getInstance().sendMessage(msg);
								player.sendMessage(charmsg);
								player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, charmsg));
							}
						}
					}
					catch (SQLException e)
					{
						LOGGER.log(Level.WARNING, "보상 메일을 자동으로 보내는 도중 오류가 발생했습니다. 플레이어 ID: " + player.getObjectId(), e);
					}
				}
			}
			
			private List<ItemHolder> parseItems(String items)
			{
				List<ItemHolder> itemHolders = new ArrayList<>();
				for (String str : items.split(";"))
				{
					if (str.contains(","))
					{
						String[] parts = str.split(",");
						String itemId = parts[0];
						String itemCount = parts[1];
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
				return itemHolders;
			}
		}, 0, 10000);
	}
}
