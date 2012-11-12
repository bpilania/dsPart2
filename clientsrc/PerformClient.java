import java.rmi.*;
import ResInterface.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import exceptionPackage.*;
import LockManager.*;
import java.util.*;
import java.io.*;

public class PerformClient implements Runnable{

	static ResourceManager rm = null;
	public static void main(String args[]){
		PerformClient obj = new PerformClient();
	    	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	    	String command = "";
	    	Vector arguments  = new Vector();
	    	int Id, Cid;
	    	int flightNum;
	    	int flightPrice;
	    	int flightSeats;
	    	boolean Room;
	    	boolean Car;
	    	int price;
	    	int numRooms;
	    	int numCars;
	    	String location;
	    	int xID = 0;

	    	String server = "localhost";
	    	if (args.length == 1) 
			server = args[0]; 
	    	else if (args.length != 0 &&  args.length != 1) 
		{
			System.out.println ("Usage: java client [rmihost]"); 
			System.exit(1); 
		}
		
	    	try 
		{
		    // get a reference to the rmiregistry
		    Registry registry = LocateRegistry.getRegistry(server,8778);
		    // get the proxy and the remote reference by rmiregistry lookup
		    rm = (ResourceManager) registry.lookup("Group4ResourceManager");
		    if(rm!=null)
			{   

			    System.out.println("Successful");
			    System.out.println("Connected to RM");
			}
		    else
			{
			    System.out.println("Unsuccessful");
			}
		    // make call on remote method
		} 
	    	catch (Exception e) 
		{	
		    System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		}
	    
	}
	
	public void run(){
		
	}
}
