package jmri.jmrix.rfid;

import java.util.ArrayList;
import jmri.IdTag;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for RFID systems
 * <p>
 * System names are "FSpppp", where ppp is a representation of the RFID reader.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class RfidSensor extends AbstractSensor
        implements RfidTagListener {

    public RfidSensor(String systemName) {
        super(systemName);
    }

    public RfidSensor(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    public void notify(IdTag r) {
        setOwnState(r != null ? Sensor.ACTIVE : Sensor.INACTIVE);
    }

    // if somebody outside sets state to INACTIVE, clear list
    @Override
    public void setOwnState(int state) {
        if (state == Sensor.INACTIVE) {
            if (contents.size() > 0) {
                contents = new ArrayList<>();
            }
        }
        super.setOwnState(state);
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

//    transient Region region;
    ArrayList<Integer> contents = new ArrayList<>();

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

    @Override
    public void dispose() {
//        Model.instance().removeRegion(region);
    }

    @Override
    public void requestUpdateFromLayout() {
    }

}
