// StringUtil.java
package jmri.util;

import java.util.Collection;
import java.util.Iterator;

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
 * @version $Revision$
 */
public class StringUtil {

    /**
     * Starting with two arrays, one of names and one of corresponding numeric
     * state values, find the state value that matches a given name string
     *
     * @return -1 if not found
     */
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
     * @return empty array if none found
     */
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
        for (int i = 0; i < count; i++) {
            output[i] = temp[i];
        }
        return output;
    }

    /**
     * Starting with two arrays, one of names and one of corresponding numeric
     * state values, find the name string that matches a given state value. Only
     * one may be returned.
     *
     * @return null if not found
     */
    static public String getNameFromState(int state, int[] states, String[] names) {
        for (int i = 0; i < states.length; i++) {
            if (state == states[i]) {
                return names[i];
            }
        }
        return null;
    }

    static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Convert an int to a exactly two hexadecimal characters
     *
     * @return String exactly two characters long
     */
    static public String twoHexFromInt(int val) {
        StringBuffer sb = new StringBuffer();
        sb.append(hexChars[(val & 0xF0) >> 4]);
        sb.append(hexChars[val & 0x0F]);
        return sb.toString();
    }

    /**
     * Quickly append an int to a String as exactly two hexadecimal characters
     *
     * @param val      Value to append in hex
     * @param inString String to be extended
     * @return String exactly two characters long
     */
    static public String appendTwoHexFromInt(int val, String inString) {
        StringBuffer sb = new StringBuffer(inString);
        sb.append(hexChars[(val & 0xF0) >> 4]);
        sb.append(hexChars[val & 0x0F]);
        return sb.toString();
    }

    /**
     * Convert a small number to eight 1/0 characters.
     *
     * @param msbLeft the MSB is on the left of the display
     */
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
    static public String hexStringFromBytes(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexChars[(bytes[i] & 0xF0) >> 4]);
            sb.append(hexChars[bytes[i] & 0x0F]);
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
    static public byte[] bytesFromHexString(String s) {
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
     * This is a lexagraphic sort; lower case goes to the end. Identical entries
     * are retained, so the output length is the same as the input length.
     *
     */
    static public void sort(String[] values) {
        try {
            java.util.Arrays.sort(values);
        } catch (Throwable e1) {  // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            bubblesort(values);
        }
    }

    // Internal method to do a case-preserving sort of Strings
    static private void bubblesort(String[] values) {
        // no Java sort, so ugly bubble sort
        for (int i = 0; i <= values.length - 2; i++) { // stop sort early to save time!
            for (int j = values.length - 2; j >= i; j--) {
                // check that the jth value is smaller than j+1th,
                // else swap
                if (0 < values[j].compareTo(values[j + 1])) {
                    // swap
                    String temp = values[j];
                    values[j] = values[j + 1];
                    values[j + 1] = temp;
                }
            }
        }
    }

    // Internal method to do a case-blind sort of the .toString values
    // of objects.
    static private void bubblesort(Object[] values) {
        for (int i = 0; i <= values.length - 2; i++) { // stop sort early to save time!
            for (int j = values.length - 2; j >= i; j--) {
                // check that the jth value is smaller than j+1th,
                // else swap
                if (0 < (values[j].toString()).compareTo(values[j + 1].toString())) {
                    // swap
                    Object temp = values[j];
                    values[j] = values[j + 1];
                    values[j + 1] = temp;
                }
            }
        }
    }

    /**
     * This is a case-blind sort. Identical entries are retained, so the output
     * length is the same as the input length.
     *
     */
    static public void sort(Object[] values) {
        try {
            java.util.Arrays.sort(values);
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            // no Java sort, so ugly bubble sort
            bubblesort(values);
        }
    }

    static void bubblesortUpper(Object[] values) {
        for (int i = 0; i <= values.length - 2; i++) { // stop sort early to save time!
            for (int j = values.length - 2; j >= i; j--) {
                // check that the jth value is smaller than j+1th,
                // else swap
                if (0 < (values[j].toString().toUpperCase()).compareTo(values[j + 1].toString().toUpperCase())) {
                    // swap
                    Object temp = values[j];
                    values[j] = values[j + 1];
                    values[j + 1] = temp;
                }
            }
        }
    }

    /**
     * This is a case-independent lexagraphic sort. Identical entries are
     * retained, so the output length is the same as the input length.
     *
     */
    static public void sortUpperCase(Object[] values) {
        // no Java sort, so ugly bubble sort
        bubblesortUpper(values);
    }

    /**
     * Sort String[] representing numbers, in ascending order.
     *
     */
    static public void numberSort(String[] values) throws NumberFormatException {
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
     * Join a collection of strings, separated by a delimiter
     *
     * @param s	        collection of strings
     * @return e.g. {@code join({"abc","def,"ghi"}, ".") ==> "abc.def.ghi"}
     */
    public static String join(Collection<String> s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    /**
     * Join an array of strings, separated by a delimiter
     *
     * @param s	        collection of strings
     * @return e.g. {@code join({"abc","def,"ghi"}, ".") ==> "abc.def.ghi"}
     */
    public static String join(String[] s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < s.length; i++) {
            buffer.append(s[i]);
            if (i < s.length - 1) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    /**
     * Split a string into an array of Strings, at a particular divider. This is
     * similar to the new String.split method, except that this does not provide
     * regular expression handling; the divider string is just a string.
     *
     * @param input   String to split
     * @param divider Where to divide the input; this does not appear in output
     */
    static public String[] split(String input, String divider) {
        int size = 0;
        String temp = input;

        // count entries
        while (temp.length() > 0) {
            size++;
            int index = temp.indexOf(divider);
            if (index < 0) {
                break;    // break not found
            }
            temp = temp.substring(index + divider.length());
            if (temp.length() == 0) {  // found at end
                size++;
                break;
            }
        }

        String[] result = new String[size];

        // find entries
        temp = input;
        size = 0;
        while (temp.length() > 0) {
            int index = temp.indexOf(divider);
            if (index < 0) {
                break;    // done with all but last
            }
            result[size] = temp.substring(0, index);
            temp = temp.substring(index + divider.length());
            size++;
        }
        result[size] = temp;

        return result;
    }

    /**
     * If there's an unmatched ), quote it with \, and quote \ with \ too.
     */
    static public String parenQuote(String in) {
        if (in.equals("")) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        int level = 0;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '(') {
                level++;
            } else if (c == '\\') {
                result.append('\\');
            } else if (c == ')') {
                level--;
                if (level < 0) {
                    level = 0;
                    result.append('\\');
                }
            }
            result.append(c);
        }
        return new String(result);
    }

    /**
     * Undo parenQuote
     */
    static String parenUnQuote(String in) {
        if (in.equals("")) {
            return "";
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

    static public java.util.List<String> splitParens(String in) {
        java.util.ArrayList<String> result = new java.util.ArrayList<String>();
        if (in.equals("")) {
            return result;
        }
        int level = 0;
        String temp = "";
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '(') {
                level++;
            } else if (c == '\\') {
                temp += c;
                i++;
                c = in.charAt(i);
            } else if (c == ')') {
                level--;
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
     * Return String after replacing various special characters with their
     * "escaped" counterpart, to facilitate use with web servers.
     *
     * @param s String to escape
     * @return String with escaped values
     */
    static public String escapeString(String s) {
        return s.replaceAll(" ", "%20").replaceAll("#", "%23").replaceAll("&", "%26").replaceAll("'", "%27").replaceAll("\"", "%22").replaceAll("<", "%3C").replaceAll(">", "%3E");
    }

    /**
     * Return String after replacing various escaped character with their
     * "regular" counterpart, to facilitate use with web servers.
     *
     * @param s String to unescape
     * @return String with escaped values replaced with regular values
     */
    static public String unescapeString(String s) {
        return s.replaceAll("%20", " ").replaceAll("%23", "#").replaceAll("%26", "&").replaceAll("%27", "'").replaceAll("%22", "\"").replaceAll("%3C", "<").replaceAll("%3E", ">");
    }

    /**
     * Convert an array of objects into a single string. Each object's toString
     * value is displayed within square brackets and separated by commas.
     */
    static public <E> String arrayToString(E[] v) {
        StringBuffer retval = new StringBuffer();
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
     */
    static public String arrayToString(byte[] v) {
        StringBuffer retval = new StringBuffer();
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
     * Convert an array of ints into a single string. Each element is displayed
     * within square brackets and separated by commas.
     */
    static public String arrayToString(int[] v) {
        StringBuffer retval = new StringBuffer();
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
}
