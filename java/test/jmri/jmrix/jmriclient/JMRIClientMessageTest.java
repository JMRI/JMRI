package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JMRIClientMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientMessage class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new JMRIClientMessage(3);
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

}
