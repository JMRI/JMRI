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
 * Used for printing train Manifests and switch lists. Text can have color and
 * bold characters.
 *
 * @author Daniel Boudreau (C) 2025, 2026
 */
public class TrainPrintManifest extends TrainCommon {

    protected static final char SPACE_CHAR = ' ';
    private static boolean isPrintingStyleDone = false;
    private static boolean isPrintingColor = false;
    private static boolean isTextSizeDone = false;
    private static Color color;
    private static int _fontSize;

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

            _fontSize = fontSize;

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
            print(writer, lines, true); // last block
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
        int lineSize = getNumberOfLines(writer, lines);
        if (Setup.isPrintNoPageBreaksEnabled() &&
                writer.getCurrentLineNumber() != 0 &&
                writer.getLinesPerPage() - writer.getCurrentLineNumber() < (lastBlock ? lineSize : lineSize - 1)) {
            writer.pageBreak();
        }
        // check for exact page break
        if (writer.getLinesPerPage() - writer.getCurrentLineNumber() == lineSize - 1) {
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
            if (printHorizontalLineSeparator(writer, line)) {
                color = null;
                continue;
            }

            // font size change?
            line = setFontSize(writer, line);

            // bold or italic text?
            line = printStyle(writer, line);

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

            // done text size change?
            if (isTextSizeDone) {
                writer.setFont(null, null, _fontSize);
                isTextSizeDone = false;
            }

            // done bold or italic text?
            if (isPrintingStyleDone) {
                writer.setFontStyle(Font.PLAIN);
                isPrintingStyleDone = false;
            }
        }
        lines.clear();
    }

    /*
     * When determining the number of lines to print, we need to ignore any
     * horizontal lines.
     */
    private static int getNumberOfLines(CompatibleHardcopyWriter writer, List<String> lines) {
        int numberLines = lines.size();
        for (String line : lines) {
            if (isHorizontalLineSpearator(writer, line)) {
                numberLines--;
            }
        }
        return numberLines;
    }

    /*
     * Returns true if horizontal line was printed
     */
    private static boolean printHorizontalLineSeparator(CompatibleHardcopyWriter writer, String line) {
        boolean horizontalLineSeparatorFound = isHorizontalLineSpearator(writer, line);
        if (horizontalLineSeparatorFound) {
            int lineOffset = Setup.getHorizontalLineAdjustment();
            float vStart = writer.getCurrentVPos() + lineOffset;
            float hEnd = writer.getPrintablePagesizePoints().width;
            writer.writeLine(vStart, 0, vStart, hEnd);
        }
        return horizontalLineSeparatorFound;
    }

    /*
     * Determines if horizontal line. Requires the number of horizontal line
     * characters equal to the page width and no other characters in the line.
     * The smallest horizontal line is when the 2.25 wide paper is selected and
     * largest font 18. About 12 horizontal line characters.
     */
    private static boolean isHorizontalLineSpearator(CompatibleHardcopyWriter writer, String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == HORIZONTAL_LINE_CHAR) {
                count++;
            } else {
                count = 0;
                break; // all characters need to be horizontal char
            }
        }
        // one less character most likely not necessary
        return count >= writer.getCharactersPerLine() - 1;
    }

    private static String printVerticalLineSeparator(CompatibleHardcopyWriter writer, String line) {
        if (line.contains(Character.toString(VERTICAL_LINE_CHAR))) {
            // make a frame (two column format)
            int lineOffset = Setup.getHorizontalLineAdjustment();
            float vStart = writer.getCurrentVPos() + lineOffset;
            float hEnd = writer.getPrintablePagesizePoints().width;
            writer.writeLine(vStart, 0, vStart + writer.getLineHeight(), 0);
            writer.writeLine(vStart, hEnd / 2, vStart + writer.getLineHeight(), hEnd / 2);
            writer.writeLine(vStart, hEnd, vStart + writer.getLineHeight(), hEnd);
            line = line.replace(VERTICAL_LINE_CHAR, SPACE_CHAR);
        }
        return line;
    }

    private static String printStyle(CompatibleHardcopyWriter writer, String line) throws IOException {
        if (line.contains(TEXT_BOLD_END) || line.contains(TEXT_ITALIC_END)) {
            isPrintingStyleDone = true;
        }
        // If monospaced font, it is possible to only style a subset of words in the line.
        // Can't combine color and bold and italic words in a single line today. Would need to combine routines.
        if (writer.isMonospaced() &&
                !line.contains(TEXT_COLOR_START) &&
                !line.contains(TEXT_COLOR_END) &&
                line.contains(TEXT_BOLD) &&
                line.contains(TEXT_BOLD_END)) {
            offset = 0;
            printStyleWords(writer, line, TEXT_BOLD, TEXT_BOLD_END, Font.BOLD);
            line = ""; // done
        } else if (writer.isMonospaced() &&
                !line.contains(TEXT_COLOR_START) &&
                !line.contains(TEXT_COLOR_END) &&
                line.contains(TEXT_ITALIC) &&
                line.contains(TEXT_ITALIC_END)) {
            offset = 0;
            printStyleWords(writer, line, TEXT_ITALIC, TEXT_ITALIC_END, Font.ITALIC);
            line = ""; // done
        } else {
            if (line.contains(TEXT_ITALIC)) {
                writer.setFontStyle(Font.ITALIC); // italicize the entire line
            }
            if (line.contains(TEXT_BOLD)) {
                writer.setFontStyle(Font.BOLD); // bold the entire line
            }
            if (line.contains(TEXT_ITALIC) || line.contains(TEXT_ITALIC_END)) {
                line = getTextItalicString(line); // strip the italic characters
            }
            if (line.contains(TEXT_BOLD) || line.contains(TEXT_BOLD_END)) {
                line = getTextBoldString(line); // strip the bold characters
            }
        }
        return line;
    }

    // where in the line to add words
    private static int offset;

    private static void printStyleWords(CompatibleHardcopyWriter writer, String line, String startStyle,
            String endStyle, int style) throws IOException {
        // determine how many bold or italic words to print
        String[] strings = line.split(startStyle);
        for (String s : strings) {
            if (s.contains(endStyle)) {
                writer.setFontStyle(style);
                String text = s.substring(0, s.indexOf(endStyle));
                writeWords(writer, text); // bold or italic text

                writer.setFontStyle(Font.PLAIN);
                s = s.substring(s.indexOf(endStyle) + endStyle.length());
            }
            // special case where the line contains both bold and italic words
            if (s.contains(TEXT_ITALIC)) {
                printStyleWords(writer, s, TEXT_ITALIC, TEXT_ITALIC_END, Font.ITALIC);
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
            // if no TEXT_COLOR_END then printing multiple lines in color
            isPrintingColor = !line.contains(TEXT_COLOR_END);
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
            } else if (writer.isMonospaced() &&
                    printColorWords(writer, line)) {
                line = ""; // done
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

    // If monospaced font, it is possible to only color subset of words in the line can't combine color and bold words
    private static boolean printColorWords(CompatibleHardcopyWriter writer, String line) throws IOException {
        offset = 0;
        for (String words : getColorWords(line)) {
            Color color = getTextColor(words);
            words = getOnlyText(words);
            writeColorWords(writer, words, color);
        }
        return true;
    }

    private static List<String> getColorWords(String line) {
        ArrayList<String> list = new ArrayList<>();
        String s;
        while (line.length() > 0) {
            if (line.contains(TEXT_COLOR_START)) {
                s = line.substring(0, line.indexOf(TEXT_COLOR_START));
                if (s.length() > 0) {
                    list.add(s);
                }
                if (line.contains(TEXT_COLOR_END)) {
                    s = line.substring(line.indexOf(TEXT_COLOR_START),
                            line.indexOf(TEXT_COLOR_END) + TEXT_COLOR_END.length());
                    list.add(s);
                    line = line.substring(line.indexOf(TEXT_COLOR_END) + TEXT_COLOR_END.length());
                } else {
                    s = line.substring(line.indexOf(TEXT_COLOR_START));
                    list.add(s);
                    break;
                }
            } else {
                list.add(line);
                break; //done
            }
        }
        return list;
    }

    private static void writeColorWords(CompatibleHardcopyWriter writer, String s, Color color) throws IOException {
        String text = tabString(s, offset);
        writer.write(color, text);
        offset = +text.length();
    }

    private static String setFontSize(CompatibleHardcopyWriter writer, String line) {
        if (line.contains(TEXT_SIZE_START)) {
            int size = getFontSize(line);
            writer.setFont(null, null, size);
        }
        if (line.contains(TEXT_SIZE_END)) {
            isTextSizeDone = true;
        }
        return getTextSizeString(line);
    }

    private static final Logger log = LoggerFactory.getLogger(TrainPrintManifest.class);
}
