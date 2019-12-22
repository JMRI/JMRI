package jmri.jmrit.logixng;

/**
 * A LogixNG logix emulator action.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalBooleanAction extends Base {

    /**
     * Execute this DigitalActionBean.
     * @param hasChangedToTrue true if the expression has changed to true.
     * false if the expression has changed to false
     */
    public void execute(boolean hasChangedToTrue) throws Exception;
    
}
