package custom.events.WaterMelon;

import java.util.Calendar;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.Event;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * 왕수박 버프를 관리하는 클래스입니다.
 */
public class WaterMelonBuff extends Event
{
	private static final int HOURS = 1; // 버프 재사용 시간(1 시간)
	private static final String REUSE = WaterMelonBuff.class.getSimpleName() + "_reuse";
	
	public WaterMelonBuff()
	{
		if (Config.WATERMELON_EVENT_ENABLED)
		{
			addStartNpc(WaterMelon.MANAGER);
			addFirstTalkId(WaterMelon.MANAGER);
			addTalkId(WaterMelon.MANAGER);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "give_buff":
			{
				final Item item = player.getInventory().getItemByItemId(47822);
				
				final Calendar starttime = Calendar.getInstance();
				starttime.set(Calendar.MINUTE, 0);
				starttime.set(Calendar.SECOND, 0);
				starttime.set(Calendar.HOUR_OF_DAY, 20);
				
				final Calendar bufftime = Calendar.getInstance();
				bufftime.set(Calendar.MINUTE, 0);
				bufftime.set(Calendar.SECOND, 0);
				bufftime.set(Calendar.HOUR_OF_DAY, 01);
				
				final Calendar endtime = Calendar.getInstance();
				endtime.set(Calendar.MINUTE, 0);
				endtime.set(Calendar.SECOND, 0);
				endtime.set(Calendar.HOUR_OF_DAY, 02);
				
				final long reuse = player.getVariables().getLong(REUSE, 0);
				if (item == null)
				{
					player.sendMessage("푸스 더 캣의 증명사진을 보유한 상태만 이용 가능합니다.");
					htmltext = getHtm(player, "31860-noitem.htm");
				}
				else if ((System.currentTimeMillis() > endtime.getTimeInMillis()) && (System.currentTimeMillis() < starttime.getTimeInMillis()))
				{
					player.sendMessage("푸스 더 캣의 축복은 밤 8시 ~ 새벽 2시까지만 이용 가능합니다.");
					htmltext = getHtm(player, "31860-no.htm");
				}
				else if (reuse > System.currentTimeMillis())
				{
					if ((System.currentTimeMillis() > bufftime.getTimeInMillis()) && (System.currentTimeMillis() < endtime.getTimeInMillis()))
					{
						player.sendMessage("오늘은 더이상 푸스 더 캣의 축복을 받을 수 없을거 같네요.");
						htmltext = getHtm(player, "31860-nextday.htm");
					}
					else
					{
						final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
						final int minutes = (int) ((remainingTime % 3600) / 60);
						player.sendMessage("푸스 더 캣의 축복은 " + minutes + "분 후에 다시 이용 가능합니다.");
						htmltext = getHtm(player, "31860-notime.htm");
						htmltext = htmltext.replace("%mins%", Integer.toString(minutes));
					}
				}
				else
				{
					final Skill PusstheCatBlessing = SkillData.getInstance().getSkill(18794, 1);
					PusstheCatBlessing.applyEffects(player, player, false, 7200); // 3600초(1시간) 동안 버프 적용
					player.getVariables().set(REUSE, System.currentTimeMillis() + (HOURS * 3600000)); // 1시간 재사용 딜레이 설정
					htmltext = "31860-okbuff.htm";
				}
				break;
			}
			case "give_item":
			{
				int account = player.getAccountVariables().getInt("왕수박이벤트", 0);
				if (BorinetUtil.getInstance().checkDB(player, "왕수박이벤트") && (account != 1))
				{
					player.addItem("왕수박이벤트", 47822, 1, player, true);
					BorinetUtil.getInstance().insertDB(player, "왕수박이벤트", 0);
					player.getAccountVariables().set("왕수박이벤트", 1);
				}
				else
				{
					player.sendMessage("계정 및 해당 PC에서 이미 지급받았습니다.");
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public boolean eventBypass(Player player, String bypass)
	{
		return true;
	}
	
	public static void main(String[] args)
	{
		new WaterMelonBuff();
	}
	
	@Override
	public boolean eventStart(Player eventMaker)
	{
		return false;
	}
	
	@Override
	public boolean eventStop()
	{
		return false;
	}
}
