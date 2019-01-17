package jmri.jmrix.internal;

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
       tr.pushEast("Hello");
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport());
       tr.pushEast("World");
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport());
       Assert.assertEquals("pull last entered","World",tr.pullEast());
       Assert.assertEquals("last report","Hello",tr.getCurrentReport());
       Assert.assertEquals("pull first entered","Hello",tr.pullEast());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Test
   public void testSingleEndedTrackWest(){
       // this track should work like a stack, add or remove from one end only.
       TrackReporter tr = (TrackReporter)r;
       tr.pushWest("Hello");
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport());
       tr.pushWest("World");
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport());
       Assert.assertEquals("pull last entered","World",tr.pullWest());
       Assert.assertEquals("last report","Hello",tr.getCurrentReport());
       Assert.assertEquals("pull first entered","Hello",tr.pullWest());
       Assert.assertNull("last report",tr.getCurrentReport());
   }

   @Test
   public void testDoubleEndedTrack(){
       // this track should work like a queue, add from one end remove from the other.
       TrackReporter tr = (TrackReporter)r;
       tr.pushWest("Hello");
       Assert.assertEquals("after 1st push","Hello",tr.getCurrentReport());
       tr.pushWest("World");
       Assert.assertEquals("after 2nd push","World",tr.getCurrentReport());
       Assert.assertEquals("pull first entered","Hello",tr.pullEast());
       Assert.assertEquals("last report","World",tr.getCurrentReport());
       Assert.assertEquals("pull last entered","World",tr.pullEast());
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
