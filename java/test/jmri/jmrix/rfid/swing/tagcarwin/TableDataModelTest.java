package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Unit tests for the TableDataModel class.
 * @author Steve Young Copyright (C) 2022
 */
public class TableDataModelTest {

    @Test
    public void testCtor() {
        TableDataModel t = new TableDataModel();
        Assertions.assertNotNull(t,"exists");
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
