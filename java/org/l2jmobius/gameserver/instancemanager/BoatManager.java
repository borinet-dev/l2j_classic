package org.l2jmobius.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.HarborNearLocation;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jmobius.gameserver.model.events.AbstractScript;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * 보트 매니저 클래스. 보트의 생성, 관리 및 브로드캐스팅을 담당. 보리넷 가츠
 */
public class BoatManager
{
	protected static final Logger LOGGER = Logger.getLogger(BoatManager.class.getName());
	public final Map<Integer, Boat> _boats = new HashMap<>();
	public final List<Integer> boatIds = new ArrayList<>(); // 보트 ID 저장
	private static List<Npc> boatNpcs;
	public static boolean npcSpawned = false;
	
	// 싱글톤 인스턴스 반환
	public static BoatManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	// 생성자
	protected BoatManager()
	{
		if (!Config.ALLOW_BOAT)
		{
			return;
		}
		boatNpcSpawn();
	}
	
	// 새로운 보트를 생성하고 반환
	public Boat getNewBoat(int boatId, int x, int y, int z, int heading, String boatName)
	{
		final StatSet npcDat = new StatSet();
		npcDat.set("npcId", boatId);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);
		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("baseHpMax", 50000);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		final CreatureTemplate template = new CreatureTemplate(npcDat);
		final Boat boat = new Boat(template);
		boat.setBoatName(boatName); // 보트 객체에 출발지 정보 설정
		_boats.put(boat.getObjectId(), boat);
		boat.setHeading(heading);
		boat.setXYZInvisible(x, y, z);
		boat.spawnMe();
		boatIds.add(boat.getObjectId()); // 보트 ID 저장
		return boat;
	}
	
	// 보트 ID로 보트 객체를 반환
	public Boat getBoat(int boatId)
	{
		return _boats.get(boatId);
	}
	
	// 보트 ID 목록 반환
	public List<Integer> getBoatIds()
	{
		return new ArrayList<>(boatIds);
	}
	
	// 특정 좌표 근처의 플레이어들에게 패킷 브로드캐스트
	public void broadcastPacketsToPlayer(HarborNearLocation point, IClientOutgoingPacket... packets)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.calculateDistance2D(point.getX(), point.getY(), point.getZ()) < Config.BOAT_BROADCAST_RADIUS)
			{
				for (IClientOutgoingPacket p : packets)
				{
					player.sendPacket(p);
				}
			}
		}
	}
	
	// 특정 좌표 근처의 플레이어들에게 패킷 브로드캐스트
	public void broadcastPacketsToPlayers(HarborNearLocation point, String text)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.calculateDistance2D(point.getX(), point.getY(), point.getZ()) < Config.BOAT_BROADCAST_RADIUS)
			{
				player.sendPacket(new CreatureSay(null, ChatType.SHOUT, "정기선 안내", text));
			}
		}
	}
	
	// 보트를 제거하는 메서드
	public void removeBoats(List<Integer> boatIdsToRemove)
	{
		for (int boatId : boatIdsToRemove)
		{
			Boat boat = getBoat(boatId);
			if (boat != null)
			{
				// 보트를 정지
				boat.stopMove(null);
				
				// 스케줄 취소
				boat.delEngine();
				
				// 보트 삭제
				_boats.remove(boat.getObjectId());
				_boats.remove(boat.getId());
				boat.deleteBoat();
			}
		}
	}
	
	public static void boatNpcSpawn()
	{
		boatNpcs = new ArrayList<>();
		
		Npc npc1 = AbstractScript.addSpawn(Config.BOAT_WHARF_MANAGER_TALKING, -96630, 261146, -3616, 16011, false, 0); // <!-- 말섬 -->
		Npc npc2 = AbstractScript.addSpawn(Config.BOAT_WHARF_MANAGER_GIRAN, 48784, 190093, -3624, 10817, false, 0); // <!-- 기란 -->
		
		boatNpcs.add(npc1);
		boatNpcs.add(npc2);
		
		npcSpawned = true;
	}
	
	public static void boatNpcUnSpawn()
	{
		if (boatNpcs != null)
		{
			for (Npc npc : boatNpcs)
			{
				if (npc != null)
				{
					npc.deleteMe();
				}
			}
			boatNpcs.clear();
		}
	}
	
	public int getBoatObjectIdByName(String boatName)
	{
		for (Boat boat : _boats.values())
		{
			if (boat.getBoatName().equals(boatName))
			{
				return boat.getObjectId();
			}
		}
		return -1; // 보트를 찾지 못한 경우
	}
	
	// 싱글톤 패턴을 위한 홀더 클래스
	private static class SingletonHolder
	{
		protected static final BoatManager INSTANCE = new BoatManager();
	}
}
