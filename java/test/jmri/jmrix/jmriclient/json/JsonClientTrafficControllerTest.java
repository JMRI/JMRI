package jmri.jmrix.jmriclient.json;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
        apps.tests.Log4JFixture.tearDown();
    }

}
