package jmri.jmrit.operations.trains;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.setup.Control;
import jmri.util.FileUtil;
import jmri.util.SystemType;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainCustomManifest {

    // To start, all files will be created inside of
    // ../JMRI/operations/csvManifests
    private static String directoryName = "csvManifests"; // NOI18N
    private static String mcAppName = "MC4JMRI.xls"; // NOI18N
    private static final String mcAppArg = ""; // NOI18N

    private static String csvNamesFileName = "CSVFilesFile.txt"; // NOI18N

    private static int fileCount = 0;
    
    private static Process process;

    public static String getFileName() {
        return mcAppName;
    }

    public static void setFileName(String name) {
        mcAppName = name;
        TrainManagerXml.instance().setDirty(true);
    }

    public static String getCommonFileName() {
        return csvNamesFileName;
    }

    public static void setCommonFileName(String name) {
        csvNamesFileName = name;
    }

    public static String getDirectoryName() {
        return directoryName;
    }

    public static void setDirectoryName(String name) {
        directoryName = name;
    }

    /**
     * Adds one CSV file path to the collection of files to be processed.
     *
     * @param csvFile
     */
    public static void addCVSFile(File csvFile) {
        // Ignore null files...
        if (csvFile == null) {
            return;
        }
        alive = true;
        File workingDir = OperationsManager.getInstance().getFile(getDirectoryName());
        File csvNamesFile = new File(workingDir, csvNamesFileName);

        try {
            FileUtil.appendTextToFile(csvNamesFile, csvFile.getAbsolutePath());
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
        if (fileCount == 0) {
            return false;
        }

        // Build our command string out of these bits
        // We need to use cmd and start to allow launching data files like
        // Excel spreadsheets
        // It should work OK with actual programs.
        // Not sure how to do this on Mac and Linux....
        // For now, just complain if we are not on Windows...
        // if (!SystemType.isWindows()) {
        // JOptionPane
        // .showMessageDialog(
        // null,
        // "Custom processing of manifest csv files is only supported on Windows at the moment.",
        // "Custom manifests not supported",
        // JOptionPane.ERROR_MESSAGE);
        // return false;
        // }
        if (!manifestCreatorFileExists()) {
            return false;
        }

        if (SystemType.isWindows()) {
            String cmd = "cmd /c start " + getFileName() + " " + mcAppArg; // NOI18N
            try {
                process = Runtime.getRuntime().exec(cmd, null, OperationsManager.getInstance().getFile(getDirectoryName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String cmd = "open " + getFileName() + " " + mcAppArg; // NOI18N
            try {
                process = Runtime.getRuntime().exec(cmd, null, OperationsManager.getInstance().getFile(getDirectoryName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean manifestCreatorFileExists() {
        File file = new File(OperationsManager.getInstance().getFile(getDirectoryName()), getFileName());
        return file.exists();
    }
    
    public void checkProcessComplete() {
        if (alive) {
            int loopCount = Control.excelWaitTime; // number of seconds to wait
            while (loopCount > 0 && alive) {
                loopCount--;
                synchronized (this) {
                    try {
                        wait(1000); // 1 sec
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public static boolean isProcessAlive() {
        if (process != null) {
            return process.isAlive();
        } else {
            return false;
        }
    }
    
    static boolean alive = false;
    public static void waitForProcessToComplete() throws InterruptedException {
        synchronized (process) {
            process.waitFor(Control.excelWaitTime, TimeUnit.SECONDS);
        }
        alive = false;
    }

    public static void load(Element options) {
        Element mc = options.getChild(Xml.MANIFEST_CREATOR);
        if (mc != null) {
            Attribute a;
            Element directory = mc.getChild(Xml.DIRECTORY);
            if (directory != null && (a = directory.getAttribute(Xml.NAME)) != null) {
                directoryName = a.getValue();
            }
            Element file = mc.getChild(Xml.RUN_FILE);
            if (file != null && (a = file.getAttribute(Xml.NAME)) != null) {
                mcAppName = a.getValue();
            }
            Element common = mc.getChild(Xml.COMMON_FILE);
            if (common != null && (a = common.getAttribute(Xml.NAME)) != null) {
                csvNamesFileName = a.getValue();
            }
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

    private final static Logger log = LoggerFactory.getLogger(TrainCustomManifest.class.getName());
}
