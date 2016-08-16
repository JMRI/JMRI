package jmri.jmrix.connectionConfigManager;

import static jmri.jmrix.ConnectionConfigManagerTest.MFG1;
import static jmri.jmrix.ConnectionConfigManagerTest.TYPE_A;

import jmri.jmrix.ConnectionTypeList;

/**
 * Test ConnectionTypeList
 *
 * @author Randall Wood (C) 2016
 */
public class TestTypeList1A implements ConnectionTypeList {

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{TYPE_A};
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{MFG1};
    }

}
