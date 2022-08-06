package jmri.jmris.simpleserver;

import java.io.*;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServer class 
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleServerTest {

    private SimpleServer ss = null;

    @Test
    public void testCtor() {
        assertThat(ss).isNotNull();
    }

    @Test
    public void testCtorwithParameter() {
        SimpleServer a = new SimpleServer(2048);
        assertThat(a).isNotNull();
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    @Test
    // test sending a message.
    public void testSendMessage() {
        StringBuilder sb = new StringBuilder();
        try (
            DataOutputStream output = new DataOutputStream(
                new OutputStream() {
                    @Override
                    public void write(int b) {
                        sb.append((char)b);
                    }
                });
        ) {
            String code = "LIGHT IL1 OFF\n\r";
            InputStream input = new ByteArrayInputStream(code.getBytes());
            Thread t = new Thread(() -> { 
                try {
                    ss.handleClient(new DataInputStream(input),output);
                } catch( IOException ioe) {
                    // exception expected at end of input.
                }
            });
            t.setName("simpleserver client test thread LIGHT IL1 OFF");
            t.start();
            JUnitUtil.waitFor(()->{ return t.getState() == Thread.State.TERMINATED; }, "simpleserver client test thread not terminated");

        } catch ( IOException ex){ // catching the try #close
            // we just want to continue, so do nothing.
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        ss = new SimpleServer();
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        ss = null;
    }

}
