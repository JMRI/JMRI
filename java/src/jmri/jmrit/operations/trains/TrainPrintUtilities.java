package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.*;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Train print utilities. Used for train manifests and build reports.
 *
 * @author Daniel Boudreau (C) 2010
 */
public class TrainPrintUtilities {

    static final String NEW_LINE = "\n"; // NOI18N
    static final char HORIZONTAL_LINE_SEPARATOR = '-'; // NOI18N
    static final char VERTICAL_LINE_SEPARATOR = '|'; // NOI18N
    static final char SPACE = ' ';

    /**
     * Print or preview a train manifest, build report, or switch list.
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
     */
    public static void printReport(File file, String name, boolean isPreview, String fontName,
            boolean isBuildReport, String logoURL, String printerName, String orientation, int fontSize) {
        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        Frame mFrame = new Frame();
        boolean isLandScape = false;
        boolean printHeader = true;
        double margin = .5;
        Dimension pagesize = null; // HardcopyWritter provides default page
                                   // sizes for portrait and landscape
        if (orientation.equals(Setup.LANDSCAPE)) {
            margin = .65;
            isLandScape = true;
        }
        if (orientation.equals(Setup.HANDHELD) || orientation.equals(Setup.HALFPAGE)) {
            printHeader = false;
            // add margins to page size
            pagesize = new Dimension(TrainCommon.getPageSize(orientation).width + TrainCommon.PAPER_MARGINS.width,
                    TrainCommon.getPageSize(orientation).height + TrainCommon.PAPER_MARGINS.height);
        }
        try {
            writer = new HardcopyWriter(mFrame, name, fontSize, margin, margin, .5, .5,
                    isPreview, printerName, isLandScape, printHeader, pagesize);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        // set font
        if (!fontName.equals("")) {
            writer.setFontName(fontName);
        }

        // now get the build file to print
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); // NOI18N
        } catch (FileNotFoundException e) {
            log.error("Build file doesn't exist");
            writer.close();
            return;
        } catch (UnsupportedEncodingException e) {
            log.error("Doesn't support UTF-8 encoding");
            writer.close();
            return;
        }
        String line;

        if (!isBuildReport && logoURL != null && !logoURL.equals(Setup.NONE)) {
            ImageIcon icon = new ImageIcon(logoURL);
            if (icon.getIconWidth() == -1) {
                log.error("Logo not found: " + logoURL);
            } else {
                writer.write(icon.getImage(), new JLabel(icon));
            }
        }
        Color c = null;
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
                    try {
                        writer.write(" "); // need to do this in case the input
                                           // file was empty to create preview
                    } catch (IOException e) {
                        log.debug("Print write failed for null line");
                    }
                }
                break;
            }
            // log.debug("Line: {}", line.toString());
            // check for build report print level
            if (isBuildReport) {
                line = filterBuildReport(line, false); // no indent
                if (line.equals("")) {
                    continue;
                }
                // printing the train manifest
            } else {
                // determine if there's a line separator
                if (line.length() > 0) {
                    boolean horizontialLineSeparatorFound = true;
                    for (int i = 0; i < line.length(); i++) {
                        if (line.charAt(i) != HORIZONTAL_LINE_SEPARATOR) {
                            horizontialLineSeparatorFound = false;
                            break;
                        }
                    }
                    if (horizontialLineSeparatorFound) {
                        writer.write(writer.getCurrentLineNumber(), 0, writer.getCurrentLineNumber(),
                                line.length() + 1);
                        c = null;
                        continue;
                    }
                }

                // determine if line is a pickup or drop
                if ((!Setup.getPickupEnginePrefix().trim().isEmpty() &&
                        line.startsWith(Setup.getPickupEnginePrefix())) ||
                        (!Setup.getPickupCarPrefix().trim().isEmpty() &&
                                line.startsWith(Setup.getPickupCarPrefix())) ||
                        (!Setup.getSwitchListPickupCarPrefix().trim().isEmpty() &&
                                line.startsWith(Setup.getSwitchListPickupCarPrefix()))) {
                    c = Setup.getPickupColor();
                } else if ((!Setup.getDropEnginePrefix().trim().isEmpty() &&
                        line.startsWith(Setup.getDropEnginePrefix())) ||
                        (!Setup.getDropCarPrefix().trim().isEmpty() &&
                                line.startsWith(Setup.getDropCarPrefix())) ||
                        (!Setup.getSwitchListDropCarPrefix().trim().isEmpty() &&
                                line.startsWith(Setup.getSwitchListDropCarPrefix()))) {
                    c = Setup.getDropColor();
                } else if ((!Setup.getLocalPrefix().trim().isEmpty() &&
                        line.startsWith(Setup.getLocalPrefix())) ||
                        (!Setup.getSwitchListLocalPrefix().trim().isEmpty() &&
                                line.startsWith(Setup.getSwitchListLocalPrefix()))) {
                    c = Setup.getLocalColor();
                } else if (line.contains(TrainCommon.TEXT_COLOR_START)) {
                    c = TrainCommon.getTextColor(line);
                    if (!line.endsWith(TrainCommon.TEXT_COLOR_END)) {
                        printingColor = true;
                    }
                    // could be a color change when using two column format
                    if (line.contains(Character.toString(VERTICAL_LINE_SEPARATOR))) {
                        String s = line.substring(0, line.indexOf(VERTICAL_LINE_SEPARATOR));
                        s = TrainCommon.getTextColorString(s);
                        try {
                            writer.write(c, s); // 1st half of line printed
                        } catch (IOException e) {
                            log.debug("Print write color failed");
                            break;
                        }
                        // get the new color and text
                        line = line.substring(line.indexOf(VERTICAL_LINE_SEPARATOR));
                        c = TrainCommon.getTextColor(line);
                        // pad out string
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < s.length(); i++) {
                            sb.append(SPACE);
                        }
                        // 2nd half of line to be printed
                        line = sb.append(TrainCommon.getTextColorString(line)).toString();
                    } else {
                        // simple case only one color
                        line = TrainCommon.getTextColorString(line);
                    }
                } else if (line.contains(TrainCommon.TEXT_COLOR_END)) {
                    printingColor = false;
                    line = TrainCommon.getTextColorString(line);
                } else if (!line.startsWith(TrainCommon.TAB) && !printingColor) {
                    c = null;
                }
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == VERTICAL_LINE_SEPARATOR) {
                        // make a frame (two column format)
                        if (Setup.isTabEnabled()) {
                            writer.write(writer.getCurrentLineNumber(), 0, writer.getCurrentLineNumber() + 1, 0);
                            writer.write(writer.getCurrentLineNumber(), line.length() + 1,
                                    writer.getCurrentLineNumber() + 1, line.length() + 1);
                        }
                        writer.write(writer.getCurrentLineNumber(), i + 1, writer.getCurrentLineNumber() + 1, i + 1);
                    }
                }
                line = line.replace(VERTICAL_LINE_SEPARATOR, SPACE);

                if (c != null) {
                    try {
                        writer.write(c, line + NEW_LINE);
                        continue;
                    } catch (IOException e) {
                        log.debug("Print write color failed");
                        break;
                    }
                }
            }
            try {
                writer.write(line + NEW_LINE);
            } catch (IOException e) {
                log.debug("Print write failed");
                break;
            }
        }
        // and force completion of the printing
        try {
            in.close();
        } catch (IOException e) {
            log.debug("Print close failed");
        }
        writer.close();
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
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); // NOI18N
        } catch (FileNotFoundException e) {
            log.error("Build file doesn't exist");
            return;
        } catch (UnsupportedEncodingException e) {
            log.error("Doesn't support UTF-8 encoding");
            return;
        }
        PrintWriter out;
        File buildReport = InstanceManager.getDefault(TrainManagerXml.class).createTrainBuildReportFile(
                Bundle.getMessage("Report") + " " + name);
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(buildReport), "UTF-8")), true); // NOI18N
        } catch (IOException e) {
            log.error("Can not create build report file");
            try {
                in.close();
            } catch (IOException ee) {
            }
            return;
        }
        String line = " ";
        while (true) {
            try {
                line = in.readLine();
                if (line == null) {
                    break;
                }
                line = filterBuildReport(line, Setup.isBuildReportIndentEnabled());
                if (line.equals("")) {
                    continue;
                }
                out.println(line); // indent lines for each level
            } catch (IOException e) {
                log.debug("Print read failed");
                break;
            }
        }
        // and force completion of the printing
        try {
            in.close();
        } catch (IOException e) {
            log.debug("Close failed");
        }
        out.close();
        // open the file
        TrainUtilities.openDesktop(buildReport);
    }

    /*
     * Removes the print levels from the build report
     */
    private static String filterBuildReport(String line, boolean indent) {
        String[] inputLine = line.split("\\s+"); // NOI18N
        if (inputLine.length == 0) {
            return "";
        }
        if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-") ||
                inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-") ||
                inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-") ||
                inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + "-")) {

            if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_MINIMAL)) {
                if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-") ||
                        inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-") ||
                        inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
                    return ""; // don't print this line
                }
            }
            if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_NORMAL)) {
                if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-") ||
                        inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
                    return ""; // don't print this line
                }
            }
            if (Setup.getBuildReportLevel().equals(Setup.BUILD_REPORT_DETAILED)) {
                if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
                    return ""; // don't print this line
                }
            }
            // do not indent if false
            int start = 0;
            if (indent) {
                // indent lines based on level
                if (inputLine[0].equals(Setup.BUILD_REPORT_VERY_DETAILED + "-")) {
                    inputLine[0] = "   ";
                } else if (inputLine[0].equals(Setup.BUILD_REPORT_DETAILED + "-")) {
                    inputLine[0] = "  ";
                } else if (inputLine[0].equals(Setup.BUILD_REPORT_NORMAL + "-")) {
                    inputLine[0] = " ";
                } else if (inputLine[0].equals(Setup.BUILD_REPORT_MINIMAL + "-")) {
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
            log.debug("ERROR first characters of build report not valid (" + line + ")");
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
