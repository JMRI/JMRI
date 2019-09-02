package jmri.jmrix.roco.z21;

import java.util.Comparator;
import java.util.Objects;

/**
 * implements a comparator for Z21 CAN bus system names, which include a
 * hex string for the node address.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class Z21SystemNameComparitor implements Comparator<String> {

    private String prefix;
    private char typeLetter;

    public Z21SystemNameComparitor(String prefix, char typeLetter){
        this.prefix= prefix;
        this.typeLetter = typeLetter;
    }

    @Override
    public int compare(String o1, String o2) {
        if(o1.equals(o2)){
            return 0;
        }
        if(o1.startsWith(prefix) && o2.startsWith(prefix) &&
                o1.charAt(prefix.length()) == typeLetter &&
                o2.charAt(prefix.length()) == typeLetter ){
            if(o1.contains(":")){
                if (o1.indexOf(':') == o2.indexOf(':')) {
                    int startIndex = prefix.length() + 1;
                    int stopIndex = o1.indexOf(':');
                    if (0 == o1.substring(startIndex, stopIndex).
                            compareToIgnoreCase(o2.substring(startIndex, stopIndex))) {
                        return o1.substring(stopIndex).
                                compareToIgnoreCase(o2.substring(stopIndex));
                    }
                }
            } else {
                return o1.substring(prefix.length()+1).
                        compareTo(o2.substring(prefix.length()+1));
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(obj.getClass().equals(this.getClass())){
            Z21SystemNameComparitor o = (Z21SystemNameComparitor)obj;
            if(this.prefix.equals(o.prefix) && this.typeLetter==o.typeLetter){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix,String.valueOf(typeLetter));
    }

}
