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
     * @param status true if the expression has changed to true.
     * false if the expression has changed to false
     * @throws JmriException when an exception occurs
     */
    public void execute(boolean status) throws JmriException;
    
}
