package jmri.jmrit.operations.trains.tools;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.trains.TrainsTableFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintTrainsActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsTableFrame ttf = new TrainsTableFrame();
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Print Trains Action");
        PrintTrainsAction t = new PrintTrainsAction("Test Action",jf,true,ttf);
        Assert.assertNotNull("exists",t);
        ttf.dispose();
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

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainsActionTest.class.getName());

}
