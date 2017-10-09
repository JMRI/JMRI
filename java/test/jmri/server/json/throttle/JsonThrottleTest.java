package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonThrottleTest {

    @Test
    @Ignore("need to correctly build string input for readTree call")
    public void testGetThrottle() throws java.io.IOException, jmri.server.json.JsonException {
        JsonMockConnection mc = new JsonMockConnection((DataOutputStream) null);
        ObjectMapper m = new ObjectMapper();
        JsonNode jn = m.readTree("");

        JsonThrottleSocketService ts = new JsonThrottleSocketService(mc);
        JsonThrottle t = JsonThrottle.getThrottle("42",jn,ts);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonThrottleTest.class);

}
