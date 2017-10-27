package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GraphicsEnvironment;
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
public class PrintEngineRosterActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EnginesTableFrame etf = new EnginesTableFrame();
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Print Engine Roster Test Frame");
        PrintEngineRosterAction t = new PrintEngineRosterAction("Test Action",jf,true,etf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(etf);
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

    // private final static Logger log = LoggerFactory.getLogger(PrintEngineRosterActionTest.class);

}
