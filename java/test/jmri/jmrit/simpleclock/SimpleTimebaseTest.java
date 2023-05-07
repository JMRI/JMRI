package jmri.jmrit.simpleclock;

import java.beans.*;
import java.io.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;

import org.junit.jupiter.api.*;
import org.junit.Assert;

import jmri.InstanceManager;
import jmri.TimebaseRateException;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

/**
 * Tests for the SimpleTimebase class
 *
 * @author Bob Jacobsen
 */
public class SimpleTimebaseTest {

    private InternalSystemConnectionMemo memo = null;

    // test creation
    @Test
    public void testCreate() {
        SimpleTimebase p = new SimpleTimebase(memo);
        Assert.assertNotNull("exists", p);
        p.dispose();
    }

    // test quick access (should be quite close to zero)
    @Test
    public void testNoDelay() {
        SimpleTimebase p = new SimpleTimebase(memo);
        Date now = new Date();
        p.setTime(now);
        Date then = p.getTime();
        long delta = then.getTime() - now.getTime();
        Assert.assertTrue("delta ge zero", delta >= 0);
        Assert.assertTrue("delta lt 100 ms (nominal value)", delta < 100);
        p.dispose();
    }

    @Test
    public void testGetBeanType() {
        SimpleTimebase p = new SimpleTimebase(memo);
        Assert.assertEquals("Time", p.getBeanType());

        p.dispose();
    }

    @Test
    public void testSetStartTime() {
        SimpleTimebase p = new SimpleTimebase(memo);
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
    @Test
    public void testSetTimeDate() {
        SimpleTimebase p = new SimpleTimebase(memo);
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
    @Test
    public void testSetTimeInstant() {
        SimpleTimebase p = new SimpleTimebase(memo);
        p.setRun(false); // prevent clock ticking during test

        Instant now = Instant.now();

        p.setTime(now);
        Assert.assertEquals("Time Set",Date.from(now).toString(),p.getTime().toString());
        p.dispose();
    }

    @Test
    public void testSetGetRate() throws TimebaseRateException {
        SimpleTimebase p = new SimpleTimebase(memo);
        p.setRun(false); // prevent clock ticking during test

        Assert.assertEquals(1.0, p.getRate(), 0.01);

        p.setRate(2.0);
        Assert.assertEquals(2.0, p.getRate(), 0.01);
        Assert.assertFalse(p.getRun());  // still

        p.dispose();
    }

    double seenNewMinutes;
    double seenOldMinutes;

    @Test
    public void testSetSendsUpdate() {
        SimpleTimebase p = new SimpleTimebase(memo);
        p.setRun(false); // prevent clock ticking during test

        seenNewMinutes = -1;
        seenOldMinutes = -1;
        p.addMinuteChangeListener((PropertyChangeEvent e) -> {
            seenOldMinutes = (Double) e.getOldValue();
            seenNewMinutes = (Double) e.getNewValue();
        });

        Date date = p.getTime();
        Date tenMinLater = new Date(10*60*1000L+date.getTime());

        p.setTime(tenMinLater);

        // minutes wrap at 60
        if (seenNewMinutes < seenOldMinutes) seenNewMinutes += 60.;

        Assert.assertEquals(seenOldMinutes + 10.0, seenNewMinutes, 0.01);

        p.dispose();
    }

    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    @Test
    public void testTimeListener() throws TimebaseRateException {
        SimpleTimebase instance = new SimpleTimebase(memo);
        TestTimebaseTimeListener l1 = new TestTimebaseTimeListener();
        TestTimebaseTimeListener l2 = new TestTimebaseTimeListener();
        Assert.assertNull(l1.getTime());
        Assert.assertNull(l2.getTime());
        instance.addPropertyChangeListener("time", l1);
        instance.addPropertyChangeListener("time", l2);
        instance.setRate(60); // one minute a second
        Date start = new Date();
        instance.setTime(start);
        instance.setRun(true);
        JUnitUtil.waitFor(() -> {
            return instance.getTime().getMinutes() != start.getMinutes();
        },"getMinutes increased");
        instance.setRun(false);
        Assert.assertNotNull(l1.getTime());
        Assert.assertNotNull(l2.getTime());
        Assert.assertEquals(l1.getTime(), l2.getTime());

        instance.dispose();
    }

    @Test
    @Disabled("Disabled in JUnit 3")
    public void testShortDelay() throws TimebaseRateException {
        SimpleTimebase p = new SimpleTimebase(memo);
        Date now = new Date();
        p.setTime(now);
        p.setRate(100.);
        JUnitUtil.waitFor(100);
        Date then = p.getTime();
        long delta = then.getTime() - now.getTime();
        Assert.assertTrue("delta ge 50 (nominal value)", delta >= 50);
        Assert.assertTrue("delta lt 150 (nominal value)", delta < 150);

        p.dispose();
    }

    @Test
    public void testBundleTimeStorageFormats() {
        String classToSearchFor = "SimpleTimebase.class";
        try {
            var resource = SimpleTimebase.class.getResource(classToSearchFor);
            if (resource != null) {
                String s = resource.toURI().getPath();
                if (s != null) {
                    checkTimeStorageFormatsInFolder(new File(
                        s.substring(0, s.length() - classToSearchFor.length())));
                } else {
                    Assertions.fail("No Path for " + resource.getFile());
                }
            } else {
                Assertions.fail("No Resource for " + classToSearchFor);
            }
        } catch (URISyntaxException | IOException ex) {
            Assertions.fail("Exception for SimpleTimebaseTest testBundleTimeStorageFormats", ex);
        }
    }

    private void checkTimeStorageFormatsInFolder(@javax.annotation.Nonnull File folder) throws IOException {
        // System.out.println("checking folder " + folder.getPath());
        File[] files = folder.listFiles();
        if (files != null){
            for(File file : files) {
                // System.out.println("checking file " + file);
                if(file.getName().endsWith(".properties")) {
                    Properties prop = new Properties();
                    try (FileInputStream instream = new FileInputStream(file)) {
                        prop.load(instream);
                    }
                    String dateFormat = prop.getProperty("TimeStorageFormat"); // NOI18N
                    if (dateFormat != null){
                        try {
                            SimpleDateFormat timeStorageFormat = new SimpleDateFormat( dateFormat);
                            // System.out.println("DateFormat ok " + dateFormat +" "+ timeStorageFormat);
                            Assertions.assertTrue(true,"Message format ok: " + timeStorageFormat);
                        } catch (IllegalArgumentException e) {
                            Assertions.fail("Could not convert TimeStorageFormat to SimpleDateFormat in " + file, e);
                        }
                    } // not a problem if Bundle goes not contain key
                } // not a problem if file does not end with .properties
            } // end of file loop
        } else {
            Assertions.fail("Null listFiles in folder " + folder);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

    private static class TestTimebaseTimeListener implements PropertyChangeListener {

        private Date time = null;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            time = (Date) evt.getNewValue();
        }

        public Date getTime() {
            return time;
        }

    }
}
