package boat;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.instancemanager.BoatManager;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.BoatPathPoint;
import org.l2jmobius.gameserver.model.HarborNearLocation;
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.util.BoatUtil;
import org.l2jmobius.gameserver.util.BorinetTask;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author 보리넷 가츠
 */
public class BoatBaikal
{
	protected static final Logger LOGGER = Logger.getLogger(BoatBaikal.class.getName());
	
	private final Boat _boat;
	private int _cycle = BorinetTask.getInstance()._boatCycle + 9;
	
	private final PlaySound GIRAN_SOUND;
	private final PlaySound TALKING_SOUND;
	
	public BoatBaikal(Boat boat)
	{
		_boat = boat;
		
		GIRAN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, _boat.getObjectId(), BoatPathPoint.GIRAN_DOCK[0].getX(), BoatPathPoint.GIRAN_DOCK[0].getY(), BoatPathPoint.GIRAN_DOCK[0].getZ());
		TALKING_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, _boat.getObjectId(), BoatPathPoint.TALKING_DOCK[0].getX(), BoatPathPoint.TALKING_DOCK[0].getY(), BoatPathPoint.TALKING_DOCK[0].getZ());
	}
	
	private void checkMethod_Baikal()
	{
		if (BoatUtil._stopRequested)
		{
			BoatUtil.isComplatedBaikal = true;
			LOGGER.info("바이칼 호의 cycle이 종료되었습니다.");
			Broadcast.toGMsendMessage("바이칼 호의 cycle이 종료되었습니다.");
			return;
		}
		try
		{
			switch (_cycle)
			{
				case 1:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 5분 후에 말하는 섬 항구로 출항합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 240000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬시작) - " + _cycle);
					}
					break;
				}
				case 2:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 1분 후에 말하는 섬 항구로 출항합니다.");
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호를 이용하실 분은 서둘러 탑승해 주세요");
					ThreadPool.schedule(this::checkMethod_Baikal, 40000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle);
					}
					break;
				}
				case 3:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 곧 말하는 섬 항구로 출항합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 20000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle);
					}
					break;
				}
				case 4:
				{
					BoatUtil.getInstance().setBoatStatus("바이칼 호", "운항 중");
					BoatUtil.getInstance().setBoatArrival("바이칼 호", 850000 + System.currentTimeMillis());
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 말하는 섬 항구로 출항하였습니다.");
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 약 15분 후 말하는 섬 항구에 도착합니다.");
					_boat.broadcastPacket(GIRAN_SOUND);
					_boat.payForRide(3946, 48579, 190058, -3628); // 기란 선착장 관리인
					_boat.executePath(BoatPathPoint.GIRAN_TO_TALKING);
					ThreadPool.schedule(this::checkMethod_Baikal, 250000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle + " - 운항 시작");
					}
					break;
				}
				case 5:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 약 10분 후 말하는 섬 항구에 도착합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 300000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle);
					}
					break;
				}
				case 6:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 약 5분 후 말하는 섬 항구에 도착합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 240000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle);
					}
					break;
				}
				case 7:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 약 1분 후 말하는 섬 항구에 도착합니다.");
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle);
					}
					break;
				}
				case 8:
				{
					BoatUtil.getInstance().setBoatStatus("바이칼 호", "정박 중");
					BoatUtil.getInstance().setBoatDestination("바이칼 호", "기란 항구");
					BoatUtil.getInstance().setBoatArrival("바이칼 호", 0); // 도착시간 초기화
					_boat.broadcastToPassengers("바이칼 호");
					_boat.executePath(BoatPathPoint.TALKING_DOCK);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle);
					}
					break;
				}
				case 9:
				{
					String 출발시간 = BoatUtil.getInstance().getBoatDepartureTime("바이칼 호"); // 출발시간 표시
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 도착했습니다.");
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "약 " + 출발시간 + "에 기란 항구로 출항합니다.");
					_boat.missionComplate(-96801, 261052, -3625); // 말섬 항구
					_boat.broadcastPacket(TALKING_SOUND);
					long[] delay = BoatUtil.getInstance().nextScheduler();
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(말섬) - " + _cycle + " - 말하는 섬 항구 도착: " + delay[0] + " 밀리초 남음");
					}
					ThreadPool.schedule(this::checkMethod_Baikal, delay[0]);
					break;
				}
				case 10:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 5분 후에 기란 항구로 출항합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 240000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란시작) - " + _cycle);
					}
					break;
				}
				case 11:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 1분 후에 기란 항구로 출항합니다.");
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호를 이용하실 분은 서둘러 탑승해 주세요.");
					ThreadPool.schedule(this::checkMethod_Baikal, 40000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 12:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 곧 기란 항구로 출항합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 20000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 13:
				{
					BoatUtil.getInstance().setBoatStatus("바이칼 호", "운항 중");
					BoatUtil.getInstance().setBoatArrival("바이칼 호", 1400000 + System.currentTimeMillis());
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 기란 항구로 출항하였습니다.");
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.TALKING[0], "바이칼 호가 약 23분 후 기란 항구에 도착합니다.");
					_boat.broadcastPacket(TALKING_SOUND);
					_boat.payForRide(3945, -96801, 261052, -3625); // 말섬 선착장 관리인
					_boat.executePath(BoatPathPoint.TALKING_TO_GIRAN);
					ThreadPool.schedule(this::checkMethod_Baikal, 200000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle + " - 운항 시작");
					}
					break;
				}
				case 14:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 약 20분 후 기란 항구에 도착합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 300000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 15:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 약 15분 후 기란 항구에 도착합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 300000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 16:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 약 10분 후 기란 항구에 도착합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 300000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 17:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 약 5분 후 기란 항구에 도착합니다.");
					ThreadPool.schedule(this::checkMethod_Baikal, 240000);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 18:
				{
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 약 1분 후 기란 항구에 도착합니다.");
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 19:
				{
					BoatUtil.getInstance().setBoatStatus("바이칼 호", "정박 중");
					BoatUtil.getInstance().setBoatDestination("바이칼 호", "말하는 섬 항구");
					BoatUtil.getInstance().setBoatArrival("바이칼 호", 0); // 도착시간 초기화
					_boat.broadcastToPassengers("바이칼 호");
					_boat.executePath(BoatPathPoint.GIRAN_DOCK);
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle);
					}
					break;
				}
				case 20:
				{
					String 출발시간 = BoatUtil.getInstance().getBoatDepartureTime("바이칼 호"); // 출발시간 표시
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "바이칼 호가 도착했습니다.");
					BoatManager.getInstance().broadcastPacketsToPlayers(HarborNearLocation.GIRAN[0], "약 " + 출발시간 + "에 말하는 섬 항구로 출항합니다.");
					_boat.missionComplate(48579, 190058, -3628);
					_boat.broadcastPacket(GIRAN_SOUND);
					long[] delay = BoatUtil.getInstance().nextScheduler();
					if (Config.ENABLE_DEBUG_LOGGING)
					{
						LOGGER.info("바이칼 호(기란) - " + _cycle + " - 기란 항구 도착: " + delay[0] + " 밀리초 남음");
					}
					ThreadPool.schedule(this::checkMethod_Baikal, delay[0]);
					break;
				}
			}
			_cycle++;
			if (_cycle > 20)
			{
				_cycle = 1;
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, e.getMessage());
		}
	}
	
	public static void main(String[] args)
	{
		if (Config.ALLOW_BOAT && !BorinetTask._isActive)
		{
			final Boat 바이칼_호 = BoatManager.getInstance().getNewBoat(1, -96622, 261660, -3600, 32710, "바이칼 호");
			long _delay = GlobalVariablesManager.getInstance().getLong("boat_departureTime", 0) - System.currentTimeMillis();
			if (바이칼_호 != null)
			{
				BoatUtil.isStartedBoatBaikal = true;
				바이칼_호.registerEngine(new BoatBaikal(바이칼_호)::checkMethod_Baikal);
				바이칼_호.runEngine(_delay);
			}
		}
	}
}