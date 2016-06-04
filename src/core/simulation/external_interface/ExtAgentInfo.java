package core.simulation.external_interface;

import core.agent.CompositeAgent;

/**
 * Created by yonutix on 15/05/2016.
 */
public class ExtAgentInfo {
    String mName = "Undefined";
    CompositeAgent mAgent;

    public ExtAgentInfo(){

    }

    public ExtAgentInfo(CompositeAgent agent){
        mAgent = agent;
    }

    public String getName(){
        return mAgent.getAgentName();
    }

}
