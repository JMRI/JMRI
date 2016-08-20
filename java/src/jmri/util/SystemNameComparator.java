package jmri.util;

import java.util.Comparator;

/**
 * Comparator for JMRI System Names.
 * <P>
 * A System Name is two letters followed by either an alpha name or a number. In
 * the number case, this does a numeric comparison. If the number is appended
 * with letters, does the numeric sort on the digits followed by a lexigraphic
 * sort on the remainder.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @author Howard Penny
 * @author Pete Cressman
 */
public class SystemNameComparator implements Comparator<Object>, java.io.Serializable {

    public SystemNameComparator() {
    }

    public int compare(Object o1, Object o2) {
        if (o1.toString().length() <= 3 && o2.toString().length() <= 3) {
            return o1.toString().compareTo(o2.toString());
        } else if (!o1.toString().regionMatches(0, o2.toString(), 0, 2)) {
            return o1.toString().compareTo(o2.toString());
        } else {
            // extract length of digits
            char[] ch1 = o1.toString().substring(2).toCharArray();
            char[] ch2 = o2.toString().substring(2).toCharArray();
            int numDigit1 = 0;
            int numDigit2 = 0;
            for (int i = 0; i < ch1.length; i++) {
                if (Character.isDigit(ch1[i])) {
                    numDigit1++;
                } else {
                    break;
                }
            }
            for (int i = 0; i < ch2.length; i++) {
                if (Character.isDigit(ch2[i])) {
                    numDigit2++;
                } else {
                    break;
                }
            }
            if (numDigit1 == numDigit2) {
                try {
                    int diff = Integer.parseInt(new String(ch1, 0, numDigit1))
                            - Integer.parseInt(new String(ch2, 0, numDigit2));
                    if (diff != 0) {
                        return diff;
                    }
                    if (numDigit1 == ch1.length && numDigit2 == ch2.length) {
                        return diff;
                    } else {
                        if (numDigit1 == ch1.length) {
                            return -1;
                        }
                        // both have non-digit chars remaining
                        return new String(ch1, numDigit1, ch1.length - numDigit1).compareTo(
                                new String(ch2, numDigit2, ch2.length - numDigit2));
                    }
                } catch (NumberFormatException nfe) {
                    return o1.toString().compareTo(o2.toString());
                } catch (IndexOutOfBoundsException ioob) {
                    return o1.toString().compareTo(o2.toString());
                }
            } else {
                return (numDigit1 - numDigit2);
            }
        }
    }
}
