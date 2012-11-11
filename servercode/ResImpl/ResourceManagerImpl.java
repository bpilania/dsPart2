// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package ResImpl;

import ResInterface.*;

import java.util.*;
import java.rmi.*;
import exceptionPackage.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import LockManager.*;

//public class ResourceManagerImpl extends java.rmi.server.UnicastRemoteObject
public class ResourceManagerImpl
	implements ResourceManager {
	
	protected RMHashtable m_itemHT = new RMHashtable();
    static ResourceManager rmCar = null;
    static ResourceManager rmHotel = null;
    static ResourceManager rmFlight = null;
    static int xID = 0;
    static LockManager lm=null;
    static Hashtable rmTracker=new Hashtable(); 
    

	public static void main(String args[]) {
        // Figure out where server is running
        String server = "mimi";
        int port = 8778;
//         if (args.length == 1) {
//             server = server + ":" + args[0];
//         } else if (args.length != 0 &&  args.length != 1) {
//             System.err.println ("Wrong usage");
//             System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
//             System.exit(1);
//         }

	 try 
	     {
		 // create a new Server object
		 ResourceManagerImpl obj = new ResourceManagerImpl();
		 // dynamically generate the stub (client proxy)
		 ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
		 
		 // Bind the remote object's stub in the registry
		 Registry registry = LocateRegistry.getRegistry(8778);
		 registry.rebind("Group4ResourceManager", rm);
		 
		 System.err.println("Server ready");
	     } 
	 catch (Exception e) 
	     {
		 System.err.println("Server exception: " + e.toString());
		 e.printStackTrace();
	     }
	 
	 // Code as client
	
	rmCar =  connection("Group4CarRM", server, port);
	rmHotel = connection("Group4HotelRM", server, port);
	rmFlight = connection("Group4FlightRM", server, port);
	
	 //Code as client ends
	 
	 lm = new LockManager();
	 	 
         // Create and install a security manager
/*
         if (System.getSecurityManager() == null) {
	     System.setSecurityManager(new RMISecurityManager());
         }
*/
}	 
	 
	 public ResourceManagerImpl() throws RemoteException {
	 }
	 
	 public static ResourceManager connection(String rmName, String server, int port)
	 {
		 ResourceManager rm = null;
		 try 
			{
			    // get a reference to the rmiregistry
			    Registry registry = LocateRegistry.getRegistry(server,port);
			    // get the proxy and the remote reference by rmiregistry lookup
		    		
			    rm = (ResourceManager) registry.lookup(rmName);
						    if(rm!=null)
							{
							    System.out.println("Successful");
							    System.out.println("Connected to "+rmName+"RM");
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
		 
		 return rm;
	 }
	

	 	// Reads a data item
	 	private RMItem readData( int id, String key )
	 	{
	 		synchronized(m_itemHT){
	 			return (RMItem) m_itemHT.get(key);
	 		}
	 	}
	 
	 	// Writes a data item
	 	private void writeData( int id, String key, RMItem value )
	 	{
	 		synchronized(m_itemHT){
	 			m_itemHT.put(key, value);
	 		}
	 	}
	 	
	 	// Remove the item out of storage
	 	protected RMItem removeData(int id, String key){
	 		synchronized(m_itemHT){
	 			return (RMItem)m_itemHT.remove(key);
	 		}
	 	}
	 	
	 	
	 	// deletes the entire item
	 	protected boolean deleteItem(int id, String key)
	 	{
	 		return deleteItem(id,key);
	 	}
	 	
	 
	 	// query the number of available seats/rooms/cars
	 	protected int queryNum(int id, String key) {

	 		return 0;
	 	}	
	 	
	 	// query the price of an item
	 	protected int queryPrice(int id, String key){
	
	 	return 0;
	 	}
	 	
	 	// reserve an item
	 	protected boolean reserveItem(int id, int customerID, String key, String location){
		
	 	return true;
	 	}
	
	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("flight"+flightNum).trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Flight X");
			return rmFlight.addFlight(id, flightNum, flightSeats, flightPrice);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
			
		}
		*/
		catch(Exception e){
			abort(id);
			throw e;
		}
	}
	
	public boolean deleteFlight(int id, int flightNum)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{	
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("flight"+flightNum).trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Flight X");
			return rmFlight.deleteFlight(id, flightNum);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
		
	}

	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, String location, int count, int price)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, "hotel"+location.trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Hotel X");
			return rmHotel.addRooms(id, location, count, price);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
		
	}

	// Delete rooms from a location
	public boolean deleteRooms(int id, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
			throw new InvalidTransactionException(id,"Transaction Id is not valid");
		}
		if(lm.Lock (id, "hotel"+location.trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Hotel X");
			return rmHotel.deleteRooms(id, location);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}

	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
			throw new InvalidTransactionException(id,"Transaction Id is not valid");
		}
		if(lm.Lock (id, "car"+location.trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Car X");
			return rmCar.addCars(id, location, count, price);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Delete cars from a location
	public boolean deleteCars(int id, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
			throw new InvalidTransactionException(id,"Transaction Id is not valid");
		}
		if(lm.Lock (id, "car"+location.trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Car X");
			return rmCar.deleteCars(id, location);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}



	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
			if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
			if(lm.Lock (id, ("flight"+flightNum).trim().toString(), LockManager.READ)){
				System.out.println("Lock granted");
				enlist(id,"Flight S");
				return rmFlight.queryFlight(id, flightNum);
			}
			else{
				return 0;
			}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum )
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
			if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("flight"+flightNum).trim().toString(), LockManager.READ)){
			System.out.println("Lock granted");
			enlist(id,"Flight S");
			return rmFlight.queryFlightPrice(id, flightNum);
		}
		else{
			return 0;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, "hotel"+location.trim().toString(), LockManager.READ)){
			System.out.println("Lock granted");
			enlist(id,"Hotel S");
			return rmHotel.queryRooms(id, location);
		}
		else{
			return 0;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}

	
	// Returns room price at this location
	public int queryRoomsPrice(int id, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, "hotel"+location.trim().toString(), LockManager.READ)){
			System.out.println("Lock granted");
			enlist(id,"Hotel S");
			return rmHotel.queryRoomsPrice(id, location);
		}
		else{
			return 0;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Returns the number of cars available at a location
	public int queryCars(int id, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, "car"+location.trim().toString(), LockManager.READ)){
			System.out.println("Lock granted");
			enlist(id,"Car X");
			return rmCar.queryCars(id, location);
		}
		else{
			return 0;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, "car"+location.trim().toString(), LockManager.READ)){
			System.out.println("Lock granted");
			enlist(id,"Car S");
			return rmCar.queryCarsPrice(id, location);
		}
		else{
			return 0;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
		throws RemoteException
	{

		return new RMHashtable();
	}

	 //return a bill
	public String queryCustomerInfo(int id, int customerID)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("customer"+customerID).trim().toString(), LockManager.READ)){
			enlist(id,"Flight S");
			enlist(id,"Car S");
			enlist(id,"Hotel S");
			String s="BIll for Customer ID"+ customerID+":\n";
		
			s=s+rmCar.queryCustomerInfo(id, customerID);
		
			s=s+ rmFlight.queryCustomerInfo(id, customerID);
		
			s=s+rmHotel.queryCustomerInfo(id, customerID);

			return s;
		}
		else{
			return "No Bill";
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}

  // customer functions
  // new customer just returns a unique customer identifier
	
  public int newCustomer(int id)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		int cid;
	  	boolean result;
		cid = Integer.parseInt( String.valueOf(id) +
				String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("customer"+cid).trim().toString(), LockManager.WRITE)){
			enlist(id,"Flight X");
			enlist(id,"Car X");
			enlist(id,"Hotel X");
		  	if(rmCar.newCustomer(id,cid)){
		  		if(rmFlight.newCustomer(id,cid)){
		  			if(rmHotel.newCustomer(id,cid)){
		  				System.out.println("Lock granted");
						return cid;
					}
					else{
					return 0;
					}
				}
				else{
				return 0;
				}
			}
			else{
			return 0;
			}
			
		  	
			//Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid ); //To be activated later

		}
		else{
			return 0;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}

	// I opted to pass in customerID instead. This makes testing easier
  public boolean newCustomer(int id, int customerID )
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("customer"+customerID).trim().toString(), LockManager.WRITE)){
			enlist(id,"Flight X");
			enlist(id,"Hotel X");
			enlist(id,"Cars X");
			System.out.println("Lock granted");
		  	boolean result;
		  	
		  	if(rmCar.newCustomer(id,customerID)){
		  		if(rmFlight.newCustomer(id,customerID)){
		  			if(rmHotel.newCustomer(id,customerID)){
		  				System.out.println("Lock granted");
						return true;
					}
					else{
					return false;
					}
				}
				else{
				return false;
				}
			}
			else{
			return false;
			}
			
		  	
			
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Deletes customer from the database. 
	public boolean deleteCustomer(int id, int customerID)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("customer"+customerID).trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Flight X");
			enlist(id,"Car X");
			enlist(id,"Hotel X");
			boolean result;
		  	if(rmCar.deleteCustomer(id,customerID)){
		  		if(rmFlight.deleteCustomer(id,customerID)){
		  			if(rmHotel.deleteCustomer(id,customerID)){
		  				System.out.println("Lock granted");
						return true;
					}
					else{
					return false;
					}
				}
				else{
				return false;
				}
			}
			else{
			return false;
			}
			

		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Adds car reservation to this customer. 
	public boolean reserveCar(int id, int customerID, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, "car"+location.trim().toString(), LockManager.WRITE) && lm.Lock (id, ("customer"+customerID).trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Car X");
			enlist(id,"Car X");
			return rmCar.reserveCar(id, customerID, location);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}


	// Adds room reservation to this customer. 
	public boolean reserveRoom(int id, int customerID, String location)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, "hotel"+location.trim().toString(), LockManager.WRITE) && lm.Lock (id, ("customer"+customerID).trim().toString(), LockManager.WRITE)){
			System.out.println("Lock granted");
			enlist(id,"Room X");
			return rmHotel.reserveRoom(id, customerID, location);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}
	
	// Adds flight reservation to this customer.  
	public boolean reserveFlight(int id, int customerID, int flightNum)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
		try{
		if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
		if(lm.Lock (id, ("flight"+flightNum).trim().toString(), LockManager.WRITE) && lm.Lock (id, ("customer"+customerID).trim().toString(), LockManager.WRITE)){
			enlist(id,"Flight X");
			System.out.println("Lock granted");
			return rmFlight.reserveFlight(id, customerID, flightNum);
		}
		else{
			return false;
		}
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
	}
	
	/* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
		throws RemoteException, TransactionAbortedException, InvalidTransactionException, Exception
	{
    	boolean result = true;
		try{
			if(Car){
				if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
				}
				lm.Lock (id, "car"+location.trim().toString(), LockManager.WRITE);
				enlist(id,"Car X");
			}
			if(Room){
				if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
				}
				lm.Lock (id, "hotel"+location.trim().toString(), LockManager.WRITE);
				enlist(id,"Hotel X");
			}
			if(!isValid(id)){
				throw new InvalidTransactionException(id,"Transaction Id is not valid");
			}
			for(int i=0;i<flightNumbers.size();i++){
				lm.Lock (id, ("flight"+flightNumbers.elementAt(i)).trim().toString(), LockManager.WRITE);
				enlist(id,"Flight X");
			}
			
			System.out.println("Lock granted");
			if(Car)
		    		{
		    		result = rmCar.itinerary(id, customer, flightNumbers, location, Car, Room);
		    		System.out.println("car result"+result);
		    		}
		    	if(result == false)
		    		abort(id);
		    		
		    	if(result == true){
		    		result = rmFlight.itinerary(id, customer, flightNumbers, location, Car, Room);
		    		System.out.println("flightresult"+result);
		    		}
		    	
		    	if(result == false)
		    		abort(id);
		    	
		    	if((false != result) && Room)
		    	{
		    		result = rmHotel.itinerary(id, customer, flightNumbers, location, Car, Room);
		    		System.out.println("room reserved"+result);
		    	}
			
		    	if(result == false)
		    		abort(id);
		
			return result;  
		}catch(DeadlockException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}/*catch(TransactionAbortedException e){
			System.out.println(e.getMessage());
			abort(id);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+id+" has been aborted!");
		}*/catch(Exception e){
			abort(id);
			throw e;
		}
    }
    
    public int start() throws RemoteException{
  	xID = xID + 1;
  	System.out.println("New xID is: "+xID);
	return xID;
    }
    
    public boolean commit(int transactionId) throws RemoteException,TransactionAbortedException,InvalidTransactionException, Exception{
   	 try{
		if((rmCar.commit(transactionId) == true ) && (rmFlight.commit(transactionId) == true) && (rmHotel.commit(transactionId) == true)){
			lm.UnlockAll(transactionId);
			return true;
		}
		else{
			lm.UnlockAll(transactionId);
			throw new TransactionAbortedException("Server could not process your request. Transaction "+transactionId+" has been aborted!");
		}
	
			
	}
	/*
	catch(InvalidTransactionException e){
		lm.UnlockAll(transactionId);
			throw new InvalidTransactionException("Server could not process your request. Transaction "+transactionId+" is invalid!");
	
	}
	*/
	catch(Exception e){
		abort(transactionId);
		throw e;
	}
    }
    
    public void abort(int transactionId) throws RemoteException,InvalidTransactionException, Exception{
    	try{
	    	
	    	rmCar.abort(transactionId);    
	    	rmFlight.abort(transactionId);    
	    	rmHotel.abort(transactionId);
	    	
	    	lm.UnlockAll(transactionId);    
	}
	/*
	catch(InvalidTransactionException e){
	    	lm.UnlockAll(transactionId);    
		throw new InvalidTransactionException("Server could not process your request. Transaction "+transactionId+" is invalid!");
	}
	*/
	catch(Exception e){
	    	lm.UnlockAll(transactionId);    
		throw e;
	}
	
    }    
    
    
 public boolean shutdown() throws RemoteException{
 	
 	if(rmTracker.isEmpty()){
 		rmCar.shutdown();
 		rmFlight.shutdown();
 		rmHotel.shutdown();
 		return true;		
 	}
 	else {
 		return false;
 	}
	
 }
 
 public  void addToTracker(int xid){
 	//creates hashtable entry for new transaction id
 	Vector rmValues=new Vector();
 	rmTracker.put(xid,rmValues);
 }
 
 public void enlist(int xid,String rmVal){
 	Vector vec = (Vector) rmTracker.get(xid);
 	vec.add(rmVal);
 	rmTracker.put(xid,rmVal);
 }

public boolean isValid(int xid){
	return rmTracker.containsKey(xid);
}
 public void removeFromTracker(int xid){
 	rmTracker.remove(xid);
 }

}
