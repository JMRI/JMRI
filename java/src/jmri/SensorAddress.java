package jmri;

/**
 * Object to handle "user" and "system" sensor addresses.
 * SensorManager is primary consumer of these
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SensorAddress extends Address {

    public SensorAddress(String system, String user) {
        super(system, user);
    }

    /**
     * Both names are the same in this ctor
     */
    public SensorAddress(String name) {
        super(name);
    }
}
