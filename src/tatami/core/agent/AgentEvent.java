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

import tatami.core.util.ParameterSet;

/**
 * The class stores an agent event, characterized by its type and, optionally, a set of parameters that have names.
 * <p>
 * The class is baked by a {@link ParameterSet}.
 * 
 * @author andreiolaru
 */
public class AgentEvent extends ParameterSet
{
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = 379942317804425591L;

	/**
	 * The enumeration specified the full set of agent-internal events that can occur.
	 * 
	 * @author Andrei Olaru
	 */
	public static enum AgentEventType {
		
		/**
		 * Event occurs when the agent starts and the components need to be initialized.
		 */
		AGENT_START(AgentSequenceType.CONSTRUCTIVE),
		
		/**
		 * Event occurs when the agent must be destroyed and components need to close.
		 */
		AGENT_STOP(AgentSequenceType.DESTRUCTIVE),
		
		/**
		 * Event occurs when the agent has received a message. Events of this type will only get posted when no
		 * messaging component exists; otherwise, message routing will be handled by the messaging component.
		 */
		AGENT_MESSAGE(AgentSequenceType.UNORDERED),
		
		/**
		 * Event occurs when an active input (see AgentGui) is activated by the user and the agent must react to it.
		 */
		GUI_INPUT(AgentSequenceType.UNORDERED),
		
		/**
		 * Event occurs when the start of the simulation is requested by the user.
		 */
		SIMULATION_START(AgentSequenceType.UNORDERED),
		
		/**
		 * Event occurs when the simulation is paused by the user.
		 */
		SIMULATION_PAUSE(AgentSequenceType.UNORDERED),
		
		;
		
		/**
		 * The sequence type that is characteristic to the event.
		 */
		protected AgentSequenceType	sequenceType;
		
		/**
		 * The constructor assigns a sequence type to the event type.
		 * 
		 * @param sequence
		 *            - the sequence type, as a {@link AgentSequenceType} instance.
		 */
		private AgentEventType(AgentSequenceType sequence)
		{
			sequenceType = sequence;
		}
		
		/**
		 * @return the type of sequence associated with the event type.
		 */
		public AgentSequenceType getSequenceType()
		{
			return sequenceType;
		}
	}
	
	/**
	 * The sequence type of an agent event specifies the order in which components should be notified of the event.
	 * 
	 * @author Andrei Olaru
	 */
	public static enum AgentSequenceType {
		
		/**
		 * The components should be invoked in the order they were added.
		 */
		CONSTRUCTIVE,
		
		/**
		 * The components should be invoked in inverse order as to that in which they were added.
		 */
		DESTRUCTIVE,
		
		/**
		 * The components can be invoked in any order.
		 */
		UNORDERED,
	}
	
	/**
	 * The interface should be implemented by a class that can handle agent events for an agent component. Each
	 * {@link AgentComponent} instance is able to register, for each event, an event handler.
	 * <p>
	 * The class also contains enumerations relevant to event handling: event types ( {@link AgentEventType}) and types
	 * of sequences for events ({@link AgentSequenceType}).
	 * 
	 * @author Andrei Olaru
	 */
	public interface AgentEventHandler extends Serializable
	{
		/**
		 * The method is invoked whenever the event is posted to the {@link CompositeAgent} the component is part of.
		 * <p>
		 * The handlers in various components will be invoked (through the method in {@link AgentComponent}) in the
		 * order specified by the {@link AgentSequenceType} associated with the event.
		 * 
		 * @param event
		 *            - the event that occurred.
		 */
		public void handleEvent(AgentEvent event);
	}
	
	/**
	 * The name of the parameter in the parameter set, storing the type of the event.
	 */
	protected static final String EVENT_TYPE_PARAMETER_NAME = "EVENT_TYPE";
	
	/**
	 * Creates a new agent event.
	 * 
	 * @param eventType
	 *            - the type of the event.
	 */
	public AgentEvent(AgentEventType eventType)
	{
		setType(eventType);
	}
	
	/**
	 * Getter for the type of the event.
	 * 
	 * @return the type of the event.
	 */
	public AgentEventType getType()
	{
		return (AgentEventType) getObject(EVENT_TYPE_PARAMETER_NAME);
	}
	
	/**
	 * Sets the type of the event.
	 * 
	 * @param eventType
	 *            - the type of the event.
	 */
	private void setType(AgentEventType eventType)
	{
		locked();
		addObject(EVENT_TYPE_PARAMETER_NAME, eventType);
	}
}
