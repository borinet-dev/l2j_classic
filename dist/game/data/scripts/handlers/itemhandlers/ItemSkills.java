package handlers.itemhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExVitalityEffectInfo;
import org.l2jmobius.gameserver.taskmanager.auto.AutoLuckyCoinTaskManager;
import org.l2jmobius.gameserver.util.BorinetTask;

public class ItemSkills extends ItemSkillsTemplate
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		final Player player = playable.getActingPlayer();
		if ((player != null) && player.isInOlympiadMode() && !availableItem(item))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_OLYMPIAD_MATCH);
			return false;
		}
		if ((player != null) && player.isOnEvent() && !availableItem(item))
		{
			player.sendMessage("이벤트 참가 중에는 아이템을 사용할 수 없습니다.");
			return false;
		}
		if (player == null)
		{
			return false;
		}
		if ((item.getId() == 15366) || (item.getId() == 15367))
		{
			if (!Config.WATERMELON_EVENT_ENABLED || !BorinetTask.WeekendCheck())
			{
				player.sendMessage("왕수박 이벤트 기간 중에만 " + item.getItemName() + "를 사용할 수 있습니다.");
				return false;
			}
		}
		if (player.isInBoat() && player.isOnBoat())
		{
			player.sendMessage("정기선 탑승 상태에서는 아이템을 사용할 수 없습니다. 잠시 후 다시 시도해주세요.");
			return false;
		}
		
		switch (item.getId())
		{
			case 90730:
			{
				if ((playable.getActingPlayer().getVitalityPoints() + 35000) <= PlayerStat.MAX_VITALITY_POINTS)
				{
					playable.getActingPlayer().setVitalityPoints(playable.getActingPlayer().getVitalityPoints() + 35000, false);
				}
				else
				{
					playable.getActingPlayer().setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS, false);
				}
				playable.getActingPlayer().setVitalityItemsUsed(playable.getActingPlayer().getVitalityItemsUsed() + 1);
				playable.getActingPlayer().sendPacket(new ExVitalityEffectInfo(playable.getActingPlayer()));
				super.useItem(playable, item, forceUse);
				break;
			}
			case 41012:
			case 41013:
			case 41014:
			case 41015:
			{
				final Clan clan = playable.getActingPlayer().getClan();
				if (clan == null)
				{
					playable.getActingPlayer().sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					return false;
				}
				else if (!playable.getActingPlayer().isClanLeader())
				{
					playable.getActingPlayer().sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					return false;
				}
				break;
			}
			case 37886:
			case 49777:
			case 49778:
			case 49779:
			case 49780:
			case 49781:
			case 90917:
			case 90918:
			case 90919:
			case 34742:
			{
				if (player.isSubClassActive())
				{
					player.sendMessage("서브클래스 상태에서는 사용할 수 없습니다.");
					return false;
				}
				break;
			}
			case 49784:
			{
				final long times = 60000 * (Rnd.get(5, 30));
				player.getVariables().set("LuckyCoin", System.currentTimeMillis() + times);
				AutoLuckyCoinTaskManager.getInstance().addAutoLuckyCoin(player);
				break;
			}
			case 90876:
			{
				if (Rnd.chance(3))
				{
					playable.getInventory().addItem("사이하의 은총 상자", 90877, 1, player, null);
					playable.sendMessage("사이하의 금빛 보물을 획득했습니다.");
				}
				else if (Rnd.chance(5))
				{
					playable.getInventory().addItem("사이하의 은총 상자", 90878, 1, player, null);
					playable.sendMessage("사이하의 은빛 보물을 획득했습니다.");
				}
				break;
			}
			case 15474:
			case 15475:
			{
				if (player.getTrainedBeasts() != null)
				{
					int count = player.getTrainedBeasts().size();
					if (count > 0)
					{
						playable.sendMessage("더이상 야수를 조련할 수 없습니다.");
						playable.sendMessage("현재 조련 가능한 야수는 " + count + "마리 입니다.");
						return false;
					}
				}
				break;
			}
			case 41258:
			case 41259:
			{
				final long count = playable.getActingPlayer().getVariables().getLong("Lollipop_" + item.getId(), 0);
				if (ItemDays.checkUsing(playable.getActingPlayer(), item.getId()))
				{
					if (count < 9)
					{
						player.getVariables().set("Lollipop_" + item.getId(), count + 1);
					}
					else
					{
						ItemDays.insertItem(playable.getActingPlayer(), item.getId());
					}
				}
				else
				{
					playable.getActingPlayer().sendMessage(item.getItemName() + "의 하루 사용 제한량을 초과하였습니다.");
					playable.getActingPlayer().sendMessage(item.getItemName() + "의 재사용은 시간은 매일 아침 6시 30분 이후에 사용가능 합니다.");
					return false;
				}
				break;
			}
		}
		
		return super.useItem(playable, item, forceUse);
	}
	
	private boolean availableItem(Item item)
	{
		if (item.getId() == 41235)
		{
			return true;
		}
		return false;
	}
}
