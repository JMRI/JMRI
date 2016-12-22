package jmri.jmrix.tams;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * TamsTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.tams.TamsTrafficController
 * class
 *
 * @author	Bob Jacobsen
 */
public class TamsTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new TamsTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
