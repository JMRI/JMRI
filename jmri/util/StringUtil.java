// StringUtil.java

package jmri.util;

/**
 * Common utility methods for working with Strings.
 * <P>
 * We needed a place to refactor common string-processing idioms in JMRI
 * code, so this class was created. It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 *
 * @author Bob Jacobsen  Copyright 2003
 * @version $Revision: 1.1 $
 */

public class StringUtil {

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

}