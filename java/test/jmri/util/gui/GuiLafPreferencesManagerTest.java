package jmri.util.gui;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017 
 */
public class GuiLafPreferencesManagerTest {

    @Test
    public void testCTor() {
        GuiLafPreferencesManager t = new GuiLafPreferencesManager();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(GuiLafPreferencesManagerTest.class);

}
