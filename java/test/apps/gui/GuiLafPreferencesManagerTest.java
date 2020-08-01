package apps.gui;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GuiLafPreferencesManagerTest {

    @Test
    public void testCTor() {
        @SuppressWarnings("deprecation")
        GuiLafPreferencesManager t = new GuiLafPreferencesManager();
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

    // private final static Logger log = LoggerFactory.getLogger(GuiLafPreferencesManagerTest.class);

}
