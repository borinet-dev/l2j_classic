package handlers.itemhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ItemSkillType;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.holders.ItemSkillHolder;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.BorinetUtil;

public class ItemDays extends ItemSkillsTemplate
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		final Player player = playable.getActingPlayer();
		String itemName = item.getItemName();
		if ((player != null) && player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_OLYMPIAD_MATCH);
			return false;
		}
		if (!checkUsing(playable.getActingPlayer(), item.getId()))
		{
			playable.getActingPlayer().sendMessage(itemName + "의 재사용 시간은 매일 아침 6시 30분 이후에 사용가능 합니다.");
			return false;
		}
		else if ((item.getId() >= 13010) && (item.getId() <= 13012))
		{
			if (playable.isInInstance())
			{
				playable.getActingPlayer().sendMessage("인스턴트 던전 내에서는 사용할 수 없습니다.");
				return false;
			}
			insertItem(playable.getActingPlayer(), item.getId());
			return super.useItem(playable, item, forceUse);
		}
		return useItems(playable, item);
	}
	
	public static boolean checkUsing(Player player, int itemId)
	{
		String query = "SELECT itemId FROM character_use_item WHERE charId = ? AND itemId = ?";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, itemId);
			
			try (ResultSet rset = ps.executeQuery())
			{
				if (rset.next())
				{
					return false; // 아이템이 이미 사용 중인 경우
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "아이템 사용 여부를 확인하는 도중 오류가 발생했습니다. 플레이어 ID: " + player.getObjectId() + ", 아이템 ID: " + itemId, e);
		}
		
		return true; // 아이템이 사용 중이지 않은 경우
	}
	
	private boolean useItems(Playable playable, Item item)
	{
		final Player player = playable.getActingPlayer();
		if (item.getId() == 41099)
		{
			chanceItem(player, item.getItemName());
			insertItem(playable.getActingPlayer(), item.getId());
			// player.getVariables().set(REUSE, System.currentTimeMillis() + (HOURS * 3600000));
			return false;
		}
		final List<ItemSkillHolder> skills = item.getTemplate().getSkills(ItemSkillType.NORMAL);
		boolean successfulUse = false;
		for (SkillHolder skillInfo : skills)
		{
			if (skillInfo == null)
			{
				continue;
			}
			
			final Skill itemSkill = skillInfo.getSkill();
			if (itemSkill != null)
			{
				if (itemSkill.hasEffectType(EffectType.EXTRACT_ITEM) && !playable.getActingPlayer().isInventoryUnder80(false))
				{
					playable.getActingPlayer().sendMessage("인벤토리 슬롯이 부족합니다.");
					return false;
				}
				SkillCaster.triggerCast(playable, null, itemSkill, item, false);
				insertItem(playable.getActingPlayer(), item.getId());
				successfulUse = true;
			}
		}
		return successfulUse;
	}
	
	public static void insertItem(Player player, int itemId)
	{
		String query = "INSERT INTO character_use_item (charId, itemId) VALUES (?, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, itemId);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
		}
	}
	
	private void chanceItem(Player player, String oldName)
	{
		int chance = Rnd.getR(1, 11);
		
		if (chance == 1)
		{
			Items(player, oldName);
		}
		else if (chance == 2)
		{
			elemental(player, oldName);
		}
		else if (chance == 3)
		{
			int count = Rnd.get(50, 100);
			player.addItem(getClass().getSimpleName(), 41001, count, player, true); // 접속 코인
			sendMessage(player, oldName, 41001, count);
		}
		else if (chance == 4)
		{
			int count = Rnd.get(10, 30);
			player.addItem(getClass().getSimpleName(), 49783, count, player, true); // 행운 주화
			sendMessage(player, oldName, 49783, count);
		}
		else if (chance == 5)
		{
			artifact(player, oldName);
		}
		else if (chance == 6)
		{
			player.addItem(getClass().getSimpleName(), 70106, 1, player, true); // 향상의 룬
			sendMessage(player, oldName, 70106, 1);
		}
		else if (chance == 7)
		{
			player.addItem(getClass().getSimpleName(), 45641, 1, player, true); // 겸치 룬
			sendMessage(player, oldName, 45641, 1);
		}
		else if (chance == 8)
		{
			int count = Rnd.get(100, 200);
			player.addItem(getClass().getSimpleName(), 41000, count, player, true); // 루나
			sendMessage(player, oldName, 41000, count);
		}
		else if (chance == 9)
		{
			player.addItem(getClass().getSimpleName(), 37732, 1, player, true); // 축복 안타귀걸이 (7일)
			sendMessage(player, oldName, 37732, 1);
		}
		else if (chance == 10)
		{
			player.addItem(getClass().getSimpleName(), 37733, 1, player, true); // 축복 발라목걸이 (7일)
			sendMessage(player, oldName, 37733, 1);
		}
		else
		{
			etcItems(player, oldName); // 10% 확률
		}
	}
	
	private void elemental(Player player, String oldName)
	{
		int chance = Rnd.getR(1, 6);
		int count = Rnd.get(20, 50);
		
		if (chance == 1)
		{
			player.addItem(getClass().getSimpleName(), 9552, count, player, true); // 불의 수정
			sendMessage(player, oldName, 9552, count);
		}
		else if (chance == 2)
		{
			player.addItem(getClass().getSimpleName(), 9553, count, player, true); // 불의 수정
			sendMessage(player, oldName, 9553, count);
		}
		else if (chance == 3)
		{
			player.addItem(getClass().getSimpleName(), 9554, count, player, true); // 불의 수정
			sendMessage(player, oldName, 9554, count);
		}
		else if (chance == 4)
		{
			player.addItem(getClass().getSimpleName(), 9555, count, player, true); // 불의 수정
			sendMessage(player, oldName, 9555, count);
		}
		else if (chance == 5)
		{
			player.addItem(getClass().getSimpleName(), 9556, count, player, true); // 불의 수정
			sendMessage(player, oldName, 9556, count);
		}
		else if (chance == 6)
		{
			player.addItem(getClass().getSimpleName(), 9557, count, player, true); // 신성의 수정
			sendMessage(player, oldName, 9557, count);
		}
	}
	
	private void artifact(Player player, String oldName)
	{
		if (Rnd.chance(60))
		{
			int count = Rnd.get(200, 320);
			player.addItem(getClass().getSimpleName(), 41073, count, player, true); // 잊혀진 유물
			sendMessage(player, oldName, 41073, count);
		}
		else
		{
			int count = Rnd.get(100, 150);
			player.addItem(getClass().getSimpleName(), 41079, count, player, true); // 수도원 유물
			sendMessage(player, oldName, 41079, count);
		}
	}
	
	private void etcItems(Player player, String oldName)
	{
		int chance = Rnd.getR(1, 8);
		
		if (chance == 1)
		{
			player.addItem(getClass().getSimpleName(), 13015, 5, player, true); // 자유 텔레포트의 서
			sendMessage(player, oldName, 13015, 5);
		}
		else if (chance == 2)
		{
			player.addItem(getClass().getSimpleName(), 13016, 10, player, true); // 자유 텔레포트의 주문서
			sendMessage(player, oldName, 13016, 10);
		}
		else if (chance == 3)
		{
			player.addItem(getClass().getSimpleName(), 20033, 3, player, true); // 자유 텔레포트의 깃발
			sendMessage(player, oldName, 20033, 3);
		}
		else if (chance == 4)
		{
			player.addItem(getClass().getSimpleName(), 23768, 10, player, true); // 프리미엄 럭키 타로카드
			sendMessage(player, oldName, 23768, 10);
		}
		else if (chance == 5)
		{
			player.addItem(getClass().getSimpleName(), 49845, 10, player, true); // 사이하 축복
			sendMessage(player, oldName, 49845, 10);
		}
		else if (chance == 6)
		{
			player.addItem(getClass().getSimpleName(), 49846, 10, player, true); // 사이하 은빛 축복
			sendMessage(player, oldName, 49846, 10);
		}
		else if (chance == 7)
		{
			player.addItem(getClass().getSimpleName(), 49847, 10, player, true); // 사이하 금빛 축복
			sendMessage(player, oldName, 49847, 10);
		}
		else
		{
			int count = Rnd.get(50, 100);
			player.addItem(getClass().getSimpleName(), 90015, count, player, true); // 최상 생돌
			sendMessage(player, oldName, 90015, count);
		}
	}
	
	private void Items(Player player, String oldName)
	{
		int chance = Rnd.getR(1, 10);
		int chance2 = Rnd.getR(1, 10);
		
		if (chance >= 4)
		{
			if (chance2 >= 6)
			{
				player.addItem(getClass().getSimpleName(), 13991, 1, player, true); // S방어구 상자
				sendMessage(player, oldName, 13991, 1);
			}
			else if (chance >= 4)
			{
				player.addItem(getClass().getSimpleName(), 22340, 1, player, true); // 엘레기아 방어구 상자
				sendMessage(player, oldName, 22340, 1);
			}
			else
			{
				player.addItem(getClass().getSimpleName(), 39331, 1, player, true); // 카데이라 방어구 상자
				sendMessage(player, oldName, 39331, 1);
			}
		}
		else
		{
			if (chance2 >= 6)
			{
				player.addItem(getClass().getSimpleName(), 13990, 1, player, true); // S무기 상자
				sendMessage(player, oldName, 13990, 1);
			}
			else if (chance2 >= 4)
			{
				player.addItem(getClass().getSimpleName(), 22339, 1, player, true); // S최상급 무기 상자
				sendMessage(player, oldName, 22339, 1);
			}
			else
			{
				player.addItem(getClass().getSimpleName(), 39361, 1, player, true); // 아포칼립스 무기 상자
				sendMessage(player, oldName, 39361, 1);
			}
		}
	}
	
	private void sendMessage(Player player, String oldItemName, int newitemId, int count)
	{
		String message = BorinetUtil.getInstance().createMessage(player.getName(), oldItemName, newitemId, count, false);
		BorinetUtil.getInstance().broadcastMessageToAllPlayers(message);
	}
	
	private static final ItemDays _instance = new ItemDays();
	
	public static ItemDays getInstance()
	{
		return _instance;
	}
}
