// LoadAndStoreTestBase.java
package jmri.configurexml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for testing load-and-store of configuration files.
 * <p>
 * Including "LoadAndStoreTestBase.makeSuite("java/test/jmri/jmrit/display/configurexml/")"
 * in a subclass's test suite
 * will test each file in a "load" directory by loading it, then storing it,
 * then comparing (with certain lines skipped) against either a file by the same
 * name in the "loadref" directory, or against the original file itself.
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 * @version $Revision$
 */
public class LoadAndStoreTestBase extends TestCase {

    public LoadAndStoreTestBase(String s) {
        super(s);
    }

    /**
     * Create the tests that load-and-store test contents of all files in a
     * directory
     */
    static public void loadAndStoreAllInDirectory(TestSuite suite, String name) {

        java.io.File dir = new java.io.File(name + "/load/");
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".xml")) {
                suite.addTest(new CheckOneFilePasses(files[i]));
            }
        }
    }

    static void loadInit() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
    }
    
    static void checkFile(File inFile, File outFile) throws Exception {
        // find comparison files
        File compFile = new File(inFile.getCanonicalFile().getParentFile().getParent() + "/loadref/" + inFile.getName());
        if (!compFile.exists()) {
            compFile = inFile;
        }
        log.debug("   Chose comparison file " + compFile.getCanonicalPath());

        // compare files, except for certain special lines
        BufferedReader inFileStream = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(compFile)));
        BufferedReader outFileStream = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(outFile)));
        String inLine;
        String outLine;
        int count = 0;
        while ((inLine = inFileStream.readLine()) != null && (outLine = outFileStream.readLine()) != null) {
            count++;
            if (!inLine.startsWith("  <!--Written by JMRI version")
                    && !inLine.startsWith("  <timebase") // time changes from timezone to timezone
                    && !inLine.startsWith("    <test>") // version changes over time
                    && !inLine.startsWith("    <modifier") // version changes over time
                    && !inLine.startsWith("    <major") // version changes over time
                    && !inLine.startsWith("    <minor") // version changes over time
                    && !inLine.startsWith("<?xml-stylesheet") // Linux seems to put attributes in different order
                    && !inLine.startsWith("    <memory systemName=\"IMCURRENTTIME\"") // time varies
                    && !inLine.startsWith("    <modifier>This line ignored</modifier>")) {
                if (!inLine.equals(outLine)) {
                    log.error("match failed in testLoadStoreCurrent line " + count);
                    log.error("   inLine = \"" + inLine + "\"");
                    log.error("  outLine = \"" + outLine + "\"");
                }
                Assert.assertEquals(inLine, outLine);
            }
        }
        inFileStream.close();
        outFileStream.close();
    }
    
    static void loadFile(File inFile) throws Exception  {
        // load file
        InstanceManager.configureManagerInstance().load(inFile);

        InstanceManager.logixManagerInstance().activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
    }
    
    static File storeFile(File inFile) throws Exception  {
        String name = inFile.getName();
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name);
        InstanceManager.configureManagerInstance().storeConfig(outFile);
        return outFile;
    }
    
    static public void loadStoreFileCheck(File inFile) throws Exception {

        log.debug("Start check file " + inFile.getCanonicalPath());

        loadInit();
        
        loadFile(inFile);

        // store file
        String name = inFile.getName();
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name);
        InstanceManager.configureManagerInstance().storeConfig(outFile);

        checkFile(inFile, outFile);
    }

    /**
     * Test by loading twice
     */
    static public void loadLoadStoreFileCheck(File inFile) throws Exception {

        log.debug("Start check file " + inFile.getCanonicalPath());

        loadInit();
        
        loadFile(inFile);
        loadFile(inFile);

        String name = inFile.getName();

        // store file
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name);
        InstanceManager.configureManagerInstance().storeConfig(outFile);

        checkFile(inFile, outFile);
    }

    /**
     * Internal TestCase class to allow separate tests for every file
     */
    static public class CheckOneFilePasses extends TestCase {

        File file;

        public CheckOneFilePasses(File file) {
            super("Test schema valid: " + file);
            this.file = file;
        }

        public void runTest() throws Exception {
            loadLoadStoreFileCheck(file);
        }
    }

    // Default test suite does all files in load subdirectory
    // test suite from all defined tests
    public static Test makeSuite(String dir) {
        TestSuite suite = new TestSuite("Load-and-store checks");
        loadAndStoreAllInDirectory(suite, dir);
        return suite;
    }

    // The minimal setup for log4J and debug instances
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(LoadAndStoreTest.class.getName());
}
