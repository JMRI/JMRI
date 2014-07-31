// JMRIClientConnectionTypeList.java
package jmri.jmrix.jmriclient;

/**
 * Returns a list of valid JMRIClient Connection Types
 * <P>
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @uthoer Randall Wood Copyright (C) 2014
 */
public class JMRIClientConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.jmriclient.json.JsonNetworkConnectionConfig",
            "jmri.jmrix.jmriclient.networkdriver.ConnectionConfig"
        };
    }

}
