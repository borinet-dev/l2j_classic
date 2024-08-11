package org.l2jmobius.gameserver.instancemanager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.l2jmobius.commons.util.IXmlReader;

public class AutoBuffManager implements IXmlReader
{
	// private static final Logger LOGGER = Logger.getLogger(AutoBuffManager.class.getName());
	public static final Set<Integer> ALLOWED_BUFFS = new HashSet<>();
	
	protected AutoBuffManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		ALLOWED_BUFFS.clear();
		parseDatapackFile("data/AutoBuffData.xml");
		LOGGER.info("자동사냥: " + ALLOWED_BUFFS.size() + "개의 버프스킬을 로드하였습니다.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		final NodeList node = doc.getDocumentElement().getElementsByTagName("skill");
		for (int i = 0; i < node.getLength(); ++i)
		{
			final Element elem = (Element) node.item(i);
			final int skillId = Integer.parseInt(elem.getAttribute("id"));
			ALLOWED_BUFFS.add(skillId);
		}
	}
	
	/**
	 * Gets the single instance of {@code SellBuffsManager}.
	 * @return single instance of {@code SellBuffsManager}
	 */
	public static AutoBuffManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoBuffManager INSTANCE = new AutoBuffManager();
	}
}