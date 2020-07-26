package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JMRIClientMessageTest.java
 *
 * Test for the jmri.jmrix.jmriclient.JMRIClientMessage class
 *
 * @author Bob Jacobsen
 */
public class JMRIClientMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new JMRIClientMessage(3);
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
