package apps;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AppsTest {

    @Test
    @Ignore("Test emits an (unknown) error message on Appveyor")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Apps t = new Apps();
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

    // private final static Logger log = LoggerFactory.getLogger(AppsTest.class);
}
