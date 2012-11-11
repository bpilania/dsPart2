package exceptionPackage;
public class InvalidTransactionException extends Exception{

	int id;
	String msg;
	public InvalidTransactionException(int id,String msg){
		super(msg);
		this.id=id;
		this.msg=new String(msg);
	}
	
	public int getId(){
		return id;
	}
	public String getMsg(){
		return msg; 
	}
	
}

