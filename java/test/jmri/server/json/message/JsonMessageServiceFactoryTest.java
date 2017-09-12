/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.server.json.message;

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
 * @author rhwood
 */
public class JsonMessageServiceFactoryTest {

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
        JsonMessageServiceFactory instance = new JsonMessageServiceFactory();
        Assert.assertArrayEquals(new String[]{JSON.HELLO, JsonMessage.CLIENT}, instance.getTypes());
    }

    @Test
    public void testGetSocketService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonMessageServiceFactory instance = new JsonMessageServiceFactory();
        Assert.assertNotNull(instance.getSocketService(connection));
    }

    @Test
    public void testGetHttpService() {
        ObjectMapper mapper = new ObjectMapper();
        JsonMessageServiceFactory instance = new JsonMessageServiceFactory();
        Assert.assertNull(instance.getHttpService(mapper));
    }

}
