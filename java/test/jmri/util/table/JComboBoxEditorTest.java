package jmri.util.table;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
 
/**
 * Tests for the JComboBoxEditorTest class.
 * @author Steve Young Copyright (C) 2024
 */
public class JComboBoxEditorTest {

    @Test
    public void testJComboBoxEditorCtor() {
        javax.swing.JComboBox<String> jcb = new javax.swing.JComboBox<>();
        JComboBoxEditor t = new JComboBoxEditor( jcb, null);
        Assertions.assertNotNull(t);
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
