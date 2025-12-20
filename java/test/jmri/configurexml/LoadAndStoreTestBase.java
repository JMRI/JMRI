package jmri.configurexml;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.stream.Stream;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Base for testing load-and-store of configuration files.
 * <p>
 * Creating a parameterized test class that extends this class will test each
 * file in a "load" directory by loading it, then storing it, then comparing
 * (with certain lines skipped) against either a file by the same name in the
 * "loadref" directory, or against the original file itself. A minimal test
 * class is:
 <pre>
   public class LoadAndStoreTest extends LoadAndStoreTestBase {

     public static Stream&amp;Arguments&amp; data() {
       return getFiles(new File("java/test/jmri/configurexml"), false, true);
     }

     @ParameterizedTest
     @MethodSource("data")
     public void loadAndStoreTest(File file, boolean pass) { super.validate(file, pass); }
   }
</pre>
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 2.5.5 (renamed & reworked in 3.9 series)
 */
public class LoadAndStoreTestBase {

    public enum SaveType {
        All, Config, Prefs, User, UserPrefs
    }

    private SaveType saveType = SaveType.Config;
    private boolean guiOnly = false;

    /**
     * Get all XML files in a directory and validate the ability to load and
     * store them.
     *
     * @param saveType the type (i.e. level) of ConfigureXml information being
     *                 saved
     * @param isGUI    true for files containing GUI elements, i.e. panels.
     *                 These can only be loaded once (others can be loaded
     *                 twice, and that's tested when this is false), and can't
     *                 be loaded when running headless.
     */
    public LoadAndStoreTestBase(SaveType saveType, boolean isGUI) {
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
     * @return a stream of {@link Arguments}, where each Argument contains the
     *         {@link java.io.File} to validate and a boolean matching the pass
     *         parameter
     */
    public static Stream<Arguments> getFiles(File directory, boolean recurse, boolean pass) {
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
    public static Stream<Arguments> getDirectories(File directory, boolean recurse, boolean pass) throws IllegalArgumentException {
        // since this method gets the files to test, but does not trigger any
        // tests itself, we can use SchemaTestBase.getDirectories() by adding "load"
        // to the directory to test
        return SchemaTestBase.getDirectories(new File(directory, "load"), recurse, pass);
    }

    public static void checkFile(File inFile1, File inFile2) throws Exception {

        try ( // compare files, except for certain special lines
            BufferedReader fileStream1 = new BufferedReader( new InputStreamReader(new FileInputStream(inFile1)));
            BufferedReader fileStream2 = new BufferedReader( new InputStreamReader(new FileInputStream(inFile2)));
        ) {
            
            String line1 = fileStream1.readLine();
            String line2 = fileStream2.readLine();
            int lineNumber1 = 0, lineNumber2 = 0;
            String next1, next2;
            while ((next1 = fileStream1.readLine()) != null && (next2 = fileStream2.readLine()) != null) {
                lineNumber1++;
                lineNumber2++;
                
                // Do we have a multi line comment? Comments in the xml file is used by LogixNG.
                // This only happens in the first file since store() will not store comments
                if  (next1.startsWith("<!--")) {
                    while ((next1 = fileStream1.readLine()) != null && !next1.endsWith("-->")) {
                        lineNumber1++;
                    }
                    
                    // If here, we either have a line that ends with --> or we have reached end of file
                    String nullCheck = fileStream1.readLine();
                    if (nullCheck == null) {
                        break;
                    }
                    
                    // If here, we have a line that ends with --> or we have reached end of file
                    continue;
                }
                
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
                    "  <timebase",      // time changes from timezone to timezone
                    "    <test>",       // version changes over time
                    "    <modifier",    // version changes over time
                    "    <major",       // version changes over time
                    "    <minor",       // version changes over time
                    "<layout-config",   // Linux seems to put attributes in different order
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
                
                // Screen size will vary when written out
                if (!match) {
                    if (line1.contains("  <LayoutEditor")) {
                        // if either line contains a windowheight attribute
                        String windowheight_regexe = "( windowheight=\"[^\"]*\")";
                        line1 = filterLineUsingRegEx(line1, windowheight_regexe);
                        line2 = filterLineUsingRegEx(line2, windowheight_regexe);
                        // if either line contains a windowheight attribute
                        String windowwidth_regexe = "( windowwidth=\"[^\"]*\")";
                        line1 = filterLineUsingRegEx(line1, windowwidth_regexe);
                        line2 = filterLineUsingRegEx(line2, windowwidth_regexe);
                    }
                }
                
                // window positions will sometimes differ based on window decorations.
                if (!match) {
                    if (line1.contains("  <LayoutEditor") ||
                            line1.contains(" <switchboardeditor")) {
                        // if either line contains a y position attribute
                        String yposition_regexe = "( y=\"[^\"]*\")";
                        line1 = filterLineUsingRegEx(line1, yposition_regexe);
                        line2 = filterLineUsingRegEx(line2, yposition_regexe);
                        // if either line contains an x position attribute
                        String xposition_regexe = "( x=\"[^\"]*\")";
                        line1 = filterLineUsingRegEx(line1, xposition_regexe);
                        line2 = filterLineUsingRegEx(line2, xposition_regexe);
                    }
                }
                
                // Time will vary when written out
                if (!match) {
                    String memory_value = "<memory value";
                    if (line1.contains(memory_value) && line2.contains(memory_value)) {
                        String imcurrenttime = "<systemName>IMCURRENTTIME</systemName>";
                        if (next1.contains(imcurrenttime) && next2.contains(imcurrenttime)) {
                            match = true;
                        }
                    }
                }
                
                // Dates can vary when written out
                String date_string = "<date>";
                if (!match && line1.contains(date_string) && line2.contains(date_string)) {
                    match = true;
                }
                
                if (!match) {
                    // remove fontname and fontFamily attributes
                    String fontname_regexe = "( fontname=\"[^\"]*\")";
                    line1 = filterLineUsingRegEx(line1, fontname_regexe);
                    line2 = filterLineUsingRegEx(line2, fontname_regexe);
                    String fontFamily_regexe = "( fontFamily=\"[^\"]*\")";
                    line1 = filterLineUsingRegEx(line1, fontFamily_regexe);
                    line2 = filterLineUsingRegEx(line2, fontFamily_regexe);
                }
                
                if (!match && !line1.equals(line2)) {
                    log.error("match failed in LoadAndStoreTest:");
                    log.error("    file1:line {}: \"{}\"", lineNumber1, line1);
                    log.error("    file2:line {}: \"{}\"", lineNumber2, line2);
                    log.error("  comparing file1:\"{}\"", inFile1.getPath());
                    log.error("         to file2:\"{}\"", inFile2.getPath());
                    Assert.assertEquals(line1, line2);
                }
                line1 = next1;
                line2 = next2;
            }   // while readLine() != null
        }
    }

    private static String filterLineUsingRegEx(String line, String regexe) {
        String[] splits = line.split(regexe);
        if (splits.length == 2) {  // (yes) remove it
            line = splits[0] + splits[1];
        }
        return line;
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
                Assert.fail("Unknown save type "+inSaveType);
                break;
            }
        }

        return outFile;
    }

    public void loadLoadStoreFileCheck(File file) throws Exception {
        if (guiOnly) {
            Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        }

        log.debug("Start check file {}", file.getCanonicalPath());

        loadFile(file);
        // Panel sub-classes (with GUI) will fail if you try to load them twice.
        // (So don't!)
        if (!guiOnly) {
            loadFile(file);
        }

        // to ease comparison, dump the history information;
        // if you need to turn that off you can override
        dumpHistory();

        // find comparison files
        File tmpFile = file.getCanonicalFile().getParentFile();
        if ( tmpFile == null ) {
            log.warn("null file to check {}", file);
            return;
        }
        File compFile = new File( tmpFile.getParent() + "/loadref/" + file.getName() );
        if (!compFile.exists()) {
            compFile = file;
        }
        log.debug("   Chose comparison file {}", compFile.getCanonicalPath());

        postLoadProcessing();

        File outFile = storeFile(file, this.saveType);
        checkFile(compFile, outFile);

        JUnitAppender.suppressErrorMessageStartsWith("systemName is already registered: ");

    }

    /**
     * By default, drop the history information
     * to simplify diffing the files.
     * Override if that info is needed for a test.
     */
    protected void dumpHistory(){
        jmri.InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory.class).purge(0);
    }

    /**
     * If anything, i.e. typically a delay,
     * is needed after loading the file before storing or doing
     * any final tests,
     * it can be added by override here.
     */
    protected void postLoadProcessing() {
        // by default do nothing
    }

    @BeforeEach
    public void setUp(@TempDir java.io.File tempDir) throws IOException  {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.clearBlockBossLogic();
        System.setProperty("jmri.test.no-dialogs", "true");

        // kill the fast clock and set to a consistent time
        jmri.Timebase clock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        clock.setRun(false);

        try {
            clock.setTime(
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse("2021-12-02 00:00:00.0")
            );
        } catch (Exception e) {
            log.warn("Unexpected Exception in test setup", e);
        }

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.closeAllPanels();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.clearBlockBossLogic();
        JUnitUtil.tearDown();
        System.setProperty("jmri.test.no-dialogs", "false");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadAndStoreTestBase.class);

}
