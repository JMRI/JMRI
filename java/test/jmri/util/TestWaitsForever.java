package jmri.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * A single test that waits forever.
 * Put this at the end of a PackageTest file to 
 * have execution wait so that you can e.g. get a 
 * thread or heap dump at the end of execution.
 * @author Bob Jacobsen 2019
 */
@Disabled("Tests test failure, should never be a part of a normal test suite")
public class TestWaitsForever {

    @Test
    public synchronized void deliberatelyWaitForever() throws InterruptedException {
        Runtime.getRuntime().gc();
        System.err.println("start permanent wait");
        this.wait();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TestWaitsForever.class);

}
