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
package tatami.core.agent.mobility;


import Logging.Log;
import tatami.core.agent.AgentComponent;
import tatami.core.agent.AgentComponent.AgentComponentName;
import tatami.core.agent.AgentEvent;
import tatami.core.agent.CompositeAgent;

public class MobilityComponent extends AgentComponent
{
	public static final String DESTINATION_PARAMETER = "mobility_destination";
	
	public MobilityComponent()
	{
		super(AgentComponentName.MOBILITY_COMPONENT);
		registerHandler(AgentEvent.AgentEventType.AGENT_MESSAGE, new AgentEvent.AgentEventHandler() {
			@Override
			public void handleEvent(AgentEvent event)
			{
			
			}
		});
	}
	
	public String extractDestination(Object eventData)
	{
		return ((AgentEvent) eventData).get(DESTINATION_PARAMETER);
	}
	
	public boolean move(String nodeName)
	{
		Log.v("mobility", "===============================" + getAgentName());
		// TODO notify platform (in an extension of this class) that this agent is going to move to nodeName
		postAgentEvent((AgentEvent) new AgentEvent(AgentEvent.AgentEventType.AGENT_STOP)
				.add(CompositeAgent.TRANSIENT_EVENT_PARAMETER, null).add(DESTINATION_PARAMETER, nodeName));
		return true; // if it is most likely that the agent will move
	}
}
