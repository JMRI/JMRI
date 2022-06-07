package jmri.configurexml;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import jmri.configurexml.ShutdownPreferences;

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
        if (_preferences.isStoreCheckEnabled()) {
            if (dataHasChanged() && !GraphicsEnvironment.isHeadless()) {
                jmri.configurexml.swing.StoreAndCompareDialog.showDialog();
            }
        }
    }

    public static boolean dataHasChanged() {
        var result = false;

        // Get file 1 :: This will be the file used to load the layout data.
        JFileChooser chooser = LoadStoreBaseAction.getUserFileChooser();
        File file1 = chooser.getSelectedFile();
        if (file1 == null) {
            return true;    // No file loaded, request store by default.
        }

        // Get file 2 :: This is the default tmp directory with a random xml file name.
        var tempDir = System.getProperty("java.io.tmpdir");
        var fileName = UUID.randomUUID().toString();
        File file2 = new File(tempDir + fileName + ".xml");

        log.info("File 2 = {}", file2);

        // Store the current data using the temp file.
        jmri.ConfigureManager cm = jmri.InstanceManager.getDefault(jmri.ConfigureManager.class);
        boolean stored = cm.storeUser(file2);
        log.info("stored = {}", stored);

        try {
            result = checkFile(file1, file2);
            log.info("result = {}", result);
        } catch (Exception ex) {
            log.info("exception: ", ex);
        }

        if (!file2.delete() {
            log.warn("An error occurred while deleting temporary file {}", file2.getPath());
        }

        return result;
    }

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
                // if either line contains a fontname attribute
                String fontname_regexe = "( fontname=\"[^\"]*\")";
                line1 = filterLineUsingRegEx(line1, fontname_regexe);
                line2 = filterLineUsingRegEx(line2, fontname_regexe);
            }

            if (!match && !line1.equals(line2)) {
                log.error("match failed in StoreAndCompare:");
                log.error("    file1:line {}: \"{}\"", lineNumber1, line1);
                log.error("    file2:line {}: \"{}\"", lineNumber2, line2);
                log.error("  comparing file1:\"{}\"", inFile1.getPath());
                log.error("         to file2:\"{}\"", inFile2.getPath());
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndCompare.class);
}



