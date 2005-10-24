// StringUtil.java

package jmri.util;

import java.util.Collection;
import java.util.Iterator;

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
 * @version $Revision: 1.14 $
 */

public class StringUtil {

static char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' } ;

    /**
     * Convert an int to a exactly two hexadecimal characters
     * @param val
     * @return String exactly two characters long
     */
    static public String twoHexFromInt(int val) {
        StringBuffer sb = new StringBuffer() ;
	    sb.append( hexChars[ (val&0xF0) >> 4 ] );
		sb.append( hexChars[ val & 0x0F ] ) ;
		return sb.toString() ;
    }

    /**
     * Quickly append an int to a String as exactly two hexadecimal characters
     * @param val Value to append in hex
     * @param inString String to be extended
     * @return String exactly two characters long
     */
    static public String appendTwoHexFromInt(int val, String inString) {
        StringBuffer sb = new StringBuffer(inString) ;
	    sb.append( hexChars[ (val&0xF0) >> 4 ] );
		sb.append( hexChars[ val & 0x0F ] );
		return sb.toString() ;
    }

    /**
     * Create a String containing hexadecimal values from a byte[]. 
     *
     * @param bytes byte array.  Can be zero length,
     *  but must not be null.
     * @return String of hex values, ala "01 02 0A B1 21 ".
     */
    static public String hexStringFromBytes(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i<bytes.length; i++) {
	        sb.append( hexChars[ (bytes[i]&0xF0) >> 4 ] );
		    sb.append( hexChars[ bytes[i] & 0x0F ] );
		    sb.append(' ');
        }
        return new String(sb);
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
        } catch (Throwable e1) {  // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            bubblesort(values);
        }
    }

    static void bubblesort(String[] values) {
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

    static void bubblesort(Object[] values) {
        for (int i=0; i<=values.length-2; i++) { // stop sort early to save time!
            for (int j=values.length-2; j>=i; j--) {
                // check that the jth value is smaller than j+1th,
                // else swap
                if (0 < (values[j].toString()).compareTo(values[j+1].toString())) {
                    // swap
                    Object temp = values[j];
                    values[j] = values[j+1];
                    values[j+1] = temp;
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
        } catch (Throwable e) { // NoSuchMethodError, NoClassDefFoundError and others on early JVMs
            // no Java sort, so ugly bubble sort
            bubblesort(values);
        }
    }

    static void bubblesortUpper(Object[] values) {
        for (int i=0; i<=values.length-2; i++) { // stop sort early to save time!
            for (int j=values.length-2; j>=i; j--) {
                // check that the jth value is smaller than j+1th,
                // else swap
                if (0 < (values[j].toString().toUpperCase()).compareTo(values[j+1].toString().toUpperCase())) {
                    // swap
                    Object temp = values[j];
                    values[j] = values[j+1];
                    values[j+1] = temp;
                }
            }
        }
    }

    /**
     * This is a case-independent lexagraphic sort.
     * Identical entries are retained, so the output length is the same
     * as the input length.
     * @param values
     */
    static public void sortUpperCase(Object[] values) {
        // no Java sort, so ugly bubble sort
        bubblesortUpper(values);
    }
    
    /**
     * Join a collection of strings, separated by a delimiter
     * @param s	collection of strings
     * @param delimiter
     * @return e.g. join({"abc","def,"ghi"}, ".") ==> "abc.def.ghi"
     */
    public static String join(Collection s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
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
     * @param s	collection of strings
     * @param delimiter
     * @return e.g. join({"abc","def,"ghi"}, ".") ==> "abc.def.ghi"
     */
    public static String join(String[] s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<s.length; i++) {
            buffer.append(s[i]);
            if (i<s.length-1) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
    /**
     * Provide a version of String replaceAll() that also works on Java 1.1.8
     */
     static public String replaceAll(String input, String find, String replace) {
        try {
            return input.replaceAll(find, replace);
        } catch (Throwable t) {
            // not available, do the hard way
            return localReplaceAll(input, find, replace);
        }
     }

     static String localReplaceAll(String input, String find, String replace) {
        String local = input;
        String output = "";
        int loc;
        while ( (loc = local.indexOf(find)) >= 0) {
            // found string, so have to do replacement
            output = output+local.substring(0,loc)+replace;
            local = local.substring(loc+find.length(), local.length());
        }
        return output+local;
     }

    /**
     * Split a string into an array of Strings, at a particular
     * divider.  This is similar to the new String.split method,
     * except that this does not provide regular expression
     * handling; the divider string is just a string.
     * @param input String to split
     * @param divider Where to divide the input; this does not appear in output
     */
    static public String[] split(String input, String divider) {
        int size = 0;
        String temp = input;
        
        // count entries
        while (temp.length() > 0) {
            size++;
            int index = temp.indexOf(divider);
            if (index < 0) break;    // break not found
            temp = temp.substring(index+divider.length());
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
            if (index < 0) break;    // done with all but last
            result[size] = temp.substring(0,index);
            temp = temp.substring(index+divider.length());
            size++;
        }
        result[size] = temp;
        
        return result;
    }
    
}