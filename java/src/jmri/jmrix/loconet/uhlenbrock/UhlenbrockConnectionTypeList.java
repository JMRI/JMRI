// UhlenbrockConnectionTypeList.java.java

package jmri.jmrix.loconet.uhlenbrock;


/**
 * Returns a list of valid Uhlenbrock Connection Types
 * <P>
 * @author      Bob Jacobsen   Copyright (C) 2010
 * @author      Kevin Dickerson    Copyright (C) 2010
 * @version	$Revision: 17977 $
 *
 */
public class UhlenbrockConnectionTypeList  implements jmri.jmrix.ConnectionTypeList {

    public String[] getAvailableProtocolClasses() {
        String[] masterList = new jmri.jmrix.loconet.LnConnectionTypeList().getAvailableProtocolClasses();
        
        String[] tempList = new String[masterList.length + 2];
        tempList[0] = "jmri.jmrix.loconet.uhlenbrock.ConnectionConfig";
        tempList[1] = "jmri.jmrix.loconet.Intellibox.ConnectionConfig";
        int x = 2;
        for (int i = 0; i<masterList.length; i++) {
            tempList[x] = masterList[i];
            x++;
        }
        return tempList;
    }
}

