package jmri.configurexml;

import apps.tests.Log4JFixture;
import java.awt.GraphicsEnvironment;
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
import org.junit.Assume;
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
 * <p>
 * public LoadAndStoreTest(File file, boolean pass) { super(file, pass); }
 * }
 * }
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
@RunWith(Parameterized.class)
public class LoadAndStoreTestBase {

    // allows code reuse when building the parameter collection in getFiles()
    private final File file;

    public enum SaveType {
        All, Config, Prefs, User, UserPrefs
    }

    private SaveType saveType = SaveType.Config;

    public LoadAndStoreTestBase(File file, boolean pass) {
        this(file, pass, SaveType.Config);
    }

    public LoadAndStoreTestBase(File file, boolean pass, SaveType saveType) {
        this.file = file;
        this.saveType = saveType;
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

    public static void checkFile(File inFile1, File inFile2) throws Exception {
        // compare files, except for certain special lines
        BufferedReader fileStream1 = new BufferedReader(
                new InputStreamReader(new FileInputStream(inFile1)));
        BufferedReader fileStream2 = new BufferedReader(
                new InputStreamReader(new FileInputStream(inFile2)));

        String line1 = fileStream1.readLine();
        String line2 = fileStream2.readLine();

        int count = 0;
        String next1, next2;
        while ((next1 = fileStream1.readLine()) != null && (next2 = fileStream2.readLine()) != null) {
            count++;

            boolean match = false;  // assume failure (pessimist!)

            String[] startsWithStrings = {
                "  <!--Written by JMRI version",
                "  <timebase", // time changes from timezone to timezone
                "    <test>", // version changes over time
                "    <modifier", // version changes over time
                "    <major", // version changes over time
                "    <minor", // version changes over time
                "<?xml-stylesheet", // Linux seems to put attributes in different order
                "    <memory systemName=\"IMCURRENTTIME\"", // time varies - old format
                "    <modifier>This line ignored</modifier>"
            };
            for (String startsWithString : startsWithStrings) {
                if (line1.startsWith(startsWithString) && line2.startsWith(startsWithString)) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                String memory_value = "<memory value";
                if (line1.contains(memory_value) && line2.contains(memory_value)) {
                    String imcurrenttime = "<systemName>IMCURRENTTIME</systemName>";
                    if (next1.contains(imcurrenttime) && next2.contains(imcurrenttime)) {
                        match = true;
                        break;
                    }
                }
            }
            if (!match) {
                String date_string = "<date>";
                if (line1.contains(date_string) && line2.contains(date_string)) {
                    match = true;
                }
            }
            if (!match && !line1.equals(line2)) {
                log.error("match failed in LoadAndStoreTest: Current line " + count);
                log.error("    line1: \"" + line1 + "\"");
                log.error("    line2: \"" + line2 + "\"");
                log.error("  comparing file \"" + inFile1.getPath() + "\"");
                log.error("         to file \"" + inFile2.getPath() + "\"");
                Assert.assertEquals(line1, line2);
            }
            line1 = next1;
            line2 = next2;
        }
        fileStream1.close();
        fileStream2.close();
    }

    public static void loadFile(File inFile) throws Exception {
        // load file
        InstanceManager.getDefault(ConfigureManager.class).load(inFile);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
    }

    public static File storeFile(File inFile, SaveType inSaveType) throws Exception {
        String name = inFile.getName();
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name);

        switch (inSaveType) {
            case All: {
                InstanceManager.getDefault(ConfigureManager.class).storeAll(outFile);
                break;

            }
            case Config: {
                InstanceManager.getDefault(ConfigureManager.class).storeConfig(outFile);
                break;

            }
            case Prefs: {
                InstanceManager.getDefault(ConfigureManager.class).storePrefs(outFile);
                break;

            }
            case User: {
                InstanceManager.getDefault(ConfigureManager.class).storeUser(outFile);
                break;

            }
            case UserPrefs: {
                InstanceManager.getDefault(ConfigureManager.class).storeUserPrefs(outFile);
                break;
            }
            default: {
                log.error("Unknown save type {}.", inSaveType);
                break;
            }
        }

        return outFile;
    }

    @Test
    public void loadLoadStoreFileCheck() throws Exception {
        if ((saveType == SaveType.All) || (saveType == SaveType.User)) {
            Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        }

        log.debug("Start check file " + this.file.getCanonicalPath());

        loadFile(this.file);
        // Panel sub-classes (with GUI) will fail if you try to load them twice.
        // (So don't!)
        if ((saveType != SaveType.All) && (saveType != SaveType.User)) {
            loadFile(this.file);
        }

        // find comparison files
        File compFile = new File(this.file.getCanonicalFile().getParentFile().getParent() + "/loadref/" + this.file.getName());
        if (!compFile.exists()) {
            compFile = this.file;
        }
        log.debug("   Chose comparison file " + compFile.getCanonicalPath());

        File outFile = storeFile(this.file, this.saveType);
        checkFile(compFile, outFile);
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
