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
package org.l2jmobius.gameserver.model.events;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.events.impl.IBaseEvent;
import org.l2jmobius.gameserver.model.events.listeners.AbstractEventListener;
import org.l2jmobius.gameserver.model.events.returns.AbstractEventReturn;

/**
 * @author UnAfraid
 */
public class EventDispatcher
{
	private static final Logger LOGGER = Logger.getLogger(EventDispatcher.class.getName());
	
	protected EventDispatcher()
	{
	}
	
	/**
	 * @param type EventType
	 * @return {@code true} if global containers have a listener of the given type.
	 */
	public boolean hasListener(EventType type)
	{
		return Containers.Global().hasListener(type);
	}
	
	/**
	 * @param type EventType
	 * @param container ListenersContainer
	 * @return {@code true} if container has a listener of the given type.
	 */
	public boolean hasListener(EventType type, ListenersContainer container)
	{
		return Containers.Global().hasListener(type) || ((container != null) && container.hasListener(type));
	}
	
	/**
	 * @param type EventType
	 * @param containers ListenersContainer...
	 * @return {@code true} if containers have a listener of the given type.
	 */
	public boolean hasListener(EventType type, ListenersContainer... containers)
	{
		boolean hasListeners = Containers.Global().hasListener(type);
		if (!hasListeners)
		{
			for (ListenersContainer container : containers)
			{
				if (container.hasListener(type))
				{
					hasListeners = true;
					break;
				}
			}
		}
		return hasListeners;
	}
	
	/**
	 * @param <T>
	 * @param event
	 * @return
	 */
	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event)
	{
		return notifyEvent(event, null, null);
	}
	
	/**
	 * @param <T>
	 * @param event
	 * @param callbackClass
	 * @return
	 */
	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event, Class<T> callbackClass)
	{
		return notifyEvent(event, null, callbackClass);
	}
	
	/**
	 * @param <T>
	 * @param event
	 * @param container
	 * @return
	 */
	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event, ListenersContainer container)
	{
		return notifyEvent(event, container, null);
	}
	
	/**
	 * @param <T>
	 * @param event
	 * @param container
	 * @param callbackClass
	 * @return
	 */
	public <T extends AbstractEventReturn> T notifyEvent(IBaseEvent event, ListenersContainer container, Class<T> callbackClass)
	{
		try
		{
			return Containers.Global().hasListener(event.getType()) || ((container != null) && container.hasListener(event.getType())) ? notifyEventImpl(event, container, callbackClass) : null;
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't notify event " + event.getClass().getSimpleName(), e);
		}
		return null;
	}
	
	/**
	 * Executing current listener notification asynchronously
	 * @param event
	 * @param containers
	 */
	public void notifyEventAsync(IBaseEvent event, ListenersContainer... containers)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		
		boolean hasListeners = Containers.Global().hasListener(event.getType());
		if (!hasListeners)
		{
			for (ListenersContainer container : containers)
			{
				if (container.hasListener(event.getType()))
				{
					hasListeners = true;
					break;
				}
			}
		}
		
		if (hasListeners)
		{
			ThreadPool.execute(() -> notifyEventToMultipleContainers(event, containers, null));
		}
	}
	
	/**
	 * Scheduling current listener notification asynchronously after specified delay.
	 * @param event
	 * @param container
	 * @param delay
	 */
	public void notifyEventAsyncDelayed(IBaseEvent event, ListenersContainer container, long delay)
	{
		if (Containers.Global().hasListener(event.getType()) || container.hasListener(event.getType()))
		{
			ThreadPool.schedule(() -> notifyEvent(event, container, null), delay);
		}
	}
	
	/**
	 * @param <T>
	 * @param event
	 * @param containers
	 * @param callbackClass
	 * @return
	 */
	private <T extends AbstractEventReturn> T notifyEventToMultipleContainers(IBaseEvent event, ListenersContainer[] containers, Class<T> callbackClass)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		
		try
		{
			T callback = null;
			if (containers != null)
			{
				// Local listeners container first.
				for (ListenersContainer container : containers)
				{
					if ((callback == null) || !callback.abort())
					{
						callback = notifyToListeners(container.getListeners(event.getType()), event, callbackClass, callback);
					}
				}
			}
			
			// Global listener container.
			if ((callback == null) || !callback.abort())
			{
				callback = notifyToListeners(Containers.Global().getListeners(event.getType()), event, callbackClass, callback);
			}
			
			return callback;
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't notify event " + event.getClass().getSimpleName(), e);
		}
		return null;
	}
	
	/**
	 * @param <T>
	 * @param event
	 * @param container
	 * @param callbackClass
	 * @return {@link AbstractEventReturn} object that may keep data from the first listener, or last that breaks notification.
	 */
	private <T extends AbstractEventReturn> T notifyEventImpl(IBaseEvent event, ListenersContainer container, Class<T> callbackClass)
	{
		if (event == null)
		{
			throw new NullPointerException("Event cannot be null!");
		}
		
		T callback = null;
		// Local listener container first.
		if (container != null)
		{
			callback = notifyToListeners(container.getListeners(event.getType()), event, callbackClass, callback);
		}
		
		// Global listener container.
		if ((callback == null) || !callback.abort())
		{
			callback = notifyToListeners(Containers.Global().getListeners(event.getType()), event, callbackClass, callback);
		}
		
		return callback;
	}
	
	/**
	 * @param <T>
	 * @param listeners
	 * @param event
	 * @param returnBackClass
	 * @param callbackValue
	 * @return
	 */
	private <T extends AbstractEventReturn> T notifyToListeners(Queue<AbstractEventListener> listeners, IBaseEvent event, Class<T> returnBackClass, T callbackValue)
	{
		T callback = callbackValue;
		for (AbstractEventListener listener : listeners)
		{
			try
			{
				final T rb = listener.executeEvent(event, returnBackClass);
				if (rb == null)
				{
					continue;
				}
				if ((callback == null) || rb.override()) // Let's check if this listener wants to override previous return object or we simply don't have one
				{
					callback = rb;
				}
				else if (rb.abort()) // This listener wants to abort the notification to others.
				{
					break;
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Exception during notification of event: " + event.getClass().getSimpleName() + " listener: " + listener.getClass().getSimpleName(), e);
			}
		}
		return callback;
	}
	
	public static EventDispatcher getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final EventDispatcher INSTANCE = new EventDispatcher();
	}
}
