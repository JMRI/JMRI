package jmri.configurexml;


import apps.tests.Log4JFixture;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for testing load-and-store of configuration files.
 * <p>
 * Creating a parameterized test class that extends this class will test each
 * file in a "load" directory by loading it, then storing it, then comparing
 * (with certain lines skipped) against either a file by the same name in the
 * "loadref" directory, or against the original file itself. A minimal test
 * class is: {@code
 * @RunWith(Parameterized.class)
 * public class LoadAndStoreTest extends LoadAndStoreTestBase {
 *
 * @Parameterized.Parameters(name = "{0} (pass={1})")
 * public static Iterable<Object[]> data() { return getFiles(new
 * File("java/test/jmri/configurexml"), false, true); }
 *
 * public LoadAndStoreTest(File file, boolean pass) { super(file, pass); }
 * }
 * }
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
@RunWith(Parameterized.class)
public class LoadAndStoreTestBase {

    private final File file;
    // allows code reuse when building the parameter
    // collection in getFiles()

    public LoadAndStoreTestBase(File file, boolean pass) {
        this.file = file;
    }

    /**
     * Get all XML files in a directory and validate the ability to load and
     * store them.
     *
     * @param directory the directory containing XML files; the subdirectory
     *                  <code>load</code> under this directory will be used
     * @param recurse   if true, will recurse into subdirectories
     * @param pass      if true, successful validation will pass; if false,
     *                  successful validation will fail
     * @return a collection of Object arrays, where each array contains the
     *         {@link java.io.File} to validate and a boolean matching the pass
     *         parameter
     */
    public static Collection<Object[]> getFiles(File directory, boolean recurse, boolean pass) {
        // since this method gets the files to test, but does not trigger any
        // tests itself, we can use SchemaTestBase.getFiles() by adding "load"
        // to the directory to test
        return SchemaTestBase.getFiles(new File(directory, "load"), recurse, pass);
    }

    /**
     * Get all XML files in the immediate subdirectories of a directory and
     * validate them.
     *
     * @param directory the directory containing subdirectories containing XML
     *                  files
     * @param recurse   if true, will recurse into subdirectories
     * @param pass      if true, successful validation will pass; if false,
     *                  successful validation will fail
     * @return a collection of Object arrays, where each array contains the
     *         {@link java.io.File} with a filename ending in {@literal .xml} to
     *         validate and a boolean matching the pass parameter
     * @throws IllegalArgumentException if directory is a file
     */
    public static Collection<Object[]> getDirectories(File directory, boolean recurse, boolean pass) throws IllegalArgumentException {
        // since this method gets the files to test, but does not trigger any
        // tests itself, we can use SchemaTestBase.getDirectories() by adding "load"
        // to the directory to test
        return SchemaTestBase.getDirectories(new File(directory, "load"), recurse, pass);
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
        String nextIn;
        String nextOut;
        int count = 0;
        inLine = inFileStream.readLine();
        outLine = outFileStream.readLine();
        while ( (nextIn = inFileStream.readLine()) != null && (nextOut = outFileStream.readLine()) != null) {
            count++;
            if (!inLine.startsWith("  <!--Written by JMRI version")
                    && !inLine.startsWith("  <timebase") // time changes from timezone to timezone
                    && !inLine.startsWith("    <test>") // version changes over time
                    && !inLine.startsWith("    <modifier") // version changes over time
                    && !inLine.startsWith("    <major") // version changes over time
                    && !inLine.startsWith("    <minor") // version changes over time
                    && !inLine.startsWith("<?xml-stylesheet") // Linux seems to put attributes in different order
                    && !inLine.startsWith("    <memory systemName=\"IMCURRENTTIME\"") // time varies - old format
                    && !(inLine.contains("<memory value") && nextIn.contains("IMCURRENTTIME")) // time varies - new format
                    && !inLine.startsWith("    <modifier>This line ignored</modifier>")) {
                if (!inLine.equals(outLine)) {
                    log.error("match failed in testLoadStoreCurrent line " + count);
                    log.error("   inLine = \"" + inLine + "\"");
                    log.error("  outLine = \"" + outLine + "\"");
                    log.error("     comparing \"" + inFile.getName() + "\" and \"" + outFile.getName() + "\"");
                }
                Assert.assertEquals(inLine, outLine);
            }
            inLine = nextIn;
            outLine = nextOut;
        }
        inFileStream.close();
        outFileStream.close();
    }

    static void loadFile(File inFile) throws Exception {
        // load file
        InstanceManager.getDefault(ConfigureManager.class).load(inFile);

        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
    }

    static File storeFile(File inFile) throws Exception {
        String name = inFile.getName();
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name);
        InstanceManager.getDefault(ConfigureManager.class).storeConfig(outFile);
        return outFile;
    }

    @Test
    public void loadLoadStoreFileCheck() throws Exception {

        log.debug("Start check file " + this.file.getCanonicalPath());

        loadFile(this.file);
        loadFile(this.file);

        String name = this.file.getName();

        // store file
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name);
        InstanceManager.getDefault(ConfigureManager.class).storeConfig(outFile);

        checkFile(this.file, outFile);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LoadAndStoreTest.class);
}
