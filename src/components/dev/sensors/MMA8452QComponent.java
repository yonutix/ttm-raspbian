package components.dev.sensors;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import Logging.Log;
import components.dev.mobility.StateAgentTestComponent;
import core.agent.AgentComponent;
import core.agent.AgentEvent;
import core.agent.CompositeAgent;

class PeriodicSample extends TimerTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	I2CDevice accelerometerReference;

	/**
	 * 
	 * @param parent
	 *            The parent of this object
	 */
	public PeriodicSample(MMA8452QComponent parent) {
		accelerometerReference = parent.getDeviceRef();
	}

	@Override
	public void run() {
		try {
			byte[] buffer = new byte[2];
			buffer[0] = MMA8452QComponent.CTRL_REG1;
			buffer[1] = 0x3;

			accelerometerReference.write(buffer, 0, buffer.length);

			buffer[0] = MMA8452QComponent.XYZ_DATA_CFG;
			buffer[1] = 0x0;

			accelerometerReference.write(buffer, 0, buffer.length);
			double g_mul = 2.0 / 128;

			buffer[0] = MMA8452QComponent.SYSMOD;
			buffer[1] = 0x1;

			accelerometerReference.write(buffer, 0, buffer.length);

			accelerometerReference.write((byte) 0x0);

			byte[] result = new byte[4];
			accelerometerReference.read(result, 0, 4);

			double x = result[1] * g_mul;
			double y = result[2] * g_mul;
			double z = result[3] * g_mul;

			Log.v("accelerometer", x + " " + y + " " + z);

		} catch (Exception e) {
			Log.v("accelerometer", "Failed to read/write i2c");
		}

	}
}

public class MMA8452QComponent extends AgentComponent {

	final int DEFAULT_I2C_ADDRESS = 0x1C;
	final int DEFAULT_I2C_BUS = 1;

	static final int SYSMOD = 0x0B;
	static final int XYZ_DATA_CFG = 0x0E;
	static final int CTRL_REG1 = 0x2A;

	I2CBus i2c_bus;
	I2CDevice accelerometer;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Cache for the name of this agent.
	 */
	String thisAgent = null;

	public MMA8452QComponent() {
		super(AgentComponentName.MMA8452Q_COMPONENT);

		registerHandler(AgentEvent.AgentEventType.AGENT_MESSAGE, new AgentEvent.AgentEventHandler() {
			@Override
			public void handleEvent(AgentEvent event) {

			}
		});
	}

	private void init() {
		try {
			i2c_bus = I2CFactory.getInstance(DEFAULT_I2C_BUS);
			accelerometer = i2c_bus.getDevice(DEFAULT_I2C_ADDRESS);
		} catch (Exception e) {
			Log.v("accelerometer", "" + e.getMessage());
			e.printStackTrace();
		}
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
		Log.v("smth", "Agent started atAgentStart MMA8452Q");
		if (getParent() != null) {
			thisAgent = getAgentName();
		}
		pingTimer = new Timer();
		init();
	}

	@Override
	protected void atSimulationStart(AgentEvent event) {
		super.atSimulationStart(event);
		pingTimer.schedule(new PeriodicSample(this), PING_INITIAL_DELAY, PING_PERIOD);
	}

	@Override
	protected void atAgentStop(AgentEvent event) {
		super.atAgentStop(event);
	}

	protected void atAgentResume(AgentEvent event) {
	}

	public I2CDevice getDeviceRef() {
		return accelerometer;
	}

}
