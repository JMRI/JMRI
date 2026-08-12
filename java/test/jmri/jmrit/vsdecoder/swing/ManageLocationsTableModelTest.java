package jmri.jmrit.vsdecoder.swing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ManageLocationsTableModel
 *
 * Based on ThrottlesTableModelTest by Paul Bender
 * @author Klaus Killinger Copyright (C) 2020
 */
public class ManageLocationsTableModelTest {

    // no Ctor test, class only supplies other classes / constants.

    @Test
    public void testManageLocationsTableModelCtors() {

        final Object[][] objects = {};

        var a = new ManageLocationsTableModel.ListenerTableModel(objects);
        assertNotNull( a, "exists");

        var b = new ManageLocationsTableModel.LocationTableModel(objects);
        assertNotNull( b, "exists");

        var c = new ManageLocationsTableModel.ReporterBlockTableModel(objects);
        assertNotNull( c, "exists");
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
