// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package ResImpl;

import ResInterface.*;

import java.util.*;
import java.rmi.*;

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
//	 		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
//	 		ReservableItem curObj = (ReservableItem) readData( id, key);
//	 		int value = 0;  
//	 		if( curObj != null ) {
//	 			value = curObj.getCount();
//	 		} // else
//	 		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
//	 		return value;
	 		return 0;
	 	}	
	 	
	 	// query the price of an item
	 	protected int queryPrice(int id, String key){
//	 		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
//	 		ReservableItem curObj = (ReservableItem) readData( id, key);
//	 		int value = 0; 
//	 		if( curObj != null ) {
//	 			value = curObj.getPrice();
//	 		} // else
//	 		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
//	 		return value;		
	 	return 0;
	 	}
	 	
	 	// reserve an item
	 	protected boolean reserveItem(int id, int customerID, String key, String location){
//	 		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
//	 		// Read customer object if it exists (and read lock it)
//	 		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
//	 		if( cust == null ) {
//	 			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
//	 			return false;
//	 		} 
//	 		
//	 		// check if the item is available
//	 		ReservableItem item = (ReservableItem)readData(id, key);
//	 		if(item==null){
//	 			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
//	 			return false;
//	 		}else if(item.getCount()==0){
//	 			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
//	 			return false;
//	 		}else{			
//	 			cust.reserve( key, location, item.getPrice());		
//	 			writeData( id, cust.getKey(), cust );
//	 			
//	 			// decrease the number of available items in the storage
//	 			item.setCount(item.getCount() - 1);
//	 			item.setReserved(item.getReserved()+1);
//	 			
//	 			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
//	 			return true;
//	 		}		
	 	return true;
	 	}
	
	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
		throws RemoteException
	{

		return rmFlight.addFlight(id, flightNum, flightSeats, flightPrice) ;
	}


	
	public boolean deleteFlight(int id, int flightNum)
		throws RemoteException
	{
		return rmFlight.deleteFlight(id, flightNum);
	}



	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, String location, int count, int price)
		throws RemoteException
	{
		return rmHotel.addRooms(id, location, count, price);
	}

	// Delete rooms from a location
	public boolean deleteRooms(int id, String location)
		throws RemoteException
	{
		return rmHotel.deleteRooms(id, location);
		
	}

	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price)
		throws RemoteException
	{
		rmCar.addCars(id, location, count, price);
		return(true);
	}


	// Delete cars from a location
	public boolean deleteCars(int id, String location)
		throws RemoteException
	{
		return rmCar.deleteCars(id, location);
	}



	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum)
		throws RemoteException
	{
		return rmFlight.queryFlight(id, flightNum);
	}


	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum )
		throws RemoteException
	{
		return rmFlight.queryFlightPrice(id, flightNum);
	}


	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location)
		throws RemoteException
	{
		return rmHotel.queryRooms(id, location);
	}

	
	// Returns room price at this location
	public int queryRoomsPrice(int id, String location)
		throws RemoteException
	{
		return rmHotel.queryRoomsPrice(id, location);
	}


	// Returns the number of cars available at a location
	public int queryCars(int id, String location)
		throws RemoteException
	{
		return rmCar.queryCars(id, location);
	}


	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location)
		throws RemoteException
	{
		return rmCar.queryCarsPrice(id, location);
	}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
		throws RemoteException
	{
//		Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
//		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
//		if( cust == null ) {
//			Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
//			return null;
//		} else {
//			return cust.getReservations();
//		} // if
		return new RMHashtable();
	}

	 //return a bill
	public String queryCustomerInfo(int id, int customerID)
		throws RemoteException
	{
//		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
//		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
//		if( cust == null ) {
//			Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
//			return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
//		} else {
//				String s = cust.printBill();
//				Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
//				System.out.println( s );
//				return s;
//		} // if
		String s="BIll for Customer ID"+ customerID+":\n";
		
		s=s+rmCar.queryCustomerInfo(id, customerID);
		
		s=s+ rmFlight.queryCustomerInfo(id, customerID);
		
		s=s+rmHotel.queryCustomerInfo(id, customerID);
		
		return s;
	}

  // customer functions
  // new customer just returns a unique customer identifier
	
  public int newCustomer(int id)
		throws RemoteException
	{
	  	int cid;
	  	boolean result;
		 cid = Integer.parseInt( String.valueOf(id) +
				String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		
	  	result = rmCar.newCustomer(id,cid);
	  	result = rmFlight.newCustomer(id,cid);
	  	result = rmHotel.newCustomer(id,cid);
	  	
		//Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid ); //To be activated later
		return cid;
	}

	// I opted to pass in customerID instead. This makes testing easier
  public boolean newCustomer(int id, int customerID )
		throws RemoteException
	{
	  	boolean result;
	  	result = rmCar.newCustomer(id, customerID);
	  	result = rmFlight.newCustomer(id, customerID);
	  	result = rmHotel.newCustomer(id, customerID);
	  	return result;
	}


	// Deletes customer from the database. 
	public boolean deleteCustomer(int id, int customerID)
			throws RemoteException
	{
		boolean result;
	  	result = rmCar.deleteCustomer(id, customerID);
	  	result = rmFlight.deleteCustomer(id, customerID);
	  	result = rmHotel.deleteCustomer(id, customerID);
	  	return result;
	}


	// Adds car reservation to this customer. 
	public boolean reserveCar(int id, int customerID, String location)
		throws RemoteException
	{
		return rmCar.reserveCar(id, customerID, location);
	}


	// Adds room reservation to this customer. 
	public boolean reserveRoom(int id, int customerID, String location)
		throws RemoteException
	{
		return rmHotel.reserveRoom(id, customerID, location);
	}
	// Adds flight reservation to this customer.  
	public boolean reserveFlight(int id, int customerID, int flightNum)
		throws RemoteException
	{
		return rmFlight.reserveFlight(id, customerID, flightNum);
	}
	
	/* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
	throws RemoteException {
    	boolean result = true;
    	if(Car)
    		{
    		result = rmCar.itinerary(id, customer, flightNumbers, location, Car, Room);
    		System.out.println("car result"+result);
    		}
    	if(false != result)
    		result = rmFlight.itinerary(id, customer, flightNumbers, location, Car, Room);
    		System.out.println("flightresult"+result);
    	if((false != result) && Room)
    	{
    		result = rmHotel.itinerary(id, customer, flightNumbers, location, Car, Room);
    		System.out.println("room reserved"+result);
    	}
    	
    	return result;    		
    	
    	
    }

}
