package jmri.jmrit.roster;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.*;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2018	
 */
public class PrintListActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame jf = new JmriJFrame("TestPrintListActionWindow");
        jmri.util.swing.WindowInterface wi = jf;
        PrintListAction t = new PrintListAction("test print action",wi);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
