package jmri.implementation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.DccLocoAddress;
import jmri.IdTag;
import jmri.IdTagListener;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.PhysicalLocationReporter;
import jmri.RailCom;
import jmri.ReporterManager;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend AbstractReporter for RailCom reporters
 * <P>
 * This file is based on @{link jmri.jmrix.rfid.RfidReporter}
 * <P>
 *
 * @author Matthew Harris Copyright (c) 2011
 * @author Paul Bender Copyright (c) 2016
 * @since 4.5.4
 */
public class AbstractRailComReporter extends AbstractIdTagReporter {

    public AbstractRailComReporter(String systemName) {
        super(systemName);
    }

    public AbstractRailComReporter(String systemName, String userName) {
        super(systemName, userName);
    }

    // Methods to support PhysicalLocationReporter interface
    /**
     * getLocoAddress()
     *
     * get the locomotive address we're reporting about from the current report.
     *
     * Note: We ignore the string passed in, because RailCom Reporters don't send
     * String type reports.
     */
    @Override
    public LocoAddress getLocoAddress(String rep) {
        // For now, we assume the current report.
        // IdTags passed by RailCom reporters are actually jmri.RailCom objects
        // so we use properties of the RailCom object here.
        RailCom cr = (RailCom) this.getCurrentReport();
        return cr.getDccLocoAddress();
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractRailComReporter.class);

}
