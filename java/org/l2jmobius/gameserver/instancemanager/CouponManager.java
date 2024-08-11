package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ShowCouponUI;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * @author 보리넷 가츠
 */
public class CouponManager
{
	private static final Logger LOGGER = Logger.getLogger(CouponManager.class.getName());
	private final Map<String, Coupons> COUPONS = new HashMap<>();
	
	public void loadCoupon()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM coupons");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				// 각 열에서 데이터 가져오기
				String couponId = rset.getString("coupon_id").toUpperCase();
				String rewardItem = rset.getString("reward_item");
				long startTime = rset.getLong("startTime");
				long endTime = rset.getLong("endTime");
				
				// Coupon 객체 생성
				Coupons coupon = new Coupons(rewardItem, startTime, endTime);
				
				// HashMap에 저장
				COUPONS.put(couponId, coupon);
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, "쿠폰 시스템 로드 중 오류가 발생했습니다.", e);
		}
		finally
		{
			LOGGER.info("쿠폰 시스템: " + COUPONS.size() + "개의 쿠폰 정보를 로드하였습니다.");
		}
	}
	
	public class Coupons
	{
		private final String rewardItem;
		private final long startTime;
		private final long endTime;
		
		public Coupons(String rewardItem, long startTime, long endTime)
		{
			this.rewardItem = rewardItem;
			this.startTime = startTime;
			this.endTime = endTime;
		}
		
		public String getRewardItem()
		{
			return rewardItem;
		}
		
		public long getStartTime()
		{
			return startTime;
		}
		
		public long getEndTime()
		{
			return endTime;
		}
	}
	
	public synchronized final void tryUseCoupon(Player player, String id)
	{
		Coupons coupon = COUPONS.get(id);
		if (coupon == null)
		{
			player.sendMessage("유효한 일련번호가 아닙니다. 다시 확인 후 입력해주세요.");
			player.sendPacket(ShowCouponUI.STATIC_PACKET);
			return;
		}
		String rewardItem = coupon.getRewardItem();
		long startTime = coupon.getStartTime();
		long endTime = coupon.getEndTime();
		long current_time = System.currentTimeMillis();
		String eventName = "COUPON_" + id;
		
		if (startTime > current_time)
		{
			player.sendMessage("아직 사용할 수 없는 일련번호 입니다.");
			player.sendPacket(ShowCouponUI.STATIC_PACKET);
			return;
		}
		else if (endTime < current_time)
		{
			player.sendMessage("사용기한이 만료된 일련번호 입니다.");
			player.sendPacket(ShowCouponUI.STATIC_PACKET);
			return;
		}
		int checkGift = player.getAccountVariables().getInt(eventName, 0);
		if (checkGift == 1)
		{
			player.sendPacket(SystemMessageId.THIS_SERIAL_NUMBER_HAS_ALREADY_BEEN_USED);
			player.sendPacket(ShowCouponUI.STATIC_PACKET);
			return;
		}
		String topic = "쿠폰을 사용하여 아이템을 획득했습니다.";
		String body = "아이템을 첨부하였으니 확인하여 수령해 주시기 바랍니다!\n\n" + Config.SERVER_NAME_KOR + "과 함께 즐거운 시간 되시기 바랍니다.\n감사합니다.";
		BorinetUtil.getInstance().sendEventMail(player, topic, body, rewardItem, eventName, false);
	}
	
	public static CouponManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CouponManager INSTANCE = new CouponManager();
	}
}