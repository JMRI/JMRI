package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * A LogixNG digitalaction.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalAction extends Base {

    /**
     * Execute this DigitalActionBean.
     * 
     * @throws JmriException when an exception occurs
     */
    public void execute() throws JmriException;
    
}
