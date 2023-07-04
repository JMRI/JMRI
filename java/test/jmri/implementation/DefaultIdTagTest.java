package jmri.implementation;

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
import org.junit.Assert;

/**
 * Tests for the DefaultIdTag class
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class DefaultIdTagTest {

    @Test
    public void testCreateIdTag() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertNotNull("IdTag not null", r);
    }

    @Test
    public void testGetIdTagUserName() {
        IdTag r = new DefaultIdTag("ID0413276BC1", "Test Tag");
        Assert.assertEquals("IdTag user name is 'Test Tag'", "Test Tag", r.getUserName());
    }

    @Test
    public void testGetIdTagTagID() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag TagID is 0413276BC1", "0413276BC1", r.getTagID());
    }

    @Test
    public void testIdTagToString() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag toString is ID0413276BC1", "ID0413276BC1", r.toString());
    }

    @Test
    public void testIdTagToReportString() {
        DefaultIdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertEquals("IdTag toReportString is 0413276BC1", "0413276BC1", r.toReportString());

        r.setUserName("Test Tag");
        Assert.assertEquals("IdTag toReportString is 'Test Tag'", "Test Tag", r.toReportString());
    }

    @Test
    public void testNotYetSeen() {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Assert.assertNull("At creation, Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("At creation, Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("At creation, IdTag status is UNSEEN", IdTag.UNSEEN, r.getState());

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), IdTag status is UNSEEN", IdTag.UNSEEN, r.getState());
    }

    @Test
    public void testHasBeenSeen() throws InterruptedException {
        IdTag r = new DefaultIdTag("ID0413276BC1");
        Reporter rep = new TestIdTagReporter("IR1");

        Date timeBefore = Calendar.getInstance().getTime();
        Thread.sleep(5);
        r.setWhereLastSeen(rep);
        Thread.sleep(5);
        Date timeAfter = Calendar.getInstance().getTime();
        Date whenLastSeen = r.getWhenLastSeen();

        Assert.assertEquals("Where last seen is 'IR1'", rep, r.getWhereLastSeen());
        Assert.assertNotNull("When last seen is not null", whenLastSeen);
        Assert.assertEquals("Status is SEEN", IdTag.SEEN, r.getState());
        Assert.assertTrue("Time when last seen is later than 'timeBefore'", whenLastSeen.after(timeBefore));
        Assert.assertTrue("Time when last seen is earlier than 'timeAfter'", whenLastSeen.before(timeAfter));

        r.setWhereLastSeen(null);
        Assert.assertNull("After setWhereLastSeen(null), Reporter where seen is null", r.getWhereLastSeen());
        Assert.assertNull("After setWhereLastSeen(null), Date when seen is null", r.getWhenLastSeen());
        Assert.assertEquals("After setWhereLastSeen(null), IdTag status is UNSEEN", IdTag.UNSEEN, r.getState());

    }

    @Test
    public void testStoreTag() {
        IdTag r = new DefaultIdTag("ID0213276BC5");
        Reporter rep = new TestIdTagReporter("IR2");

        Element e = r.store(true);
        Assertions.assertNotNull(e);
        Assertions.assertEquals("idtag", e.getName());

        Assertions.assertEquals("ID0213276BC5", e.getChildText("systemName"));
        Assertions.assertNull( e.getChildText("userName"));
        Assertions.assertNull( e.getChildText("comment"));
        Assertions.assertNull( e.getChildText("whereLastSeen"));
        Assertions.assertNull( e.getChildText("whenLastSeen"));

        r.setUserName("");
        r.setComment("");
        e = r.store(false);
        Assertions.assertNull( e.getChildText("userName"));
        Assertions.assertNull( e.getChildText("comment"));

        r.setUserName("Test UNamE");
        r.setComment("Test CommenT");
        e = r.store(true);
        Assertions.assertEquals("Test UNamE", e.getChildText("userName"));
        Assertions.assertEquals("Test CommenT", e.getChildText("comment"));

        r.setWhereLastSeen(rep);
        e = r.store(false);
        Assertions.assertNull( e.getChildText("whereLastSeen"));
        Assertions.assertNull( e.getChildText("whenLastSeen"));

        e = r.store(true);
        Assertions.assertEquals("IR2", e.getChildText("whereLastSeen"));
        Assertions.assertNotNull( e.getChildText("whenLastSeen") );

        Timebase tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);
        
        Date now = new Date(1234567890000L);
        tb.setTime(now);

        InstanceManager.getDefault(IdTagManager.class).setFastClockUsed(true);
        r.setWhereLastSeen(rep);
        e = r.store(true);
        Assertions.assertEquals(new StdDateFormat().format(now), e.getChildText("whenLastSeen"));

    }

    @Test
    public void testLoadData(){

        IdTag r = new DefaultIdTag("ID0373276FC9");
        Element e = new Element("idtag"); // NOI18N

        r.load(e);
        Assertions.assertNull(r.getUserName());
        Assertions.assertNull(r.getComment());
        Assertions.assertNull(r.getWhereLastSeen());
        Assertions.assertNull(r.getWhenLastSeen());
        Assertions.assertEquals(0, InstanceManager.getDefault(ReporterManager.class).getNamedBeanSet().size());

        e.addContent(new Element("userName").addContent("loadUname"));
        e.addContent(new Element("comment").addContent("loadComment"));
        e.addContent(new Element("whereLastSeen").addContent("IR1234"));

        // ISO 8601 format JMRI > 5.3.6
        Date now = new Date();
        e.addContent(new Element("whenLastSeen").addContent(new StdDateFormat().format(now))); // NOI18N

        r.load(e);

        Assertions.assertEquals("loadUname", r.getUserName());
        Assertions.assertEquals("loadComment", r.getComment());
        Assertions.assertEquals(1, InstanceManager.getDefault(ReporterManager.class).getNamedBeanSet().size());
        Assertions.assertNotNull(InstanceManager.getDefault(ReporterManager.class).getBySystemName("IR1234"));
        Assertions.assertEquals(InstanceManager.getDefault(ReporterManager.class).getBySystemName("IR1234"), r.getWhereLastSeen());

        Date d = r.getWhenLastSeen();
        Assertions.assertNotNull(d);
        Assertions.assertEquals(now, d);
    }
    
    @Test
    public void testLoadPreviousDateFormat(){

        IdTag r = new DefaultIdTag("ID0973236FB9");
        Element e = new Element("idtag"); // NOI18N

        // En-US Format < JMRI < 5.3.6
        e.addContent(new Element("whenLastSeen").addContent("Apr 24, 2020, 5:57:03 PM"));
        r.load(e);

        Date d = r.getWhenLastSeen();
        Assertions.assertNotNull(d);
        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 3);
        Assertions.assertEquals(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant()), d);
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

    static class TestIdTagReporter extends AbstractReporter {

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
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initIdTagManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
