package jmri.util.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JFrameInterfaceTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        JFrameInterface t = new JFrameInterface(new jmri.util.JmriJFrame("foo"));

        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JFrameInterfaceTest.class);

}
