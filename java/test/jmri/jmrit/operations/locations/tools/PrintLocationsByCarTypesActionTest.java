package jmri.jmrit.operations.locations.tools;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.locations.LocationEditFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintLocationsByCarTypesActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationEditFrame f = new LocationEditFrame(null);
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Print Locations By Car Types");
        PrintLocationsByCarTypesAction t = new PrintLocationsByCarTypesAction("Test Action",jf,true,f);
        Assert.assertNotNull("exists",t);
        f.dispose();
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(PrintLocationsByCarTypesActionTest.class.getName());

}
