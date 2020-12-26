package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JMRIClientTrafficControllerTest.java
 *
 * Test for the jmri.jmrix.jmriclient.JMRIClientTrafficController
 * class
 *
 * @author Bob Jacobsen
 */
public class JMRIClientTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new JMRIClientTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
