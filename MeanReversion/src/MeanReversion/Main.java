package MeanReversion;

import java.util.Scanner;

public class Main {

    public static void main (String args[]) {
    	IBWrapper myIBWrapper = new IBWrapper();
    	
    	System.out.println("Press <Enter> to quit.");
    	Scanner keyboard = new Scanner(System.in);
    	keyboard.nextLine();	
    	keyboard.close();

    	myIBWrapper.disconnect();
    }
}
