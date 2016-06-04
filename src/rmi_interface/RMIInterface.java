package rmi_interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import core.simulation.external_interface.ExtAgentInfo;

public interface RMIInterface  extends Remote{
	public Vector<String> getAgentsNames() throws RemoteException;
	
	public void createAgents() throws RemoteException;

    public void startAgents() throws RemoteException;
}
