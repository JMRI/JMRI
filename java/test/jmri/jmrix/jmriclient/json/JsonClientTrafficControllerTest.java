package jmri.jmrix.jmriclient.json;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JsonClientTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.json.JsonClientTrafficController
 * class
 *
 * @author	Bob Jacobsen
 */
public class JsonClientTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new JsonClientTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
