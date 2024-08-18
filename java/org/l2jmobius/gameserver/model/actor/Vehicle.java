package org.l2jmobius.gameserver.model.actor;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.MapRegionManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.VehiclePathPoint;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.stat.VehicleStat;
import org.l2jmobius.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBoatComplete;
import org.l2jmobius.gameserver.model.interfaces.ILocational;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.zone.ZoneRegion;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.taskmanager.MovementTaskManager;
import org.l2jmobius.gameserver.util.BoatReward;
import org.l2jmobius.gameserver.util.Util;

public abstract class Vehicle extends Creature
{
	protected int _dockId = 0;
	protected final Set<Player> _passengers = ConcurrentHashMap.newKeySet();
	protected Location _oustLoc = null;
	private Runnable _engine = null;
	
	protected VehiclePathPoint[] _currentPath = null;
	protected int _runState = 0;
	private ScheduledFuture<?> _monitorTask = null;
	private final Location _monitorLocation = new Location(this);
	
	public Vehicle(CreatureTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.Vehicle);
		setFlying(true);
	}
	
	public boolean isBoat()
	{
		return false;
	}
	
	public boolean isAirShip()
	{
		return false;
	}
	
	public boolean canBeControlled()
	{
		return _engine == null;
	}
	
	public void registerEngine(Runnable r)
	{
		_engine = r;
	}
	
	public void runEngine(long delay)
	{
		if (_engine != null)
		{
			ThreadPool.schedule(_engine, delay);
		}
	}
	
	public void delEngine()
	{
		_engine = null;
	}
	
	public void executePath(VehiclePathPoint[] path)
	{
		_runState = 0;
		_currentPath = path;
		if ((_currentPath != null) && (_currentPath.length > 0))
		{
			final VehiclePathPoint point = _currentPath[0];
			if (point.getMoveSpeed() > 0)
			{
				getStat().setMoveSpeed(point.getMoveSpeed());
			}
			if (point.getRotationSpeed() > 0)
			{
				getStat().setRotationSpeed(point.getRotationSpeed());
			}
			
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(point.getX(), point.getY(), point.getZ(), 0));
			return;
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
	
	@Override
	public boolean moveToNextRoutePoint()
	{
		_move = null;
		if (_currentPath != null)
		{
			_runState++;
			if (_runState < _currentPath.length)
			{
				final VehiclePathPoint point = _currentPath[_runState];
				if (!isMovementDisabled())
				{
					if (point.getMoveSpeed() == 0)
					{
						point.setHeading(point.getRotationSpeed());
						teleToLocation(point, false);
						if (_monitorTask != null)
						{
							_monitorTask.cancel(true);
							_monitorTask = null;
						}
						_currentPath = null;
					}
					else
					{
						if (point.getMoveSpeed() > 0)
						{
							getStat().setMoveSpeed(point.getMoveSpeed());
						}
						if (point.getRotationSpeed() > 0)
						{
							getStat().setRotationSpeed(point.getRotationSpeed());
						}
						
						final MoveData m = new MoveData();
						m.disregardingGeodata = false;
						m.onGeodataPathIndex = -1;
						m._xDestination = point.getX();
						m._yDestination = point.getY();
						m._zDestination = point.getZ();
						m._heading = 0;
						
						final double distance = Math.hypot(point.getX() - getX(), point.getY() - getY());
						if (distance > 1)
						{
							setHeading(Util.calculateHeadingFrom(getX(), getY(), point.getX(), point.getY()));
						}
						
						m._moveStartTime = GameTimeTaskManager.getInstance().getGameTicks();
						_move = m;
						MovementTaskManager.getInstance().registerMovingObject(this);
						
						// Make sure vehicle is not stuck.
						if (_monitorTask == null)
						{
							_monitorTask = ThreadPool.scheduleAtFixedRate(() ->
							{
								if (!isInDock() && (calculateDistance3D(_monitorLocation) == 0))
								{
									if (_currentPath != null)
									{
										if (_runState < _currentPath.length)
										{
											_runState = Math.max(0, _runState - 1);
											moveToNextRoutePoint();
										}
										else
										{
											broadcastInfo();
										}
									}
								}
								else
								{
									_monitorLocation.setXYZ(this);
								}
							}, 1000, 1000);
						}
						
						return true;
					}
				}
			}
			else
			{
				if (_monitorTask != null)
				{
					_monitorTask.cancel(true);
					_monitorTask = null;
				}
				_currentPath = null;
			}
		}
		
		runEngine(10);
		return false;
	}
	
	@Override
	public VehicleStat getStat()
	{
		return (VehicleStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new VehicleStat(this));
	}
	
	public boolean isInDock()
	{
		return _dockId > 0;
	}
	
	public int getDockId()
	{
		return _dockId;
	}
	
	public void setInDock(int d)
	{
		_dockId = d;
	}
	
	public void setOustLoc(Location loc)
	{
		_oustLoc = loc;
	}
	
	public Location getOustLoc()
	{
		return _oustLoc != null ? _oustLoc : MapRegionManager.getInstance().getTeleToLocation(this, TeleportWhereType.TOWN);
	}
	
	public void oustPlayers()
	{
		Player player;
		
		// Use iterator because oustPlayer will try to remove player from _passengers
		final Iterator<Player> iter = _passengers.iterator();
		while (iter.hasNext())
		{
			player = iter.next();
			iter.remove();
			if (player != null)
			{
				oustPlayer(player);
			}
		}
	}
	
	public void oustPlayer(Player player)
	{
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		removePassenger(player);
	}
	
	public boolean addPassenger(Player player)
	{
		if ((player == null) || _passengers.contains(player))
		{
			return false;
		}
		
		// already in other vehicle
		if ((player.getVehicle() != null) && (player.getVehicle() != this))
		{
			return false;
		}
		
		_passengers.add(player);
		return true;
	}
	
	public void removePassenger(Player player)
	{
		try
		{
			_passengers.remove(player);
		}
		catch (Exception e)
		{
		}
	}
	
	public boolean isEmpty()
	{
		return _passengers.isEmpty();
	}
	
	public Set<Player> getPassengers()
	{
		return _passengers;
	}
	
	public void broadcastToPassengers(IClientOutgoingPacket sm)
	{
		for (Player player : _passengers)
		{
			if (player != null)
			{
				player.sendPacket(sm);
			}
		}
	}
	
	public void broadcastToPassengers(String boatName)
	{
		for (Player player : _passengers)
		{
			if (player != null)
			{
				if ((player.getBoat() != null) && (player.getBoat().getObjectId() == getObjectId()))
				{
					player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "정기선 안내", boatName + "가 정박을 완료할 때까지 하선을 잠시 기다려 주세요."));
					// 확인/취소 버튼이 있는 팝업창 추가
					// String popupMessage = boatName + "가 정박을 완료할 때까지 하선을 잠시 기다려 주세요.";
					// ConfirmDlg confirmDlg = new ConfirmDlg(popupMessage);
					// confirmDlg.addTime(5000); // 5초 동안 팝업을 표시
					// player.sendPacket(confirmDlg);
					
					player.setBlockActions(true);
					player.sendMessage("이동불가 상태가 되었습니다.");
				}
			}
		}
	}
	
	public boolean checkPassengers(Player player)
	{
		if (_passengers.contains(player))
		{
			return true;
		}
		return false;
	}
	
	public void clearPassenger(Player player)
	{
		_passengers.remove(player);
	}
	
	/**
	 * Consume ticket(s) and teleport player from boat if no correct ticket
	 * @param ticketId
	 * @param oustX
	 * @param oustY
	 * @param oustZ
	 */
	public void payForRide(int ticketId, int oustX, int oustY, int oustZ)
	{
		World.getInstance().forEachVisibleObjectInRange(this, Player.class, 1000, player ->
		{
			if (player.isOnBoat() && player.isInBoat() && (player.getBoat() == this))
			{
				final Item ticket = player.getInventory().getItemByItemId(ticketId);
				final InventoryUpdate iu = new InventoryUpdate();
				if (ticket == null)
				{
					player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
					player.sendMessage("정기선 배표가 없으면 탑승할 수 없습니다.");
					player.teleToLocation(new Location(oustX, oustY, oustZ), true);
					player.setLocation(new Location(oustX, oustY, oustZ));
					player.sendPacket(new ValidateLocation(player));
					player.broadcastPacket(new ValidateLocation(player));
					
					String popupMessage = "정기선 배표가 없으면 탑승할 수 없습니다.";
					ConfirmDlg confirmDlg = new ConfirmDlg(popupMessage);
					confirmDlg.addTime(5000); // 5초 동안 팝업을 표시
					player.sendPacket(confirmDlg);
					return;
				}
				player.getInventory().destroyItemByItemId(getName(), ticketId, 1, player, null);
				iu.addModifiedItem(ticket);
				player.sendInventoryUpdate(iu);
				addPassenger(player);
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(ticket);
				player.sendPacket(sm);
				if (Config.ALLOW_REWARD)
				{
					player.addQuickVar("boatReward", true); // 보상 체크 상태를 예약됨으로 설정
					BoatReward.scheduleRewardCheck(player); // 보상 스케쥴 시작됨.
				}
				String popupMessage = player.getBoat().getBoatName() + "가 운항 중일 때 캐릭터가 비정상적인 곳에 끼이는 경우 운항이 종료될 때까지 기다려 주세요.";
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "정기선 안내", popupMessage));
				ConfirmDlg confirmDlg = new ConfirmDlg(popupMessage);
				confirmDlg.addTime(10000); // 5초 동안 팝업을 표시
				player.sendPacket(confirmDlg);
			}
		});
	}
	
	public void missionComplate(int oustX, int oustY, int oustZ)
	{
		World.getInstance().forEachVisibleObjectInRange(this, Player.class, 1000, player ->
		{
			if (player == null)
			{
				return;
			}
			
			if (player.isInBoat() && (player.getBoat() == this) && _passengers.contains(player))
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerBoatComplete(player), player);
				player.sendPacket(new PlaySound("ItemSound3.sys_pledge_join"));
				
				String message = player.getBoat().getBoatName() + "의 운항이 종료되었습니다. " + Config.SERVER_NAME_KOR + "과 함께 즐거운 시간 되세요.";
				player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "정기선 안내", message));
				
				player.setBlockActions(false);
				player.sendMessage("이동불가 상태가 해제 되었습니다.");
				BoatReward.distributeReward(player);
				player.deleteQuickVar("boatReward");
				player.teleToLocation(new Location(oustX, oustY, oustZ), true);
				player.setLocation(new Location(oustX, oustY, oustZ));
				player.sendPacket(new ValidateLocation(player));
				player.broadcastPacket(new ValidateLocation(player));
			}
		});
	}
	
	@Override
	public boolean updatePosition()
	{
		final boolean result = super.updatePosition();
		for (Player player : _passengers)
		{
			if ((player != null) && (player.getVehicle() == this))
			{
				player.setXYZ(getX(), getY(), getZ());
				player.revalidateZone(false);
			}
		}
		return result;
	}
	
	@Override
	public void teleToLocation(ILocational loc, boolean allowRandomOffset)
	{
		if (isMoving())
		{
			stopMove(null);
		}
		
		setTeleporting(true);
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		for (Player player : _passengers)
		{
			if (player != null)
			{
				player.teleToLocation(loc, false);
			}
		}
		
		decayMe();
		setXYZ(loc);
		
		// temporary fix for heading on teleports
		if (loc.getHeading() != 0)
		{
			setHeading(loc.getHeading());
		}
		
		onTeleported();
		revalidateZone(true);
	}
	
	@Override
	public void stopMove(Location loc)
	{
		_move = null;
		if (loc != null)
		{
			setXYZ(loc);
			setHeading(loc.getHeading());
			revalidateZone(true);
		}
	}
	
	public void deleteBoat()
	{
		deleteMe();
	}
	
	@Override
	public boolean deleteMe()
	{
		_engine = null;
		
		try
		{
			if (isMoving())
			{
				stopMove(null);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed stopMove().", e);
		}
		
		try
		{
			oustPlayers();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed oustPlayers().", e);
		}
		
		final ZoneRegion oldZoneRegion = ZoneManager.getInstance().getRegion(this);
		
		try
		{
			decayMe();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed decayMe().", e);
		}
		
		oldZoneRegion.removeFromZones(this);
		
		return super.deleteMe();
	}
	
	@Override
	public Item getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public int getLevel()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	public void detachAI()
	{
	}
	
	@Override
	public boolean isVehicle()
	{
		return true;
	}
}
