package components.dev.actuators;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import Logging.Log;
import core.agent.AgentComponent;
import core.agent.AgentEvent;
import core.agent.CompositeAgent;

public class ElectricMotorComponent extends AgentComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String thisAgent = null;

	final GpioController gpio;

	// provision gpio pin #01 as an output pin and turn on
	GpioPinDigitalOutput Motor1A;
	GpioPinDigitalOutput Motor1B;
	GpioPinDigitalOutput Motor2A;
	GpioPinDigitalOutput Motor2B;

	public ElectricMotorComponent() {
		super(AgentComponentName.MOTOR_COMPONENT);
		
		gpio = GpioFactory.getInstance();
		
		Motor1A = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW);
		Motor1B = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, PinState.LOW);
		
		
		
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
	}
	
	@Override
	protected void atSimulationStart(AgentEvent event) {
		super.atSimulationStart(event);
		Log.v("distance_sensor", "Simulation started");
		
		gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.HIGH);
		
	}
	
	@Override
	protected void atAgentStop(AgentEvent event) {
		super.atAgentStop(event);
		gpio.shutdown();
	}
	
	protected void atAgentResume(AgentEvent event) {		
	}

}
