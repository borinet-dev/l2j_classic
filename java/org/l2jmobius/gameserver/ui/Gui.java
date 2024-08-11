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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.l2jmobius.Config;
import org.l2jmobius.commons.ui.DarkTheme;
import org.l2jmobius.commons.ui.LimitLinesDocumentListener;
import org.l2jmobius.commons.ui.SplashScreen;
import org.l2jmobius.gameserver.Shutdown;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.data.xml.PrimeShopData;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author Mobius
 */
public class Gui
{
	JTextArea txtrConsole;
	
	static final String[] shutdownOptions =
	{
		"서버종료",
		"취소"
	};
	static final String[] restartOptions =
	{
		"재시작",
		"취소"
	};
	static final String[] abortOptions =
	{
		"중단",
		"취소"
	};
	static final String[] confirmOptions =
	{
		"확인",
		"취소"
	};
	
	public Gui()
	{
		if (Config.DARK_THEME)
		{
			DarkTheme.activate();
		}
		
		// Initialize console.
		txtrConsole = new JTextArea();
		txtrConsole.setEditable(false);
		txtrConsole.setLineWrap(true);
		txtrConsole.setWrapStyleWord(true);
		txtrConsole.setDropMode(DropMode.INSERT);
		txtrConsole.setFont(new Font("Monospaced", Font.PLAIN, 16));
		txtrConsole.getDocument().addDocumentListener(new LimitLinesDocumentListener(500));
		
		// Initialize menu items.
		final JMenuBar menuBar = new JMenuBar();
		menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		
		final JMenu mnActions = new JMenu("Actions");
		mnActions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnActions);
		
		final JMenuItem mntmShutdown = new JMenuItem("Shutdown");
		mntmShutdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmShutdown.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "게임서버를 종료하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
			{
				final Object answer = JOptionPane.showInputDialog(null, "종료 딜레이(초)를 입력하세요.", Config.SERVER_NAME_KOR, JOptionPane.INFORMATION_MESSAGE, null, null, "600");
				if (answer != null)
				{
					final String input = ((String) answer).trim();
					if (Util.isDigit(input))
					{
						final int delay = Integer.parseInt(input);
						if (delay > 0)
						{
							Shutdown.getInstance().startShutdown(null, delay, false);
						}
					}
				}
			}
		});
		mnActions.add(mntmShutdown);
		
		final JMenuItem mntmRestart = new JMenuItem("Restart");
		mntmRestart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmRestart.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "게임서버를 재시작 하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, restartOptions, restartOptions[1]) == 0)
			{
				final Object answer = JOptionPane.showInputDialog(null, "재시작 딜레이(초)를 입력하세요.", Config.SERVER_NAME_KOR, JOptionPane.INFORMATION_MESSAGE, null, null, "600");
				if (answer != null)
				{
					final String input = ((String) answer).trim();
					if (Util.isDigit(input))
					{
						final int delay = Integer.parseInt(input);
						if (delay > 0)
						{
							Shutdown.getInstance().startShutdown(null, delay, true);
						}
					}
				}
			}
		});
		mnActions.add(mntmRestart);
		
		final JMenuItem mntmAbort = new JMenuItem("Abort");
		mntmAbort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmAbort.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "게임서버종료를 중단하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, abortOptions, abortOptions[1]) == 0)
			{
				Shutdown.getInstance().abort(null);
			}
		});
		mnActions.add(mntmAbort);
		
		final JMenu mnReload = new JMenu("Reload");
		mnReload.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnReload);
		
		final JMenuItem mntmConfigs = new JMenuItem("Configs");
		mntmConfigs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmConfigs.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "컨피그 파일을 리로딩 하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0)
			{
				Config.load(Config.SERVER_MODE);
			}
		});
		mnReload.add(mntmConfigs);
		
		final JMenuItem mntmAccess = new JMenuItem("Access");
		mntmAccess.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmAccess.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "권한 레벨을 리로딩 하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0)
			{
				AdminData.getInstance().load();
			}
		});
		mnReload.add(mntmAccess);
		
		final JMenuItem mntmHtml = new JMenuItem("HTML");
		mntmHtml.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmHtml.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "HTML을 리로딩 하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0)
			{
				HtmCache.getInstance().reload();
			}
		});
		mnReload.add(mntmHtml);
		
		final JMenuItem mntmMultisells = new JMenuItem("Multisells");
		mntmMultisells.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmMultisells.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "multisell을 리로딩 하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0)
			{
				MultisellData.getInstance().load();
			}
		});
		mnReload.add(mntmMultisells);
		
		final JMenuItem mntmBuylists = new JMenuItem("Buylists");
		mntmBuylists.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmBuylists.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "buylist를 리로딩 하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0)
			{
				BuyListData.getInstance().load();
			}
		});
		mnReload.add(mntmBuylists);
		
		final JMenuItem mntmPrimeShop = new JMenuItem("PrimeShop");
		mntmPrimeShop.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmPrimeShop.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "프리미엄 샵을 리로딩 하시겠습니까?", Config.SERVER_NAME_KOR, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0)
			{
				PrimeShopData.getInstance().load();
			}
		});
		mnReload.add(mntmPrimeShop);
		
		final JMenu mnAnnounce = new JMenu("Announce");
		mnAnnounce.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnAnnounce);
		
		final JMenuItem mntmNormal = new JMenuItem("Normal");
		mntmNormal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmNormal.addActionListener(arg0 ->
		{
			final Object input = JOptionPane.showInputDialog(null, "공지사항 입력", Config.SERVER_NAME_KOR, JOptionPane.INFORMATION_MESSAGE, null, null, "");
			if (input != null)
			{
				final String message = ((String) input).trim();
				if (!message.isEmpty())
				{
					Broadcast.toAllOnlinePlayers(message, false);
				}
			}
		});
		mnAnnounce.add(mntmNormal);
		
		final JMenuItem mntmCritical = new JMenuItem("Critical");
		mntmCritical.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmCritical.addActionListener(arg0 ->
		{
			final Object input = JOptionPane.showInputDialog(null, "Critical 공지사항 입력", Config.SERVER_NAME_KOR, JOptionPane.INFORMATION_MESSAGE, null, null, "");
			if (input != null)
			{
				final String message = ((String) input).trim();
				if (!message.isEmpty())
				{
					Broadcast.toAllOnlinePlayers(message, true);
				}
			}
		});
		mnAnnounce.add(mntmCritical);
		
		final JMenu mnFont = new JMenu("Font");
		mnFont.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnFont);
		
		final String[] fonts =
		{
			"16",
			"21",
			"27",
			"33"
		};
		for (String font : fonts)
		{
			final JMenuItem mntmFont = new JMenuItem(font);
			mntmFont.setFont(new Font("Segoe UI", Font.PLAIN, 13));
			mntmFont.addActionListener(arg0 -> txtrConsole.setFont(new Font("Monospaced", Font.PLAIN, Integer.parseInt(font))));
			mnFont.add(mntmFont);
		}
		
		final JMenu mnHelp = new JMenu("Help");
		mnHelp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnHelp);
		
		final JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmAbout.addActionListener(arg0 -> new frmAbout());
		mnHelp.add(mntmAbout);
		
		// Set icons.
		final List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "borinet_16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "borinet_32x32.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "borinet_64x64.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "borinet_128x128.png").getImage());
		
		// Set Panels.
		final JPanel systemPanel = new SystemPanel();
		final JScrollPane scrollPanel = new JScrollPane(txtrConsole);
		scrollPanel.setBounds(0, 0, 1200, 550);
		final JLayeredPane layeredPanel = new JLayeredPane();
		layeredPanel.add(scrollPanel, 0, 0);
		layeredPanel.add(systemPanel, 1, 0);
		
		// Set frame.
		final JFrame frame = new JFrame("와썹 - GameServer");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent ev)
			{
				if (JOptionPane.showOptionDialog(null, "서버를 즉시 종료하시겠습니까?", "서버종료", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
				{
					Shutdown.getInstance().startShutdown(null, 1, false);
				}
			}
		});
		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent ev)
			{
				scrollPanel.setSize(frame.getContentPane().getSize());
				systemPanel.setLocation(frame.getContentPane().getWidth() - systemPanel.getWidth() - 34, systemPanel.getY());
			}
		});
		frame.setJMenuBar(menuBar);
		frame.setIconImages(icons);
		frame.add(layeredPanel, BorderLayout.CENTER);
		frame.getContentPane().setPreferredSize(new Dimension(1200, 550));
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		// Redirect output to text area.
		redirectSystemStreams();
		
		// Show SplashScreen.
		new SplashScreen(".." + File.separator + "images" + File.separator + "borinet.png", 500, frame);
	}
	
	// Set where the text is redirected. In this case, txtrConsole.
	void updateTextArea(String text)
	{
		SwingUtilities.invokeLater(() ->
		{
			txtrConsole.append(text);
			txtrConsole.setCaretPosition(txtrConsole.getText().length());
		});
	}
	
	// Method that manages the redirect.
	private void redirectSystemStreams()
	{
		final OutputStream out = new OutputStream()
		{
			@Override
			public void write(int b)
			{
				updateTextArea(String.valueOf((char) b));
			}
			
			@Override
			public void write(byte[] b, int off, int len)
			{
				updateTextArea(new String(b, off, len));
			}
			
			@Override
			public void write(byte[] b)
			{
				write(b, 0, b.length);
			}
		};
		
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
}
