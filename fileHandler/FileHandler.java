package fileHandler;
import java.io.*;
import ResImpl.*;


public class FileHandler{

	private BufferedWriter openW(String fileName ){
		FileWriter fstream=null;
		BufferedWriter br=null;
		try{
			fstream = new FileWriter(fileName,true);
            		br = new BufferedWriter(fstream);	
			
		 }catch(Exception e){
		 	e.printStackTrace();
		 }
		 return br;

	}



	private DataInputStream openR(String fileName){
		DataInputStream d=null;	
		try{
			FileInputStream in=new FileInputStream(fileName);
			d=new DataInputStream(in);
			return d;
		}catch(Exception  e){
			e.printStackTrace();
		}
		return d;
		
	}


	void write(String fileName,String data){
		try{
		
			BufferedWriter out =openW(fileName);
			out.write(data);
			out.newLine();
			out.close();
		}catch(Exception e ){
			e.printStackTrace();
		}
	}

	String Read(String fileName,String key){
		return null;
	}

	RMHashtable recover(String fileName){
		return null;
	}

}

