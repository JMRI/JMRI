package jmri.jmrix.loconet;

import java.util.Calendar;
import java.util.Date;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.implementation.AbstractReporter;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the TranspondingTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class TranspondingTagTest {

    @Test
    public void testCreateTranspondingTag() {
        TranspondingTag r = new TranspondingTag("ID1234");
        Assert.assertNotNull("TranspondingTag not null", r);
    }

    @Test
    public void testGetTranspondingTagUserName() {
        TranspondingTag r = new TranspondingTag("ID1234", "Test Tag");
        Assert.assertEquals("TranspondingTag user name is 'Test Tag'", "Test Tag", r.getUserName());
    }

    @Test
    public void testGetTranspondingTagTagID() {
        TranspondingTag r = new TranspondingTag("ID1234");
        Assert.assertEquals("TranspondingTag TagID is 1234", "1234", r.getTagID());
    }

    @Test
    public void testTranspondingTagToString() {
        TranspondingTag r = new TranspondingTag("ID1234");
        // set the entryexit property
        r.setProperty("entryexit","exits");
        Assert.assertEquals("TranspondingTag toString ", "1234 exits", r.toString());
    }

    @Test
    public void testTranspondingTagToReportString() {
        TranspondingTag r = new TranspondingTag("LD1234");
        Assert.assertEquals("TranspondingTag toReportString ", "1234", r.toReportString());
    }

    @Test
    public void testNotYetSeen() {
        TranspondingTag r = new TranspondingTag("ID0413276BC1");
        Assert.assertNull("At creation, Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("At creation, Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("At creation, TranspondingTag status is UNSEEN", TranspondingTag.UNSEEN, r.getState());

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), TranspondingTag status is UNSEEN", TranspondingTag.UNSEEN, r.getState());
    }

    @Test
    public void testHasBeenSeen() throws InterruptedException {
        TranspondingTag r = new TranspondingTag("ID0413276BC1");
        Reporter rep = new AbstractReporter("IR1") {
            @Override
            public int getState() {
                return state;
            }

            @Override
            public void setState(int s) {
                state = s;
            }
            int state = 0;
        };

        Date timeBefore = Calendar.getInstance().getTime();
        Thread.sleep(5);
        r.setWhereLastSeen(rep);
        Thread.sleep(5);
        Date timeAfter = Calendar.getInstance().getTime();

        Assert.assertEquals("Where last seen is 'IR1'", rep, r.getWhereLastSeen());
        Assert.assertNotNull("When last seen is not null", r.getWhenLastSeen());
        Assert.assertEquals("Status is SEEN", TranspondingTag.SEEN, r.getState());
        Assert.assertTrue("Time when last seen is later than 'timeBefore'", r.getWhenLastSeen().after(timeBefore));
        Assert.assertTrue("Time when last seen is earlier than 'timeAfter'", r.getWhenLastSeen().before(timeAfter));

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), TranspondingTag status is UNSEEN", TranspondingTag.UNSEEN, r.getState());

    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        new TranspondingTagManager();
    }

    @After
    public void tearDown() throws Exception {
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
