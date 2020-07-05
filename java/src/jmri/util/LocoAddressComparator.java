package jmri.util;

import java.util.Comparator;
import jmri.LocoAddress;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Comparator for LocoAddress objects.
 * <p>
 * Compares objects based on protocol and then address.
 *
 * @author Paul Bender Copyright (C) 2015
 */
@API(status = EXPERIMENTAL)
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
