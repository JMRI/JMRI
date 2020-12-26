package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JMRIClientReplyTest.java
 *
 * Test for the jmri.jmrix.jmriclient.JMRIClientReply class
 *
 * @author Bob Jacobsen
 */
public class JMRIClientReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new JMRIClientReply();
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
