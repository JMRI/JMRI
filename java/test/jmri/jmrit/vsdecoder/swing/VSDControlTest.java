package jmri.jmrit.vsdecoder.swing;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDControlTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        VSDControl t = new VSDControl("Test");
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDControlTest.class);

}
