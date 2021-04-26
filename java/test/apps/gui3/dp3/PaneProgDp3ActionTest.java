package apps.gui3.dp3;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PaneProgDp3ActionTest {

    @Test
    public void testCTor() {
        PaneProgDp3Action t = new PaneProgDp3Action();
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

    // private final static Logger log = LoggerFactory.getLogger(PaneProgDp3ActionTest.class);
}
