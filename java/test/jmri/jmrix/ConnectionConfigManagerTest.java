/**
 * AbstractPowerManagerTest.java
 *
 * Description:	AbsBaseClass for PowerManager tests in specific jmrix. packages
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
package jmri.jmrix;

import apps.tests.Log4JFixture;
import jmri.jmrix.internal.InternalConnectionTypeList;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConnectionConfigManagerTest extends TestCase {

    public void testGetConnectionManufacturers() {
        ConnectionConfigManager manager = new ConnectionConfigManager();
        String[] result = manager.getConnectionManufacturers();
        assertTrue(result.length > 1);
        assertEquals(result[0], InternalConnectionTypeList.NONE);
        JUnitUtil.resetInstanceManager();
    }

    public void testGetConnectionTypes() {
        ConnectionConfigManager manager = new ConnectionConfigManager();
        String[] result = manager.getConnectionTypes(InternalConnectionTypeList.NONE);
        assertEquals(1, result.length);
        assertEquals(result[0], "jmri.jmrix.internal.ConnectionConfig");
        JUnitUtil.resetInstanceManager();
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(ConnectionConfigManagerTest.class);
    }

    @Override
    public void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }

}
