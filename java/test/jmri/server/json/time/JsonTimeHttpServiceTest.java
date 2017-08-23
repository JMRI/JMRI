package jmri.server.json.time;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonTimeHttpServiceTest {

    @Test
    public void testCTor() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonTimeHttpService t = new JsonTimeHttpService(objectMapper);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JsonTimeHttpServiceTest.class.getName());

}
