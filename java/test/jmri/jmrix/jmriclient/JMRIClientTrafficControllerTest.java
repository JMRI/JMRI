package jmri.jmrix.jmriclient;

import org.junit.After;
import org.junit.Before;

/**
 * JMRIClientTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientTrafficController
 * class
 *
 * @author	Bob Jacobsen
 */
public class JMRIClientTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new JMRIClientTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
