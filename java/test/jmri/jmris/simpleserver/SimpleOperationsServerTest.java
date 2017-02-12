//SimpleOperationsServerTest.java
package jmri.jmris.simpleserver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleOperationsServer class
 *
 * @author Paul Bender
 */
public class SimpleOperationsServerTest {
        
    private StringBuilder sb = null;
    private java.io.DataOutputStream output = null;
    private java.io.DataInputStream input = null;

    @Test
    public void testCtor() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        Assert.assertNotNull(a);
    }

    @Test
    public void testConnectionCtor() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleOperationsServer a = new SimpleOperationsServer(jcs);
        Assert.assertNotNull(a);
    }


    // test sending a message.
    @Test public void testSendMessage() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleOperationsServer class. " );
        }

        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           Assert.assertEquals("SendMessage Check","Hello World",sb.toString());
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleOperationsServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
    }

    // test sending a message.
    @Test public void testSendMessageWithConnection() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleOperationsServer a = new SimpleOperationsServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleOperationsServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           Assert.assertEquals("SendMessage Check","Hello World",jcs.getOutput());
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleOperationsServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
    }

    // test sending the train list.
    @Test public void testSendTrainList() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendTrainList();   
        Assert.assertEquals("SendTrainList Check","OPERATIONS , TRAINS=SFF\nOPERATIONS , TRAINS=STF\n",sb.toString());
    }

    // test sending the locations list.
    @Test public void testSendLocationList() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendLocationList();   
        Assert.assertEquals("SendLocationList Check","OPERATIONS , LOCATIONS=North End\nOPERATIONS , LOCATIONS=North Industries\nOPERATIONS , LOCATIONS=South End\n",sb.toString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitOperationsUtil.resetOperationsManager();
        jmri.util.JUnitOperationsUtil.initOperationsData();
        sb = new StringBuilder();
        output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        input = new java.io.DataInputStream(System.in);
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
