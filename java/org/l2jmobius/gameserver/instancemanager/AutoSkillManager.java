package org.l2jmobius.gameserver.instancemanager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.l2jmobius.commons.util.IXmlReader;

public class AutoSkillManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(AutoSkillManager.class.getName());
	public static final Set<Integer> ALLOWED_SKILLS = new HashSet<>();
	
	protected AutoSkillManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		ALLOWED_SKILLS.clear();
		parseDatapackFile("data/AutoSkillData.xml");
		LOGGER.info("자동사냥: " + ALLOWED_SKILLS.size() + "개의 공격스킬을 로드하였습니다.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		final NodeList node = doc.getDocumentElement().getElementsByTagName("skill");
		for (int i = 0; i < node.getLength(); ++i)
		{
			final Element elem = (Element) node.item(i);
			final int skillId = Integer.parseInt(elem.getAttribute("id"));
			ALLOWED_SKILLS.add(skillId);
		}
	}
	
	/**
	 * Gets the single instance of {@code SellBuffsManager}.
	 * @return single instance of {@code SellBuffsManager}
	 */
	public static AutoSkillManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSkillManager INSTANCE = new AutoSkillManager();
	}
}