package jmri.server.json.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.beans.PropertyChangeEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.*;
import jmri.util.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonOperationsSocketServiceTest {

    private JsonMockConnection connection;
    private JsonOperationsSocketService service;
    private ObjectMapper mapper;
    private final Locale locale = Locale.ENGLISH;

    @Test
    public void testOnListCar() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.CAR, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
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
        assertNotNull(message);
        assertEquals( 10, message.size());
    }

    @Test
    public void testGetCar() throws IOException, JmriException, JsonException {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        assertNull(manager.getById("GNWR300005"));
        // get non-existent car
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonOperations.CAR, mapper.createObjectNode().put(JSON.NAME, "GNWR300005"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode());
        assertNull(manager.getById("GNWR300005"));
        // create car
        manager.newRS("GNWR", "300005");
        assertNotNull(manager.getById("GNWR300005"));
        // get existent car
        service.onMessage(JsonOperations.CAR, mapper.createObjectNode().put(JSON.NAME, "GNWR300005"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.CAR, message.path(JSON.TYPE).asText());
        assertEquals( "GNWR300005", message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "", message.path(JSON.DATA).path(JsonOperations.TYPE).asText());
        // change car
        manager.getById("GNWR300005").setTypeName("Boxcar");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.CAR, message.path(JSON.TYPE).asText());
        assertEquals( "GNWR300005", message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "Boxcar", message.path(JSON.DATA).path(JsonOperations.TYPE).asText());
    }

    @Test
    public void testEditCar() throws IOException, JmriException, JsonException {
        CarManager manager = InstanceManager.getDefault(CarManager.class);
        assertNull(manager.getById("GNWR300005"));
        // creates car
        service.onMessage(JsonOperations.CAR,
                mapper.createObjectNode().put(JsonOperations.ROAD, "GNWR").put(JsonOperations.NUMBER, "300005"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        assertNotNull(manager.getById("GNWR300005"));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.CAR, message.path(JSON.TYPE).asText());
        assertEquals( "", message.path(JSON.DATA).path(JsonOperations.TYPE).asText());
        // makes change
        service.onMessage(JsonOperations.CAR,
                mapper.createObjectNode().put(JSON.NAME, "GNWR300005").put(JsonOperations.TYPE, "Boxcar"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( "Boxcar", message.path(JSON.DATA).path(JsonOperations.TYPE).asText());
        // gets external change
        manager.getById("GNWR300005").setTypeName("Flatcar");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( "Flatcar", message.path(JSON.DATA).path(JsonOperations.TYPE).asText());
        // deletes car
        service.onMessage(JsonOperations.CAR, mapper.createObjectNode().put(JSON.NAME, "GNWR300005"),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertNull(manager.getById("GNWR300005"));
    }

    @Test
    public void testOnListEngine() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.ENGINE, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
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
        assertNotNull(message);
        assertEquals( 5, message.size());
    }

    @Test
    public void testGetEngine() throws IOException, JmriException, JsonException {
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        assertNull(manager.getById("GNWR45"));
        // get non-existent car
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonOperations.ENGINE, mapper.createObjectNode().put(JSON.NAME, "GNWR45"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode());
        assertNull(manager.getById("GNWR45"));
        // create car
        manager.newRS("GNWR", "45");
        assertNotNull(manager.getById("GNWR45"));
        // get existent car
        service.onMessage(JsonOperations.ENGINE, mapper.createObjectNode().put(JSON.NAME, "GNWR45"),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.ENGINE, message.path(JSON.TYPE).asText());
        assertEquals( "GNWR45", message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "", message.path(JSON.DATA).path(JsonOperations.MODEL).asText());
        // change car
        manager.getById("GNWR45").setModel("MP15DC");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.ENGINE, message.path(JSON.TYPE).asText());
        assertEquals( "GNWR45", message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "MP15DC", message.path(JSON.DATA).path(JsonOperations.MODEL).asText());
        // capture error messages from using "unknown" engine model
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
    }

    @Test
    public void testEditEngine() throws IOException, JmriException, JsonException {
        EngineManager manager = InstanceManager.getDefault(EngineManager.class);
        assertNull( manager.getById("GNWR45"));
        // creates car
        service.onMessage(JsonOperations.ENGINE,
                mapper.createObjectNode().put(JsonOperations.ROAD, "GNWR").put(JsonOperations.NUMBER, "45"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        assertNotNull(manager.getById("GNWR45"));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.ENGINE, message.path(JSON.TYPE).asText());
        assertEquals( "", message.path(JSON.DATA).path(JsonOperations.MODEL).asText());
        // makes change
        service.onMessage(JsonOperations.ENGINE,
                mapper.createObjectNode().put(JSON.NAME, "GNWR45").put(JsonOperations.MODEL, "MP15DC"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( "MP15DC", message.path(JSON.DATA).path(JsonOperations.MODEL).asText());
        // gets external change
        manager.getById("GNWR45").setModel("");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( "", message.path(JSON.DATA).path(JsonOperations.MODEL).asText());
        // deletes car
        service.onMessage(JsonOperations.ENGINE, mapper.createObjectNode().put(JSON.NAME, "GNWR45"),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertNull(manager.getById("GNWR45"));
        // capture error messages from using "unknown" engine model
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
        JUnitAppender.assertErrorMessage("Rolling stock (GNWR 45) length () is not valid");
    }

    @Test
    public void testOnListLocation() throws IOException, JmriException, JsonException {
        service.onList(JSON.LOCATION, mapper.createObjectNode(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals(3, message.size());
        assertEquals(JSON.LOCATION, message.path(0).path(JSON.TYPE).asText());
        assertEquals("1", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.LOCATION, message.path(1).path(JSON.TYPE).asText());
        assertEquals("3", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.LOCATION, message.path(2).path(JSON.TYPE).asText());
        assertEquals("20", message.path(2).path(JSON.DATA).path(JSON.NAME).asText());
        // add engine and assert new messages sent with 10 engines
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        manager.newLocation("Acme Transfer");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 4, message.size());
    }

    @Test
    public void testGetLocation() throws IOException, JmriException, JsonException {
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        assertNull(manager.getLocationByName("Acme Transfer"));
        // get non-existent location
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JSON.LOCATION, mapper.createObjectNode().put(JSON.NAME, "Acme Transfer"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode());
        assertNull(manager.getLocationByName("Acme Transfer"));
        // create location
        Location location = manager.newLocation("Acme Transfer");
        assertNotNull(location);
        // get existent location
        service.onMessage(JSON.LOCATION, mapper.createObjectNode().put(JSON.NAME, location.getId()),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JSON.LOCATION, message.path(JSON.TYPE).asText());
        assertEquals( location.getId(), message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // change location
        location.setComment("Watch for coyotes");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JSON.LOCATION, message.path(JSON.TYPE).asText());
        assertEquals( location.getId(), message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "Watch for coyotes", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // silence expected error
        JUnitAppender.assertErrorMessage("Unable to get location id [Acme Transfer].");
    }

    @Test
    public void testEditLocation() throws IOException, JmriException, JsonException {
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        assertNull(manager.getLocationByName("Acme Transfer"));
        // creates location
        service.onMessage(JSON.LOCATION, mapper.createObjectNode().put(JSON.USERNAME, "Acme Transfer"),
                new JsonRequest(locale, JSON.V5, JSON.PUT, 42));
        Location location = manager.getLocationByName("Acme Transfer");
        assertNotNull(location);
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JSON.LOCATION, message.path(JSON.TYPE).asText());
        assertEquals( "", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // makes change
        service.onMessage(JSON.LOCATION,
                mapper.createObjectNode().put(JSON.NAME, location.getId()).put(JSON.COMMENT, "Watch for coyotes"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( "Watch for coyotes", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // gets external change
        location.setComment("");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( "", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // deletes location
        service.onMessage(JSON.LOCATION, mapper.createObjectNode().put(JSON.NAME, location.getId()),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertNull(manager.getLocationById(location.getId()));
    }

    @Test
    public void testOnListTrain() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.TRAIN, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals(2, message.size());
        assertEquals(JsonOperations.TRAIN, message.path(0).path(JSON.TYPE).asText());
        assertEquals("1", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JsonOperations.TRAIN, message.path(1).path(JSON.TYPE).asText());
        assertEquals("2", message.path(1).path(JSON.DATA).path(JSON.NAME).asText());
        // add engine and assert new messages sent with 10 engines
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        manager.newTrain("Acme Transfer");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( 3, message.size());
    }

    @Test
    public void testGetTrain() throws IOException, JmriException, JsonException {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        assertNull(manager.getTrainByName("Acme Transfer"));
        // get non-existent train
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JSON.LOCATION, mapper.createObjectNode().put(JSON.NAME, "Acme Transfer"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals( 404, ex.getCode());
        assertNull(manager.getTrainByName("Acme Transfer"));
        // create train
        Train train = manager.newTrain("Acme Transfer");
        assertNotNull(train);
        // get existent location
        service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId()),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.TRAIN, message.path(JSON.TYPE).asText());
        assertEquals( train.getId(), message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // change location
        train.setComment("Watch for coyotes");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.TRAIN, message.path(JSON.TYPE).asText());
        assertEquals( train.getId(), message.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals( "Watch for coyotes", message.path(JSON.DATA).path(JSON.COMMENT).asText());
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
        assertTrue(train.build());
        assertEquals( location1, train.getCurrentRouteLocation().getLocation());
        assertNull(manager.getTrainByName("Acme Transfer"));
        // creates train
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.USERNAME, "Acme Transfer"),
                    new JsonRequest(locale, JSON.V5, JSON.PUT, 42)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode());
        assertNotNull(train);
        service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId()),
                new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonOperations.TRAIN, message.path(JSON.TYPE).asText());
        assertTrue(message.path(JSON.DATA).path(JSON.LOCATION).isEmpty());
        // makes invalid change
        ex = assertThrows( JsonException.class, () ->
            service.onMessage(
                    JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId())
                            .put(JSON.LOCATION, location1.getName()),
                    new JsonRequest(locale, JSON.V5, JSON.POST, 42)),
            "Expected exception not thrown");
        assertEquals( 428, ex.getCode());
        assertEquals( "Unable to move train 1 to location North End Staging.", ex.getMessage());
        // makes valid change
        service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId())
                .put(JSON.LOCATION, location2.getName()), new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( location2.getName(), message.path(JSON.DATA).path(JSON.LOCATION).asText());
        assertEquals( "Test comment for train STF", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // makes ignored change
        service.onMessage(JsonOperations.TRAIN,
                mapper.createObjectNode().put(JSON.NAME, train.getId()).put(JSON.COMMENT, "Watch for coyotes"),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( location2.getName(), message.path(JSON.DATA).path(JSON.LOCATION).asText());
        assertEquals( "Test comment for train STF", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // gets external change
        train.setComment("Watch for coyotes");
        message = connection.getMessage();
        assertNotNull(message);
        assertEquals( "Watch for coyotes", message.path(JSON.DATA).path(JSON.COMMENT).asText());
        // terminates train
        service.onMessage(JsonOperations.TRAIN,
                mapper.createObjectNode().put(JSON.NAME, train.getId()).putNull(JSON.LOCATION),
                new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        message = connection.getMessage();
        assertNotNull(message);
        assertFalse(message.path(JSON.DATA).path(JSON.LOCATION).isNull());
        assertEquals( "", message.path(JSON.DATA).path(JSON.LOCATION).asText());
        // deletes train
        ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonOperations.TRAIN, mapper.createObjectNode().put(JSON.NAME, train.getId()),
                    new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode());
        assertEquals( "Deleting train is not allowed.", ex.getMessage());
        assertNotNull(manager.getTrainById(train.getId()));
    }

    @Test
    public void testOnMessageKernel() throws IOException, JmriException, JsonException {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage(JsonOperations.KERNEL, mapper.createObjectNode().put(JSON.NAME, "non-existant"),
                    new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(404, ex.getCode());
    }

    @Test
    public void testOnListKernel() throws IOException, JmriException, JsonException {
        service.onList(JsonOperations.KERNEL, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals(1, message.size());
        assertEquals(JsonOperations.KERNEL, message.path(0).path(JSON.TYPE).asText());
        JsonNode kernel = message.path(0).path(JSON.DATA);
        assertNotNull(kernel);
        assertEquals(1, kernel.path(JsonOperations.CARS).size());
        assertFalse(kernel.path(JsonOperations.LEAD).isMissingNode());
        assertEquals(kernel.path(JsonOperations.LEAD), kernel.path(JsonOperations.CARS).path(0));
        assertFalse(kernel.path(JSON.WEIGHT).isMissingNode());
        assertEquals(0, kernel.path(JSON.WEIGHT).asInt());
        assertEquals(94, kernel.path(JSON.LENGTH).asInt());
    }

    @Test
    public void testOnMessageInvalidType() throws IOException, JmriException {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onMessage("invalid-type", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 500, ex.getCode());
        ex = assertThrows( JsonException.class, () ->
            service.onMessage("invalid-type", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.DELETE, 0)),
            "Expected exception not thrown");
        assertEquals( 405, ex.getCode());
    }

    @Test
    public void testOnListInvalidType() throws IOException, JmriException {
        JsonException ex = assertThrows( JsonException.class, () ->
            service.onList("invalid-type", NullNode.getInstance(),
                new JsonRequest(locale, JSON.V5, JSON.GET, 0)),
            "Expected exception not thrown");
        assertEquals( 500, ex.getCode());
    }

    @Test
    public void testBeanListenerFailures() {
        InvalidJsonOperationsSocketService mock = new InvalidJsonOperationsSocketService(connection, new JsonOperationsHttpService(mapper));
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        assertEquals( 0, mock.invalidBeanListeners.size());
        assertEquals( 0, train.getPropertyChangeListeners().length);
        InvalidJsonOperationsSocketService.InvalidBeanListener bl = mock.addInvalidBeanListener(train);
        // add listener to test that an IOException removes the listener
        // the listener would normally be added by onMessage, but that method creates
        // a valid listener, and we are not interested in that
        train.addPropertyChangeListener(bl);
        assertEquals( 1, mock.invalidBeanListeners.size());
        assertEquals( 1, train.getPropertyChangeListeners().length);
        // throw JsonException on invalid type
        bl.propertyChange("invalid-type", (PropertyChangeEvent) null);
        assertEquals( 1, mock.invalidBeanListeners.size());
        assertEquals( 1, train.getPropertyChangeListeners().length);
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonException.ERROR, message.path(JSON.TYPE).asText());
        assertEquals( 500, message.path(JSON.DATA).path(JsonException.CODE).asInt());
        assertEquals( "Internal invalid-type handling error. See JMRI logs for information.",
            message.path(JSON.DATA).path(JsonException.MESSAGE).asText());
        JUnitAppender.assertWarnMessageStartsWith("json error sending invalid-type:");
        // throw IOException
        connection.setThrowIOException(true);
        assertEquals( 1, mock.invalidBeanListeners.size());
        assertEquals( 1, train.getPropertyChangeListeners().length);
        bl.propertyChange(JsonOperations.TRAIN, (PropertyChangeEvent) null);
        assertEquals( 0, mock.invalidBeanListeners.size());
        assertEquals( 0, train.getPropertyChangeListeners().length);
    }

    @Test
    public void testManagerListenerFailures() {
        InvalidJsonOperationsSocketService mock = new InvalidJsonOperationsSocketService(connection, new JsonOperationsHttpService(mapper));
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        assertEquals( 0, manager.getPropertyChangeListeners().length);
        // add listener to test that an IOException removes the listener
        // the listener would normally be added by onList, but that method creates
        // a valid listener, and we are not interested in that
        manager.addPropertyChangeListener(mock.invalidBeansListener);
        assertEquals( 1, manager.getPropertyChangeListeners().length);
        // throw JsonException on invalid type
        mock.invalidBeansListener.propertyChange("invalid-type");
        assertEquals( 1, manager.getPropertyChangeListeners().length);
        JsonNode message = connection.getMessage();
        assertNotNull(message);
        assertEquals( JsonException.ERROR, message.path(JSON.TYPE).asText());
        assertEquals( 500, message.path(JSON.DATA).path(JsonException.CODE).asInt());
        assertEquals( "Internal invalid-type handling error. See JMRI logs for information.",
            message.path(JSON.DATA).path(JsonException.MESSAGE).asText());
        JUnitAppender.assertWarnMessageStartsWith("json error sending invalid-type:");
        // throw IOException
        connection.setThrowIOException(true);
        assertEquals( 1, manager.getPropertyChangeListeners().length);
        mock.invalidBeansListener.propertyChange(JsonOperations.TRAIN);
        assertEquals( 0, manager.getPropertyChangeListeners().length);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initIdTagManager();
        JUnitOperationsUtil.setupOperationsTests();
        JUnitOperationsUtil.initOperationsData();
        Kernel kernel = InstanceManager.getDefault(KernelManager.class).newKernel("test1");
        InstanceManager.getDefault(CarManager.class).getById("CP99").setKernel(kernel);
        mapper = new ObjectMapper();
        connection = new JsonMockConnection((DataOutputStream) null);
        service = new JsonOperationsSocketService(connection, new JsonOperationsHttpService(mapper));
    }

    @AfterEach
    public void tearDown() {
        service.onClose();
        service = null;
        connection = null;
        mapper = null;
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    static protected class InvalidJsonOperationsSocketService extends JsonOperationsSocketService {

        protected final HashMap<String, BeanListener<Train>> invalidBeanListeners = new HashMap<>();
        protected final InvalidBeansListener invalidBeansListener = new InvalidBeansListener();

        protected InvalidJsonOperationsSocketService(JsonMockConnection connection, JsonOperationsHttpService service) {
            super(connection, service);
        }

        protected class InvalidBeanListener extends BeanListener<Train> {

            InvalidBeanListener(Train bean) {
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
