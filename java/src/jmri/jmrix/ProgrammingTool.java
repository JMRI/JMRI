package jmri.jmrix;

import jmri.Programmer;
import jmri.jmrit.roster.RosterEntry;

/**
 * An interface to allow for "callback" operations to open a symbolic programmer from connection tools.
 *
 * @author B. Milhaupt Copyright (c) 2020
 */
public interface ProgrammingTool {
    /**
     * Open a symbolic programmer for the device in the roster entry.
     *
     * @param re Roster Entry of the device to be programmed
     * @param name name of the device to be programmed
     * @param programmerFile the programmer file
     * @param p the programmer
     */
    public void openPaneOpsProgFrame(RosterEntry re, String name,
                                     String programmerFile, Programmer p);

}
