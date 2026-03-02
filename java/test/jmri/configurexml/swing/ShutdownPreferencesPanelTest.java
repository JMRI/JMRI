package jmri.configurexml.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Dave Sand Copyright (C) 2022
 */
public class ShutdownPreferencesPanelTest {

    @Test
    public void testCTor() {
        ShutdownPreferencesPanel t = new ShutdownPreferencesPanel();
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

    // private final static Logger log = LoggerFactory.getLogger(ShutdownPreferencesPanelTest.class);

}
