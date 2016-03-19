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
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalConnectionTypeList;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public abstract class ConnectionConfigManagerTest extends TestCase {

    public ConnectionConfigManagerTest(String s) {
        super(s);
    }

    public void testGetConnectionManufacturers() throws ClassNotFoundException {
        ConnectionConfigManager manager = new ConnectionConfigManager();
        // ConnectionTypeManager is private within ConnectionConfigManager
        Class<?> typeManager = Class.forName(ConnectionConfigManager.class.getCanonicalName() + "$ConnectionTypeManager");
        assertNull(InstanceManager.getDefault(typeManager));
        String[] result = manager.getConnectionManufacturers();
        assertNotNull(InstanceManager.getDefault(typeManager));
        assertTrue(result.length > 1);
        assertEquals(result[0], InternalConnectionTypeList.NONE);
        JUnitUtil.resetInstanceManager();
    }

    public void testGetConnectionTypes() throws ClassNotFoundException {
        ConnectionConfigManager manager = new ConnectionConfigManager();
        // ConnectionTypeManager is private within ConnectionConfigManager
        Class<?> typeManager = Class.forName(ConnectionConfigManager.class.getCanonicalName() + "$ConnectionTypeManager");
        assertNull(InstanceManager.getDefault(typeManager));
        String[] result = manager.getConnectionTypes(InternalConnectionTypeList.NONE);
        assertNotNull(InstanceManager.getDefault(typeManager));
        assertEquals(1, result.length);
        assertEquals(result[0], "jmri.jmrix.internal.ConnectionConfig");
        JUnitUtil.resetInstanceManager();
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConnectionConfigManagerTest.class);
        return suite;
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
