package jmri.util;

import java.io.PipedReader;
import java.io.PipedWriter;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
public class PipeListenerTest {

    @Test
    public void testCTor() throws java.io.IOException {
        JTextArea jta = new JTextArea();
        PipedReader pr = new PipedReader();
        PipeListener t = new PipeListener(pr,jta);
        Assert.assertNotNull("exists",t);
    }

    @SuppressWarnings("deprecation")        // Thread.stop()
    @Test
    public void testWrite() throws java.io.IOException {
        JTextArea jta = new JTextArea();
        PipedWriter wr = new PipedWriter();
        PipedReader pr = new PipedReader(wr,1);
        PipeListener t = new PipeListener(pr,jta);
        t.setName("PipeListenerTest thread");
        t.start();

        String testString = "Test String";
        wr.write(testString);
        wr.flush();
        JUnitUtil.waitFor(()->{return !(pr.ready());},"buffer empty");

        JUnitUtil.waitFor(()->{return testString.equals(jta.getText());}, "find text after character write");
        t.stop();
        try {
            t.join();
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    @SuppressWarnings("deprecation")        // Thread.stop()
    @Test
    public void testWriteGuiThread() throws java.io.IOException {
        JTextArea jta = new JTextArea();
        PipedWriter wr = new PipedWriter();
        PipedReader pr = new PipedReader(wr,1);
        PipeListener t = new PipeListener(pr,jta);
        t.setName("PipeListenerTest thread");
        t.start();

        var ref = new jmri.Reference<java.io.IOException>();

        String testString = "Test String";

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            try {
                wr.write(testString);
                wr.flush();
            } catch (java.io.IOException e) {
                ref.set(e);
            }
        });

        if (ref.get() != null) {
            throw ref.get();
        }

        JUnitUtil.waitFor(()->{return !(pr.ready());},"buffer empty");

        JUnitUtil.waitFor(()->{return testString.equals(jta.getText());}, "find text after character write");
        t.stop();
        try {
            t.join();
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PipeListenerTest.class);

}
