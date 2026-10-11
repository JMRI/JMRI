package jmri.implementation;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.util.PhysicalLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend AbstractReporter for IdTag reporters
 * <p>
 * This file is based on {@link jmri.jmrix.rfid.RfidReporter}
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

    /**
     * Indicates whether the underlying hardware sends messages determining
     * when an Id is no longer in this reporter (e.g. RailCom block occupancy exit events).
     * <p>
     * When {@code false} (e.g. RFID point detectors), seeing a tag elsewhere implies
     * it has departed this reporter.
     * When {@code true}, departure is strictly governed by hardware exit/clear messages.
     *
     * @return true if hardware sends explicit exit/departure messages; false otherwise.
     */
    public boolean hasExitReports() {
        return false;
    }

    private String lastReportString = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(IdTag id) {
        log.debug("Notify: {}", mSystemName);
        String oldReportString = lastReportString;
        String newReportString = null;
        if (id != null) {
            newReportString = (id instanceof Reportable) ? ((Reportable) id).toReportString() : id.toString();
        }
        lastReportString = newReportString;
        // These are both nullable.
        if (!Objects.equals(oldReportString, newReportString)) {
            firePropertyChange(PROPERTY_REPORT_METADATA, oldReportString, newReportString);
        }
        if (id != null) {
            log.debug("Tag {} notified in {}", id, this);
            // do not update last reporter and last seen if this is an "exit" report
            // Only happens on LocoNet transponding reports
            var entryexit = id.getProperty("entryexit");
            if (entryexit == null || !entryexit.equals("exits")) {
                Reporter r = id.getWhereLastSeen();
                id.setWhereLastSeen(this);
                if (r != null && !r.equals(this)) {
                    log.trace("{} notifySeenElsewhere on {}", id, r);
                    r.notifySeenElsewhere(id, this);
                }
                log.trace("{} last seen here: {}", id, this.mSystemName);
            } else {
                log.trace("{} skipping setWhereLastSeen on {} exits report", this, id);
            }
        }
        setReport(id);
        setState(id != null ? IdTag.SEEN : IdTag.UNSEEN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReport(Object r) {
        super.setReport(r);
        if (r == null) {
            lastReportString = null;
        } else if (r instanceof Reportable) {
            lastReportString = ((Reportable) r).toReportString();
        } else {
            lastReportString = r.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySeenElsewhere(IdTag tag, Reporter newReporter) {
        if (!hasExitReports() && !this.equals(newReporter) && getCurrentReport() == tag) {
            notify(null);
        }
    }

    private int state = UNKNOWN;

    /** {@inheritDoc} */
    @Override
    public void setState(int s) {
        state = s;
    }

    /** {@inheritDoc} */
    @Override
    public int getState() {
        return state;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String describeState(int state) {
        switch (state) {
            case IdTag.SEEN:
                return Bundle.getMessage("IdTagReporterStateSeen");
            case IdTag.UNSEEN:
                return Bundle.getMessage("IdTagReporterStateUnSeen");
            default:
                return super.describeState(state);
        }
    }

    // Methods to support PhysicalLocationReporter interface

    /**
     * Get the locomotive address we're reporting about from the current report.
     * {@inheritDoc}
     * @param rep ignored, IdTag Reporters don't send String type reports.
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
            log.debug("Parsed address: {}", m.group(1));
            // I have no idea what kind of loco address an Ecos reporter uses,
            // so we'll default to DCC for now.
            return (new DccLocoAddress(Integer.parseInt(m.group(1)), LocoAddress.Protocol.DCC));
        } else {
            return (null);
        }
    }

    /**
     * Gets the direction (ENTER/EXIT) of the report.
     * <p>
     * Because of the way
     * IdTag Reporters work, all reports are ENTER type.
     * {@inheritDoc}
     */
    @Override
    public PhysicalLocationReporter.Direction getDirection(String rep) {
        // TEMPORARY:  Assume we're always Entering, if asked.
        return (PhysicalLocationReporter.Direction.ENTER);
    }

    /**
     * Get the PhysicalLocation of the Reporter
     *
     * Reports its own location, for now.
     * Not sure if that's the right thing or
     * not. NOT DONE YET
     *
     * {@inheritDoc}
     */
    @Override
    public PhysicalLocation getPhysicalLocation() {
        return (this.getPhysicalLocation(null));
    }

    /**
     * Get the PhysicalLocation of the Reporter.
     *
     * {@inheritDoc}
     * @param s unused.
     */
    @Override
    public PhysicalLocation getPhysicalLocation(String s) {
        return (PhysicalLocation.getBeanPhysicalLocation(this));
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractIdTagReporter.class);

}
