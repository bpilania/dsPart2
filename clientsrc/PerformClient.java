import java.rmi.*;

import ResInterface.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import exceptionPackage.*;
import LockManager.*;
import java.util.*;
import java.io.*;

public class PerformClient implements Runnable {
	private static String RMIBindingName = null;
	private static String RMIRegistry = null;
	static ResourceManager rm = null;
	static int count = 0;
	static FileOutputStream out; // declare a file output object
	static PrintStream p; // declare a print stream object
	

	public static void main(String args[]) {

		if (args[0].equals("RMI"))
			RMIBindingName = args[2];
		System.out.println(RMIBindingName);
		RMIRegistry = args[1];
		Thread t[] = new Thread[10];
		String[] _server = RMIRegistry.split(":");
		try {
		out = new FileOutputStream("Performance.csv");

		// Connect print stream to the output stream
		p = new PrintStream(out);
		}catch(Exception e)
		{
			System.out.println("Could not open file!");
		}
		try {
			Registry registry = LocateRegistry.getRegistry(_server[0],
					Integer.valueOf(_server[1]));
			rm = (ResourceManager) registry.lookup(RMIBindingName);

			PerformClient client = new PerformClient();
			for (int numClient = 1; numClient < 10; numClient++) {
				count = 0;
				long start = System.nanoTime();
				while (count < numClient) {
					t[count] = new Thread(client);
					t[count].start();
					Thread.sleep(10);
					count++;
				}
				long end = System.nanoTime();
				long microseconds = (end - start) / 1000000;
				System.out.println("Customer " + 1000
						+ " ::  Time elapsed is : " + microseconds);
				time(microseconds, count, 27*200*count);
				
				for (int i = 0; i < count; i++) {
					t[i].join();

				}
			}
			p.close();
		} catch (Exception E) {
			E.printStackTrace();
		}

	}

	public void run() {
		
		int xID = 0;
		int count = 0;
		try {

			xID = rm.start();
			System.out.println(xID + " Entered");
			/*
			 * if(rm.queryFlight(xID,101)) {
			 * System.out.println("Flight added!"); } else {
			 * System.out.println("Flight Not Added"); }
			 */
			int customerID = 1000;
			int flightNumber = 101;
			String location = "location";

			
			for (count = 0; count < 1; count++) {
				customerID = 1000;
				flightNumber = 101;
				location = (location + flightNumber + customerID + xID)
						.toString();

				rm.newCustomer(xID, customerID);
				// System.out.println(xID+" :1");
				rm.addFlight(xID, flightNumber, 2, 3);
				// System.out.println(xID+" :2");
				rm.addFlight(xID, flightNumber + 1, 2, 3);
				// System.out.println(xID+" :3");
				rm.addFlight(xID, flightNumber + 2, 2, 3);
				rm.commit(xID);
				xID = rm.start();
				// System.out.println(xID+" :4");
				rm.addRooms(xID, location, 2, 3);
				// System.out.println(xID+" :5");
				rm.addCars(xID, location, 2, 3);
				// System.out.println(xID+" :6");
				rm.queryFlight(xID, flightNumber);
				// System.out.println(xID+" :7");
				rm.queryCars(xID, location);
				// rm.commit(xID);
				// xID = rm.start();
				// System.out.println(xID+" :8");
				rm.queryRooms(xID, location);
				// System.out.println(xID+" :9");
				rm.queryFlightPrice(xID, flightNumber);
				// System.out.println(xID+" :10");
				rm.queryCarsPrice(xID, location);
				// System.out.println(xID+" :11");
				rm.queryRoomsPrice(xID, location);
				rm.reserveCar(xID, customerID, location);
				rm.reserveFlight(xID, customerID, flightNumber);
				rm.reserveRoom(xID, customerID, location);
				rm.queryCustomerInfo(xID, customerID);
				Vector flightNum = new Vector();
				flightNum.add(flightNumber);
				flightNum.add(flightNumber + 1);
				flightNum.add(flightNumber + 2);
				// rm.commit(xID);
				// xID = rm.start();
				rm.itinerary(xID, customerID, flightNum, location, true, true);
				rm.queryCustomerInfo(xID, customerID);
				rm.deleteCustomer(xID, customerID);
				rm.deleteCars(xID, location);
				rm.deleteRooms(xID, location);
				// rm.commit(xID);
				// xID = rm.start();
				rm.deleteFlight(xID, flightNumber);
				rm.deleteFlight(xID, flightNumber + 1);
				rm.deleteFlight(xID, flightNumber + 2);
				rm.commit(xID);
				xID = rm.start();
				// System.out.println("iteration complete");
				customerID++;
				flightNumber++;
			}
			rm.commit(xID);
		} catch (Exception E) {
			System.out.println(E.getMessage());
		}

		

		
		
	}
	
	synchronized static void time(long microseconds, int clients, int requests)
	{

		try {
			// Create a new file output stream
			// connected to "myfile.txt"

			String csvVlaue = microseconds + "," + clients
					+ "," + requests;
			p.println(csvVlaue);
			p.println();
			
		} catch (Exception e) {
			System.err.println("Error writing to file");
		}
	}

}
