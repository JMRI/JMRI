package jmri.jmrit.simpleclock;

import java.beans.*;
import java.time.Instant;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the SimpleTimebase class
 *
 * @author	Bob Jacobsen
 */
public class SimpleTimebaseTest extends TestCase {

    void wait(int msec) {
        try {
            super.wait(msec);
        } catch (Exception e) {
        }
    }

    // test creation
    public void testCreate() {
        SimpleTimebase p = new SimpleTimebase();
        Assert.assertNotNull("exists", p);
        p.dispose();
    }

    // test quick access (should be quite close to zero)
    public void testNoDelay() {
        SimpleTimebase p = new SimpleTimebase();
        Date now = new Date();
        p.setTime(now);
        Date then = p.getTime();
        long delta = then.getTime() - now.getTime();
        Assert.assertTrue("delta ge zero", delta >= 0);
        Assert.assertTrue("delta lt 100 msec (nominal value)", delta < 100);
        p.dispose();
    }

    public void testGetBeanType() {
        SimpleTimebase p = new SimpleTimebase();
        Assert.assertEquals("Time", p.getBeanType());
    }
    
    public void testSetStartTime() {
        SimpleTimebase p = new SimpleTimebase();
        p.setRun(false); // prevent clock ticking during test

        Date now = new Date();

        p.setStartSetTime(true, now);

        Assert.assertTrue("startSetTime true", p.getStartSetTime());

        p.setStartSetTime(false, now);
        Assert.assertTrue("startSetTime false", !p.getStartSetTime());

        Assert.assertEquals("setTime now", now, p.getStartTime());

        Date then = new Date(now.getTime() + 100);
        p.setStartSetTime(false, then);

        Assert.assertEquals("setTime then", then, p.getStartTime());
        p.dispose();
    }

    // set the time based on a date.
    public void testSetTimeDate() {
        SimpleTimebase p = new SimpleTimebase();
        p.setRun(false); // prevent clock ticking during test
        Assert.assertFalse(p.getRun());

        Date now = new Date();

        p.setTime(now);
        Assert.assertFalse(p.getRun());  // still
        Assert.assertEquals("Time Set",now.toString(),p.getTime().toString());
        
        p.setRun(true);       
        Assert.assertTrue(p.getRun());

        p.dispose();
    }

    // set the time based on an instant.
    public void testSetTimeInstant() {
        SimpleTimebase p = new SimpleTimebase();
        p.setRun(false); // prevent clock ticking during test

        Instant now = Instant.now();
        
        p.setTime(now);
        Assert.assertEquals("Time Set",Date.from(now).toString(),p.getTime().toString());
        p.dispose();
    }

    public void testSetGetRate() {
        SimpleTimebase p = new SimpleTimebase();
        p.setRun(false); // prevent clock ticking during test

        Assert.assertEquals(1.0, p.getRate(), 0.01);
        
        p.setRate(2.0);
        Assert.assertEquals(2.0, p.getRate(), 0.01);        
        Assert.assertFalse(p.getRun());  // still
        
    }
    
    double seenNewMinutes;
    double seenOldMinutes;
    
    public void testSetSendsUpdate() {
        SimpleTimebase p = new SimpleTimebase();
        p.setRun(false); // prevent clock ticking during test

        seenNewMinutes = -1;
        seenOldMinutes = -1;
        p.addMinuteChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                seenOldMinutes = (Double) e.getOldValue();
                seenNewMinutes = (Double) e.getNewValue();
            }
        });

        Date date = p.getTime();
        Date tenMinLater = new Date(10*60*1000L+date.getTime());
        
        p.setTime(tenMinLater);
        Assert.assertEquals(seenOldMinutes + 10.0, seenNewMinutes, 0.01);
    }
    
    /* 	public void testShortDelay() { */
    /* 		SimpleTimebase p = new SimpleTimebase(); */
    /* 		Date now = new Date(); */
    /* 		p.setTime(now); */
    /* 		p.setRate(100.); */
    /* 		wait(100); */
    /* 		Date then = p.getTime(); */
    /* 		long delta = then.getTime()-now.getTime(); */
    /* 		Assert.assertTrue("delta ge 50 (nominal value)", delta>=50); */
    /* 		Assert.assertTrue("delta lt 150 (nominal value)", delta<150); */
    /* 	} */
    // from here down is testing infrastructure
    public SimpleTimebaseTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleTimebaseTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleTimebaseTest.class);
        return suite;
    }

    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
