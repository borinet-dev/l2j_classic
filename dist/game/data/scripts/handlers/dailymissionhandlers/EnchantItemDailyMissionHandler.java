package handlers.dailymissionhandlers;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.item.OnItemEnchant;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.CrystalType;

public class EnchantItemDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;
	private final int _minLevel;
	private final int _isMaster;
	private final CrystalType _minGrade;
	private final String _name;
	
	public EnchantItemDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_amount = holder.getRequiredCompletions();
		_minLevel = holder.getParams().getInt("minLevel", 0);
		_isMaster = holder.getParams().getInt("isMaster", 0);
		final String minGradeStr = holder.getParams().getString("minGrade", "D");
		_minGrade = CrystalType.valueOf(minGradeStr.toUpperCase());
		_name = holder.getName();
	}
	
	@Override
	public void init()
	{
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_ITEM_ENCHANT, (OnItemEnchant event) -> onItemEnchant(event), this));
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		if (entry != null)
		{
			switch (entry.getStatus())
			{
				case 진행중: // Initial state
				{
					if (entry.getProgress() >= _amount)
					{
						entry.setStatus(DailyMissionStatus.수령가능);
						storePlayerEntry(entry);
					}
					break;
				}
				case 수령가능:
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private void onItemEnchant(OnItemEnchant event)
	{
		final Player player = event.getPlayer();
		final Item item = event.getItem();
		int variable = player.getVariables().getInt("강화전도사", 0);
		if ((_isMaster == 2) && (variable == 1))
		{
			return;
		}
		if ((item == null) || (item.getOwnerId() != player.getObjectId()) || (item.getEnchantLevel() <= _minLevel) || (item.getTemplate().getCrystalType().ordinal() < _minGrade.ordinal()))
		{
			return;
		}
		processPlayerProgress(player);
	}
	
	private void processPlayerProgress(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), true);
		if (DailyMissionStatus.수령완료.equals(entry.getStatus()))
		{
			return;
		}
		if (DailyMissionStatus.진행중.equals(entry.getStatus()))
		{
			if (entry.increaseProgress() >= _amount)
			{
				entry.setStatus(DailyMissionStatus.수령가능);
				missionComplete(player, _name);
			}
			if (_isMaster == 1)
			{
				// 1초 지연 후 변수 설정
				ThreadPool.schedule(() ->
				{
					player.getVariables().set("강화전도사", 1);
				}, 1000); // 1초 (단위: 밀리초)
			}
			storePlayerEntry(entry);
		}
	}
}
