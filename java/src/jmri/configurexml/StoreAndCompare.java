package jmri.configurexml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import jmri.*;

/**
 * Determine if there have been changes made to the PanelPro data.  If so, then a prompt will
 * be displayed to store the data before the JMRI shutdown process proceeds.
 * <p>
 * If the JMRI application is DecoderPro, the checking does not occur.  If the PanelPro tables
 * contain only 3 time related beans and no panels, the checking does not occur.
 * <p>
 * The main check process uses the checkFile process which is used by the load and store tests.
 * The current configuration is stored to a temporary file. This temp file is compared to the file
 * that was loaded manually or via a start up action.  If there are differences and the
 * shutdown store check preference is enabled, a store request prompt is displayed.  The
 * prompt does not occur when running in headless mode.
 *
 * @author Dave Sand Copyright (c) 2022
 */
public class StoreAndCompare extends AbstractAction {

    public StoreAndCompare() {
        this("Store and Compare");  // NOI18N
    }

    public StoreAndCompare(String s) {
        super(s);
    }

    private static ShutdownPreferences _preferences = jmri.InstanceManager.getDefault(ShutdownPreferences.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        requestStoreIfNeeded();
    }

    public static void requestStoreIfNeeded() {
        if (Application.getApplicationName().equals("PanelPro")) {
            if (_preferences.isStoreCheckEnabled()) {
                if (dataHasChanged() && !GraphicsEnvironment.isHeadless()) {
                    jmri.configurexml.swing.StoreAndCompareDialog.showDialog();
                }
            }
        }
    }

    public static boolean dataHasChanged() {
        var result = false;

        // Get file 1 :: This will be the file used to load the layout data.
        JFileChooser chooser = LoadStoreBaseAction.getUserFileChooser();
        File file1 = chooser.getSelectedFile();
        if (file1 == null) {
            // No file loaded, check for possible additions.
            return noFileChecks();
        }

        // Get file 2 :: This is the default tmp directory with a random xml file name.
        var tempDir = System.getProperty("java.io.tmpdir") + File.separator;
        var fileName = UUID.randomUUID().toString();
        File file2 = new File(tempDir + fileName + ".xml");

        // Store the current data using the temp file.
        jmri.ConfigureManager cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            boolean stored = cm.storeUser(file2);
            log.debug("temp file '{}' stored :: {}", file2, stored);

            try {
                result = checkFile(file1, file2);
            } catch (Exception ex) {
                log.debug("checkFile exception: ", ex);
            }

            if (!file2.delete()) {
                log.warn("An error occurred while deleting temporary file {}", file2.getPath());
            }
        }

        return result;
    }

    /**
     * When a file has not been loaded, there might be items that should be stored.  This check
     * is not exhaustive.
     * <p>
     * If ISCLOCKRUNNING is the only sensor, that is not considered a change.  This also applies
     * to the IMCURRENTTIME and IMRATEFACTOR memories.
     * @return true if notification should occur.
     */
    @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"},
            justification =
                    "spotbugs did not like the protection provided by the result boolean, but the second test was declared redundant")
    private static boolean noFileChecks() {
        var result = false;

        var tMgr = InstanceManager.getDefault(TurnoutManager.class);
        var sMgr = InstanceManager.getDefault(SensorManager.class);
        var mMgr = InstanceManager.getDefault(MemoryManager.class);

        // Get the system prefix for internal beans using the memory manager to avoid the default prefix.
        var systemPrefix = "I";
        if (mMgr != null) {
            systemPrefix = mMgr.getSystemPrefix();
        }

        if (tMgr == null || sMgr == null || mMgr == null) {
            log.debug("triple manager test sets true");
            result = true;
        }

        if (!result && tMgr != null && tMgr.getNamedBeanSet().size() > 0) {
            log.debug("turnout manager test sets true");
            result = true;
        }

        if (!result && sMgr != null) {
            var sensorSize = sMgr.getNamedBeanSet().size();
            if (sensorSize > 1) {
                log.debug("sensor > 1 sets true");
                result = true;
            } else if (sensorSize == 1) {
                if (sMgr.getBySystemName(systemPrefix + "SCLOCKRUNNING") == null) {
                    log.debug("sensor == 1 sets true");
                    result = true;  // One sensor but it is not ISCLOCKRUNNING
                }
            }
        }

        if (!result && mMgr != null) {
            var memSize = mMgr.getNamedBeanSet().size();
            if (memSize > 2) {
                log.debug("memory > 2 sets true");
                result = true;
            } else if (memSize != 0) {
                if (mMgr.getBySystemName(systemPrefix + "MCURRENTTIME") == null) {
                    log.debug("memory no MCURRENTTIME sets true");
                    result = true;  // Two memories but one is not IMCURRENTTIME
                }
                if (mMgr.getBySystemName(systemPrefix + "MRATEFACTOR") == null) {
                    log.debug("memory no MRATEFACTOR sets true");
                    result = true;  // Two memories but one is not IMRATEFACTOR
                }
            }
        }

        if (!result) {
            if (InstanceManager.getDefault(jmri.jmrit.display.EditorManager.class).getList().size() > 0) {
                log.debug("panel check sets true");
                result = true;  // One or more panels have been added.
            }
        }

        return result;
    }

    @SuppressFBWarnings(value = {"OS_OPEN_STREAM_EXCEPTION_PATH", "RV_DONT_JUST_NULL_CHECK_READLINE"},
            justification =
            "Open streams are not a problem during JMRI shutdown."
            + "The line represents the end of a XML comment and is not relevant")
    public static boolean checkFile(File inFile1, File inFile2) throws Exception {
        boolean result = false;
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

            // Do we have a multi line comment? Comments in the xml file is used by LogixNG.
            // This only happens in the first file since store() will not store comments
            if  (next1.startsWith("<!--")) {
                while ((next1 = fileStream1.readLine()) != null && !next1.endsWith("-->")) {
                    lineNumber1++;
                }

                // If here, we either have a line that ends with --> or we have reached endf of file
                if (fileStream1.readLine() == null) break;

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

            // Memory variables have a value attribute for non-null values or no attribute.
            if (!match) {
                var mem1 = line1.startsWith("    <memory value") || line1.startsWith("    <memory>");
                var mem2 = line2.startsWith("    <memory value") || line2.startsWith("    <memory>");
                if (mem1 && mem2) {
                    match = true;
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

            // Dates can vary when written out
            String date_string = "<date>";
            if (!match && line1.contains(date_string) && line2.contains(date_string)) {
                match = true;
            }

            if (!match) {
                // remove fontname and fontFamily attributes from comparison
                String fontname_regexe = "( fontname=\"[^\"]*\")";
                line1 = filterLineUsingRegEx(line1, fontname_regexe);
                line2 = filterLineUsingRegEx(line2, fontname_regexe);
                String fontFamily_regexe = "( fontFamily=\"[^\"]*\")";
                line1 = filterLineUsingRegEx(line1, fontFamily_regexe);
                line2 = filterLineUsingRegEx(line2, fontFamily_regexe);
            }

            // Check if timebase is ignored
            if (!match && line1.startsWith("  <timebase") && line2.startsWith("  <timebase")) {
                if (_preferences.isIgnoreTimebaseEnabled()) {
                    match = true;
                }
            }

            // Check if sensor icon label colors are ignored
            if (!match
                    && line1.startsWith("    <sensoricon") && line2.startsWith("    <sensoricon")
                    && line1.contains("icon=\"no\"") && line2.contains("icon=\"no\"")
                    && _preferences.isIgnoreSensorColorsEnabled()) {
                line1 = removeSensorColors(line1);
                line2 = removeSensorColors(line2);
            }

            if (!match && !line1.equals(line2)) {
                log.warn("Match failed in StoreAndCompare:");
                log.warn("    file1:line {}: \"{}\"", lineNumber1, line1);
                log.warn("    file2:line {}: \"{}\"", lineNumber2, line2);
                log.warn("  comparing file1:\"{}\"", inFile1.getPath());
                log.warn("         to file2:\"{}\"", inFile2.getPath());
                result = true;
                break;
            }
            line1 = next1;
            line2 = next2;
        }   // while readLine() != null

        fileStream1.close();
        fileStream2.close();

        return result;
    }

    private static String filterLineUsingRegEx(String line, String regexe) {
        String[] splits = line.split(regexe);
        if (splits.length == 2) {  // (yes) remove it
            line = splits[0] + splits[1];
        }
        return line;
    }

    private static String removeSensorColors(String line) {
        var leftSide = line.substring(0, line.indexOf(" red="));

        // Find the next non color attribute.  "justification" is always present."
        var index = 0;
        if (line.indexOf("margin=") != -1) {
            index = line.indexOf("margin=");
        } else if (line.indexOf("borderSize=") != -1) {
            index = line.indexOf("borderSize=");
        } else if (line.indexOf("redBorder=") != -1) {
            index = line.indexOf("redBorder=");
        } else if (line.indexOf("greenBorder=") != -1) {
            index = line.indexOf("greenBorder=");
        } else if (line.indexOf("blueBorder=") != -1) {
            index = line.indexOf("blueBorder=");
        } else if (line.indexOf("fixedWidth=") != -1) {
            index = line.indexOf("fixedWidth=");
        } else if (line.indexOf("fixedHeight=") != -1) {
            index = line.indexOf("fixedHeight=");
        } else {
            index = line.indexOf("justification=");
        }

        var rightSide = line.substring(index - 1, line.length());
        return leftSide + rightSide;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndCompare.class);
}
