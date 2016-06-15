package jmri;

/**
 * Object to handle "user" and "system" turnout addresses.
 * TurnoutManager is primary consumer of these
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @deprecated since 4.3.7
 */
@Deprecated
public class TurnoutAddress extends Address {

    public TurnoutAddress(String system, String user) {
        super(system, user);
    }

    /**
     * both names are the same in this ctor
     */
    public TurnoutAddress(String name) {
        super(name);
    }
}
