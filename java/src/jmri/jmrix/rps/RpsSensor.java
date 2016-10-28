package jmri.jmrix.rps;

import java.util.ArrayList;
import javax.vecmath.Point3d;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for RPS systems
 * <P>
 * System names are "RSpppp", where ppp is a representation of the region, for
 * example "RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)".
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class RpsSensor extends AbstractSensor
        implements MeasurementListener {

    public RpsSensor(String systemName) {
        super(systemName);
        // create Region from all but prefix
        region = new Region(systemName.substring(2, systemName.length()));
        Model.instance().addRegion(region);
    }

    public RpsSensor(String systemName, String userName) {
        super(systemName, userName);
        // create Region from all but prefix
        region = new Region(systemName.substring(2, systemName.length()));
        Model.instance().addRegion(region);
    }

    public void notify(Measurement r) {
        Point3d p = new Point3d(r.getX(), r.getY(), r.getZ());
        Integer id = Integer.valueOf(r.getReading().getID());

        // ignore if code not OK
        if (!r.isOkPoint()) {
            return;
        }

        // ignore if not in Z fiducial volume
        if (r.getZ() > 20 || r.getZ() < -20) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("starting " + getSystemName());
        }
        if (region.isInside(p)) {
            notifyInRegion(id);
        } else {
            notifyOutOfRegion(id);
        }
        if (contents.size() > 0) {
            setOwnState(Sensor.ACTIVE);
        } else {
            setOwnState(Sensor.INACTIVE);
        }
    }

    // if somebody outside sets state to INACTIVE, clear list
    public void setOwnState(int state) {
        if (state == Sensor.INACTIVE) {
            if (contents.size() > 0) {
                contents = new ArrayList<Integer>();
            }
        }
        super.setOwnState(state);
    }

    Region getRegion() {
        return region;
    }

    java.util.List<Integer> getContents() {
        return contents;
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
     * this sensor
     */
    void notifyLeaving(Integer id) {
        firePropertyChange("Leaving", null, id);
    }

    /**
     * Notify parameter listeners that a device has entered the region covered
     * by this sensor
     */
    void notifyArriving(Integer id) {
        firePropertyChange("Arriving", null, id);
    }

    public void dispose() {
        Model.instance().removeRegion(region);
    }

    public void requestUpdateFromLayout() {
    }

    private final static Logger log = LoggerFactory.getLogger(RpsSensor.class.getName());

}

/* @(#)RpsSensor.java */
