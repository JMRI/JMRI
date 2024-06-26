package jmri.jmrit.operations.trains.excel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.FileUtil;
import jmri.util.SystemType;

public abstract class TrainCustomCommon {

    protected final String xmlElement;
    protected String directoryName;
    private String mcAppName = "MC4JMRI.xls"; // NOI18N
    private final String mcAppArg = ""; // NOI18N
    private String csvNamesFileName = "CSVFilesFile.txt"; // NOI18N
    private int fileCount = 0;
    private long waitTimeSeconds = 0;
    private Process process;
    private boolean alive = false; // when true files to be processed

    protected TrainCustomCommon(String dirName, String xmlElement) {
        directoryName = dirName;
        this.xmlElement = xmlElement;
    }

    public String getFileName() {
        return mcAppName;
    }

    public void setFileName(String name) {
        if (!getFileName().equals(name)) {
            mcAppName = name;
            InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        }
    }

    public String getCommonFileName() {
        return csvNamesFileName;
    }

    public void setCommonFileName(String name) {
        csvNamesFileName = name;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String name) {
        directoryName = name;
    }

    public String getDirectoryPathName() {
        return InstanceManager.getDefault(OperationsManager.class).getFile(getDirectoryName()).getPath();
    }

    /**
     * Adds one CSV file path to the collection of files to be processed.
     *
     * @param csvFile The File to add.
     * @return true if successful
     *
     */
    @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification = "FindBugs incorrectly reports not guarded by conditional control flow")
    public synchronized boolean addCsvFile(File csvFile) {
        // Ignore null files...
        if (csvFile == null || !excelFileExists()) {
            return false;
        }

        // once the process starts, we can't add files to the common file
        while (InstanceManager.getDefault(TrainCustomManifest.class).isProcessAlive() ||
                InstanceManager.getDefault(TrainCustomSwitchList.class).isProcessAlive()) {
            synchronized (this) {
                try {
                    wait(1000); // 1 sec
                } catch (InterruptedException e) {
                    // we don't care
                }
            }
        }

        fileCount++;
        waitTimeSeconds = fileCount * Control.excelWaitTime;
        alive = true;

        File csvNamesFile = new File(InstanceManager.getDefault(OperationsManager.class).getFile(getDirectoryName()),
                getCommonFileName());

        try {
            FileUtil.appendTextToFile(csvNamesFile, csvFile.getAbsolutePath());
            log.debug("Queuing file {} to list", csvFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Unable to write to {}", csvNamesFile, e);
            return false;
        }
        return true;
    }

    /**
     * Processes the CSV files using a Custom external program that reads the
     * file of file names.
     *
     * @return True if successful.
     */
    @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification = "FindBugs incorrectly reports not guarded by conditional control flow")
    public synchronized boolean process() {

        // check to see it the Excel program is available
        if (!excelFileExists() || getFileName().isBlank()) {
            return false;
        }

        // Only continue if we have some files to process.
        if (fileCount == 0) {
            return true; // done
        }

        // only one copy of the excel program is allowed to run.  Two copies running in parallel has issues.
        while (InstanceManager.getDefault(TrainCustomManifest.class).isProcessAlive() ||
                InstanceManager.getDefault(TrainCustomSwitchList.class).isProcessAlive()) {
            synchronized (this) {
                try {
                    wait(1000); // 1 sec
                } catch (InterruptedException e) {
                    // we don't care
                }
            }
        }

        log.debug("Queued {} files to custom Excel program", fileCount);

        // Build our command string out of these bits
        // We need to use cmd and start to allow launching data files like
        // Excel spreadsheets
        // It should work OK with actual programs.
        if (SystemType.isWindows()) {
            String[] cmd = {"cmd", "/c", "start", getFileName(), mcAppArg}; // NOI18N
            try {
                process = Runtime.getRuntime().exec(cmd, null,
                        InstanceManager.getDefault(OperationsManager.class).getFile(getDirectoryName()));
            } catch (IOException e) {
                log.error("Unable to execute {}", getFileName(), e);
            }
        } else {
            String[] cmd = {"open", getFileName(), mcAppArg}; // NOI18N
            try {
                process = Runtime.getRuntime().exec(cmd, null,
                        InstanceManager.getDefault(OperationsManager.class).getFile(getDirectoryName()));
            } catch (IOException e) {
                log.error("Unable to execute {}", getFileName(), e);
            }
        }
        fileCount = 0;
        return true;
    }

    public boolean excelFileExists() {
        File file = new File(InstanceManager.getDefault(OperationsManager.class).getFile(getDirectoryName()),
                getFileName());
        return file.exists();
    }

    @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification = "FindBugs incorrectly reports not guarded by conditional control flow")
    public boolean checkProcessReady() {
        if (!isProcessAlive()) {
            return true;
        }
        if (alive) {
            log.debug("Wait time: {} seconds process ready", waitTimeSeconds);
            long loopCount = waitTimeSeconds; // number of seconds to wait
            while (loopCount-- > 0 && alive) {
                synchronized (this) {
                    try {
                        wait(1000); // 1 sec
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        log.error("Thread unexpectedly interrupted", e);
                    }
                }
            }
        }
        return !alive;
    }

    public boolean isProcessAlive() {
        if (process != null) {
            return process.isAlive();
        } else {
            return false;
        }
    }

    /**
     *
     * @return true if process completes without a timeout, false if there's a
     *         timeout.
     * @throws InterruptedException if process thread is interrupted
     */
    @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification = "FindBugs incorrectly reports not guarded by conditional control flow")
    public boolean waitForProcessToComplete() throws InterruptedException {
        if (process == null) {
            return true; // process hasn't been initialized
        }
        boolean status = false;
        synchronized (process) {
            File file = new File(InstanceManager.getDefault(OperationsManager.class).getFile(getDirectoryName()),
                    getCommonFileName());
            if (!file.exists()) {
                log.debug("Common file not found! Normal when processing multiple files");
            }
            log.debug("Waiting up to {} seconds for Excel program to complete", waitTimeSeconds);
            status = process.waitFor(waitTimeSeconds, TimeUnit.SECONDS);
            // printing can take a long time, wait to complete
            if (status && file.exists()) {
                long loopCount = waitTimeSeconds; // number of seconds to wait
                while (loopCount-- > 0 && file.exists()) {
                    synchronized (this) {
                        try {
                            wait(1000); // 1 sec
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            log.error("Thread unexpectedly interrupted", e);
                        }
                    }
                }
            }
            if (file.exists()) {
                log.error("Common file ({}) not deleted! Wait time {} seconds", file.getPath(), waitTimeSeconds);
                return false;
            }
            log.debug("Excel program complete!");
        }
        alive = false; // done!
        return status;
    }

    /**
     * Checks to see if the common file exists
     *
     * @return true if the common file exists
     */
    public boolean doesCommonFileExist() {
        File file = new File(InstanceManager.getDefault(OperationsManager.class).getFile(getDirectoryName()),
                getCommonFileName());
        return file.exists();
    }

    public void load(Element options) {
        Element mc = options.getChild(xmlElement);
        if (mc != null) {
            Attribute a;
            Element directory = mc.getChild(Xml.DIRECTORY);
            if (directory != null && (a = directory.getAttribute(Xml.NAME)) != null) {
                setDirectoryName(a.getValue());
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

    public void store(Element options) {
        Element mc = new Element(xmlElement);
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

    private final static Logger log = LoggerFactory.getLogger(TrainCustomCommon.class);
}
