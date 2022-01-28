package jmri.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;

/**
 * Classes that aid working with org.apache.commons.csv.CSVFormat
 * 
 * @author Daniel Bergqvist (C) 2022
 */
public class CsvUtil {

    public static final CsvUtil.CSVPredefinedFormat TDF_FORMAT = CsvUtil.CSVPredefinedFormat.getFormat(CSVFormat.Predefined.TDF);

    /**
     * Class that allows CSVFormat.Predefined to be listed in a JComboBox
     * together with the option "User defined".
     */
    public enum CSVPredefinedFormat {

        UserDefined(Bundle.getMessage("CsvUtil_CSVPredefinedFormat_UserDefined"), null, true),
        Excel(Bundle.getMessage("CsvUtil_CSVPredefinedFormat_Excel"), CSVFormat.Predefined.Excel, false),
        TabSeparated(Bundle.getMessage("CsvUtil_CSVPredefinedFormat_TabSeparated"), CSVFormat.Predefined.TDF, false);

        private final String _name;
        private final CSVFormat.Predefined _format;
        private final boolean _userDefined;
        private static final List<CSVPredefinedFormat> _formats = new ArrayList<>();

        static {
            for (CSVPredefinedFormat f : CSVPredefinedFormat.values()) {
                _formats.add(f);
            }
/*
            _formats.add(new CSVPredefinedFormat(Bundle.getMessage("CsvUtil_CSVPredefinedFormat_UserDefined"), null));
            for (CSVFormat.Predefined f : CSVFormat.Predefined.values()) {
                _formats.add(new CSVPredefinedFormat(f.name(), f));
            }
*/
        }

        private CSVPredefinedFormat(String name, CSVFormat.Predefined format, boolean userDefined) {
            _name = name;
            _format = format;
            _userDefined = userDefined;
//            _formats.add(this);
        }

        public CSVFormat.Predefined getFormat() {
            return _format;
        }

        public boolean isUserDefined() {
            return _userDefined;
        }

        @Override
        public String toString() {
            return _name;
        }

        public static CSVPredefinedFormat getFormat(CSVFormat.Predefined format) {
            for (CSVPredefinedFormat f : _formats) {
                if (f._format == format) return f;
            }
            throw new IllegalArgumentException("The format is an unknown format");
        }

        public static List<CSVPredefinedFormat> getFormats() {
            return Collections.unmodifiableList(_formats);
        }
    }

    /**
     * Class that allows CSVFormat delimiters to be listed in a JComboBox.
     */
    public static final class CSVDelimiter {
        private final String _name;
        private final String _delimiterString;
        private static final List<CSVDelimiter> _delimiters = new ArrayList<>();
        
        static {
            _delimiters.add(new CSVDelimiter(Bundle.getMessage("CsvUtil_CSVDelimiter_Comma"), ","));
            _delimiters.add(new CSVDelimiter(Bundle.getMessage("CsvUtil_CSVDelimiter_VerticalLine"), "|"));
            _delimiters.add(new CSVDelimiter(Bundle.getMessage("CsvUtil_CSVDelimiter_Tab"), "\t"));
        }

        private CSVDelimiter(String name, String delimiterString) {
            _name = name;
            _delimiterString = delimiterString;
        }

        public String getDelimiter() {
            return _delimiterString;
        }

        @Override
        public String toString() {
            return _name;
        }

        public static CSVDelimiter parse(char ch) {
            for (CSVDelimiter delimiter : getDelimiters()) {
                if ((delimiter._delimiterString.length() == 1)
                        && (delimiter._delimiterString.charAt(0) == ch)) {
                    return delimiter;
                }
            }
            throw new IllegalArgumentException("The char '" + ch + "' is an unknown delimiter");
        }

        public static CSVDelimiter parse(String str) {
            for (CSVDelimiter delimiter : getDelimiters()) {
                if (delimiter._delimiterString.equals(str)) return delimiter;
            }
            throw new IllegalArgumentException("The string '" + str + "' is an unknown delimiter");
        }

        public static List<CSVDelimiter> getDelimiters() {
            return Collections.unmodifiableList(_delimiters);
        }
    }

    /**
     * Class that allows CSVFormat escape character to be listed in a JComboBox.
     */
    public static final class CSVEscape {
        private final String _name;
        private final Character _escapeChar;
        private static final List<CSVEscape> _escapes = new ArrayList<>();
        
        static {
            _escapes.add(new CSVEscape(Bundle.getMessage("CsvUtil_CSVEscape_DoubleQuotes"), '"'));
            _escapes.add(new CSVEscape(Bundle.getMessage("CsvUtil_CSVEscape_Backspace"), '\\'));
            _escapes.add(new CSVEscape(Bundle.getMessage("CsvUtil_CSVEscape_None"), null));
        }

        private CSVEscape(String name, Character escapeChar) {
            _name = name;
            _escapeChar = escapeChar;
        }

        public Character getEscapeChar() {
            return _escapeChar;
        }

        @Override
        public String toString() {
            return _name;
        }

        public static CSVEscape parse(Character ch) {
            for (CSVEscape delimiter : getEscapes()) {
                if (delimiter._escapeChar == null) {
                    if (ch == null) return delimiter;
                } else if (delimiter._escapeChar.equals(ch)) {
                    return delimiter;
                }
            }
            throw new IllegalArgumentException("The string '" + ch + "' is an unknown escape char");
        }

        public static List<CSVEscape> getEscapes() {
            return Collections.unmodifiableList(_escapes);
        }
    }

    /**
     * Class that allows CSVFormat delimiters to be listed in a JComboBox.
     */
    public static final class CSVQuote {
        private final String _name;
        private final String _quoteString;
        private static final List<CSVQuote> _quotes = new ArrayList<>();
        
        static {
            _quotes.add(new CSVQuote(Bundle.getMessage("CsvUtil_CSVQuote_DoubleQuotes"), "\""));
            _quotes.add(new CSVQuote(Bundle.getMessage("CsvUtil_CSVQuote_None"), null));
        }

        private CSVQuote(String name, String quoteString) {
            _name = name;
            _quoteString = quoteString;
        }

        public String getQuote() {
            return _quoteString;
        }

        @Override
        public String toString() {
            return _name;
        }

        public static CSVQuote parse(Character ch) {
            String str = ch != null ? "" + ch : null;
            return parse(str);
        }

        public static CSVQuote parse(String str) {
            for (CSVQuote delimiter : getQuotes()) {
                if (delimiter._quoteString == null) {
                    if (str == null) return delimiter;
                } else if (delimiter._quoteString.equals(str)) {
                    return delimiter;
                }
            }
            throw new IllegalArgumentException("The string '" + str + "' is an unknown quote");
        }

        public static List<CSVQuote> getQuotes() {
            return Collections.unmodifiableList(_quotes);
        }
    }

    /**
     * Class that allows CSVFormat delimiters to be listed in a JComboBox.
     */
    public static final class CSVRecordSeparator {
        private final String _name;
        private final String _recordSeparatorString;
        private static final List<CSVRecordSeparator> _recordSeparators = new ArrayList<>();
        
        static {
            _recordSeparators.add(new CSVRecordSeparator(Bundle.getMessage("CsvUtil_CSVRecordSeparator_CR_LF"), "\r\n"));
            _recordSeparators.add(new CSVRecordSeparator(Bundle.getMessage("CsvUtil_CSVRecordSeparator_LF"), "\n"));
        }

        private CSVRecordSeparator(String name, String recordSeparatorString) {
            _name = name;
            _recordSeparatorString = recordSeparatorString;
        }

        public String getRecordSeparator() {
            return _recordSeparatorString;
        }

        @Override
        public String toString() {
            return _name;
        }

        public static CSVRecordSeparator parse(String str) {
            for (CSVRecordSeparator delimiter : getRecordSeparators()) {
                if (delimiter._recordSeparatorString.equals(str)) return delimiter;
            }
            throw new IllegalArgumentException("The string '" + str + "' is an unknown record separator");
        }

        public static List<CSVRecordSeparator> getRecordSeparators() {
            return Collections.unmodifiableList(_recordSeparators);
        }
    }

}
