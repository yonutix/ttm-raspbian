package components.dev.sensors;

import Logging.Log;
import core.agent.AgentComponent;
import core.agent.AgentEvent;
import core.agent.CompositeAgent;

public class HC_SR04Component extends AgentComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Cache for the name of this agent.
	 */
	String thisAgent = null;
	
	
	public HC_SR04Component() {
		super(AgentComponentName.HC_SR04_COMPONENT);
		
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
		Log.v("smth", "Agent started atAgentStart HC-SR04");
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
		// AgentEvent event = new AgentEvent(AgentEventType.BEFORE_MOVE);
		super.atAgentStop(event);
	}
	
	protected void atAgentResume(AgentEvent event) {
	}
	
	

}
