// RpsReporterManager.java
package jmri.jmrix.rps;

import jmri.Reporter;
import jmri.managers.AbstractReporterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPS implementation of a ReporterManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 * @since 2.3.1
 */
public class RpsReporterManager extends AbstractReporterManager {

    public String getSystemPrefix() {
        return "R";
    }

    protected Reporter createNewReporter(String systemName, String userName) {
        RpsReporter r = new RpsReporter(systemName, userName);
        Distributor.instance().addMeasurementListener(r);
        return r;
    }

    static public RpsReporterManager instance() {
        if (_instance == null) {
            _instance = new RpsReporterManager();
        }
        return _instance;
    }

    static RpsReporterManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(RpsReporterManager.class.getName());

    static { // class initialization
        // now want a ReporterManager always, not just when RPS is created
        if (_instance == null) {
            _instance = new RpsReporterManager();
            jmri.InstanceManager.setReporterManager(jmri.jmrix.rps.RpsReporterManager.instance());
            // log.warn("Setting RpsSensorManager instance at startup time!");
        }
    }
}

/* @(#)Rps ReporterManager.java */
