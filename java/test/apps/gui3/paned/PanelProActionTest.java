package apps.gui3.paned;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JFrameInterface;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PanelProActionTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        JFrameInterface w = new JFrameInterface(new jmri.util.JmriJFrame("foo"));
        PanelProAction t = new PanelProAction("test",w);
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PanelProActionTest.class);

}
