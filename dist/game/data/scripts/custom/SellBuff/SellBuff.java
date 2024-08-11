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
package custom.SellBuff;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.handler.BypassHandler;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.handler.VoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.SellBuffsManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.AbstractScript;
import org.l2jmobius.gameserver.model.holders.SellBuffHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;
import org.l2jmobius.gameserver.util.Util;

/**
 * Sell Buffs voice command
 * @author St3eT
 */
public class SellBuff implements IVoicedCommandHandler, IBypassHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"버프판매",
		"버프판매중지",
		"버프판매취소",
		"버프판매중단",
	};
	
	private static final String[] BYPASS_COMMANDS =
	{
		"sellbuffadd",
		"sellbuffaddskill",
		"sellbuffedit",
		"sellbuffchangeprice",
		"sellbuffremove",
		"sellbuffbuymenu",
		"sellbuffbuyskill",
		"sellbuffbuyskillPet",
		"sellbuffstart",
	};
	
	private SellBuff()
	{
		BypassHandler.getInstance().registerHandler(this);
		VoicedCommandHandler.getInstance().registerHandler(this);
	}
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		String cmd = "";
		final StringBuilder params = new StringBuilder();
		final StringTokenizer st = new StringTokenizer(command, " ");
		
		if (st.hasMoreTokens())
		{
			cmd = st.nextToken();
		}
		
		while (st.hasMoreTokens())
		{
			params.append(st.nextToken() + (st.hasMoreTokens() ? " " : ""));
		}
		
		if (cmd.isEmpty())
		{
			return false;
		}
		return useBypass(cmd, player, params.toString());
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String params)
	{
		switch (command)
		{
			case "버프판매":
			{
				if (Config.SELLBUFF_ENABLED)
				{
					SellBuffsManager.getInstance().sendSellMenu(player);
				}
				else
				{
					player.sendMessage("현재 버프판매 시스템을 사용할 수 없습니다.");
				}
				break;
			}
			case "버프판매중지":
			case "버프판매취소":
			case "버프판매중단":
			{
				if (player.isSellingBuffs())
				{
					SellBuffsManager.getInstance().stopSellBuffs(player);
					player.sendPacket(new ShowBoard());
				}
				break;
			}
		}
		return true;
	}
	
	public boolean useBypass(String command, Player player, String params)
	{
		if (!Config.SELLBUFF_ENABLED)
		{
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage("저주받은 무기를 소유한 상태에서는 사용할 수 없습니다.");
			return false;
		}
		
		switch (command)
		{
			case "sellbuffstart":
			{
				if (player.isSellingBuffs() || (params == null) || params.isEmpty())
				{
					return false;
				}
				else if (player.getSellingBuffs().isEmpty())
				{
					player.sendMessage("버프 목록이 비어 있습니다. 먼저 버프를 추가하세요!");
					return false;
				}
				else
				{
					final StringBuilder title = new StringBuilder();
					title.append("버프 판매: ");
					final StringTokenizer st = new StringTokenizer(params, " ");
					while (st.hasMoreTokens())
					{
						title.append(st.nextToken() + " ");
					}
					
					if (title.length() > 40)
					{
						player.sendMessage("제목은 29자를 초과할 수 없습니다. 다시 시도해 주세요.");
						return false;
					}
					
					SellBuffsManager.getInstance().startSellBuffs(player, title.toString());
				}
				break;
			}
			case "sellbuffadd":
			{
				if (!player.isSellingBuffs())
				{
					int index = 0;
					if ((params != null) && !params.isEmpty() && Util.isDigit(params))
					{
						index = Integer.parseInt(params);
					}
					
					SellBuffsManager.getInstance().sendBuffChoiceMenu(player, index);
				}
				break;
			}
			case "sellbuffedit":
			{
				if (!player.isSellingBuffs())
				{
					SellBuffsManager.getInstance().sendBuffEditMenu(player);
				}
				break;
			}
			case "sellbuffchangeprice":
			{
				if (!player.isSellingBuffs() && (params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int skillId = -1;
					int price = -1;
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						try
						{
							price = Integer.parseInt(st.nextToken());
						}
						catch (NumberFormatException e)
						{
							player.sendMessage("판매가가 너무 높습니다. 판매가 최대치는 " + Config.SELLBUFF_MAX_PRICE + " 아데나 입니다.");
							SellBuffsManager.getInstance().sendBuffEditMenu(player);
						}
					}
					
					if ((skillId == -1) || (price == -1))
					{
						return false;
					}
					
					final Skill skillToChange = player.getKnownSkill(skillId);
					if (skillToChange == null)
					{
						return false;
					}
					
					final SellBuffHolder holder = player.getSellingBuffs().stream().filter(h -> (h.getSkillId() == skillToChange.getId())).findFirst().orElse(null);
					if ((holder != null))
					{
						player.sendMessage(player.getKnownSkill(holder.getSkillId()).getName() + " 버프가격이  " + price + " 아데나로 변경되었습니다!");
						holder.setPrice(price);
						SellBuffsManager.getInstance().sendBuffEditMenu(player);
					}
				}
				break;
			}
			case "sellbuffremove":
			{
				if (!player.isSellingBuffs() && (params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int skillId = -1;
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if ((skillId == -1))
					{
						return false;
					}
					
					final Skill skillToRemove = player.getKnownSkill(skillId);
					if (skillToRemove == null)
					{
						return false;
					}
					
					final SellBuffHolder holder = player.getSellingBuffs().stream().filter(h -> (h.getSkillId() == skillToRemove.getId())).findFirst().orElse(null);
					if ((holder != null) && player.getSellingBuffs().remove(holder))
					{
						player.sendMessage(player.getKnownSkill(holder.getSkillId()).getName() + " 버프를 제거했습니다!");
						SellBuffsManager.getInstance().sendBuffEditMenu(player);
					}
				}
				break;
			}
			case "sellbuffaddskill":
			{
				if (!player.isSellingBuffs() && (params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int skillId = -1;
					long price = -1;
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						try
						{
							price = Integer.parseInt(st.nextToken());
						}
						catch (NumberFormatException e)
						{
							player.sendMessage("판매가가 너무 낮습니다. 최소 판매가는 " + Config.SELLBUFF_MIN_PRICE + " 아데나 입니다.");
							SellBuffsManager.getInstance().sendBuffEditMenu(player);
						}
					}
					
					if ((skillId == -1) || (price == -1))
					{
						return false;
					}
					
					final Skill skillToAdd = player.getKnownSkill(skillId);
					if (skillToAdd == null)
					{
						return false;
					}
					else if (price < Config.SELLBUFF_MIN_PRICE)
					{
						player.sendMessage("판매가가 너무 낮습니다. 최소 판매가는 " + Config.SELLBUFF_MIN_PRICE + " 아데나 입니다.");
						return false;
					}
					else if (price > Config.SELLBUFF_MAX_PRICE)
					{
						player.sendMessage("판매가가 너무 높습니다. 판매가 최대치는 " + Config.SELLBUFF_MAX_PRICE + " 아데나 입니다.");
						return false;
					}
					else if (player.getSellingBuffs().size() > Config.SELLBUFF_MAX_BUFFS)
					{
						player.sendMessage("판매가능한 최대 버프수량(" + Config.SELLBUFF_MAX_BUFFS + ")을 초과하였습니다.");
						return false;
					}
					else if (!SellBuffsManager.getInstance().isInSellList(player, skillToAdd))
					{
						player.getSellingBuffs().add(new SellBuffHolder(skillToAdd.getId(), price));
						player.sendMessage(skillToAdd.getName() + " 버프가 추가되었습니다.");
						SellBuffsManager.getInstance().sendBuffChoiceMenu(player, 0);
					}
				}
				break;
			}
			case "sellbuffbuymenu":
			{
				if ((params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int objId = -1;
					int index = 0;
					if (st.hasMoreTokens())
					{
						objId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						index = Integer.parseInt(st.nextToken());
					}
					
					final Player seller = World.getInstance().getPlayer(objId);
					if (seller != null)
					{
						if (!seller.isSellingBuffs() || !player.isInsideRadius3D(seller, Npc.INTERACTION_DISTANCE))
						{
							return false;
						}
						
						SellBuffsManager.getInstance().sendBuffMenu(player, seller, index);
					}
				}
				break;
			}
			case "sellbuffbuyskill":
			{
				if ((params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					int objId = -1;
					int skillId = -1;
					int index = 0;
					
					if (st.hasMoreTokens())
					{
						objId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						index = Integer.parseInt(st.nextToken());
					}
					
					if ((skillId == -1) || (objId == -1))
					{
						return false;
					}
					
					final Player seller = World.getInstance().getPlayer(objId);
					if (seller == null)
					{
						return false;
					}
					
					final Skill skillToBuy = seller.getKnownSkill(skillId);
					if (!seller.isSellingBuffs() || !Util.checkIfInRange(Npc.INTERACTION_DISTANCE, player, seller, true) || (skillToBuy == null))
					{
						return false;
					}
					
					if (seller.getCurrentMp() < (skillToBuy.getMpConsume() * Config.SELLBUFF_MP_MULTIPLER))
					{
						player.sendMessage(seller.getName() + "님의 MP가 부족하여 " + skillToBuy.getName() + " 버프를 받을 수 없습니다!");
						SellBuffsManager.getInstance().sendBuffMenu(player, seller, index);
						return false;
					}
					
					final SellBuffHolder holder = seller.getSellingBuffs().stream().filter(h -> (h.getSkillId() == skillToBuy.getId())).findFirst().orElse(null);
					if (holder != null)
					{
						if (AbstractScript.getQuestItemsCount(player, Config.SELLBUFF_PAYMENT_ID) >= holder.getPrice())
						{
							AbstractScript.takeItems(player, Config.SELLBUFF_PAYMENT_ID, holder.getPrice());
							AbstractScript.giveItems(seller, Config.SELLBUFF_PAYMENT_ID, holder.getPrice());
							seller.reduceCurrentMp(skillToBuy.getMpConsume() * Config.SELLBUFF_MP_MULTIPLER);
							skillToBuy.activateSkill(seller, player);
						}
						else
						{
							final ItemTemplate item = ItemTable.getInstance().getTemplate(Config.SELLBUFF_PAYMENT_ID);
							if (item != null)
							{
								player.sendMessage(item.getName() + "가 부족합니다!");
							}
							else
							{
								player.sendMessage("아데나가 부족합니다!");
							}
						}
					}
					SellBuffsManager.getInstance().sendBuffMenu(player, seller, index);
				}
				break;
			}
			case "sellbuffbuyskillPet":
			{
				if (player.hasPet())
				{
					if ((params != null) && !params.isEmpty())
					{
						final StringTokenizer st = new StringTokenizer(params, " ");
						int objId = -1;
						int skillId = -1;
						int index = 0;
						
						if (st.hasMoreTokens())
						{
							objId = Integer.parseInt(st.nextToken());
						}
						
						if (st.hasMoreTokens())
						{
							skillId = Integer.parseInt(st.nextToken());
						}
						
						if (st.hasMoreTokens())
						{
							index = Integer.parseInt(st.nextToken());
						}
						
						if ((skillId == -1) || (objId == -1))
						{
							return false;
						}
						
						final Player seller = World.getInstance().getPlayer(objId);
						if (seller == null)
						{
							return false;
						}
						
						final Skill skillToBuy = seller.getKnownSkill(skillId);
						if (!seller.isSellingBuffs() || !Util.checkIfInRange(Npc.INTERACTION_DISTANCE, player, seller, true) || (skillToBuy == null))
						{
							return false;
						}
						
						if (seller.getCurrentMp() < (skillToBuy.getMpConsume() * Config.SELLBUFF_MP_MULTIPLER))
						{
							player.sendMessage(seller.getName() + "님의 MP가 부족하여 " + skillToBuy.getName() + " 버프를 받을 수 없습니다!");
							SellBuffsManager.getInstance().sendBuffMenu(player, seller, index);
							return false;
						}
						
						final SellBuffHolder holder = seller.getSellingBuffs().stream().filter(h -> (h.getSkillId() == skillToBuy.getId())).findFirst().orElse(null);
						if (holder != null)
						{
							if (AbstractScript.getQuestItemsCount(player, Config.SELLBUFF_PAYMENT_ID) >= holder.getPrice())
							{
								AbstractScript.takeItems(player, Config.SELLBUFF_PAYMENT_ID, holder.getPrice());
								AbstractScript.giveItems(seller, Config.SELLBUFF_PAYMENT_ID, holder.getPrice());
								seller.reduceCurrentMp(skillToBuy.getMpConsume() * Config.SELLBUFF_MP_MULTIPLER);
								List<Creature> target = new ArrayList<>();
								target.add(player.getPet());
								seller.setTarget(player.getPet());
								skillToBuy.activateSkill(seller, player.getPet());
							}
							else
							{
								final ItemTemplate item = ItemTable.getInstance().getTemplate(Config.SELLBUFF_PAYMENT_ID);
								if (item != null)
								{
									player.sendMessage(item.getName() + "가 부족합니다!");
								}
								else
								{
									player.sendMessage("아데나가 부족합니다!");
								}
							}
						}
						SellBuffsManager.getInstance().sendBuffMenu(player, seller, index);
					}
				}
				else
				{
					player.sendMessage("펫이 없습니다!");
				}
				break;
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	@Override
	public String[] getBypassList()
	{
		return BYPASS_COMMANDS;
	}
	
	public static void main(String[] args)
	{
		new SellBuff();
	}
}