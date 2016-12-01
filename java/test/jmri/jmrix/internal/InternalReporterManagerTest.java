package jmri.jmrix.internal;

import jmri.Reporter;
import jmri.ReporterManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the InternalReporterManager
 *
 * @author	Bob Jacobsen 2003, 2006, 2008
 * @author	Mark Underwood 2012
 * @author	Paul Bender 2016
 */
public class InternalReporterManagerTest extends jmri.managers.AbstractReporterMgrTest {

    @Override
    public String getSystemName(int i) {
        return "IR" + i;
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // create and register the manager object
        l = new InternalReporterManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
