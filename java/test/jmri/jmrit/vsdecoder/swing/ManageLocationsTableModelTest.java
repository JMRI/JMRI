package jmri.jmrit.vsdecoder.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ManageLocationsTableModel
 *
 * Based on ThrottlesTableModelTest by Paul Bender
 * @author Klaus Killinger Copyright (C) 2020
 */
public class ManageLocationsTableModelTest {

    @Test
    public void testCtor() {
        ManageLocationsTableModel frame = new ManageLocationsTableModel();
        Assert.assertNotNull("exists", frame);
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
