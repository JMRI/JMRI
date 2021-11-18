package jmri.jmrix.rps;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.PhysicalLocation;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.implementation.AbstractReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPS implementation of the Reporter interface.
 *
 * @author Bob Jacobsen      Copyright (C) 2008
 * @author Daniel Bergqvist  Copyright (C) 2021
 * @since 2.3.1
 */
public class RpsReporter extends AbstractReporter implements MeasurementListener {

    public RpsReporter(String systemName, String prefix) {
        super(systemName);
        // create Region from all but prefix
        region = new Region(systemName.substring(prefix.length() + 1)); // multichar prefix from memo
        Model.instance().addRegion(region);
    }

    public RpsReporter(String systemName, String userName, String prefix) {
        super(systemName, userName);
        // create Region from all but prefix
        region = new Region(systemName.substring(prefix.length() + 1)); // multichar prefix from memo
        Model.instance().addRegion(region);
    }

    @Override
    public boolean isExtendedReportsSupported() {
        return true;
    }

    @Override
    public void notify(Measurement r) {
        Point3d p = new Point3d(r.getX(), r.getY(), r.getZ());
        Integer id = Integer.valueOf(r.getReading().getId());

        // ignore if code not OK
        if (!r.isOkPoint()) {
            return;
        }

        // ignore if not in Z fiducial volume
        if (r.getZ() > 20 || r.getZ() < -20) {
            return;
        }

        log.debug("starting {}", getSystemName());

        PhysicalLocation physicalLocation =
                jmri.util.PhysicalLocation.getBeanPhysicalLocation(this);
        if (physicalLocation == jmri.PhysicalLocation.Origin) {
            physicalLocation = new jmri.util.PhysicalLocation(r.getX(), r.getY(), r.getZ());
        }

        if (region.isInside(p)) {
            RpsReport report =
                    new RpsReport(
                            id,
                            PhysicalLocationReporter.Direction.ENTER,
                            physicalLocation);
            notifyInRegion(id, report);
        } else {
            RpsReport report =
                    new RpsReport(
                            id,
                            PhysicalLocationReporter.Direction.EXIT,
                            physicalLocation);
            notifyOutOfRegion(id, report);
        }
    }

    void notifyInRegion(Integer id, RpsReport report) {
        // make sure region contains this Reading.getId();
        if (!contents.contains(id)) {
            contents.add(id);
            notifyArriving(id, report);
        }
    }

    void notifyOutOfRegion(Integer id, RpsReport report) {
        // make sure region does not contain this Reading.getId();
        if (contents.contains(id)) {
            contents.remove(id);
            notifyLeaving(id, report);
        }
    }

    transient Region region;
    List<Integer> contents = new ArrayList<>();

    /**
     * Notify parameter listeners that a device has left the region covered by
     * this reporter.
     * @param id Number of region being left
     * @param report The report
     */
    void notifyLeaving(Integer id, RpsReport report) {
        firePropertyChange("Leaving", null, id);
        firePropertyChange(Reporter.REPORT_PROPERTY, report, null);
        setExtendedReport(null);
    }

    /**
     * Notify parameter listeners that a device has entered the region covered
     * by this reporter.
     * @param id Number of region being entered
     * @param report The report
     */
    void notifyArriving(Integer id, RpsReport report) {
        firePropertyChange("Arriving", null, id);
        firePropertyChange(Reporter.REPORT_PROPERTY, null, report);
        setExtendedReport(report);
    }

    /**
     * Numerical state is the number of transmitters in the region.
     * @return number of transmitters
     */
    @Override
    public int getState() {
        return contents.size();
    }

    @Override
    public void setState(int i) {
    }

    @Override
    public void dispose() {
        Model.instance().removeRegion(region);
        super.dispose();
    }

    // Methods to support PhysicalLocationReporter interface

    /* *
     * Parses out a (possibly old) RpsReporter-generated report string to
     * extract the address from the front.
     * Assumes the RpsReporter format is "NNNN".
     * @param rep loco string.
     * @return loco address, may be null.
     * /
    public LocoAddress getLocoAddress(String rep) {
        // The report is a string, that is the ID of the locomotive (I think)
        log.debug("Parsed ID: {}", rep);
        // I have no idea what kind of loco address an RPS reporter uses,
        // so we'll default to DCC for now.
        if (rep.length() > 0) {
            try {
                int id = Integer.parseInt(rep);
                int addr = Engine.instance().getTransmitter(id).getAddress();
                return (new DccLocoAddress(addr, LocoAddress.Protocol.DCC));
            } catch (NumberFormatException e) {
                return (null);
            }
        } else {
            return (null);
        }
    }

    /* *
     * Get the direction (ENTER/EXIT) of the report.
     * <p>
     * Because of the way Ecos Reporters work (or appear to), all reports are ENTER type.
     * @param rep reporter ID in string form.
     * @return direction is always a location entrance
     * /
    public PhysicalLocationReporter.Direction getDirection(String rep) {
        // The RPS reporter only sends a report on entry.
        return (PhysicalLocationReporter.Direction.ENTER);
    }

    /* *
     * Get the PhysicalLocation of the Reporter.
     * <p>
     * Reports its own location, for now.
     * Not sure if that's the right thing or not. 
     * Would be nice if it reported the exact measured location of the
     * transmitter, but right now that doesn't appear to be being stored
     * anywhere retrievable. NOT DONE YET
     * @return PhysicalLocation.getBeanPhysicalLocation
     * /
    public PhysicalLocation getPhysicalLocation() {
        return (jmri.util.PhysicalLocation.getBeanPhysicalLocation(this));
    }

    /**
     * Get the PhysicalLocation of the Transmitter for a given ID.
     * <p>
     * Given an ID (in String form), looks up the Transmitter and gets its
     * current PhysicalLocation (translated from the RPS Measurement).
     *
     * @param s transmitter ID.
     * @return physical location.
     * /
    public PhysicalLocation getPhysicalLocation(String s) {
        if (s.length() > 0) {
            try {
                int id = Integer.parseInt(s);
                Vector3d v = Engine.instance().getTransmitter(id).getLastMeasurement().getVector();
                return (new jmri.util.PhysicalLocation(new Vector3f(v)));
            } catch (NumberFormatException e) {
                return (null);
            } catch (NullPointerException e) {
                return (null);
            }
        } else {
            return (null);
        }
    }
*/
    private final static Logger log = LoggerFactory.getLogger(RpsReporter.class);

}
