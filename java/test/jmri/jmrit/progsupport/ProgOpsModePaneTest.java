package jmri.jmrit.progsupport;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProgOpsModePaneTest {

    @Test
    public void testCTor() {
        ProgOpsModePane t = new ProgOpsModePane(javax.swing.BoxLayout.X_AXIS);
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ProgOpsModePaneTest.class);

}
