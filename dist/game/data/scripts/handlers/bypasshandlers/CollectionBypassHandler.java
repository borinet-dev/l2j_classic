package handlers.bypasshandlers;

import org.l2jmobius.gameserver.handler.BypassHandler;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.collections.CollectionHandler;

public class CollectionBypassHandler implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"collection",
		"collection_one",
		"collection_two",
		"collection_three",
		"collection_four",
		"collection_five",
		"collection_six",
		"collection_select",
		"collection_collect"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (command.equals("collection") || command.equals("collection_one"))
		{
			CollectionHandler.showCollectionWindow(player, 1, 30, "첫번째", "collection_one");
		}
		else if (command.equals("collection_two"))
		{
			CollectionHandler.showCollectionWindow(player, 31, 60, "두번째", "collection_two");
		}
		else if (command.equals("collection_three"))
		{
			CollectionHandler.showCollectionWindow(player, 61, 90, "세번째", "collection_three");
		}
		else if (command.equals("collection_four"))
		{
			CollectionHandler.showCollectionWindow(player, 91, 120, "네번째", "collection_four");
		}
		else if (command.equals("collection_five"))
		{
			CollectionHandler.showCollectionWindow(player, 121, 150, "다섯번째", "collection_five");
		}
		else if (command.equals("collection_six"))
		{
			CollectionHandler.showCollectionWindow(player, 151, 180, "여섯번째", "collection_six");
		}
		else if (command.startsWith("collection_select"))
		{
			String[] parts = command.split(" ");
			int collectionId = Integer.parseInt(parts[1]);
			String backCommand = parts.length > 2 ? parts[2] : "collection_one";
			CollectionHandler.showCollectionDetails(player, collectionId, backCommand);
		}
		else if (command.startsWith("collection_collect"))
		{
			String[] parts = command.split(" ");
			int collectionId = Integer.parseInt(parts[1]);
			String backCommand = parts.length > 2 ? parts[2] : "collection_one";
			CollectionHandler.collectItems(player, collectionId);
			CollectionHandler.showCollectionDetails(player, collectionId, backCommand);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
	
	public static void main(String[] args)
	{
		BypassHandler.getInstance().registerHandler(new CollectionBypassHandler());
	}
}