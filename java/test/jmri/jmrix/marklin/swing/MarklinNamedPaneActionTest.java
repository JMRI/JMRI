package jmri.jmrix.marklin.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinNamedPaneActionTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        jmri.jmrix.marklin.MarklinSystemConnectionMemo memo = new jmri.jmrix.marklin.MarklinSystemConnectionMemo();
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Marklin Named Pane Test");
        MarklinNamedPaneAction t = new MarklinNamedPaneAction("Test Action",jf,"test",memo);
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(jf);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinNamedPaneActionTest.class);

}
