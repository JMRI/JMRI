package jmri.server.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JSONTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testConstructor() {

        // because the constructor throws UnsupportedOperationException, and
        // that is thrown by newInstance() into an InvocationTargetException
        // we pass an InvocationTargetException that is caused by an
        // UnsupportedOperationException and fail everything else.

        InvocationTargetException ex = Assertions.assertThrows( InvocationTargetException.class, () -> {
            Constructor<JSON> constructor;
            constructor = JSON.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Instance of JSON created");
        });
        UnsupportedOperationException cause = Assertions.assertInstanceOf(
            UnsupportedOperationException.class, ex.getCause());
        assertNotNull(cause);
    }

    @Test
    public void testJsonVersions() {
        assertArrayEquals( new String[]{"v5"}, JSON.VERSIONS.toArray(), "JSON protocol versions");
        assertEquals( JSON.JSON_PROTOCOL_VERSION, JSON.V5_PROTOCOL_VERSION, "JSON protocol version is v5");
    }

}
