package jmri.jmrit.operations.trains;

import java.awt.*;
import java.awt.JobAttributes.SidesType;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Train print utilities. Used for train Manifests and build reports.
 *
 * @author Daniel Boudreau (C) 2010, 2025
 */
public class TrainPrintUtilities extends TrainCommon {

    static final char SPACE = ' ';

    /**
     * Print or preview a train Manifest, build report, or switch list.
     *
     * @param file          File to be printed or previewed
     * @param name          Title of document
     * @param isPreview     true if preview
     * @param fontName      optional font to use when printing document
     * @param isBuildReport true if build report
     * @param logoURL       optional pathname for logo
     * @param printerName   optional default printer name
     * @param orientation   Setup.LANDSCAPE, Setup.PORTRAIT, or Setup.HANDHELD
     * @param fontSize      font size
     * @param isPrintHeader when true print page header
     * @param sidesType     two sides long or short can be null
     */
    public static void printReport(File file, String name, boolean isPreview, String fontName, boolean isBuildReport,
            String logoURL, String printerName, String orientation, int fontSize, boolean isPrintHeader,
            SidesType sidesType) {
        // obtain a HardcopyWriter to do this

        boolean isLandScape = false;
        double margin = .5;
        Dimension pagesize = null; // HardcopyWritter provides default page
                                   // sizes for portrait and landscape
        if (orientation.equals(Setup.LANDSCAPE)) {
            margin = .65;
            isLandScape = true;
        }
        if (orientation.equals(Setup.HANDHELD) || orientation.equals(Setup.HALFPAGE)) {
            isPrintHeader = false;
            // add margins to page size
            pagesize = new Dimension(getPageSize(orientation).width + PAPER_MARGINS.width,
                    getPageSize(orientation).height + PAPER_MARGINS.height);
        }
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), name, fontSize, margin,
                margin, .5, .5, isPreview, printerName, isLandScape, isPrintHeader, sidesType, pagesize);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8));) {

            // set font
            if (!fontName.isEmpty()) {
                writer.setFontName(fontName);
            }

            if (!isBuildReport && logoURL != null && !logoURL.equals(Setup.NONE)) {
                ImageIcon icon = new ImageIcon(logoURL);
                if (icon.getIconWidth() == -1) {
                    log.error("Logo not found: {}", logoURL);
                } else {
                    writer.write(icon.getImage(), new JLabel(icon));
                }
            }

            String line;
            Color color = null;
            boolean printingColor = false;
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
                if (isBuildReport) {
                    line = filterBuildReport(line, false); // no indent
                    if (line.isEmpty()) {
                        continue;
                    }
                } else {
                    // printing the train Manifest or switch list
                    // determine if there's a line separator
                    if (printHorizontialLineSeparator(writer, line)) {
                        color = null;
                        continue;
                    }
                    // color text?
                    if (line.contains(TEXT_COLOR_START)) {
                        color = getTextColor(line);
                        if (line.contains(TEXT_COLOR_END)) {
                            printingColor = false;
                        } else {
                            // printing multiple lines in color
                            printingColor = true;
                        }
                        // could be a color change when using two column format
                        if (line.contains(Character.toString(VERTICAL_LINE_CHAR))) {
                            String s = line.substring(0, line.indexOf(VERTICAL_LINE_CHAR));
                            s = getTextColorString(s);
                            writer.write(color, s); // 1st half of line printed
                            // get the new color and text
                            line = line.substring(line.indexOf(VERTICAL_LINE_CHAR));
                            color = getTextColor(line);
                            // pad out string
                            line = tabString(getTextColorString(line), s.length());
                        } else {
                            // simple case only one color
                            line = getTextColorString(line);
                        }
                    } else if (line.contains(TEXT_COLOR_END)) {
                        printingColor = false;
                        line = getTextColorString(line);
                    } else if (!printingColor) {
                        color = null;
                    }

                    printVerticalLineSeparator(writer, line);
                    line = line.replace(VERTICAL_LINE_CHAR, SPACE);

                    if (color != null) {
                        writer.write(color, line + NEW_LINE);
                        continue;
                    }
                }
                writer.write(line + NEW_LINE);
            }
            in.close();
        } catch (FileNotFoundException e) {
            log.error("Build file doesn't exist", e);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException e) {
            log.warn("Exception printing: {}", e.getLocalizedMessage());
        }
    }

    /*
     * Returns true if horizontal line was printed, or line length = 0
     */
    private static boolean printHorizontialLineSeparator(HardcopyWriter writer, String line) {
        boolean horizontialLineSeparatorFound = true;
        if (line.length() > 0) {
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) != HORIZONTAL_LINE_CHAR) {
                    horizontialLineSeparatorFound = false;
                    break;
                }
            }
            if (horizontialLineSeparatorFound) {
                writer.write(writer.getCurrentLineNumber(), 0, writer.getCurrentLineNumber(),
                        line.length() + 1);
            }
        }
        return horizontialLineSeparatorFound;
    }

    private static void printVerticalLineSeparator(HardcopyWriter writer, String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == VERTICAL_LINE_CHAR) {
                // make a frame (two column format)
                if (Setup.isTabEnabled()) {
                    writer.write(writer.getCurrentLineNumber(), 0, writer.getCurrentLineNumber() + 1, 0);
                    writer.write(writer.getCurrentLineNumber(), line.length() + 1,
                            writer.getCurrentLineNumber() + 1, line.length() + 1);
                }
                writer.write(writer.getCurrentLineNumber(), i + 1, writer.getCurrentLineNumber() + 1,
                        i + 1);
            }
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
            in.close();
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

    public static JComboBox<String> getPrinterJComboBox() {
        JComboBox<String> box = new JComboBox<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : services) {
            box.addItem(printService.getName());
        }

        // Set to default printer
        box.setSelectedItem(getDefaultPrinterName());

        return box;
    }

    public static String getDefaultPrinterName() {
        if (PrintServiceLookup.lookupDefaultPrintService() != null) {
            return PrintServiceLookup.lookupDefaultPrintService().getName();
        }
        return ""; // no default printer specified
    }

    private final static Logger log = LoggerFactory.getLogger(TrainPrintUtilities.class);
}
