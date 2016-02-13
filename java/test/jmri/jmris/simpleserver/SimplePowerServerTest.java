//SimplePowerServerTest.java
package jmri.jmris.simpleserver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.simpleserver.SimplePowerServer class
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class SimplePowerServerTest extends TestCase {

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

    // test sending an error message.
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

    // test parsing an ON status message.
    public void testParseOnStatus() {
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               // null output string drops characters
               // could be replaced by one that checks for specific outputs
               @Override
               public void write(int b) throws java.io.IOException {
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
       } catch(jmri.JmriException jmrie){
         Assert.fail("Exception retrieving Status");
       }
    }

    // test parsing an OFF status message.
    public void testParseOffStatus() {
           java.io.DataOutputStream output = new java.io.DataOutputStream(
           new java.io.OutputStream() {
               // null output string drops characters
               // could be replaced by one that checks for specific outputs
               @Override
               public void write(int b) throws java.io.IOException {
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
       } catch(jmri.JmriException jmrie){
         Assert.fail("Exception retrieving Status");
       }
    }


    // from here down is testing infrastructure
    public SimplePowerServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimplePowerServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.simpleserver.SimplePowerServerTest.class);

        return suite;
    }

 

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initDebugPowerManager();
    }

    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }


    private final static Logger log = LoggerFactory.getLogger(SimplePowerServerTest.class.getName());

}
