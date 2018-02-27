package jmri.server.json.throttle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonThrottleHttpServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JsonThrottleHttpService t = new JsonThrottleHttpService(mapper);
        Assert.assertNotNull("exists",t);
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
