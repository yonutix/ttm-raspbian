package components.dev.mobility;

import Logging.Log;
import components.dev.sensors.ForceSensorComponent;
import components.dev.sensors.HC_SR04Component;
import components.dev.sensors.MMA8452QComponent;
import core.agent.AgentComponent;
import core.agent.mobility.MobilityComponent;
import core.agent.parametric.ParametricComponent;
import core.control.ControlComponent;
import core.simulation.PlatformLoader;
import websocket.WebSocketMessagingComponent;
import websocket.WebSocketMessagingPlatform;

/**
 * Created by yonutix on 01/04/2016.
 */

public class ComponentFactory {

    private static ComponentFactory singleton = null;

    public static final String PARAMETRIC_COMPONENT = "tatami.core.agent.parametric.ParametricComponent";
    public static final String MOBILITY_COMPONENET = "tatami.core.agent.mobility.MobilityComponent";
    public static final String WEBSOCKET_COMPONENT = "websocket.WebSocketMessagingComponent";
    public static final String STATE_AGENT_COMPONENT = "StateAgentTestComponent";
    public static final String CONTROL_COMPONENT = "tatami.core.agentcontrol.ControlComponent";
    public static final String HCSR04 = "components.dev.sensors.HC_SR04Component";
    public static final String MMA8452Q = "components.dev.sensors.MMA8452QComponent";
    public static final String FORCE_SENSOR = "components.dev.sensors.ForceSensorComponent";
    


    public static final String PLATFORM_WEBSOCKET_MESSAGING = "websocket";

    private ComponentFactory(){

    }

    public static ComponentFactory getInst(){
        if(singleton == null){
            singleton = new ComponentFactory();
        }
        return singleton;
    }




    public AgentComponent getComponent(String className, Object... arg){
        Log.v("smth", "reach here " + className);
        if(className.compareTo(PARAMETRIC_COMPONENT) == 0){
            Log.v("smth", "Parametric component");
            return new ParametricComponent();
        }

        if(className.compareTo(WEBSOCKET_COMPONENT) == 0){
            Log.v("smth", "Parametric component");
            return new WebSocketMessagingComponent();
        }

        if(className.compareTo(MOBILITY_COMPONENET) == 0){
            Log.v("smth", "Parametric component");
            return new MobilityComponent();
        }
        if(className.compareTo(STATE_AGENT_COMPONENT) == 0){
            Log.v("smth", "Parametric component");
            return new StateAgentTestComponent();
        }

        if(className.compareTo(CONTROL_COMPONENT) == 0){
            Log.v("smth", "Parametric component");
            return new ControlComponent();
        }
        
        if(className.compareTo(HCSR04) == 0){
            Log.v("smth", "Parametric component");
            return new HC_SR04Component();
        }
        
        if(className.compareTo(MMA8452Q) == 0){
            Log.v("smth", "Parametric component");
            return new MMA8452QComponent();
        }
        
        if(className.compareTo(FORCE_SENSOR) == 0){
            Log.v("smth", "Parametric component");
            return new ForceSensorComponent();
        }

        Log.e("smth", "Error loading component " + className);

        return null;
    }

    public PlatformLoader getPlatform(String platformName, Object... arg){
        if(platformName.compareTo(PLATFORM_WEBSOCKET_MESSAGING) == 0){
            return new WebSocketMessagingPlatform();
        }

        return null;
    }

}
