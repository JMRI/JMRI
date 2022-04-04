package jmri.jmrit.display;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test Positionable
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class PositionableTest {

    @Test
    public void testCtor() {
        Positionable.DuplicateIdException e = new Positionable.DuplicateIdException();
        Assert.assertNotNull(e);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
