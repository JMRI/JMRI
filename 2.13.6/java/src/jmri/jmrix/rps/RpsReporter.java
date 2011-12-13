// RpsReporter.java

package jmri.jmrix.rps;

import javax.vecmath.*;
import java.util.ArrayList;

import jmri.implementation.AbstractReporter;

/**
 * RPS implementation of the Reporter interface.
 * <P>
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 * @since 2.3.1
 */
public class RpsReporter extends AbstractReporter implements MeasurementListener {

    public RpsReporter(String systemName) {
        super(systemName);
        // create Region from all but prefix
        region = new Region(systemName.substring(2,systemName.length()));
        Model.instance().addRegion(region);
    }

    public RpsReporter(String systemName, String userName) {
        super(systemName, userName);
       // create Region from all but prefix
        region = new Region(systemName.substring(2,systemName.length()));
        Model.instance().addRegion(region);
    }

    public void notify(Measurement r) {
        Point3d p = new Point3d(r.getX(), r.getY(), r.getZ());
        Integer id = Integer.valueOf(r.getReading().getID());
        
        // ignore if code not OK
        if (!r.isOkPoint()) return;
        
        // ignore if not in Z fiducial volume
        if (r.getZ() > 20 || r.getZ() < -20) return;
        
        if (log.isDebugEnabled()) log.debug("starting "+getSystemName());
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
    
    private static final long serialVersionUID = 1L;
    
    transient Region region;
    ArrayList<Integer> contents = new ArrayList<Integer>();
    
    /**
     * Notify parameter listeners that 
     * a device has left the region covered by
     * this sensor
     */
    void notifyLeaving(Integer id) {
        firePropertyChange("Leaving", null, id);
        setReport("");
    }
    
    /**
     * Notify parameter listeners that 
     * a device has entered the region covered by
     * this sensor
     */
    void notifyArriving(Integer id) {
        firePropertyChange("Arriving", null, id);
        setReport(""+id);
    }
    
    /**
     * Numerical state is the number of 
     * transmitters in the region
     */
    public int getState() {
        return contents.size();
    }
    public void setState(int i) {}
        
    public void dispose() {
        Model.instance().removeRegion(region);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RpsReporter.class.getName());
 }

/* @(#)AbstractReporter.java */
