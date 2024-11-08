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
package org.l2jmobius.gameserver.model.holders;

import java.util.Set;

public class EventDropHolder extends DropHolder
{
	private final int _minLevel;
	private final int _maxLevel;
	private final Set<Integer> _monsterIds;
	private final boolean _dropTrue;
	
	public EventDropHolder(int itemId, long min, long max, double chance, int minLevel, int maxLevel, Set<Integer> monsterIds, boolean isDrop)
	{
		super(null, itemId, min, max, chance);
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_monsterIds = monsterIds;
		_dropTrue = isDrop;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public Set<Integer> getMonsterIds()
	{
		return _monsterIds;
	}
	
	public boolean isEventDrop()
	{
		return _dropTrue;
	}
}
