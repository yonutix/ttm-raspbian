/*******************************************************************************
 * Copyright (C) 2015 Andrei Olaru, Marius-Tudor Benea, Nguyen Thi Thuy Nga, Amal El Fallah Seghrouchni, Cedric Herpson.
 * 
 * This file is part of tATAmI-PC.
 * 
 * tATAmI-PC is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * tATAmI-PC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with tATAmI-PC.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package tatami.simulation;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import Logging.Log;
import tatami.core.agent.AgentComponent;
import tatami.core.agent.AgentEvent;
import tatami.core.agent.messaging.MessagingComponent;
import tatami.core.agent.messaging.NameBasedMessagingComponent;

/**
 * Simple platform that allows agents to send messages locally (inside the same JVM) based simply on agent name.
 * 
 * @author Andrei Olaru
 */
public class LocalDeploymentPlatform extends DefaultPlatform implements PlatformLoader.PlatformLink
{
	/**
	 * Simple implementation of {@link MessagingComponent}, that uses agents' names as their addresses.
	 * 
	 * @author Andrei Olaru
	 */
	public static class SimpleLocalMessaging extends NameBasedMessagingComponent
	{
		/**
		 * The serial UID.
		 */
		private static final long serialVersionUID = 1L;
		
		@Override
		public boolean sendMessage(String target, String source, String content)
		{
			if(!(getPlatformLink() instanceof LocalDeploymentPlatform))
				throw new IllegalStateException("Platform Link is not of expected type");
			LocalDeploymentPlatform p = ((LocalDeploymentPlatform) getPlatformLink());
			String[] targetElements = target.split(ADDRESS_SEPARATOR, 2);
			SimpleLocalMessaging targetComponent = p.registry.get(targetElements[0]);
			if(targetComponent != null)
			{
				if(p.useThread)
				{
					LinkedBlockingQueue<Entry<SimpleLocalMessaging, Vector<String>>> q = p.messageQueue;
					try
					{
						synchronized(q)
						{
							Vector<String> v = new Vector<String>(3);
							v.add(source);
							v.add(target);
							v.add(content);
							q.put(new AbstractMap.SimpleEntry<SimpleLocalMessaging, Vector<String>>(
									targetComponent, v));
							q.notify();
						}
					} catch(InterruptedException e)
					{
						e.printStackTrace();
						return false;
					}
				}
				else
					targetComponent.receiveMessage(source, target, content);
			}
			else
				try
				{
					Log.e("core", "No messaging component registered for name []." + targetElements[0]);
				} catch(NullPointerException e)
				{
					// nothing
				}
			return true;
		}
		
		@Override
		protected void atAgentStart(AgentEvent event)
		{
			super.atAgentStart(event);
			if(!(getPlatformLink() instanceof LocalDeploymentPlatform))
				throw new IllegalStateException("Platform Link is not of expected type");
			try
			{
				Log.d("core", "Registered with platform.");
			} catch(NullPointerException e)
			{
				// nothing
			}
			((LocalDeploymentPlatform) getPlatformLink()).registry.put(getAgentName(), this);
		}
		
		@Override
		protected void receiveMessage(String source, String destination, String content)
		{
			super.receiveMessage(source, destination, content);
		}
	}
	
	/**
	 * The thread that manages the message queue.
	 * 
	 * @author Andrei Olaru
	 */
	class MessageThread implements Runnable
	{
		@Override
		public void run()
		{
			// System.out.println("oops");
			while(useThread)
			{
				if(messageQueue.isEmpty())
					try
					{
						synchronized(messageQueue)
						{
							messageQueue.wait();
						}
					} catch(InterruptedException e)
					{
						// do nothing
					}
				else
				{
					Entry<SimpleLocalMessaging, Vector<String>> event = messageQueue.poll();
					event.getKey().receiveMessage(event.getValue().get(0), event.getValue().get(1),
							event.getValue().get(2));
				}
			}
		}
	}
	
	/**
	 * The registry of agents that can receive messages, specifying the {@link MessagingComponent} receiving the
	 * message.
	 *
	 */
	protected Map<String, SimpleLocalMessaging> registry = new HashMap<String, SimpleLocalMessaging>();
	
	/**
	 * If <code>true</code>, a separate thread will be used to buffer messages. Otherwise, only method calling will be
	 * used.
	 * <p>
	 * <b>WARNING:</b> not using a thread may lead to race conditions and deadlocks. Use only if you know what you are
	 * doing.
	 */
	protected boolean useThread = true;
	
	/**
	 * If a separate thread is used for messages ({@link #useThread} is <code>true</code>) this queue is used to gather
	 * messages.
	 */
	protected LinkedBlockingQueue<Entry<SimpleLocalMessaging, Vector<String>>> messageQueue = null;
	
	/**
	 * If a separate thread is used for messages ({@link #useThread} is <code>true</code>) this is a reference to that
	 * thread.
	 */
	protected Thread messageThread = null;
	
	@Override
	public boolean start()
	{
		if(!super.start())
			return false;
		if(useThread)
		{
			messageQueue = new LinkedBlockingQueue<Entry<SimpleLocalMessaging, Vector<String>>>();
			messageThread = new Thread(new MessageThread());
			messageThread.start();
		}
		return true;
	}
	
	@Override
	public boolean stop()
	{
		super.stop();
		if(useThread)
		{
			useThread = false; // signal to the thread
			synchronized(messageQueue)
			{
				messageQueue.clear();
				messageQueue.notifyAll();
			}
			try
			{
				messageThread.join();
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			messageQueue = null;
			messageThread = null;
		}
		return true;
	}
	
	@Override
	public boolean loadAgent(String containerName, AgentManager agentManager)
	{
		return agentManager.setPlatformLink(this) && super.loadAgent(containerName, agentManager);
	}
	
	@Override
	public String getRecommendedComponentClass(AgentComponent.AgentComponentName componentName)
	{
		if(componentName == AgentComponent.AgentComponentName.MESSAGING_COMPONENT)
			return SimpleLocalMessaging.class.getName();
		return super.getRecommendedComponentClass(componentName);
	}
	
	@Override
	public String getName()
	{
		return StandardPlatformType.LOCAL.toString();
	}

	@Override
	public void onAgentStateChanged(AgentManager agent)
	{
		// nothing to do
	}

	@Override
	public void onAgentStateChanged(AgentEvent event, AgentManager agent) {
		// TODO Auto-generated method stub
		
	}
}
