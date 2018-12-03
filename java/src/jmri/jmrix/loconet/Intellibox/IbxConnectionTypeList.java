package jmri.jmrix.loconet.Intellibox;

/**
 * Returns a list of valid Intellibox Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2010
 */
public class IbxConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    @Override
    public String[] getAvailableProtocolClasses() {
        String[] masterList = new jmri.jmrix.loconet.LnConnectionTypeList().getAvailableProtocolClasses();

        String[] tempList = new String[masterList.length + 1];
        tempList[0] = "jmri.jmrix.loconet.Intellibox.ConnectionConfig";
        int x = 1;
        for (int i = 0; i < masterList.length; i++) {
            tempList[x] = masterList[i];
            x++;
        }
        return tempList;
    }

    @Override
    public String[] getManufacturers() {
        // does not appear to be used, so return class name as placeholder
        return new String[]{IbxConnectionTypeList.class.getCanonicalName()};
    }

}
