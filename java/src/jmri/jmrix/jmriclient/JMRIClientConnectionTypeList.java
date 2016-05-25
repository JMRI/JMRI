// JMRIClientConnectionTypeList.java
package jmri.jmrix.jmriclient;

/**
 * Returns a list of valid JMRIClient Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2014
 */
public class JMRIClientConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String JMRI = "JMRI (Network)";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            // "jmri.jmrix.jmriclient.json.JsonNetworkConnectionConfig", // do not allow configuration
            "jmri.jmrix.jmriclient.networkdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{JMRI};
    }

}
