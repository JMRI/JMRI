package jmri.jmrix.xpa;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * XpaMessageTest.java
 *
 * Description: tests for the jmri.jmrix.xpa.XpaMessage class
 *
 * @author Paul Bender
 */
public class XpaMessageTest {

    @Test
    public void testCtor() {
        XpaMessage m = new XpaMessage(3);
        Assert.assertNotNull("Length Constructor Succeeded", m);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    @Test
    public void testStringConstructor() {
        String s = "ATDT0;";
        XpaMessage m = new XpaMessage(s);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", 6, m.getNumDataElements());
    }

    @Test
    public void testDefaultCtor() {
        XpaMessage m = new XpaMessage();
        Assert.assertNotNull("Length Constructor Succeeded", m);
        Assert.assertEquals("length", XpaMessage.MAX_SIZE, m.getNumDataElements());
    }

    @Test
    public void testCopyCtor() {
        String s = "ATDT0;";
        XpaMessage m = new XpaMessage(s);
        XpaMessage m2 = new XpaMessage(m);
        Assert.assertNotNull("String Constructor Succeeded", m2);
        Assert.assertEquals("length", m.getNumDataElements(), m2.getNumDataElements());
        Assert.assertTrue("content", m.equals(m2));
    }

    @Test
    public void testGetNumDataElements() {
        XpaMessage m = new XpaMessage("ATDT0;");
        Assert.assertTrue("Num Data Elements correct", 6 == m.getNumDataElements());
    }

    @Test
    public void testSetAndGetElement() {
        XpaMessage m = new XpaMessage();
        m.setElement(0, 'A');
        Assert.assertTrue("Element 0 Set to \'A\'", 'A' == m.getElement(0));
    }

    @Test
    public void testToString() {
        String s = "ATDT0;";
        XpaMessage m = new XpaMessage(s);
        Assert.assertTrue("toString Result", m.toString().equals(s));
    }

    @Test
    public void testEquals() {
        String s1 = "ATDT0;";
        XpaMessage m1 = new XpaMessage(s1);
        XpaMessage m2 = new XpaMessage(s1);
        String s2 = "ATDT1;";
        XpaMessage m3 = new XpaMessage(s2);
        Assert.assertTrue("Messaes Equal", m1.equals(m2));
        Assert.assertFalse("Messaes Not Equal", m1.equals(m3));
    }

    // test canned message generation
    @Test
    public void testGetDefaultInitMsg() {
        String s = "ATX0E0;"; // expected value.
        XpaMessage m = XpaMessage.getDefaultInitMsg();
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetEStopMsg() {
        String s = "ATDT0;"; // expected value.
        XpaMessage m = XpaMessage.getEStopMsg();
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetIdleMsg() {
        String s = "ATDT#65*5;"; // expected value.
        XpaMessage m = XpaMessage.getIdleMsg(65);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetIncreaseSpeedMsg() {
        String s = "ATDT#65*33333;"; // expected value.
        XpaMessage m = XpaMessage.getIncSpeedMsg(65, 5);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetDecreaseSpeedMsg() {
        String s = "ATDT#65*11111;"; // expected value.
        XpaMessage m = XpaMessage.getDecSpeedMsg(65, 5);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetDirForwardMsg() {
        String s = "ATDT#65*52;"; // expected value.
        XpaMessage m = XpaMessage.getDirForwardMsg(65);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetDirReverseMsg() {
        String s = "ATDT#65*58;"; // expected value.
        XpaMessage m = XpaMessage.getDirReverseMsg(65);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetToggleFunctionMsg() {
        String s = "ATDT#65**0;"; // expected value.
        XpaMessage m = XpaMessage.getFunctionMsg(65, 0);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    // switch commands
    @Test
    public void testGetSwitchNormalMsg() {
        String s = "ATDT#65#3;"; // expected value.
        XpaMessage m = XpaMessage.getSwitchNormalMsg(65);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetSwitchReveseMsg() {
        String s = "ATDT#65#1;"; // expected value.
        XpaMessage m = XpaMessage.getSwitchReverseMsg(65);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Test
    public void testGetDeviceSettingMsg() {
        String s = "ATDT*5*"; // expected value.
        XpaMessage m = XpaMessage.getDeviceSettingMsg(5);
        Assert.assertNotNull("String Constructor Succeeded", m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
