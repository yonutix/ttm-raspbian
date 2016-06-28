package components.dev.sensors;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import Logging.Log;
import tatami.core.agent.AgentComponent;
import tatami.core.agent.AgentComponent.AgentComponentName;
import tatami.core.agent.AgentEvent;
import tatami.core.agent.CompositeAgent;


class ForceSensorUpdateClock extends TimerTask implements Serializable {
	private static final long serialVersionUID = 1L;
	
	ForceSensorComponent mParent;
	/**
	 * 
	 * @param parent
	 *            The parent of this object
	 */
	public ForceSensorUpdateClock(ForceSensorComponent parent) {
		mParent = parent;
	}

	@Override
	public void run() {
		
	}
}



public class ForceSensorComponent extends AgentComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Initial delay before the first ping message.
	 */
	protected static final long PING_INITIAL_DELAY = 0;
	/**
	 * Time between ping messages.
	 */
	protected static final long PING_PERIOD = 1000;

	/**
	 * Timer for pinging.
	 */
	transient Timer pingTimer = null;
	
	ForceSensorUpdateClock updateClock;
	
	/**
	 * Cache for the name of this agent.
	 */
	String thisAgent = null;
	
	
	public ForceSensorComponent() {
		super(AgentComponentName.FORCESENSOR_COMPONENT);
		updateClock = new ForceSensorUpdateClock(this);
		
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
		pingTimer = new Timer();
	}
	
	@Override
	protected void atSimulationStart(AgentEvent event) {
		super.atSimulationStart(event);
		pingTimer.schedule(updateClock, PING_INITIAL_DELAY, PING_PERIOD);
	}
	
	@Override
	protected void atAgentStop(AgentEvent event) {
		super.atAgentStop(event);
	}
	
	protected void atAgentResume(AgentEvent event) {
	}
	
	

}
