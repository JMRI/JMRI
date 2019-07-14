package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A single test that waits forever.
 * Put this at the end of a PackageTest file to 
 * have execution wait so that you can e.g. get a 
 * thread or heap dump at the end of execution.
 * @author Bob Jacobsen 2019	
 */
public class TestWaitsForever {

    @Test
    public synchronized void deliberatelyWaitForever() throws InterruptedException {
        Runtime.getRuntime().gc();
        System.err.println("start permanent wait");
        this.wait();
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

    // private final static Logger log = LoggerFactory.getLogger(TestWaitsForever.class);

}
