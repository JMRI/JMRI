package jmri.jmris.simpleserver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimplePowerServer class
 *
 * @author Paul Bender
 */
public class SimplePowerServerTest{

   @Test 
    public void testCtorFailure() {
        jmri.util.JUnitUtil.resetInstanceManager(); // remove the debug power manager for this test only.
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);

        SimplePowerServer a = new SimplePowerServer(input, output);

        jmri.util.JUnitAppender.assertErrorMessage("No power manager instance found");
        Assert.assertNotNull(a);
    }

   @Test 
    public void testCtorSuccess() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);

        SimplePowerServer a = new SimplePowerServer(input, output);

        Assert.assertNotNull(a);
    }

   @Test 
    public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimplePowerServer a = new SimplePowerServer(jcs);

        Assert.assertNotNull(a);
    }

    // test sending an error message.
   @Test 
    public void testSendErrorStatus() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          a.sendErrorStatus();
          Assert.assertEquals("sendErrorStatus check","POWER ERROR\n",sb.toString());
       } catch(java.io.IOException ioe){
         Assert.fail("Exception sending Error Status");
       }
    }

    // test sending an ON status message.
   @Test 
    public void testSendOnStatus() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          a.sendStatus(jmri.PowerManager.ON);
          Assert.assertEquals("send ON Status check","POWER ON\n",sb.toString());
       } catch(java.io.IOException ioe){
         Assert.fail("Exception sending On Status");
       }
    }

    // test sending an OFF status message.
   @Test 
    public void testSendOffStatus() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          a.sendStatus(jmri.PowerManager.OFF);
          Assert.assertEquals("send OFF Status check","POWER OFF\n",sb.toString());
       } catch(java.io.IOException ioe){
         Assert.fail("Exception sending Off Status");
       }
    }

    // test sending an UNKNOWN status message.
   @Test 
    public void testSendUnknownStatus() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          a.sendStatus(-1);
          Assert.assertEquals("send UNKNOWN status check","POWER UNKNOWN\n",sb.toString());
       } catch(java.io.IOException ioe){
         Assert.fail("Exception sending Unknown Status");
       }
    }

    // test sending a status string.
   @Test 
    public void testSendStatusString() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          a.sendStatus("Hello World\n");
          Assert.assertEquals("send status string","Hello World\n",sb.toString());
       } catch(java.io.IOException ioe){
         Assert.fail("Exception sending Unknown Status");
       }
    }

    // test sending a status string.
   @Test 
    public void testSendStatusStringWithConnection() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
       SimplePowerServer a = new SimplePowerServer(jcs);
       try {
          a.sendStatus("Hello World\n");
          Assert.assertEquals("send status string","Hello World\n",jcs.getOutput());
       } catch(java.io.IOException ioe){
         Assert.fail("Exception sending Unknown Status");
       }
    }

    // test parsing an ON status message.
   @Test 
    public void testParseOnStatus() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          a.parseStatus("POWER ON\n");
          Assert.assertEquals("Parse On Status Check", 
                       jmri.InstanceManager
                           .getDefault(jmri.PowerManager.class).getPower(),
                       jmri.PowerManager.ON);
          Assert.assertEquals("status as a result of parsing on","POWER ON\n",sb.toString());
       } catch(jmri.JmriException jmrie){
         Assert.fail("Exception retrieving Status");
       }
    }

    // test parsing an OFF status message.
   @Test 
    public void testParseOffStatus() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          a.parseStatus("POWER OFF\n");
          Assert.assertEquals("Parse OFF Status Check", 
                       jmri.InstanceManager
                           .getDefault(jmri.PowerManager.class).getPower(),
                       jmri.PowerManager.OFF);
          Assert.assertEquals("status as a result of parsing off","POWER OFF\n",sb.toString());
       } catch(jmri.JmriException jmrie){
         Assert.fail("Exception retrieving Status");
       }
    }

   @Ignore("Not ready yet")
   @Test 
   // test parsing an OFF status message.
    public void testParseBadStatus() {
           StringBuilder sb = new StringBuilder();
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               @Override
               public void write(int b) throws java.io.IOException {
                   sb.append((char)b);
               }
          });
       java.io.DataInputStream input = new java.io.DataInputStream(System.in);
       SimplePowerServer a = new SimplePowerServer(input,output);
       try {
          // this should just trigger an error message sent to the client.
          a.parseStatus("POWER FFO\n");
          Assert.assertEquals("error from bad parse","POWER ERROR\n",sb.toString());
       } catch(jmri.JmriException jmrie){
         Assert.fail("Exception retrieving Status");
       }
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
        jmri.util.JUnitUtil.initDebugPowerManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
