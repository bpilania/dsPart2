package ResImpl;
import java.util.*;

class Log{
	
	int xId;
	RMHashtable logTable =new RMHashtable();
	Log(){
		super();
	}
	Log(int xId,RMHashtable log){
		this.xId=xId;
		this.logTable=log;
	}
	
	int getXId(){
		return this.xId;
	}
	
	void setXId(int xId){
		this.xId=xId;
	}
	
	RMHashtable getLog(){
		return this.logTable;
	}
	
	void setLog(RMHashtable log){
		this.logTable=log;
	}
}
