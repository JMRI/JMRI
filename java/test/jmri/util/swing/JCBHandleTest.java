package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Bob Jacobsen Copyright 2014
 */
public class JCBHandleTest {

    @Test
    public void testToStringReal() {
        JCBHandle<DummyObject> a = new JCBHandle<>(new DummyObject());
        assertEquals("dummy output", a.toString());
    }

    @Test
    public void testToStringEmpty() {
        JCBHandle<DummyObject> a = new JCBHandle<>("no object");
        assertEquals("no object", a.toString());
    }

    private static class DummyObject {

        @Override
        public String toString() {
            return "dummy output";
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
