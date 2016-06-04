import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Vector;

import rmi_interface.RMIInterface;

public class Main {

	public static void main(String[] args) {
		
		String host = "127.0.0.1";
		try {
		    Registry registry = LocateRegistry.getRegistry(host);
		    RMIInterface stub = (RMIInterface) registry.lookup("RMIInterface");
		    
		    if(args.length < 1){
		    	System.out.println("Command not available");
		    	return;
		    }
		    
		    if(args[0].compareTo("-h") == 0){
		    	System.out.println("-names   Lists all runnging agents names");
		    	System.out.println("-create  Create all the agents from the XML configuration");
		    	System.out.println("-start   Starts all the agents");
		    }
		    
		    if(args[0].compareTo("-names") == 0){
		    	Vector<String> response = stub.getAgentsNames();
		    	for(String s: response){
		    		System.out.println(s);
		    	}
		    }
		    
		    if(args[0].compareTo("-create") == 0){
		    	stub.createAgents();
		    }
		    
		    if(args[0].compareTo("-start") == 0){
		    	stub.startAgents();
		    }
		    
		} catch (Exception e) {
		    System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		}
		
	}
}
