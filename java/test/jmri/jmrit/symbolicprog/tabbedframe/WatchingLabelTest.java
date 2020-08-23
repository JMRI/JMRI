package jmri.jmrit.symbolicprog.tabbedframe;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WatchingLabelTest {

    @Test
    public void testCTor() {
        WatchingLabel t = new WatchingLabel("Test Label",new javax.swing.JPanel());
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WatchingLabelTest.class.getName());

}
