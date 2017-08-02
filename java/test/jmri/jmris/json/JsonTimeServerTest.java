package jmri.jmris.json;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
<<<<<<< HEAD
public class JsonTimeServerTest {

    @Test
    public void testCTor() {
=======
public class JsonTimeServerTest extends jmri.jmris.AbstractTimeServerTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
>>>>>>> JMRI/master
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        JsonConnection jc = new JsonConnection(output);
<<<<<<< HEAD
        JsonTimeServer t = new JsonTimeServer(jc);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
=======
        a = new JsonTimeServer(jc);
>>>>>>> JMRI/master
    }

    @After
    public void tearDown() {
<<<<<<< HEAD
=======
        a = null;
>>>>>>> JMRI/master
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonTimeServerTest.class.getName());

}
