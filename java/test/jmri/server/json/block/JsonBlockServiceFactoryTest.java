package jmri.server.json.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Randall Wood Copyright 2018
 */
public class JsonBlockServiceFactoryTest {

    @Test
    public void testGetTypes() {
        JsonBlockServiceFactory instance = new JsonBlockServiceFactory();
        Assert.assertArrayEquals(new String[]{JsonBlock.BLOCK, JsonBlock.BLOCKS}, instance.getTypes());
    }

    @Test
    public void testGetSocketService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonBlockServiceFactory instance = new JsonBlockServiceFactory();
        Assert.assertNotNull(instance.getSocketService(connection));
    }

    @Test
    public void testGetHttpService() {
        ObjectMapper mapper = new ObjectMapper();
        JsonBlockServiceFactory instance = new JsonBlockServiceFactory();
        Assert.assertNotNull(instance.getHttpService(mapper));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
