package jmri.configurexml;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
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
 * class is:
 <pre>
   @RunWith(Parameterized.class)
   public class LoadAndStoreTest extends LoadAndStoreTestBase {
 
     @Parameterized.Parameters(name = "{0} (pass={1})")
     public static Iterable<Object[]> data() { 
       return getFiles(new File("java/test/jmri/configurexml"), false, true); 
     }
  
     public LoadAndStoreTest(File file, boolean pass) { super(file, pass); }
   }
</pre>
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
    private boolean guiOnly = false;

    /**
     * Get all XML files in a directory and validate the ability to load and
     * store them.
     *
     * @param file      the file to be tested
     * @param pass      if true, successful validation will pass; if false,
     *                  successful validation will fail
     * @param saveType  the type (i.e. level) of ConfigureXml information being saved
     * @param isGUI     true for files containing GUI elements, i.e. panels.  These
     *                  can only be loaded once (others can be loaded twice, and that's
     *                  tested when this is false), and can't be loaded when running headless.
     */
    public LoadAndStoreTestBase(File file, boolean pass, SaveType saveType, boolean isGUI) {
        this.file = file;
        this.saveType = saveType;
        this.guiOnly = isGUI;
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

        int lineNumber1 = 0, lineNumber2 = 0;
        String next1, next2;
        while ((next1 = fileStream1.readLine()) != null && (next2 = fileStream2.readLine()) != null) {
            lineNumber1++;
            lineNumber2++;

            // where the (empty) entryexitpairs line ends up seems to be non-deterministic
            // so if we see it in either file we just skip it
            String entryexitpairs = "<entryexitpairs class=\"jmri.jmrit.signalling.configurexml.EntryExitPairsXml\" />";
            if (line1.contains(entryexitpairs)) {
                line1 = next1;
                if ((next1 = fileStream1.readLine()) == null) {
                    break;
                }
                lineNumber1++;
            }
            if (line2.contains(entryexitpairs)) {
                line2 = next2;
                if ((next2 = fileStream2.readLine()) == null) {
                    break;
                }
                lineNumber2++;
            }

            // if we get to the file history...
            String filehistory = "filehistory";
            if (line1.contains(filehistory) && line2.contains(filehistory)) {
                break;  // we're done!
            }

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
                    }
                }
            }
            String date_string = "<date>";
            if (!match && line1.contains(date_string) && line2.contains(date_string)) {
                match = true;
            }

            if (!match) {
                // if either line contains a fontname attribute
                String fontname_regexe = "( fontname=\"[^\"]*\")";
                String[] splits1 = line1.split(fontname_regexe);
                if (splits1.length == 2) {  // (yes) remove it
                    line1 = splits1[0] + splits1[1];
                }
                String[] splits2 = line2.split(fontname_regexe);
                if (splits2.length == 2) {  // (yes) remove it
                    line2 = splits2[0] + splits2[1];
                }
            }
            if (!match && !line1.equals(line2)) {
                log.error("match failed in LoadAndStoreTest:");
                log.error("    file1:line {}: \"{}\"", lineNumber1, line1);
                log.error("    file2:line {}: \"{}\"", lineNumber2, line2);
                log.error("  comparing file1:\"" + inFile1.getPath() + "\"");
                log.error("         to file2:\"" + inFile2.getPath() + "\"");
                Assert.assertEquals(line1, line2);
            }
            line1 = next1;
            line2 = next2;
        }   // while readLine() != null

        fileStream1.close();
        fileStream2.close();
    }

    // load file
    public static void loadFile(File inFile) throws Exception {
        ConfigureManager cm = InstanceManager.getDefault(ConfigureManager.class);
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        boolean good = cm.load(inFile);
        Assert.assertTrue("loadFile(\"" + inFile.getPath() + "\")", good);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
    }

    // store file
    public static File storeFile(File inFile, SaveType inSaveType) throws Exception {
        String name = inFile.getName();
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File outFile = new File(FileUtil.getUserFilesPath() + "temp/" + name);

        ConfigureManager cm = InstanceManager.getDefault(ConfigureManager.class);
        switch (inSaveType) {
            case All: {
                cm.storeAll(outFile);
                break;
            }
            case Config: {
                cm.storeConfig(outFile);
                break;
            }
            case Prefs: {
                cm.storePrefs(outFile);
                break;
            }
            case User: {
                cm.storeUser(outFile);
                break;
            }
            case UserPrefs: {
                cm.storeUserPrefs(outFile);
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
        if (guiOnly) {
            Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        }

        log.debug("Start check file {}", this.file.getCanonicalPath());

        loadFile(this.file);
        // Panel sub-classes (with GUI) will fail if you try to load them twice.
        // (So don't!)
        if (!guiOnly) {
            loadFile(this.file);
        }

        // find comparison files
        File compFile = new File(this.file.getCanonicalFile().getParentFile().
                getParent() + "/loadref/" + this.file.getName());
        if (!compFile.exists()) {
            compFile = this.file;
        }
        log.debug("   Chose comparison file {}", compFile.getCanonicalPath());

        File outFile = storeFile(this.file, this.saveType);
        checkFile(compFile, outFile);
        
        JUnitAppender.suppressErrorMessage("systemName is already registered: ");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearBlockBossLogic();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LoadAndStoreTest.class);

}
