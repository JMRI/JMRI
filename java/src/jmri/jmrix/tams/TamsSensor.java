package jmri.jmrix.tams;

import jmri.implementation.AbstractSensor;

/**
 * Implement a Sensor via Tams communications.
 * <p>
 * This object doesn't listen to the Tams communications. This is because the
 * sensor manager will handle all the messages as some sensor updates will come
 * bundled together in one message. It also saves having multiple sensor beans
 * each having to decoder the same message which would be better off being done
 * in one location.
 *
 * @author Kevin Dickerson (C) 2009
 */
public class TamsSensor extends AbstractSensor {
    
    public TamsSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName);
    }

    public TamsSensor(String systemName) {
        super(systemName);
        init(systemName);
    }

    private void init(String id) {
    }

    @Override
    public void requestUpdateFromLayout() {
    }

}
