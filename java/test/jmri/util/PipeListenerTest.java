package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JTextArea;
import java.io.PipedReader;
import java.io.PipedWriter;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PipeListenerTest {

    @Test
    public void testCTor() {
        JTextArea jta = new JTextArea();
        PipedReader pr = new PipedReader();
        PipeListener t = new PipeListener(pr,jta);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testWrite() {
        JTextArea jta = new JTextArea();
        PipedWriter wr = new PipedWriter();
        PipedReader pr = new PipedReader(wr);
        wr.connect(pr);
        PipeListener t = new PipeListener(pr,jta);
        wr.write('a');
        Assert.assertEquals("text after character write","a",jta.getText());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(PipeListenerTest.class.getName());

}
