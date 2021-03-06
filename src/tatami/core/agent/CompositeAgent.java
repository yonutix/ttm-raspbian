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
package tatami.core.agent;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import Logging.Log;
import tatami.core.agent.AgentComponent.AgentComponentName;
import tatami.core.agent.AgentEvent.AgentEventType;
import tatami.core.agent.AgentEvent.AgentSequenceType;
import tatami.core.agent.parametric.AgentParameterName;
import tatami.core.agent.parametric.ParametricComponent;
import tatami.simulation.AgentManager;
import tatami.simulation.PlatformLoader;

/**
 * This class reunites the components of an agent in order for components to be able to call each other and for events
 * to be distributed to all components.
 * <p>
 * Various agent components -- instances of {@link AgentComponent} -- can be added. 'Standard' components have names
 * that are instances of {@link AgentComponentName}. 'Other' (non-standard) components can have any name (TODO). At most
 * one component with a name is allowed (i.e. at most one component per functionality).
 * <p>
 * It is this class that handles agent events, by means of the <code>postAgentEvent()</code> method, which disseminates
 * an event to all components, which handle it by means of registered handles (each component registers a handle for an
 * event with itself). See {@link AgentComponent}.
 * <p>
 * A composite agent instance is its own {@link AgentManager}.
 * 
 * @author Andrei Olaru
 * 		
 */
public class CompositeAgent implements Serializable, AgentManager
{
	/**
	 * Values indicating the current state of the agent, especially with respect to processing events.
	 * <p>
	 * The normal transition between states is the following: <br/>
	 * <ul>
	 * <li>{@link #STOPPED} [here components are normally added] + {@link AgentEventType#AGENT_START} &rarr;
	 * {@link #STARTING} [starting thread; starting components] &rarr; {@link #RUNNING}.
	 * <li>while in {@link #RUNNING}, components can be added or removed.
	 * <li>{@link #RUNNING} + {@link AgentEventType#AGENT_STOP} &rarr; {@link #STOPPING} [no more events accepted; stop
	 * components; stop thread] &rarr; {@link #STOPPED}.
	 * <li>when the {@link #TRANSIENT} state is involved, the transitions are as follows: {@link #RUNNING} +
	 * {@link AgentEventType#AGENT_STOP} w/ parameter {@link CompositeAgent#TRANSIENT_EVENT_PARAMETER} &rarr;
	 * {@link #STOPPING} &rarr {@link #TRANSIENT} [unable to modify agent] + {@link AgentEventType#AGENT_START} w/
	 * parameter {@link CompositeAgent#TRANSIENT_EVENT_PARAMETER} &rarr; {@link #RUNNING}.
	 * </ul>
	 * 
	 * @author Andrei Olaru
	 */
	enum AgentState {
		/**
		 * State indicating that the agent is currently behaving normally and agent events are processed in good order.
		 * All components are running.
		 */
		RUNNING,
		
		/**
		 * State indicating that the agent is stopped and is unable to process events. The agent's thread is stopped.
		 * All components are stopped.
		 */
		STOPPED,
		
		/**
		 * This state is a version of the {@link #STOPPED} state, with the exception that it does not allow any changes
		 * the general state of the agent (e.g. component list). The state should be used to "freeze" the agent, such as
		 * for it to be serialized.. Normally, in this state components should not allow any changes either.
		 */
		TRANSIENT,
		
		/**
		 * State indicating that the agent is in the process of starting, but is not currently accepting events. The
		 * thread may or may not have been started. The components are in the process of starting.
		 */
		STARTING,
		
		/**
		 * State indicating that the agent is currently stopping. It is not accepting events any more. The thread may or
		 * may not be running. The components are in the process of stopping.
		 */
		STOPPING,
	}
	
	/**
	 * This is the event-processing thread of the agent.
	 * 
	 * @author Andrei Olaru
	 */
	class AgentThread implements Runnable
	{
		@Override
		public void run()
		{
            Log.v("smth", "Agent thread is running");
			boolean threadExit = false;
			while(!threadExit)
			{
				if((eventQueue != null) && eventQueue.isEmpty())
					try
					{
						synchronized(eventQueue)
						{
							eventQueue.wait();
						}
					} catch(InterruptedException e)
					{
						// do nothing
					}
				else
				{
					AgentEvent event = eventQueue.poll();
                    Log.v("smth", "Agent signaled with " + event.toString());
					// log(event.toString());
					switch(event.getType().getSequenceType())
					{
					case CONSTRUCTIVE:
					case UNORDERED:
                        Log.v("smth", "unordered");
						for(AgentComponent component : componentOrder) {
                            Log.v("smth", "unordered " + component.getComponentName() + " " + component.getAgentName());
                            component.signalAgentEvent(event);
                        }
						break;
					case DESTRUCTIVE:
						for(ListIterator<AgentComponent> it = componentOrder.listIterator(componentOrder.size()); it
								.hasPrevious();){

							it.previous().signalAgentEvent(event);
                        }
					}
					threadExit = FSMEventOut(event, event.isSet(TRANSIENT_EVENT_PARAMETER));
				}
			}
		}
	}
	
	/**
	 * The class UID
	 */
	private static final long							serialVersionUID			= -2693230015986527097L;
																					
	/**
	 * Time (in milliseconds) to wait for the agent thread to exit.
	 */
	@Deprecated
	protected static final long							EXIT_TIMEOUT				= 500;
																					
	/**
	 * The name of the parameter that should be added to {@link AgentEventType#AGENT_START} /
	 * {@link AgentEventType#AGENT_STOP} events in order to take the agent out of / into the <code>TRANSIENT</code>
	 * state.
	 */
	public static final String							TRANSIENT_EVENT_PARAMETER	= "TO_FROM_TRANSIENT";
																					
	/**
	 * This can be used by platform-specific components to contact the platform.
	 */
	transient protected PlatformLoader.PlatformLink platformLink				= null;
																					
	/**
	 * The {@link Map} that links component names (functionalities) to standard component instances.
	 */
	protected Map<AgentComponent.AgentComponentName, AgentComponent>	components					= new HashMap<AgentComponent.AgentComponentName, AgentComponent>();
																					
	/**
	 * A {@link List} that holds the order in which components were added, so as to signal agent events to components in
	 * the correct order (as specified by {@link AgentSequenceType}).
	 * <p>
	 * It is important that this list is managed together with <code>components</code>.
	 */
	protected ArrayList<AgentComponent>					componentOrder				= new ArrayList<AgentComponent>();
																					
	// TODO: add support for non-standard components.
	// /**
	// * The {@link Map} that holds the non-standard components (names are {@link String}).
	// */
	// protected Map<String, AgentComponent> otherComponents = new HashMap<String,
	// AgentComponent>();
	
	/**
	 * A synchronized queue of agent events, as posted by the components.
	 */
	protected LinkedBlockingQueue<AgentEvent>			eventQueue					= null;
																					
	/**
	 * The thread managing the agent's lifecycle (managing events).
	 */
	transient protected Thread							agentThread					= null;
																					
	/**
	 * The agent state. See {@link AgentState}. Access to this member should be synchronized with the lock of
	 * <code>eventQueue</code>.
	 */
	protected AgentState								agentState					= AgentState.STOPPED;

			
	/**
	 * This switch activates the use of the {@link #localLog}.
	 */
	protected boolean									USE_LOCAL_LOG				= true;
																					
	/**
	 * Starts the lifecycle of the agent. All components will receive an {@link AgentEventType#AGENT_START} event.
	 * 
	 * @return true if the event has been successfully posted. See <code>postAgentEvent()</code>.
	 */
	@Override
	public boolean start()
	{
		
		return postAgentEvent(new AgentEvent(AgentEvent.AgentEventType.AGENT_START));
	}
	
	/**
	 * Instructs the agent to unload all components and exit. All components will receive an
	 * {@link AgentEventType#AGENT_STOP} event.
	 * <p>
	 * No events will be successfully received after this event has been posted.
	 * 
	 * @return true if the event has been successfully posted. See <code>postAgentEvent()</code>.
	 */
	public boolean exit()
	{
		return postAgentEvent(new AgentEvent(AgentEvent.AgentEventType.AGENT_STOP));
	}
	
	/**
	 * Alias for {@link #exit()}.
	 */
	@Override
	public boolean stop()
	{
		return exit();
	}
	
	/**
	 * This method contains some legacy code for forcing the agent thread to stop. Testing currently shows that posting
	 * a stop event should be sufficient, so this method should only be used in case of malfunction that cannot be
	 * solved otherwise at the time.
	 */
	@Deprecated
	public void killAgent()
	{
		exit();
		try
		{
			agentThread.join(EXIT_TIMEOUT);
		} catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Instructs the agent to switch state between <code>STOPPED</code> and <code>TRANSIENT</code>.
	 * 
	 * @return <code>true</code> if the agent is now in the <code>TRANSIENT</code> state, <code>false</code> otherwise.
	 * 		
	 * @throws RuntimeException
	 *             if the agent was in any other state than the two.
	 */
	public boolean toggleTransient() throws RuntimeException
	{
		return FSMToggleTransient();
	}
	
	@Override
	public boolean setPlatformLink(PlatformLoader.PlatformLink link)
	{
		if(!canAddComponents() || isRunning())
			return false;
		platformLink = link;
		return true;
	}
	
	/**
	 * The method should be called by an agent component (relayed through {@link AgentComponent}) to disseminate a an
	 * {@link AgentEvent} to the other components.
	 * <p>
	 * If the event has been successfully posted, the method returns <code>true</code>, guaranteeing that, except in the
	 * case of abnormal termination, the event will be processed eventually. Otherwise, it returns <code>false</code>,
	 * indicating that either the agent has not been started, or has been instructed to exit, or is in another
	 * inappropriate state.
	 * 
	 * @param event
	 *            the event to disseminate.
	 * @return <code>true</code> if the event has been successfully posted; <code>false</code> otherwise.
	 */
	protected boolean postAgentEvent(AgentEvent event)
	{
		event.lock();
		Log.v("smth", "Add agent envent");
		
		if(!canPostEvent(event))
			return false;
			
		AgentState futureState = FSMEventIn(event.getType(), event.isSet(TRANSIENT_EVENT_PARAMETER));
		
		try
		{
			if(eventQueue != null)
				synchronized(eventQueue)
				{
					if(futureState != null)
						agentState = futureState;
					eventQueue.put(event);
					eventQueue.notify();
				}
			else
				return false;
		} catch(InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Change the state of the agent (if it is the case) and perform other actions, <i>before</i> an event is added to
	 * the event queue. It is presumed that the event has already been checked with {@link #canPostEvent(AgentEvent)}
	 * and that the event can indeed be posted to the queue in the current state.
	 * <p>
	 * If the event was {@link AgentEventType#AGENT_START}, the agent will enter {@link AgentState#STARTING}, the event
	 * queue is created and the agent thread is started.
	 * <p>
	 * If the event was {@link AgentEventType#AGENT_STOP}, the agent will enter {@link AgentState#STOPPING}.
	 * 
	 * @param eventType
	 *            - the type of the event.
	 * @param fromToTransient
	 *            - <code>true</code> if the agent should enter / exit from the {@link AgentState#TRANSIENT} state.
	 * @return the state the agent should enter next (the actual state change will happen in
	 *         {@link #postAgentEvent(AgentEvent)}, together with posting the event to the queue.
	 */
	protected AgentState FSMEventIn(AgentEvent.AgentEventType eventType, boolean fromToTransient)
	{
        Log.v("smth", "FSMEventIn reached");
		AgentState futureState = null;
		switch(eventType)
		{
		case AGENT_START:
			futureState = AgentState.STARTING;
			
			if(eventQueue != null)
				Log.v("core","event queue already present");
			eventQueue = new LinkedBlockingQueue<AgentEvent>();
			agentThread = new Thread(new AgentThread());
			agentThread.start();
			break;
		case AGENT_STOP:
			futureState = AgentState.STOPPING;
			break;
		default:
			// nothing to do
		}
		if(futureState != null)
			Log.v("core", "Agent state is soon [][]" + futureState+ (fromToTransient ? "transient" : ""));

		return futureState;
	}
	
	/**
	 * Change the state of the agent (if it is the case) and perform other actions, <i>after</i> an event has been
	 * processed by all components.
	 * <p>
	 * If the event was {@link AgentEventType#AGENT_START}, the state will be {@link AgentState#RUNNING}.
	 * <p>
	 * If the event was {@link AgentEventType#AGENT_STOP}, the event queue will be consumed, the state will be
	 * {@link AgentState#STOPPED} or {@link AgentState#TRANSIENT} (depending on the event parameters), and the log and
	 * thread will exit.
	 * 
	 * @param eventType
	 *            - the type of the event.
	 * @param toFromTransient
	 *            - <code>true</code> if the agent should enter / exit from the {@link AgentState#TRANSIENT} state.
	 * @return <code>true</code> if the agent thread should exit.
	 */
	protected boolean FSMEventOut(AgentEvent event, boolean toFromTransient)
	{
		boolean stateChanged = false;
		switch(event.getType())
		{
		case AGENT_START: // the agent has completed starting and all components are up.
			synchronized(eventQueue)
			{
				agentState = AgentState.RUNNING;
				stateChanged = true;
			}
			log("state is now ", agentState);
			break;
		case AGENT_STOP:
			synchronized(eventQueue)
			{
				if(!eventQueue.isEmpty())
				{
					while(!eventQueue.isEmpty())
						log("ignoring event ", eventQueue.poll());
				}
				if(toFromTransient)
					agentState = AgentState.TRANSIENT;
				else
					agentState = AgentState.STOPPED;
				stateChanged = true;
			}
			log("state is now ", agentState);
			eventQueue = null;
	//		localLog.doExit();
			break;
		default:
			// do nothing
		}
		if(stateChanged)
			getPlatformLink().onAgentStateChanged(event, this);
		// if the agent is now stopped (or transient) the agent thread can exit.
		return isStopped();
	}
	
	/**
	 * Changes the agent state between {@link AgentState#STOPPED} and {@link AgentState#TRANSIENT}. If the agent is in
	 * any other state, an exception is thrown.
	 * 
	 * @return <code>true</code> if the agent is now (after the change) in the {@link AgentState#TRANSIENT} state.
	 *         <code>false</code> if it is now in {@link AgentState#STOPPED}.
	 * 		
	 * @throws RuntimeException
	 *             if the agent is in any other state than the two above.
	 */
	protected boolean FSMToggleTransient() throws RuntimeException
	{
		switch(agentState)
		{
		case STOPPED:
			agentState = AgentState.TRANSIENT;
			break;
		case TRANSIENT:
			agentState = AgentState.STOPPED;
			break;
		default:
			throw new IllegalStateException("Unable to toggle TRANSIENT state while in " + agentState);
		}
		/*
		if(localLog.getUnitName() != null)
			// protect against locking the log
			log("state switched to ", agentState);
			*/
		return isTransient();
	}
	
	/**
	 * Adds a component to the agent that has been configured beforehand. The agent will register with the component, as
	 * parent.
	 * <p>
	 * The component will be identified by the agent by means of its <code>getComponentName</code> method. Only one
	 * instance per name (functionality) will be allowed.
	 * 
	 * @param component
	 *            - the {@link AgentComponent} instance to add.
	 * @return the agent instance itself. This can be used to continue adding other components.
	 */
	public CompositeAgent addComponent(AgentComponent component)
	{
		if(!canAddComponents())
			throw new IllegalStateException("Cannot add components in state [" + agentState + "].");
		if(component == null)
			throw new InvalidParameterException("Component is null");
		if(hasComponent(component.getComponentName()))
			throw new InvalidParameterException(
					"Cannot add multiple components for name [" + component.getComponentName() + "]");
		components.put(component.getComponentName(), component);
		componentOrder.add(component);
		component.setParent(this);
		return this;
	}
	
	/**
	 * Removes an existing component of the agent.
	 * <p>
	 * The method will call the method <code>getComponentName()</code> of the component with a <code>null</code>
	 * parameter.
	 * 
	 * @param name
	 *            - the name of the component to remove (as instance of {@link AgentComponentName}.
	 * @return a reference to the just-removed component instance.
	 */
	public AgentComponent removeComponent(AgentComponent.AgentComponentName name)
	{
		if(!hasComponent(name))
			throw new InvalidParameterException("Component [" + name + "] does not exist");
		AgentComponent component = getComponent(name);
		componentOrder.remove(component);
		components.remove(component);
		return component;
	}
	
	/**
	 * Returns <code>true</code> if the agent contains said component.
	 * 
	 * @param name
	 *            - the name of the component to search (as instance of {@link AgentComponentName}.
	 * @return <code>true</code> if the component exists, false otherwise.
	 */
	protected boolean hasComponent(AgentComponent.AgentComponentName name)
	{
		return components.containsKey(name);
	}
	
	/**
	 * Retrieves a component of the agent, by name.
	 * <p>
	 * It is <i>strongly recommended</i> that the reference is not kept, as the component may be removed without notice.
	 * 
	 * @param name
	 *            - the name of the component to retrieve (as instance of {@link AgentComponentName} .
	 * @return the {@link AgentComponent} instance, if any. <code>null</code> otherwise.
	 */
	protected AgentComponent getComponent(AgentComponent.AgentComponentName name)
	{
		return components.get(name);
	}
	
	/**
	 * Retrieves the platform link.
	 * 
	 * @return the platform link.
	 */
	protected PlatformLoader.PlatformLink getPlatformLink()
	{
		return platformLink;
	}
	
	/**
	 * Returns the name of the agent. It is the name that has been set through the <code>AGENT_NAME</code> parameter.
	 * 
	 * @return the name of the agent.
	 */
	@Override
	public String getAgentName()
	{ // TODO name should be cached
		String agentName = null;
		if(hasComponent(AgentComponent.AgentComponentName.PARAMETRIC_COMPONENT))
			agentName = ((ParametricComponent) getComponent(AgentComponent.AgentComponentName.PARAMETRIC_COMPONENT))
					.parVal(AgentParameterName.AGENT_NAME);
		return agentName;
	}
	
	/**
	 * Checks if the agent is currently in <code>RUNNING</code> state. In case components are added during this state,
	 * they must consider that the agent is already running and no additional {@link AgentEventType#AGENT_START} events
	 * will be issued.
	 * 
	 * @return <code>true</code> if the agent is currently <code>RUNNING</code>; <code>false</code> otherwise.
	 */
	@Override
	public boolean isRunning()
	{
		if(eventQueue == null)
			return false;
		synchronized(eventQueue)
		{
			return agentState == AgentState.RUNNING;
		}
	}
	
	/**
	 * Checks if the agent is currently in <code>STOPPED</code> state.
	 * 
	 * @return <code>true</code> if the agent is currently <code>STOPPED</code>; <code>false</code> otherwise.
	 */
	@Override
	public boolean isStopped()
	{
		if(eventQueue == null)
			return agentState == AgentState.STOPPED || agentState == AgentState.TRANSIENT;
		synchronized(eventQueue)
		{
			return agentState == AgentState.STOPPED || agentState == AgentState.TRANSIENT;
		}
	}
	
	/**
	 * Checks whether the agent is in the <code>TRANSIENT</code> state.
	 * 
	 * @return <code>true</code> if the agent is currently <code>TRANSIENT</code>; <code>false</code> otherwise.
	 */
	public boolean isTransient()
	{
		if(eventQueue == null)
			return agentState == AgentState.TRANSIENT;
		synchronized(eventQueue)
		{
			return agentState == AgentState.TRANSIENT;
		}
	}
	
	/**
	 * Checks if the state of the agent allows adding components. Components should not be added in intermediary states
	 * in which the agent is starting or stopping.
	 * 
	 * @return <code>true</code> if in the current state components can be added.
	 */
	public boolean canAddComponents()
	{
		if(eventQueue == null)
			return true;
		synchronized(eventQueue)
		{
			return (agentState == AgentState.STOPPED) || (agentState == AgentState.RUNNING);
		}
	}
	
	/**
	 * Checks whether the specified event can be posted in the current agent state.
	 * <p>
	 * Basically, there are two checks:
	 * <ul>
	 * <li>Any event except {@link AgentEventType#AGENT_START} can be posted only in the {@link AgentState#RUNNING}
	 * state.
	 * <li>If the {@link AgentEventType#AGENT_START} is posted while the agent is in the {@link AgentState#TRANSIENT}
	 * state, it needs to feature a parameter called {@value #TRANSIENT_EVENT_PARAMETER} (with any value).
	 * <li>The {@link AgentEventType#AGENT_START} event can be posted while the agent is {@link AgentState#STOPPED}.
	 * 
	 * @param event
	 * @return
	 */
	protected boolean canPostEvent(AgentEvent event)
	{
		if(eventQueue == null)
			switch(event.getType())
			{
			case AGENT_START:
				if(agentState == AgentState.TRANSIENT)
					return event.isSet(TRANSIENT_EVENT_PARAMETER);
				return agentState == AgentState.STOPPED;
			default:
				return agentState == AgentState.RUNNING;
			}
		synchronized(eventQueue)
		{
			switch(event.getType())
			{
			case AGENT_START:
				if(agentState == AgentState.TRANSIENT)
					return event.isSet(TRANSIENT_EVENT_PARAMETER);
				return agentState == AgentState.STOPPED;
			default:
				return agentState == AgentState.RUNNING;
			}
		}
	}
	
	/**
	 * Returns the name of the agent.
	 */
	@Override
	public String toString()
	{
		return getAgentName();
	}
	
	/**
	 * Use this method to output to the local log. Do not abuse. The call is relayed to a
	 * {@link UnitComponent#li(String, Object...)} call.
	 * 
	 * @param message
	 *            - the message.
	 * @param arguments
	 *            - objects to include in the message.
	 */
	protected void log(String message, Object... arguments)
	{
		/*
		if(USE_LOCAL_LOG && (localLog != null))
		{
			if(localLog.getUnitName() == null)
			{
				if(getAgentName() != null)
					localLog.setUnitName(getAgentName() + "#");
				else
					localLog.setUnitName(super.toString().replace(getClass().getName(), "CompAg") + "#");
			}
			localLog.li(message, arguments);
		}
		*/
	}
	
	public void resume()
	{
		eventQueue = new LinkedBlockingQueue<AgentEvent>();
		agentThread = new Thread(new AgentThread());
		agentThread.start();
		System.out.println("Composite agent started " + componentOrder.size());
		for(AgentComponent ac : componentOrder)
		{
			ac.atAgentResume(new AgentEvent(AgentEvent.AgentEventType.AGENT_START));
			ac.setParent(this);
			
		}
	}
}
