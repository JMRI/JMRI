package jmri.jmrix.internal;

/**
 * Returns a list of valid lenz Virtual Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 *
 */
public class InternalConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String NONE = "None";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.internal.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{NONE};
    }

}
