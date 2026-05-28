package jmri.jmrit.operations.trains;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.Sides;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.CompatibleHardcopyWriter;

/**
 * Used for printing train Manifests and switch lists.
 *
 * @author Daniel Boudreau (C) 2025
 */
public class TrainPrintManifest extends TrainCommon {

    protected static final char SPACE = ' ';
    private static boolean isPrintingBoldDone = false;
    private static boolean isPrintingColor = false;
    private static Color color;

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
     * @param sides         two sides long or short can be null
     */
    public static void printReport(File file, String name, boolean isPreview, String fontName, String logoURL,
            String printerName, String orientation, int fontSize, boolean isPrintHeader, Sides sides) {

        double leftmargin = .5;
        double rightmargin = .5;
        double topmargin = .5;
        double bottommargin = .5;

        // get hand held or half page dimensions in DPI
        Dimension pageSize = getFullPageSizeDPI(orientation);

        if (orientation.equals(Setup.RECEIPT)) {
            leftmargin = .2;
            rightmargin = .2;
        }

        try (CompatibleHardcopyWriter writer = new CompatibleHardcopyWriter(new Frame(), name, fontSize, leftmargin,
                rightmargin, topmargin, bottommargin, isPreview, printerName, orientation.equals(Setup.LANDSCAPE),
                isPrintHeader, sides, pageSize);
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
        } catch (CompatibleHardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException e) {
            log.warn("Exception printing: {}", e.getLocalizedMessage());
        }
    }

    private static void print(CompatibleHardcopyWriter writer, List<String> lines, boolean lastBlock)
            throws IOException {
        int lineSize = getNumberOfLines(lines);
        if (Setup.isPrintNoPageBreaksEnabled() &&
                writer.getCurrentLineNumber() != 0 &&
                writer.getLinesPerPage() - writer.getCurrentLineNumber() < lineSize) {
            writer.pageBreak();
        }
        // check for exact page break
        if (writer.getLinesPerPage() - writer.getCurrentLineNumber() == lineSize) {
            // eliminate blank line after page break
            String s = lines.get(lines.size() - 1);
            if (s.isBlank()) {
                lines.remove(lines.size() - 1);
            }
        }
        // use line feed for all lines?
        if (lastBlock && writer.getLinesPerPage() - writer.getCurrentLineNumber() < lineSize) {
            lastBlock = false; // yes
        }

        isPrintingColor = false;
        color = null;

        for (String line : lines) {
            // determine if there's a line separator
            if (printHorizontialLineSeparator(writer, line)) {
                color = null;
                continue;
            }

            // bold text?
            line = printBold(writer, line);

            // color text?
            line = printColor(writer, line);

            line = printVerticalLineSeparator(writer, line);

            if (color != null) {
                writer.write(color, line);
            } else {
                writer.write(line);
            }

            // no line feed if last line of file, eliminates blank page
            if (!lastBlock ||
                    writer.getCurrentLineNumber() < writer.getLinesPerPage() - 1) {
                writer.write(NEW_LINE);
            }

            // done bold text?
            if (isPrintingBoldDone) {
                writer.setFontStyle(Font.PLAIN);
                isPrintingBoldDone = false;
            }
        }
        lines.clear();
    }

    /*
     * When determining the number of lines to print, we need to ignore any
     * horizontal lines.
     */
    private static int getNumberOfLines(List<String> lines) {
        int numberLines = lines.size();
        for (String line : lines) {
            for (char c : line.toCharArray()) {
                if (c == HORIZONTAL_LINE_CHAR) {
                    numberLines--;
                    break;
                }
            }
        }
        return numberLines;
    }

    /*
     * Returns true if horizontal line was printed, or line length = 0
     */
    private static boolean printHorizontialLineSeparator(CompatibleHardcopyWriter writer, String line) {
        boolean horizontialLineSeparatorFound = true;
        if (line.length() > 0) {
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) != HORIZONTAL_LINE_CHAR) {
                    horizontialLineSeparatorFound = false;
                    break;
                }
            }
            if (horizontialLineSeparatorFound) {
                int endCol = writer.getCharactersPerLine() + 1;
                writer.write(writer.getCurrentLineNumber(), 0, writer.getCurrentLineNumber(),
                        endCol);
            }
        }
        return horizontialLineSeparatorFound;
    }

    private static String printVerticalLineSeparator(CompatibleHardcopyWriter writer, String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == VERTICAL_LINE_CHAR) {
                // make a frame (two column format)
                if (Setup.isTabEnabled()) {
                    int endCol = writer.getCharactersPerLine() + 1;
                    writer.write(writer.getCurrentLineNumber(), 0, writer.getCurrentLineNumber() + 1, 0);
                    writer.write(writer.getCurrentLineNumber(), endCol,
                            writer.getCurrentLineNumber() + 1, endCol);
                }
                writer.write(writer.getCurrentLineNumber(), i + 1, writer.getCurrentLineNumber() + 1,
                        i + 1);
            }
        }
        line = line.replace(VERTICAL_LINE_CHAR, SPACE);
        return line;
    }

    private static String printBold(CompatibleHardcopyWriter writer, String line) throws IOException {
        if (line.contains(TEXT_BOLD_END)) {
            isPrintingBoldDone = true;
        }
        // if monospaced font, it is possible to only bold a subset of words in the line
        // can't combine color and bold words
        if (writer.isMonospaced() &&
                line.contains(TEXT_BOLD) &&
                line.contains(TEXT_BOLD_END) &&
                !line.contains(TEXT_COLOR_START) &&
                !line.contains(TEXT_COLOR_END)) {
            printBoldWords(writer, line);
            line = ""; // done
        } else {
            if (line.contains(TEXT_BOLD)) {
                writer.setFontStyle(Font.BOLD); // bold the entire line
            }
            if (line.contains(TEXT_BOLD) || line.contains(TEXT_BOLD_END)) {
                line = getTextBoldString(line); // strip the bold characters
            }
        }
        return line;
    }

    // where in the line to add words
    private static int offset;

    private static void printBoldWords(CompatibleHardcopyWriter writer, String line) throws IOException {
        offset = 0;
        // determine how many bold words to print
        String[] strings = line.split(TEXT_BOLD);
        for (String s : strings) {
            if (s.contains(TEXT_BOLD_END)) {
                writer.setFontStyle(Font.BOLD);
                String text = s.substring(0, s.indexOf(TEXT_BOLD_END));
                writeWords(writer, text); // bold text

                writer.setFontStyle(Font.PLAIN);
                text = s.substring(s.indexOf(TEXT_BOLD_END) + TEXT_BOLD_END.length());
                writeWords(writer, text); // plain text
            } else {
                writeWords(writer, s); // plain text
            }
        }
    }

    private static void writeWords(CompatibleHardcopyWriter writer, String s) throws IOException {
        String text = tabString(s, offset);
        writer.write(text);
        offset = +text.length();
    }

    private static String printColor(CompatibleHardcopyWriter writer, String line) throws IOException {
        if (line.contains(TEXT_COLOR_START)) {
            color = getTextColor(line);
            if (line.contains(TEXT_COLOR_END)) {
                isPrintingColor = false;
            } else {
                // printing multiple lines in color
                isPrintingColor = true;
            }
            // could be a color change when using two column format
            if (line.contains(Character.toString(VERTICAL_LINE_CHAR))) {
                String s = line.substring(0, line.indexOf(VERTICAL_LINE_CHAR));
                s = getOnlyText(s);
                writer.write(color, s); // 1st half of line printed
                // get the new color and text
                line = line.substring(line.indexOf(VERTICAL_LINE_CHAR));
                color = getTextColor(line);
                // pad out string
                line = tabString(getOnlyText(line), s.length());
            } else {
                // simple case only one color
                line = getOnlyText(line);
            }
        } else if (line.contains(TEXT_COLOR_END)) {
            isPrintingColor = false;
            line = getOnlyText(line);
        } else if (!isPrintingColor) {
            color = null;
        }
        return line;
    }

    private static final Logger log = LoggerFactory.getLogger(TrainPrintManifest.class);
}
