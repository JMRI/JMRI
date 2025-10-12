package jmri.util.swing.mdi;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriJInternalFrameInterfaceTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Internal Frame Interface Test");
        javax.swing.JDesktopPane jd = new javax.swing.JDesktopPane();
        JmriJInternalFrameInterface t = new JmriJInternalFrameInterface(jf,jd);
        Assertions.assertNotNull( t, "exists");
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

    // private final static Logger log = LoggerFactory.getLogger(JmriJInternalFrameInterfaceTest.class);

}
