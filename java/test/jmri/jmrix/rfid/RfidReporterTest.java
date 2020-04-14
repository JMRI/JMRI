package jmri.jmrix.rfid;

import org.junit.*;
import jmri.IdTag;

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

    // RfidReporter implements the IdTagListener interface, which
    // includes a notify(IdTag) method.
    @Test
    public void testNotify() {
        Assume.assumeTrue(r instanceof RfidReporter);
        Assume.assumeTrue(generateObjectToReport() instanceof IdTag);
        Assert.assertEquals("IdTag not Seen",IdTag.UNKNOWN,r.getState());
        ((RfidReporter)r).notify((IdTag)generateObjectToReport());
        // Check that both CurrentReport and LastReport are not null
        Assert.assertNotNull("CurrentReport Object exists", r.getCurrentReport());
        Assert.assertNotNull("LastReport Object exists", r.getLastReport());
        // Check the value of both CurrentReport and LastReport
        Assert.assertEquals("CurrentReport equals LastReport",r.getLastReport(), r.getCurrentReport());
        Assert.assertEquals("IdTag Seen",IdTag.SEEN,r.getState());

        // send a null report.

        ((RfidReporter)r).notify((IdTag)null);
        // Check that both CurrentReport and LastReport are not null
        Assert.assertNull("CurrentReport Object Null", r.getCurrentReport());
        Assert.assertNotNull("LastReport Object exists", r.getLastReport());
        Assert.assertEquals("IdTag Seen",IdTag.UNSEEN,r.getState());
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
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	jmri.util.JUnitUtil.tearDown();
    }

}
