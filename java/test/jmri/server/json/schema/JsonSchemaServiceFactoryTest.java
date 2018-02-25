package jmri.server.json.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaServiceFactoryTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testGetTypes() {
        JsonSchemaServiceFactory instance = new JsonSchemaServiceFactory();
        Assert.assertArrayEquals(new String[]{JSON.JSON, JSON.SCHEMA, JSON.TYPES}, instance.getTypes());
    }

    @Test
    public void testGetSocketService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSchemaServiceFactory instance = new JsonSchemaServiceFactory();
        Assert.assertNotNull(instance.getSocketService(connection));
    }

    @Test
    public void testGetHttpService() {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaServiceFactory instance = new JsonSchemaServiceFactory();
        Assert.assertNotNull(instance.getHttpService(mapper));
    }

}
