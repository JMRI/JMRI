package jmri.jmrit.operations.trains;

import java.awt.*;
import java.awt.JobAttributes.SidesType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Used for train Manifests and switch lists.
 *
 * @author Daniel Boudreau (C) 2025
 */
public class TrainPrintManifest extends TrainCommon {

    static final char SPACE = ' ';

    /**
     * Print or preview a train Manifest or switch list.
     *
     * @param file          File to be printed or previewed
     * @param name          Title of document
     * @param isPreview     true if preview
     * @param fontName      optional font to use when printing document
     * @param logoURL       optional pathname for logo
     * @param printerName   optional default printer name
     * @param orientation   Setup.LANDSCAPE, Setup.PORTRAIT, or Setup.HANDHELD
     * @param fontSize      font size
     * @param isPrintHeader when true print page header
     * @param sidesType     two sides long or short can be null
     */
    public static void printReport(File file, String name, boolean isPreview, String fontName, String logoURL,
            String printerName, String orientation, int fontSize, boolean isPrintHeader, SidesType sidesType) {
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

            if (logoURL != null && !logoURL.equals(Setup.NONE)) {
                ImageIcon icon = new ImageIcon(logoURL);
                if (icon.getIconWidth() == -1) {
                    log.error("Logo not found: {}", logoURL);
                } else {
                    writer.write(icon.getImage(), new JLabel(icon));
                }
            }

            List<String> lines = new ArrayList<>();
            String line;
            while (true) {
                line = in.readLine();
                if (line == null) {
                    if (isPreview) {
                        // need to do this in case the input file was empty to create preview
                        writer.write(" ");
                    }
                    break;
                }
                lines.add(line);
                if (line.isBlank()) {
                    print(writer, lines, false);
                }
            }
            print(writer, lines, true);
        } catch (FileNotFoundException e) {
            log.error("Build file doesn't exist", e);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException e) {
            log.warn("Exception printing: {}", e.getLocalizedMessage());
        }
    }

    private static void print(HardcopyWriter writer, List<String> lines, boolean lastBlock) throws IOException {
        if (Setup.isPrintNoPageBreaksEnabled() &&
                writer.getCurrentLineNumber() != 0 &&
                writer.getLinesPerPage() - writer.getCurrentLineNumber() < lines.size()) {
            writer.pageBreak();
        }
        // check for exact page break
        if (writer.getLinesPerPage() - writer.getCurrentLineNumber() + 1 == lines.size()) {
            // eliminate blank line after page break
            String s = lines.get(lines.size() - 1);
            if (s.isBlank()) {
                lines.remove(lines.size() - 1);
            }
        }
        // use line feed for all lines?
        if (lastBlock && writer.getLinesPerPage() - writer.getCurrentLineNumber() < lines.size()) {
            lastBlock = false; // yes
        }

        Color color = null;
        boolean printingColor = false;
        for (String line : lines) {
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
            writer.write(line);
            // no line feed if last line of file, eliminates blank page
            if (!lastBlock ||
                    writer.getCurrentLineNumber() < writer.getLinesPerPage() - 1) {
                writer.write(NEW_LINE);
            }
        }
        lines.clear();
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

    private final static Logger log = LoggerFactory.getLogger(TrainPrintManifest.class);
}
