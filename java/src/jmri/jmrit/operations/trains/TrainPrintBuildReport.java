package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Used for train build reports.
 *
 * @author Daniel Boudreau (C) 2025
 */
public class TrainPrintBuildReport extends TrainCommon {

    /**
     * Print or preview a build report.
     *
     * @param file      File to be printed or previewed
     * @param name      Title of document
     * @param isPreview true if preview
     */
    public static void printReport(File file, String name, boolean isPreview) {
        // obtain a HardcopyWriter to do this

        String printerName = "";
        int fontSize = Setup.getBuildReportFontSize();
        boolean isLandScape = false;
        double margin = .5;
        Dimension pagesize = null; // HardcopyWritter provides default page
                                   // sizes for portrait and landscape

        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), name, fontSize, margin,
                margin, .5, .5, isPreview, printerName, isLandScape, true, null, pagesize);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8));) {

            String line;
            while (true) {
                try {
                    line = in.readLine();
                } catch (IOException e) {
                    log.debug("Print read failed");
                    break;
                }
                if (line == null) {
                    if (isPreview) {
                        // need to do this in case the input file was empty to create preview
                        writer.write(" ");
                    }
                    break;
                }
                // check for build report print level
                line = filterBuildReport(line, false); // no indent
                if (line.isEmpty()) {
                    continue;
                }
                writer.write(line + NEW_LINE);
            }
        } catch (FileNotFoundException e) {
            log.error("Build file doesn't exist", e);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException e) {
            log.warn("Exception printing: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Creates a new build report file with the print detail numbers replaced by
     * indentations. Then calls open desktop editor.
     *
     * @param file build file
     * @param name train name
     */
    public static void editReport(File file, String name) {
        // make a new file with the build report levels removed
        File buildReport = InstanceManager.getDefault(TrainManagerXml.class)
                .createTrainBuildReportFile(Bundle.getMessage("Report") + " " + name);
        editReport(file, buildReport);
        // open the file
        TrainUtilities.openDesktop(buildReport);
    }

    /**
     * Creates a new build report file with the print detail numbers replaced by
     * indentations.
     * 
     * @param file    Raw file with detail level numbers
     * @param fileOut Formated file with indentations
     */
    public static void editReport(File file, File fileOut) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(fileOut), StandardCharsets.UTF_8)), true);) {

            String line;
            while (true) {
                try {
                    line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    line = filterBuildReport(line, Setup.isBuildReportIndentEnabled());
                    if (line.isEmpty()) {
                        continue;
                    }
                    out.println(line); // indent lines for each level
                } catch (IOException e) {
                    log.debug("Print read failed");
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Build file doesn't exist: {}", e.getLocalizedMessage());
        } catch (IOException e) {
            log.error("Can not create build report file: {}", e.getLocalizedMessage());
        }
    }

    /*
     * Removes the print levels from the build report
     */
    private static String filterBuildReport(String line, boolean indent) {
        String[] inputLine = line.split("\\s+"); // NOI18N
        if (inputLine.length == 0) {
            return "";
        }
        if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + BUILD_REPORT_CHAR) ||
                inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + BUILD_REPORT_CHAR) ||
                inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + BUILD_REPORT_CHAR) ||
                inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + BUILD_REPORT_CHAR)) {

            if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL)) {
                if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + BUILD_REPORT_CHAR) ||
                        inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + BUILD_REPORT_CHAR) ||
                        inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + BUILD_REPORT_CHAR)) {
                    return ""; // don't print this line
                }
            }
            if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)) {
                if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + BUILD_REPORT_CHAR) ||
                        inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + BUILD_REPORT_CHAR)) {
                    return ""; // don't print this line
                }
            }
            if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED)) {
                if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + BUILD_REPORT_CHAR)) {
                    return ""; // don't print this line
                }
            }
            // do not indent if false
            int start = 0;
            if (indent) {
                // indent lines based on level
                if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + BUILD_REPORT_CHAR)) {
                    inputLine[0] = "   ";
                } else if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + BUILD_REPORT_CHAR)) {
                    inputLine[0] = "  ";
                } else if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + BUILD_REPORT_CHAR)) {
                    inputLine[0] = " ";
                } else if (inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + BUILD_REPORT_CHAR)) {
                    inputLine[0] = "";
                }
            } else {
                start = 1;
            }
            // rebuild line
            StringBuffer buf = new StringBuffer();
            for (int i = start; i < inputLine.length; i++) {
                buf.append(inputLine[i] + " ");
            }
            // blank line?
            if (buf.length() == 0) {
                return " ";
            }
            return buf.toString();
        } else {
            log.debug("ERROR first characters of build report not valid ({})", line);
            return "ERROR " + line; // NOI18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainPrintBuildReport.class);
}
