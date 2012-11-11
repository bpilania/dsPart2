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

//public class CarRM extends java.rmi.server.UnicastRemoteObject
public class HotelRM
	implements ResourceManager {
	Vector logArray =new Vector();
	protected RMHashtable m_itemHT = new RMHashtable();
    static ResourceManager rm = null;

	public static void main(String args[]) {
        // Figure out where server is running
        String server = "localhost";

         /*if (args.length == 1) {
             server = server + ":" + args[0];
         } else if (args.length != 0 &&  args.length != 1) {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java ResImpl.CarRM [port]");
             System.exit(1);
         }*/

	 try 
	     {
		 // create a new Server object
		 HotelRM obj = new HotelRM();
		 // dynamically generate the stub (client proxy)
		 ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
		 
		 // Bind the remote object's stub in the registry
		 Registry registry = LocateRegistry.getRegistry(8778);
		 registry.rebind("Group4HotelRM", rm);
		 
		 System.err.println("Hotel Server ready");
	     } 
	 catch (Exception e) 
	     {
		 System.err.println("Server exception: " + e.toString());
		 e.printStackTrace();
	     }
	 	 
         // Create and install a security manager
/*
         if (System.getSecurityManager() == null) {
	     System.setSecurityManager(new RMISecurityManager());
         }
*/
}	 
	 
	 public HotelRM() throws RemoteException {
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
	
	private RMItem readDataFromLog(int id,String key,int xId){
		synchronized(logArray){
			int indx;
			if((indx=logContains(xId))!=-1){
				Log temp=(Log)logArray.elementAt(indx);
				return (RMItem)temp.get(key);
			}		
		}
		return null;
	}
	
	
	private void writeDataToLog(int xId, String key, RMItem value){
		System.out.println("entering writedatatolog");
		synchronized(logArray){
			Log temp;
			int indx;
			if((indx=logContains(xId))!=-1){
				temp=(Log)logArray.elementAt(indx);
				temp.put(key,value);
			}
			else{
				temp=new Log(xId,new RMHashtable());
				temp.put(key,value);
				logArray.add(temp);
				
			}	
		}
	}
	
	
	private int logContains(int xId){
		synchronized(logArray){
			for(int i=0;i<logArray.size();i++){
				Log temp=(Log)logArray.elementAt(i);
				if(temp.getXId()==xId){
					return i;
				}
			}
		}
		return -1;
	}
	
	protected boolean removeDataFromLog(int xId){
		int indx=logContains(xId);
		if(indx>=0)
			logArray.remove(indx);
		return true;
	}
	// deletes the entire item
	protected boolean deleteItem(int id, String key)
	{
		Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key );
		Hotel tempItem =new Hotel(curObj.getLocation(),curObj.getCount(),curObj.getPrice());
		tempItem.setReserved(curObj.getReserved());
		// Check if there is such an item in the storage
		if( curObj == null ) {
			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
			return false;
		} else {
			if(curObj.getReserved()==0){
				tempItem.setType(0);
				writeDataToLog(id,curObj.getKey(),tempItem);
				removeData(id, curObj.getKey());
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
				return true;
			}
			else{
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
				return false;
			}
		} // if
	}
	

	// query the number of available seats/rooms/cars
	protected int queryNum(int id, String key) {
		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0;  
		if( curObj != null ) {
			value = curObj.getCount();
		} // else
		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
		return value;
	}	
	
	// query the price of an item
	protected int queryPrice(int id, String key){
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0; 
		if( curObj != null ) {
			value = curObj.getPrice();
		} // else
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;		
	}
	
	// reserve an item
	protected boolean reserveItem(int id, int customerID, String key, String location){
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
		// Read customer object if it exists (and read lock it)
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
		if( cust == null ) {
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return false;
		} 
		
		// check if the item is available
		ReservableItem item = (ReservableItem)readData(id, key);
		
		if(item==null){
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
			return false;
		}else if(item.getCount()==0){
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
			return false;
		}else{		
			Hotel tempItem =new Hotel(item.getLocation(),item.getCount(),item.getPrice());
			tempItem.setReserved(item.getReserved());	
			Customer temp=cust.clone();
			temp.setType(1);
			if(readDataFromLog(id,cust.getKey(),id)==null){
				writeDataToLog(id,cust.getKey(),temp);
			}
			cust.reserve( key, location, item.getPrice());		
			writeData( id, cust.getKey(), cust );
			
			// decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved()+1);
			if(readDataFromLog(id,item.getKey(),id)==null){
				
				tempItem.setType(0);
				writeDataToLog(id,item.getKey(),tempItem);
				
			}
			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			return true;
		}		
	}
	
	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
		throws RemoteException
	{
		return(true);
	}


	
	public boolean deleteFlight(int id, int flightNum)
		throws RemoteException
	{
		return deleteItem(id, Flight.getKey(flightNum));
	}



	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, String location, int count, int price)
		throws RemoteException
	{
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
		Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
		
		if( curObj == null ) {
			// doesn't exist...add it
			Hotel newObj = new Hotel( location, count, price );
			
			writeData( id, newObj.getKey(), newObj);
			String key=newObj.getKey();
			if(readDataFromLog(id,key,id)==null){
				Hotel logObj =(Hotel)newObj.clone();
				logObj.setCount(-1);
				logObj.type=0;
				writeDataToLog(id,key,logObj);
			}
			Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
		} else {
			// add count to existing object and update price...
			Hotel logObj =(Hotel)curObj.clone();
			if(readDataFromLog(id,curObj.getKey(),id)==null)
				writeDataToLog(id,curObj.getKey(),logObj);
			
			curObj.setCount( curObj.getCount() + count );
			
			if( price > 0 ) {
				curObj.setPrice( price );
			} // if
			writeData( id, curObj.getKey(), curObj );
			Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
		} // else
		return(true);
	}

	// Delete rooms from a location
	public boolean deleteRooms(int id, String location)
		throws RemoteException
	{
		return deleteItem(id, Hotel.getKey(location));
		
	}

	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price)
		throws RemoteException
	{
		
		return(true);
	}


	// Delete cars from a location
	public boolean deleteCars(int id, String location)
		throws RemoteException
	{
		return deleteItem(id, Car.getKey(location));
	}



	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum)
		throws RemoteException
	{
		return queryNum(id, Flight.getKey(flightNum));
	}



	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum )
		throws RemoteException
	{
		return queryPrice(id, Flight.getKey(flightNum));
	}


	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location)
		throws RemoteException
	{
		return queryNum(id, Hotel.getKey(location));
	}


	
	
	// Returns room price at this location
	public int queryRoomsPrice(int id, String location)
		throws RemoteException
	{
		return queryPrice(id, Hotel.getKey(location));
	}


	// Returns the number of cars available at a location
	public int queryCars(int id, String location)
		throws RemoteException
	{
		return queryNum(id, Car.getKey(location));
	}


	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location)
		throws RemoteException
	{
		return queryPrice(id, Car.getKey(location));
	}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
		throws RemoteException
	{
		Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return null;
		} else {
			return cust.getReservations();
		} // if
	}

	// return a bill
	public String queryCustomerInfo(int id, int customerID)
		throws RemoteException
	{
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
		} else {
				String s = cust.printBill();
				Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
				System.out.println( s );
				return s;
		} // if
	}

  // customer functions
  // new customer just returns a unique customer identifier
	
 public int newCustomer(int id)
		throws RemoteException
	{
		Trace.info("INFO: RM::newCustomer(" + id + ") called" );
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt( String.valueOf(id) +
								String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
								String.valueOf( Math.round( Math.random() * 100 + 1 )));
		Customer cust = new Customer( cid );
		writeData( id, cust.getKey(), cust );
		Customer temp=cust.clone();
		temp.setID(-1);
		temp.setType(1);
		writeDataToLog(id,cust.getKey(),temp);
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
		return cid;
	}
	// I opted to pass in customerID instead. This makes testing easier
   public boolean newCustomer(int id, int customerID )
		throws RemoteException
	{
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			cust = new Customer(customerID);
			writeData( id, cust.getKey(), cust );
			Customer temp=cust.clone();
			temp.setType(1);
			temp.setID(-1);
			writeDataToLog(id,cust.getKey(),temp);
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
			return true;
		} else {
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
			return false;
		} // else
	}



	// Deletes customer from the database. 
	public boolean deleteCustomer(int id, int customerID)
			throws RemoteException
	{
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		
		if( cust == null ) {
			Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return false;
		} else {	
			Customer temp=cust.clone();		
			// Increase the reserved numbers of all reservable items which the customer reserved. 
			RMHashtable reservationHT = cust.getReservations();
			for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
				String reservedkey = (String) (e.nextElement());
				ReservedItem reserveditem = cust.getReservedItem(reservedkey);
				Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
				ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
				Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
				Hotel tempItem =new Hotel(item.getLocation(),item.getCount(),item.getPrice());
				tempItem.setReserved(item.getReserved());
				tempItem.setType(0);
				item.setReserved(item.getReserved()-reserveditem.getCount());
				item.setCount(item.getCount()+reserveditem.getCount());
				if(readDataFromLog(id,item.getKey(),id)==null)
					writeDataToLog(id,item.getKey(),tempItem);
			}
			
			// remove the customer from the storage
			temp.setType(1);
			if(readDataFromLog(id,cust.getKey(),id)==null)
				writeDataToLog(id,cust.getKey(),temp);
			removeData(id, cust.getKey());
			
			Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
			return true;
		} // if
	}





	
	// Adds car reservation to this customer. 
	public boolean reserveCar(int id, int customerID, String location)
		throws RemoteException
	{
		return reserveItem(id, customerID, Car.getKey(location), location);
	}


	// Adds room reservation to this customer. 
	public boolean reserveRoom(int id, int customerID, String location)
		throws RemoteException
	{
		return reserveItem(id, customerID, Hotel.getKey(location), location);
	}
	// Adds flight reservation to this customer.  
	public boolean reserveFlight(int id, int customerID, int flightNum)
		throws RemoteException
	{
		return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
	}
	
	/* reserve an itinerary */
    public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
	throws RemoteException {
    	
    	return reserveRoom(id, customer, location);
    }
	
    public int start() throws RemoteException{
    		return 1;
	}
    
    public boolean commit(int transactionId) throws RemoteException,TransactionAbortedException,InvalidTransactionException{
    	removeDataFromLog(transactionId);
    	return true;	
    }
    
    public void abort(int transactionId) throws RemoteException,InvalidTransactionException{    
    	int indx=logContains(transactionId);
    	
    	Log temp;
    	
    	if(indx>-1){
    		
    		temp=(Log)logArray.elementAt(indx);
    		
    	}
    	else{
    		System.out.println("nothing in array");
    		return;
    	}
    	
 	for(Enumeration e = temp.getKeys(); e.hasMoreElements();){
 		System.out.println("For loop");
 		String key = (String) (e.nextElement());
 		RMItem obj=temp.get(key);
 		if(obj.getType()==0){
 			Hotel hotel=(Hotel) obj;
 			if(hotel.getCount()==-1){
 				System.out.println("entering count=-1 block");
 				removeData(transactionId,key);
 			}
 			else{
 				System.out.println("entering other block");
 				writeData(transactionId,key,hotel);
 			
 			}
 		}
 		else if(obj.getType()==1){
 			Customer cust=(Customer)obj;
 			if(cust.getID()==-1){
 				System.out.println("entering remove data for customer");
 				removeData(transactionId,key);
 			}
 			else{
 				System.out.println("entering write data for customer");
 				writeData(transactionId,key,obj);
 			}
 		}
 	}   	
    	
    }    
 public boolean shutdown() throws RemoteException{
 	return true;
 }
}
