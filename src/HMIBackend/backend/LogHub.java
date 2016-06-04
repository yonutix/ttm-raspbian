package HMIBackend.backend;

import java.util.HashMap;

import Logging.Log;
import core.simulation.external_interface.ExtAgentInfo;
import core.simulation.external_interface.ExternalInterfaceListener;

/**
 * Created by uidk9631 on 17.05.2016.
 */
public class LogHub implements  ExternalInterfaceListener.AgentRelated{

    static HashMap<String, AgentLogBackend> agentsOutput = new HashMap<String, AgentLogBackend>();

    public LogHub(){

    }

    public static AgentLogBackend getAgentLog(String name){
        return agentsOutput.get(name);
    }

    synchronized public void onAgentLog(String agentName, String newLog){
        Log.v("smth", "printeeeeeed " + newLog);
        agentsOutput.get(agentName).add(newLog);
    }

    synchronized public void onAgentAdded(ExtAgentInfo agentInfo){
        agentsOutput.put(agentInfo.getName(), new AgentLogBackend());
    }

    public void clear(String name){
        agentsOutput.get(name).clear();
    }
}
