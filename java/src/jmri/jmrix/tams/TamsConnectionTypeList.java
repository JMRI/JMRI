// TamsConnectionTypeList.java
package jmri.jmrix.tams;

/**
 * Returns a list of valid ESU Tams Connection Types
 * <P>
 * @author Kevin Dickerson Copyright (C) 2012
 *
 */
public class TamsConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String TAMS = "Tams";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.tams.serialdriver.ConnectionConfig",
            "jmri.jmrix.tams.simulator.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{TAMS};
    }

}
