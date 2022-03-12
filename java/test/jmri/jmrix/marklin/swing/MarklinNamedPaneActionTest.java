package jmri.jmrix.marklin.swing;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinNamedPaneActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        jmri.jmrix.marklin.MarklinSystemConnectionMemo memo = new jmri.jmrix.marklin.MarklinSystemConnectionMemo();
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Marklin Named Pane Test");
        MarklinNamedPaneAction t = new MarklinNamedPaneAction("Test Action",jf,"test",memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinNamedPaneActionTest.class);

}
