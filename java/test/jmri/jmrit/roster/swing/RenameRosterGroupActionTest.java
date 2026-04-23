package jmri.jmrit.roster.swing;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RenameRosterGroupActionTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        JmriJFrame jf = new JmriJFrame("TestRenameWindow");
        jmri.util.swing.WindowInterface wi = jf;
        RenameRosterGroupAction t = new RenameRosterGroupAction("test rename roster group",wi);
        Assertions.assertNotNull(t,"exists");
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

    // private final static Logger log = LoggerFactory.getLogger(RenameRosterGroupActionTest.class);

}
