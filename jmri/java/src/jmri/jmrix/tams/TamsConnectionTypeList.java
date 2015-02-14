// TamsConnectionTypeList.java

package jmri.jmrix.tams;


/**
 * Returns a list of valid ESU Tams Connection Types
 * <P>
 * @author      Kevin Dickerson    Copyright (C) 2012
 * @version	$Revision: 17977 $
 *
 */
public class TamsConnectionTypeList implements jmri.jmrix.ConnectionTypeList{
    
    public String[] getAvailableProtocolClasses() { 
        return new String[] {
          "jmri.jmrix.tams.serialdriver.ConnectionConfig",
          "jmri.jmrix.tams.simulator.ConnectionConfig"
        };
    }

}

