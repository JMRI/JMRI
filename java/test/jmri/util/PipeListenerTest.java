package jmri.util;

import java.io.PipedReader;
import java.io.PipedWriter;
import javax.swing.JTextArea;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PipeListenerTest {

    static final int TESTMAXTIME = 10;    // seconds - not too long, so job doesn't hang
    @Rule
    public Timeout globalTimeout = Timeout.seconds(TESTMAXTIME);

    @Test
    public void testCTor() throws java.io.IOException {
        JTextArea jta = new JTextArea();
        PipedReader pr = new PipedReader();
        PipeListener t = new PipeListener(pr,jta);
        Assert.assertNotNull("exists",t);
    }

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

    // private final static Logger log = LoggerFactory.getLogger(PipeListenerTest.class);

}
