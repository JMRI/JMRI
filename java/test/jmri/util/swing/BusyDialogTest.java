package jmri.util.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BusyDialogTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("test");
        BusyDialog t = new BusyDialog(jf,"test busy dialog",false);
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

    // private final static Logger log = LoggerFactory.getLogger(BusyDialogTest.class);

}
