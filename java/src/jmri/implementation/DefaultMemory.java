// DefaultMemory.java
package jmri.implementation;

/**
 * Concrete implementation of the Reporter interface for the Internal system.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version	$Revision$
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
     * <P>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead. (E.g. getCommandedState in Turnout) This provided to make
     * Jython script access easier to read.
     * <P>
     * If the current value can be reduced to an integer, that is returned,
     * otherwise a value of -1 is returned.
     */
    public int getState() {
        try {
            return Integer.valueOf(getValue().toString()).intValue();
        } catch (java.lang.NumberFormatException ex1) {
            return -1;
        } catch (java.lang.NullPointerException ex2) {
            return -1;
        }
    }

    public void setState(int s) {
        setValue("" + s);
    }
}

/* @(#)DefaultMemory.java */
