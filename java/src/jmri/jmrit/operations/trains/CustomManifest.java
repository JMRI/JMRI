package jmri.jmrit.operations.trains;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

import org.jdom.Attribute;
import org.jdom.Element;

import jmri.jmrit.operations.FileHelper;
import jmri.util.SystemType;

public class CustomManifest {

	// To start, all files will be created inside of
	// ../JMRI/operations/csvManifests
	
	private static String directoryName = "csvManifests";
	private static String mcAppName = "MC4JMRI.xls";
	private static final String mcAppArg = "";
	
	private static String csvNamesFileName = "CSVFilesFile.txt";


	private static int fileCount = 0;


//	public CustomManifest() {
//		// First get our working directory, normally
//		// ../Users/User/JMRI/operations/csvManifests
//		workingDir = FileHelper.getOperationsFile("csvManifests");
//
//		csvNamesFile = new File(workingDir, csvNamesFileName);
//
//		// Delete it if it exists
//		if (csvNamesFile.exists())
//			if (!csvNamesFile.delete())
//				log.warn("Not able to delete csv file!");
//	}
	
	public static String getFileName() {
		return mcAppName;
	}
	
	public void setFileName(String name) {
		mcAppName = name;
	}
	
	public static String getCommonFileName() {
		return csvNamesFileName;
	}
	
	public void setCommonFileName(String name) {
		csvNamesFileName = name;
	}

	
	public static String getDirectoryName() {
		return directoryName;
	}
	
	public void setDirectoryName(String name) {
		directoryName = name;
	}

	/**
	 * Adds one CSV file path to the collection of files to be processed.
	 * 
	 * @param csvFile
	 */
	public static void addCVSFile(File csvFile) {
		// Ignore null files...
		if (csvFile == null)
			return;
		
		File workingDir = FileHelper.getOperationsFile(getDirectoryName());
		File csvNamesFile = new File(workingDir, csvNamesFileName);

		try {
			FileHelper.appendTextToFile(csvNamesFile, csvFile.getAbsolutePath());
			fileCount++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes the CSV files using a Custom external program that reads the
	 * file of file names.
	 */
	public static boolean process() {

		// Some alternates for testing...
		// mcAppName="notepad";
		// mcAppName = "ShowCurrentDir.exe";

		// Only continue if we have some files to process.
		if (fileCount == 0)
			return false;

		// Build our command string out of these bits
		// We need to use cmd and start to allow launching data files like
		// Excel spreadsheets
		// It should work OK with actual programs.
		// Not sure how to do this on Mac and Linux....
		// For now, just complain if we are not on Windows...

		if (!SystemType.isWindows()) {
			JOptionPane
					.showMessageDialog(
							null,
							"Custom processing of manifest csv files is only supported on Windows at the moment.",
							"Custom manifests not supported",
							JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if (!manifestCreatorFileExists())
			return false;

		String cmd = "cmd /c start " + getFileName() + " " + mcAppArg; // NOI18N

		try {
			Runtime.getRuntime().exec(cmd, null, FileHelper.getOperationsFile(getDirectoryName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static boolean manifestCreatorFileExists() {
		File file = new File(FileHelper.getOperationsFile(getDirectoryName()), getFileName());
		return file.exists();
	}
	
	
	
	public static void load(Element options) {
		Element mc = options.getChild(Xml.MANIFEST_CREATOR);
		if (mc != null) {
			Attribute a;
			Element directory = mc.getChild(Xml.DIRECTORY);
			if (directory != null && (a = directory.getAttribute(Xml.NAME)) != null)
				directoryName = a.getValue();
			Element file = mc.getChild(Xml.RUN_FILE);
			if (file != null && (a = file.getAttribute(Xml.NAME)) != null)
				mcAppName = a.getValue();
			Element common = mc.getChild(Xml.COMMON_FILE);
			if (common != null && (a = common.getAttribute(Xml.NAME)) != null)
				csvNamesFileName = a.getValue();
		}
	}
	
	public static void store(Element options) {
		Element mc = new Element(Xml.MANIFEST_CREATOR);
		Element file = new Element(Xml.RUN_FILE);
		file.setAttribute(Xml.NAME, getFileName());
		Element directory = new Element(Xml.DIRECTORY);
		directory.setAttribute(Xml.NAME, getDirectoryName());
		Element common = new Element(Xml.COMMON_FILE);
		common.setAttribute(Xml.NAME, getCommonFileName());
		mc.addContent(directory);
		mc.addContent(file);
		mc.addContent(common);
		options.addContent(mc);
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CustomManifest.class
			.getName());
}
