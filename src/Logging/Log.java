package Logging;

public class Log {
	enum SystemMachine{WINDOWS, ANDROID, RASPBIAN, PC, NOT_KNOWN};
	
	//TODO
	public static SystemMachine getSystemMachine(){
		if(System.getProperty("java.specification.vendor").toLowerCase().contains("android")){
			return SystemMachine.ANDROID;
		}
		
		if(System.getProperty("java.specification.vendor").toLowerCase().contains("raspberry")){
			return SystemMachine.RASPBIAN;
		}
		
		
		return SystemMachine.NOT_KNOWN;
	}
	
	public static void v(String tag, String message){
		System.out.println(tag +"    " + message);
	}
	public static void i(String tag, String message){
		System.out.println(tag +"    " + message);
	}
	public static void e(String tag, String message){
		System.out.println(tag +"    " + message);
	}
	public static void w(String tag, String message){
		System.out.println(tag +"    " + message);
	}
	public static void d(String tag, String message){
		System.out.println(tag +"    " + message);
	}
}
