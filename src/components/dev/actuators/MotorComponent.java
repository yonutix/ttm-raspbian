package components.dev.actuators;

import java.util.Random;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import Logging.Log;
import tatami.core.agent.AgentComponent;
import tatami.core.agent.AgentComponent.AgentComponentName;
import tatami.core.agent.AgentEvent;
import tatami.core.agent.CompositeAgent;

public class MotorComponent extends AgentComponent{
	
	enum Direction{
		LEFT, 
		RIGHT,
		FORWARD,
		BACKWARD,
		STAY
	}
	
	Random r = new Random();
	
	Direction currentDirection =  Direction.STAY;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String thisAgent = null;

	GpioController gpio;

	// provision gpio pin #01 as an output pin and turn on
	GpioPinDigitalOutput Motor1A;
	GpioPinDigitalOutput Motor1B;
	
	GpioPinDigitalOutput Motor2A;
	GpioPinDigitalOutput Motor2B;

	public MotorComponent() {
		super(AgentComponentName.MOTOR_COMPONENT);
		
		gpio = GpioFactory.getInstance();
		
		Motor1A = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, PinState.LOW);
		Motor1B = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, PinState.LOW);
		
		Motor2A = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, PinState.LOW);
		Motor2B = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.LOW);
		
		
		
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
		Log.v("motor_actuator", "Agent started atAgentStart HC-SR04");
		if (getParent() != null) {
			thisAgent = getAgentName();
		}
	}
	
	void test(){
		long ti = System.currentTimeMillis();
		
		while((System.currentTimeMillis() - ti) < 10000){
			int rand = r.nextInt()%5;
			if(rand == 0){
				pulseForward();
			}
			if(rand == 1){
				pulseBackward();
			}
			if(rand == 2){
				pulseLeft();
			}
			if(rand == 3){
				pulseRight();
			}
			if(rand == 4){
				stay();
			}
		}
	}
	
	void pulseForward(){
		Motor1A.pulse(500, PinState.HIGH, true);
		Motor2A.pulse(500, PinState.HIGH, true);
	}
	
	
	void pulseBackward(){
		Motor1B.pulse(500, PinState.HIGH, true);
		Motor2B.pulse(500, PinState.HIGH, true);
	}
	
	void pulseLeft(){
		Motor1A.pulse(500, PinState.HIGH, true);
		Motor2B.pulse(500, PinState.HIGH, true);
	}
	
	void pulseRight(){
		Motor1B.pulse(500, PinState.HIGH, true);
		Motor2A.pulse(500, PinState.HIGH, true);
	}
	
	void stay(){
		Motor1B.pulse(500, PinState.LOW, true);
		Motor2A.pulse(500, PinState.LOW, true);
	}
	
	@Override
	protected void atSimulationStart(AgentEvent event) {
		super.atSimulationStart(event);
		test();
	}
	
	@Override
	protected void atAgentStop(AgentEvent event) {
		super.atAgentStop(event);
		gpio.shutdown();
	}
	
	protected void atAgentResume(AgentEvent event) {		
	}

}
