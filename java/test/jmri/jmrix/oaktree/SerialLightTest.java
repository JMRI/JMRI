package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialLightTest {

    private OakTreeSystemConnectionMemo _memo = null;

    @Test
    public void testCTor() {
        SerialLight t = new SerialLight("OL1", _memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTor2() {
        SerialLight t2 = new SerialLight("OL2", "L2", _memo);
        Assert.assertNotNull("exists",t2);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        _memo = new OakTreeSystemConnectionMemo("O", "Oaktree");    }

    @After
    public void tearDown() {

        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(SerialLightTest.class);

}
