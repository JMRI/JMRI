package jmri.implementation;


/**
 * A signal head that exists only within the program.
 * <p>
 * This can be useful e.g. as part of a more complex signal calculation.
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class VirtualSignalHead extends DefaultSignalHead {

    public VirtualSignalHead(String sys, String user) {
        super(sys, user);
    }

    public VirtualSignalHead(String sys) {
        super(sys);
    }

    @Override
    protected void updateOutput() {
    }

    @Override
    boolean isTurnoutUsed(jmri.Turnout t) {
        return false;
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose() {
        super.dispose();
    }
}
