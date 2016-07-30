package jmri.jmrix.connectionConfigManager;

import static jmri.jmrix.ConnectionConfigManagerTest.MFG2;
import static jmri.jmrix.ConnectionConfigManagerTest.TYPE_C;

import jmri.jmrix.ConnectionTypeList;

/**
 * Test ConnectionTypeList
 *
 * @author Randall Wood (C) 2016
 */
public class TestTypeList2 implements ConnectionTypeList {

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{TYPE_C};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{MFG2};
    }

}
