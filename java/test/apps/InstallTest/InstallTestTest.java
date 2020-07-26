package apps.InstallTest;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InstallTestTest {

    @Test
    @Disabled("gives error message about an invalid profile on Travis")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstallTest t = new InstallTest();
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

    // private final static Logger log = LoggerFactory.getLogger(InstallTestTest.class);
}
