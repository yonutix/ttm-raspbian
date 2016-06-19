package components.dev.sensors;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import Logging.Log;
import core.agent.AgentComponent;
import core.agent.AgentEvent;
import core.agent.CompositeAgent;



class EchoListener implements GpioPinListenerDigital{
	UpdateClock mclk;
	
	public EchoListener(UpdateClock clk){
		mclk = clk;
	}

	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		if(event.getState() == PinState.HIGH){
			
		}
		else{
			mclk.updateDistance(System.currentTimeMillis());
			mclk.echoReceived();
		}
	}
}


class UpdateClock extends TimerTask implements Serializable {
	
	
	static int x = 0;
	long triggered;
	
	enum State {
		EMIT_START, EMIT_END
	};
	
	State currentState = State.EMIT_START;

	private static final long serialVersionUID = 1L;
	HC_SR04Component mParent;
	/**
	 * 
	 * @param parent
	 *            The parent of this object
	 */
	public UpdateClock(HC_SR04Component parent) {
		mParent = parent;
	}
	
	public void updateDistance(long received){
		mParent.updateDistance(((double)(received - triggered))/1000 * 17150);
		
	}
	
	public void echoReceived(){
		currentState = State.EMIT_START;
	}

	@Override
	public void run() {
		if (currentState == State.EMIT_START){
			currentState = State.EMIT_END;
			mParent.getTriggerPin().pulse(300, PinState.HIGH, true);
			triggered = System.currentTimeMillis();
			
		}
	}
}



public class HC_SR04Component extends AgentComponent{
	
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
	
	UpdateClock updateClock;
	
	
	final GpioController gpio;
    
    // provision gpio pin #01 as an output pin and turn on
    GpioPinDigitalOutput triggerPin ;
    GpioPinDigitalInput  echoPin;
	
	double mDistance = -1;
	
	
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
		updateClock = new UpdateClock(this);
		
		gpio = GpioFactory.getInstance();
		
		triggerPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04);
		echoPin    = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05);
		
		echoPin.addListener(new EchoListener(updateClock));
		
		
		
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
		Log.v("distance_sensor", "Agent started atAgentStart HC-SR04");
		if (getParent() != null) {
			thisAgent = getAgentName();
		}
		pingTimer = new Timer();
	}
	
	@Override
	protected void atSimulationStart(AgentEvent event) {
		super.atSimulationStart(event);
		Log.v("distance_sensor", "Simulation started");
		pingTimer.schedule(updateClock, PING_INITIAL_DELAY, PING_PERIOD);
	}
	
	@Override
	protected void atAgentStop(AgentEvent event) {
		super.atAgentStop(event);
		gpio.shutdown();
	}
	
	protected void atAgentResume(AgentEvent event) {		
	}
	
	public GpioPinDigitalOutput getTriggerPin(){
		return triggerPin;
	}
	
	public GpioPinDigitalInput getEchoPin(){
		return echoPin;
	}
	
	public void updateDistance(double distance){
		mDistance = distance;
		Log.v("distance", "" + distance);
	}

}
