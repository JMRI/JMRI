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
     * @param value the value of the expression
     * @throws JmriException when an exception occurs
     */
    void execute(boolean value) throws JmriException;

}
