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
package org.l2jmobius.gameserver.taskmanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExChangePostState;

/**
 * @author Mobius
 */
public class MessageDeletionTaskManager implements Runnable
{
	private static final Map<Integer, Long> PENDING_MESSAGES = new ConcurrentHashMap<>();
	private static boolean _working = false;
	
	protected MessageDeletionTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 10000, 10000);
	}
	
	@Override
	public void run()
	{
		if (_working)
		{
			return;
		}
		_working = true;
		
		Integer msgId;
		Message msg;
		final long time = System.currentTimeMillis();
		for (Entry<Integer, Long> entry : PENDING_MESSAGES.entrySet())
		{
			if (time > entry.getValue().longValue())
			{
				msgId = entry.getKey();
				msg = MailManager.getInstance().getMessage(msgId.intValue());
				if (msg == null)
				{
					PENDING_MESSAGES.remove(msgId);
					return;
				}
				
				if (msg.hasAttachments())
				{
					final Player receiver = World.getInstance().getPlayer(msg.getReceiverId());
					final Player sender = World.getInstance().getPlayer(msg.getSenderId());
					if (sender != null)
					{
						sender.sendPacket(SystemMessageId.THE_MAIL_WAS_RETURNED_DUE_TO_THE_EXCEEDED_WAITING_TIME);
					}
					if (receiver != null)
					{
						receiver.sendPacket(new ExChangePostState(true, msgId, Message.REJECTED));
						receiver.sendPacket(SystemMessageId.THE_MAIL_WAS_RETURNED_DUE_TO_THE_EXCEEDED_WAITING_TIME);
					}
					MailManager.getInstance().sendMessage(new Message(msg, "대금청구 아이템이 수신대기 시간초과로 반송되었습니다.", "확인 후 반송된 아이템을 수령하시기 바랍니다."));
				}
				MailManager.getInstance().deleteMessageInDb(msgId.intValue());
				PENDING_MESSAGES.remove(msgId);
			}
		}
		
		_working = false;
	}
	
	public void add(int msgId, long deletionTime)
	{
		PENDING_MESSAGES.put(msgId, deletionTime);
	}
	
	public static MessageDeletionTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MessageDeletionTaskManager INSTANCE = new MessageDeletionTaskManager();
	}
}
