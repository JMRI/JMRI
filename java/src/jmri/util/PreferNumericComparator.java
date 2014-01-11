package jmri.util;

import java.util.Comparator;

/**
 * If the two objects are Strings
 * that can be made into integers, compare like that
 * otherwise as Strings.
 *
 * @author	Bob Jacobsen   Copyright (C) 2013
 * @version	$Revision: 24569 $
 */

public class PreferNumericComparator implements Comparator<Object>, java.io.Serializable {
    public PreferNumericComparator() {}
    
    public int compare(Object oo1, Object oo2) {
                
        boolean isFirstNumeric, isSecondNumeric;
        String o1 = oo1.toString(), o2 = oo2.toString();

        isFirstNumeric = o1.matches("\\d+");
        isSecondNumeric = o2.matches("\\d+");

        if (isFirstNumeric) {
            if (isSecondNumeric) {
                return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
            } else {
                return -1; // numbers always smaller than letters
            }
        } else {
            if (isSecondNumeric) {
                return 1; // numbers always smaller than letters
            } else {
                // Neither numeric
                isFirstNumeric = o1.split("\\.")[0].matches("\\d+");
                isSecondNumeric = o2.split("\\.")[0].matches("\\d+");

                if (isFirstNumeric) {
                    if (isSecondNumeric) {
                        int intCompare = Integer.valueOf(o1.split("\\.")[0]).compareTo(Integer.valueOf(o2.split("\\.")[0]));
                        if (intCompare == 0) {
                            return o1.compareToIgnoreCase(o2);
                        }
                        return intCompare;
                    } else {
                        return -1; // numbers always smaller than letters
                    }
                } else {
                    if (isSecondNumeric) {
                        return 1; // numbers always smaller than letters
                    } else {
                        return o1.compareToIgnoreCase(o2);
                    }
                }
            }
        }
    }
}
