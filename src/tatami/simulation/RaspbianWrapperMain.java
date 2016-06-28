package tatami.simulation;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import Logging.Log;
import rmi_interface.RMIInterface;

public class RaspbianWrapperMain implements RMIInterface{

	static Boot fwkInstance = null;
	
	public static Boot getBoot(){
		return fwkInstance;
	}
	
	public static void main(String[] args) {
		
		try {
			Log.v("RMI", "Starting RMI Server");
    		LocateRegistry.createRegistry(1099);

    		RaspbianWrapperMain obj = new RaspbianWrapperMain();
    		RMIInterface stub = (RMIInterface) UnicastRemoteObject.exportObject(obj, 0);

    		Registry registry = LocateRegistry.getRegistry();
    		registry.bind("RMIInterface", stub);

    		Log.v("RMI", "Server ready");
    		
    		//Starting the framework
    		fwkInstance = new Boot();
    		fwkInstance.boot(new String[0]);
    		
    		
    		
    	} catch (Exception e) {
    		Log.e("RMI", "The RMI server failed to start");
    		e.printStackTrace();
    	}

	}


	@Override
	public Vector<String> getAgentsNames() throws RemoteException {
		return fwkInstance.getSimulationManager().ext().getAgentsNames();
	}

	@Override
	public void createAgents() throws RemoteException {
		fwkInstance.getSimulationManager().ext().createAgents();
		
	}

	@Override
	public void startAgents() throws RemoteException {
		fwkInstance.getSimulationManager().ext().startAgents();
		
	}

	@Override
	public void stop() throws RemoteException {
		fwkInstance.getSimulationManager().ext().stop();
		
	}
}
