package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 * Tests for the DefaultIdTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class DefaultIdTagTest {

    @Test
    public void testCreateIdTag() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        assertNotNull( r, "IdTag not null");
    }

    @Test
    public void testGetIdTagUserName() {
        IdTag r = new DefaultIdTag("ID0413276BC1", "Test Tag");
        assertEquals( "Test Tag", r.getUserName(), "IdTag user name is 'Test Tag'");
    }

    @Test
    public void testGetIdTagTagID() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        assertEquals( "0413276BC1", r.getTagID(), "IdTag TagID is 0413276BC1");
    }

    @Test
    public void testIdTagToString() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        assertEquals( "ID0413276BC1", r.toString(), "IdTag toString is ID0413276BC1");
    }

    @Test
    public void testIdTagToReportString() {
        DefaultIdTag r = new DefaultIdTag("ID0413276BC1");
        assertEquals( "0413276BC1", r.toReportString(), "IdTag toReportString is 0413276BC1");

        r.setUserName("Test Tag");
        assertEquals( "Test Tag", r.toReportString(), "IdTag toReportString is 'Test Tag'");
    }

    @Test
    public void testNotYetSeen() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        assertNull( r.getWhereLastSeen(), "At creation, Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "At creation, Date when seen is null");
        assertEquals( IdTag.UNSEEN, r.getState(), "At creation, IdTag status is UNSEEN");

        r.setWhereLastSeen(null);
        assertNull( r.getWhereLastSeen(), "After setWhereLastSeen(null), Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "After setWhereLastSeen(null), Date when seen is null");
        assertEquals( IdTag.UNSEEN, r.getState(), "After setWhereLastSeen(null), IdTag status is UNSEEN");
    }

    @Test
    public void testHasBeenSeen() throws InterruptedException {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Reporter rep = new TestIdTagReporter("IR1");

        Date timeBefore = Calendar.getInstance().getTime();
        JUnitUtil.waitFor(5);
        r.setWhereLastSeen(rep);
        JUnitUtil.waitFor(5);
        Date timeAfter = Calendar.getInstance().getTime();
        Date whenLastSeen = r.getWhenLastSeen();

        assertEquals( rep, r.getWhereLastSeen(), "Where last seen is 'IR1'");
        assertNotNull( whenLastSeen, "When last seen is not null");
        assertEquals( IdTag.SEEN, r.getState(), "Status is SEEN");
        assertTrue( whenLastSeen.after(timeBefore), "Time when last seen is later than 'timeBefore'");
        assertTrue( whenLastSeen.before(timeAfter), "Time when last seen is earlier than 'timeAfter'");

        r.setWhereLastSeen(null);
        assertNull( r.getWhereLastSeen(), "After setWhereLastSeen(null), Reporter where seen is null");
        assertNull( r.getWhenLastSeen(), "After setWhereLastSeen(null), Date when seen is null");
        assertEquals( IdTag.UNSEEN, r.getState(), "After setWhereLastSeen(null), IdTag status is UNSEEN");

    }

    @Test
    public void testStoreTag() {
        IdTag r = new DefaultIdTag("ID0213276BC5");
        Reporter rep = new TestIdTagReporter("IR2");

        Element e = r.store(true);
        assertNotNull(e);
        assertEquals("idtag", e.getName());

        assertEquals("ID0213276BC5", e.getChildText("systemName"));
        assertNull( e.getChildText("userName"));
        assertNull( e.getChildText("comment"));
        assertNull( e.getChildText("whereLastSeen"));
        assertNull( e.getChildText("whenLastSeen"));

        r.setUserName("");
        r.setComment("");
        e = r.store(false);
        assertNull( e.getChildText("userName"));
        assertNull( e.getChildText("comment"));

        r.setUserName("Test UNamE");
        r.setComment("Test CommenT");
        e = r.store(true);
        assertEquals("Test UNamE", e.getChildText("userName"));
        assertEquals("Test CommenT", e.getChildText("comment"));

        r.setWhereLastSeen(rep);
        e = r.store(false);
        assertNull( e.getChildText("whereLastSeen"));
        assertNull( e.getChildText("whenLastSeen"));

        e = r.store(true);
        assertEquals("IR2", e.getChildText("whereLastSeen"));
        assertNotNull( e.getChildText("whenLastSeen") );

        Timebase tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);
        
        Date now = new Date(1234567890000L);
        tb.setTime(now);

        InstanceManager.getDefault(IdTagManager.class).setFastClockUsed(true);
        r.setWhereLastSeen(rep);
        e = r.store(true);
        assertEquals(new StdDateFormat().format(now), e.getChildText("whenLastSeen"));

    }

    @Test
    public void testLoadData(){

        IdTag r = new DefaultIdTag("ID0373276FC9");
        Element e = new Element("idtag"); // NOI18N

        r.load(e);
        assertNull(r.getUserName());
        assertNull(r.getComment());
        assertNull(r.getWhereLastSeen());
        assertNull(r.getWhenLastSeen());
        assertEquals(0, InstanceManager.getDefault(ReporterManager.class).getNamedBeanSet().size());

        e.addContent(new Element("userName").addContent("loadUname"));
        e.addContent(new Element("comment").addContent("loadComment"));
        e.addContent(new Element("whereLastSeen").addContent("IR1234"));

        // ISO 8601 format JMRI > 5.3.6
        Date now = new Date();
        e.addContent(new Element("whenLastSeen").addContent(new StdDateFormat().format(now))); // NOI18N

        r.load(e);

        assertEquals("loadUname", r.getUserName());
        assertEquals("loadComment", r.getComment());
        assertEquals(1, InstanceManager.getDefault(ReporterManager.class).getNamedBeanSet().size());
        assertNotNull(InstanceManager.getDefault(ReporterManager.class).getBySystemName("IR1234"));
        assertEquals(InstanceManager.getDefault(ReporterManager.class).getBySystemName("IR1234"), r.getWhereLastSeen());

        Date d = r.getWhenLastSeen();
        assertNotNull(d);
        assertEquals(now, d);
    }

    @Test
    public void testLoadPreviousDateFormat(){

        IdTag r = new DefaultIdTag("ID0973236FB9");
        Element e = new Element("idtag"); // NOI18N

        // En-US Format < JMRI < 5.3.6
        e.addContent(new Element("whenLastSeen").addContent("Apr 24, 2020, 5:57:03 PM"));
        r.load(e);

        Date d = r.getWhenLastSeen();
        assertNotNull(d);
        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 3);
        assertEquals(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant()), d);
    }

    @Test
    public void testLastSeenFail(){
        IdTag r = new DefaultIdTag("ID0973999EE3");
        Element e = new Element("idtag");
        e.addContent(new Element("whenLastSeen").addContent("Not ValiD"));
        r.load(e);
        JUnitAppender.assertWarnMessageStartingWith("During load of IdTag \"ID0973999EE3\" Cannot parse date \"Not ValiD\": ");
    }

    @Test
    public void testProvideReporterFail(){
        IdTag r = new DefaultIdTag("ID0973274FA9");
        Element e = new Element("idtag");
        e.addContent(new Element("whereLastSeen").addContent(""));
        r.load(e);
        JUnitAppender.assertErrorMessageStartsWith("Invalid system name for Reporter");
        JUnitAppender.assertWarnMessage("Failed to provide Reporter \"\" in load of \"ID0973274FA9\"");
    }
    
    @Test
    public void testElementNameFail(){
        IdTag r = new DefaultIdTag("ID0673474AA7");
        Element e = new Element("NotAnIdTag");
        r.load(e);
        JUnitAppender.assertErrorMessage("Not an IdTag element: \"NotAnIdTag\" for Tag \"ID0673474AA7\"");
    }

    private static class TestIdTagReporter extends AbstractReporter {

        TestIdTagReporter(String id){
            super(id);
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public void setState(int s) {
            state = s;
        }
        int state = 0;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initIdTagManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
