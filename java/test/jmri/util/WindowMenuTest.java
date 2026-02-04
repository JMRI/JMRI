package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class WindowMenuTest {

    @Test
    public void testCTor() {
        JmriJFrame jf = new JmriJFrame("Window Menu Test");
        WindowMenu t = new WindowMenu(jf);
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

    // private final static Logger log = LoggerFactory.getLogger(WindowMenuTest.class);

}
