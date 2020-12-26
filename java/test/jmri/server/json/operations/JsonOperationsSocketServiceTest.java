package jmri.server.json.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitAppender;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

public class JsonOperationsSocketServiceTest {

    private JsonMockConnection connection;
    private JsonOperationsSocketService service;
    private ObjectMapper mapper;
    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testOnListCar() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.CAR, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertEquals(9, message.size());
        assertEquals(JsonOperations.CAR, message.path(0).path(JSON.TYPE).asText());
        assertEquals("CP777", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(1).path(JSON.TYPE).asText());
        assertEquals("CP888", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(2).path(JSON.TYPE).asText());
        assertEquals("CP99", message.path(2).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(3).path(JSON.TYPE).asText());
        assertEquals("CPC10099", message.path(3).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(4).path(JSON.TYPE).asText());
        assertEquals("CPC20099", message.path(4).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(5).path(JSON.TYPE).asText());
        assertEquals("CPX10001", message.path(5).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(6).path(JSON.TYPE).asText());
        assertEquals("CPX10002", message.path(6).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(7).path(JSON.TYPE).asText());
        assertEquals("CPX20001", message.path(7).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.CAR, message.path(8).path(JSON.TYPE).asText());
        assertEquals("CPX20002", message.path(8).path(JSON.DATA).path(JSON.NAME).asText());
        // add car and assert new messages sent with 10 cars
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        manager.newRS("GNWR", "300005");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.size()).isEqualTo(10);
    }

    @Test
    public void testGetCar() throws IOException, JmriException, JsonException {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        assertThat(manager.getById("GNWR300005")).isNull();
        // get non-existent car
        try {
            service.onMessage(JsonOperations.CAR, mapper.createObjectNode().put(JSON.NAME, "GNWR300005"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(404);
        }
        assertThat(manager.getById("GNWR300005")).isNull();
        // create car
        manager.newRS("GNWR", "300005");
        assertThat(manager.getById("GNWR300005")).isNotNull();
        // get existent car
        service.onMessage(JsonOperations.CAR, mapper.createObjectNode().put(JSON.NAME, "GNWR300005"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.CAR);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo("GNWR300005");
        assertThat(message.path(JSON.DATA).path(JsonOperations.CAR_TYPE).asText()).isEqualTo("");
        // change car
        manager.getById("GNWR300005").setTypeName("Boxcar");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.CAR);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo("GNWR300005");
        assertThat(message.path(JSON.DATA).path(JsonOperations.CAR_TYPE).asText()).isEqualTo("Boxcar");
    }

    @Test
    public void testEditCar() throws IOException, JmriException, JsonException {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        assertThat(manager.getById("GNWR300005")).isNull();
        // creates car
        service.onMessage(JsonOperations.CAR,
                mapper.createObjectNode().put(JSON.ROAD, "GNWR").put(JSON.NUMBER, "300005"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        assertThat(manager.getById("GNWR300005")).isNotNull();
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.CAR);
        assertThat(message.path(JSON.DATA).path(JsonOperations.CAR_TYPE).asText()).isEqualTo("");
        // makes change
        service.onMessage(JsonOperations.CAR,
                mapper.createObjectNode().put(JSON.NAME, "GNWR300005").put(JsonOperations.CAR_TYPE, "Boxcar"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JsonOperations.CAR_TYPE).asText()).isEqualTo("Boxcar");
        // gets external change
        manager.getById("GNWR300005").setTypeName("Flatcar");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JsonOperations.CAR_TYPE).asText()).isEqualTo("Flatcar");
        // deletes car
        service.onMessage(JsonOperations.CAR, mapper.createObjectNode().put(JSON.NAME, "GNWR300005"),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertThat(manager.getById("GNWR300005")).isNull();
    }

    @Test
    public void testOnListEngine() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.ENGINE, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertEquals(4, message.size());
        assertEquals(JsonOperations.ENGINE, message.path(0).path(JSON.TYPE).asText());
        assertEquals("PC5016", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.ENGINE, message.path(1).path(JSON.TYPE).asText());
        assertEquals("PC5019", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.ENGINE, message.path(2).path(JSON.TYPE).asText());
        assertEquals("PC5524", message.path(2).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.ENGINE, message.path(3).path(JSON.TYPE).asText());
        assertEquals("PC5559", message.path(3).path(JSON.DATA).path(JSON.NAME).asText());
        // add engine and assert new messages sent with 10 engines
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        manager.newRS("GNWR", "45");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.size()).isEqualTo(5);
    }

    @Test
    public void testGetEngine() throws IOException, JmriException, JsonException {
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        assertThat(manager.getById("GNWR45")).isNull();
        // get non-existent car
        try {
            service.onMessage(JsonOperations.ENGINE, mapper.createObjectNode().put(JSON.NAME, "GNWR45"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(404);
        }
        assertThat(manager.getById("GNWR45")).isNull();
        // create car
        manager.newRS("GNWR", "45");
        assertThat(manager.getById("GNWR45")).isNotNull();
        // get existent car
        service.onMessage(JsonOperations.ENGINE, mapper.createObjectNode().put(JSON.NAME, "GNWR45"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.ENGINE);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo("GNWR45");
        assertThat(message.path(JSON.DATA).path(JSON.MODEL).asText()).isEqualTo("");
        // change car
        manager.getById("GNWR45").setModel("MP15DC");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.ENGINE);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo("GNWR45");
        assertThat(message.path(JSON.DATA).path(JSON.MODEL).asText()).isEqualTo("MP15DC");
        // capture error messages from using "unknown" engine model
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
    }

    @Test
    public void testEditEngine() throws IOException, JmriException, JsonException {
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        assertThat(manager.getById("GNWR45")).isNull();
        // creates car
        service.onMessage(JsonOperations.ENGINE,
                mapper.createObjectNode().put(JSON.ROAD, "GNWR").put(JSON.NUMBER, "45"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        assertThat(manager.getById("GNWR45")).isNotNull();
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.ENGINE);
        assertThat(message.path(JSON.DATA).path(JSON.MODEL).asText()).isEqualTo("");
        // makes change
        service.onMessage(JsonOperations.ENGINE,
                mapper.createObjectNode().put(JSON.NAME, "GNWR45").put(JSON.MODEL, "MP15DC"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.MODEL).asText()).isEqualTo("MP15DC");
        // gets external change
        manager.getById("GNWR45").setModel("");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.MODEL).asText()).isEqualTo("");
        // deletes car
        service.onMessage(JsonOperations.ENGINE, mapper.createObjectNode().put(JSON.NAME, "GNWR45"),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertThat(manager.getById("GNWR45")).isNull();
        // capture error messages from using "unknown" engine model
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
    }

    @Test
    public void testOnListLocation() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.LOCATION, mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertEquals(3, message.size());
        assertEquals(JsonOperations.LOCATION, message.path(0).path(JSON.TYPE).asText());
        assertEquals("1", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.LOCATION, message.path(1).path(JSON.TYPE).asText());
        assertEquals("3", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.LOCATION, message.path(2).path(JSON.TYPE).asText());
        assertEquals("20", message.path(2).path(JSON.DATA).path(JSON.NAME).asText());
        // add engine and assert new messages sent with 10 engines
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        manager.newLocation("Acme Transfer");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.size()).isEqualTo(4);
    }

    @Test
    public void testGetLocation() throws IOException, JmriException, JsonException {
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        assertThat(manager.getLocationByName("Acme Transfer")).isNull();
        // get non-existent location
        try {
            service.onMessage(JsonOperations.LOCATION, mapper.createObjectNode().put(JSON.NAME, "Acme Transfer"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(404);
        }
        assertThat(manager.getLocationByName("Acme Transfer")).isNull();
        // create location
        Location location = manager.newLocation("Acme Transfer");
        assertThat(location).isNotNull();
        // get existent location
        service.onMessage(JsonOperations.LOCATION, mapper.createObjectNode().put(JSON.NAME, location.getId()),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.LOCATION);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo(location.getId());
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("");
        // change location
        location.setComment("Watch for coyotes");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.LOCATION);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo(location.getId());
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("Watch for coyotes");
        // silence expected error
        JUnitAppender.assertErrorMessage("Unable to get location id [Acme Transfer].");
    }

    @Test
    public void testEditLocation() throws IOException, JmriException, JsonException {
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        assertThat(manager.getLocationByName("Acme Transfer")).isNull();
        // creates location
        service.onMessage(JsonOperations.LOCATION, mapper.createObjectNode().put(JSON.USERNAME, "Acme Transfer"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        Location location = manager.getLocationByName("Acme Transfer");
        assertThat(location).isNotNull();
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.LOCATION);
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("");
        // makes change
        service.onMessage(JsonOperations.LOCATION,
                mapper.createObjectNode().put(JSON.NAME, location.getId()).put(JSON.COMMENT, "Watch for coyotes"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("Watch for coyotes");
        // gets external change
        location.setComment("");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("");
        // deletes location
        service.onMessage(JsonOperations.LOCATION, mapper.createObjectNode().put(JSON.NAME, location.getId()),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertThat(manager.getLocationById(location.getId())).isNull();
    }

    @Test
    public void testOnListTrain() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.TRAIN, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertEquals(2, message.size());
        assertEquals(JsonOperations.TRAIN, message.path(0).path(JSON.TYPE).asText());
        assertEquals("1", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.TRAIN, message.path(1).path(JSON.TYPE).asText());
        assertEquals("2", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        // add engine and assert new messages sent with 10 engines
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        manager.newTrain("Acme Transfer");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.size()).isEqualTo(3);
    }

    @Test
    public void testGetTrain() throws IOException, JmriException, JsonException {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        assertThat(manager.getTrainByName("Acme Transfer")).isNull();
        // get non-existent train
        try {
            service.onMessage(JsonOperations.LOCATION, mapper.createObjectNode().put(JSON.NAME, "Acme Transfer"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(404);
        }
        assertThat(manager.getTrainByName("Acme Transfer")).isNull();
        // create train
        Train train = manager.newTrain("Acme Transfer");
        assertThat(train).isNotNull();
        // get existent location
        service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId()),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.TRAIN);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo(train.getId());
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("");
        // change location
        train.setComment("Watch for coyotes");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.TRAIN);
        assertThat(message.path(JSON.DATA).path(JSON.NAME).asText()).isEqualTo(train.getId());
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("Watch for coyotes");
        // silence expected error
        JUnitAppender.assertErrorMessage("Unable to get location id [Acme Transfer].");
    }

    @Test
    public void testEditTrain() throws IOException, JmriException, JsonException {
        JUnitOperationsUtil.loadTrains();
        Location location1 = InstanceManager.getDefault(LocationManager.class).getLocationById("1");
        Location location2 = InstanceManager.getDefault(LocationManager.class).getLocationById("3");
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Train train = manager.getTrainById("1");
        assertThat(train.build()).isTrue();
        assertThat(train.getCurrentRouteLocation().getLocation()).isEqualTo(location1);
        assertThat(manager.getTrainByName("Acme Transfer")).isNull();
        // creates train
        try {
            service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.USERNAME, "Acme Transfer"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(405);
        }
        assertThat(train).isNotNull();
        service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId()),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonOperations.TRAIN);
        assertThat(message.path(JSON.DATA).path(JsonOperations.LOCATION).isEmpty()).isTrue();
        // makes invalid change
        try {
            service.onMessage(
                    JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId())
                            .put(JsonOperations.LOCATION, location1.getName()),
                    new JsonRequest(locale, JSON.V5, JSON.POST, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(428);
            assertThat(ex.getMessage()).isEqualTo("Unable to move train 1 to location North End Staging.");
        }
        // makes valid change
        service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId())
                .put(JsonOperations.LOCATION, location2.getName()), new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JsonOperations.LOCATION).asText()).isEqualTo(location2.getName());
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("Test comment for train STF");
        // makes ignored change
        service.onMessage(JsonOperations.TRAIN,
                mapper.createObjectNode().put(JSON.NAME, train.getId()).put(JSON.COMMENT, "Watch for coyotes"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JsonOperations.LOCATION).asText()).isEqualTo(location2.getName());
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("Test comment for train STF");
        // gets external change
        train.setComment("Watch for coyotes");
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JSON.COMMENT).asText()).isEqualTo("Watch for coyotes");
        // terminates train
        service.onMessage(JsonOperations.TRAIN,
                mapper.createObjectNode().put(JSON.NAME, train.getId()).putNull(JsonOperations.LOCATION),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.DATA).path(JsonOperations.LOCATION).isNull()).isFalse();
        assertThat(message.path(JSON.DATA).path(JsonOperations.LOCATION).asText()).isEmpty();
        // deletes train
        try {
            service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId()),
                    new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(405);
            assertThat(ex.getMessage()).isEqualTo("Deleting train is not allowed.");
        }
        assertThat(manager.getTrainById(train.getId())).isNotNull();
    }

    @Test
    public void testOnMessageKernel() throws IOException, JmriException, JsonException {
        try {
            service.onMessage(JsonOperations.KERNEL, mapper.createObjectNode().put(JSON.NAME, "non-existant"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    @Test
    public void testOnListKernel() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.KERNEL, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertEquals(1, message.size());
        assertEquals(JsonOperations.KERNEL, message.path(0).path(JSON.TYPE).asText());
        JsonNode kernel = message.path(0).path(JSON.DATA);
        assertThat(kernel).isNotNull();
        assertEquals(1, kernel.path(JsonOperations.CARS).size());
        assertFalse(kernel.path(JsonOperations.LEAD).isMissingNode());
        assertEquals(kernel.path(JsonOperations.LEAD), kernel.path(JsonOperations.CARS).path(0));
        assertFalse(kernel.path(JsonOperations.WEIGHT).isMissingNode());
        assertEquals(0, kernel.path(JsonOperations.WEIGHT).asInt());
        assertEquals(94, kernel.path(JSON.LENGTH).asInt());
    }

    @Test
    public void testOnMessageInvalidType() throws IOException, JmriException {
        try {
            service.onMessage("invalid-type", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(500);
        }
        try {
            service.onMessage("invalid-type", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.DELETE, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(405);
        }
    }

    @Test
    public void testOnListInvalidType() throws IOException, JmriException {
        try {
            service.onList("invalid-type", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertThat(ex.getCode()).isEqualTo(500);
        }
    }

    @Test
    public void testBeanListenerFailures() {
        InvalidJsonOperationsSocketService mock = new InvalidJsonOperationsSocketService(connection, new JsonOperationsHttpService(mapper));
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        assertThat(mock.invalidBeanListeners).isEmpty();
        assertThat(train.getPropertyChangeListeners()).isEmpty();
        InvalidJsonOperationsSocketService.InvalidBeanListener bl = mock.addInvalidBeanListener(train);
        // add listener to test that an IOException removes the listener
        // the listener would normally be added by onMessage, but that method creates
        // a valid listener, and we are not interested in that
        train.addPropertyChangeListener(bl);
        assertThat(mock.invalidBeanListeners.size()).isEqualTo(1);
        assertThat(train.getPropertyChangeListeners().length).isEqualTo(1);
        // throw JsonException on invalid type
        bl.propertyChange("invalid-type", (PropertyChangeEvent) null);
        assertThat(mock.invalidBeanListeners.size()).isEqualTo(1);
        assertThat(train.getPropertyChangeListeners().length).isEqualTo(1);
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonException.ERROR);
        assertThat(message.path(JSON.DATA).path(JsonException.CODE).asInt()).isEqualTo(500);
        assertThat(message.path(JSON.DATA).path(JsonException.MESSAGE).asText()).isEqualTo("Internal invalid-type handling error. See JMRI logs for information.");
        JUnitAppender.assertWarnMessageStartingWith("json error sending invalid-type:");
        // throw IOException
        connection.setThrowIOException(true);
        assertThat(mock.invalidBeanListeners.size()).isEqualTo(1);
        assertThat(train.getPropertyChangeListeners().length).isEqualTo(1);
        bl.propertyChange(JsonOperations.TRAIN, (PropertyChangeEvent) null);
        assertThat(mock.invalidBeanListeners).isEmpty();
        assertThat(train.getPropertyChangeListeners()).isEmpty();
    }

    @Test
    public void testManagerListenerFailures() {
        InvalidJsonOperationsSocketService mock = new InvalidJsonOperationsSocketService(connection, new JsonOperationsHttpService(mapper));
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        assertThat(manager.getPropertyChangeListeners()).isEmpty();
        // add listener to test that an IOException removes the listener
        // the listener would normally be added by onList, but that method creates
        // a valid listener, and we are not interested in that
        manager.addPropertyChangeListener(mock.invalidBeansListener);
        assertThat(manager.getPropertyChangeListeners().length).isEqualTo(1);
        // throw JsonException on invalid type
        mock.invalidBeansListener.propertyChange("invalid-type");
        assertThat(manager.getPropertyChangeListeners().length).isEqualTo(1);
        JsonNode message = connection.getMessage();
        assertThat(message).isNotNull();
        assertThat(message.path(JSON.TYPE).asText()).isEqualTo(JsonException.ERROR);
        assertThat(message.path(JSON.DATA).path(JsonException.CODE).asInt()).isEqualTo(500);
        assertThat(message.path(JSON.DATA).path(JsonException.MESSAGE).asText()).isEqualTo("Internal invalid-type handling error. See JMRI logs for information.");
        JUnitAppender.assertWarnMessageStartingWith("json error sending invalid-type:");
        // throw IOException
        connection.setThrowIOException(true);
        assertThat(manager.getPropertyChangeListeners().length).isEqualTo(1);
        mock.invalidBeansListener.propertyChange(JsonOperations.TRAIN);
        assertThat(manager.getPropertyChangeListeners()).isEmpty();
    }

    @BeforeEach
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

    @SuppressWarnings("deprecation")
    @AfterEach
    public void tearDown() {
        service.onClose();
        service = null;
        connection = null;
        mapper = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
    
    protected class InvalidJsonOperationsSocketService extends JsonOperationsSocketService {
        
        protected final HashMap<String, BeanListener<Train>> invalidBeanListeners = new HashMap<>();
        protected final InvalidBeansListener invalidBeansListener = new InvalidBeansListener();

        protected InvalidJsonOperationsSocketService(JsonMockConnection connection, JsonOperationsHttpService service) {
            super(connection, service);
        }

        protected class InvalidBeanListener extends BeanListener<Train> {

            public InvalidBeanListener(Train bean) {
                super(bean);
            }

            public void propertyChange(String type, PropertyChangeEvent evt) {
                super.propertyChange(type, invalidBeanListeners);
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing, use #propertyChange(String, PropertyChangeEvent) in tests
            }

        }
        
        protected InvalidBeanListener addInvalidBeanListener(Train bean) {
            InvalidBeanListener l = new InvalidBeanListener(bean);
            invalidBeanListeners.put(bean.getId(), l);
            return l;
        }
        
        protected class InvalidBeansListener extends ManagerListener<TrainManager> {

            protected InvalidBeansListener() {
                super(InstanceManager.getDefault(TrainManager.class));
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing, use #propertyChange(String) directly in tests
            }
            
        }
    }

}
