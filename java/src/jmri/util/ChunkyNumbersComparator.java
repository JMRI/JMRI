package jmri.util;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * A comparator for mixed Strings containing a mix of words and numbers:  "ATSF 123",
 * "CN 100B", etc.
 *
 * Regex implementation first, but that might be too slow
 */

public class ChunkyNumbersComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        
        var m1 = sortPattern.matcher(s1);
        var m2 = sortPattern.matcher(s2);
       
        if (!m1.find() || !m2.find()) {
            log.warn("Failed to parse '{}' '{}'", s1, s2);
            return s1.compareTo(s2);
        }
        
        // note increment by 2, because of two cases inside the loop
        for (int index = 1; index < 6; index = index + 2) {
        
            // check first alpha group
            String g1 = m1.group(index);
            String g2 = m2.group(index);
            int len1 = g1.length();
            int len2 = g2.length();
            
            // Is there at least one letter group?
            if (len1 > 0 || len2 > 0) {
                int result = g1.compareTo(g2);
                if (result !=0) {
                    return result;
                }
            }
    
            // check number group
            g1 = m1.group(index+1);
            g2 = m2.group(index+1);
            
            boolean g1e = g1.isEmpty();
            boolean g2e = g2.isEmpty();
            if (g1e && g2e) {
                // no match, so done (can't be another letter group, would have been picked up above)
                return 0;
            }
            if (g1e) {
                return -1;
            }
            if (g2e) {
                return +1;
            }
            
            // Here an number group in each input, parse as numbers and compare
            Long v1;
            Long v2;
            try {
                v1 = Long.valueOf(g1);
                v2 = Long.valueOf(g2);
            } catch (NumberFormatException e1) {
                log.warn("Comparison should not have reached here with {} {}", g1, g2);
                return s1.compareTo(s2);
            }
            // check for parsed numbers
            int result = v1.compareTo(v2);
            if (result !=0) {
                return result;
            }  

            // reached here, go around again
        }
        // reached here, didn't find a difference
        return 0;
    }

    static final Pattern sortPattern = Pattern.compile("([^0-9]*)([0-9]*)([^0-9]*)([0-9]*)([^0-9]*)([0-9]*)");
    
    //private final boolean isDigit(char ch) {
    //    return (('0' <= ch) && (ch <= '9'));
    //}

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChunkyNumbersComparator.class);

}
