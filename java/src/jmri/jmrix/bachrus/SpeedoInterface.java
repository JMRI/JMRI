package jmri.jmrix.bachrus;

/**
 * Define interface for receiving messages from the reader.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew Crosland Copyright (C) 2010
 */
public interface SpeedoInterface {

    public void addSpeedoListener(SpeedoListener l);

    public void removeSpeedoListener(SpeedoListener l);

    /**
     * Test operational status of interface.
     *
     * @return true is interface implementation is operational.
     */
    boolean status();

}
