package jmri.jmrix.internal;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test the InternalReporterManager
 *
 * @author Bob Jacobsen 2003, 2006, 2008
 * @author Mark Underwood 2012
 * @author Paul Bender 2016
 */
public class InternalReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "IR" + i;
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        // create and register the manager object
        l = new InternalReporterManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
