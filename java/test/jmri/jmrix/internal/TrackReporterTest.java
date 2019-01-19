package jmri.jmrix.internal;

import jmri.implementation.StringReport;
import jmri.util.JUnitUtil;
import org.junit.*;

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
       tr.pushEast(new StringReport("Hello"));
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport().getString());
       tr.pushEast(new StringReport("World"));
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport().getString());
       Assert.assertEquals("pull last entered","World",tr.pullEast().getString());
       Assert.assertEquals("last report","Hello",tr.getCurrentReport().getString());
       Assert.assertEquals("pull first entered","Hello",tr.pullEast().getString());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Test
   public void testSingleEndedTrackWest(){
       // this track should work like a stack, add or remove from one end only.
       TrackReporter tr = (TrackReporter)r;
       tr.pushWest(new StringReport("Hello"));
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport().getString());
       tr.pushWest(new StringReport("World"));
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport().getString());
       Assert.assertEquals("pull last entered","World",tr.pullWest().getString());
       Assert.assertEquals("last report","Hello",tr.getCurrentReport().getString());
       Assert.assertEquals("pull first entered","Hello",tr.pullWest().getString());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Test
   public void testDoubleEndedTrack(){
       // this track should work like a queue, add from one end remove from the other.
       TrackReporter tr = (TrackReporter)r;
       tr.pushWest(new StringReport("Hello"));
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport().getString());
       tr.pushWest(new StringReport("World"));
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport().getString());
       Assert.assertEquals("pull first entered","Hello",tr.pullEast().getString());
       Assert.assertEquals("last report","World",tr.getCurrentReport().getString());
       Assert.assertEquals("pull last entered","World",tr.pullEast().getString());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Override
   @Before
   public void setUp() {
        JUnitUtil.setUp();
        r = new TrackReporter("TrackR1","hello world");

   }

   @Override
   @After
   public void tearDown(){
	   r = null;
       JUnitUtil.tearDown();
   }

}
