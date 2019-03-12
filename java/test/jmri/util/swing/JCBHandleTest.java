package jmri.util.swing;

import jmri.util.JUnitUtil;
import org.junit.Test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author	Bob Jacobsen Copyright 2014
 */
public class JCBHandleTest {

    @Test
    public void testToStringReal() {
        JCBHandle<DummyObject> a = new JCBHandle<DummyObject>(new DummyObject());
        Assert.assertEquals("dummy output", a.toString());
    }

    @Test
    public void testToStringEmpty() {
        JCBHandle<DummyObject> a = new JCBHandle<DummyObject>("no object");
        Assert.assertEquals("no object", a.toString());
    }

    class DummyObject {

        @Override
        public String toString() {
            return "dummy output";
        }
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
