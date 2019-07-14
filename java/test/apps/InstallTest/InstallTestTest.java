package apps.InstallTest;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class InstallTestTest {

    @Test
    @Ignore("gives error message about an invalid profile on Travis")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstallTest t = new InstallTest();
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(InstallTestTest.class);
}
