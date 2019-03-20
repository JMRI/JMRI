package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Lightweight class to denote that a system is active and provide general
 * information
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EliteXNetSystemConnectionMemo extends XNetSystemConnectionMemo {

    public EliteXNetSystemConnectionMemo(XNetTrafficController xt) {
        super(xt);
    }

    public EliteXNetSystemConnectionMemo() {
        super();
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.ConsistManager.class)) {
            return false;
        } else {
            return super.provides(type); // defer to the superclass.
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(EliteXNetSystemConnectionMemo.class);

}
