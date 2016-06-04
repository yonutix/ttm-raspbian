package core.simulation.external_interface;

/**
 * Created by yonutix on 15/05/2016.
 */
public interface ExternalInterfaceListener {


    interface AgentRelated{
        public void onAgentAdded(ExtAgentInfo agentInfo);
        public void onAgentLog(String agentName, String newLog);
    }
}
