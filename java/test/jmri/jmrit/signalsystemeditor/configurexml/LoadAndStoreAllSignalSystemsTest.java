package jmri.jmrit.signalsystemeditor.configurexml;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.jmrit.signalsystemeditor.SignalSystem;
import jmri.jmrit.signalsystemeditor.SignalMastType;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Loads and stores all the signal systems and verifies that the stored data
 * is equal to the loaded data.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LoadAndStoreAllSignalSystemsTest {

    private static String lastSignalSystem = null;

    /**
     * Get all XML files in a directory and validate them.
     *
     * @param directory the directory containing XML files
     * @param recurse   if true, will recurse into subdirectories
     * @param pass      if true, successful validation will pass; if false,
     *                  successful validation will fail
     * @return a stream of {@link Arguments}, where each Argument contains the
     *         {@link java.io.File} with a filename ending in {@literal .xml} to
     *         validate and a boolean matching the pass parameter
     */
    public static Stream<Arguments> getFiles(File directory, boolean recurse, boolean pass) {
        ArrayList<Arguments> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    if (recurse) {
                        files.addAll(getFiles(file, recurse, pass).collect(Collectors.toList()));
                    }
                } else {
                    files.addAll(getFiles(file, recurse, pass).collect(Collectors.toList()));
                }
            }
        } else if (directory.getName().endsWith(".xml") || directory.getName().endsWith(".html")) {
            files.add(Arguments.of(directory, pass));
        }
        return files.stream();
    }

    public static Stream<Arguments> data() {
        return getFiles(new File("xml/signals"), true, true);
    }

    public static void checkFile(File inFile1, File inFile2) throws Exception {

        try ( // compare files, except for certain special lines
            BufferedReader fileStream1 = new BufferedReader( new InputStreamReader(
                    new FileInputStream(inFile1), java.nio.charset.StandardCharsets.UTF_8));
            BufferedReader fileStream2 = new BufferedReader( new InputStreamReader(
                    new FileInputStream(inFile2), java.nio.charset.StandardCharsets.UTF_8));
        ) {

            String line1 = fileStream1.readLine();
            String line2 = fileStream2.readLine();
            int lineNumber1 = 0, lineNumber2 = 0;
            String next1 = null;
            String next2 = null;

            // Remove BOM (Byte Order Mark)
            // https://en.wikipedia.org/wiki/Byte_order_mark
            if (line2.codePointAt(0) == 65279) {
                line2 = line2.substring(1);
            }
            line2 = line2.replaceAll(" encoding=\"utf-8\"", " encoding=\"UTF-8\"");

            while ((next1 = fileStream1.readLine()) != null && (next2 = fileStream2.readLine()) != null) {
                lineNumber1++;
                lineNumber2++;

                while ((next1.isBlank()) && (next1 = fileStream2.readLine()) != null) {
                    lineNumber1++;
                }

                next2 = next2.replace("<!-- Start of Specific Appearances list -->", "");
                next2 = next2.replace("<!-- End of Specific Appearances list -->", "");
                next2 = next2.replace("<!-- Start of Aspect Mapping -->", "");
                next2 = next2.replace("<!-- End of Aspect Mapping -->", "");
                next2 = next2.replace("<!-- Start of Advanced Aspect Mapping -->", "");
                next2 = next2.replace("<!-- Start of  Advanced Aspect Mapping -->", "");
                next2 = next2.replace("<!-- Start of Advanced Aspect mapping -->", "");
                next2 = next2.replace("<!-- Start of Advanced-Aspect mapping -->", "");
                next2 = next2.replace("<!-- End of  Advanced Aspect Mapping -->", "");
                next2 = next2.replace("<!-- NOTE 1:  advancedAspect here means the signal ahead of \"our\", and aspect is same or more restrictive -->", "");
                next2 = next2.replace("<!-- NOTE 2:  Refer to related aspects.xml to consider and apply all possible aspects ahead to these \"our\" aspects -->", "");
                next2 = next2.replace("<!-- The following references the \"Restricted Proceed\" aspect, which is undefined here-->", "");
                next2 = next2.replace("<email></email>", "");

                while (next2 != null && next2.isBlank()) {
                    next2 = fileStream2.readLine();
                    next2 = next2.replace("<!-- Start of Specific Appearances list -->", "");
                    next2 = next2.replace("<!-- End of Specific Appearances list -->", "");
                    next2 = next2.replace("<!-- Start of Aspect Mapping -->", "");
                    next2 = next2.replace("<!-- End of Aspect Mapping -->", "");
                    next2 = next2.replace("<!-- Start of Advanced Aspect Mapping -->", "");
                    next2 = next2.replace("<!-- Start of  Advanced Aspect Mapping -->", "");
                    next2 = next2.replace("<!-- End of  Advanced Aspect Mapping -->", "");
                    next2 = next2.replace("<!-- NOTE 1:  advancedAspect here means the signal ahead of \"our\", and aspect is same or more restrictive -->", "");
                    next2 = next2.replace("<!-- NOTE 2:  Refer to related aspects.xml to consider and apply all possible aspects ahead to these \"our\" aspects -->", "");
                    next2 = next2.replace("<!-- The following references the \"Restricted Proceed\" aspect, which is undefined here-->", "");
                    next2 = next2.replace("<email></email>", "");
                    lineNumber2++;
                }

                if (next1 == null || next2 == null) break;

                next1 = next1.strip();
                next2 = next2.strip();

                if (next1.startsWith("<aspecttable ") && next1.startsWith(next2) && !next1.equals(next2)) {
                    while (next1.startsWith(next2) && !next1.equals(next2)) {
                        next2 += " " + fileStream2.readLine().strip();
                        // Remove space before and after = sign
                        next2 = next2.replaceAll("\\s*=\\s*", "=");
                        lineNumber2++;
                    }
                }

                if (next1.startsWith("<appearancetable ") && next1.startsWith(next2) && !next1.equals(next2)) {
                    while (next1.startsWith(next2) && !next1.equals(next2)) {
                        next2 += " " + fileStream2.readLine().strip();
                        // Remove space before and after = sign
                        next2 = next2.replaceAll("\\s*=\\s*", "=");
                        lineNumber2++;
                    }
                }


                while (next1.startsWith("<reference>")
                        && next2.startsWith("<reference>")
                        && (next1.startsWith(next2) || next2.startsWith(next1))
                        && (!next1.endsWith("</reference>") || !next2.endsWith("</reference>"))) {

                    if (next1.startsWith(next2)) {
                        // \u00A0 is non breaking space
                        next2 += fileStream2.readLine().strip().replaceAll("\u00A0", " ");
                        lineNumber2++;
                    } else {    // next2.startsWith(next1)
                        // \u00A0 is non breaking space
                        next1 += fileStream1.readLine().strip().replaceAll("\u00A0", " ");
                        lineNumber1++;
                    }
                }

                while (next1.startsWith("<description>")
                        && next2.startsWith("<description>")
                        && (next1.startsWith(next2) || next2.startsWith(next1))
                        && (!next1.endsWith("</description>") || !next2.endsWith("</description>"))) {

                    if (next1.startsWith(next2)) {
                        // \u00A0 is non breaking space
                        next2 += fileStream2.readLine().strip().replaceAll("\u00A0", " ");
                        lineNumber2++;
                    } else {    // next2.startsWith(next1)
                        // \u00A0 is non breaking space
                        next1 += fileStream1.readLine().strip().replaceAll("\u00A0", " ");
                        lineNumber1++;
                    }
                }

/*
                if (next2.startsWith("<reference>") && next2.startsWith(next1) && !next2.equals(next1)) {
                    while (next2.startsWith(next1) && !next2.equals(next1)) {
                        // \u00A0 is non breaking space
                        next1 += fileStream1.readLine().strip().replaceAll("\u00A0", " ");
                        lineNumber1++;
                    }
                }

                if (next2.startsWith("<description>") && next2.startsWith(next1) && !next2.equals(next1)) {
                    while (next2.startsWith(next1) && !next2.equals(next1)) {
                        // \u00A0 is non breaking space
                        next1 += fileStream1.readLine().strip().replaceAll("\u00A0", " ");
                        lineNumber1++;
                    }
                }
*/
/*
                if (next2.startsWith("<") && !next2.strip().endsWith(">")) {
                    while (!next2.strip().endsWith(">")) {
                        if (!next2.startsWith("<reference>")
                                && !next2.startsWith("<revremark>")
                                && !next2.startsWith("<indication>")
                                && !next2.startsWith("<description>")
                                && !next2.startsWith("<reference>")) {
                            next2 += " ";
                        }
                        next2 += fileStream2.readLine().strip();
                        lineNumber2++;
                    }
                }
*/
//                while (next2.strip().equals("<email></email>")) {
//                    next2 = fileStream2.readLine().strip();
//                    lineNumber2++;
//                }

                // Remove xmlns=""
                next1 = next1.replaceAll(" xmlns=\"\"", "");

                // Remove space before and after = sign
                next2 = next2.replaceAll("\\s*=\\s*", "=");
                // Remove space between " and >
                next2 = next2.replaceAll("\"\\s+\\>", "\">");
/*
                if (next2.strip().equals("<!-- Start of Specific Appearances list -->")
                        || next2.strip().equals("<!-- End of Specific Appearances list -->")
                        || next2.strip().equals("<!-- Start of Aspect Mapping -->")
                        || next2.strip().equals("<!-- End of Aspect Mapping -->")) {
                    next2 = fileStream2.readLine().strip();
                    lineNumber2++;
                }
*/
                while (!next2.equals(next1) && next2.startsWith(next1)) {
                    next1 += fileStream1.readLine().strip();
                    // Remove xmlns=""
                    next1 = next1.replaceAll(" xmlns=\"\"", "");
                    lineNumber1++;
                }

                if (next2.endsWith("\"/>")) {
                    next2 = next2.substring(0, next2.length() - "\"/>".length()) + "\" />";
                }

                boolean match = false;  // assume failure (pessimist!)

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
/*
            if (next1 != null) {
                while ((next1 = fileStream1.readLine()) != null) {
                    lineNumber1++;
                    if (!next1.isBlank()) {
                        log.warn("The file "+inFile1.getPath()+" has extra content: {}", next1.strip());
                    }
                }
            }

            if (next2 != null) {
                while ((next2 = fileStream2.readLine()) != null) {
                    lineNumber2++;
                    if (!next2.isBlank()) {
                        log.warn("The file "+inFile2.getPath()+" has extra content: {}", next2.strip());
                    }
                }
            }
*/
        } catch (java.io.FileNotFoundException ex) {
            // Ignore for now. Fix later
//            log.warn("File not found: {}", ex.getMessage());
        }
    }

    public void loadAndStoreFileCheck(File file) throws Exception {

//        if (!file.getAbsolutePath().equals("F:\\Projekt\\Java\\GitHub\\JMRI\\xml\\signals\\DB-HV-1969\\appearance-block.xml")) return;

        log.debug("Start check file {}", file.getCanonicalPath());

        File signalSystemFolder = file.getCanonicalFile().getParentFile();
        String signalSystemName = signalSystemFolder.getName();

        if (!signalSystemName.equals(lastSignalSystem)) {

            lastSignalSystem = signalSystemName;

            SignalSystemXml signalSystemXml = new SignalSystemXml();
            SignalMastTypeXml signalMastXml = new SignalMastTypeXml();

            SignalSystem signalSystem = signalSystemXml.load(new File(file.getParent()+"/aspects.xml"));

            signalSystemXml.save(signalSystem);

            for (SignalMastType signalMastType : signalSystem.getSignalMastTypes()) {
                signalMastXml.save(signalSystem, signalMastType);
            }
        }

        File compFile = new File( jmri.util.FileUtil.getProfilePath()
                + "xml/signals/" + "/" + file.getParentFile().getName() + "/" + file.getName() );
        checkFile(compFile, file);
    }

    @ParameterizedTest(name = "{index}: {0} (pass={1})")
    @MethodSource("data")
    public void loadAndStoreTest(File file, boolean pass) throws Exception {
        if (!file.getParent().equals("xml/signals") && !file.getParent().equals("xml\\signals")) {
            loadAndStoreFileCheck(file);
        }
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException  {
        tempDir = new File("temp/temp/SignalSystemEditor");
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadAndStoreAllSignalSystemsTest.class);
}
