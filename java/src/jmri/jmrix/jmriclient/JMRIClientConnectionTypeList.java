// JMRIClientConnectionTypeList.java

package jmri.jmrix.jmriclient;


/**
 * Returns a list of valid JMRIClient Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @author      Paul Bender    Copyright (C) 2010
 * @version	$Revision$
 *
 */
public class JMRIClientConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() { 
        return new String[] {
              "jmri.jmrix.jmriclient.networkdriver.ConnectionConfig"
        };
    }

}

