package rmi_interface;

/**
 * Created by uidk9631 on 25.04.2016.
 */
public class AgentEntryInfo {
    String mAgentName;

    public AgentEntryInfo(){

    }

    public AgentEntryInfo(String agentName){
        mAgentName = agentName;
    }

    public String getAgentName(){
        return mAgentName;
    }

    public void setAgentName(String agentName){
        mAgentName = agentName;
    }



}
