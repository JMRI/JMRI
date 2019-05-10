package jmri.jmrix.rps;

import java.util.ArrayList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.PhysicalLocationReporter;
import jmri.implementation.AbstractReporter;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPS implementation of the Reporter interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
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
        if (region.isInside(p)) {
            notifyInRegion(id);
        } else {
            notifyOutOfRegion(id);
        }
    }

    void notifyInRegion(Integer id) {
        // make sure region contains this Reading.getId();
        if (!contents.contains(id)) {
            contents.add(id);
            notifyArriving(id);
        }
    }

    void notifyOutOfRegion(Integer id) {
        // make sure region does not contain this Reading.getId();
        if (contents.contains(id)) {
            contents.remove(id);
            notifyLeaving(id);
        }
    }

    transient Region region;
    ArrayList<Integer> contents = new ArrayList<Integer>();

    /**
     * Notify parameter listeners that a device has left the region covered by
     * this sensor.
     */
    void notifyLeaving(Integer id) {
        firePropertyChange("Leaving", null, id);
        setReport("");
    }

    /**
     * Notify parameter listeners that a device has entered the region covered
     * by this sensor.
     */
    void notifyArriving(Integer id) {
        firePropertyChange("Arriving", null, id);
        setReport("" + id);
    }

    /**
     * Numerical state is the number of transmitters in the region.
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
    }

    // Methods to support PhysicalLocationReporter interface

    /**
     * Parses out a (possibly old) RpsReporter-generated report string to
     * extract the address from the front. Assumes the RpsReporter format is
     * "NNNN".
     */
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

    /**
     * Get the direction (ENTER/EXIT) of the report.
     * <p>
     * Because of the way Ecos Reporters work (or appear to), all reports are ENTER type.
     */
    public PhysicalLocationReporter.Direction getDirection(String rep) {
        // The RPS reporter only sends a report on entry.
        return (PhysicalLocationReporter.Direction.ENTER);
    }

    /**
     * Get the PhysicalLocation of the Reporter
     *
     * Reports its own location, for now. Not sure if that's the right thing or
     * not. Would be nice if it reported the exact measured location of the
     * transmitter, but right now that doesn't appear to be being stored
     * anywhere retrievable. NOT DONE YET
     */
    public PhysicalLocation getPhysicalLocation() {
        return (PhysicalLocation.getBeanPhysicalLocation(this));
    }

    /**
     * Get the PhysicalLocation of the Transmitter for a given ID
     *
     * Given an ID (in String form), looks up the Transmitter and gets its
     * current PhysicalLocation (translated from the RPS Measurement).
     */
    public PhysicalLocation getPhysicalLocation(String s) {
        if (s.length() > 0) {
            try {
                int id = Integer.parseInt(s);
                Vector3d v = Engine.instance().getTransmitter(id).getLastMeasurement().getVector();
                return (new PhysicalLocation(new Vector3f(v)));
            } catch (NumberFormatException e) {
                return (null);
            } catch (NullPointerException e) {
                return (null);
            }
        } else {
            return (null);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RpsReporter.class);

}
