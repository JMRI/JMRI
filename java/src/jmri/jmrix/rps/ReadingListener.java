package jmri.jmrix.rps;

/**
 * Connect to a source of Readings.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public interface ReadingListener {

    void notify(Reading r);

}
