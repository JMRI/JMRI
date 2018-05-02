package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.trains.TrainsTableFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.dispose(ttf);
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainsActionTest.class);

}
