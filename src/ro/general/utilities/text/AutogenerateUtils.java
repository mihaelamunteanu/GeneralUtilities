package ro.general.utilities.text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AutogenerateUtils {

	public static String firstLetterUpperCase(String word) {
		return 	word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	public static String firstLetterLowerCase(String word) {
		return 	word.substring(0, 1).toLowerCase() + word.substring(1);
	}
	
//	public static String generateFromCodeWithLoopForFields(String code, Map<String> replacements) {
//		
//	}

	public static void makeMissingDirectoriesForPackages(String packageName, String path) {

		String[] folders = packageName.split("\\.");
		for (int i = 0; i < folders.length; i++) {
			path = path + "\\" + folders[i];
			File directory = new File(path);
			if (!directory.exists()){
				directory.mkdir();
				// If you require it to make the entire directory path including parents,
				// use directory.mkdirs(); here instead.
			}
		}

	}

	public static void writeJavaFileFromString(String fileName, String fileContent) {
		try {
			String bytes = fileContent;
			byte[] buffer = bytes.getBytes();

			FileOutputStream outputStream =
					new FileOutputStream(fileName);

			// write() writes as many bytes from the buffer
			// as the length of the buffer. You can also
			// use
			// write(buffer, offset, length)
			// if you want to write a specific number of
			// bytes, or only part of the buffer.
			outputStream.write(buffer);

			// Always close files.
			outputStream.close();       

			System.out.println("Wrote " + buffer.length + 
					" bytes in " + fileName );
		}
		catch(IOException ex) {
			System.out.println(
					"Error writing file '"
							+ fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
	}
}
