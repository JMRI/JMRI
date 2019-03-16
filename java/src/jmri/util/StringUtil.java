package jmri.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Common utility methods for working with Strings.
 * <P>
 * We needed a place to refactor common string-processing idioms in JMRI code,
 * so this class was created. It's more of a library of procedures than a real
 * class, as (so far) all of the operations have needed no state information.
 * <P>
 * In some cases, these routines use a Java 1.3 or later method, falling back to
 * an explicit implementation when running on Java 1.1
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class StringUtil {

    /**
     * Starting with two arrays, one of names and one of corresponding numeric
     * state values, find the state value that matches a given name string
     *
     * @param name   the name to search for
     * @param states the state values
     * @param names  the name values
     * @return the state or -1 if none found
     */
    @CheckReturnValue
    static public int getStateFromName(String name, int[] states, String[] names) {
        for (int i = 0; i < states.length; i++) {
            if (name.equals(names[i])) {
                return states[i];
            }
        }
        return -1;
    }

    /**
     * Starting with three arrays, one of names, one of corresponding numeric
     * state values, and one of masks for the state values, find the name
     * string(s) that match a given state value
     *
     * @param state  the given state
     * @param states the state values
     * @param masks  the state masks
     * @param names  the state names
     * @return names matching the given state or an empty array
     */
    @CheckReturnValue
    static public String[] getNamesFromStateMasked(int state, int[] states, int[] masks, String[] names) {
        // first pass to count, get refs
        int count = 0;
        String[] temp = new String[states.length];

        for (int i = 0; i < states.length; i++) {
            if (((state ^ states[i]) & masks[i]) == 0) {
                temp[count++] = names[i];
            }
        }
        // second pass to create output array
        String[] output = new String[count];
        System.arraycopy(temp, 0, output, 0, count);
        return output;
    }

    /**
     * Starting with two arrays, one of names and one of corresponding numeric
     * state values, find the name string that matches a given state value. Only
     * one may be returned.
     *
     * @param state  the given state
     * @param states the state values
     * @param names  the state names
     * @return the first matching name or null if none found
     */
    @CheckReturnValue
    @CheckForNull
    static public String getNameFromState(int state, @Nonnull int[] states, @Nonnull String[] names) {
        for (int i = 0; i < states.length; i++) {
            if (state == states[i]) {
                return names[i];
            }
        }
        return null;
    }

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Convert an integer to an exactly two hexadecimal characters string
     *
     * @param val the integer value
     * @return String exactly two characters long
     */
    @CheckReturnValue
    @Nonnull
    static public String twoHexFromInt(int val) {
        StringBuilder sb = new StringBuilder();
        sb.append(HEX_CHARS[(val & 0xF0) >> 4]);
        sb.append(HEX_CHARS[val & 0x0F]);
        return sb.toString();
    }

    /**
     * Quickly append an integer to a String as exactly two hexadecimal
     * characters
     *
     * @param val      Value to append in hex
     * @param inString String to be extended
     * @return String exactly two characters long
     */
    @CheckReturnValue
    @Nonnull
    static public String appendTwoHexFromInt(int val, @Nonnull String inString) {
        StringBuilder sb = new StringBuilder(inString);
        sb.append(StringUtil.twoHexFromInt(val));
        return sb.toString();
    }

    /**
     * Convert a small number to eight 1/0 characters.
     *
     * @param val     the number to convert
     * @param msbLeft true if the MSB is on the left of the display
     * @return a string of binary characters
     */
    @CheckReturnValue
    @Nonnull
    static public String to8Bits(int val, boolean msbLeft) {
        String result = "";
        for (int i = 0; i < 8; i++) {
            if (msbLeft) {
                result = (((val & 0x01) != 0) ? "1" : "0") + result;
            } else {
                result = result + (((val & 0x01) != 0) ? "1" : "0");
            }
            val = val >> 1;
        }
        return result;
    }

    /**
     * Create a String containing hexadecimal values from a byte[].
     *
     * @param bytes byte array. Can be zero length, but must not be null.
     * @return String of hex values, ala "01 02 0A B1 21 ".
     */
    @CheckReturnValue
    @Nonnull
    static public String hexStringFromBytes(@Nonnull byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(HEX_CHARS[(bytes[i] & 0xF0) >> 4]);
            sb.append(HEX_CHARS[bytes[i] & 0x0F]);
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Create a byte[] from a String containing hexadecimal values.
     *
     * @param s String of hex values, ala "01 02 0A B1 21".
     * @return byte array, with one byte for each pair. Can be zero length, but
     *         will not be null.
     */
    @CheckReturnValue
    @Nonnull
    static public byte[] bytesFromHexString(@Nonnull String s) {
        String ts = s + "  "; // ensure blanks on end to make scan easier
        int len = 0;
        // scan for length
        for (int i = 0; i < s.length(); i++) {
            if (ts.charAt(i) != ' ') {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i + 1) != ' ') {
                    // 2 char value
                    i++;
                    len++;
                } else {
                    // 1 char value
                    len++;
                }
            }
        }
        byte[] b = new byte[len];
        // scan for content
        int saveAt = 0;
        for (int i = 0; i < s.length(); i++) {
            if (ts.charAt(i) != ' ') {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i + 1) != ' ') {
                    // 2 char value
                    String v = "" + ts.charAt(i) + ts.charAt(i + 1);
                    b[saveAt] = (byte) Integer.valueOf(v, 16).intValue();
                    i++;
                    saveAt++;
                } else {
                    // 1 char value
                    String v = "" + ts.charAt(i);
                    b[saveAt] = (byte) Integer.valueOf(v, 16).intValue();
                    saveAt++;
                }
            }
        }
        return b;
    }

    /**
     * This is a case-independent lexagraphic sort. Identical entries are
     * retained, so the output length is the same as the input length.
     *
     * @param values the Objects to sort
     */
    static public void sortUpperCase(@Nonnull Object[] values) {
        Arrays.sort(values, (Object o1, Object o2) -> o1.toString().compareToIgnoreCase(o2.toString()));
    }

    /**
     * Sort String[] representing numbers, in ascending order.
     *
     * @param values the Strings to sort
     */
    static public void numberSort(@Nonnull String[] values) throws NumberFormatException {
        for (int i = 0; i <= values.length - 2; i++) { // stop sort early to save time!
            for (int j = values.length - 2; j >= i; j--) {
                // check that the jth value is larger than j+1th,
                // else swap
                if (Integer.parseInt(values[j]) > Integer.parseInt(values[j + 1])) {
                    // swap
                    String temp = values[j];
                    values[j] = values[j + 1];
                    values[j + 1] = temp;
                }
            }
        }
    }

    /**
     * Quotes unmatched closed parentheses; matched ( ) pairs are left
     * unchanged.
     *
     * If there's an unmatched ), quote it with \, and quote \ with \ too.
     *
     * @param in String potentially containing unmatched closing parenthesis
     * @return null if given null
     */
    @CheckReturnValue
    @CheckForNull
    static public String parenQuote(@CheckForNull String in) {
        if (in == null || in.equals("")) {
            return in;
        }
        StringBuilder result = new StringBuilder();
        int level = 0;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '(':
                    level++;
                    break;
                case '\\':
                    result.append('\\');
                    break;
                case ')':
                    level--;
                    if (level < 0) {
                        level = 0;
                        result.append('\\');
                    }
                    break;
                default:
                    break;
            }
            result.append(c);
        }
        return new String(result);
    }

    /**
     * Undo parenQuote
     *
     * @param in the input String
     * @return null if given null
     */
    @CheckReturnValue
    @CheckForNull
    static String parenUnQuote(@CheckForNull String in) {
        if (in == null || in.equals("")) {
            return in;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\\') {
                i++;
                c = in.charAt(i);
                if (c != '\\' && c != ')') {
                    // if none of those, just leave both in place
                    c += '\\';
                }
            }
            result.append(c);
        }
        return new String(result);
    }

    @CheckReturnValue
    @Nonnull
    static public java.util.List<String> splitParens(@CheckForNull String in) {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        if (in == null || in.equals("")) {
            return result;
        }
        int level = 0;
        String temp = "";
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '(':
                    level++;
                    break;
                case '\\':
                    temp += c;
                    i++;
                    c = in.charAt(i);
                    break;
                case ')':
                    level--;
                    break;
                default:
                    break;
            }
            temp += c;
            if (level == 0) {
                result.add(temp);
                temp = "";
            }
        }
        return result;
    }

    /**
     * Replace various special characters with their "escaped" counterpart in
     * UTF-8 character encoding, to facilitate use with web servers.
     *
     * @param s String to escape
     * @return String with escaped values
     * @throws java.io.UnsupportedEncodingException if unable to escape in UTF-8
     * @deprecated since 4.9.1; use
     * {@link java.net.URLEncoder#encode(java.lang.String, java.lang.String)}
     * directly
     */
    @CheckReturnValue
    @Nonnull
    @Deprecated // since 4.9.1
    static public String escapeString(@Nonnull String s) throws UnsupportedEncodingException {
        jmri.util.Log4JUtil.deprecationWarning(log, "escapeString");        
        return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
    }

    /**
     * Replace various escaped character in UTF-8 character encoding with their
     * "regular" counterpart, to facilitate use with web servers.
     *
     * @param s String to unescape
     * @return String with escaped values replaced with regular values
     * @throws java.io.UnsupportedEncodingException if unable to unescape from
     *                                              UTF-8
     * @deprecated since 4.9.1; use
     * {@link java.net.URLDecoder#decode(java.lang.String, java.lang.String)}
     * directly
     */
    @CheckReturnValue
    @Nonnull
    @Deprecated // since 4.9.1
    static public String unescapeString(@Nonnull String s) throws UnsupportedEncodingException {
        jmri.util.Log4JUtil.deprecationWarning(log, "unescapeString");        
        return URLDecoder.decode(s, StandardCharsets.UTF_8.toString());
    }

    /**
     * Convert an array of objects into a single string. Each object's toString
     * value is displayed within square brackets and separated by commas.
     *
     * @param <E> the array class
     * @param v   the array to process
     * @return a string; empty if the array was empty
     */
    @CheckReturnValue
    @Nonnull
    static public <E> String arrayToString(@Nonnull E[] v) {
        StringBuilder retval = new StringBuilder();
        boolean first = true;
        for (E e : v) {
            if (!first) {
                retval.append(',');
            }
            first = false;
            retval.append('[');
            retval.append(e.toString());
            retval.append(']');
        }
        return new String(retval);
    }

    /**
     * Convert an array of bytes into a single string. Each element is displayed
     * within square brackets and separated by commas.
     *
     * @param v the array of bytes
     * @return the formatted String, or an empty String
     */
    @CheckReturnValue
    @Nonnull
    static public String arrayToString(@Nonnull byte[] v) {
        StringBuilder retval = new StringBuilder();
        boolean first = true;
        for (byte e : v) {
            if (!first) {
                retval.append(',');
            }
            first = false;
            retval.append('[');
            retval.append(e);
            retval.append(']');
        }
        return new String(retval);
    }

    /**
     * Convert an array of integers into a single string. Each element is
     * displayed within square brackets and separated by commas.
     *
     * @param v the array of integers
     * @return the formatted String or an empty String
     */
    @CheckReturnValue
    @Nonnull
    static public String arrayToString(@Nonnull int[] v) {
        StringBuilder retval = new StringBuilder();
        boolean first = true;
        for (int e : v) {
            if (!first) {
                retval.append(',');
            }
            first = false;
            retval.append('[');
            retval.append(e);
            retval.append(']');
        }
        return new String(retval);
    }

    /**
     * Trim a text string to length provided and (if shorter) pad with trailing spaces.
     * Removes 1 extra character to the right for clear column view.
     *
     * @param value contents to process
     * @param length trimming length
     * @return trimmed string, left aligned by padding to the right
     */
    @CheckReturnValue
    static public String padString (String value, int length) {
        if (length > 1) {
            return String.format("%-" + length + "s", value.substring(0, Math.min(value.length(), length - 1)));
        } else {
            return value;
        }
    }

    /**
     * Return the first int value within a string
     * eg :X X123XX456X: will return 123
     * eg :X123 456: will return 123
     *
     * @param str contents to process
     * @return first value in int form , -1 if not found
     */
    @CheckReturnValue
    static public int getFirstIntFromString(@Nonnull String str){
        StringBuilder sb = new StringBuilder();
        for (int i =0; i<str.length(); i ++) {
            char c = str.charAt(i);
            if (c != ' ' ){
                if (Character.isDigit(c)) {
                    sb.append(c);
                } else {
                    if ( sb.length() > 0 ) {
                        break;
                    }
                }
            } else {
                if ( sb.length() > 0 ) {
                    break;
                }
            }
        }
        if ( sb.length() > 0 ) {
            return (Integer.parseInt(sb.toString()));  
        }
        return -1;
    }

    /**
     * Return the last int value within a string
     * eg :XX123XX456X: will return 456
     * eg :X123 456: will return 456
     *
     * @param str contents to process
     * @return last value in int form , -1 if not found
     */
    @CheckReturnValue
    static public int getLastIntFromString(@Nonnull String str){
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() - 1; i >= 0; i --) {
            char c = str.charAt(i);
            if(c != ' '){
                if (Character.isDigit(c)) {
                    sb.insert(0, c);
                } else {
                    if ( sb.length() > 0 ) {
                        break;
                    }
                }
            } else {
                if ( sb.length() > 0 ) {
                    break;
                }
            }
        }
        if ( sb.length() > 0 ) {
            return (Integer.parseInt(sb.toString()));  
        }
        return -1;
    }

    /**
     * Replace the last occurance of string value within a String
     * eg  from ABC to DEF will convert XXABCXXXABCX to XXABCXXXDEFX
     *
     * @param string contents to process
     * @param from value within string to be replaced
     * @param to new value
     * @return string with the replacement, original value if no match.
     */
    @CheckReturnValue
    @Nonnull
    static public String replaceLast(@Nonnull String string, @Nonnull String from, @Nonnull String to) {
        int lastIndex = string.lastIndexOf(from);
        if (lastIndex < 0) {
            return string;
        }
        String tail = string.substring(lastIndex).replaceFirst(from, to);
        return string.substring(0, lastIndex) + tail;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringUtil.class);
}
