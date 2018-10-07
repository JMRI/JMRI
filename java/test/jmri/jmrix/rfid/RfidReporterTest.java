package jmri.jmrix.rfid;

import org.junit.*;

/**
 * RfidReporterTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidReporter class
 *
 * @author	Paul Bender Copyright (C) 2016,2018
 */
public class RfidReporterTest extends jmri.implementation.AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }

    @Test
    public void test1ParamCtor() {
       RfidReporter s = new RfidReporter("FRA");
       Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        r = new RfidReporter("FRA", "Test");
    }

    @Override
    @After
    public void tearDown() {
        r = null;
    	jmri.util.JUnitUtil.tearDown();
    }

}
