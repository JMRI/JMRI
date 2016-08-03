package jmri.jmrix.connectionConfigManager;

import static jmri.jmrix.ConnectionConfigManagerTest.MFG3;
import static jmri.jmrix.ConnectionConfigManagerTest.TYPE_D;

import jmri.jmrix.ConnectionTypeList;

/**
 * Test ConnectionTypeList
 *
 * @author Randall Wood (C) 2016
 */
public class TestTypeList3 implements ConnectionTypeList {

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{TYPE_D};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{MFG3};
    }

}
