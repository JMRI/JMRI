// SerialConnectionTypeList.java
package jmri.jmrix.powerline;

/**
 * Returns a list of valid Powerline Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class SerialConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String POWERLINE = "Powerline";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.powerline.cm11.ConnectionConfig",
            "jmri.jmrix.powerline.cp290.ConnectionConfig",
            "jmri.jmrix.powerline.insteon2412s.ConnectionConfig",
            "jmri.jmrix.powerline.simulator.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{POWERLINE};
    }

}
