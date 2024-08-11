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
package org.l2jmobius.gameserver.ui;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.GameServer;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.util.BorinetUtil;

/**
 * @author Mobius
 */
public class SystemPanel extends JPanel
{
	protected static final Logger LOGGER = Logger.getLogger(SystemPanel.class.getName());
	protected static final long START_TIME = System.currentTimeMillis();
	
	public SystemPanel()
	{
		if (!Config.DARK_THEME)
		{
			setBackground(Color.WHITE);
		}
		
		setBounds(500, 20, 284, 178);
		setBorder(new LineBorder(new Color(0, 0, 0), 1, false));
		setOpaque(true);
		setLayout(null);
		
		final JLabel lblProtocol = new JLabel("프로토콜");
		lblProtocol.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblProtocol.setBounds(10, 5, 264, 17);
		add(lblProtocol);
		
		final JLabel lblConnected = new JLabel("현재 접속자");
		lblConnected.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblConnected.setBounds(10, 23, 264, 17);
		add(lblConnected);
		
		final JLabel lblMaxConnected = new JLabel("최대 동시접속자");
		lblMaxConnected.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblMaxConnected.setBounds(10, 41, 264, 17);
		add(lblMaxConnected);
		
		final JLabel lblOfflineShops = new JLabel("오프라인 상점");
		lblOfflineShops.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblOfflineShops.setBounds(10, 59, 264, 17);
		add(lblOfflineShops);
		
		final JLabel lblElapsedTime = new JLabel("구동시간");
		lblElapsedTime.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblElapsedTime.setBounds(10, 77, 264, 17);
		add(lblElapsedTime);
		
		final JLabel lblUseMem = new JLabel("메모리");
		lblUseMem.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblUseMem.setBounds(10, 95, 264, 17);
		add(lblUseMem);
		
		final JLabel lblRevision = new JLabel("Build JDK");
		lblRevision.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblRevision.setBounds(10, 113, 264, 17);
		add(lblRevision);
		
		final JLabel lblServiceDays = new JLabel("총 운영기간");
		lblServiceDays.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblServiceDays.setBounds(10, 131, 264, 17);
		add(lblServiceDays);
		
		final JLabel lblNewServiceDays = new JLabel("현시즌 기간");
		lblNewServiceDays.setFont(new Font("Monospaced", Font.PLAIN, 16));
		lblNewServiceDays.setBounds(10, 149, 264, 17);
		add(lblNewServiceDays);
		
		BorinetUtil.getInstance();
		// Set initial values.
		lblProtocol.setText("프로토콜: 클래식 [152]");
		lblRevision.setText("라이센스: 검사 중");
		lblServiceDays.setText("총 운영기간: " + BorinetUtil.serviceDays(true) + "일");
		lblNewServiceDays.setText("현시즌 기간: " + BorinetUtil.serviceDays(false) + "일");
		
		// Repeating elapsed time task.
		new Timer().scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				BorinetUtil.getCounts();
				double usedMem = BorinetUtil.getInstance().getUsedMemoryGB();
				long totalMem = BorinetUtil.getInstance().getTotalMemoryGB();
				lblConnected.setText("현재 접속자: " + BorinetUtil.online + "명");
				lblMaxConnected.setText("최대 동시접속자: " + World.MAX_CONNECTED_COUNT + "명");
				lblOfflineShops.setText("오프 상점: " + BorinetUtil.offline + "명 | 낚시: " + BorinetUtil.fishing + "명");
				lblElapsedTime.setText("구동시간: " + getDurationBreakdown(System.currentTimeMillis() - START_TIME));
				lblUseMem.setText("메모리(GB): " + usedMem + " / " + totalMem + " 사용 중");
				
				try (Connection con = DatabaseFactory.getConnection();
					PreparedStatement ps = con.prepareStatement("UPDATE web_connect SET offline=" + BorinetUtil.offline + ", used_mem=" + usedMem + ", total_mem=" + totalMem))
				{
					ps.execute();
				}
				catch (Exception e)
				{
				}
			}
		}, 1000, 1000);
		
		// 라이센스 체크
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				lblRevision.setText("라이센스: " + (GameServer.masterServer ? "마스터 서버" : (GameServer.checkInternet ? BorinetUtil.getInstance().CheckLicense() : "")));
			}
		}, 2000);
		
		new Timer().scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				// 라이센스 체크
				lblRevision.setText("라이센스: " + (GameServer.masterServer ? "마스터 서버" : (GameServer.checkInternet ? BorinetUtil.getInstance().CheckLicense() : "")));
				// 운영일수 체크
				lblServiceDays.setText("총 운영기간: " + BorinetUtil.serviceDays(true) + "일");
				lblNewServiceDays.setText("현시즌 기간: " + BorinetUtil.serviceDays(false) + "일");
			}
		}, service(), 86400000);
	}
	
	private long service()
	{
		final long currentTime = System.currentTimeMillis();
		// Schedule reset everyday at 6:30.
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 6);
			calendar.set(Calendar.MINUTE, 30);
			calendar.set(Calendar.SECOND, 0);
		}
		
		final long startDelay = Math.max(0, calendar.getTimeInMillis() - currentTime);
		
		return startDelay;
	}
	
	static String getDurationBreakdown(long millis)
	{
		long remaining = millis;
		final long days = TimeUnit.MILLISECONDS.toDays(remaining);
		remaining -= TimeUnit.DAYS.toMillis(days);
		final long hours = TimeUnit.MILLISECONDS.toHours(remaining);
		remaining -= TimeUnit.HOURS.toMillis(hours);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
		remaining -= TimeUnit.MINUTES.toMillis(minutes);
		final long seconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
		return (days + "일 " + hours + "시간 " + minutes + "분 " + seconds + "초");
	}
}
