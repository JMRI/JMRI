package apps;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AppsTest {

    @Test
    @Disabled("Test emits an (unknown) error message on Appveyor")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Apps t = new Apps();
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

    // private final static Logger log = LoggerFactory.getLogger(AppsTest.class);
}
