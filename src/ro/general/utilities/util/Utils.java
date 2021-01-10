package ro.general.utilities.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Utils {
	
	private static final Logger LOGGER_UTILS = Logger.getLogger(Constants.LOGGER_NAME);
	
	public static Properties prop = new Properties();
	
	private static String[] FORMULARE_LOCALITATI = 
		{"COM", "COMUNA", "ORAS", "ORASUL", "ORS", "LOC", "LOCALITATE", "LOCALITATEA", "MUN", "MUNICIPIU", "MUNICIPIUL", "SAT"};
	
//	public static String buildApplicationErrorMessage(ApplicationException applicationException, Logger LOGGER) {
//		String extraMesaj = "Daca mesajul de eroare nu este clar rugati administratorul sa verifice log-urile si dati-i aceste detalii: \n";
//		String errorMessage = "In data de " + applicationException.getOccuranceDate() + " pentru utilizatorul " + applicationException.getUserId() + " - " + applicationException.getUser() + 
//				" a aparut o problema la " + applicationException.getLocation() + " cu obiectul: " + applicationException.getObject() + 
//				" mesaj Aplicatie: " + applicationException.getApplicationMessage() + "mesaj eroare original: " + applicationException.getMessage();
//		LOGGER.log(Level.ERROR, errorMessage);
//		applicationException.printStackTrace();
//		return extraMesaj + errorMessage;
//	}
	
	public static String removeDiacriticalMarks(String string) {
	    return Normalizer.normalize(string, Form.NFD)
	        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
	public static String getCurrentDate() {
		String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		return currentDate;
	}

	public static String getCurrentHour() {
		Date currentDateTime = new Date();
		DateFormat dateFormat = new SimpleDateFormat("HH");	
		String ora = dateFormat.format(currentDateTime);
		return ora;
	}
	
	public static boolean verifyTelephone(String telefon) {
		boolean b = Pattern.matches("([0]{2}[0-9]{11})|([0]{1}[0-9]{9})|([+]{1}[0-9]{11})", telefon);
		return b;
	}
	
	public static boolean verifyCNP(String cnp) {
		
		if (cnp == null || "".equals(cnp)) {
			return false;
		}

		//format of cnp SAALLZZJJNNNC (SEX AN LUNA ZI JUDET NUMAR ALOCAT CONTROL)
		//where C is control number each number is multiplied with the value on the same position for 279146358279
		//C = is the sum of the multiplication divided by 11 and the Remaining is taken - for 10 is 1.
		
		boolean b = Pattern.matches("[1-6]{1}[0-9]{1}[0-9]{1}[0-1]{1}[0-9]{1}[0-3]{1}[0-9]{1}[0-5]{1}[0-9]{1}[0-9]{1}[0-9]{1}[0-9]{1}[0-9]{1}", cnp);
		if (!b) {
			return false;
		}
		String controlNumber = "279146358279";
		
		int sum = 0;
		for (int i=0;i<12;i++) {
			sum = sum + Integer.valueOf(String.valueOf(cnp.charAt(i))) * Integer.valueOf(String.valueOf(controlNumber.charAt(i)));
		}
		
		int rest = sum % 11;
		if (rest == 10) rest = 1;
		
		if(Integer.valueOf(String.valueOf(cnp.charAt(12))) != rest) return false;
		
		return true;
	}

	public static LocalDateTime calculateLocalDateTimeFromFileName(String excelName) {
		if (excelName.contains(".")) {
			int indexOfFirstPoint = excelName.indexOf(".");
			String date = excelName.substring(indexOfFirstPoint-2, indexOfFirstPoint+3) + ".2020 00:00";
			
			return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
			
		}
		return null;
	}
	
	public static LocalDate parseBirthDateFromCNP(String cnp) {
		if (Utils.isEmpty(cnp)) return null; 
		
		String birthDateString = cnp.substring(1, 7);
		
		if (cnp.startsWith("1") || cnp.startsWith("2")) {
			birthDateString = "19" + birthDateString;
		} else if (cnp.startsWith("5") || cnp.startsWith("6")){
			birthDateString = "20" + birthDateString;
		} else if (cnp.startsWith("3") || cnp.startsWith("4")){
			birthDateString = "18" + birthDateString;
		} 
		LocalDate birthDate = LocalDate.parse(birthDateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
		
		return birthDate;
		
	}
	
	public static LocalDateTime parseTimeFromFormat(String dateTimeString) {
		// 2020-05-08 21:57 yyyy-MM-dd hh:mm
		// 9/13/20 21:20 M/d/yy HH:mm
		LocalDateTime dateTime = null;
		if (dateTimeString.contains("/")) {
			dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("M/d/yy H:mm"));
		} else {
			if (dateTimeString.length() == 16) {
				dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
			} else {
				dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			}
		}
		return dateTime;
		
	}
	
	/**
	 * Used to make a date into datetime. For example when entering the country for the filter in interface start time for date will be 00:00 and end will be 23:59
	 * @param dateStringFromFilter
	 * @param format
	 * @param localTime
	 * @return
	 */
	public static LocalDateTime parseTimeFromDate(String dateStringFromFilter, String format, LocalTime localTime) {
		//LocalDateTime.of(date, time);
		LocalDateTime dateTime = LocalDateTime.of(Utils.parseDateFromJSPInterface(dateStringFromFilter, format), localTime);
		return dateTime;
		
	}
	
	/** Utility method to parse date from formats that come from the gui interface usually yyyy-MM-dd */
	public static LocalDate parseDateFromJSPInterface(String date, String format) {
		LocalDate processedDate = null;
		try {
			processedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(Constants.JSP_DEFAULT));
		} catch (DateTimeParseException e) {
			processedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
			e.printStackTrace();
			LOGGER_UTILS.log(Level.FATAL, date + "nu s-a putut parsa cu " + format);
		}
			
		
		return processedDate;
		
	}
	
	public static LocalDate parseDateFromDDMMYYYYFormat(String date) {
		LocalDate processedDate = null;
		if ( date.contains(".")) {
			processedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(Constants.DATE_PATTERN));
		} else {
			processedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(Constants.SLASH_DATE_PATTER_SIMPLE));			
		}
		
		return processedDate;
		
	}
	
	public static LocalDate parseDateFromExcel(String date) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
	            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/d/yy"))
	    	    .appendOptional(DateTimeFormatter.ofPattern("M/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/d/yy"))
	            .toFormatter();
		
		LocalDate processedDate = LocalDate.parse(date, formatter);;
		
		return processedDate;
	}
	
	public static LocalDate parseDataNasteriiFromExcel(String date) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
	            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/d/yy"))
	    	    .appendOptional(DateTimeFormatter.ofPattern("M/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/d/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("d.MM.yyyy"))
	            .toFormatter();
		
		LocalDate processedDate = LocalDate.parse(date, formatter);;
		
		return processedDate;
	}
	
	public static LocalDate parseDataScoatereDinMonitorizare(String date) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
	            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("d/MM/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("dd/M/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("d/M/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/d/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/d/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

	            .toFormatter();
		
		LocalDate processedDate = LocalDate.parse(date, formatter);;
		
		return processedDate;
	}
	
	public static LocalDate parseDataIntrareInTaraSauContactLocalDate(String date) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
	            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("d/MM/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("dd/M/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("d/M/yyyy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/d/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/d/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

	            .toFormatter();
		
		LocalDate processedDate = LocalDate.parse(date, formatter);;
		
		return processedDate;
	}
	
	public static LocalDate parseDateFromMDY(String date) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
	            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("MM/d/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/dd/yy"))
	            .appendOptional(DateTimeFormatter.ofPattern("M/d/yy"))
	            .toFormatter();
		
		LocalDate processedDate = LocalDate.parse(date, formatter);;
		
		return processedDate;
	}
	
	public static LocalDate parseLocalDateForExcelImport(String date) {
		if (date.contains("."))
			return LocalDate.parse(date, DateTimeFormatter.ofPattern(Constants.DATE_PATTERN));
		else 
			return LocalDate.parse(date, DateTimeFormatter.ofPattern(Constants.SLASH_DATE_PATTER));
	
	}
	
	public static String fromDateToString(LocalDate date) {
		return date.format(DateTimeFormatter.ofPattern(Constants.JSP_DEFAULT));
	}
	
	public static String fromDateTimeToString(LocalDateTime date) {
		return date.format(DateTimeFormatter.ofPattern(Constants.DB_DEFAUT));
	}
	
	public static boolean isEmpty(String text) {
		if (text != null && !text.isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Really important method that checks toate orasele_comune si localtatile(satele) aferente dca sunt in adresa si intoarce orasul/comuna. 
	 * Ex. ALEXANDRU CEL BUN - VIISOARA, ALEXANDRU CEL BUN, VALENI
	 *  
	 * @param localitati
	 * @param localitatiContinute - localitatile care pot fi incluse ca nume in alte localitati, de ex TIBUCANI SI TIBUCANII DE JOS
	 * @param localitatiCuOrasComunaAferenta - map cu localitatile ca si cheie si orasul/comuna ca valoare
	 * @param localitatiDuplicate - localitatile care se pot regasi si in alte orase/comune (ex. Rediu)
	 * @param adresaDeVerificat
	 * @return
	 */
	public static String detecteazaOrasComunaInAdresa (Map<String, List<String>> localitati, Map<String, List<String>> localitatiContinute, 
			Map<String, String> localitatiCuOrasComunaAferenta, List<String> localitatiDuplicate, String adresaDeVerificat) {
		//verifica toate localitatile daca se regasesc in adresa si daca sunt cele repetitive daca au comuna, sat imediat inainte altfel pune in mesaj si nu salva
		if (Utils.isEmpty(adresaDeVerificat)) {
			return null;
		}
		
		//tratare cazuri speciale, de exemplu 'BICAZ-CHEI' ar trebui sa fie 'BICAZ CHEI', PIATRA-NEAMT se asteapta sa fie PIATRA NEAMT, P. NEAMT => PIATRA NEAMT etc. 
		adresaDeVerificat = adresaDeVerificat.toUpperCase();
		adresaDeVerificat = adresaDeVerificat.replaceAll("BICAZ-CHEI", "COM. BICAZ CHEI");
		adresaDeVerificat = adresaDeVerificat.replaceAll("PIATRA NEAMT", "MUN. PIATRA NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("PIATRA-NEAMT", "MUN. PIATRA NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("P NEAMT", "MUN. PIATRA NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("P. NEAMT", "MUN. PIATRA NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TIRGU NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TIRGU-NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TARGU-NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TARGU NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TG NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TG. NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TG.NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TRG NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TRG. NEAMT", "ORAS TARGU NEAMT");
		adresaDeVerificat = adresaDeVerificat.replaceAll("TRG.NEAMT", "ORAS TARGU NEAMT");
		
		//cazuri speciale: BODESTI BICAZ CHEI, BICAZU ARDELEAN, BICAZ, POIENI POIENILE OANCEI RAZBOIENI TIBUCANII DE JOS, TICOS-FLOAREA MUNCELU DE JOS, IZVORU MUNTELUI IZVORU ALB, IZVORU
		//"POIANA", "POIANA CRACAOANI", "POIANA HUMEI", "POIANA LARGULUI", "POIANA TEIULUI"
		//select * from dsp.localitati order by nume;

		//select * from dsp.comune_orase order by nume;
		
		for (String orasComuna:localitati.keySet()) {
			for (String localitate:localitati.get(orasComuna)) {
				
				//e un caz in care mai exista si localitati cu nume mai lung
				if (localitatiContinute.containsKey(localitate)) {
					//verfica mai intai pe cele ce ar putea contine un nume ca daca vin cele scurte sa se elimine posibilitatea de a fi trimise gresit 
					//de exemplu verifica mai intai BICAZ CHEI, cand va urma BICAZ sa nu se duca eronat in BICAZ CHEI
					for (String localitateLunga:localitatiContinute.get(localitate)) {
						
						//verifica cele lungi
						if (existaLocalitateDinAdresa(localitatiDuplicate, adresaDeVerificat, localitatiCuOrasComunaAferenta.get(localitateLunga), localitateLunga)) {
										return localitatiCuOrasComunaAferenta.get(localitateLunga);
						}
					}
					//abia acum verific-o si pe cea scurta cu orasul si Comuna de la inceput
					if (existaLocalitateDinAdresa(localitatiDuplicate, adresaDeVerificat, orasComuna, localitate)) {
						return localitatiCuOrasComunaAferenta.get(localitate);
					}	
				}
				//abia acum cauta tot dupa ce te-ai uitat in cele lungi
				if (existaLocalitateDinAdresa(localitatiDuplicate, adresaDeVerificat, orasComuna, localitate)) {
					return orasComuna;
				}
			}
		}
		
		return null;
	}

	/**
	 * @param localitatiDuplicate
	 * @param adresaDeVerificat
	 * @param orasComuna
	 * @param localitate
	 */
	private static boolean existaLocalitateDinAdresa(List<String> localitatiDuplicate, String adresaDeVerificat,
			String orasComuna, String localitate) {
		//verificam urmatoarele in combinatii de cu punct, fara punct, faara spatiu
		for (String formulare: FORMULARE_LOCALITATI) {
			String formularePlusLocalitate = formulare.toUpperCase() + localitate.toUpperCase();
			String formularePlusLocalitatePunct = formulare.toUpperCase() + "." + localitate.toUpperCase();
			String formularePlusLocalitateCuSpatiu = formulare.toUpperCase() + " " + localitate.toUpperCase();
			String formularePlusLocalitatePunctCuSpatiu = formulare.toUpperCase() + ". " + localitate.toUpperCase();
			
			String formularePlusOrasComuna = formulare.toUpperCase() + orasComuna.toUpperCase();
			String formularePlusOrasComunaPunct = formulare.toUpperCase() + "." + orasComuna.toUpperCase();
			String formularePlusOrasComunaCuSpatiu = formulare.toUpperCase() + " " + orasComuna.toUpperCase();
			String formularePlusOrasComunaPunctCuSpatiu = formulare.toUpperCase() + ". " + orasComuna.toUpperCase();
			
			if ((adresaDeVerificat.toUpperCase().contains(formularePlusLocalitate) ||
					adresaDeVerificat.toUpperCase().contains(formularePlusLocalitatePunct) ||
					adresaDeVerificat.toUpperCase().contains(formularePlusLocalitateCuSpatiu) ||
					adresaDeVerificat.toUpperCase().contains(formularePlusLocalitatePunctCuSpatiu))
					&& !localitatiDuplicate.contains(localitate)) {
				return true;
			} else if (orasComuna != localitate 
					&& (adresaDeVerificat.toUpperCase().contains(formularePlusOrasComuna) ||
							adresaDeVerificat.toUpperCase().contains(formularePlusOrasComunaPunct) ||
							adresaDeVerificat.toUpperCase().contains(formularePlusOrasComunaCuSpatiu) ||
							adresaDeVerificat.toUpperCase().contains(formularePlusOrasComunaPunctCuSpatiu))
					&& (adresaDeVerificat.toUpperCase().contains(formularePlusLocalitate) ||
							adresaDeVerificat.toUpperCase().contains(formularePlusLocalitatePunct) ||
							adresaDeVerificat.toUpperCase().contains(formularePlusLocalitateCuSpatiu) ||
							adresaDeVerificat.toUpperCase().contains(formularePlusLocalitatePunctCuSpatiu))) {
				return true; //acesta este cazul cand localitatile sunt dintre cele dublate, dar se gaseste si comuna si localitatea
			}
		}
		
		return false;
	}
	
	public static void loadProperties() {
		InputStream input = null; 
		
		try {
	
			input = new FileInputStream("config.properties");
	
			// load a properties file
			prop.load(input);
	
			// get the property value and print it out
			LOGGER_UTILS.log(Level.INFO, "DB Server Name: " + prop.getProperty("dbserver") + " Port       : " + prop.getProperty("dbport") + " DB User       : " + prop.getProperty("dbuser"));	
			
//			DatabaseConnection.loadDatabaseProperties(prop.getProperty("dbserver"), 
//					prop.getProperty("dbport"), prop.getProperty("dbuser"), prop.getProperty("dbpass"), prop.getProperty("dbname"));
//			
//			ApplicationData.loadApplicationData(prop.getProperty("nume_aplicatie"), prop.getProperty("locatie_fisiere"), 
//					prop.getProperty("locatie_excel_polie_nt"));
			
//			String logFileName = "log" + dateTime.substring(0, 
//					dateTime.lastIndexOf(".")).replaceAll(":", "_") + "." + "log";
		} catch (IOException ex) {
			LOGGER_UTILS.log(Level.FATAL, "Error reading config.properties file", ex);
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void loadLoggingProperties( ) {
		
	}
	
	/**
	 * Public static method to detect the number of lines in a text. 
	 * 
	 * @param text
	 * @return number of lines in the given text
	 */
	public static int countLines(String text) {
	    if(text == null || text.isEmpty())
	    {
	        return 0;
	    }
	    int lines = 1;
	    int pos = 0;
	    while ((pos = text.indexOf("\n", pos) + 1) != 0) {
	        lines++;
	    }
	    return lines;
	}
	
	/**
	 * Method to get the index of the nth occurrence of a char. It will be used mainly for newline \n.
	 * 
	 * @param str
	 * @param c
	 * @param n
	 * @return position of nth occurrence of char c in the given text
	 */
	public static int nthLastIndexOf(String text, char c, int n) {
	    for (int i = 0; i < text.length(); i++)
	    {
	        if (text.charAt(i) == c)
	        {
	            n--;
	            if (n == 0)
	            {
	                return i;
	            }
	        }
	    }
	    return -1;
	}

}
