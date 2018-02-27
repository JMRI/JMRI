package jmri.server.json.message;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JsonMessageHttpServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCTor() {
        JsonMessageHttpService t = new JsonMessageHttpService(mapper);
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
