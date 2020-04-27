package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleReporterServer class
 *
 * @author Paul Bender
 */
public class SimpleReporterServerTest {

    @Test
    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        assertThat(a).isNotNull();
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
        SimpleReporterServer a = new SimpleReporterServer(jcs);
        assertThat(a).isNotNull();
    }

    @Test
    // test sending a message.
    public void testSendMessage() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            assertThat(sendMessageMethod).isNotNull();
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a,"Hello World");
        });
        assertThat(thrown).withFailMessage("Exception sending Status").isNull();
        assertThat(sb.toString()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
    }

    @Test
    // test sending a message.
    public void testSendMessageWithConnection() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleReporterServer a = new SimpleReporterServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            assertThat(sendMessageMethod).isNotNull();
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a, "Hello World");
        });
        assertThat(thrown).withFailMessage("Exception sending Status").isNull();
        assertThat(jcs.getOutput()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
    }


    @Test
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
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        Throwable thrown = catchThrowable( () -> a.sendErrorStatus("IT1"));
        assertThat(thrown).withFailMessage("Exception sending Error Status").isNull();
        assertThat(sb.toString()).isEqualTo("REPORTER ERROR\n").withFailMessage("sendErrorStatus check");
    }

    @Test
    // test sending a Report message.
    public void testSendReport() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        Throwable thrown = catchThrowable( () -> {
            a.initReporter("IR1");
            a.sendReport("IR1", "Hello World");
        });
        assertThat(thrown).withFailMessage("Exception sending Error Status").isNull();
        assertThat(sb.toString()).isEqualTo("REPORTER IR1 Hello World\n").withFailMessage("sendErrorStatus check");
    }

    @Test
    // test sending an ID tag as a Report message.
    public void testSendIdTagReport() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        Throwable thrown = catchThrowable( () -> {
            a.initReporter("IR1");
            a.sendReport("IR1", new jmri.implementation.DefaultIdTag("ID1234", "Hello World"));
        });
        assertThat(thrown).withFailMessage("Exception sending Error Status").isNull();
        assertThat(sb.toString()).isEqualTo("REPORTER IR1 Hello World\n").withFailMessage("sendErrorStatus check");
    }

    @Test
    // test sending a null Report message.
    public void testSendNullReport() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        Throwable thrown = catchThrowable( () -> {
             a.initReporter("IR1");
             a.sendReport("IR1", null);
        });
        // null report, sends back the reporter name only.
        assertThat(thrown).withFailMessage("Exception sending Error Status").isNull();
        assertThat(sb.toString()).isEqualTo("REPORTER IR1\n").withFailMessage("sendErrorStatus check");
    }

    @Test
    // test parsing a Report message.
    public void testParseStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        Throwable thrown = catchThrowable( () ->
            a.parseStatus("REPORTER IR1 Hello World\n\r"));
        assertThat(thrown).withFailMessage("Exception sending Error Status").isNull();
        assertThat(sb.toString()).isEqualTo("REPORTER IR1 Hello World\n").withFailMessage("sendErrorStatus check");
    }

    @Test
    // test parsing a null Report message.
    public void testParseNullStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        Throwable thrown = catchThrowable( () -> 
            a.parseStatus("REPORTER IR1\n\r"));
        assertThat(thrown).withFailMessage("Exception sending Error Status").isNull();
        assertThat(sb.toString()).isEqualTo("REPORTER IR1\n").withFailMessage("sendErrorStatus check");
    }


    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initReporterManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
