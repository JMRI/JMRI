package jmri.jmrix.marklin;

import jmri.implementation.AbstractSensor;

/**
 * Implement a Sensor via Marklin communications.
 * <p>
 * This object doesn't listen to the Marklin communications. This is because the
 * sensor manager will handle all the messages as some sensor updates will come
 * bundled together in one message. It also saves having multiple sensor beans
 * each having to decoder the same message which would be better off being done
 * in one location.
 *
 * @author Kevin Dickerson (C) 2009
 */
public class MarklinSensor extends AbstractSensor {

    public MarklinSensor(String systemName, String userName) {
        super(systemName, userName);
        init(systemName);
    }

    public MarklinSensor(String systemName) {
        super(systemName);
        init(systemName);
    }

    private void init(String id) {
    }

    @Override
    public void requestUpdateFromLayout() {
    }

}
