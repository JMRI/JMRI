package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XpaMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaMessage class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XpaMessageTest extends TestCase {

    public void testCtor() {
        XpaMessage m = new XpaMessage(3);
        Assert.assertNotNull("Length Constructor Succeeded",m);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    public void testStringConstructor(){
        String s = "ATDT0;";
        XpaMessage m = new XpaMessage(s);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", 6, m.getNumDataElements());
    }

    public void testDefaultCtor() {
        XpaMessage m = new XpaMessage();
        Assert.assertNotNull("Length Constructor Succeeded",m);
        Assert.assertEquals("length", XpaMessage.maxSize, m.getNumDataElements());
    }

    public void testCopyCtor(){
        String s = "ATDT0;";
        XpaMessage m = new XpaMessage(s);
        XpaMessage m2 = new XpaMessage(m);
        Assert.assertNotNull("String Constructor Succeeded",m2);
        Assert.assertEquals("length", m.getNumDataElements(), m2.getNumDataElements());
        Assert.assertTrue("content", m.Equals(m2));
    }

    public void testGetNumDataElements(){
        XpaMessage m = new XpaMessage("ATDT0;");
        Assert.assertTrue("Num Data Elements correct",6==m.getNumDataElements());
    }

    public void testSetAndGetElement(){
        XpaMessage m = new XpaMessage();
        m.setElement(0,'A');
        Assert.assertTrue("Element 0 Set to \'A\'",'A'==m.getElement(0));
    }

    public void testToString(){
       String s = "ATDT0;";
       XpaMessage m = new XpaMessage(s);
       Assert.assertTrue("toString Result",m.toString().equals(s));
    }

    public void testEquals(){
       String s1 = "ATDT0;";
       XpaMessage m1 = new XpaMessage(s1);
       XpaMessage m2 = new XpaMessage(s1);
       String s2 = "ATDT1;";
       XpaMessage m3 = new XpaMessage(s2);
       Assert.assertTrue("Messaes Equal",m1.Equals(m2));
       Assert.assertFalse("Messaes Not Equal",m1.Equals(m3));
    }


    // test canned message generation
    public void testGetDefaultInitMsg(){
        String s = "ATX0E0;"; // expected value.
        XpaMessage m = XpaMessage.getDefaultInitMsg();
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetEStopMsg(){
        String s = "ATDT0;"; // expected value.
        XpaMessage m = XpaMessage.getEStopMsg();
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetIdleMsg(){
        String s = "ATDT#65*5;"; // expected value.
        XpaMessage m = XpaMessage.getIdleMsg(65);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetIncreaseSpeedMsg(){
        String s = "ATDT#65*33333;"; // expected value.
        XpaMessage m = XpaMessage.getIncSpeedMsg(65,5);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetDecreaseSpeedMsg(){
        String s = "ATDT#65*11111;"; // expected value.
        XpaMessage m = XpaMessage.getDecSpeedMsg(65,5);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetDirForwardMsg(){
        String s = "ATDT#65*52;"; // expected value.
        XpaMessage m = XpaMessage.getDirForwardMsg(65);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetDirReverseMsg(){
        String s = "ATDT#65*58;"; // expected value.
        XpaMessage m = XpaMessage.getDirReverseMsg(65);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetToggleFunctionMsg(){
        String s = "ATDT#65**0;"; // expected value.
        XpaMessage m = XpaMessage.getFunctionMsg(65,0);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    // switch commands
    public void testGetSwitchNormalMsg(){
        String s = "ATDT#65#3;"; // expected value.
        XpaMessage m = XpaMessage.getSwitchNormalMsg(65);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetSwitchReveseMsg(){
        String s = "ATDT#65#1;"; // expected value.
        XpaMessage m = XpaMessage.getSwitchReverseMsg(65);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }

    public void testGetDeviceSettingMsg(){
        String s = "ATDT*5*"; // expected value.
        XpaMessage m = XpaMessage.getDeviceSettingMsg(5);
        Assert.assertNotNull("String Constructor Succeeded",m);
        Assert.assertEquals("length", s.length(), m.getNumDataElements());
        Assert.assertTrue("content", s.equals(m.toString()));
    }


    // from here down is testing infrastructure
    public XpaMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaMessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaMessageTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
