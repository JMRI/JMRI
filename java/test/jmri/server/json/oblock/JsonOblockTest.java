package jmri.server.json.oblock;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonOblockTest {

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
            Constructor<JsonOblock> constructor;
            constructor = JsonOblock.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            Assertions.fail("Instance of JsonOblock created");
        });
        UnsupportedOperationException cause = Assertions.assertInstanceOf(
            UnsupportedOperationException.class, ex.getCause());
        Assertions.assertNotNull(cause);
    }

}
