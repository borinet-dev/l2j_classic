/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.enums.AttributeType;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;

/**
 * @author Migi, DS
 */
public class Message
{
	private static final int EXPIRATION = 7; // 15 days
	private static final int ONE_DAY = 86400000; // 1일
	private static final int COD_EXPIRATION = 12; // 12 hours
	
	// post state
	public static final int DELETED = 0;
	public static final int READED = 1;
	public static final int REJECTED = 2;
	
	private final int _messageId;
	private final int _senderId;
	private final int _receiverId;
	private final long _expiration;
	private String _senderName = null;
	private String _receiverName = null;
	private final String _subject;
	private final String _content;
	private boolean _unread;
	private boolean _returned;
	private MailType _messageType = MailType.REGULAR;
	private boolean _deletedBySender;
	private boolean _deletedByReceiver;
	private final long _reqAdena;
	private boolean _hasAttachments;
	private Mail _attachments = null;
	
	private int _itemId;
	private int _enchantLvl;
	private final int[] _elementals = new int[6];
	
	/*
	 * Constructor for restoring from DB.
	 */
	public Message(ResultSet rset) throws SQLException
	{
		_messageId = rset.getInt("messageId");
		_senderId = rset.getInt("senderId");
		_receiverId = rset.getInt("receiverId");
		_subject = rset.getString("subject");
		_content = rset.getString("content");
		_expiration = rset.getLong("expiration");
		_reqAdena = rset.getLong("reqAdena");
		_hasAttachments = rset.getBoolean("hasAttachments");
		_unread = rset.getBoolean("isUnread");
		_deletedBySender = rset.getBoolean("isDeletedBySender");
		_deletedByReceiver = rset.getBoolean("isDeletedByReceiver");
		_messageType = MailType.values()[rset.getInt("sendBySystem")];
		_returned = rset.getBoolean("isReturned");
		_itemId = rset.getInt("itemId");
		_enchantLvl = rset.getInt("enchantLvl");
		final String elemental = rset.getString("elementals");
		if (elemental != null)
		{
			final String[] elemDef = elemental.split(";");
			for (int i = 0; i < 6; i++)
			{
				_elementals[i] = Integer.parseInt(elemDef[i]);
			}
		}
	}
	
	/*
	 * 아이템 발송 모듈
	 */
	public Message(int msgId, int receiverId, String subject, String content, int days, MailType sendBySystem, boolean returned)
	{
		_messageId = msgId;
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + (days * ONE_DAY);
		_reqAdena = 0;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = sendBySystem;
		_returned = returned;
	}
	
	/*
	 * 루나 구매요청 발송 모듈
	 */
	public Message(int msgId, int receiverId, long expiration, String subject, String content, MailType sendBySystem, boolean returned)
	{
		_messageId = msgId;
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = expiration + (3600000);
		_reqAdena = 0;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = sendBySystem;
		_returned = returned;
	}
	
	/*
	 * This constructor used for creating new message.
	 */
	public Message(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena)
	{
		_messageId = IdManager.getInstance().getNextId();
		_senderId = senderId;
		_receiverId = receiverId;
		_subject = subject;
		_content = text;
		_expiration = (isCod ? System.currentTimeMillis() + (COD_EXPIRATION * 3600000) : System.currentTimeMillis() + (EXPIRATION * 86400000));
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = false;
		_deletedByReceiver = false;
		_reqAdena = reqAdena;
		_messageType = MailType.REGULAR;
	}
	
	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, MailType sendBySystem)
	{
		_messageId = IdManager.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + (EXPIRATION * 86400000);
		_reqAdena = 0;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = sendBySystem;
		_returned = false;
	}
	
	/*
	 * This constructor is used for creating new System message
	 */
	public Message(int senderId, int receiverId, String subject, String content, MailType sendBySystem)
	{
		_messageId = IdManager.getInstance().getNextId();
		_senderId = senderId;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + (EXPIRATION * 86400000);
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_reqAdena = 0;
		_messageType = sendBySystem;
	}
	
	/*
	 * This constructor used for auto-generation of the "return attachments" message
	 */
	public Message(Message msg, String reciverName)
	{
		_messageId = IdManager.getInstance().getNextId();
		_senderId = msg.getSenderId();
		_receiverId = msg.getSenderId();
		_subject = reciverName + "님이 우편을 반송하였습니다.";
		_content = "";
		_expiration = System.currentTimeMillis() + (EXPIRATION * 86400000);
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = MailType.REGULAR;
		_returned = true;
		_reqAdena = 0;
		_hasAttachments = true;
		_attachments = msg.getAttachments();
		msg.removeAttachments();
		_attachments.setNewMessageId(_messageId);
	}
	
	/*
	 * 대금청구 아이템 반송 모듈
	 */
	public Message(Message msg, String subject, String content)
	{
		_messageId = IdManager.getInstance().getNextId();
		_senderId = msg.getSenderId();
		_receiverId = msg.getSenderId();
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + (EXPIRATION * 86400000);
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = MailType.REGULAR;
		_returned = true;
		_reqAdena = 0;
		_hasAttachments = true;
		_attachments = msg.getAttachments();
		_attachments.setNewMessageId(_messageId);
		msg.removeAttachments();
		_returned = false;
	}
	
	public Message(int receiverId, String subject, String body, String bodyEnd, Item item, MailType mailType)
	{
		_messageId = IdManager.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = body + item.getName() + bodyEnd;
		_expiration = System.currentTimeMillis() + (EXPIRATION * 86400000);
		_reqAdena = 0;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = mailType;
		_returned = false;
		if (mailType == MailType.COMMISSION_SOLD_ITEM)
		{
			_hasAttachments = false;
			_itemId = item.getId();
			_enchantLvl = item.getEnchantLevel();
			if (item.isArmor())
			{
				for (AttributeType type : AttributeType.ATTRIBUTE_TYPES)
				{
					_elementals[type.getClientId()] = item.getDefenceAttribute(type);
				}
			}
			else if (item.isWeapon() && (item.getAttackAttributeType() != AttributeType.NONE))
			{
				_elementals[item.getAttackAttributeType().getClientId()] = item.getAttackAttributePower();
			}
		}
		else if (mailType == MailType.COMMISSION_RETURN_ITEM)
		{
			final Mail attachement = createAttachments();
			attachement.addItem("CommissionReturnItem", item, null, null);
		}
	}
	
	public static PreparedStatement getStatement(Message msg, Connection con) throws SQLException
	{
		final PreparedStatement stmt = con.prepareStatement("INSERT INTO messages (messageId, senderId, receiverId, subject, content, expiration, reqAdena, hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, sendBySystem, isReturned, itemId, enchantLvl, elementals) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		stmt.setInt(1, msg._messageId);
		stmt.setInt(2, msg._senderId);
		stmt.setInt(3, msg._receiverId);
		stmt.setString(4, msg._subject);
		stmt.setString(5, msg._content);
		stmt.setLong(6, msg._expiration);
		stmt.setLong(7, msg._reqAdena);
		stmt.setString(8, String.valueOf(msg._hasAttachments));
		stmt.setString(9, String.valueOf(msg._unread));
		stmt.setString(10, String.valueOf(msg._deletedBySender));
		stmt.setString(11, String.valueOf(msg._deletedByReceiver));
		stmt.setInt(12, msg._messageType.ordinal());
		stmt.setString(13, String.valueOf(msg._returned));
		stmt.setInt(14, msg._itemId);
		stmt.setInt(15, msg._enchantLvl);
		stmt.setString(16, msg._elementals[0] + ";" + msg._elementals[1] + ";" + msg._elementals[2] + ";" + msg._elementals[3] + ";" + msg._elementals[4] + ";" + msg._elementals[5]);
		return stmt;
	}
	
	public int getId()
	{
		return _messageId;
	}
	
	public int getSenderId()
	{
		return _senderId;
	}
	
	public int getReceiverId()
	{
		return _receiverId;
	}
	
	public final String getSenderName()
	{
		switch (_messageType)
		{
			case REGULAR:
			{
				_senderName = CharInfoTable.getInstance().getNameById(_senderId);
				break;
			}
			case PRIME_SHOP_GIFT: // Not in client, tbd
			case SERVER:
			{
				_senderName = "*" + Config.SERVER_NAME_KOR + "*";
				break;
			}
			case COMMISSION_SOLD_ITEM:
			case COMMISSION_RETURN_ITEM:
			{
				_senderName = "판매 대행 관리자";
				break;
			}
			case NEWS_INFORMER: // Handled by Sysstring in client
			case NPC: // Handled by NpcName in client
			case BIRTHDAY: // Handled by Sysstring in client
			case COMMISSION_ITEM_SOLD: // Handled by Sysstring in client
			case COMMISSION_ITEM_RETURNED: // Handled by Sysstring in client
			case MENTOR_NPC: // Handled in client
			default:
			{
				_senderName = "알 수 없음";
				break;
			}
		}
		return _senderName;
	}
	
	public final String getReceiverName()
	{
		if (_receiverName == null)
		{
			_receiverName = CharInfoTable.getInstance().getNameById(_receiverId);
			if (_receiverName == null)
			{
				_receiverName = "";
			}
		}
		return _receiverName;
	}
	
	public String getSubject()
	{
		return _subject;
	}
	
	public String getContent()
	{
		return _content;
	}
	
	public boolean isLocked()
	{
		return _reqAdena > 0;
	}
	
	public long getExpiration()
	{
		return _expiration;
	}
	
	public int getExpirationSeconds()
	{
		return (int) (_expiration / 1000);
	}
	
	public boolean isUnread()
	{
		return _unread;
	}
	
	public void markAsRead()
	{
		if (_unread)
		{
			_unread = false;
			MailManager.getInstance().markAsReadInDb(_messageId);
		}
	}
	
	public boolean isDeletedBySender()
	{
		return _deletedBySender;
	}
	
	public void setDeletedBySender()
	{
		if (!_deletedBySender)
		{
			_deletedBySender = true;
			if (_deletedByReceiver)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedBySenderInDb(_messageId);
			}
		}
	}
	
	public boolean isDeletedByReceiver()
	{
		return _deletedByReceiver;
	}
	
	public void setDeletedByReceiver()
	{
		if (!_deletedByReceiver)
		{
			_deletedByReceiver = true;
			if (_deletedBySender)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedByReceiverInDb(_messageId);
			}
		}
	}
	
	public MailType getMailType()
	{
		return _messageType;
	}
	
	public boolean isReturned()
	{
		return _returned;
	}
	
	public void setReturned(boolean value)
	{
		_returned = value;
	}
	
	public long getReqAdena()
	{
		return _reqAdena;
	}
	
	public synchronized Mail getAttachments()
	{
		if (!_hasAttachments)
		{
			return null;
		}
		
		if (_attachments == null)
		{
			_attachments = new Mail(_senderId, _messageId);
			_attachments.restore();
		}
		
		return _attachments;
	}
	
	public boolean hasAttachments()
	{
		return _hasAttachments;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getEnchantLvl()
	{
		return _enchantLvl;
	}
	
	public int[] getElementals()
	{
		return _elementals;
	}
	
	public synchronized void removeAttachments()
	{
		if (_attachments != null)
		{
			_attachments = null;
			_hasAttachments = false;
			MailManager.getInstance().removeAttachmentsInDb(_messageId);
		}
	}
	
	public synchronized Mail createAttachments()
	{
		if (_hasAttachments || (_attachments != null))
		{
			return null;
		}
		
		_attachments = new Mail(_senderId, _messageId);
		_hasAttachments = true;
		return _attachments;
	}
	
	protected final synchronized void unloadAttachments()
	{
		if (_attachments != null)
		{
			_attachments.deleteMe();
			MailManager.getInstance().removeAttachmentsInDb(_messageId);
			_attachments = null;
		}
	}
}
