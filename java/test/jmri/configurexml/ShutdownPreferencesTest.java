package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Dave Sand Copyright (C) 2022
 */
public class ShutdownPreferencesTest {

    @Test
    public void testCTor() {
        ShutdownPreferences sh = new ShutdownPreferences();
        Assert.assertNotNull("exists", sh);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ShutdownPreferencesTest.class);

}
