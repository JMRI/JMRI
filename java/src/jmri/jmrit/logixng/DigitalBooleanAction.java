package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * A LogixNG logix emulator action.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalBooleanAction extends Base {

    /**
     * Execute this DigitalActionBean.
     * 
     * @param hasChangedToTrue true if the expression has changed to true, false otherwise
     * @param hasChangedToFalse true if the expression has changed to false, false otherwise
     * @throws JmriException when an exception occurs
     */
    public void execute(boolean hasChangedToTrue, boolean hasChangedToFalse) throws JmriException;
    
}
