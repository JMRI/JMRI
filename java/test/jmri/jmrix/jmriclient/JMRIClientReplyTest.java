package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JMRIClientReplyTest.java
 *
 * Test for the jmri.jmrix.jmriclient.JMRIClientReply class
 *
 * @author Bob Jacobsen
 */
public class JMRIClientReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new JMRIClientReply();
    }

    @After
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
