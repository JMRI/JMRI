package jmri.util;

import java.io.PipedReader;
import java.io.PipedWriter;
import javax.swing.JTextArea;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PipeListenerTest {

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
        t.start();
        wr.write("Test String");
        wr.flush();
        jmri.util.JUnitUtil.waitFor(()->{return !(pr.ready());},"buffer empty");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100); // pause to let the JTextArea catch up.
        Assert.assertEquals("text after character write","Test String",jta.getText());
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
