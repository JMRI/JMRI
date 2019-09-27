package jmri.jmrix.ecos;

import jmri.implementation.AbstractSensor;

/**
 * Implement a Sensor via ECoS communications.
 * <p>
 * This object doesn't listen to the Ecos communications. This is because it
 * should be the only object that is sending messages for this sensor; more than
 * one Sensor object pointing to a single device is not allowed.
 *
 * @author Kevin Dickerson (C) 2009
 */
public class EcosSensor extends AbstractSensor {

    int objectNumber = 0;

    public EcosSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName);
    }

    public EcosSensor(String systemName) {
        super(systemName);
        init(systemName);
    }

    private void init(String id) {
    }

    void setObjectNumber(int o) {
        objectNumber = o;
    }

    @Override
    public void requestUpdateFromLayout() {
    }

    public int getObject() {
        return objectNumber;
    }

}
