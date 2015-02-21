// ReadingListener.java
package jmri.jmrix.rps;

/**
 * Connect to a source of Readings
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version	$Revision$
 */
public interface ReadingListener {

    public void notify(Reading r);

}

/* @(#)ReadingListener.java */
