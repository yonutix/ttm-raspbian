package tatami.simulation.external_interface;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import Logging.Log;
import tatami.core.agent.CompositeAgent;
import tatami.simulation.SimulationManager;

/**
 * Created by yonutix on 15/05/2016.
 */
public class ExternalInterface {
    SimulationManager mContext;

    HashMap<String, ExternalInterfaceListener.AgentRelated>  allListeners;

    HashMap<String, ExtAgentInfo> allAgentsInfo;

    public ExternalInterface(SimulationManager context){
        allListeners = new HashMap<String, ExternalInterfaceListener.AgentRelated>();
        mContext = context;
        allAgentsInfo = new HashMap<String, ExtAgentInfo>();
    }


    public boolean registerListener(String name, ExternalInterfaceListener.AgentRelated newListener){
        if(allListeners.containsKey(name)){
            Log.e("EXT_INTERFACE", "Listener with name " + name + "already exists");
            return false;
        }
        allListeners.put(name, newListener);
        return true;
    }

    public void unregisterListener(String name){
        allListeners.remove(name);
    }
    
    public void stop(){
    	mContext.stop();
    }

    public void createAgents(){
        mContext.createAgent();
    }

    public void startAgents(){
        mContext.startAgents();
    }

    public HashMap<String, ExtAgentInfo> getAgentsInfo(){
        return allAgentsInfo;
    }

    public Vector<String> getAgentsNames(){
        Vector<String> names = new Vector<String>();
        for(Map.Entry<String, ExtAgentInfo> agentInfo: allAgentsInfo.entrySet()){
            names.add(agentInfo.getValue().getName());
        }

        return names;
    }

    public void onAgentAdded(CompositeAgent agent){
        ExtAgentInfo agentInfo = new ExtAgentInfo(agent);
        allAgentsInfo.put(agentInfo.getName(), agentInfo);

        for(Map.Entry<String, ExternalInterfaceListener.AgentRelated> entry: allListeners.entrySet()){
            entry.getValue().onAgentAdded(agentInfo);
        }
    }

    public void onAgentLog(String agentName, String newLog){
        for(Map.Entry<String, ExternalInterfaceListener.AgentRelated> entry: allListeners.entrySet()){
            entry.getValue().onAgentLog(agentName, newLog);
        }
    }

    public void onAgentRemoved(){

    }

    public void afterSystemStart(){

    }


}
