// RpsSensor.java

package jmri.jmrix.rps;

import jmri.AbstractSensor;
import jmri.Sensor;

import javax.vecmath.*;
import java.util.ArrayList;

/**
 * Extend jmri.AbstractSensor for RPS systems
 * <P>
 * System names are "RSpppp", where ppp is a 
 * representation of the region, for example
 * "RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)".
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version     $Revision: 1.2 $
 */
public class RpsSensor extends AbstractSensor
                    implements MeasurementListener {

    public RpsSensor(String systemName) {
        super(systemName);
        // create Region from all but prefix
        region = new Region(systemName.substring(2,systemName.length()));
        Model.instance().addRegion(region);
    }

    public RpsSensor(String systemName, String userName) {
        super(systemName, userName);
       // create Region from all but prefix
        region = new Region(systemName.substring(2,systemName.length()));
        Model.instance().addRegion(region);
    }

    public void notify(Measurement r) {
        Point3d p = new Point3d(r.getX(), r.getY(), r.getZ());
        Integer id = new Integer(r.getReading().getID());
        
        // ignore if code not OK
        if (r.getCode() > 0 ) return;
        
        // ignore if not in Z fiducial volume
        if (r.getZ() > 20 || r.getZ() < -20) return;
        
        if (log.isDebugEnabled()) log.debug("starting "+getSystemName());
        if (region.isInside(p)) {
            notifyInRegion(id);
        } else {
            notifyOutOfRegion(id);
        }
        if (contents.size() > 0) setOwnState(Sensor.ACTIVE);
        else setOwnState(Sensor.INACTIVE);
    }

    Region getRegion() {
        return region;
    }
    
    java.util.List getContents() {
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
    
    Region region;
    ArrayList contents = new ArrayList();
    
    /**
     * Notify parameter listeners that 
     * a device has left the region covered by
     * this sensor
     */
    void notifyLeaving(Integer id) {
        firePropertyChange("Leaving", null, id);
    }
    
    /**
     * Notify parameter listeners that 
     * a device has entered the region covered by
     * this sensor
     */
    void notifyArriving(Integer id) {
        firePropertyChange("Arriving", null, id);
    }
    
    public void dispose() {
        Model.instance().removeRegion(region);
    }

    public void requestUpdateFromLayout() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsSensor.class.getName());

}

/* @(#)RpsSensor.java */
