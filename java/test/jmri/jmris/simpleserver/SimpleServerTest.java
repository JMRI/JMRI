//SimpleServerTest.java
package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServer class 
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleServerTest {

    @Test
    public void testCtor() {
        SimpleServer a = new SimpleServer();
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    @Test
    public void testCtorwithParameter() {
        SimpleServer a = new SimpleServer(2048);
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    @Test
    // test sending a message.
    public void testSendMessage() {
        SimpleServer a = new SimpleServer();
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        String code = "LIGHT IL1 OFF\n\r";
        java.io.InputStream input = new java.io.ByteArrayInputStream(code.getBytes());
        Thread t = new Thread(() -> { 
            try{
               a.handleClient(new java.io.DataInputStream(input),output); }
            catch(java.io.IOException ioe){
               // exception expected at end of input.
               return;
            }
            });
        t.setName("simpleserver client test thread");
        t.start();
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
