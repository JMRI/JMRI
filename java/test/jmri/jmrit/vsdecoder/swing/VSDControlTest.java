package jmri.jmrit.vsdecoder.swing;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class VSDControlTest {

    @Test
    @Ignore("tests causes NPE, needs more setup")
    public void testCTor() {
        VSDControl t = new VSDControl();
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(VSDControlTest.class);

}
