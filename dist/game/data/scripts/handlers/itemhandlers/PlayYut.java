package handlers.itemhandlers;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.ItemNameTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.util.BorinetUtil;
import org.l2jmobius.gameserver.util.KorNameUtil;

public class PlayYut implements IItemHandler
{
	// @formatter:off
	private static final String[] YUT_RESULTS = {"do", "gae", "gur", "yut", "mo"};
	private static final double[] YUT_PROBABILITIES = {0.25, 0.375, 0.25, 0.0625, 0.0625};

	private static final String HTML_PATH = "data/html/item/PlayYut.htm";
    private static final String IMAGE_TAG = "<?img?>";
    private static final int DISPLAY_DURATION = 230; // 각 이미지를 보여주는 시간 (밀리초)
    private static final int TOTAL_DURATION = 3200; // 전체 시간 (밀리초)
	
    private static final int[] ITEM_DO =
    {
    	41002, 3884, 3885, 3881, 3883, 3877, 3887, 49783        
    };
	
	private static final int[] ITEM_GAE =
	{
		5549, 5550, 5551, 5552, 5553, 5554, 9628, 9629, 9630, 9631
	};
	
	private static final int[] ITEM_GUR =
	{
		41263,  
        19259, 19260, 19261, 19262, 19263, 19264, 19265, 19266, 19267,
        19203, 19204, 19205, 19206, 19207, 19208, 19209, 19211, 19212, 19213
	};
	
	private static final int[] ITEM_YUT =
	{
		41262, 41263,  41233, 41234,
		49995, 49996, 49997, 49998, 49999, 90000, 90001, 90002, 90003, 90004, 90005, 90006,
	};
	
	private static final int[] ITEM_MO =
	{
		41263, 41233, 41234, //41264, 
		38890, 38895, 38885, 38880, 38875, 38870, 38865, 38860,
		38850, 26502, 26497, 26486, 26481, 26476
	};
	// @formatter:on
	
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = (Player) playable;
		
		if (player.getInventory().getInventoryItemCount(41262, 0) >= 1)
		{
			boolean playCheck = player.getQuickVarB("playingYut", false);
			if (playCheck)
			{
				player.sendMessage("윷놀이가 진행 중 입니다. 잠시 후 다시 시도하세요.");
			}
			else
			{
				player.destroyItemByItemId("윷놀이", 41262, 1, player, true);
				playYut(player);
			}
		}
		else
		{
			player.sendMessage("필요 아이템이 부족합니다.");
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return true;
	}
	
	private void playYut(Player player)
	{
		Random random = new Random();
		String html = HtmCache.getInstance().getHtm(null, HTML_PATH);
		if ((html == null))
		{
			return;
		}
		player.addQuickVar("playingYut", true);
		
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		// 이미지를 변경하는 작업
		Runnable displayTask = () ->
		{
			int index = random.nextInt(YUT_RESULTS.length);
			String imageUrl = "nIcon_big.ev_new_year_yut_" + YUT_RESULTS[index];
			String updatedHtml = html.replace(IMAGE_TAG, "<img src=\"" + imageUrl + "\" width=64 height=64>").replace("<?result?>", String.valueOf("두구두구두구두구~ 뭐가 나올까~"));
			player.sendPacket(new NpcHtmlMessage(updatedHtml));
		};
		
		// 최종 결과를 출력하는 작업
		Runnable finalTask = () ->
		{
			executorService.shutdown(); // 실행을 중단
			double randomNumber = random.nextDouble();
			double cumulativeProbability = 0.0;
			int index = 0;
			while (index < YUT_PROBABILITIES.length)
			{
				cumulativeProbability += YUT_PROBABILITIES[index];
				if (randomNumber < cumulativeProbability)
				{
					break;
				}
				index++;
			}
			
			String msg = "축하합니다! <font color=LEVEL>" + prize(player, YUT_RESULTS[index]) + "</font> 나왔습니다!";
			String finalImageUrl = "nIcon_big.ev_new_year_yut_" + YUT_RESULTS[index];
			String finalHtml = html.replace(IMAGE_TAG, "<img src=\"" + finalImageUrl + "\" width=64 height=64>").replace("<?result?>", String.valueOf(msg));
			player.sendPacket(new NpcHtmlMessage(finalHtml));
		};
		
		// 이미지 변경 작업을 일정 간격으로 실행
		executorService.scheduleAtFixedRate(displayTask, 0, DISPLAY_DURATION, TimeUnit.MILLISECONDS);
		// 최종 결과를 출력하는 작업을 지정된 시간 이후에 실행
		executorService.schedule(finalTask, TOTAL_DURATION, TimeUnit.MILLISECONDS);
	}
	
	private String prize(Player player, String prize)
	{
		int itemId = 0;
		String name = null;
		boolean isMo = false;
		int counts = 1;
		
		switch (prize)
		{
			case "do":
			{
				itemId = ITEM_DO[Rnd.get(ITEM_DO.length)];
				counts = Rnd.get(1, 3);
				name = "도가";
				break;
			}
			case "gae":
			{
				itemId = ITEM_GAE[Rnd.get(ITEM_GAE.length)];
				counts = Rnd.get(3, 7);
				name = "개가";
				break;
			}
			case "gur":
			{
				itemId = ITEM_GUR[Rnd.get(ITEM_GUR.length)];
				if (itemId == 41263)
				{
					counts = 1;
				}
				else
				{
					counts = Rnd.get(2, 5);
				}
				name = "걸이";
				break;
			}
			case "yut":
			{
				itemId = ITEM_YUT[Rnd.get(ITEM_YUT.length)];
				if ((itemId == 41262) || (itemId == 41263) || (itemId == 41233) || (itemId == 41234))
				{
					counts = 1;
				}
				else
				{
					counts = Rnd.get(5, 10);
				}
				name = "윷이";
				break;
			}
			case "mo":
			{
				itemId = ITEM_MO[Rnd.get(ITEM_MO.length)];
				name = "모가";
				isMo = true;
				break;
			}
		}
		player.sendMessage("축하합니다! " + name + " 나왔습니다!!");
		if (isMo)
		{
			player.addItem("윳놀이", 41262, 1, player, true);
		}
		player.addItem("윳놀이", itemId, counts, player, true);
		player.deleteQuickVar("playingYut");
		
		String message = createMessage(player.getName(), itemId, counts);
		BorinetUtil.getInstance().broadcastMessageToAllPlayers(message);
		return name;
	}
	
	private String createMessage(String playerName, int rewardId, int rewardCount)
	{
		String rewardName = ItemNameTable.getInstance().getItemNameKor(rewardId);
		String itemCountText = rewardCount > 1 ? "] " + rewardCount + "개를" : "]";
		String rewardText = rewardCount > 1 ? rewardName + itemCountText : KorNameUtil.getName(rewardName, "]을", "]를");
		
		return playerName + "님이 윷놀이 게임에서 [" + rewardText + " 획득했습니다!";
	}
}
