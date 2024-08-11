package handlers.voicedcommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author 보리넷
 */
public class ChangePassword implements IVoicedCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(ChangePassword.class.getName());
	
	private static final String[] VOICED_COMMANDS =
	{
		"비밀번호변경"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (target != null)
		{
			final StringTokenizer st = new StringTokenizer(target);
			try
			{
				String curpass = null;
				String newpass = null;
				String repeatnewpass = null;
				if (st.hasMoreTokens())
				{
					curpass = st.nextToken();
				}
				if (st.hasMoreTokens())
				{
					newpass = st.nextToken();
				}
				if (st.hasMoreTokens())
				{
					repeatnewpass = st.nextToken();
				}
				
				if (!((curpass == null) || (newpass == null) || (repeatnewpass == null)))
				{
					if (!newpass.equals(repeatnewpass))
					{
						activeChar.sendMessage("비밀번호가 일치하지 않습니다.");
						return false;
					}
					if ((newpass.length() < 6) || (newpass.length() > 25))
					{
						activeChar.sendMessage("비밀번호는 6자리 이상 또는 25자리 이하로 설정하셔야 합니다.");
						return false;
					}
					
					LoginServerThread.getInstance().sendChangePassword(activeChar.getAccountName(), activeChar.getName(), curpass, newpass);
				}
				else
				{
					activeChar.sendMessage("다시 입력해 주세요.");
					return false;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("비밀번호 변경을 하지 못했습니다.");
				LOGGER.log(Level.WARNING, "", e);
			}
		}
		else
		{
			// showHTML(activeChar);
			String html = HtmCache.getInstance().getHtm(null, "data/html/mods/ChangePassword.htm");
			if (html == null)
			{
				html = "<html><body><br><br><center><font color=LEVEL>대화파일을 찾을 수 없습니다.</font><br>운영자에게 문의해주세요! </center></body></html>";
			}
			activeChar.sendPacket(new NpcHtmlMessage(html));
			return true;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
