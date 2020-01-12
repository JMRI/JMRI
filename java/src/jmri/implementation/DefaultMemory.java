package jmri.implementation;

/**
 * Concrete implementation of the Reporter interface for the Internal system.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class DefaultMemory extends AbstractMemory {

    public DefaultMemory(String systemName) {
        super(systemName);
    }

    public DefaultMemory(String systemName, String userName) {
        super(systemName, userName);
    }

    /**
     * Provide generic access to internal state.
     * <p>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead. (E.g. getCommandedState in Turnout) This provided to make
     * Jython script access easier to read.
     * <p>
     * If the current value can be reduced to an integer, that is returned,
     * otherwise a value of -1 is returned.
     */
    @Override
    public int getState() {
        try {
            return Integer.parseInt(getValue().toString());
        } catch (java.lang.NumberFormatException ex1) {
            return -1;
        } catch (java.lang.NullPointerException ex2) {
            return -1;
        }
    }

    @Override
    public void setState(int s) {
        setValue("" + s);
    }
}
