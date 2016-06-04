package HMIBackend.backend;

import core.simulation.RaspbianWrapperMain;

/**
 * Created by uidk9631 on 17.05.2016.
 */
public class Backend {

    LogHub logHub;

    public Backend(){
        logHub = new LogHub();
        while(RaspbianWrapperMain.getBoot() == null);
        while(RaspbianWrapperMain.getBoot().getSimulationManager() == null);
        while(RaspbianWrapperMain.getBoot().getSimulationManager().ext() == null);

        RaspbianWrapperMain.getBoot().getSimulationManager().ext().registerListener("Loghub", logHub);
    }

    public LogHub getLogHub(){
        return logHub;
    }
}
