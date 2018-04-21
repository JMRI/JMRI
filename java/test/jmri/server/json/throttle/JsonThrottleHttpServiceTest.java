package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonThrottleHttpServiceTest {

    @Test
    public void testCTor() {
        ObjectMapper mapper = new ObjectMapper();
        JsonThrottleHttpService t = new JsonThrottleHttpService(mapper);
        Assert.assertNotNull("exists",t);
    }

    /**
     * Test of getObjectMapper method, of class JsonThrottleHttpService.
     */
    @Test
    public void testGetObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        Assert.assertEquals("get object mapper", mapper, (new JsonThrottleHttpService(mapper)).getObjectMapper());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
