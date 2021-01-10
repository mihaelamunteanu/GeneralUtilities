package ro.general.utilities.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseMailLogs {
	public static String filePathAndName0 = "C:\\Mihaela\\Other projects\\Primarie\\11. Audit IT\\Mail\\mail.log\\mail.log";
	public static String filePathAndName1 = "C:\\Mihaela\\Other projects\\Primarie\\11. Audit IT\\Mail\\mail.log\\mail.log.1";
	public static String filePathAndName2 = "C:\\Mihaela\\Other projects\\Primarie\\11. Audit IT\\Mail\\mail.log\\mail.log.2";
	public static String filePathAndName3 = "C:\\Mihaela\\Other projects\\Primarie\\11. Audit IT\\Mail\\mail.log\\mail.log.3";
	public static String filePathAndName4 = "C:\\Mihaela\\Other projects\\Primarie\\11. Audit IT\\Mail\\mail.log\\mail.log.4";
	public static String outputFileAllMails = "C:\\Mihaela\\Other projects\\Primarie\\11. Audit IT\\Mail\\mail.log\\AllMails.log";
	public static String outputFileAllMailsWithoutGerogiana = "C:\\Mihaela\\Other projects\\Primarie\\11. Audit IT\\Mail\\mail.log\\AllMailsWithoutGeorgiana.log";
	
	public static StringBuilder allMails = new StringBuilder();
	public static StringBuilder allMailsWithoutGeorgiana = new StringBuilder();
	
	//public static String pattern = "from=<[A-Z0-9._%+-]@primariapn.ro>";
	
	//public static Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@primariapn.ro">, Pattern.CASE_INSENSITIVE);
	public static Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("from=<[A-Z0-9.%+-]+@primariapn.ro>", Pattern.CASE_INSENSITIVE); 
		    //Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	
	public static void main(String[] args) {
		
		int numberOfFromPPNMail = 0;
		numberOfFromPPNMail += processFile(filePathAndName4);
		numberOfFromPPNMail += processFile(filePathAndName3);
		numberOfFromPPNMail += processFile(filePathAndName2);
		numberOfFromPPNMail += processFile(filePathAndName1);
		numberOfFromPPNMail += processFile(filePathAndName0);
		
		AutogenerateUtils.writeJavaFileFromString(outputFileAllMails, allMails.toString());
		AutogenerateUtils.writeJavaFileFromString(outputFileAllMailsWithoutGerogiana, allMailsWithoutGeorgiana.toString());
	}
	
	public static int processFile(String filePathAndName) {
		Scanner fileScanner;
		int numberOfFromPPNMail = 0;
		StringBuilder allMailsLocal = new StringBuilder();
		StringBuilder allMailsWithoutGeorgianaRaspopaLocal = new StringBuilder();
		
	    try {
	        fileScanner = new Scanner(new File(filePathAndName));

	    
	    while (fileScanner.hasNextLine()) {
//	    	lineScanner = new Scanner(fileScanner.nextLine()); while (lineScanner.hasNext()) {
	    	String lineText = fileScanner.nextLine();
	    	Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(lineText);
	    	if (matcher.find()) {
	    		System.out.print("***"+lineText+"\n");
	    		allMailsLocal.append(lineText+"\n");
	    		if (!lineText.contains("from=<georgiana.raspopa@primariapn.ro>")) {
	    			allMailsWithoutGeorgianaRaspopaLocal.append(lineText+"\n");
	    		}
	    	}
	    		
	    	}
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();  
	    }
	    
	    allMails.append(allMailsLocal.toString());
	    allMailsWithoutGeorgiana.append(allMailsWithoutGeorgianaRaspopaLocal.toString());
	    
	    return numberOfFromPPNMail;
	}
}
