package jmri.jmrit.progsupport;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ProgModePaneTest {

    @Test
    public void testCTor() {
        ProgModePane t = new ProgModePane(javax.swing.BoxLayout.X_AXIS);
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

    // private final static Logger log = LoggerFactory.getLogger(ProgModePaneTest.class);

}
