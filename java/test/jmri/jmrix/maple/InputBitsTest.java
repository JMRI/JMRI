package jmri.jmrix.maple;

import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the InputBits class.
 *
 * @author	Dave Duchamp 2009
 */
public class InputBitsTest {

    @Test
    public void testConstructor1() {
        Assert.assertNotNull("check instance", ibit);
    }

    @Test
    public void testAccessors() {
        InputBits.setNumInputBits(72);
        InputBits.setTimeoutTime(1500);
        Assert.assertEquals("check numInputBits", 72, InputBits.getNumInputBits());
        Assert.assertEquals("check timeoutTime", 1500, InputBits.getTimeoutTime());
    }

    @Test
    public void testMarkChangesInitial() {
        SerialSensor s1 = new SerialSensor("KS1", "a");
        Assert.assertEquals("check bit number", 1, SerialAddress.getBitFromSystemName("KS1", "K"));
        SerialSensor s2 = new SerialSensor("KS2", "ab");
        SerialSensor s3 = new SerialSensor("KS3", "abc");
        SerialSensor s6 = new SerialSensor("KS6", "abcd");
        ibit.registerSensor(s1, 0);
        ibit.registerSensor(s2, 1);
        ibit.registerSensor(s3, 2);
        ibit.registerSensor(s6, 5);
        Assert.assertEquals("check lastUsedSensor", 5, ibit.getLastSensor());
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        // set reply all zero
        for (int i = 5; i < 5 + 48; i++) {
            r.setElement(i, '0');
        }

        // and interpret
        ibit.markChanges(r);
        ibit.makeChanges();

        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("check s6", Sensor.INACTIVE, s6.getKnownState());
    }

    @Test
    public void testForceUnknown() {
        SerialSensor s1 = new SerialSensor("KS1", "a");
        SerialSensor s2 = new SerialSensor("KS2", "ab");
        SerialSensor s3 = new SerialSensor("KS3", "abc");
        SerialSensor s6 = new SerialSensor("KS6", "abcd");
        ibit.registerSensor(s1, 0);
        ibit.registerSensor(s2, 1);
        ibit.registerSensor(s3, 2);
        ibit.registerSensor(s6, 5);
        SerialReply r = new SerialReply();
        // set reply all zero
        for (int i = 5; i < 5 + 48; i++) {
            r.setElement(i, '0');
        }
        ibit.markChanges(r);
        ibit.makeChanges();
        Assert.assertEquals("check inactive s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check inactive s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("check inactive s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("check inactive s6", Sensor.INACTIVE, s6.getKnownState());
        // force all sensors to unknown state
        ibit.forceSensorsUnknown();
        Assert.assertEquals("check unknown s1", Sensor.UNKNOWN, s1.getKnownState());
        Assert.assertEquals("check unknown s2", Sensor.UNKNOWN, s2.getKnownState());
        Assert.assertEquals("check unknown s3", Sensor.UNKNOWN, s3.getKnownState());
        Assert.assertEquals("check unknown s6", Sensor.UNKNOWN, s6.getKnownState());
    }

    @Test
    public void testMarkChangesDebounce() {
        SerialSensor s1 = new SerialSensor("KS1", "a");
        SerialSensor s2 = new SerialSensor("KS2", "ab");
        SerialSensor s3 = new SerialSensor("KS3", "abc");
        SerialSensor s4 = new SerialSensor("KS4", "abcd");
        ibit.registerSensor(s1, 0);
        ibit.registerSensor(s2, 1);
        ibit.registerSensor(s3, 2);
        ibit.registerSensor(s4, 3);
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        // set reply all zero
        for (int i = 5; i < 5 + 48; i++) {
            r.setElement(i, '0');
        }
        // and interpret
        ibit.markChanges(r);
        ibit.makeChanges();
        Assert.assertEquals("poll0 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll0 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll0 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll0 s4", Sensor.INACTIVE, s4.getKnownState());

        // check out OR ing of bits among different panels
        ibit.markChanges(r);
        r.setElement(6, '1');
        ibit.markChanges(r);
        r.setElement(6, '0');
        ibit.markChanges(r);
        ibit.makeChanges();
        Assert.assertEquals("poll1 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll1 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll1 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll1 s4", Sensor.INACTIVE, s4.getKnownState());
        ibit.markChanges(r);
        r.setElement(6, '1');
        ibit.markChanges(r);
        r.setElement(6, '0');
        ibit.markChanges(r);
        ibit.makeChanges();
        Assert.assertEquals("poll10 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll10 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll10 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll10 s4", Sensor.INACTIVE, s4.getKnownState());
        // single poll shouldn't change
        ibit.markChanges(r);
        ibit.markChanges(r);
        ibit.markChanges(r);
        ibit.makeChanges();
        Assert.assertEquals("poll2 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll2 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll2 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll2 s4", Sensor.INACTIVE, s4.getKnownState());
        // 2nd poll should, but only if same
        r.setElement(5, '1');
        r.setElement(6, '0');
        r.setElement(7, '1');
        ibit.markChanges(r);
        r.setElement(5, '0');
        r.setElement(7, '0');
        ibit.markChanges(r);
        ibit.markChanges(r);
        ibit.makeChanges();
        Assert.assertEquals("poll3 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll3 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll3 s3", Sensor.INACTIVE, s3.getKnownState());
        Assert.assertEquals("poll3 s4", Sensor.INACTIVE, s4.getKnownState());
        // 3rd poll changes last two
        r.setElement(5, '1');
        r.setElement(6, '0');
        r.setElement(7, '1');
        ibit.markChanges(r);
        r.setElement(5, '0');
        r.setElement(7, '0');
        ibit.markChanges(r);
        ibit.markChanges(r);
        ibit.makeChanges();
        Assert.assertEquals("poll4 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll4 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll4 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll4 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll4 s4", Sensor.INACTIVE, s4.getKnownState());
    }

    private InputBits ibit;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SerialTrafficControlScaffold tc = new SerialTrafficControlScaffold();
        ibit = new InputBits(tc);
    }

    @After
    public void tearDown() {
        ibit = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
