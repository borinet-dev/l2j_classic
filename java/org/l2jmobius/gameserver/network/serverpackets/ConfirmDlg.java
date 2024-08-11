package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage.SMParam;

/**
 * ConfirmDlg server packet implementation.
 */
public class ConfirmDlg implements IClientOutgoingPacket
{
	private int _time;
	private int _requesterId;
	private final SystemMessage _systemMessage;
	private Runnable _yesAction;
	
	public ConfirmDlg(SystemMessageId smId)
	{
		_systemMessage = new SystemMessage(smId);
	}
	
	public ConfirmDlg(int id)
	{
		_systemMessage = new SystemMessage(id);
	}
	
	public ConfirmDlg(String text)
	{
		_systemMessage = new SystemMessage(SystemMessageId.S1_3);
		_systemMessage.addString(text);
	}
	
	public ConfirmDlg addTime(int time)
	{
		_time = time;
		return this;
	}
	
	public ConfirmDlg addRequesterId(int id)
	{
		_requesterId = id;
		return this;
	}
	
	public ConfirmDlg setYesAction(Runnable action)
	{
		_yesAction = action;
		return this;
	}
	
	public SystemMessage getSystemMessage()
	{
		return _systemMessage;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CONFIRM_DLG.writeId(packet);
		final SMParam[] params = _systemMessage.getParams();
		packet.writeD(_systemMessage.getId());
		packet.writeD(params.length);
		for (SMParam param : params)
		{
			packet.writeD(param.getType());
			switch (param.getType())
			{
				case SystemMessage.TYPE_ELEMENT_NAME:
				case SystemMessage.TYPE_BYTE:
				case SystemMessage.TYPE_FACTION_NAME:
				case SystemMessage.TYPE_ELEMENTAL_SPIRIT:
				{
					packet.writeC(param.getIntValue());
					break;
				}
				case SystemMessage.TYPE_CASTLE_NAME:
				case SystemMessage.TYPE_SYSTEM_STRING:
				case SystemMessage.TYPE_INSTANCE_NAME:
				case SystemMessage.TYPE_CLASS_ID:
				{
					packet.writeH(param.getIntValue());
					break;
				}
				case SystemMessage.TYPE_ITEM_NAME:
				case SystemMessage.TYPE_INT_NUMBER:
				case SystemMessage.TYPE_NPC_NAME:
				case SystemMessage.TYPE_DOOR_NAME:
				{
					packet.writeD(param.getIntValue());
					break;
				}
				case SystemMessage.TYPE_LONG_NUMBER:
				{
					packet.writeQ(param.getLongValue());
					break;
				}
				case SystemMessage.TYPE_TEXT:
				case SystemMessage.TYPE_PLAYER_NAME:
				{
					packet.writeS(param.getStringValue());
					break;
				}
				case SystemMessage.TYPE_SKILL_NAME:
				{
					final int[] array = param.getIntArrayValue();
					packet.writeD(array[0]); // skill id
					packet.writeH(array[1]); // skill level
					packet.writeH(array[2]); // skill sub level
					break;
				}
				case SystemMessage.TYPE_POPUP_ID:
				case SystemMessage.TYPE_ZONE_NAME:
				{
					final int[] array = param.getIntArrayValue();
					packet.writeD(array[0]); // x
					packet.writeD(array[1]); // y
					packet.writeD(array[2]); // z
					break;
				}
			}
		}
		packet.writeD(_time);
		packet.writeD(_requesterId);
		return true;
	}
	
	public void doYesAction()
	{
		if (_yesAction != null)
		{
			_yesAction.run();
		}
	}
}
