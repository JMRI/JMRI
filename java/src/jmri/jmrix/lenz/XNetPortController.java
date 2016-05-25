// XNetPortController.java
package jmri.jmrix.lenz;

/**
 * interface for XNetPortController objects.
 * <p>
 *
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */
public interface XNetPortController extends jmri.jmrix.PortAdapter {

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    public boolean status();

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     */
    public boolean okToSend();

    /**
     * We need a way to say if the output buffer is empty or not
     */
    public void setOutputBufferEmpty(boolean s);

}


/* @(#)XNetPortController.java */
