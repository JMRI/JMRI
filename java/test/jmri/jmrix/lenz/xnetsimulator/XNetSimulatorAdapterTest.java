package jmri.jmrix.lenz.xnetsimulator;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetSimulatorAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter
 * class
 *
 * @author	Paul Bender
 */
public class XNetSimulatorAdapterTest {

    @Test
    public void testCtor() {
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        Assert.assertNotNull(a);
    }

    @Test
    public void testGenerateCSVersionReply(){
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", XNetMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in XNetSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           XNetReply r = (XNetReply) generateReplyMethod.invoke(a,new XNetMessage("21 21 00"));

           Assert.assertEquals("CS Version Reply",new XNetReply("63 21 36 00 74"),r);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method generateReply in XNetSimulatoradapter class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("generateReply execution failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testGenerateResumeOperationsReply(){
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", XNetMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in XNetSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           XNetReply r = (XNetReply) generateReplyMethod.invoke(a,new XNetMessage("21 81 A0"));

           Assert.assertEquals("CS Resume Operations Reply",new XNetReply("61 82 E3"),r);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method generateReply in XNetSimulatoradapter class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("generateReply execution failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testGenerateEmergencyStopReply(){
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", XNetMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in XNetSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           XNetReply r = (XNetReply) generateReplyMethod.invoke(a,new XNetMessage("21 80 A1"));

           Assert.assertEquals("CS Emergency Stop Reply",new XNetReply("61 82 E3"),r);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method generateReply in XNetSimulatoradapter class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("generateReply execution failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testEmergencyStopAllReply(){
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", XNetMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in XNetSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           XNetReply r = (XNetReply) generateReplyMethod.invoke(a,new XNetMessage("80 80"));

           Assert.assertEquals("CS Emergency Stop All Reply ",new XNetReply("81 00 81"),r);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method generateReply in XNetSimulatoradapter class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("generateReply execution failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testEmergencyStopLocoReply(){
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", XNetMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in XNetSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           XNetReply r = (XNetReply) generateReplyMethod.invoke(a,new XNetMessage("92 00 02 90"));

           Assert.assertEquals("CS Emergency Specific Loco (XNetV4) Reply ",new XNetReply("01 04 05"),r);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method generateReply in XNetSimulatoradapter class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("generateReply execution failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testEmergencyStopLocoReplyV1V2(){
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", XNetMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in XNetSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           XNetReply r = (XNetReply) generateReplyMethod.invoke(a,new XNetMessage("91 02 93"));

           Assert.assertEquals("CS Emergency Specific Loco (XNetV1,V2) Reply ",new XNetReply("01 04 05"),r);
        } catch(java.lang.IllegalAccessException ite){
             Assert.fail("could not access method generateReply in XNetSimulatoradapter class");
        } catch(java.lang.reflect.InvocationTargetException ite){
             Throwable cause = ite.getCause();
             Assert.fail("generateReply execution failed reason: " + cause.getMessage());
        }
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
