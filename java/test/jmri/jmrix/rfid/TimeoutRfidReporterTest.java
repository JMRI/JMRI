package jmri.jmrix.rfid;

import org.junit.*;

/**
 * TimeoutRfidReporterTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.TimeoutRfidReporter class
 *
 * @author	Paul Bender
 */
public class TimeoutRfidReporterTest extends RfidReporterTest {

    @Test
    @Override
    public void test1ParamCtor() {
       TimeoutRfidReporter s = new TimeoutRfidReporter("FRA");
       Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        r = new TimeoutRfidReporter("FRA", "Test");
    }

    @Override
    @After
    public void tearDown() {
	r = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

    }

}
