package jmri.jmrix.pricom.pockettester;

/**
 * Receive notification when data arrives from a Pocket Tester.
 * <p>
 * You register this listener with a DataSource object
 *
 * @see jmri.jmrix.pricom.pockettester.DataSource
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 */
public interface DataListener {

    public void asciiFormattedMessage(String m);

    // public void rawMessage(String m);
}


