package jmri.util;

import java.util.Comparator;
import jmri.LocoAddress;

/**
 * Comparator for LocoAddress objects.
 * <P>
 * Compares objects based on protocol and then address.
 *
 * @author	Paul Bender Copyright (C) 2015
 */
public class LocoAddressComparator implements Comparator<LocoAddress> {

    public LocoAddressComparator() {
    }

    @Override
    public int compare(LocoAddress l1, LocoAddress l2) {
         if( l1.getProtocol() == l2.getProtocol() ){
             // protocol is the same, compare the number fields
             return Integer.signum(l1.getNumber() - l2.getNumber());
         } else {
             return Integer.signum((l1.getProtocol().getShortName().compareTo(l2.getProtocol().getShortName())));
         }
    }
}
