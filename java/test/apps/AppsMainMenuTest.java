package apps;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Dave Sand Copyright (C) 2021
 */
public class AppsMainMenuTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AppsMainMenu t = new AppsMainMenu();
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
        public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
        public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AppsMainMenuTest.class);
}
