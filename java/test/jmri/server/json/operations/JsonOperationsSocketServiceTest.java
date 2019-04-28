package jmri.server.json.operations;

import static org.junit.Assert.fail;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

public class JsonOperationsSocketServiceTest {

    private JsonMockConnection connection;
    private JsonOperationsSocketService service;
    private ObjectMapper mapper;
    private Locale locale = Locale.ENGLISH;

    @Test
    public void testOnListCar() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.CAR, mapper.createObjectNode(), locale, 42);
    }

    @Test
    public void testOnMessageKernel() throws IOException, JmriException, JsonException {
        try {
            service.onMessage(JsonOperations.KERNEL, mapper.createObjectNode().put(JSON.NAME, "non-existant"), JSON.GET,
                    locale, 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            if (ex.getCode() != 404) {
                throw ex;
            }
        }
    }

    @Test
    public void testOnListKernel() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.KERNEL, mapper.createObjectNode(), locale, 42);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initIdTagManager();
        JUnitOperationsUtil.setupOperationsTests();
        JUnitOperationsUtil.initOperationsData();
        Kernel kernel = InstanceManager.getDefault(CarManager.class).newKernel("test1");
        InstanceManager.getDefault(CarManager.class).getById("CP99").setKernel(kernel);
        mapper = new ObjectMapper();
        connection = new JsonMockConnection((DataOutputStream) null);
        service = new JsonOperationsSocketService(connection, new JsonOperationsHttpService(mapper));
    }

    @After
    public void tearDown() {
        service = null;
        connection = null;
        mapper = null;
        JUnitUtil.tearDown();
    }
}