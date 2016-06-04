package components.dev.sensors;

import Logging.Log;
import core.agent.AgentComponent;
import core.agent.AgentEvent;
import core.agent.CompositeAgent;

public class ForceSensorComponent extends AgentComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Cache for the name of this agent.
	 */
	String thisAgent = null;
	
	
	public ForceSensorComponent() {
		super(AgentComponentName.FORCESENSOR_COMPONENT);
		
		registerHandler(AgentEvent.AgentEventType.AGENT_MESSAGE, new AgentEvent.AgentEventHandler() {
			@Override
			public void handleEvent(AgentEvent event)
			{

			}
		});
	}
	
	@Override
	protected String getAgentName() {
		return super.getAgentName();
	}

	@Override
	protected AgentComponent getAgentComponent(AgentComponentName name) {
		return super.getAgentComponent(name);
	}

	@Override
	protected void componentInitializer() {
		super.componentInitializer();
	}

	@Override
	protected void parentChangeNotifier(CompositeAgent oldParent) {
		super.parentChangeNotifier(oldParent);
	}
	
	
	@Override
	protected void atAgentStart(AgentEvent event) {
		super.atAgentStart(event);
		Log.v("smth", "Agent started atAgentStart Force sensor");
		if (getParent() != null) {
			thisAgent = getAgentName();
		}
	}
	
	@Override
	protected void atSimulationStart(AgentEvent event) {
		super.atSimulationStart(event);
	}
	
	@Override
	protected void atAgentStop(AgentEvent event) {
		super.atAgentStop(event);
	}
	
	protected void atAgentResume(AgentEvent event) {
	}
	
	

}
