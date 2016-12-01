package jmri.jmrix.lenz.xnetsimulator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;

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
           XNetReply r = (XNetReply) generateReplyMethod.invoke(a,new XNetMessage("21 21"));

           Assert.assertEquals("CS Version Reply",new XNetReply("63 21 36 00 74"),r);
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
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
