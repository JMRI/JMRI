package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of AssignmentTableModel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AssignmentTableModelTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        AssignmentTableModel action = new AssignmentTableModel();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
