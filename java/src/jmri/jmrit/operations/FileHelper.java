package jmri.jmrit.operations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import jmri.util.FileUtil;

/**
 * Helper class for working with Files and Paths.
 * 
 * @author Gregory Madsen Copyright (C) 2012
 * 
 */

public class FileHelper {

//	/**
//	 * Simple helper method to just append a text string to the end of the given
//	 * filename. The file will be created if it does not exist.
//	 */
//	public static void appendTextToFile(String fileName, String text)
//			throws IOException {
//
//		FileWriter out = new FileWriter(fileName, true);
//		PrintWriter pw = new PrintWriter(out);
//
//		pw.println(text);
//
//		pw.close();
//	}

	public static void appendTextToFile(File file, String text)
			throws IOException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				file, true), "UTF-8"));	// NOI18N

		pw.println(text);

		pw.close();
	}


	/**
	 * Returns a File reference to the Operations main directory.  
	 * @return file
	 */
	public static File getOperationsDirectory() {
		return new File(FileUtil.getUserFilesPath(),
				OperationsXml.getOperationsDirectoryName());
	}

	/**
	 * Returns a File reference to a file inside the Operations directory.
	 */
	public static File getOperationsFile(String fileName) {

		return new File(getOperationsDirectory(), fileName);
	}

	/**
	 * Returns a File reference to a file inside of the given subdirectory,
	 * under the Operations directory.
	 * 
	 * @param subDir
	 * @param fileName
	 * @return file
	 */
	public static File getOperationsSubFile(String subDir, String fileName) {
		File operations = getOperationsDirectory();

		File sub = new File(operations, subDir);

		return new File(sub, fileName);
	}

}
