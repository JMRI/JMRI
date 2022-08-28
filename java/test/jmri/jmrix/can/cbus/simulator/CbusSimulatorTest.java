package jmri.jmrix.can.cbus.simulator;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 */
public class CbusSimulatorTest {

    private CbusSimulator t = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStartedCounts() {
        Assertions.assertNotNull(t);
        Assert.assertTrue("cs 1 ", t.getNumCS() == 1);
        Assert.assertTrue("No nodes auto started ", t.getNumNd() == 0);
        Assert.assertTrue("ev 1 ", t.getNumEv() == 1);
        
        Assert.assertNotNull("cs get ", t.getCS(0));
       //  Assert.assertNotNull("nd get ", t.getNd(0));
        Assert.assertNotNull("ev get ", t.getEv(0));
    }
    
    @Test
    public void testgetNew() {
        Assertions.assertNotNull(t);
        Assert.assertNotNull("cs get new", t.getNewCS());
        Assert.assertNotNull("ev get new", t.getNewEv());
        
        Assert.assertTrue("cs 2 ", t.getNumCS() == 2);
        Assert.assertTrue("ev 2 ", t.getNumEv() == 2);        
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new CbusSimulator(null);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(t);
        t.dispose();
        t = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusSimulatorTest.class);

}
