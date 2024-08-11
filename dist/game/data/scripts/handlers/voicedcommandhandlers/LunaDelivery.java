package handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author 보리넷
 */
public class LunaDelivery implements IVoicedCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(LunaDelivery.class.getName());
	final String itemName = ItemNameTable.getInstance().getItemNameKor(Config.LUNA);
	final int itemCount = Integer.parseInt(Util.formatAdena(Config.CHANGE_NAME_PRICE));
	
	private static final String[] VOICED_COMMANDS =
	{
		"루나선물"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/html/guide/lunadelivery.htm");
		if (html == null)
		{
			html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
		}
		activeChar.sendPacket(new NpcHtmlMessage(html));
		return true;
	}
	
	public static void sendLuna(String charName, int luna, String sender)
	{
		String query = "SELECT charId FROM characters WHERE char_name = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setString(1, charName);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int charId = rs.getInt("charId");
					
					int msgId = IdManager.getInstance().getNextId();
					Message msg = new Message(msgId, charId, "선물이 왔습니다.", sender + "님께서 " + luna + "루나를 선물로 보내셨습니다.", 20, MailType.PRIME_SHOP_GIFT, false);
					Mail attachments = msg.createAttachments();
					attachments.addItem("아이템 메일발송", Config.LUNA, luna, null, null);
					MailManager.getInstance().sendMessage(msg);
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "루나 선물을 보내는 도중 오류가 발생했습니다.", e);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
