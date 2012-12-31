package jmri.jmrit.operations.trains;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.FileHelper;
import jmri.util.SystemType;

public class CustomManifest {

	// To start, all files will be created inside of
	// ../JMRI/operations/csvManifests
	private File workingDir;
	
	private String csvNamesFileName = "CSVFilesFile.txt";
	private File csvNamesFile;

	public String mcAppName;
	public String mcAppArg = "";

	private int fileCount = 0;


	public CustomManifest() {
		// First get our working directory, normally
		// ../Users/User/JMRI/operations/csvManifests
		workingDir = FileHelper.getOperationsFile("csvManifests");

		csvNamesFile = new File(workingDir, csvNamesFileName);

		// Delete it if it exists
		if (csvNamesFile.exists())
			csvNamesFile.delete();
	}

	/**
	 * Adds one CSV file path to the collection of files to be processed.
	 * 
	 * @param csvPath
	 */
	public void AddCVSFile(File csvFile) {
		// Ignore null files...
		if (csvFile == null)
			return;

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
	public void Process() {

		// Set our application name and any arguments
		// These will come from some user changeable settings mechanism later...
		mcAppName = "ManifestCreatorVer2.11Appl.xls";

		// Some alternates for testing...
		// mcAppName="notepad";
		// mcAppName = "ShowCurrentDir.exe";

		// This will normally not be used for an XLS file, but could be used for
		// a different type of program.
		// mcAppArg = "csvFilesFile.txt";
		mcAppArg = "";

		// Only continue if we have some files to process.
		if (fileCount == 0)
			return;

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
			return;
		}

		String cmd = "cmd /c start " + mcAppName + " " + mcAppArg;

		try {
			Runtime.getRuntime().exec(cmd, null, workingDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
