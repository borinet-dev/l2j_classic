package org.l2jmobius.gameserver.taskmanager.auto;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.util.BorinetHtml;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author 보리넷 가츠
 */
public class AutoItemTaskManager implements Runnable
{
	private static final String URL = "data/html/borinet/AutoUseItem/";
	private static final Set<Player> AUTO_ITEM = ConcurrentHashMap.newKeySet();
	private static final Set<Player> AUTO_POTION = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	int count = 0;
	
	protected AutoItemTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 0, 1000);
	}
	
	@Override
	public void run()
	{
		if (_working && (count < 20))
		{
			count++;
			return;
		}
		count = 0;
		_working = true;
		
		AUTO_ITEM: for (Player player : AUTO_ITEM)
		{
			if ((player == null) || player.isAlikeDead() || (player.isOnlineInt() != 1) || (player.isInOlympiadMode()))
			{
				continue AUTO_ITEM;
			}
			
			final boolean isInPeaceZone = player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.SAYUNE);
			boolean teleporting = player.getQuickVarB("isTeleporting", false);
			if (isInPeaceZone || teleporting)
			{
				continue AUTO_ITEM;
			}
			
			if (player.getVariables().getBoolean("자동아이템_1", false) && !player.isAffectedBySkill(39273))
			{
				final Item item = player.getInventory().getItemByItemId(90499); // 드래곤의 특별한 열매
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_2", false) && !player.isAffectedBySkill(39085))
			{
				final Item item = player.getInventory().getItemByItemId(29817); // 해적단의 특별한 열매
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_3", false) && !player.isAffectedBySkill(39224))
			{
				final Item item = player.getInventory().getItemByItemId(90168); // 딸기 바나나 쉐이크
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_4", false) && !player.isAffectedBySkill(39225))
			{
				final Item item = player.getInventory().getItemByItemId(90169); // 망고 바나나 쉐이크
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_5", false) && !player.isAffectedBySkill(39226))
			{
				final Item item = player.getInventory().getItemByItemId(90170); // 체리 바나나 쉐이크
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_6", false) && !player.isAffectedBySkill(39047))
			{
				final Item item = player.getInventory().getItemByItemId(90439); // 마이트
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_7", false) && !player.isAffectedBySkill(39048))
			{
				final Item item = player.getInventory().getItemByItemId(90441); // 쉴드
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_8", false) && !player.isAffectedBySkill(39049))
			{
				final Item item = player.getInventory().getItemByItemId(90444); // 매직 베리어
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_9", false) && !player.isAffectedBySkill(39050))
			{
				final Item item = player.getInventory().getItemByItemId(90446); // 뱀피릭 레이지
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_10", false) && !player.isAffectedBySkill(39051))
			{
				final Item item = player.getInventory().getItemByItemId(90443); // 엠파워
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_11", false) && !player.isAffectedBySkill(39263))
			{
				final Item item = player.getInventory().getItemByItemId(90449); // 와일드 매직
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_12", false) && !player.isAffectedBySkill(39264))
			{
				final Item item = player.getInventory().getItemByItemId(90451); // 버서커
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_13", false) && !player.isAffectedBySkill(55187))
			{
				final Item item = player.getInventory().getItemByItemId(70040); // STR
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_14", false) && !player.isAffectedBySkill(55188))
			{
				final Item item = player.getInventory().getItemByItemId(70041); // INT
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_15", false) && !player.isAffectedBySkill(55189))
			{
				final Item item = player.getInventory().getItemByItemId(70042); // CON
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_16", false) && !player.isAffectedBySkill(55190))
			{
				final Item item = player.getInventory().getItemByItemId(70043); // DEX
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_17", false) && !player.isAffectedBySkill(55191))
			{
				final Item item = player.getInventory().getItemByItemId(70044); // MEN
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_18", false) && !player.isAffectedBySkill(55192))
			{
				final Item item = player.getInventory().getItemByItemId(70045); // WIT
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_19", false) && !player.isAffectedBySkill(39076))
			{
				final Item item = player.getInventory().getItemByItemId(49501); // 조합원의 보은 1
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_20", false) && !player.isAffectedBySkill(39076))
			{
				final Item item = player.getInventory().getItemByItemId(91391); // 조합원의 보은 2
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_21", false) && !player.isAffectedBySkill(18787))
			{
				final Item item = player.getInventory().getItemByItemId(35669); // 앤젤 캣의 축복
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_22", false) && !player.isAffectedBySkill(39074))
			{
				final Item item = player.getInventory().getItemByItemId(49499); // 앤젤 캣의 기운
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_23", false) && !player.isAffectedBySkill(39171))
			{
				final Item item = player.getInventory().getItemByItemId(49784); // 발터스 기사단의 증표
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_24", false) && !player.isAffectedBySkill(55700))
			{
				final Item item = player.getInventory().getItemByItemId(29014); // 댄스 오브 파이어 주문서
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_25", false) && !player.isAffectedBySkill(55243))
			{
				final Item item = player.getInventory().getItemByItemId(70110); // 댄스 오브 워리어 주문서
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_26", false) && !player.isAffectedBySkill(55244))
			{
				final Item item = player.getInventory().getItemByItemId(70111); // 댄스 오브 미스틱 주문서
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_27", false) && !player.isAffectedBySkill(55699))
			{
				final Item item = player.getInventory().getItemByItemId(29013); // 송 오브 헌터 주문서
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_28", false) && !player.isAffectedBySkill(55000))
			{
				final Item BoostN = player.getInventory().getItemByItemId(29009); // EXP/SP 부스트 주문서 - 일반
				final Item BoostM = player.getInventory().getItemByItemId(29519); // EXP/SP 부스트 주문서 - 중급
				final Item BoostH = player.getInventory().getItemByItemId(29010); // EXP/SP 부스트 주문서 - 상급
				if ((BoostH != null) && (BoostH.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(BoostH.getEtcItem()).useItem(player, BoostH, false);
				}
				else if ((BoostM != null) && (BoostM.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(BoostM.getEtcItem()).useItem(player, BoostM, false);
				}
				else if ((BoostN != null) && (BoostN.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(BoostN.getEtcItem()).useItem(player, BoostN, false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_29", false) && !player.isAffectedBySkill(55061))
			{
				final Item BoostN = player.getInventory().getItemByItemId(29669); // 성 EXP/SP 부스트 주문서 - 일반
				final Item BoostM = player.getInventory().getItemByItemId(29670); // 성 EXP/SP 부스트 주문서 - 중급
				if ((BoostM != null) && (BoostM.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(BoostM.getEtcItem()).useItem(player, BoostM, false);
				}
				else if ((BoostN != null) && (BoostN.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(BoostN.getEtcItem()).useItem(player, BoostN, false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_30", false) && (!player.isAffectedBySkill(17156) || !player.isAffectedBySkill(17161)))
			{
				final Item item = player.getInventory().getItemByItemId(37705); // 킹카의 기운이 감도는 초콜릿 || 킹카의 입맞춤
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_31", false) && (!player.isAffectedBySkill(17157) || !player.isAffectedBySkill(17162)))
			{
				final Item item = player.getInventory().getItemByItemId(37706); // 퀸카의 기운이 감도는 사탕 || 퀸카의 속삭임
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
			if (player.getVariables().getBoolean("자동아이템_32", false) && (!player.isAffectedBySkill(17158) || !player.isAffectedBySkill(17163)))
			{
				final Item item = player.getInventory().getItemByItemId(37707); // 천상천하 유아독존 쿠키 || 유아독존
				if ((item != null) && (item.getCount() > 0))
				{
					ItemHandler.getInstance().getHandler(item.getEtcItem()).useItem(player, item, false);
					// ItemNameTable.autoItemName(player, item.getItemName(), false);
				}
			}
		}
		
		AUTO_POTION: for (Player player : AUTO_POTION)
		{
			if ((player == null) || player.isAlikeDead() || (player.isOnlineInt() != 1) || (player.isInOlympiadMode()))
			{
				continue AUTO_POTION;
			}
			
			final boolean isInPeaceZone = player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.SAYUNE);
			if (isInPeaceZone)
			{
				continue AUTO_POTION;
			}
			
			final int hp_perc = player.getVariables().getInt("AUTO_POTION_HP_PERCENT", 0);
			final int hp_item = player.getVariables().getInt("AUTO_POTION_HP_ITEM", 0);
			final int mp_perc = player.getVariables().getInt("AUTO_POTION_MP_PERCENT", 0);
			final long time_hp = player.getVariables().getLong("AUTO_POTION_HP_TIME", 0);
			final long time_mp = player.getVariables().getLong("AUTO_POTION_MP_TIME", 0);
			
			final boolean restoreHP = ((player.getStatus().getCurrentHp() / player.getMaxHp()) * 100) < hp_perc;
			final boolean restoreMP = ((player.getStatus().getCurrentMp() / player.getMaxMp()) * 100) < mp_perc;
			
			if (time_hp < System.currentTimeMillis())
			{
				final Item hpPotion = player.getInventory().getItemByItemId(hp_item);
				if ((hpPotion != null) && (hpPotion.getCount() > 0))
				{
					if (restoreHP)
					{
						ItemHandler.getInstance().getHandler(hpPotion.getEtcItem()).useItem(player, hpPotion, false);
						player.getVariables().set("AUTO_POTION_HP_TIME", System.currentTimeMillis() + (14000));
					}
				}
			}
			if (time_mp < System.currentTimeMillis())
			{
				final Item mpPotion = player.getInventory().getItemByItemId(728);
				if ((mpPotion != null) && (mpPotion.getCount() > 0))
				{
					if (restoreMP)
					{
						ItemHandler.getInstance().getHandler(mpPotion.getEtcItem()).useItem(player, mpPotion, false);
						player.getVariables().set("AUTO_POTION_MP_TIME", System.currentTimeMillis() + (14000));
					}
				}
			}
		}
		_working = false;
	}
	
	public void addAutoItem(Player player)
	{
		if (!AUTO_ITEM.contains(player))
		{
			AUTO_ITEM.add(player);
		}
	}
	
	public void removeAutoItem(Player player)
	{
		AUTO_ITEM.remove(player);
		player.getVariables().remove("AUTO_ITEM_USE");
	}
	
	public void addAutoPotion(Player player)
	{
		if (!AUTO_POTION.contains(player))
		{
			AUTO_POTION.add(player);
		}
	}
	
	public void removeAutoPotion(Player player)
	{
		AUTO_POTION.remove(player);
		player.getVariables().remove("AUTO_POTION_HP_PERCENT");
		player.getVariables().remove("AUTO_POTION_HP_ITEM");
		player.getVariables().remove("AUTO_POTION_MP_PERCENT");
		player.getVariables().remove("AUTO_POTION_HP_TIME");
		player.getVariables().remove("AUTO_POTION_MP_TIME");
	}
	
	public boolean checkItemRegister(Player player)
	{
		if (player.getVariables().getBoolean("자동아이템_1", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_2", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_3", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_4", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_5", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_6", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_7", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_8", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_9", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_10", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_11", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_12", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_13", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_14", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_15", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_16", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_17", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_18", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_19", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_20", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_21", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_22", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_23", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_24", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_25", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_26", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_27", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_28", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_29", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_30", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_31", false))
		{
			return true;
		}
		else if (player.getVariables().getBoolean("자동아이템_32", false))
		{
			return true;
		}
		return false;
	}
	
	public String showHtml(Player player, int page)
	{
		final int hp_perc = player.getVariables().getInt("AUTO_POTION_HP_PERCENT", 0);
		final int hp_item = player.getVariables().getInt("AUTO_POTION_HP_ITEM", 0);
		final int mp_perc = player.getVariables().getInt("AUTO_POTION_MP_PERCENT", 0);
		
		String itemName = null;
		switch (hp_item)
		{
			case 1539:
				itemName = "강력 체력 회복제";
				break;
			case 1061:
				itemName = "고급 체력 회복제";
				break;
			case 1060:
				itemName = "체력 회복제";
				break;
			case 725:
				itemName = "체력 회복 보조 물약";
				break;
		}
		
		String html = HtmCache.getInstance().getHtm(null, page == 1 ? URL + "index.htm" : URL + "index_2.htm");
		html = html.replace("%isDragon%", player.getVariables().getBoolean("자동아이템_1", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isFirate%", player.getVariables().getBoolean("자동아이템_2", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isStrawberry%", player.getVariables().getBoolean("자동아이템_3", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isMango%", player.getVariables().getBoolean("자동아이템_4", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isCherry%", player.getVariables().getBoolean("자동아이템_5", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isMight%", player.getVariables().getBoolean("자동아이템_6", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isShield%", player.getVariables().getBoolean("자동아이템_7", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isBarrier%", player.getVariables().getBoolean("자동아이템_8", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isVamp%", player.getVariables().getBoolean("자동아이템_9", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isEmpower%", player.getVariables().getBoolean("자동아이템_10", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isWildMagic%", player.getVariables().getBoolean("자동아이템_11", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isBerserker%", player.getVariables().getBoolean("자동아이템_12", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isSTR%", player.getVariables().getBoolean("자동아이템_13", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isINT%", player.getVariables().getBoolean("자동아이템_14", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isCON%", player.getVariables().getBoolean("자동아이템_15", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isDEX%", player.getVariables().getBoolean("자동아이템_16", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isMEN%", player.getVariables().getBoolean("자동아이템_17", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isWIT%", player.getVariables().getBoolean("자동아이템_18", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isGratitude1%", player.getVariables().getBoolean("자동아이템_19", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isGratitude2%", player.getVariables().getBoolean("자동아이템_20", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isBlessAngel%", player.getVariables().getBoolean("자동아이템_21", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isPowerAngel%", player.getVariables().getBoolean("자동아이템_22", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isBalthusMark%", player.getVariables().getBoolean("자동아이템_23", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isFire%", player.getVariables().getBoolean("자동아이템_24", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isWarrior%", player.getVariables().getBoolean("자동아이템_25", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isMystic%", player.getVariables().getBoolean("자동아이템_26", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isHunter%", player.getVariables().getBoolean("자동아이템_27", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isBoostN%", player.getVariables().getBoolean("자동아이템_28", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isBoostC%", player.getVariables().getBoolean("자동아이템_29", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isKingka%", player.getVariables().getBoolean("자동아이템_30", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isQuenka%", player.getVariables().getBoolean("자동아이템_31", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		html = html.replace("%isOnlyMe%", player.getVariables().getBoolean("자동아이템_32", false) ? "<font color=LEVEL>등록 됨</font>" : "<font color=FF0000>미등록</font>");
		
		html = html.replace("%previousPage%", "<Button width=120 ALIGN=LEFT ICON=\"RETURN\" action=\"bypass -h voice .A자동아이템설정\"><font color=FF6600>이전 페이지</font></Button>");
		html = html.replace("%nextPage%", "<Button width=120 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .A자동아이템다음\"><font color=FF6600>다음 페이지</font></Button>");
		html = html.replace("%startAuto%", BorinetUtil.usingAutoItem(player) ? "<Button width=250 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .A자동아이템취소\">자동 아이템 사용을 중단한다</Button>" : "<Button width=250 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .A자동아이템시작\">자동 아이템 사용을 시작한다</Button>");
		// 자동 포션
		html = html.replace("%hp%", hp_perc > 0 ? "<font color=LEVEL>" + hp_perc + "%</font>" : "<edit var=\"perchp\" type=\"numer\" width=120 height=12 length=2 />");
		html = html.replace("%items%", itemName != null ? "<font color=LEVEL>" + itemName + "</font>" : "<combobox var=\"itemhp\" list=강력체력회복제;고급체력회복제;체력회복제;체력회복보조물약 width=120>");
		html = html.replace("%mp%", mp_perc > 0 ? "<font color=LEVEL>" + mp_perc + "%</font>" : "<edit var=\"percmp\" type=\"numer\" width=120 height=12 length=2 />");
		html = html.replace("%startAutoPotion%", hp_perc > 0 ? "<Button width=160 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .A자동아이템포션취소\">자동 포션 사용중단</Button>" : "<Button width=160 ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h voice .A자동아이템포션설정 $perchp $itemhp $percmp\">설정 저장 및 사용</Button>");
		html = html.replace("%mainbanner%", BorinetHtml.getBannerForRace(player));
		
		Util.sendCBHtml(player, html);
		
		return html;
	}
	
	public static AutoItemTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoItemTaskManager INSTANCE = new AutoItemTaskManager();
	}
}
