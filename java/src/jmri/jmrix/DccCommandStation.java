package jmri.jmrix;

/**
 * This turned out to be a dead-end interface, which has now been deprecated.
 * {@link jmri.CommandStation} is the replacement.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2013
 * @deprecated Since JMRI 4.11.3, will remove in following development series;
 * use {@link jmri.CommandStation} instead.
 */
@Deprecated
public interface DccCommandStation {

    /**
     * Does this command station have a "service mode", where it stops normal
     * train operation while programming?
     */
    public boolean getHasServiceMode();

    /**
     * If this command station has a service mode, is the command station
     * currently in that mode?
     */
    public boolean getInServiceMode();

    /**
     * Provides an-implementation specific version string from the command
     * station. In general, this should be as close as possible to what the
     * command station replied when asked; it should not be reformatted
     *
     */
    public String getVersionString();
}

