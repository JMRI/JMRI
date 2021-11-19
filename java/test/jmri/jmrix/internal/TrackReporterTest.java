package jmri.jmrix.internal;

import jmri.ExtendedReport;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for TrackReporter class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class TrackReporterTest extends jmri.implementation.AbstractRailComReporterTest {

   @Test
   public void testSingleEndedTrackEast(){
       // this track should work like a stack, add or remove from one end only.
       TrackReporter tr = (TrackReporter)r;
       tr.pushEast(new ExtendedReport.StringReport("Hello"));
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport().toString());
       tr.pushEast(new ExtendedReport.StringReport("World"));
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport().toString());
       Assert.assertEquals("pull last entered","World",tr.pullEast().toString());
       Assert.assertEquals("last report","Hello",tr.getCurrentReport().toString());
       Assert.assertEquals("pull first entered","Hello",tr.pullEast().toString());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Test
   public void testSingleEndedTrackWest(){
       // this track should work like a stack, add or remove from one end only.
       TrackReporter tr = (TrackReporter)r;
       tr.pushWest(new ExtendedReport.StringReport("Hello"));
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport().toString());
       tr.pushWest(new ExtendedReport.StringReport("World"));
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport().toString());
       Assert.assertEquals("pull last entered","World",tr.pullWest().toString());
       Assert.assertEquals("last report","Hello",tr.getCurrentReport().toString());
       Assert.assertEquals("pull first entered","Hello",tr.pullWest().toString());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Test
   public void testDoubleEndedTrack(){
       // this track should work like a queue, add from one end remove from the other.
       TrackReporter tr = (TrackReporter)r;
       tr.pushWest(new ExtendedReport.StringReport("Hello"));
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport().toString());
       tr.pushWest(new ExtendedReport.StringReport("World"));
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport().toString());
       Assert.assertEquals("pull first entered","Hello",tr.pullEast().toString());
       Assert.assertEquals("last report","World",tr.getCurrentReport().toString());
       Assert.assertEquals("pull last entered","World",tr.pullEast().toString());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Override
   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();
        r = new TrackReporter("TrackR1","hello world");

   }

   @Override
   @AfterEach
   public void tearDown(){
       r = null;
       JUnitUtil.tearDown();
   }

}
