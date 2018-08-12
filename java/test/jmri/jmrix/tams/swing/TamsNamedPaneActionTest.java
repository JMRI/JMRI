package jmri.jmrix.tams.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
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
public class TamsNamedPaneActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Tams named Pane Test");
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo();
        TamsNamedPaneAction t = new TamsNamedPaneAction("Test Action",jf,"Test",memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsNamedPaneActionTest.class);

}
