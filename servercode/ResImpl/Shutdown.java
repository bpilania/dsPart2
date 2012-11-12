package ResImpl;
import java.util.*;
class Shutdown implements Runnable{

public void run(){
	try{
		Thread.sleep(1000);
		System.exit(0);
	}catch(Exception e){
		e.printStackTrace();
	}
}
} 
