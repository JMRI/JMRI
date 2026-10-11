package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import jmri.Reporter;
import jmri.implementation.AbstractReporter;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the TranspondingTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class TranspondingTagTest {

    @Test
    public void testCreateTranspondingTag() {
        TranspondingTag r = new TranspondingTag("ID1234");
        assertNotNull( r, "TranspondingTag not null");
    }

    @Test
    public void testGetTranspondingTagUserName() {
        TranspondingTag r = new TranspondingTag("ID1234", "Test Tag");
        assertEquals( "Test Tag", r.getUserName(), "TranspondingTag user name is 'Test Tag'");
    }

    @Test
    public void testGetTranspondingTagTagID() {
        TranspondingTag r = new TranspondingTag("ID1234");
        assertEquals( "1234", r.getTagID(), "TranspondingTag TagID is 1234");
    }

    @Test
    public void testTranspondingTagToString() {
        TranspondingTag r = new TranspondingTag("ID1234");
        // set the entryexit property
        r.setProperty("entryexit", "exits");
        assertEquals( "ID1234", r.toString(), "TranspondingTag toString ");
    }

    @Test
    public void testTranspondingTagToReportString() {
        TranspondingTag r = new TranspondingTag("LD1234");
        assertEquals( "1234", r.toReportString(), "TranspondingTag toReportString ");
        r.setProperty("entryexit", "exits");
        assertEquals( "1234 exits", r.toReportString(), "TranspondingTag toReportString ");
    }

    @Test
    public void testNotYetSeen() {
        TranspondingTag r = new TranspondingTag("ID0413276BC1");
        assertNull( r.getWhereLastSeen(), "At creation, Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "At creation, Date when seen is null");
        assertEquals( TranspondingTag.UNSEEN, r.getState(), "At creation, TranspondingTag status is UNSEEN");

        r.setWhereLastSeen(null);
        assertNull( r.getWhereLastSeen(), "After setWhereLastSeen(null), Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "After setWhereLastSeen(null), Date when seen is null");
        assertEquals( TranspondingTag.UNSEEN, r.getState(),
            "After setWhereLastSeen(null), TranspondingTag status is UNSEEN");
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
            private int state = 0;
        };

        Date timeBefore = Calendar.getInstance().getTime();
        JUnitUtil.waitFor(5);
        r.setWhereLastSeen(rep);
        JUnitUtil.waitFor(5);
        Date timeAfter = Calendar.getInstance().getTime();

        assertEquals( rep, r.getWhereLastSeen(), "Where last seen is 'IR1'");
        assertNotNull( r.getWhenLastSeen(), "When last seen is not null");
        assertEquals( TranspondingTag.SEEN, r.getState(), "Status is SEEN");
        assertTrue( r.getWhenLastSeen().after(timeBefore), "Time when last seen is later than 'timeBefore'");
        assertTrue( r.getWhenLastSeen().before(timeAfter), "Time when last seen is earlier than 'timeAfter'");

        r.setWhereLastSeen(null);
        assertNull( r.getWhereLastSeen(), "After setWhereLastSeen(null), Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "After setWhereLastSeen(null), Date when seen is null");
        assertEquals( TranspondingTag.UNSEEN, r.getState(), "After setWhereLastSeen(null), TranspondingTag status is UNSEEN");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        TranspondingTagManager t = new TranspondingTagManager();
        assertNotNull(t);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
