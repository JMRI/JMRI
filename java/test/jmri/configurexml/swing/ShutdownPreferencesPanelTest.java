package jmri.configurexml.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Dave Sand Copyright (C) 2022
 */
public class ShutdownPreferencesPanelTest {

    @Test
    public void testCTor() {
        ShutdownPreferencesPanel t = new ShutdownPreferencesPanel();
        Assert.assertNotNull("exists",t);
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
