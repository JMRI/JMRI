package jmri.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonHttpServiceTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getObjectMapper method, of class JsonHttpService.
     */
    @Test
    public void testGetObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        Assert.assertEquals("get object mapper", mapper, (new JsonTestHttpService(mapper)).getObjectMapper());
    }

}
