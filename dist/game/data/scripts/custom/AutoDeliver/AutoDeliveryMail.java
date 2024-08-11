package custom.AutoDeliver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

public class AutoDeliveryMail
{
	private static final Logger LOGGER = Logger.getLogger(AutoDeliveryMail.class.getName());
	
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
						PreparedStatement rss = con.prepareStatement("SELECT id, itemId, itemId2, itemCount, itemCount2, charId FROM items_mail WHERE delivered = 0 AND charId = ?"))
					{
						rss.setInt(1, player.getObjectId());
						try (ResultSet donate = rss.executeQuery())
						{
							while (donate.next())
							{
								String topic;
								String body;
								String charmsg;
								String bodyEnd = "\n\n아이템을 첨부하였으니 반드시 수령하시기 바랍니다.";
								
								switch (donate.getInt("itemId"))
								{
									case 41003:
										topic = "수고하였어요!";
										body = "홍보보상으로 " + Config.SERVER_NAME_KOR + " 홍보 증표를 드립니다!";
										charmsg = "홍보보상이 왔어요. 어서 확인해 주세요!";
										break;
									case 41011:
										topic = "환영합니다!";
										body = "혈맹이전 선물을 드립니다!";
										charmsg = "혈맹이전 선물이 왔어요. 어서 확인해 주세요!";
										break;
									default:
										topic = "선물이 배달 왔어요!";
										body = "선물이 배달 왔어요!!";
										charmsg = "선물이 배달 왔어요. 어서 확인해 주세요!";
										break;
								}
								
								int msgId = IdManager.getInstance().getNextId();
								try (PreparedStatement rssb = con.prepareStatement("UPDATE items_mail SET messageId = ?, delivered = 1 WHERE id = ?"))
								{
									rssb.setInt(1, msgId);
									rssb.setInt(2, donate.getInt("id"));
									rssb.executeUpdate();
								}
								
								Message msg = new Message(msgId, player.getObjectId(), topic, body + bodyEnd, 7, MailType.PRIME_SHOP_GIFT, false);
								Mail attachments = msg.createAttachments();
								attachments.addItem("아이템 메일발송", donate.getInt("itemId"), donate.getInt("itemCount"), null, null);
								attachments.addItem("아이템 메일발송", donate.getInt("itemId2"), donate.getInt("itemCount2"), null, null);
								MailManager.getInstance().sendMessage(msg);
								player.sendMessage(charmsg);
								player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, Config.SERVER_NAME_KOR, charmsg));
							}
						}
					}
					catch (SQLException e)
					{
						LOGGER.log(Level.WARNING, "자동 배송 중 오류가 발생했습니다. 플레이어 ID: " + player.getObjectId(), e);
					}
				}
			}
		}, 0, 10000);
	}
}
