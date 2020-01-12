package jmri.implementation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.DccLocoAddress;
import jmri.IdTag;
import jmri.IdTagListener;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.PhysicalLocationReporter;
import jmri.ReporterManager;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend AbstractReporter for IdTag reporters
 * <p>
 * This file is based on @{link jmri.jmrix.rfid.RfidReporter}
 *
 * @author Matthew Harris Copyright (c) 2011
 * @author Paul Bender Copyright (c) 2016, 2019
 * @since 4.15.3
 */
public class AbstractIdTagReporter extends AbstractReporter
        implements IdTagListener, PhysicalLocationReporter {

    public AbstractIdTagReporter(String systemName) {
        super(systemName);
    }

    public AbstractIdTagReporter(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    public void notify(IdTag id) {
        log.debug("Notify: {}",mSystemName);
        if (id != null) {
            log.debug("Tag: {}",id);
            AbstractIdTagReporter r;
            if ((r = (AbstractIdTagReporter) id.getWhereLastSeen()) != null) {
                log.debug("Previous reporter: {}",r.mSystemName);
                if (!(r.equals(this)) && r.getCurrentReport() == id) {
                    log.debug("Notify previous");
                    r.notify(null);
                } else {
                    log.debug("Current report was: {}",r.getCurrentReport());
                }
            }
            id.setWhereLastSeen(this);
            log.debug("Seen here: {}",this.mSystemName);
        }
        setReport(id);
        setState(id != null ? IdTag.SEEN : IdTag.UNSEEN);
    }

    private int state = UNKNOWN;

    @Override
    public void setState(int s) {
        state = s;
    }

    @Override
    public int getState() {
        return state;
    }

    // Methods to support PhysicalLocationReporter interface
    /**
     * getLocoAddress()
     *
     * get the locomotive address we're reporting about from the current report.
     *
     * Note: We ignore the string passed in, because IdTag Reporters don't send
     * String type reports.
     */
    @Override
    public LocoAddress getLocoAddress(String rep) {
        // For now, we assume the current report.
        // IdTag.getTagID() is a system-name-ized version of the loco address. I think.
        // Matcher.group(1) : loco address (I think)
        IdTag cr = (IdTag) this.getCurrentReport();
        ReporterManager rm = InstanceManager.getDefault(jmri.ReporterManager.class);
        Pattern p = Pattern.compile("" + rm.getSystemPrefix() + rm.typeLetter() + "(\\d+)");
        Matcher m = p.matcher(cr.getTagID());
        if (m.find()) {
            if(log.isDebugEnabled()) {
                log.debug("Parsed address: {}", m.group(1));
            }
            // I have no idea what kind of loco address an Ecos reporter uses,
            // so we'll default to DCC for now.
            return (new DccLocoAddress(Integer.parseInt(m.group(1)), LocoAddress.Protocol.DCC));
        } else {
            return (null);
        }
    }

    /**
     * getDirection()
     *
     * Gets the direction (ENTER/EXIT) of the report. Because of the way 
     * IdTag Reporters work, all reports are ENTER type.
     */
    @Override
    public PhysicalLocationReporter.Direction getDirection(String rep) {
        // TEMPORARY:  Assume we're always Entering, if asked.
        return (PhysicalLocationReporter.Direction.ENTER);
    }

    /**
     * getPhysicalLocation()
     *
     * Returns the PhysicalLocation of the Reporter
     *
     * Reports its own location, for now. Not sure if that's the right thing or
     * not. NOT DONE YET
     */
    @Override
    public PhysicalLocation getPhysicalLocation() {
        return (this.getPhysicalLocation(null));
    }

    /**
     * getPhysicalLocation(String s)
     *
     * Returns the PhysicalLocation of the Reporter
     *
     * Does not use the parameter s
     */
    @Override
    public PhysicalLocation getPhysicalLocation(String s) {
        return (PhysicalLocation.getBeanPhysicalLocation(this));
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractIdTagReporter.class);

}
