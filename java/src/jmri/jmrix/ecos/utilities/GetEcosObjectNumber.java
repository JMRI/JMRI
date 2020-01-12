package jmri.jmrix.ecos.utilities;


/**
 * This method simply returns a integer value from a string, that is between
 * two given character positions.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class GetEcosObjectNumber {

    /**
     * @param s Name of this action, e.g. in menus
     */
    static public int getEcosObjectNumber(String s, String start, String finish) {
        int intStart = 0;
        int intEnd;
        if (start != null) {
            intStart = s.indexOf(start) + start.length();
        }
        if (finish == null) {
            intEnd = s.length();
        } else {
            /* Make sure that the finish substring is searched for only after the start substring appears in s */
            String s2 = s.substring(intStart, s.length());
            intEnd = s2.indexOf(finish) + intStart;
        }
        int object = Integer.parseInt(s.substring(intStart, intEnd));
        return object;
    }

}
