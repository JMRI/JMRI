// StringUtil.java

package jmri.util;

/**
 * Common utility methods for working with Strings.
 * <P>
 * We needed a place to refactor common string-processing idioms in JMRI
 * code, so this class was created. It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 * <P>
 * In some cases, these routines use a Java 1.3 or later method, falling
 * back to an explicit implementation when running on Java 1.1
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision: 1.5 $
 */

public class StringUtil {

    /**
     * Convert an int to a exactly two hexadecimal characters
     * @param n
     * @return String exactly two characters long
     */
    static public String twoHexFromInt(int n) {
        if ((n&0xFF) < 16) return "0"+Integer.toHexString(n&0xFF);
        else return Integer.toHexString(n&0xFF);
    }

    /**
     * Create a byte[] from a String containing hexadecimal values.
     *
     * @param s String of hex values, ala "01 02 0A B1 21".
     * @return byte array, with one byte for each pair.  Can be zero length,
     *  but will not be null.
     */
    static public byte[] bytesFromHexString(String s) {
        String ts = s+"  "; // ensure blanks on end to make scan easier
        int len = 0;
        // scan for length
        for (int i= 0; i< s.length(); i++) {
            if (ts.charAt(i) != ' ')  {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i+1) != ' ') {
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
        for (int i= 0; i< s.length(); i++) {
            if (ts.charAt(i) != ' ')  {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i+1) != ' ') {
                    // 2 char value
                    String v = new String(""+ts.charAt(i))+ts.charAt(i+1);
                    b[saveAt] = (byte)Integer.valueOf(v,16).intValue();
                    i++;
                    saveAt++;
                } else {
                    // 1 char value
                    String v = new String(""+ts.charAt(i));
                    b[saveAt] = (byte)Integer.valueOf(v,16).intValue();
                    saveAt++;
                }
            }
        }
        return b;
    }

    /**
     * This is a lexagraphic sort; lower case goes to the end.
     * Identical entries are retained, so the output length is the same
     * as the input length.
     * @param values
     */
    static public void sort(String[] values) {
        try {
            java.util.Arrays.sort(values);
        } catch (NoSuchMethodError e) {
            // no Java sort, so ugly bubble sort
            for (int i=0; i<=values.length-2; i++) { // stop sort early to save time!
                for (int j=values.length-2; j>=i; j--) {
                    // check that the jth value is smaller than j+1th,
                    // else swap
                    if (0 < values[j].compareTo(values[j+1])) {
                        // swap
                        String temp = values[j];
                        values[j] = values[j+1];
                        values[j+1] = temp;
                    }
                }
            }
        }
    }

    /**
     * This is a lexagraphic sort; lower case goes to the end.
     * Identical entries are retained, so the output length is the same
     * as the input length.
     * @param values
     */
    static public void sort(Object[] values) {
        try {
            java.util.Arrays.sort(values);
        } catch (NoSuchMethodError e) {
            // no Java sort, so ugly bubble sort
            for (int i=0; i<=values.length-2; i++) { // stop sort early to save time!
                for (int j=values.length-2; j>=i; j--) {
                    // check that the jth value is smaller than j+1th,
                    // else swap
                    if (0 < ((String)values[j]).compareTo((String)values[j+1])) {
                        // swap
                        Object temp = values[j];
                        values[j] = values[j+1];
                        values[j+1] = temp;
                    }
                }
            }
        }
    }

}