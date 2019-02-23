package jmri.implementation;

import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2019	
 */
public class DefaultCabSignalTest {

    @Test
    public void testCTor() {
        DefaultCabSignal cs = new DefaultCabSignal(new DccLocoAddress(1234,true));
        Assert.assertNotNull("exists",cs);
        //check the defaults.
        Assert.assertEquals("Address",new DccLocoAddress(1234,true),cs.getCabSignalAddress());
        Assert.assertNull("current block",cs.getBlock());
        Assert.assertNull("next block",cs.getNextBlock());
        Assert.assertNull("next mast",cs.getNextMast());
        Assert.assertTrue("cab signal active",cs.isCabSignalActive());
        cs.dispose(); // verify no exceptions
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

    //private final static Logger log = LoggerFactory.getLogger(DefaultCabSignalTest.class);

}
